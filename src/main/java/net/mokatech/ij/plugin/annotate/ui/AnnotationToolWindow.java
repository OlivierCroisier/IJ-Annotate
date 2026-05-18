package net.mokatech.ij.plugin.annotate.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.messages.MessageBusConnection;
import net.mokatech.ij.plugin.annotate.listeners.AnnotationChangeListener;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AnnotationToolWindow extends SimpleToolWindowPanel implements Disposable {

    private final AnnotationFileListPanel annotationPanel;
    private final MessageBusConnection connection;

    public AnnotationToolWindow(Project project) {
        super(true, true);

        annotationPanel = new AnnotationFileListPanel(project);
        setToolbar(createToolbar());
        setContent(annotationPanel);

        connection = project.getMessageBus().connect();
        connection.subscribe(AnnotationChangeListener.ANNOTATION_CHANGED_TOPIC,
                (AnnotationChangeListener) this::refreshAnnotations);
    }

    private JComponent createToolbar() {
        ActionGroup group = new DefaultActionGroup(
                new ExpandAllAction(),
                new CollapseAllAction()
        );
        return ActionManager.getInstance()
                .createActionToolbar("AnnotationsToolbar", group, true)
                .getComponent();
    }

    private class ExpandAllAction extends AnAction {
        ExpandAllAction() {
            super("Expand All", "Expand all", AllIcons.Actions.Expandall);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            annotationPanel.expandAll();
        }
    }

    private class CollapseAllAction extends AnAction {
        CollapseAllAction() {
            super("Collapse All", "Collapse all", AllIcons.Actions.Collapseall);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            annotationPanel.collapseAll();
        }
    }

    public void refreshAnnotations() {
        annotationPanel.refresh();
    }

    @Override
    public void dispose() {
        if (connection != null) {
            connection.disconnect();
        }
    }

}
