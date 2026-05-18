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

/**
 * An action that allows users to add annotations to a selected region of text in an editor.
 * This action becomes available only when the user has an active text selection in the editor.
 */
public class AnnotateAction extends AnAction {

    public static final String KEY = "net.mokatech.ij.plugin.annotate";

    public static final TextAttributesKey TEXT_ATTRIBUTES_KEY = TextAttributesKey.createTextAttributesKey(KEY);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) return;

        // No selection: do nothing
        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) return;

        AnnotationSettingsDialog dialog = new AnnotationSettingsDialog(project, null);
        if (dialog.showAndGet()) {
            String label = dialog.getLabel();
            String hex = dialog.getColorHex();
            int startOffset = selectionModel.getSelectionStart();
            int endOffset = selectionModel.getSelectionEnd();

            // Create a new Annotation on selected contents
            AnnotationService annotationService = AnnotationService.getInstance(project);
            AnnotationInfos annotation = new AnnotationInfos(editor.getVirtualFile().getPath(), startOffset, endOffset, label, hex);
            annotationService.addAnnotation(editor, annotation);
        }
    }

    /**
     * Tells if the action should be available/active or not.
     * Condition: some text is selected in the editor
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        boolean hasSelection = editor != null && editor.getSelectionModel().hasSelection();
        e.getPresentation().setEnabledAndVisible(hasSelection);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}