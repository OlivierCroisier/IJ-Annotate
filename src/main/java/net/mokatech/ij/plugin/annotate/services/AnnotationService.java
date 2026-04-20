package net.mokatech.ij.plugin.annotate.services;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import net.mokatech.ij.plugin.annotate.actions.AnnotateAction;
import net.mokatech.ij.plugin.annotate.model.AnnotationInfos;
import net.mokatech.ij.plugin.annotate.ui.AnnotationLabelRenderer;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;


@State(name = "AnnotationInfos", storages = @Storage("net.mokatech.annotate.xml"))
@Service(Service.Level.PROJECT)
public final class AnnotationService implements PersistentStateComponent<AnnotationState>, Disposable {

    public static AnnotationService getInstance(Project project) {
        return project.getService(AnnotationService.class);
    }


    private AnnotationState myState = new AnnotationState();

    final Map<AnnotationInfos, Visuals> activeVisuals = new HashMap<>();




    public void addAnnotation(AnnotationInfos annotation) {
        myState.annotations.add(annotation);
    }

    public boolean hasAnnotationsForProject() {
        return !myState.annotations.isEmpty();
    }

    public boolean hasAnnotationsForFile(String path) {
        return myState.annotations.stream().anyMatch(a -> a.filePath.equals(path));
    }

    public List<AnnotationInfos> findForFile(String path) {
        return myState.annotations.stream().filter(a -> a.filePath.equals(path)).toList();
    }

    public void removeForFile(String path) {
        activeVisuals.entrySet().removeIf(entry -> {
            if (entry.getKey().filePath.equals(path)) {
                entry.getValue().dispose();
                return true;
            }
            return false;
        });
        myState.annotations.removeIf(a -> a.filePath.equals(path));
    }

    public void removeAnnotation(AnnotationInfos annotation) {
        myState.annotations.remove(annotation);
        Visuals visuals = activeVisuals.remove(annotation);
        if (visuals != null) {
            visuals.dispose();
        }
    }

    public void removeForProject() {
        activeVisuals.values().forEach(Visuals::dispose);
        activeVisuals.clear();

        myState.annotations.clear();
    }


  public void registerMarker(Editor editor, AnnotationInfos ann) {
    Visuals visuals = createVisuals(editor, ann);
    Visuals oldVisuals = activeVisuals.put(ann, visuals);
    if (oldVisuals != null) {
      oldVisuals.dispose();
    }
  }

    private Visuals createVisuals(Editor editor, AnnotationInfos ann) {
        Document document = editor.getDocument();

        RangeMarker marker = document.createRangeMarker(ann.startOffset, ann.endOffset);
        marker.setGreedyToLeft(true);
        marker.setGreedyToRight(true);

        Color color = Color.decode(ann.colorHex);

        Visuals visuals = new Visuals();
        visuals.marker = marker;

        visuals.highlighter = editor.getMarkupModel().addRangeHighlighter(
                marker.getStartOffset(),
                marker.getEndOffset(),
                HighlighterLayer.SELECTION - 1,
                new TextAttributes(null, null, color, EffectType.ROUNDED_BOX, Font.PLAIN),
                HighlighterTargetArea.EXACT_RANGE
        );
        visuals.highlighter.setTextAttributesKey(AnnotateAction.TEXT_ATTRIBUTES_KEY);
        visuals.highlighter.setGreedyToLeft(true);
        visuals.highlighter.setGreedyToRight(true);
        visuals.highlighter.setGutterIconRenderer(new EditLabelGutterIconRenderer(ann));

        if (ann.label != null && !ann.label.isBlank()) {
            visuals.inlay = editor.getInlayModel().addBlockElement(
                    marker.getStartOffset(), true, true, 1,
                    new AnnotationLabelRenderer(ann.label));
        }

        return visuals;
    }

    public void clearVisuals(Editor editor) {
        // Kept for safety, though visuals now tracked
        Stream.of(editor.getMarkupModel().getAllHighlighters())
            .filter(highlighter -> AnnotateAction.TEXT_ATTRIBUTES_KEY.equals(highlighter.getTextAttributesKey()))
            .forEach(highlighter -> editor.getMarkupModel().removeHighlighter(highlighter));

        editor.getInlayModel()
            .getBlockElementsInRange(0, editor.getDocument().getTextLength(), AnnotationLabelRenderer.class)
            .forEach(Disposable::dispose);
    }

    @Override
    public AnnotationState getState() {
        activeVisuals.forEach((annot, v) -> {
            if (v.marker != null && v.marker.isValid()) {
                annot.startOffset = v.marker.getStartOffset();
                annot.endOffset = v.marker.getEndOffset();
            }
        });
        return myState;
    }

    @Override
    public void loadState(@NotNull AnnotationState state) {
        myState = state;
    }

    public void loadMarkersForEditor(Editor editor, String filePath) {
        List<AnnotationInfos> anns = findForFile(filePath);
        for (AnnotationInfos annot : anns) {
            Visuals v = activeVisuals.get(annot);
            if (v != null && v.marker != null && v.marker.isValid() && v.marker.getDocument() == editor.getDocument()) {
                annot.startOffset = v.marker.getStartOffset();
                annot.endOffset = v.marker.getEndOffset();
            }
        }
        // Dispose old visuals
        for (AnnotationInfos annot : anns) {
            Visuals v = activeVisuals.remove(annot);
            if (v != null) {
                v.dispose();
            }
        }
        clearVisuals(editor); // safety
        for (AnnotationInfos annot : anns) {
            registerMarker(editor, annot);
        }
    }


    @Override
    public void dispose() {
        activeVisuals.values().forEach(Visuals::dispose);
        activeVisuals.clear();
    }

}
