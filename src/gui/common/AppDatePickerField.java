package gui.common;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;

public class AppDatePickerField extends JPanel {
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    private final JTextField txtDisplay;
    private final JPopupMenu popup;
    private final CalendarPanel calendarPanel;
    private final boolean defaultTodayIfEmpty;

    public AppDatePickerField(String value, boolean defaultTodayIfEmpty) {
        super(new BorderLayout(6, 0));
        this.defaultTodayIfEmpty = defaultTodayIfEmpty;
        setOpaque(false);
        setPreferredSize(new Dimension(240, 34));

        txtDisplay = new JTextField();
        txtDisplay.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        txtDisplay.setPreferredSize(new Dimension(220, 34));
        txtDisplay.setEditable(true);
        txtDisplay.setBackground(Color.WHITE);
        txtDisplay.addActionListener(e -> normalizeTypedValue());
        txtDisplay.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                normalizeTypedValue();
            }
        });

        JButton btnCalendar = createTriggerButton();
        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        calendarPanel = new CalendarPanel(resolveInitialDate(value));
        popup.add(calendarPanel);

        add(txtDisplay, BorderLayout.CENTER);
        add(btnCalendar, BorderLayout.EAST);
        setText(value);
    }

    public String getText() {
        return txtDisplay.getText().trim();
    }

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        if (txtDisplay != null) {
            txtDisplay.setToolTipText(text);
        }
    }

    public void setText(String value) {
        LocalDate parsed = parseDate(value);
        if (parsed != null) {
            txtDisplay.setText(parsed.format(DATE_FORMATTER));
            calendarPanel.setSelectedDate(parsed);
            return;
        }
        if (defaultTodayIfEmpty && (value == null || value.trim().isEmpty())) {
            setDateValue(LocalDate.now());
            return;
        }
        txtDisplay.setText(value == null ? "" : value.trim());
        calendarPanel.setSelectedDate(resolveInitialDate(value));
    }

    public LocalDate getDateValue() {
        return parseDate(getText());
    }

    public void setDateValue(LocalDate value) {
        if (value == null) {
            txtDisplay.setText("");
            calendarPanel.setSelectedDate(null);
            return;
        }
        txtDisplay.setText(value.format(DATE_FORMATTER));
        calendarPanel.setSelectedDate(value);
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
        LocalDate current = getDateValue();
        if (current == null) {
            current = defaultTodayIfEmpty && getText().isEmpty() ? LocalDate.now() : resolveInitialDate(getText());
        }
        calendarPanel.setSelectedDate(current);
        popup.show(this, 0, getHeight());
    }

    private void normalizeTypedValue() {
        String normalized = normalizeFlexibleDateInput(txtDisplay.getText());
        if (normalized != null) {
            LocalDate parsed = parseDate(normalized);
            txtDisplay.setText(normalized);
            calendarPanel.setSelectedDate(parsed);
        } else if (txtDisplay.getText().trim().isEmpty()) {
            calendarPanel.setSelectedDate(resolveInitialDate(""));
        }
    }

    private LocalDate resolveInitialDate(String value) {
        LocalDate parsed = parseDate(value);
        if (parsed != null) {
            return parsed;
        }
        return LocalDate.now();
    }

    private LocalDate parseDate(String value) {
        String normalized = normalizeFlexibleDateInput(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDate.parse(normalized, DATE_FORMATTER);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String normalizeFlexibleDateInput(String value) {
        if (value == null) {
            return null;
        }
        String input = value.trim();
        if (input.isEmpty()) {
            return null;
        }

        String[] parts = input.split("/");
        if (parts.length != 3) {
            return null;
        }

        String dayPart = parts[0].trim();
        String monthPart = parts[1].trim();
        String yearPart = parts[2].trim();
        if (!isNumeric(dayPart) || !isNumeric(monthPart) || !isNumeric(yearPart)) {
            return null;
        }
        if (dayPart.length() < 1 || dayPart.length() > 2
                || monthPart.length() < 1 || monthPart.length() > 2
                || (yearPart.length() != 2 && yearPart.length() != 4)) {
            return null;
        }

        try {
            int day = Integer.parseInt(dayPart);
            int month = Integer.parseInt(monthPart);
            int year = Integer.parseInt(yearPart);
            if (yearPart.length() == 2) {
                year += 2000;
            }
            return LocalDate.of(year, month, day).format(DATE_FORMATTER);
        } catch (DateTimeException | NumberFormatException ex) {
            return null;
        }
    }

    private static boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
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

    private final class CalendarPanel extends JPanel {
        private final JLabel lblMonth;
        private final JPanel dayGrid;
        private final JComboBox<String> cboMonth;
        private final JComboBox<String> cboYear;
        private LocalDate selectedDate;
        private YearMonth displayedMonth;

        private CalendarPanel(LocalDate initialDate) {
            super(new BorderLayout(0, 8));
            selectedDate = initialDate;
            displayedMonth = YearMonth.from(initialDate);
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JButton btnPrev = createSmallButton("<", e -> changeMonth(-1));
            JButton btnNext = createSmallButton(">", e -> changeMonth(1));

            cboMonth = new JComboBox<String>(buildMonthOptions());
            cboMonth.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            cboMonth.setSelectedIndex(displayedMonth.getMonthValue() - 1);
            cboMonth.addActionListener(e -> onMonthYearChanged());

            cboYear = new JComboBox<String>(buildYearOptions(initialDate.getYear()));
            cboYear.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            cboYear.setSelectedItem(String.valueOf(displayedMonth.getYear()));
            cboYear.addActionListener(e -> onMonthYearChanged());

            lblMonth = new JLabel("", SwingConstants.CENTER);
            lblMonth.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            lblMonth.setForeground(TEXT_PRIMARY);

            JPanel header = new JPanel(new BorderLayout(8, 0));
            header.setOpaque(false);
            header.add(btnPrev, BorderLayout.WEST);

            JPanel selector = new JPanel(new BorderLayout(6, 0));
            selector.setOpaque(false);
            selector.add(cboMonth, BorderLayout.WEST);
            selector.add(cboYear, BorderLayout.EAST);
            selector.add(lblMonth, BorderLayout.CENTER);
            header.add(selector, BorderLayout.CENTER);
            header.add(btnNext, BorderLayout.EAST);

            JPanel weekdayHeader = new JPanel(new GridLayout(1, 7, 6, 6));
            weekdayHeader.setOpaque(false);
            String[] weekdays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            for (String weekday : weekdays) {
                JLabel lbl = new JLabel(weekday, SwingConstants.CENTER);
                lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
                lbl.setForeground(TEXT_MUTED);
                weekdayHeader.add(lbl);
            }

            dayGrid = new JPanel(new GridLayout(6, 7, 6, 6));
            dayGrid.setOpaque(false);

            add(header, BorderLayout.NORTH);
            add(weekdayHeader, BorderLayout.CENTER);
            add(dayGrid, BorderLayout.SOUTH);
            refreshCalendar();
        }

        private String[] buildMonthOptions() {
            String[] months = new String[12];
            for (int i = 0; i < 12; i++) {
                months[i] = String.format("%02d", i + 1);
            }
            return months;
        }

        private String[] buildYearOptions(int currentYear) {
            String[] years = new String[21];
            int startYear = currentYear - 10;
            for (int i = 0; i < years.length; i++) {
                years[i] = String.valueOf(startYear + i);
            }
            return years;
        }

        private void onMonthYearChanged() {
            int month = cboMonth.getSelectedIndex() + 1;
            int year = Integer.parseInt(String.valueOf(cboYear.getSelectedItem()));
            displayedMonth = YearMonth.of(year, month);
            refreshCalendar();
        }

        private void changeMonth(int offset) {
            displayedMonth = displayedMonth.plusMonths(offset);
            cboMonth.setSelectedIndex(displayedMonth.getMonthValue() - 1);
            cboYear.setSelectedItem(String.valueOf(displayedMonth.getYear()));
            refreshCalendar();
        }

        private void setSelectedDate(LocalDate date) {
            selectedDate = date;
            if (date != null) {
                displayedMonth = YearMonth.from(date);
                cboMonth.setSelectedIndex(displayedMonth.getMonthValue() - 1);
                cboYear.setSelectedItem(String.valueOf(displayedMonth.getYear()));
            }
            refreshCalendar();
        }

        private void refreshCalendar() {
            lblMonth.setText("Tháng " + displayedMonth.atDay(1).format(MONTH_FORMATTER));
            dayGrid.removeAll();

            LocalDate firstDay = displayedMonth.atDay(1);
            int offset = firstDay.getDayOfWeek().getValue() - 1;
            int totalDays = displayedMonth.lengthOfMonth();

            for (int i = 0; i < 42; i++) {
                if (i < offset || i >= offset + totalDays) {
                    JPanel empty = new JPanel();
                    empty.setOpaque(false);
                    dayGrid.add(empty);
                    continue;
                }

                int day = i - offset + 1;
                LocalDate date = displayedMonth.atDay(day);
                JButton btnDay = new JButton(String.valueOf(day));
                btnDay.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
                btnDay.setFocusPainted(false);
                btnDay.setOpaque(true);
                btnDay.setContentAreaFilled(true);
                btnDay.setBorderPainted(true);

                boolean selected = selectedDate != null && selectedDate.equals(date);
                if (selected) {
                    btnDay.setBackground(ACCENT_BLUE);
                    btnDay.setForeground(Color.WHITE);
                    btnDay.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE.darker(), 1, true));
                } else if (LocalDate.now().equals(date)) {
                    btnDay.setBackground(new Color(219, 234, 254));
                    btnDay.setForeground(TEXT_PRIMARY);
                    btnDay.setBorder(BorderFactory.createLineBorder(new Color(96, 165, 250), 1, true));
                } else {
                    btnDay.setBackground(Color.WHITE);
                    btnDay.setForeground(TEXT_PRIMARY);
                    btnDay.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
                }

                btnDay.addActionListener(e -> {
                    selectedDate = date;
                    txtDisplay.setText(date.format(DATE_FORMATTER));
                    popup.setVisible(false);
                    refreshCalendar();
                });
                dayGrid.add(btnDay);
            }

            dayGrid.revalidate();
            dayGrid.repaint();
        }

        private JButton createSmallButton(String text, java.awt.event.ActionListener listener) {
            JButton button = new JButton(text);
            button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            button.setForeground(TEXT_PRIMARY);
            button.setBackground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                    new EmptyBorder(6, 10, 6, 10)
            ));
            button.addActionListener(listener);
            return button;
        }
    }
}
