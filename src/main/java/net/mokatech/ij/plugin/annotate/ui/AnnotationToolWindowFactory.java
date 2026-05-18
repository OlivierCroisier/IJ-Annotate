package net.mokatech.ij.plugin.annotate.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class responsible for creating the content of the Annotation Tool Window.
 */
public class AnnotationToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("Annotations");
        toolWindow.setStripeTitle("Annotations");
        AnnotationToolWindow panel = new AnnotationToolWindow(project);
        Disposer.register(toolWindow.getDisposable(), panel);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        content.setTabName(null);
        toolWindow.getContentManager().addContent(content);
    }

}
