package net.mokatech.ij.plugin.annotate.model;

public class AnnotationInfos {

    public String filePath;
    public int startOffset;
    public int endOffset;
    public String label;
    public String colorHex;

    // Empty constructor required for serialization
    public AnnotationInfos() {}

    public AnnotationInfos(String filePath, int start, int end, String label, String colorHex) {
        this.filePath = filePath;
        this.startOffset = start;
        this.endOffset = end;
        this.label = label;
        this.colorHex = colorHex;
    }

}
