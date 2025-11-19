# Code Awareness PyCharm Plugin - Developer Guide

## Overview

This guide provides technical details for developers working on the Code Awareness PyCharm plugin.

## Architecture

The plugin follows a 3-layer architecture:

1. **Communication Layer** - Socket I/O and message protocol
2. **Event Handling** - Message routing and event dispatch
3. **UI Integration** - Editor highlighting and status display

## Connection Flow

### 1. Initialization

```java
CodeAwarenessApplicationService appService =
    ApplicationManager.getApplication().getService(CodeAwarenessApplicationService.class);
```

### 2. Connection Sequence

```
Client (PyCharm)                Catalog Service           IPC Service
     |                                |                        |
     |----(1) Connect & Register---->|                        |
     |<---(2) ACK--------------------|                        |
     |                                |                        |
     |                                |---(3) Create Socket--->|
     |                                |                        |
     |----(4) Wait for Socket-------------------------------->|
     |<---(5) Connect----------------------------------|
     |                                |                        |
     |----(6) Send/Receive Messages----------------------->|
```

**Steps:**
1. Connect to catalog service (`~/.kawa-code/sockets/caw.catalog`)
2. Register client with `clientId` message containing GUID
3. Catalog creates local IPC socket (`~/.kawa-code/sockets/caw.<GUID>`)
4. Plugin polls for IPC socket availability (max 10 seconds)
5. Connect to IPC socket
6. Start background thread to read incoming messages
7. Ready to send/receive messages

### 3. Code Example

```java
// Connect
appService.connect();

// Send message
Message message = MessageBuilder.buildActivePath(
    appService.getClientGuid(),
    "/path/to/file.py",
    "file.py"
);
appService.getIpcConnection().sendMessage(message);

// Handle incoming messages
appService.getIpcConnection().setMessageCallback(msg -> {
    Logger.info("Received: " + msg.getAction());
});
```

## Message Protocol

### Format

Messages are JSON with form-feed delimiter (`\f`):

```json
{
  "flow": "req|res|err",
  "domain": "code|auth|*",
  "action": "action_name",
  "data": {...},
  "caw": "client_guid"
}\f
```

### Common Messages

**Client Registration:**
```json
{
  "flow": "req",
  "domain": "*",
  "action": "clientId",
  "data": {"guid": "123456-789012"},
  "caw": "123456-789012"
}
```

**Active File Notification:**
```json
{
  "flow": "req",
  "domain": "code",
  "action": "active-path",
  "data": {
    "fpath": "/path/to/file.py",
    "doc": "file.py",
    "caw": "123456-789012"
  },
  "caw": "123456-789012"
}
```

**File Saved:**
```json
{
  "flow": "req",
  "domain": "code",
  "action": "file-saved",
  "data": {
    "fpath": "/path/to/file.py",
    "doc": "file.py",
    "caw": "123456-789012"
  },
  "caw": "123456-789012"
}
```

## Socket Paths

**Unix/Linux/macOS:**
- Catalog: `~/.kawa-code/sockets/caw.catalog`
- IPC: `~/.kawa-code/sockets/caw.<GUID>`

**Windows:**
- Catalog: `\\.\pipe\caw.catalog`
- IPC: `\\.\pipe\caw.<GUID>`

## Threading Model

### IntelliJ Platform Rules

1. **EDT (Event Dispatch Thread)** - All UI operations
2. **Background Thread** - All I/O and long-running operations
3. **Write Thread** - Document modifications

### Implementation

- **Socket I/O:** Background daemon thread (`CodeAwareness-IPC-Reader`)
- **Message handling:** Pooled background thread via `executeOnPooledThread()`
- **UI updates:** EDT via `invokeLater()`

## Error Handling

### Connection Failures

The plugin implements exponential backoff retry:
- Initial delay: 500ms
- Max delay: 8 seconds
- Max attempts: 10

### Recovery Strategy

- Socket errors → Automatic reconnection
- Malformed messages → Skip and continue
- Handler exceptions → Log and continue

## Testing

### Unit Tests

Run unit tests:
```bash
./gradlew test
```

### Manual Testing

1. Start Code Awareness backend
2. Load plugin in PyCharm
3. Call `appService.connect()`
4. Verify connection in logs

### Debug Logging

Enable debug mode:
```java
Logger.setDebugEnabled(true);
```

## Build

```bash
# Build plugin
./gradlew buildPlugin

# Run in IDE
./gradlew runIde

# Run tests
./gradlew test
```

## Project Structure

```
src/main/java/com/codeawareness/pycharm/
├── CodeAwarenessApplicationService.java  # Global state
├── CodeAwarenessProjectService.java      # Project state
├── communication/
│   ├── SocketAdapter.java                # Platform abstraction
│   ├── SocketManager.java                # Connection management
│   ├── UnixSocketAdapter.java            # Unix sockets
│   ├── WindowsNamedPipeAdapter.java      # Windows pipes
│   ├── Message.java                      # Message model
│   ├── MessageBuilder.java               # Message construction
│   ├── MessageProtocol.java              # JSON serialization
│   ├── MessageParser.java                # Buffered parsing
│   ├── CatalogConnection.java            # Catalog client
│   └── IpcConnection.java                # IPC client
├── events/
│   └── ResponseHandlerRegistry.java      # Async handlers
└── utils/
    ├── GuidGenerator.java                # Client ID generation
    ├── Logger.java                       # Logging
    └── PathUtils.java                    # Path handling
```

## Next Steps

- Phase 2: Event dispatcher, file monitoring, handlers
- Phase 3: UI integration (highlighting, status bar)
- Phase 4: Advanced features (diff viewer)
- Phase 5: Testing and distribution
