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

/**
 * When files are opened in an Editor, this class recreates the Visuals for the files' annotations (if any)
 */
public class OnFileOpenAnnotationInitializer implements FileEditorManagerListener {

    @Override
    public void fileOpened(@NotNull FileEditorManager manager, @NotNull VirtualFile file) {
        Project project = manager.getProject();
        FileEditor[] editors = manager.getEditors(file);
        AnnotationService service = AnnotationService.getInstance(project);
        for (FileEditor fileEditor : editors) {
            if (fileEditor instanceof TextEditor textEditor) {
                Editor editor = textEditor.getEditor();
                service.createVisualsForEditor(editor, file.getPath());
            }
        }
    }

}