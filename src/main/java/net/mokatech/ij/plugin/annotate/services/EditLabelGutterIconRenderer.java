package net.mokatech.ij.plugin.annotate.services;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import net.mokatech.ij.plugin.annotate.model.AnnotationInfos;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import java.util.Objects;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;
import net.mokatech.ij.plugin.annotate.ui.AnnotationSettingsDialog;

class EditLabelGutterIconRenderer extends GutterIconRenderer {
    private final AnnotationInfos annotation;

    public EditLabelGutterIconRenderer(AnnotationInfos annotation) {
        this.annotation = annotation;
    }

    @Override
    public @NotNull Icon getIcon() {
        return AllIcons.Actions.Annotate;
    }

    @Override
    public String getTooltipText() {
        return "Edit annotation label and color";
    }

    @Override
    public @NotNull Alignment getAlignment() {
        return Alignment.RIGHT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditLabelGutterIconRenderer that = (EditLabelGutterIconRenderer) o;
        return annotation == that.annotation;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(annotation);
    }

    @Override
    public AnAction getClickAction() {
        return new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                Project project = e.getProject();
                Editor editor = e.getData(CommonDataKeys.EDITOR);
                if (project == null || editor == null) return;
                AnnotationService service = AnnotationService.getInstance(project);
                String oldLabel = annotation.label;
                String oldColor = annotation.colorHex;
                AnnotationSettingsDialog dialog = new AnnotationSettingsDialog(project, annotation);
                if (dialog.showAndGet()) {
                    String newLabel = dialog.getLabel();
                    String newColor = dialog.getColorHex();
                    if (!Objects.equals(newLabel, oldLabel) || !Objects.equals(newColor, oldColor)) {
                        annotation.label = newLabel;
                        annotation.colorHex = newColor;
                        service.registerMarker(editor, annotation);
                    }
                }
            }
        };
    }
}