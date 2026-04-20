package net.mokatech.ij.plugin.annotate.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.ActionUpdateThread;

public class ClearFileAnnotationsAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || editor == null || file == null) return;

        AnnotationService service = AnnotationService.getInstance(project);
        service.removeForFile(file.getPath());
        service.clearVisuals(editor);
    }

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
}
