# Code Highlighting Implementation for PyCharm Extension

## Overview

The PyCharm extension now supports code highlighting to show which lines have been modified by peers in real-time, matching the functionality available in VSCode and Emacs extensions.

## How It Works

### Data Flow

1. **User opens a file** → PyCharm sends `req:code:active-path` to Gardener
2. **Gardener processes file** → Compares local version with peers' versions
3. **Gardener calculates highlights** → Generates `hl` array with 0-based line numbers
4. **Gardener responds** → Sends `res:code:active-path` with `hl` data
5. **PyCharm applies highlights** → ActivePathHandler receives response and applies highlights

### Message Format

```json
{
  "flow": "res",
  "domain": "code",
  "action": "active-path",
  "caw": "client-guid",
  "data": {
    "hl": [0, 4, 7, 25, 28],  // 0-based line numbers to highlight
    "root": "/path/to/project",
    "activePath": "src/main.py",
    "agg": { /* aggregated peer changes */ },
    // ... other project data
  }
}
```

## Implementation Components

### 1. ActivePathHandler

**Location**: `src/main/java/com/codeawareness/pycharm/events/handlers/ActivePathHandler.java`

**Purpose**: Handles `code:active-path` response messages from Gardener and applies highlights

**Key Features**:
- Parses the `hl` JSON array from response data
- Extracts 0-based line numbers
- Gets the currently active file from ProjectService
- Clears old highlights before applying new ones
- Delegates actual highlight rendering to HighlightManager

**Code Flow**:
```java
handle(Message message)
  → Check flow is RES
  → Extract hl array from data
  → Parse line numbers
  → Get active file path
  → highlightManager.clearHighlights(filePath)
  → Loop through line numbers:
      highlightManager.addHighlight(filePath, lineNumber)
```

### 2. HighlightManager (Already Existed)

**Location**: `src/main/java/com/codeawareness/pycharm/highlighting/HighlightManager.java`

**Purpose**: Manages IntelliJ's highlighting API to render line highlights

**Key Features**:
- Uses IntelliJ's `RangeHighlighter` API
- Applies full-width line highlights (using `HighlighterTargetArea.LINES_IN_RANGE`)
- Theme-aware coloring via `ColorSchemeProvider`
- Tracks highlighters per file for cleanup
- Thread-safe with UI thread invocation

**Highlight Properties**:
- Layer: `HighlighterLayer.SELECTION - 1` (just below selection)
- Target: `LINES_IN_RANGE` (full-width highlighting)
- Color: Light blue for light themes, dark blue for dark themes

### 3. ColorSchemeProvider

**Location**: `src/main/java/com/codeawareness/pycharm/highlighting/ColorSchemeProvider.java`

**Purpose**: Provides theme-aware colors for highlights

**Colors**:
- Light theme: `#00b1a420` (light cyan/blue with transparency)
- Dark theme: `#03445f` (darker blue)

## Registration in CodeAwarenessProjectService

The `ActivePathHandler` is registered alongside other event handlers:

```java
EventHandler activePathHandler = new ActivePathHandler(project, highlightManager);
appService.getEventDispatcher().registerHandler(activePathHandler);
registeredHandlers.add(activePathHandler);  // For cleanup on dispose
```

## IntelliJ Highlighting API

IntelliJ provides powerful highlighting capabilities through the `MarkupModel`:

### Key Classes

1. **`MarkupModel`**: Editor's markup layer where highlights are added
   - Obtained via `editor.getMarkupModel()`

2. **`RangeHighlighter`**: Individual highlight instance
   - Created via `markupModel.addRangeHighlighter()`
   - Must be disposed when no longer needed

3. **`TextAttributes`**: Visual properties (background color, foreground, effects)

4. **`HighlighterTargetArea`**: Defines highlight scope
   - `LINES_IN_RANGE`: Full-width line highlighting (what we use)
   - `EXACT_RANGE`: Highlight only specific text range

5. **`HighlighterLayer`**: Z-order for overlapping highlights
   - `SELECTION - 1`: Just below selection highlighting

### Line Number Conversion

**Important**: Both Gardener and IntelliJ use **0-based line numbers**, so no conversion is needed!

- Gardener `hl` array: `[0, 4, 7]` means lines 1, 5, 8 in the editor (human readable)
- IntelliJ API: `document.getLineStartOffset(0)` gets line 1
- **No +1/-1 adjustments required** ✅

### Empty Line Handling

IntelliJ handles empty lines correctly out-of-the-box when using `HighlighterTargetArea.LINES_IN_RANGE`:
- Empty lines get full-width highlighting automatically
- No special handling needed (unlike Emacs which required hl-line technique)

## Comparison with VSCode and Emacs

| Feature | VSCode | Emacs | PyCharm |
|---------|--------|-------|---------|
| Data Source | `project.hl` | `data.hl` | `data.hl` |
| Line Numbers | 0-based (API is 1-based) | 0-based → 1-based conversion | 0-based (matches API) |
| Highlighting API | Decorations | Overlays | RangeHighlighter |
| Empty Lines | Supported | Special handling needed | Automatic |
| Theme Support | Built-in | Manual detection | Built-in (JBColor) |
| Full-width | `isWholeLine: true` | `hl-line` technique | `LINES_IN_RANGE` |
| Z-order | Decoration type | Overlay priority | HighlighterLayer |

## Configuration

### Colors

Colors are defined in `ColorSchemeProvider.getHighlightJBColor()`:
- Light: `new Color(0, 177, 164, 32)` - rgba(0, 177, 164, 0.125)
- Dark: `new Color(3, 68, 95)` - rgb(3, 68, 95)

### Enable/Disable

Highlights can be toggled:
```java
highlightManager.setHighlightsEnabled(false);  // Hide all
highlightManager.setHighlightsEnabled(true);   // Show all
```

## Testing

### Manual Testing Steps

1. **Start Gardener** with a test project
2. **Open PyCharm** with the Kawa extension installed
3. **Open a file** that has peer changes
4. **Verify**:
   - Highlights appear on changed lines
   - Colors match theme (light/dark)
   - Full-width highlighting works
   - Empty lines are highlighted correctly

### Expected Behavior

- Highlights appear automatically when file is opened
- Highlights update when switching files
- Highlights clear when peer unselected
- Highlights persist across editor tab switches
- No performance degradation with many highlights

### Debug Logging

Check IntelliJ logs for:
```
[Code Awareness] ActivePathHandler.handle() called for project: XXX
[Code Awareness] Received N highlight lines for project: XXX
[Code Awareness] Applying highlights to file: /path/to/file
[Code Awareness] Applied N highlights to: /path/to/file
[Code Awareness] Added highlight: /path/to/file:LineNumber
```

## Known Limitations

1. **Editor must be open**: Highlights only appear in open editors
   - Files not currently open won't show highlights
   - This matches VSCode/Emacs behavior

2. **Per-project highlights**: Each project maintains separate highlights
   - Multi-project support via EventDispatcher list handling

3. **Theme colors are fixed**: Not yet configurable by user
   - Colors match VSCode defaults
   - Future: Add settings for custom colors

## Future Enhancements

1. **Gutter indicators**: Add symbols in the line number gutter
2. **Hover tooltips**: Show peer name/changes on hover
3. **Animation**: Flash effect when highlights change (like VSCode)
4. **Configurable colors**: User settings for highlight colors
5. **Diff markers**: Different colors for additions vs deletions
6. **Performance optimization**: Batch highlight operations for large files

## Troubleshooting

### Highlights don't appear

1. **Check logs** for ActivePathHandler messages
2. **Verify Gardener is running** and connected
3. **Confirm file has peer changes** (check Gardener logs for `hl` array)
4. **Check if highlights enabled**: `highlightManager.isHighlightsEnabled()`

### Highlights show wrong lines

1. **Verify line number base**: Should be 0-based
2. **Check document line count**: Ensure file hasn't changed
3. **Review Gardener hl array**: Confirm numbers are correct

### Highlights don't clear

1. **Check handler cleanup**: Verify `dispose()` is called
2. **Look for orphaned highlighters**: Check `highlightersByFile` map
3. **Verify editor is still valid**: File may have been closed

## Files Modified/Created

### New Files

1. `src/main/java/com/codeawareness/pycharm/events/handlers/ActivePathHandler.java`

### Modified Files

1. `src/main/java/com/codeawareness/pycharm/CodeAwarenessProjectService.java`
   - Added import for `ActivePathHandler`
   - Registered `ActivePathHandler` in `registerEventHandlers()`
   - Added to `registeredHandlers` list for cleanup

### Existing Files (Already Implemented)

1. `src/main/java/com/codeawareness/pycharm/highlighting/HighlightManager.java`
2. `src/main/java/com/codeawareness/pycharm/highlighting/ColorSchemeProvider.java`

## Build Status

✅ Code compiles successfully with `./gradlew compileJava`

## Integration with Existing Features

### Works with:
- ✅ Diff mode (highlights + diff viewer)
- ✅ Peer selection (highlights update on peer change)
- ✅ Branch switching (highlights refresh)
- ✅ Multi-project support (each project has own highlights)

### Complements:
- **File monitoring**: ActiveFileListener triggers active-path requests
- **Peer selection**: PeerSelectHandler updates context, highlights follow
- **Diff viewer**: Shows detailed changes, highlights show overview

## Summary

The highlighting implementation brings PyCharm to feature parity with VSCode and Emacs extensions. It leverages IntelliJ's robust `RangeHighlighter` API to provide:

- ✅ Real-time visual feedback of peer changes
- ✅ Theme-aware colors
- ✅ Full-width line highlighting
- ✅ Automatic empty line handling
- ✅ Multi-project support
- ✅ Proper cleanup and memory management

The implementation is clean, maintainable, and follows IntelliJ platform best practices.
