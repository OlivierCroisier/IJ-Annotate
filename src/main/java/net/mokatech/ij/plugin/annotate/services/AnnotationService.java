package net.mokatech.ij.plugin.annotate.services;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import net.mokatech.ij.plugin.annotate.actions.AnnotateAction;
import net.mokatech.ij.plugin.annotate.listeners.AnnotationChangeListener;
import net.mokatech.ij.plugin.annotate.model.AnnotationInfos;
import net.mokatech.ij.plugin.annotate.model.Visuals;
import net.mokatech.ij.plugin.annotate.ui.AnnotationLabelRenderer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@State(name = "AnnotationInfos", storages = @Storage("net.mokatech.annotate.xml"))
@Service(Service.Level.PROJECT)
public final class AnnotationService implements PersistentStateComponent<AnnotationState>, Disposable {

    public static AnnotationService getInstance(Project project) {
        return project.getService(AnnotationService.class);
    }

    private final Project myProject;

    // Annotations state, both in-memory and serialized when saved by the plugin
    private AnnotationState myState = new AnnotationState();

    // Runtime association of non-serializable Visuals for Annotations
    // AnnotationInfos = IJ-agnostic infos about file, start/stop index, color, label...
    // Visuals = runtime editor representation of the annotationInfos (RangeMarker, Inlay...)
    final Map<AnnotationInfos, Visuals> visualsForAnnotations = new HashMap<>();

    public AnnotationService(Project project) {
        myProject = project;
    }

    // ================================================================================
    // Annotations management
    // ================================================================================

    public void addAnnotation(Editor editor, AnnotationInfos annotation) {
        myState.annotations.add(annotation);
        // Also create the related Visuals, and broadcast the change
        createVisuals(editor, annotation);
        fireChanged();
    }

    public List<AnnotationInfos> getAllAnnotations() {
        return new ArrayList<>(myState.annotations);
    }

    public List<AnnotationInfos> getAllAnnotationsForFile(String path) {
        return myState.annotations.stream()
                .filter(a -> a.filePath.equals(path))
                .toList();
    }

    public boolean hasAnnotationsForProject() {
        return !myState.annotations.isEmpty();
    }

    public boolean hasAnnotationsForFile(String path) {
        return myState.annotations.stream().anyMatch(a -> a.filePath.equals(path));
    }

    public void removeAnnotation(AnnotationInfos annotation) {
        myState.annotations.remove(annotation);
        // Also remove associated Visuals
        Visuals visuals = visualsForAnnotations.remove(annotation);
        if (visuals != null) {
            visuals.dispose();
        }
        // Broadcast the change
        fireChanged();
    }

    public void removeAnnotationsForFile(String path) {
        getAllAnnotationsForFile(path).forEach(this::removeAnnotation);
    }

    public void removeAnnotationsForProject() {
        visualsForAnnotations.values().forEach(Visuals::dispose);
        visualsForAnnotations.clear();
        myState.annotations.clear();
        fireChanged();
    }

    // ================================================================================
    // Visuals management
    // ================================================================================

    public void createVisuals(Editor editor, AnnotationInfos annotation) {
        // Create a RangeMarker that will track index changes as the document is edited
        Document document = editor.getDocument();
        RangeMarker marker = document.createRangeMarker(annotation.startOffset, annotation.endOffset);
        marker.setGreedyToLeft(true);
        marker.setGreedyToRight(true);

        // Create a RangeHighlighter to draw the colored border
        RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(
                marker.getStartOffset(),
                marker.getEndOffset(),
                HighlighterLayer.SELECTION - 1,
                new TextAttributes(null, null, Color.decode(annotation.colorHex), EffectType.ROUNDED_BOX, Font.PLAIN),
                HighlighterTargetArea.EXACT_RANGE
        );
        highlighter.setTextAttributesKey(AnnotateAction.TEXT_ATTRIBUTES_KEY);
        highlighter.setGreedyToLeft(true);
        highlighter.setGreedyToRight(true);
        highlighter.setGutterIconRenderer(new EditAnnotationGutterIconRenderer(annotation));

        // Create the Inlay (label)
        Inlay<AnnotationLabelRenderer> inlay = null;
        if (annotation.label != null && !annotation.label.isBlank()) {
            inlay = editor.getInlayModel().addBlockElement(
                    marker.getStartOffset(), true, true, 1,
                    new AnnotationLabelRenderer(annotation.label));
        }

        // Assemble the Visuals with all of the above
        Visuals visuals = new Visuals();
        visuals.setMarker(marker);
        visuals.setHighlighter(highlighter);
        if (inlay != null) {
            visuals.setInlay(inlay);
        }

        // If previous Visuals were already associated with this Annotation (which should never happen), remove them
        Visuals oldVisuals = visualsForAnnotations.put(annotation, visuals);
        if (oldVisuals != null) {
            oldVisuals.dispose();
        }
    }

    // Recreate the Visuals from the Annotations (if any) when a file is loaded in an Editor
    public void createVisualsForEditor(Editor editor, String filePath) {
        List<AnnotationInfos> anns = getAllAnnotationsForFile(filePath);
        for (AnnotationInfos annot : anns) {
            Visuals currentVisuals = visualsForAnnotations.get(annot);
            if (currentVisuals != null) {
                currentVisuals.dispose();
            }
            createVisuals(editor, annot);
        }
    }

    // ================================================================================
    // State serialisation
    // ================================================================================

    @Override
    public AnnotationState getState() {
        visualsForAnnotations.forEach((annotation, visuals) -> {
            RangeMarker marker = visuals.getMarker();
            if (marker != null && marker.isValid()) {
                annotation.startOffset = marker.getStartOffset();
                annotation.endOffset = marker.getEndOffset();
            }
        });
        return myState;
    }

    @Override
    public void loadState(@NotNull AnnotationState state) {
        myState = state;
    }

    @Override
    public void dispose() {
        visualsForAnnotations.values().forEach(Visuals::dispose);
        visualsForAnnotations.clear();
    }

    // ================================================================================
    // Other
    // ================================================================================

    private void fireChanged() {
        myProject.getMessageBus().syncPublisher(AnnotationChangeListener.ANNOTATION_CHANGED_TOPIC).annotationsChanged();
    }

}
