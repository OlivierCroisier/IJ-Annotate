package net.mokatech.ij.plugin.annotate.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectAnnotationInitializer implements ProjectActivity {

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        AnnotationService service = AnnotationService.getInstance(project);
        FileEditorManager fem = FileEditorManager.getInstance(project);

        // Reload annotations for automatically-reopened editors
        Runnable annotationLoadingAction = () ->
                WriteAction.run(() -> {
                    for (FileEditor fe : fem.getAllEditors()) {
                        if (fe instanceof TextEditor textEditor) {
                            Editor editor = textEditor.getEditor();
                            VirtualFile vf = editor.getVirtualFile();
                            if (vf != null) {
                                service.loadMarkersForEditor(editor, vf.getPath());
                            }
                        }
                    }
                });
        ApplicationManager.getApplication().invokeLater(annotationLoadingAction, ModalityState.defaultModalityState());

        // Install the file tracker
        VirtualFileManager.getInstance().addAsyncFileListener(new FileTrackerListener(project), service);

        return null;
    }
}