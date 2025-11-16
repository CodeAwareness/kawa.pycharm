package com.codeawareness.pycharm.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Settings configurable for Code Awareness plugin.
 * Provides UI for plugin configuration in PyCharm settings.
 */
public class CodeAwarenessConfigurable implements Configurable {

    @Nls
    @Override
    public String getDisplayName() {
        return "Code Awareness";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        // Implementation will be added in Phase 3.5
        return new JLabel("Code Awareness settings (coming soon)");
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {
        // Implementation will be added in Phase 3.5
    }
}
