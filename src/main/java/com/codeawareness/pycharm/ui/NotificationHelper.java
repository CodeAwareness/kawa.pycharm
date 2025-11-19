package com.codeawareness.pycharm.ui;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for showing notifications to the user.
 */
public class NotificationHelper {

    private static final String NOTIFICATION_GROUP_ID = "Code Awareness";

    /**
     * Show a notification that Code Awareness has been turned ON.
     */
    public static void notifyEnabled(@Nullable Project project) {
        showNotification(
            "Code Awareness: ON",
            "Code highlights are now visible",
            NotificationType.INFORMATION,
            project
        );
    }

    /**
     * Show a notification that Code Awareness has been turned OFF.
     */
    public static void notifyDisabled(@Nullable Project project) {
        showNotification(
            "Code Awareness: OFF",
            "Code highlights are now hidden",
            NotificationType.INFORMATION,
            project
        );
    }

    /**
     * Show an information notification.
     */
    public static void showInfo(String title, String content, @Nullable Project project) {
        showNotification(title, content, NotificationType.INFORMATION, project);
    }

    /**
     * Show a warning notification.
     */
    public static void showWarning(String title, String content, @Nullable Project project) {
        showNotification(title, content, NotificationType.WARNING, project);
    }

    /**
     * Show an error notification.
     */
    public static void showError(String title, String content, @Nullable Project project) {
        showNotification(title, content, NotificationType.ERROR, project);
    }

    /**
     * Show a notification.
     */
    private static void showNotification(String title, String content, NotificationType type, @Nullable Project project) {
        try {
            NotificationGroupManager manager = NotificationGroupManager.getInstance();
            if (manager == null) {
                return;
            }

            NotificationGroup group = manager.getNotificationGroup(NOTIFICATION_GROUP_ID);
            if (group == null) {
                return;
            }

            Notification notification = group.createNotification(title, content, type);

            notification.notify(project);
        } catch (Throwable ignored) {
            // Headless/unit-test environment: silently ignore notification attempts
        }
    }
}
