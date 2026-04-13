package gui.common;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AppTimePickerField extends JPanel {
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final JTextField txtDisplay;
    private final JPopupMenu popup;
    private final JComboBox<String> cboHour;
    private final JComboBox<String> cboMinute;
    private final boolean defaultNowIfEmpty;

    public AppTimePickerField(String value, boolean defaultNowIfEmpty) {
        super(new BorderLayout(6, 0));
        this.defaultNowIfEmpty = defaultNowIfEmpty;
        setOpaque(false);
        setPreferredSize(new Dimension(180, 34));

        txtDisplay = new JTextField();
        txtDisplay.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        txtDisplay.setPreferredSize(new Dimension(150, 34));
        txtDisplay.setEditable(true);
        txtDisplay.setBackground(Color.WHITE);
        txtDisplay.addActionListener(e -> normalizeTypedValue());
        txtDisplay.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                normalizeTypedValue();
            }
        });

        JButton btnTime = createTriggerButton();

        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(Color.WHITE);
        cboHour = createCombo(24);
        cboMinute = createCombo(60);
        JButton btnApply = createApplyButton();
        panel.add(cboHour);
        panel.add(cboMinute);
        panel.add(btnApply);
        popup.add(panel);

        add(txtDisplay, BorderLayout.CENTER);
        add(btnTime, BorderLayout.EAST);
        setText(value);
    }

    public String getText() {
        return txtDisplay.getText().trim();
    }

    public void setText(String value) {
        LocalTime parsed = parseTime(value);
        if (parsed != null) {
            setTimeValue(parsed);
            return;
        }
        if (defaultNowIfEmpty && (value == null || value.trim().isEmpty())) {
            setTimeValue(LocalTime.now().withSecond(0).withNano(0));
            return;
        }
        txtDisplay.setText(value == null ? "" : value.trim());
    }

    public LocalTime getTimeValue() {
        return parseTime(getText());
    }

    public void setTimeValue(LocalTime value) {
        if (value == null) {
            txtDisplay.setText("");
            return;
        }
        cboHour.setSelectedItem(String.format("%02d", value.getHour()));
        cboMinute.setSelectedItem(String.format("%02d", value.getMinute()));
        txtDisplay.setText(value.format(TIME_FORMATTER));
    }

    public void addTextChangeListener(final Runnable listener) {
        if (listener == null) {
            return;
        }
        txtDisplay.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                listener.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                listener.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                listener.run();
            }
        });
    }

    private void togglePopup() {
        if (popup.isVisible()) {
            popup.setVisible(false);
            return;
        }
        LocalTime current = getTimeValue();
        if (current == null) {
            current = defaultNowIfEmpty ? LocalTime.now().withSecond(0).withNano(0) : LocalTime.of(0, 0);
        }
        cboHour.setSelectedItem(String.format("%02d", current.getHour()));
        cboMinute.setSelectedItem(String.format("%02d", current.getMinute()));
        popup.show(this, 0, getHeight());
    }

    private void normalizeTypedValue() {
        LocalTime parsed = parseTime(txtDisplay.getText());
        if (parsed != null) {
            setTimeValue(parsed);
        }
    }

    private JComboBox<String> createCombo(int maxExclusive) {
        String[] values = new String[maxExclusive];
        for (int i = 0; i < maxExclusive; i++) {
            values[i] = String.format("%02d", i);
        }
        JComboBox<String> comboBox = new JComboBox<String>(values);
        comboBox.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        comboBox.setPreferredSize(new Dimension(60, 30));
        return comboBox;
    }

    private JButton createTriggerButton() {
        JButton button = new JButton("...");
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(46, 34));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        button.addActionListener(e -> togglePopup());
        return button;
    }

    private JButton createApplyButton() {
        JButton button = new JButton("OK");
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT_BLUE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE.darker(), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        button.addActionListener(e -> {
            LocalTime time = LocalTime.of(
                    Integer.parseInt(String.valueOf(cboHour.getSelectedItem())),
                    Integer.parseInt(String.valueOf(cboMinute.getSelectedItem()))
            );
            txtDisplay.setText(time.format(TIME_FORMATTER));
            popup.setVisible(false);
        });
        return button;
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
