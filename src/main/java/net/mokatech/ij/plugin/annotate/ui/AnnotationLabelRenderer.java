package net.mokatech.ij.plugin.annotate.ui;

import com.intellij.codeInsight.daemon.impl.HintRenderer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.jetbrains.annotations.Nullable;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

public class AnnotationLabelRenderer extends HintRenderer {

    public AnnotationLabelRenderer(@Nullable String text) {
        super(text);
    }

    @Override
    public void paint(Inlay inlay, Graphics g, Rectangle r, TextAttributes effects) {
        Editor editor = inlay.getEditor();
        Document document = editor.getDocument();

        int lineNumber = document.getLineNumber(inlay.getOffset());
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int lineEndOffset = document.getLineEndOffset(lineNumber);
        CharSequence chars = document.getImmutableCharSequence();
        CharSequence lineText = chars.subSequence(lineStartOffset, lineEndOffset);
        int indentLength = 0;
        while (indentLength < lineText.length() && Character.isWhitespace(lineText.charAt(indentLength))) {
            indentLength++;
        }
        int indentOffset = lineStartOffset + indentLength;
        if (indentLength >= lineText.length()) {
            indentOffset = lineStartOffset;
        }
        Point p = editor.offsetToXY(indentOffset, true, false);
        int shift = p.x - r.x;
        if (shift < 0) {
            shift = 0;
        }

        Graphics gg = g.create();
        gg.translate(shift, 0);
        super.paint(inlay, gg, r, effects);
        gg.dispose();
    }

}
