package net.mokatech.ij.plugin.annotate.services;

import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.RangeHighlighter;

class Visuals {
    RangeMarker marker;
    RangeHighlighter highlighter;
    Inlay<?> inlay;

    void dispose() {
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