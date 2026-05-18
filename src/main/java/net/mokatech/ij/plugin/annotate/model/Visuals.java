package net.mokatech.ij.plugin.annotate.model;

import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.RangeHighlighter;

public class Visuals {

    // Tracks caret indexes when files are edited
    RangeMarker marker;

    // Colored border + label
    RangeHighlighter highlighter;
    Inlay<?> inlay;

    public RangeMarker getMarker() {
        return marker;
    }

    public void setMarker(RangeMarker marker) {
        this.marker = marker;
    }

    public RangeHighlighter getHighlighter() {
        return highlighter;
    }

    public void setHighlighter(RangeHighlighter highlighter) {
        this.highlighter = highlighter;
    }

    public Inlay<?> getInlay() {
        return inlay;
    }

    public void setInlay(Inlay<?> inlay) {
        this.inlay = inlay;
    }

    public void dispose() {
        if (marker != null && marker.isValid()) {
            marker.dispose();
        }
        if (highlighter != null) {
            highlighter.dispose();
        }
        if (inlay != null) {
            inlay.dispose();
        }
    }
}