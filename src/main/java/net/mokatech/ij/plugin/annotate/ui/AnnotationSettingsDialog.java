package net.mokatech.ij.plugin.annotate.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import javax.swing.BorderFactory;
import java.awt.event.ActionEvent;
import net.mokatech.ij.plugin.annotate.model.AnnotationInfos;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;

public class AnnotationSettingsDialog extends DialogWrapper {

    public static class ColorOption {
        final String name;
        final String hex;
        final Color color;

        ColorOption(String name, String hex) {
            this.name = name;
            this.hex = hex;
            this.color = Color.decode(hex);
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ColorOption that = (ColorOption) o;
            return hex.equals(that.hex);
        }

        @Override
        public int hashCode() {
            return hex.hashCode();
        }
    }

    public static final List<ColorOption> PREDEFINED_COLORS = List.of(
            new ColorOption("blue", "#0000FF"),
            new ColorOption("lightblue", "#ADD8E6"),
            new ColorOption("red", "#FF0000"),
            new ColorOption("green", "#008000"),
            new ColorOption("yellow", "#FFFF00"),
            new ColorOption("purple", "#800080")
    );

    private static class ColorComboBoxRenderer implements ListCellRenderer<ColorOption> {
        private final JPanel container = new JPanel(new BorderLayout(5, 0));
        private final JPanel swatch = new JPanel();
        private final JLabel nameLabel = new JLabel();

        {
            swatch.setPreferredSize(new Dimension(20, 16));
            swatch.setMinimumSize(new Dimension(20, 16));
            swatch.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            container.add(swatch, BorderLayout.WEST);
            container.add(nameLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ColorOption> list, ColorOption value, int index, boolean isSelected, boolean cellHasFocus) {
            swatch.setBackground(value.color);
            nameLabel.setText(value.name);
            Color bgColor = isSelected ? list.getSelectionBackground() : list.getBackground();
            Color fgColor = isSelected ? list.getSelectionForeground() : list.getForeground();
            container.setBackground(bgColor);
            nameLabel.setForeground(fgColor);
            swatch.setOpaque(true);
            container.setOpaque(true);
            nameLabel.setOpaque(false);
            return container;
        }
    }

    private final JTextField labelField = new JTextField(20);
    private final @Nullable AnnotationInfos editingAnnotation;
    private final Project project;
    private JComboBox<ColorOption> colorCombo;

    public AnnotationSettingsDialog(Project project, @Nullable AnnotationInfos editingAnnotation) {
        super(project);
        this.project = project;
        this.editingAnnotation = editingAnnotation;
        String initLabel = editingAnnotation != null ? editingAnnotation.label : "";
        setTitle(!initLabel.isEmpty() ? "Edit Annotation" : "Add Annotation");
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(5);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Label:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(labelField, gbc);

        // Color row
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Color:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        colorCombo = new ComboBox<>(PREDEFINED_COLORS.toArray(new ColorOption[0]));
        colorCombo.setRenderer(new ColorComboBoxRenderer());
        String initColorHex = editingAnnotation != null ? editingAnnotation.colorHex : "#FF0000";
        ColorOption selectedColor = PREDEFINED_COLORS.stream()
                .filter(co -> initColorHex.equals(co.hex))
                .findFirst()
                .orElse(PREDEFINED_COLORS.get(2)); // default to red
        colorCombo.setSelectedItem(selectedColor);
        panel.add(colorCombo, gbc);

        labelField.setText(editingAnnotation != null ? editingAnnotation.label : "");
        if (editingAnnotation != null && !editingAnnotation.label.isEmpty()) {
            labelField.selectAll();
        }
        return panel;
    }

    public String getLabel() { return labelField.getText(); }
    public String getColorHex() { 
        ColorOption co = (ColorOption) colorCombo.getSelectedItem();
        return co != null ? co.hex : PREDEFINED_COLORS.get(0).hex; 
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return labelField;
    }

    @Override
    protected @NotNull Action[] createLeftSideActions() {
        if (editingAnnotation == null) {
            return super.createLeftSideActions();
        }
        return new Action[] { new DeleteAction() };
    }

    private class DeleteAction extends AbstractAction {
        public DeleteAction() {
            super("Delete Annotation");
        }

        public void actionPerformed(ActionEvent e) {
            AnnotationService.getInstance(project).removeAnnotation(editingAnnotation);
            AnnotationSettingsDialog.this.doCancelAction();
        }
    }
}
