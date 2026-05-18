package net.mokatech.ij.plugin.annotate.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;
import org.jetbrains.annotations.NotNull;

/**
 * An action that clears all annotations related to the current project.
 * This is typically used to reset or remove stored annotations within the context of the project.
 */
public class ClearProjectAnnotationsAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        AnnotationService service = AnnotationService.getInstance(project);
        service.removeAnnotationsForProject();
    }

    /**
     * Tells if the action should be available/active or not.
     * Condition: the project has annotations
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            AnnotationService annotationService = AnnotationService.getInstance(project);
            e.getPresentation().setEnabled(annotationService.hasAnnotationsForProject());
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

}