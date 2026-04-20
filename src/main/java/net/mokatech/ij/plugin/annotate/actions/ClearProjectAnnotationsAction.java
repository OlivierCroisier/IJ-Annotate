package net.mokatech.ij.plugin.annotate.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.ActionUpdateThread;

public class ClearProjectAnnotationsAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        AnnotationService service = AnnotationService.getInstance(project);
        service.removeForProject();

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        for (FileEditor fileEditor : fileEditorManager.getAllEditors()) {
            if (fileEditor instanceof TextEditor) {
                Editor editor = ((TextEditor) fileEditor).getEditor();
                service.clearVisuals(editor);
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            AnnotationService annotationService = AnnotationService.getInstance(project);
            e.getPresentation().setEnabled(annotationService.hasAnnotationsForProject());
        }
    }

}