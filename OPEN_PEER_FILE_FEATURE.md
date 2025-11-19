# Open Peer File Feature Implementation

## Overview

The PyCharm extension now supports the `code:open-peer-file` event, allowing users to open files requested through the Muninn (Kawa Code) desktop application. This feature brings PyCharm to feature parity with VSCode and Emacs extensions.

## How It Works

### User Flow

1. User clicks on a file in Muninn (Kawa Code desktop app)
2. Muninn sends a request to Gardener to open the file
3. Gardener checks if the file exists locally or needs to be downloaded from a peer
4. Gardener sends `res:code:open-peer-file` to PyCharm with file path(s)
5. PyCharm opens the file or shows a diff view

### Two Scenarios

#### Scenario 1: File Exists Locally
When the file exists in the user's local repository:
- **Gardener sends**: `{ exists: true, filePath: "/path/to/file" }`
- **PyCharm action**: Opens the file directly in the editor
- **User sees**: The file opened in a new editor tab

#### Scenario 2: File Doesn't Exist Locally (Peer File)
When the file only exists in a peer's working copy:
- **Gardener sends**: `{ exists: false, filePath: "/path/to/peer/file", emptyFilePath: "/path/to/empty/file", peerId: "user123" }`
- **PyCharm action**: Opens a diff view comparing empty file vs peer's version
- **User sees**: A diff viewer showing what the peer added (similar to viewing a new file in a PR)

## Message Format

### Response from Gardener

```json
{
  "flow": "res",
  "domain": "code",
  "action": "open-peer-file",
  "caw": "client-guid",
  "data": {
    "exists": true|false,
    "filePath": "/absolute/path/to/file",
    "emptyFilePath": "/absolute/path/to/empty/file",  // Only when exists=false
    "peerId": "user-id",                               // Only when exists=false
    "content": "file contents..."                      // Not used by PyCharm
  }
}
```

### Error Response

```json
{
  "flow": "err",
  "domain": "code",
  "action": "open-peer-file",
  "err": "Error message"
}
```

## Implementation Details

### OpenPeerFileHandler

**Location**: `src/main/java/com/codeawareness/pycharm/events/handlers/OpenPeerFileHandler.java`

**Key Methods**:

1. **`handle(Message message)`**
   - Validates message is a RES flow
   - Extracts data from JSON response
   - Routes to appropriate method based on `exists` flag

2. **`openFileInEditor(String filePath)`**
   - Called when `exists = true`
   - Uses `LocalFileSystem` to get VirtualFile
   - Opens file using `FileEditorManager.openTextEditor()`
   - Runs on EDT (Event Dispatch Thread) via `invokeLater()`

3. **`openDiffView(String emptyFilePath, String peerFilePath, String peerId)`**
   - Called when `exists = false`
   - Delegates to existing `DiffViewerManager.showDiff()`
   - Shows diff with title "New File (Peer: {peerId})"

### IntelliJ APIs Used

1. **FileEditorManager**: Opens files in the editor
   ```java
   FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
   ```

2. **LocalFileSystem**: Resolves file paths to VirtualFiles
   ```java
   LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
   ```

3. **OpenFileDescriptor**: Wraps VirtualFile for opening
   ```java
   OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
   ```

4. **ApplicationManager.invokeLater()**: Ensures UI operations run on EDT
   ```java
   ApplicationManager.getApplication().invokeLater(() -> { /* UI code */ });
   ```

## Comparison with VSCode/Emacs

| Feature | VSCode | Emacs | PyCharm |
|---------|--------|-------|---------|
| Open local file | ✅ `vscode.open` | ✅ `find-file-noselect` | ✅ `openTextEditor` |
| Diff new file | ✅ `vscode.diff` | ✅ `ediff` | ✅ `DiffViewerManager` |
| Read-only peer files | ⚠️ Not implemented | ✅ `buffer-read-only` | ⚠️ Not implemented |
| Header indicator | ❌ None | ✅ "Peer file (read-only)" | ❌ None |

### Future Enhancement: Read-Only Peer Files

Currently, PyCharm doesn't mark peer files as read-only. This could be added:

```java
// After opening the file
Document document = fileEditorManager.getSelectedTextEditor().getDocument();
document.setReadOnly(true);

// Add visual indicator
// Could use editor notifications or status bar
```

## Registration

The handler is registered in `CodeAwarenessProjectService.registerEventHandlers()`:

```java
EventHandler openPeerFileHandler = new OpenPeerFileHandler(project, diffViewerManager);
appService.getEventDispatcher().registerHandler(openPeerFileHandler);
registeredHandlers.add(openPeerFileHandler);
```

This ensures:
- Handler receives all `code:open-peer-file` messages
- Handler is properly cleaned up when project closes
- Each project has its own handler instance (multi-project support)

## Testing

### Manual Testing Steps

1. **Start Gardener** and connect PyCharm
2. **Open Muninn** (Kawa Code desktop app)
3. **Click on a file** in the Muninn file tree
4. **Verify**:
   - Local files open directly in editor
   - Peer-only files open in diff view
   - Correct file is opened
   - No errors in logs

### Expected Behavior

**Local File:**
- File opens in new editor tab
- Can edit immediately
- Normal PyCharm file editor

**Peer File (doesn't exist locally):**
- Diff viewer opens
- Left side: Empty file
- Right side: Peer's version
- Title shows: "New File (Peer: {username})"

### Debug Logging

Check IntelliJ logs for:
```
[Code Awareness] OpenPeerFileHandler.handle() called for project: XXX
[Code Awareness] Opening peer file: /path/to/file (exists locally: true/false, peerId: XXX)
[Code Awareness] Opened file in editor: /path/to/file
[Code Awareness] Opened diff view for peer file
```

## Error Handling

### File Not Found
```java
if (!file.exists()) {
    Logger.warn("File does not exist: " + filePath);
    return;  // Gracefully fail, log warning
}
```

### Invalid VirtualFile
```java
if (virtualFile == null) {
    Logger.warn("Could not find virtual file: " + filePath);
    return;
}
```

### General Exceptions
```java
catch (Exception e) {
    Logger.error("Failed to open file: " + filePath, e);
}
```

All errors are logged but don't crash the plugin. User sees no file opened if error occurs.

## Known Limitations

1. **No read-only indicator**: Peer files are not marked as read-only
   - User could theoretically edit them (changes would be lost)
   - VSCode has same limitation
   - Emacs properly marks as read-only

2. **No visual distinction**: Peer files look like local files
   - Could add header notification
   - Could use different tab color
   - Could show icon in tab

3. **File paths must be absolute**: Relies on Gardener providing absolute paths
   - This is consistent with VSCode/Emacs
   - Relative paths would fail

## Future Enhancements

1. **Read-only peer files**
   ```java
   if (!exists) {
       Document doc = FileDocumentManager.getInstance().getDocument(virtualFile);
       if (doc != null) {
           doc.setReadOnly(true);
       }
   }
   ```

2. **Editor notification header**
   ```java
   EditorNotifications.getInstance(project).updateNotifications(virtualFile);
   ```

3. **Navigate to specific line**
   - Accept line number in message data
   - Scroll to that line after opening

4. **Batch file opening**
   - Support multiple files in one message
   - Open all in tabs

## Files Modified/Created

### New Files
1. `src/main/java/com/codeawareness/pycharm/events/handlers/OpenPeerFileHandler.java`

### Modified Files
1. `src/main/java/com/codeawareness/pycharm/CodeAwarenessProjectService.java`
   - Added import for `OpenPeerFileHandler`
   - Registered handler in `registerEventHandlers()`
   - Added to cleanup list

## Build Status

✅ Code compiles successfully with `./gradlew compileJava`

## Integration Points

### Works with:
- ✅ Muninn desktop app (primary use case)
- ✅ Existing diff viewer infrastructure
- ✅ Multi-project support
- ✅ File monitoring system

### Complements:
- **Peer selection**: Can open files from selected peer
- **Diff mode**: Can view detailed changes
- **Highlighting**: Shows which lines changed
- **File tree**: Muninn shows all peer files

## Summary

The `code:open-peer-file` feature enables seamless file navigation from the Muninn desktop app to PyCharm. It:

- ✅ Opens local files directly in the editor
- ✅ Shows diff view for peer-only files
- ✅ Reuses existing diff viewer infrastructure
- ✅ Properly handles errors and edge cases
- ✅ Supports multi-project setups
- ✅ Matches VSCode/Emacs functionality

The implementation is clean, maintainable, and follows IntelliJ platform best practices.
