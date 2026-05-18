package net.mokatech.ij.plugin.annotate.listeners;

import com.intellij.util.messages.Topic;

public interface AnnotationChangeListener {

    // Communication channel to broadcast annotation-related events
    Topic<AnnotationChangeListener> ANNOTATION_CHANGED_TOPIC =
        Topic.create("annotation-changed", AnnotationChangeListener.class);

    void annotationsChanged();
}
