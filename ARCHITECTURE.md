# Code Awareness PyCharm Plugin - Architecture Overview

 

## Executive Summary

 

This document outlines the architecture for the **Code Awareness PyCharm Plugin**, a JetBrains IDE extension that provides real-time team collaboration features. This plugin replicates the functionality of the existing Code Awareness extensions for Emacs and VS Code, enabling PyCharm users to see code intersections, conflicts, and overlaps with teammates before committing changes.

 

**Key Features:**

- Real-time peer code highlighting (conflicts, overlaps, modifications)

- Side-by-side diff viewing with teammate code

- Branch comparison capabilities

- Low-noise visual indicators in the editor

- Cross-platform socket-based communication with Code Awareness backend

 

---

 

## 1. System Overview

 

### 1.1 Purpose

 

The Code Awareness PyCharm plugin enables developers to:

- **Identify merge conflicts early** - See conflicts before pushing/committing

- **Navigate peer code instantly** - Travel between working copies without git operations

- **Minimize interruptions** - "Low-noise" design with non-intrusive visual indicators

- **Collaborate seamlessly** - Work across IDEs (Emacs, VSCode, PyCharm) through shared backend

 

### 1.2 High-Level Architecture

 

```

┌─────────────────────────────────────────────────────────────────┐

│                  PyCharm Code Awareness Plugin                  │

├─────────────────────────────────────────────────────────────────┤

│                                                                  │

│  ┌────────────────────────────────────────────────────────────┐ │

│  │              Plugin Entry Point & Services                  │ │

│  │  - ApplicationService (Singleton)                           │ │

│  │  - ProjectService (Per-project state)                       │ │

│  └────────────────────────────────────────────────────────────┘ │

│         ↓            ↓             ↓             ↓               │

│  ┌────────────────────────────────────────────────────────────┐ │

│  │ Communication │ Event      │ Highlighting │ UI              │ │

│  │ Layer         │ Handlers   │ System       │ Integration    │ │

│  └────────────────────────────────────────────────────────────┘ │

│         ↓            ↓             ↓             ↓               │

│  ┌────────────────────────────────────────────────────────────┐ │

│  │          IntelliJ Platform APIs                             │ │

│  │  - VirtualFileSystem (VFS)                                  │ │

│  │  - Message Bus                                              │ │

│  │  - Editor API (Highlighting, RangeHighlighter)              │ │

│  │  - Application.executeOnPooledThread                        │ │

│  └────────────────────────────────────────────────────────────┘ │

│                            ↓                                     │

│  ┌────────────────────────────────────────────────────────────┐ │

│  │      Socket Communication (Unix/Windows Named Pipes)        │ │

│  │  - Catalog Service Connection                               │ │

│  │  - Main IPC Service Connection                              │ │

│  └────────────────────────────────────────────────────────────┘ │

│                            ↓                                     │

│  ┌────────────────────────────────────────────────────────────┐ │

│  │         Code Awareness Backend Application                  │ │

│  │  - Catalog Service (client discovery)                       │ │

│  │  - Main Service (Muninn, Gardener, repository management)   │ │

│  └────────────────────────────────────────────────────────────┘ │

│                                                                  │

└─────────────────────────────────────────────────────────────────┘

```

 

---

 

## 2. Core Components

 

### 2.1 Communication Layer

 

**Responsibilities:**

- Manage socket connections to Code Awareness backend

- Implement JSON message protocol with form-feed delimiters

- Handle connection lifecycle (connect, register, authenticate, disconnect)

- Manage asynchronous request/response handling

 

**Key Classes:**

- `CatalogConnectionService` - Manages catalog service connection

- `IpcConnectionService` - Manages main IPC service connection

- `MessageProtocol` - JSON serialization/deserialization

- `SocketManager` - Cross-platform socket handling (Unix/Windows)

- `ResponseHandlerRegistry` - Manages one-time response handlers

 

**Implementation Details:**

 

```java

// Socket paths

Unix/Linux/macOS: ~/.kawa-code/sockets/caw.{catalog|<GUID>}

Windows: \\.\pipe\caw.{catalog|<GUID>}

 

// Message format (JSON with form-feed delimiter)

{

  "flow": "req|res|err",

  "domain": "code|auth|*",

  "action": "action_name",

  "data": {...},

  "caw": "client_guid"

}\f

```

 

**Connection Sequence:**

1. Generate unique GUID for PyCharm instance

2. Connect to catalog service (`~/.kawa-code/sockets/caw.catalog`)

3. Register client with `clientId` message

4. Poll for local service socket (`~/.kawa-code/sockets/caw.<GUID>`)

5. Connect to local service when available

6. Request temp directory and authentication info

7. Ready for collaboration

 

**Technologies:**

- Java NIO (SocketChannel, ServerSocketChannel)

- IntelliJ Platform threading (ApplicationManager.executeOnPooledThread)

- Gson or Jackson for JSON parsing

 

---

 

### 2.2 Event Handling System

 

**Responsibilities:**

- Dispatch incoming messages to appropriate handlers

- Manage event subscriptions and lifecycle

- Handle both request and response flows

- Clean up one-time response handlers

 

**Event Types:**

 

| Event | Direction | Purpose |

|-------|-----------|---------|

| `active-path` | OUT | Notify server of active file |

| `file-saved` | OUT | Notify server of file save |

| `diff-peer` | OUT | Request peer diff |

| `branch:select` | OUT | Select branch for comparison |

| `auth:info` | OUT | Request authentication status |

| `peer:select` | IN | Peer selected by user |

| `peer:unselect` | IN | Peer deselected |

| `branch:select` | IN | Branch selected |

| `branch:refresh` | IN | Refresh branch data |

| `auth:logout` | IN | Logout notification |

| `context:add` | IN | Context item added |

| `context:del` | IN | Context item deleted |

| `open-peer-file` | IN | Open peer file request |

 

**Key Classes:**

- `EventDispatcher` - Routes messages to handlers

- `EventHandler` interface - Base interface for all event handlers

- `ResponseHandlerRegistry` - One-time response handlers

- Individual handler implementations (e.g., `PeerSelectHandler`, `BranchSelectHandler`)

 

---

 

### 2.3 File Monitoring System

 

**Responsibilities:**

- Track active file changes

- Detect file saves

- Notify backend of file operations

- Debounce rapid changes to reduce message traffic

 

**IntelliJ Platform Integration:**

- **BulkFileListener** - Listen to VFS changes for file saves

- **FileEditorManagerListener** - Track active file changes

- **DocumentListener** - Monitor document modifications (optional, for live updates)

 

**Implementation:**

 

```xml

<!-- plugin.xml -->

<applicationListeners>

  <listener class="com.codeawareness.pycharm.listeners.FileChangeListener"

            topic="com.intellij.openapi.vfs.newvfs.BulkFileListener"/>

</applicationListeners>

 

<projectListeners>

  <listener class="com.codeawareness.pycharm.listeners.ActiveFileListener"

            topic="com.intellij.openapi.fileEditor.FileEditorManagerListener"/>

</projectListeners>

```

 

**Debouncing Strategy:**

- Use `java.util.concurrent.ScheduledExecutorService`

- Default delay: 500ms

- Cancel pending timers on new changes

- Batch multiple rapid changes into single notification

 

---

 

### 2.4 Highlighting System

 

**Responsibilities:**

- Apply visual indicators to editor lines

- Support 4 highlight types (conflict, overlap, peer, modified)

- Manage highlight lifecycle (add, update, clear)

- Support both light and dark themes

 

**Highlight Types:**

 

| Type | Color (Light) | Color (Dark) | Purpose |

|------|---------------|--------------|---------|

| Conflict | Red (#ff0000) | Dark Red (#8b0000) | Merge conflict areas |

| Overlap | Orange (#ffc000) | Dark Orange (#ff8c00) | Overlapping changes |

| Peer | Blue (#ffdd34) | Dark Blue (#1f1cc2) | Teammate modifications |

| Modified | Green (#00b1a420) | Dark Green (#03445f) | Local changes |

 

**IntelliJ Platform APIs:**

- **RangeHighlighter** - Primary highlighting API

- **MarkupModel** - Document markup management

- **EditorColorsScheme** - Theme-aware coloring

- **TextAttributes** - Styling (background color, effects)

 

**Implementation:**

 

```java

// Example highlighting code

Editor editor = FileEditorManager.getInstance(project)

    .getSelectedTextEditor();

MarkupModel markupModel = editor.getMarkupModel();

 

// Add highlight for line

int lineStartOffset = document.getLineStartOffset(lineNumber);

int lineEndOffset = document.getLineEndOffset(lineNumber);

 

TextAttributes attributes = new TextAttributes();

attributes.setBackgroundColor(colorForType);

 

RangeHighlighter highlighter = markupModel.addRangeHighlighter(

    lineStartOffset,

    lineEndOffset,

    HighlighterLayer.SELECTION - 1, // Layer below selection

    attributes,

    HighlighterTargetArea.LINES_IN_RANGE

);

 

// Store highlighter for cleanup

highlighterRegistry.put(lineNumber, highlighter);

```

 

**Lifecycle Management:**

- Store highlighters in `Map<Integer, RangeHighlighter>` (line -> highlighter)

- Clear all highlights on file close or plugin disable

- Update highlights on server response

- Remove highlights on peer deselection

 

---

 

### 2.5 Diff Viewing System

 

**Responsibilities:**

- Display side-by-side comparisons of peer code

- Extract peer files to temp directory

- Integrate with IntelliJ diff viewer

- Manage temp file lifecycle

 

**IntelliJ Platform Integration:**

- **DiffManager** - Built-in diff viewing

- **DiffContentFactory** - Create diff content from files

- **VirtualFile** - File abstraction

 

**Implementation:**

 

```java

// Open diff viewer

DiffManager diffManager = DiffManager.getInstance();

VirtualFile localFile = // current file

VirtualFile peerFile = // extracted peer file

 

DiffContent localContent = DiffContentFactory.getInstance()

    .create(project, localFile);

DiffContent peerContent = DiffContentFactory.getInstance()

    .create(project, peerFile);

 

SimpleDiffRequest request = new SimpleDiffRequest(

    "Code Awareness: " + peerName,

    localContent,

    peerContent,

    "Your Version",

    peerName + "'s Version"

);

 

diffManager.showDiff(project, request);

```

 

**Temp File Management:**

- Store peer files in `~/.cache/caw.pycharm/` or system temp

- Mark files as read-only

- Exclude from project indexing

- Clean up on plugin disable or IDE shutdown

 

---

 

### 2.6 UI Integration

 

**Responsibilities:**

- Display connection and authentication status

- Provide user actions (refresh, clear highlights, etc.)

- Show notifications for important events

- Integrate with IDE status bar and tool windows

 

**UI Components:**

 

1. **Status Bar Widget**

   - Connection status indicator

   - Authentication status

   - Click to open settings or reconnect

 

2. **Tool Window** (Optional)

   - List of active peers

   - Branch comparison controls

   - Context management

 

3. **Notifications**

   - Connection established/lost

   - Authentication required

   - Error messages

 

4. **Actions**

   - Refresh highlights

   - Clear all highlights

   - Open diff with peer

   - Connection status

   - Settings

 

**IntelliJ Platform APIs:**

- **StatusBarWidgetFactory** - Status bar integration

- **ToolWindowFactory** - Tool window creation

- **Notifications** - Balloon notifications

- **AnAction** - Custom actions

 

---

 

## 3. Configuration & Settings

 

### 3.1 Persistent Storage

 

**Settings Structure:**

 

```kotlin

data class CodeAwarenessSettings(

    // Connection

    var catalogName: String = "catalog",

    var updateDelayMs: Int = 500,

    var debugLogging: Boolean = false,

 

    // Highlighting

    var highlightIntensity: Float = 0.3f,

    var highlightRefreshDelayMs: Int = 500,

    var highlightPersistent: Boolean = false,

    var fullWidthHighlights: Boolean = true,

 

    // Theme colors (light)

    var changeColorLight: String = "#00b1a420",

    var peerColorLight: String = "#ffdd34",

    var mergeColorLight: String = "#ffc000",

    var conflictColorLight: String = "#ff0000",

 

    // Theme colors (dark)

    var changeColorDark: String = "#03445f",

    var peerColorDark: String = "#1f1cc2",

    var mergeColorDark: String = "#141299",

    var conflictColorDark: String = "#8b0000"

)

```

 

**IntelliJ Platform:**

- **PersistentStateComponent** - Settings persistence

- **Configurable** - Settings UI integration

 

### 3.2 User Preferences

 

**Settings UI Sections:**

1. Connection settings (catalog name, delays)

2. Highlighting preferences (intensity, colors)

3. Debug options (logging level)

4. Advanced options (temp directory, socket timeout)

 

---

 

## 4. State Management

 

### 4.1 Application-Level State

 

**Managed by ApplicationService:**

- Client GUID (unique identifier)

- Catalog connection

- Main IPC connection

- Response handler registry

- Configuration

 

### 4.2 Project-Level State

 

**Managed by ProjectService:**

- Active file

- Active project metadata

- Selected peer

- Highlight registry

- Authentication tokens

- User profile

 

**State Variables:**

 

```kotlin

class CodeAwarenessProjectState {

    var connected: Boolean = false

    var authenticated: Boolean = false

    var activeFile: VirtualFile? = null

    var activeProject: ProjectMetadata? = null

    var selectedPeer: PeerInfo? = null

    var user: UserInfo? = null

    var tokens: AuthTokens? = null

    var highlighters: MutableMap<Int, RangeHighlighter> = mutableMapOf()

    var tmpDir: String? = null

}

```

 

---

 

## 5. Threading Model

 

### 5.1 Thread Safety

 

**IntelliJ Platform Threading Rules:**

1. **EDT (Event Dispatch Thread)** - All UI operations

2. **Background Thread** - All I/O and long-running operations

3. **Write Thread** - Document modifications

 

**Implementation:**

- Socket I/O: Background thread pool (`Application.executeOnPooledThread`)

- Message processing: Background thread

- Highlighting updates: EDT via `ApplicationManager.getApplication().invokeLater()`

- File listeners: Already on appropriate threads (no explicit threading needed)

 

### 5.2 Synchronization

 

**Concurrent Data Structures:**

- `ConcurrentHashMap` for response handler registry

- `ConcurrentHashMap` for highlighter registry

- `AtomicBoolean` for connection state flags

- Synchronized blocks for socket I/O

 

**Example:**

 

```java

// Background thread for socket I/O

ApplicationManager.getApplication().executeOnPooledThread(() -> {

    // Read from socket

    String message = socketManager.readMessage();

 

    // Process message

    JsonObject json = parseJson(message);

 

    // Update UI on EDT

    ApplicationManager.getApplication().invokeLater(() -> {

        updateHighlights(json);

    });

});

```

 

---

 

## 6. Error Handling & Resilience

 

### 6.1 Connection Recovery

 

**Strategies:**

- Exponential backoff for retries (0.5s, 1s, 2s, 4s, 8s...)

- Max retry attempts: 10

- Timeout detection: 5s for stuck connections

- Automatic reconnection on connection loss

 

### 6.2 Graceful Degradation

 

**Failure Modes:**

- **No connection** → Show status bar warning, retry connection

- **No authentication** → Show notification, disable features

- **Bad message** → Log error, continue processing

- **Socket error** → Reconnect with backoff

 

### 6.3 Cleanup Strategy

 

**On Plugin Disable:**

1. Send disconnect message to catalog

2. Close all socket connections

3. Cancel pending timers

4. Clear all highlights

5. Remove all listeners

6. Clean up temp files

 

**On IDE Shutdown:**

1. Detect IDE shutdown via `AppLifecycleListener`

2. Trigger plugin cleanup

3. Send graceful disconnect messages

 

---

 

## 7. Testing Strategy

 

### 7.1 Unit Tests

 

**Components to Test:**

- Message protocol serialization/deserialization

- Event dispatcher routing

- Response handler registry

- Highlight color calculations

- GUID generation

 

### 7.2 Integration Tests

 

**Test Scenarios:**

- Socket connection establishment

- Message send/receive

- File change detection

- Highlight application

- Diff viewer integration

 

### 7.3 Manual Testing

 

**Test Plan:**

- Connect to real Code Awareness backend

- Select peer and verify highlights

- Save file and verify notification sent

- Open diff viewer

- Test on Windows and Unix platforms

- Test with light and dark themes

 

---

 

## 8. Cross-Platform Considerations

 

### 8.1 Platform-Specific Code

 

**Windows:**

- Named pipes: `\\.\pipe\caw.*`

- Use `RandomAccessFile` or JNA for named pipe I/O

- Handle backslash path separators

 

**Unix/Linux/macOS:**

- Unix domain sockets: `~/.kawa-code/sockets/caw.*`

- Use Java NIO `SocketChannel` with `UnixDomainSocketAddress` (Java 16+)

- Handle tilde expansion for home directory

 

### 8.2 Path Handling

 

**Utilities Needed:**

- Home directory expansion (`~` → `/home/user`)

- Path separator normalization

- Socket path validation

 

---

 

## 9. Dependencies

 

### 9.1 IntelliJ Platform APIs

 

- `com.intellij.openapi.vfs` - Virtual File System

- `com.intellij.openapi.editor` - Editor API

- `com.intellij.openapi.project` - Project API

- `com.intellij.openapi.application` - Application API

- `com.intellij.util.messages` - Message Bus

- `com.intellij.diff` - Diff Viewer

- `com.intellij.notification` - Notifications

 

### 9.2 External Libraries

 

- **Gson** or **Jackson** - JSON parsing

- **JNA** (optional) - Windows named pipe support if needed

- **SLF4J** - Logging

 

### 9.3 Gradle Dependencies

 

```kotlin

dependencies {

    implementation("com.google.code.gson:gson:2.10.1")

    // Or

    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")

}

```

 

---

 

## 10. Plugin Metadata

 

### 10.1 plugin.xml Structure

 

```xml

<idea-plugin>

  <id>com.codeawareness.pycharm</id>

  <name>Code Awareness</name>

  <vendor>Code Awareness Team</vendor>

 

  <description>

    Real-time team collaboration for PyCharm.

    See code intersections, conflicts, and overlaps with teammates

    before committing changes.

  </description>

 

  <depends>com.intellij.modules.platform</depends>

  <depends>com.intellij.modules.lang</depends>

 

  <extensions defaultExtensionNs="com.intellij">

    <!-- Services -->

    <applicationService

        serviceImplementation="com.codeawareness.pycharm.CodeAwarenessApplicationService"/>

    <projectService

        serviceImplementation="com.codeawareness.pycharm.CodeAwarenessProjectService"/>

 

    <!-- Settings -->

    <projectConfigurable

        instance="com.codeawareness.pycharm.settings.CodeAwarenessConfigurable"/>

 

    <!-- Status Bar -->

    <statusBarWidgetFactory

        implementation="com.codeawareness.pycharm.ui.CodeAwarenessStatusBarWidget"/>

  </extensions>

 

  <applicationListeners>

    <listener

        class="com.codeawareness.pycharm.listeners.FileChangeListener"

        topic="com.intellij.openapi.vfs.newvfs.BulkFileListener"/>

  </applicationListeners>

 

  <projectListeners>

    <listener

        class="com.codeawareness.pycharm.listeners.ActiveFileListener"

        topic="com.intellij.openapi.fileEditor.FileEditorManagerListener"/>

  </projectListeners>

 

  <actions>

    <group id="CodeAwareness.Menu" text="Code Awareness" popup="true">

      <action id="CodeAwareness.Refresh"

              class="com.codeawareness.pycharm.actions.RefreshAction"

              text="Refresh Highlights"/>

      <action id="CodeAwareness.ClearHighlights"

              class="com.codeawareness.pycharm.actions.ClearHighlightsAction"

              text="Clear All Highlights"/>

      <action id="CodeAwareness.ConnectionStatus"

              class="com.codeawareness.pycharm.actions.ConnectionStatusAction"

              text="Connection Status"/>

    </group>

  </actions>

</idea-plugin>

```

 

---

 

## 11. Performance Considerations

 

### 11.1 Optimization Strategies

 

1. **Lazy Initialization**

   - Initialize services on first use

   - Delay connection until project opens

 

2. **Efficient Highlighting**

   - Batch highlight updates

   - Only update visible editor region

   - Reuse existing highlighters when possible

 

3. **Message Batching**

   - Debounce file change notifications

   - Batch multiple highlight updates

 

4. **Memory Management**

   - Clean up old highlighters

   - Limit temp file cache size

   - Use weak references for listeners

 

### 11.2 Performance Metrics

 

**Target Metrics:**

- Connection establishment: < 1s

- Message round-trip: < 100ms

- Highlight update: < 50ms

- Memory footprint: < 50MB per project

 

---

 

## 12. Security Considerations

 

### 12.1 Socket Security

 

- Use Unix domain sockets (local only, no network exposure)

- Validate message format before processing

- Sanitize file paths in messages

 

### 12.2 Authentication

 

- Store auth tokens securely (use IntelliJ `PasswordSafe`)

- Don't log sensitive data

- Clear tokens on logout

 

### 12.3 Temp Files

 

- Use secure temp directory

- Set restrictive permissions

- Clean up on exit

 

---

 

## 13. Future Enhancements

 

### 13.1 Planned Features

 

1. **Enhanced UI**

   - Dedicated tool window with peer list

   - Interactive context management

   - Inline diff preview

 

2. **Advanced Highlighting**

   - Gutter icons for conflicts

   - Inline annotations with peer names

   - Animated transitions

 

3. **Performance Improvements**

   - Incremental highlight updates

   - Background indexing of peer changes

   - Caching of diff results

 

4. **Multi-IDE Support**

   - Reuse communication layer for other JetBrains IDEs

   - Plugin for WebStorm, Rider, CLion, etc.

 

---

 

## 14. Architectural Principles

 

### 14.1 Design Principles

 

1. **Separation of Concerns**

   - Clear boundaries between layers

   - Single Responsibility Principle

 

2. **Event-Driven Architecture**

   - Loose coupling via message bus

   - Asynchronous communication

 

3. **Fail-Safe Defaults**

   - Graceful degradation on errors

   - No data loss on crashes

 

4. **Extensibility**

   - Plugin architecture for handlers

   - Configuration-driven behavior

 

### 14.2 Code Organization

 

```

src/main/java/com/codeawareness/pycharm/

├── CodeAwarenessApplicationService.java

├── CodeAwarenessProjectService.java

├── communication/

│   ├── SocketManager.java

│   ├── CatalogConnection.java

│   ├── IpcConnection.java

│   └── MessageProtocol.java

├── events/

│   ├── EventDispatcher.java

│   ├── EventHandler.java

│   ├── ResponseHandlerRegistry.java

│   └── handlers/

│       ├── PeerSelectHandler.java

│       ├── BranchSelectHandler.java

│       └── ...

├── highlighting/

│   ├── HighlightManager.java

│   ├── HighlightType.java

│   └── ColorSchemeProvider.java

├── diff/

│   ├── DiffViewerManager.java

│   └── TempFileManager.java

├── listeners/

│   ├── FileChangeListener.java

│   └── ActiveFileListener.java

├── ui/

│   ├── CodeAwarenessStatusBarWidget.java

│   ├── CodeAwarenessToolWindow.java

│   └── actions/

│       ├── RefreshAction.java

│       └── ClearHighlightsAction.java

├── settings/

│   ├── CodeAwarenessSettings.java

│   └── CodeAwarenessConfigurable.java

└── utils/

    ├── GuidGenerator.java

    ├── PathUtils.java

    └── Logger.java

```

 

---

 

## 15. References

 

### 15.1 IntelliJ Platform Documentation

 

- [Plugin Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html)

- [Plugin Services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html)

- [Virtual File System](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html)

- [Editor API](https://plugins.jetbrains.com/docs/intellij/editor-basics.html)

- [Background Processes](https://plugins.jetbrains.com/docs/intellij/background-processes.html)

 

### 15.2 Code Awareness Documentation

 

- kawa.emacs repository (reference implementation)

- kawa.vscode repository (alternative reference)

- Code Awareness message protocol specification (see kawa.emacs exploration)

 

---

 

## Conclusion

 

This architecture provides a solid foundation for the Code Awareness PyCharm plugin, replicating the proven design from the Emacs and VS Code extensions while leveraging IntelliJ Platform APIs and best practices. The modular design ensures maintainability, testability, and extensibility for future enhancements.

 

**Next Steps:**

1. Review and approve architecture

2. Proceed with detailed implementation plan

3. Set up development environment

4. Begin Phase 1 implementation (Communication Layer)
