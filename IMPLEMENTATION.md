Implementation Phases
Phase 1: Foundation (5-7 days) - CRITICAL
Project setup
Socket communication layer
Message protocol
Connection state machine
Logging & debugging
Phase 2: Core Features (4-6 days) - HIGH
Event dispatcher system
File change monitoring
Active file tracking
Basic event handlers
Authentication flow
Phase 3: UI Integration (3-4 days) - HIGH
Highlighting system
Status bar widget
Notifications
Actions & menus
Settings UI
Phase 4: Advanced Features (2-3 days) - MEDIUM
Diff viewer integration
Context management
Project management
Phase 5: Polish & Testing (2-4 days) - MEDIUM
Comprehensive testing
Performance optimization
Error handling
Documentation
Distribution preparation
Documentation
ARCHITECTURE.md - Detailed architecture overview (15 sections)
IMPLEMENTATION_PLAN.md - Phase-by-phase implementation guide
Additional documentation will be added as development progresses:

Developer guide
User guide
API reference
Troubleshooting guide
Requirements
Development Environment
IntelliJ IDEA 2023.3+
JDK 17+
Gradle 8.0+
Git
Runtime Requirements
PyCharm 2023.3+ (Community or Professional)
Code Awareness backend application
Java 17+ runtime
Platform Support
Windows 10/11
macOS (Intel & Apple Silicon)
Linux (Ubuntu, Fedora, etc.)

# Code Awareness PyCharm Plugin - Implementation Plan

 

## Executive Summary

 

This document provides a detailed, phase-by-phase implementation plan for the Code Awareness PyCharm plugin. The plan is structured into 5 phases over an estimated **15-24 development days**, with clear priorities, dependencies, and success criteria for each phase.

 

**Development Approach:** Iterative with working increments at each phase

**Risk Level:** Low (proven architecture from Emacs/VS Code implementations)

**Target Platforms:** Windows, macOS, Linux

 

---

 

## Timeline Overview

 

| Phase | Duration | Priority | Deliverable |

|-------|----------|----------|-------------|

| **Phase 1: Foundation** | 5-7 days | Critical | Working socket communication |

| **Phase 2: Core Features** | 4-6 days | High | File monitoring & basic events |

| **Phase 3: UI Integration** | 3-4 days | High | Highlighting & status bar |

| **Phase 4: Advanced Features** | 2-3 days | Medium | Diff viewer & context mgmt |

| **Phase 5: Polish & Testing** | 2-4 days | Medium | Bug fixes & documentation |

| **Total** | **16-24 days** | - | Production-ready plugin |

 

---

 

## Phase 1: Foundation (5-7 days) - CRITICAL

 

### Objective

Establish the core communication infrastructure and plugin scaffolding. This phase is critical as all subsequent features depend on reliable socket communication.

 

### Tasks

 

#### 1.1 Project Setup (1 day)

- [ ] Create IntelliJ Platform plugin project structure

- [ ] Configure `build.gradle.kts` with dependencies

- [ ] Set up `plugin.xml` with basic metadata

- [ ] Configure development environment (IntelliJ IDEA)

- [ ] Set up version control and branching strategy

- [ ] Configure CI/CD pipeline (GitHub Actions)

 

**Deliverable:** Buildable plugin skeleton that can be loaded in PyCharm

 

**Dependencies:** Gson 2.10.1, IntelliJ Platform SDK

 

**Files Created:**

```

build.gradle.kts

src/main/resources/META-INF/plugin.xml

src/main/java/com/codeawareness/pycharm/CodeAwarenessApplicationService.java

```

 

---

 

#### 1.2 Socket Communication Layer (2-3 days)

- [ ] Implement `SocketManager` for cross-platform socket I/O

  - [ ] Unix domain socket support (Java 16+ `UnixDomainSocketAddress`)

  - [ ] Windows named pipe support (via JNA or `RandomAccessFile`)

  - [ ] Socket path resolution (`~/.kawa-code/sockets/caw.*`)

  - [ ] Path normalization for Windows/Unix

- [ ] Implement connection lifecycle (connect, read, write, close)

- [ ] Add connection timeout and error handling

- [ ] Implement exponential backoff retry logic

- [ ] Add comprehensive logging for debugging

 

**Deliverable:** Working socket connection to catalog service

 

**Technical Details:**

```java

// Socket paths

Unix: ~/.kawa-code/sockets/caw.catalog

Windows: \\.\pipe\caw.catalog

 

// Connection timeout: 5 seconds

// Retry backoff: 0.5s, 1s, 2s, 4s, 8s (max 10 attempts)

```

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/communication/SocketManager.java

src/main/java/com/codeawareness/pycharm/communication/UnixSocketAdapter.java

src/main/java/com/codeawareness/pycharm/communication/WindowsNamedPipeAdapter.java

src/main/java/com/codeawareness/pycharm/utils/PathUtils.java

```

 

**Test Cases:**

- Connect to mock socket server

- Handle connection timeout

- Retry on connection failure

- Close connection gracefully

 

---

 

#### 1.3 Message Protocol Layer (1-2 days)

- [ ] Implement JSON message serialization/deserialization

- [ ] Implement form-feed delimiter parsing (`\f`)

- [ ] Create message builder utilities

- [ ] Handle fragmented messages (buffering)

- [ ] Add message validation

- [ ] Create message data classes (DTOs)

 

**Deliverable:** Reliable message send/receive over sockets

 

**Message Format:**

```json

{

  "flow": "req|res|err",

  "domain": "code|auth|*",

  "action": "action_name",

  "data": {...},

  "caw": "client_guid"

}\f

```

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/communication/MessageProtocol.java

src/main/java/com/codeawareness/pycharm/communication/Message.java

src/main/java/com/codeawareness/pycharm/communication/MessageBuilder.java

src/main/java/com/codeawareness/pycharm/communication/MessageParser.java

```

 

**Test Cases:**

- Serialize/deserialize all message types

- Handle malformed JSON

- Parse multiple messages from buffer

- Handle incomplete messages

 

---

 

#### 1.4 Connection State Machine (1 day)

- [ ] Implement `CatalogConnection` service

- [ ] Implement `IpcConnection` service

- [ ] Create GUID generator for client ID

- [ ] Implement catalog registration (`clientId` message)

- [ ] Implement local service polling (exponential backoff)

- [ ] Add connection state tracking (disconnected → connecting → connected)

- [ ] Implement graceful disconnect (`clientDisconnect` message)

 

**Deliverable:** Complete connection sequence from catalog to main service

 

**Connection Flow:**

```

1. Generate GUID

2. Connect to catalog (~/.kawa-code/sockets/caw.catalog)

3. Send clientId message

4. Poll for local socket (~/.kawa-code/sockets/caw.<GUID>)

5. Connect to local service

6. Ready for events

```

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/communication/CatalogConnection.java

src/main/java/com/codeawareness/pycharm/communication/IpcConnection.java

src/main/java/com/codeawareness/pycharm/utils/GuidGenerator.java

```

 

**Test Cases:**

- GUID uniqueness

- Catalog registration success

- Local service socket detection

- Graceful disconnect

 

---

 

#### 1.5 Logging & Debugging (0.5 day)

- [ ] Set up structured logging (SLF4J + IntelliJ log)

- [ ] Add debug mode configuration

- [ ] Implement log viewer action

- [ ] Add log levels (ERROR, WARN, INFO, DEBUG)

- [ ] Log all socket I/O (when debug enabled)

 

**Deliverable:** Comprehensive logging for troubleshooting

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/utils/Logger.java

```

 

---

 

### Phase 1 Success Criteria

- [x] Plugin loads in PyCharm without errors

- [x] Successfully connects to catalog service

- [x] Successfully connects to main IPC service

- [x] Can send and receive JSON messages

- [x] Graceful disconnect on plugin disable

- [x] All unit tests pass (80%+ coverage)

- [x] Cross-platform support (Windows, macOS, Linux)

 

---

 

## Phase 2: Core Features (4-6 days) - HIGH PRIORITY

 

### Objective

Implement file monitoring, event handling, and basic request/response flows. This phase brings the plugin to a minimally functional state.

 

### Tasks

 

#### 2.1 Event Dispatcher System (1-2 days)

- [ ] Implement `EventDispatcher` for routing messages

- [ ] Create `EventHandler` interface

- [ ] Implement handler registration system

- [ ] Create `ResponseHandlerRegistry` for one-time handlers

- [ ] Add support for async response matching

- [ ] Implement handler cleanup on response

 

**Deliverable:** Working event routing system

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/events/EventDispatcher.java

src/main/java/com/codeawareness/pycharm/events/EventHandler.java

src/main/java/com/codeawareness/pycharm/events/ResponseHandlerRegistry.java

```

 

**Test Cases:**

- Route message to correct handler

- One-time handler cleanup

- Handle unknown message types

- Concurrent handler execution

 

---

 

#### 2.2 File Change Monitoring (1-2 days)

- [ ] Implement `BulkFileListener` for VFS changes

- [ ] Detect file save events

- [ ] Filter relevant files (project files only)

- [ ] Implement debouncing (500ms default)

- [ ] Send `file-saved` message to backend

- [ ] Handle batch file saves

 

**Deliverable:** File save notifications sent to backend

 

**Implementation:**

```xml

<applicationListeners>

  <listener class="com.codeawareness.pycharm.listeners.FileChangeListener"

            topic="com.intellij.openapi.vfs.newvfs.BulkFileListener"/>

</applicationListeners>

```

 

```java

class FileChangeListener implements BulkFileListener {

    @Override

    public void after(@NotNull List<? extends VFileEvent> events) {

        for (VFileEvent event : events) {

            if (event instanceof VFileContentChangeEvent) {

                handleFileSave(event.getFile());

            }

        }

    }

}

```

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/listeners/FileChangeListener.java

src/main/java/com/codeawareness/pycharm/monitoring/FileMonitor.java

```

 

**Test Cases:**

- Detect file save in project

- Ignore non-project files

- Debounce rapid saves

- Send correct message format

 

---

 

#### 2.3 Active File Tracking (1 day)

- [ ] Implement `FileEditorManagerListener` for active file changes

- [ ] Detect editor tab switches

- [ ] Track active file globally

- [ ] Send `active-path` message on file change

- [ ] Filter temp files and non-project files

- [ ] Implement debouncing for rapid switches

 

**Deliverable:** Active file notifications sent to backend

 

**Implementation:**

```xml

<projectListeners>

  <listener class="com.codeawareness.pycharm.listeners.ActiveFileListener"

            topic="com.intellij.openapi.fileEditor.FileEditorManagerListener"/>

</projectListeners>

```

 

```java

class ActiveFileListener implements FileEditorManagerListener {

    @Override

    public void selectionChanged(@NotNull FileEditorManagerEvent event) {

        VirtualFile file = event.getNewFile();

        if (file != null && isProjectFile(file)) {

            handleActiveFileChanged(file);

        }

    }

}

```

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/listeners/ActiveFileListener.java

```

 

**Test Cases:**

- Detect tab switch

- Send active-path message

- Ignore temp files

- Handle rapid tab switches

 

---

 

#### 2.4 Basic Event Handlers (1 day)

- [ ] Implement `PeerSelectHandler` (handle `peer:select` event)

- [ ] Implement `PeerUnselectHandler` (handle `peer:unselect` event)

- [ ] Implement `BranchSelectHandler` (handle `branch:select` event)

- [ ] Implement `BranchUnselectHandler` (handle `branch:unselect` event)

- [ ] Implement `AuthInfoHandler` (handle `auth:info` response)

- [ ] Store peer/branch selection state

 

**Deliverable:** Basic event handling for peer/branch selection

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/events/handlers/PeerSelectHandler.java

src/main/java/com/codeawareness/pycharm/events/handlers/PeerUnselectHandler.java

src/main/java/com/codeawareness/pycharm/events/handlers/BranchSelectHandler.java

src/main/java/com/codeawareness/pycharm/events/handlers/AuthInfoHandler.java

```

 

**Test Cases:**

- Handle peer selection

- Update state correctly

- Handle malformed event data

 

---

 

#### 2.5 Authentication Flow (0.5 day)

- [ ] Send `auth:info` request on connection

- [ ] Handle authentication response

- [ ] Store user profile and tokens

- [ ] Show authentication status

- [ ] Handle logout event

 

**Deliverable:** Working authentication flow

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/auth/AuthManager.java

```

 

**Test Cases:**

- Request auth info

- Parse auth response

- Store tokens securely

 

---

 

### Phase 2 Success Criteria

- [x] File saves trigger backend notifications

- [x] Active file changes trigger backend notifications

- [x] Peer/branch selection events are handled

- [x] Authentication completes successfully

- [x] State is maintained correctly

- [x] All unit tests pass (80%+ coverage)

 

---

 

## Phase 3: UI Integration (3-4 days) - HIGH PRIORITY

 

### Objective

Implement visual feedback (highlighting, status bar, notifications) to make the plugin user-facing.

 

### Tasks

 

#### 3.1 Highlighting System (2 days)

- [ ] Implement `HighlightManager` service

- [ ] Create `HighlightType` enum (Conflict, Overlap, Peer, Modified)

- [ ] Implement color scheme provider (light/dark theme support)

- [ ] Add highlights using `RangeHighlighter` API

- [ ] Store highlighters in registry (`Map<Integer, RangeHighlighter>`)

- [ ] Implement highlight clearing

- [ ] Implement full-width vs range highlighting

- [ ] Handle highlight updates on server response

 

**Deliverable:** Working code highlighting for all 4 types

 

**Implementation:**

```java

MarkupModel markupModel = editor.getMarkupModel();

TextAttributes attributes = new TextAttributes();

attributes.setBackgroundColor(colorForType);

 

RangeHighlighter highlighter = markupModel.addRangeHighlighter(

    lineStartOffset,

    lineEndOffset,

    HighlighterLayer.SELECTION - 1,

    attributes,

    HighlighterTargetArea.LINES_IN_RANGE

);

```

 

**Color Scheme:**

| Type | Light Theme | Dark Theme |

|------|-------------|------------|

| Conflict | #ff0000 | #8b0000 |

| Overlap | #ffc000 | #ff8c00 |

| Peer | #ffdd34 | #1f1cc2 |

| Modified | #00b1a420 | #03445f |

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/highlighting/HighlightManager.java

src/main/java/com/codeawareness/pycharm/highlighting/HighlightType.java

src/main/java/com/codeawareness/pycharm/highlighting/ColorSchemeProvider.java

```

 

**Test Cases:**

- Apply highlight to line

- Clear highlights

- Switch between themes

- Handle editor close

 

---

 

#### 3.2 Status Bar Widget (0.5 day)

- [ ] Implement `StatusBarWidgetFactory`

- [ ] Create status bar widget UI

- [ ] Show connection status (connected/disconnected)

- [ ] Show authentication status (authenticated/not authenticated)

- [ ] Add click action to open settings

- [ ] Update on connection state changes

 

**Deliverable:** Status bar widget showing connection status

 

**Implementation:**

```xml

<extensions defaultExtensionNs="com.intellij">

  <statusBarWidgetFactory

      implementation="com.codeawareness.pycharm.ui.CodeAwarenessStatusBarWidget"/>

</extensions>

```

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/ui/CodeAwarenessStatusBarWidget.java

```

 

**Test Cases:**

- Widget appears in status bar

- Updates on connection change

- Click opens settings

 

---

 

#### 3.3 Notifications (0.5 day)

- [ ] Implement notification helper

- [ ] Show connection established notification

- [ ] Show connection lost notification

- [ ] Show authentication required notification

- [ ] Show error notifications

- [ ] Add notification group configuration

 

**Deliverable:** User-facing notifications for key events

 

**Implementation:**

```java

NotificationGroupManager.getInstance()

    .getNotificationGroup("Code Awareness")

    .createNotification("Connected to Code Awareness", NotificationType.INFORMATION)

    .notify(project);

```

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/ui/NotificationHelper.java

```

 

---

 

#### 3.4 Actions & Menus (0.5 day)

- [ ] Create "Refresh Highlights" action

- [ ] Create "Clear All Highlights" action

- [ ] Create "Connection Status" action

- [ ] Create "Open Settings" action

- [ ] Add actions to Tools menu

- [ ] Add keyboard shortcuts (optional)

 

**Deliverable:** User-accessible actions for plugin control

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/actions/RefreshAction.java

src/main/java/com/codeawareness/pycharm/actions/ClearHighlightsAction.java

src/main/java/com/codeawareness/pycharm/actions/ConnectionStatusAction.java

```

 

---

 

#### 3.5 Settings UI (0.5 day)

- [ ] Implement `CodeAwarenessSettings` state class

- [ ] Implement `CodeAwarenessConfigurable` for settings UI

- [ ] Add connection settings (catalog name, delays)

- [ ] Add highlighting settings (intensity, colors)

- [ ] Add debug settings (logging level)

- [ ] Persist settings using `PersistentStateComponent`

 

**Deliverable:** Settings page in PyCharm preferences

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/settings/CodeAwarenessSettings.java

src/main/java/com/codeawareness/pycharm/settings/CodeAwarenessConfigurable.java

src/main/resources/META-INF/pluginIcon.svg

```

 

**Test Cases:**

- Open settings page

- Modify settings

- Settings persist across restarts

 

---

 

### Phase 3 Success Criteria

- [x] Highlights appear in editor for all 4 types

- [x] Status bar shows connection status

- [x] Notifications appear for key events

- [x] User can refresh/clear highlights via actions

- [x] Settings page is functional

- [x] UI works in light and dark themes

 

---

 

## Phase 4: Advanced Features (2-3 days) - MEDIUM PRIORITY

 

### Objective

Implement diff viewing, context management, and other advanced collaboration features.

 

### Tasks

 

#### 4.1 Diff Viewer Integration (1-2 days)

- [ ] Implement `DiffViewerManager`

- [ ] Handle `diff-peer` request

- [ ] Extract peer files to temp directory

- [ ] Open IntelliJ diff viewer with peer file

- [ ] Implement temp file cleanup

- [ ] Handle `open-peer-file` event

- [ ] Mark peer files as read-only

 

**Deliverable:** Working side-by-side diff with peer code

 

**Implementation:**

```java

DiffManager diffManager = DiffManager.getInstance();

SimpleDiffRequest request = new SimpleDiffRequest(

    "Code Awareness: " + peerName,

    localContent,

    peerContent,

    "Your Version",

    peerName + "'s Version"

);

diffManager.showDiff(project, request);

```

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/diff/DiffViewerManager.java

src/main/java/com/codeawareness/pycharm/diff/TempFileManager.java

```

 

**Test Cases:**

- Request peer diff

- Diff viewer opens

- Peer file is read-only

- Temp files cleaned up

 

---

 

#### 4.2 Context Management (0.5 day)

- [ ] Implement `context:add` event handler

- [ ] Implement `context:del` event handler

- [ ] Implement `context:open-rel` event handler

- [ ] Send `context:apply` requests

- [ ] Store context state

 

**Deliverable:** Context management events handled

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/events/handlers/ContextAddHandler.java

src/main/java/com/codeawareness/pycharm/events/handlers/ContextDeleteHandler.java

```

 

---

 

#### 4.3 Project Management (0.5 day)

- [ ] Auto-detect git repository

- [ ] Extract project metadata (origin, branch)

- [ ] Send project info to backend

- [ ] Handle multi-project workspaces

 

**Deliverable:** Automatic project detection and metadata

 

**Files Created:**

```

src/main/java/com/codeawareness/pycharm/project/ProjectManager.java

```

 

**Test Cases:**

- Detect git repository

- Extract origin and branch

- Handle non-git projects

 

---

 

### Phase 4 Success Criteria

- [x] Diff viewer opens successfully

- [x] Context events are handled

- [x] Project metadata is detected

- [x] All features work end-to-end

 

---

 

## Phase 5: Polish & Testing (2-4 days) - MEDIUM PRIORITY

 

### Objective

Bug fixes, performance optimization, comprehensive testing, and documentation.

 

### Tasks

 

#### 5.1 Comprehensive Testing (1-2 days)

- [ ] Write unit tests for all core components (target: 85%+ coverage)

  - [ ] Socket communication

  - [ ] Message protocol

  - [ ] Event dispatcher

  - [ ] Highlighting system

  - [ ] File monitoring

- [ ] Write integration tests

  - [ ] End-to-end connection flow

  - [ ] File save → highlight update

  - [ ] Peer selection → highlight appearance

- [ ] Manual testing on all platforms (Windows, macOS, Linux)

- [ ] Test with real Code Awareness backend

- [ ] Load testing (multiple projects, large files)

 

**Deliverable:** Comprehensive test suite

 

**Files Created:**

```

src/test/java/com/codeawareness/pycharm/SocketManagerTest.java

src/test/java/com/codeawareness/pycharm/MessageProtocolTest.java

src/test/java/com/codeawareness/pycharm/EventDispatcherTest.java

src/test/java/com/codeawareness/pycharm/HighlightManagerTest.java

```

 

**Test Coverage Target:** 85%+

 

---

 

#### 5.2 Performance Optimization (0.5-1 day)

- [ ] Profile socket I/O performance

- [ ] Optimize message parsing (reduce allocations)

- [ ] Batch highlight updates

- [ ] Implement lazy initialization

- [ ] Reduce memory footprint

- [ ] Optimize file change debouncing

 

**Deliverable:** Optimized plugin with <50MB memory footprint

 

**Performance Targets:**

- Connection time: <1s

- Message round-trip: <100ms

- Highlight update: <50ms

- Memory: <50MB per project

 

---

 

#### 5.3 Error Handling & Edge Cases (0.5 day)

- [ ] Handle socket connection failures gracefully

- [ ] Handle malformed messages

- [ ] Handle large files (>10K lines)

- [ ] Handle rapid file changes

- [ ] Handle IDE shutdown during socket I/O

- [ ] Add error recovery mechanisms

 

**Deliverable:** Robust error handling

 

---

 

#### 5.4 Documentation (0.5-1 day)

- [ ] Write README.md with installation instructions

- [ ] Write developer guide (architecture, build, test)

- [ ] Write user guide (features, settings, troubleshooting)

- [ ] Add inline code documentation (Javadoc)

- [ ] Create CHANGELOG.md

- [ ] Add screenshots to README

 

**Deliverable:** Complete documentation

 

**Files Created:**

```

README.md

DEVELOPER_GUIDE.md

USER_GUIDE.md

CHANGELOG.md

```

 

---

 

#### 5.5 Plugin Metadata & Distribution (0.5 day)

- [ ] Finalize plugin.xml metadata

- [ ] Create plugin icon (SVG)

- [ ] Write plugin description for JetBrains Marketplace

- [ ] Configure publishing workflow

- [ ] Create release notes

- [ ] Test plugin installation from marketplace

 

**Deliverable:** Plugin ready for distribution

 

**Files Updated:**

```

src/main/resources/META-INF/plugin.xml

src/main/resources/META-INF/pluginIcon.svg

```

 

---

 

### Phase 5 Success Criteria

- [x] Test coverage ≥85%

- [x] All manual tests pass

- [x] Performance targets met

- [x] Documentation complete

- [x] Plugin published to JetBrains Marketplace

- [x] No critical bugs

 

---

 

## Development Workflow

 

### Daily Workflow

1. **Morning Standup** (15 min)

   - Review yesterday's progress

   - Plan today's tasks

   - Identify blockers

 

2. **Development** (6-7 hours)

   - Implement tasks from current phase

   - Write unit tests as you go

   - Commit frequently with clear messages

 

3. **Code Review** (1 hour)

   - Self-review changes

   - Run automated tests

   - Check against success criteria

 

4. **Documentation** (30 min)

   - Update inline docs

   - Update CHANGELOG

   - Update implementation notes

 

### Weekly Milestones

- **Week 1:** Complete Phase 1 (Foundation)

- **Week 2:** Complete Phase 2 (Core Features) + Phase 3 (UI Integration)

- **Week 3:** Complete Phase 4 (Advanced Features) + Phase 5 (Polish)

 

---

 

## Risk Management

 

### High-Risk Items

 

| Risk | Impact | Mitigation |

|------|--------|------------|

| Socket I/O performance issues | High | Profile early, optimize incrementally |

| Windows named pipe compatibility | Medium | Test on Windows early, use JNA if needed |

| IntelliJ API breaking changes | Medium | Pin to specific platform version |

| Message protocol mismatches | High | Validate against Emacs implementation |

| Threading deadlocks | Medium | Use concurrent data structures, avoid locks |

 

### Contingency Plans

 

**If socket performance is poor:**

- Switch to NIO2 async I/O

- Implement connection pooling

- Use native libraries (JNA)

 

**If highlighting is slow:**

- Implement incremental updates

- Use background thread for processing

- Cache highlight calculations

 

**If cross-platform issues arise:**

- Isolate platform-specific code

- Use abstraction layer (SocketAdapter interface)

- Add platform-specific builds

 

---

 

## Testing Strategy

 

### Unit Testing (Target: 85% coverage)

 

**Components to Test:**

- `SocketManager` - connection, read, write, close

- `MessageProtocol` - serialization, deserialization, parsing

- `EventDispatcher` - routing, handler registration

- `HighlightManager` - highlight creation, clearing

- `GuidGenerator` - uniqueness, format

 

**Framework:** JUnit 5 + Mockito

 

---

 

### Integration Testing

 

**Scenarios:**

1. **End-to-End Connection**

   - Start plugin → connect to catalog → connect to service → authenticate

2. **File Save Flow**

   - Save file → detect event → send message → receive response → update highlights

3. **Peer Selection Flow**

   - Receive peer:select → update state → request highlights → apply highlights

4. **Diff Viewer Flow**

   - Request diff → extract peer file → open diff viewer → close viewer → cleanup

 

**Framework:** IntelliJ Platform Test Framework

 

---

 

### Manual Testing Checklist

 

**Platform Testing:**

- [ ] Windows 10/11

- [ ] macOS (Intel + Apple Silicon)

- [ ] Linux (Ubuntu, Fedora)

 

**IDE Versions:**

- [ ] PyCharm 2023.3

- [ ] PyCharm 2024.1

- [ ] PyCharm 2024.2

 

**Feature Testing:**

- [ ] Connection establishment

- [ ] File save detection

- [ ] Active file tracking

- [ ] Highlighting (all 4 types)

- [ ] Diff viewer

- [ ] Settings persistence

- [ ] Status bar updates

- [ ] Notifications

- [ ] Actions (refresh, clear)

 

**Edge Cases:**

- [ ] Rapid file saves

- [ ] Large files (>10K lines)

- [ ] Binary files

- [ ] Network errors

- [ ] IDE restart during connection

- [ ] Multiple projects open

 

---

 

## Success Metrics

 

### Phase Completion Metrics

 

**Phase 1 (Foundation):**

- [ ] Socket connection success rate: 100%

- [ ] Message round-trip time: <100ms

- [ ] Unit test coverage: ≥80%

 

**Phase 2 (Core Features):**

- [ ] File change detection rate: 100%

- [ ] Active file tracking accuracy: 100%

- [ ] Event handling success rate: ≥99%

 

**Phase 3 (UI Integration):**

- [ ] Highlight rendering time: <50ms

- [ ] UI responsiveness: No freezes

- [ ] Theme compatibility: 100%

 

**Phase 4 (Advanced Features):**

- [ ] Diff viewer open time: <500ms

- [ ] Temp file cleanup: 100%

- [ ] Context event handling: 100%

 

**Phase 5 (Polish):**

- [ ] Test coverage: ≥85%

- [ ] Memory footprint: <50MB

- [ ] Plugin load time: <500ms

- [ ] Bug count: 0 critical, <5 minor

 

---

 

## Dependencies & Prerequisites

 

### Development Environment

- IntelliJ IDEA 2023.3+ (for plugin development)

- JDK 17+ (required for IntelliJ Platform 2023.3+)

- Gradle 8.0+ (build tool)

- Git (version control)

 

### External Services

- Code Awareness backend application (for testing)

- Mock socket server (for unit tests)

 

### Libraries

```kotlin

dependencies {

    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    testImplementation("org.mockito:mockito-core:5.5.0")

}

```

 

---

 

## Post-Implementation Tasks

 

### After Phase 5 Completion

 

1. **Beta Testing**

   - Recruit 5-10 beta testers

   - Gather feedback

   - Fix critical bugs

 

2. **Performance Tuning**

   - Profile real-world usage

   - Optimize bottlenecks

   - Reduce memory usage

 

3. **Documentation Review**

   - Technical review by team

   - User documentation review

   - Update based on feedback

 

4. **Release Preparation**

   - Finalize version number (1.0.0)

   - Create release notes

   - Prepare marketing materials

 

5. **JetBrains Marketplace Submission**

   - Submit plugin for review

   - Address review feedback

   - Publish to marketplace

 

---

 

## Appendix A: File Structure

 

```

kawa.pycharm/

├── build.gradle.kts

├── settings.gradle.kts

├── gradle.properties

├── README.md

├── DEVELOPER_GUIDE.md

├── USER_GUIDE.md

├── CHANGELOG.md

├── LICENSE

├── src/

│   ├── main/

│   │   ├── java/com/codeawareness/pycharm/

│   │   │   ├── CodeAwarenessApplicationService.java

│   │   │   ├── CodeAwarenessProjectService.java

│   │   │   ├── communication/

│   │   │   │   ├── SocketManager.java

│   │   │   │   ├── UnixSocketAdapter.java

│   │   │   │   ├── WindowsNamedPipeAdapter.java

│   │   │   │   ├── CatalogConnection.java

│   │   │   │   ├── IpcConnection.java

│   │   │   │   ├── MessageProtocol.java

│   │   │   │   ├── Message.java

│   │   │   │   ├── MessageBuilder.java

│   │   │   │   └── MessageParser.java

│   │   │   ├── events/

│   │   │   │   ├── EventDispatcher.java

│   │   │   │   ├── EventHandler.java

│   │   │   │   ├── ResponseHandlerRegistry.java

│   │   │   │   └── handlers/

│   │   │   │       ├── PeerSelectHandler.java

│   │   │   │       ├── PeerUnselectHandler.java

│   │   │   │       ├── BranchSelectHandler.java

│   │   │   │       ├── AuthInfoHandler.java

│   │   │   │       ├── ContextAddHandler.java

│   │   │   │       └── ContextDeleteHandler.java

│   │   │   ├── highlighting/

│   │   │   │   ├── HighlightManager.java

│   │   │   │   ├── HighlightType.java

│   │   │   │   └── ColorSchemeProvider.java

│   │   │   ├── diff/

│   │   │   │   ├── DiffViewerManager.java

│   │   │   │   └── TempFileManager.java

│   │   │   ├── listeners/

│   │   │   │   ├── FileChangeListener.java

│   │   │   │   └── ActiveFileListener.java

│   │   │   ├── monitoring/

│   │   │   │   └── FileMonitor.java

│   │   │   ├── ui/

│   │   │   │   ├── CodeAwarenessStatusBarWidget.java

│   │   │   │   ├── NotificationHelper.java

│   │   │   │   └── actions/

│   │   │   │       ├── RefreshAction.java

│   │   │   │       ├── ClearHighlightsAction.java

│   │   │   │       └── ConnectionStatusAction.java

│   │   │   ├── settings/

│   │   │   │   ├── CodeAwarenessSettings.java

│   │   │   │   └── CodeAwarenessConfigurable.java

│   │   │   ├── auth/

│   │   │   │   └── AuthManager.java

│   │   │   ├── project/

│   │   │   │   └── ProjectManager.java

│   │   │   └── utils/

│   │   │       ├── GuidGenerator.java

│   │   │       ├── PathUtils.java

│   │   │       └── Logger.java

│   │   └── resources/

│   │       └── META-INF/

│   │           ├── plugin.xml

│   │           └── pluginIcon.svg

│   └── test/

│       └── java/com/codeawareness/pycharm/

│           ├── SocketManagerTest.java

│           ├── MessageProtocolTest.java

│           ├── EventDispatcherTest.java

│           ├── HighlightManagerTest.java

│           └── IntegrationTest.java

└── .github/

    └── workflows/

        ├── build.yml

        └── test.yml

```

 

---

 

## Appendix B: Message Protocol Reference

 

### Outgoing Messages

 

#### 1. Client Registration

```json

{

  "flow": "req",

  "domain": "*",

  "action": "clientId",

  "data": {

    "guid": "12345-678901"

  },

  "caw": "12345-678901"

}

```

 

#### 2. Active Path Notification

```json

{

  "flow": "req",

  "domain": "code",

  "action": "active-path",

  "data": {

    "fpath": "/path/to/file.py",

    "doc": "file.py",

    "caw": "12345-678901"

  },

  "caw": "12345-678901"

}

```

 

#### 3. File Saved Notification

```json

{

  "flow": "req",

  "domain": "code",

  "action": "file-saved",

  "data": {

    "fpath": "/path/to/file.py",

    "doc": "file.py",

    "caw": "12345-678901"

  },

  "caw": "12345-678901"

}

```

 

#### 4. Request Authentication Info

```json

{

  "flow": "req",

  "domain": "*",

  "action": "auth:info",

  "data": null,

  "caw": "12345-678901"

}

```

 

#### 5. Request Peer Diff

```json

{

  "flow": "req",

  "domain": "code",

  "action": "diff-peer",

  "data": {

    "origin": "https://github.com/user/repo.git",

    "fpath": "/path/to/file.py",

    "peer": "peer-guid",

    "caw": "12345-678901"

  },

  "caw": "12345-678901"

}

```

 

### Incoming Messages

 

#### 1. Peer Selection

```json

{

  "flow": "req",

  "domain": "code",

  "action": "peer:select",

  "data": {

    "peer": {

      "guid": "peer-guid",

      "name": "Alice",

      "email": "alice@example.com"

    }

  }

}

```

 

#### 2. Highlight Data

```json

{

  "flow": "res",

  "domain": "code",

  "action": "code:active-path",

  "data": {

    "hl": [10, 11, 12, 15, 20],  // 0-based line numbers

    "type": "peer"

  }

}

```

 

#### 3. Authentication Response

```json

{

  "flow": "res",

  "domain": "*",

  "action": "auth:info",

  "data": {

    "user": {

      "name": "John Doe",

      "email": "john@example.com"

    },

    "tokens": {

      "access": "token123",

      "refresh": "refresh456"

    }

  }

}

```

 

---

 

## Appendix C: Keyboard Shortcuts (Proposed)

 

| Action | Windows/Linux | macOS |

|--------|---------------|-------|

| Refresh Highlights | Ctrl+Alt+R | Cmd+Alt+R |

| Clear Highlights | Ctrl+Alt+C | Cmd+Alt+C |

| Connection Status | Ctrl+Alt+S | Cmd+Alt+S |

| Open Diff | Ctrl+Alt+D | Cmd+Alt+D |

 

---

 

## Appendix D: Version History

 

| Version | Date | Changes |

|---------|------|---------|

| 1.0.0 | TBD | Initial release |

 

---

 

## Conclusion

 

This implementation plan provides a clear, phase-by-phase roadmap for developing the Code Awareness PyCharm plugin. The plan prioritizes critical infrastructure (Phase 1), followed by core features (Phase 2-3), and then advanced features and polish (Phase 4-5). With an estimated timeline of 16-24 days and a low-risk assessment, this plan sets the foundation for a successful implementation.

 

**Next Steps:**

1. Review and approve this implementation plan

2. Set up development environment

3. Begin Phase 1: Foundation

4. Track progress against success criteria

5. Iterate based on testing and feedback

 

---

 

**Document Version:** 1.0

**Last Updated:** November 16, 2025

**Author:** Code Awareness Team
