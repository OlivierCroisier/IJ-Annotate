package net.mokatech.ij.plugin.annotate.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;
import org.jetbrains.annotations.NotNull;

/**
 * Action to clear annotations associated with a specific file in the project.
 */
public class ClearFileAnnotationsAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || file == null) return;

        AnnotationService service = AnnotationService.getInstance(project);
        service.removeAnnotationsForFile(file.getPath());
    }

    /**
     * Tells if the action should be available/active or not.
     * Condition: current file has annotations
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project != null && file != null) {
            AnnotationService service = AnnotationService.getInstance(project);
            e.getPresentation().setEnabled(service.hasAnnotationsForFile(file.getPath()));
        } else {
            e.getPresentation().setEnabled(false);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

}
