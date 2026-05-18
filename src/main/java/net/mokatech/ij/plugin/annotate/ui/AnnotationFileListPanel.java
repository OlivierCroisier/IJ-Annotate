package net.mokatech.ij.plugin.annotate.ui;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import net.mokatech.ij.plugin.annotate.model.AnnotationInfos;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AnnotationFileListPanel extends JPanel {

    private final Tree tree;
    private final DefaultTreeModel model;
    private final AnnotationService service;
    private final Project project;

    public AnnotationFileListPanel(Project project) {
        this.project = project;
        this.service = AnnotationService.getInstance(project);

        model = new DefaultTreeModel(null);
        tree = new Tree(model);
        tree.setRootVisible(false);
        tree.setCellRenderer(new AnnotationTreeCellRenderer());

        tree.addTreeSelectionListener(e -> {
            Object selected = e.getPath().getLastPathComponent();
            if (selected instanceof DefaultMutableTreeNode node
                    && node.getUserObject() instanceof AnnotationInfos annotation) {
                openFileAtOffset(annotation);
            }
        });

        setLayout(new BorderLayout());
        add(new JScrollPane(tree), BorderLayout.CENTER);

        refresh();
    }

    private void openFileAtOffset(AnnotationInfos annotation) {
        if (annotation.filePath == null || annotation.filePath.isEmpty()) return;

        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(annotation.filePath.replace('\\', '/'));
        if (virtualFile != null) {
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile, annotation.startOffset);
            FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
        }
    }

    public void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public void collapseAll() {
        for (int i = tree.getRowCount() - 1; i >= 0; i--) {
            tree.collapseRow(i);
        }
    }

    public void refresh() {
        List<AnnotationInfos> allAnnotations = service.getAllAnnotations();
        if (allAnnotations.isEmpty()) {
            model.setRoot(new DefaultMutableTreeNode("No annotations"));
            return;
        }

        Map<String, List<AnnotationInfos>> fileMap = new TreeMap<>();
        for (AnnotationInfos annotation : allAnnotations) {
            fileMap.computeIfAbsent(annotation.filePath, k -> new ArrayList<>()).add(annotation);
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (Map.Entry<String, List<AnnotationInfos>> entry : fileMap.entrySet()) {
            List<AnnotationInfos> annotations = entry.getValue();
            annotations.sort(Comparator.comparingInt(a -> a.startOffset));

            DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(entry.getKey());
            for (AnnotationInfos annotation : annotations) {
                fileNode.add(new DefaultMutableTreeNode(annotation));
            }
            root.add(fileNode);
        }

        model.setRoot(root);
    }

    private static final class AnnotationTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            setOpaque(sel);
            if (!sel) {
                setBackground(null);
                setBackgroundSelectionColor(null);
                setBackgroundNonSelectionColor(null);
            }

            if (value instanceof DefaultMutableTreeNode node) {
                Object userObject = node.getUserObject();
                if (userObject instanceof AnnotationInfos ann) {
                    if (ann.colorHex != null && !ann.colorHex.isEmpty()) {
                        try {
                            setIcon(createColorIcon(Color.decode(ann.colorHex)));
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (userObject instanceof String path) {
                    setText(extractFilename(path));
                    setIcon(FileTypeManager.getInstance().getFileTypeByFileName(path).getIcon());
                }
            }
            return this;
        }

        private static String extractFilename(String path) {
            int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
            return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        }

        private static Icon createColorIcon(Color color) {
            return new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    g.setColor(color);
                    g.fillOval(x + 1, y + 1, 8, 8);
                }

                @Override
                public int getIconWidth() {return 10;}

                @Override
                public int getIconHeight() {return 10;}
            };
        }
    }
}