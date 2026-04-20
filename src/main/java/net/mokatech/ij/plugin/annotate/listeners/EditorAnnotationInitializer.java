package net.mokatech.ij.plugin.annotate.listeners;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;
import org.jetbrains.annotations.NotNull;

public class EditorAnnotationInitializer implements FileEditorManagerListener {

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        Project project = source.getProject();
        AnnotationService service = AnnotationService.getInstance(project);
        FileEditor[] editors = source.getEditors(file);
        for (FileEditor fileEditor : editors) {
            if (fileEditor instanceof TextEditor textEditor) {
                Editor editor = textEditor.getEditor();
                service.loadMarkersForEditor(editor, file.getPath());
            }
        }
    }

}