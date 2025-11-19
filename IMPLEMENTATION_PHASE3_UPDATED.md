# Phase 3: UI Integration (UPDATED) - Simplified Requirements

## Overview

Phase 3 has been simplified based on updated requirements:
- **Single highlight type** (no conflict/overlap/peer/modified distinction)
- **Full-width line highlights** only (no ranges)
- **Status bar widget** with ON/OFF toggle
- **Notifications** for mode changes

## Phase 3.1: Highlighting System (1 day)

### Tasks
- [ ] Implement `HighlightManager` service
- [ ] Implement color scheme provider (light/dark theme support)
- [ ] Add full-width line highlights using `RangeHighlighter` API
- [ ] Store highlighters in registry (`Map<String, RangeHighlighter>`)
- [ ] Implement highlight clearing
- [ ] Handle highlight updates on server response
- [ ] Implement toggle visibility based on Code Awareness mode (ON/OFF)

### Deliverable
Working full-width line highlighting that can be toggled ON/OFF

### Implementation

```java
MarkupModel markupModel = editor.getMarkupModel();
TextAttributes attributes = new TextAttributes();
attributes.setBackgroundColor(highlightColor);

RangeHighlighter highlighter = markupModel.addRangeHighlighter(
    lineStartOffset,
    lineEndOffset,
    HighlighterLayer.SELECTION - 1,
    attributes,
    HighlighterTargetArea.LINES_IN_RANGE  // Full-width highlighting
);
```

### Color Scheme

| Theme | Highlight Color |
|-------|-----------------|
| Light | #ffdd34 (yellow) |
| Dark  | #1f1cc2 (blue)   |

### Files Created
```
src/main/java/com/codeawareness/pycharm/highlighting/HighlightManager.java
src/main/java/com/codeawareness/pycharm/highlighting/ColorSchemeProvider.java
src/main/java/com/codeawareness/pycharm/events/handlers/HighlightHandler.java
```

### Test Cases
- Apply full-width highlight to line
- Clear all highlights
- Toggle highlights ON/OFF
- Switch between light/dark themes
- Handle editor close

---

## Phase 3.2: Status Bar Widget (0.5 day)

### Tasks
- [ ] Implement `StatusBarWidgetFactory`
- [ ] Create status bar widget UI
- [ ] Show Code Awareness mode (ON/OFF)
- [ ] Add click action to toggle mode
- [ ] Show notification when toggling mode
- [ ] Update widget appearance based on mode

### Deliverable
Status bar widget with toggle functionality

### Implementation

```xml
<extensions defaultExtensionNs="com.intellij">
  <statusBarWidgetFactory
      implementation="com.codeawareness.pycharm.ui.CodeAwarenessStatusBarWidget"/>
</extensions>
```

### Widget Behavior
- **ON**: Shows highlights, widget displays "Code Awareness: ON"
- **OFF**: Hides highlights, widget displays "Code Awareness: OFF"
- **Click**: Toggle between ON/OFF, show notification

### Files Created
```
src/main/java/com/codeawareness/pycharm/ui/CodeAwarenessStatusBarWidget.java
src/main/java/com/codeawareness/pycharm/settings/CodeAwarenessSettings.java
```

### Test Cases
- Widget appears in status bar
- Click toggles mode ON/OFF
- Highlights show/hide based on mode
- Notification appears on toggle
- Widget text updates correctly

---

## Phase 3.3: Notifications (0.5 day)

### Tasks
- [ ] Implement notification helper
- [ ] Show "Code Awareness ON" notification
- [ ] Show "Code Awareness OFF" notification
- [ ] Add notification group configuration

### Deliverable
User-facing notifications for mode toggle

### Implementation

```java
NotificationGroupManager.getInstance()
    .getNotificationGroup("Code Awareness")
    .createNotification("Code Awareness: ON", NotificationType.INFORMATION)
    .notify(project);
```

### Files Created
```
src/main/java/com/codeawareness/pycharm/ui/NotificationHelper.java
```

### Test Cases
- Notification shows when toggling ON
- Notification shows when toggling OFF
- Notifications don't spam (debounced)

---

## Phase 3 Success Criteria

- [x] Full-width line highlights appear in editor
- [x] Status bar widget shows mode (ON/OFF)
- [x] Click widget toggles mode and shows notification
- [x] Highlights show/hide based on mode
- [x] UI works in both light and dark themes
- [x] No performance degradation with highlights
