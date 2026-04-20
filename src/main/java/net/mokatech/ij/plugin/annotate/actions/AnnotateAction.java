package net.mokatech.ij.plugin.annotate.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import net.mokatech.ij.plugin.annotate.model.AnnotationInfos;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;
import net.mokatech.ij.plugin.annotate.ui.AnnotationSettingsDialog;
import org.jetbrains.annotations.NotNull;

public class AnnotateAction extends AnAction {

    public static final String KEY = "net.mokatech.ij.plugin.annotate";

    public static final TextAttributesKey TEXT_ATTRIBUTES_KEY = TextAttributesKey.createTextAttributesKey(KEY);

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        boolean hasSelection = editor != null && editor.getSelectionModel().hasSelection();
        e.getPresentation().setEnabledAndVisible(hasSelection);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) return;

        AnnotationService annotationService = AnnotationService.getInstance(project);

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) return;

        AnnotationSettingsDialog dialog = new AnnotationSettingsDialog(project, null);
        if (dialog.showAndGet()) {
            String label = dialog.getLabel();
            String hex = dialog.getColorHex();

            int startOffset = selectionModel.getSelectionStart();
            int endOffset = selectionModel.getSelectionEnd();

            AnnotationInfos annotation = new AnnotationInfos(
                    editor.getVirtualFile().getPath(), startOffset, endOffset, label, hex);
            AnnotationService.getInstance(project).addAnnotation(annotation);

            annotationService.registerMarker(editor, annotation);
        }
    }

}