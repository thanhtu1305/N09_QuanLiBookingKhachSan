package gui.common;

import db.ConnectDB;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Panel tiện ích người dùng đặt ở góc trái dưới sidebar.
 *
 * Cách dùng trong SidebarFactory:
 *
 *   sidebar.add(Box.createVerticalGlue());
 *   sidebar.add(UserUtilityPanel.create(parentFrame, username, role));
 *
 * File này đồng thời mở dialog "Thông tin cá nhân" responsive
 * và có nút "Đăng xuất".
 */
public final class UserUtilityPanel {
    private static final Color CARD_BG = Color.WHITE;
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_DARK = new Color(30, 64, 175);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color DANGER_DARK = new Color(185, 28, 28);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font VALUE_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private UserUtilityPanel() {
    }

    public static JPanel create(Component parentComponent, String username, String role) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBorder(new EmptyBorder(8, 10, 10, 10));

        JLabel lblSection = new JLabel("Tiện ích người dùng");
        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSection.setForeground(MUTED);
        lblSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JButton btnProfile = createFlatActionButton(
                "<html><div style='line-height:1.35'><b>Thông tin cá nhân</b><br/>"
                        + safe(username, "guest") + " \u00b7 " + safe(role, "-") + "</div></html>",
                PRIMARY,
                Color.WHITE
        );
        btnProfile.setHorizontalAlignment(SwingConstants.LEFT);
        btnProfile.addActionListener(e -> new PersonalInfoDialog(parentComponent, safe(username, "guest"), safe(role, "-")).setVisible(true));

        JButton btnLogout = createFlatActionButton("Đăng xuất", DANGER, Color.WHITE);
        btnLogout.addActionListener((ActionEvent e) -> handleLogout(parentComponent));

        card.add(btnProfile, BorderLayout.CENTER);
        card.add(btnLogout, BorderLayout.SOUTH);

        wrapper.add(lblSection);
        wrapper.add(Box.createVerticalStrut(6));
        wrapper.add(card);
        return wrapper;
    }

    private static JButton createFlatActionButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return button;
    }

    private static void handleLogout(Component parentComponent) {
        Window owner = SwingUtilities.getWindowAncestor(parentComponent);
        int confirm = JOptionPane.showConfirmDialog(
                owner,
                "Bạn có chắc muốn đăng xuất không?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Window mainWindow = AppFrame.get();
        if (mainWindow == null) {
            mainWindow = owner;
        }
        if (mainWindow == null) {
            for (Window w : Window.getWindows()) {
                if (w instanceof JFrame && w.isDisplayable()) {
                    mainWindow = w;
                    break;
                }
            }
        }

        if (mainWindow != null) {
            mainWindow.dispose();
        } else if (owner != null) {
            owner.dispose();
        }

        SwingUtilities.invokeLater(UserUtilityPanel::openLoginScreen);
    }

    private static void openLoginScreen() {
        String[] loginCandidates = {
                "gui.DangNhapGUI",
                "gui.LoginGUI",
                "gui.DangNhapFrame",
                "gui.LoginFrame"
        };

        for (String className : loginCandidates) {
            try {
                Class<?> clazz = Class.forName(className);
                Constructor<?> ctor = clazz.getDeclaredConstructor();
                ctor.setAccessible(true);
                Object instance = ctor.newInstance();
                if (instance instanceof JFrame) {
                    ((JFrame) instance).setVisible(true);
                    return;
                }
                if (instance instanceof JDialog) {
                    ((JDialog) instance).setVisible(true);
                    return;
                }
            } catch (Throwable ignore) {
                // thử class tiếp theo
            }
        }

        JOptionPane.showMessageDialog(
                null,
                "Đã đăng xuất nhưng không tìm thấy màn hình đăng nhập. Vui lòng mở lại ứng dụng.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static final class PersonalInfoData {
        private int maNhanVien;
        private String hoTen;
        private String vaiTro;
        private String email;
        private String soDienThoai;
        private Timestamp lanDangNhapCuoi;
    }

    private static final class PersonalInfoDialog extends JDialog {
        private final String username;
        private final String role;
        private final PersonalInfoData data;

        private PersonalInfoDialog(Component parentComponent, String username, String role) {
            super(SwingUtilities.getWindowAncestor(parentComponent), "Thông tin cá nhân", ModalityType.APPLICATION_MODAL);
            this.username = username;
            this.role = role;
            this.data = loadPersonalInfo(username);

            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            getContentPane().setBackground(APP_BG);
            setLayout(new BorderLayout());

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBackground(APP_BG);
            content.setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel headerCard = buildHeaderCard();
            headerCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, headerCard.getPreferredSize().height));

            JPanel quickCard = buildQuickCard();
            quickCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            quickCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, quickCard.getPreferredSize().height));

            JPanel detailCard = buildDetailCard();
            detailCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, detailCard.getPreferredSize().height));

            JPanel footer = buildFooter(parentComponent);
            footer.setAlignmentX(Component.LEFT_ALIGNMENT);
            footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, footer.getPreferredSize().height));

            content.add(headerCard);
            content.add(Box.createVerticalStrut(12));
            content.add(quickCard);
            content.add(Box.createVerticalStrut(12));
            content.add(detailCard);
            content.add(Box.createVerticalStrut(12));
            content.add(footer);

            JPanel viewportHolder = new JPanel(new BorderLayout());
            viewportHolder.setBackground(APP_BG);
            viewportHolder.add(content, BorderLayout.NORTH);

            JScrollPane scrollPane = new JScrollPane(viewportHolder);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(18);
            scrollPane.getViewport().setBackground(APP_BG);
            add(scrollPane, BorderLayout.CENTER);

            prepareResponsiveDialog(this, 920, 560);
        }

        private JPanel buildHeaderCard() {
            JPanel panel = createCard(new BorderLayout(16, 0));
            panel.setBackground(new Color(41, 72, 185));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_DARK, 1, true),
                    new EmptyBorder(18, 18, 18, 18)
            ));

            JLabel avatar = new JLabel(buildAvatarText(), SwingConstants.CENTER);
            avatar.setOpaque(true);
            avatar.setBackground(new Color(219, 234, 254));
            avatar.setForeground(PRIMARY_DARK);
            avatar.setFont(new Font("Segoe UI", Font.BOLD, 24));
            avatar.setPreferredSize(new Dimension(72, 72));
            avatar.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true));

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("Thông tin cá nhân");
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            title.setForeground(Color.WHITE);

            JLabel name = new JLabel(safe(data == null ? null : data.hoTen, username));
            name.setFont(new Font("Segoe UI", Font.BOLD, 16));
            name.setForeground(Color.WHITE);

            JLabel roleLabel = new JLabel(safe(data == null ? null : data.vaiTro, role));
            roleLabel.setFont(BODY_FONT);
            roleLabel.setForeground(new Color(219, 234, 254));

            text.add(title);
            text.add(Box.createVerticalStrut(6));
            text.add(name);
            text.add(Box.createVerticalStrut(4));
            text.add(roleLabel);

            panel.add(avatar, BorderLayout.WEST);
            panel.add(text, BorderLayout.CENTER);
            return panel;
        }

        private JPanel buildQuickCard() {
            JPanel panel = createCard(new GridLayout(1, 2, 16, 0));
            panel.add(createQuickInfoBlock("Mã nhân viên", formatEmployeeCode(data == null ? -1 : data.maNhanVien)));
            panel.add(createQuickInfoBlock("Đăng nhập gần nhất", formatTimestamp(data == null ? null : data.lanDangNhapCuoi)));
            return panel;
        }

        private JPanel buildDetailCard() {
            JPanel panel = createCard(new BorderLayout());

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 0, 8, 16);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Vai trò", createValueLabel(safe(data == null ? null : data.vaiTro, role)));
            addFormRow(form, gbc, 1, "Email", createWrappedValueLabel(safe(data == null ? null : data.email, "-"), 260));
            addFormRow(form, gbc, 2, "Số điện thoại", createValueLabel(safe(data == null ? null : data.soDienThoai, "-")));

            panel.add(form, BorderLayout.CENTER);
            return panel;
        }

        private JPanel buildFooter(Component parentComponent) {
            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);

            JButton btnLogout = createFlatActionButton("Đăng xuất", DANGER, Color.WHITE);
            btnLogout.setBackground(DANGER);
            btnLogout.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DANGER_DARK, 1, true),
                    new EmptyBorder(10, 16, 10, 16)
            ));
            btnLogout.addActionListener(e -> {
                dispose();
                handleLogout(parentComponent);
            });

            JButton btnClose = createFlatActionButton("Đóng", PRIMARY, Color.WHITE);
            btnClose.addActionListener(e -> dispose());

            actions.add(btnLogout);
            actions.add(btnClose);
            footer.add(actions, BorderLayout.EAST);
            return footer;
        }

        private JPanel createQuickInfoBlock(String label, String value) {
            JPanel block = new JPanel();
            block.setOpaque(false);
            block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

            JLabel lbl = new JLabel(label);
            lbl.setFont(BODY_FONT);
            lbl.setForeground(MUTED);

            JLabel val = new JLabel(safe(value, "-"));
            val.setFont(VALUE_FONT);
            val.setForeground(new Color(17, 24, 39));

            block.add(lbl);
            block.add(Box.createVerticalStrut(8));
            block.add(val);
            return block;
        }

        private JLabel createValueLabel(String value) {
            JLabel label = new JLabel(safe(value, "-"));
            label.setFont(VALUE_FONT);
            label.setForeground(new Color(17, 24, 39));
            return label;
        }

        private JLabel createWrappedValueLabel(String value, int width) {
            JLabel label = new JLabel();
            label.setFont(VALUE_FONT);
            label.setForeground(new Color(17, 24, 39));
            String safeValue = safe(value, "-")
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
            label.setText("<html><div style='width:" + width + "px;'>" + safeValue + "</div></html>");
            return label;
        }

        private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent value) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;

            JLabel lbl = new JLabel(label);
            lbl.setFont(BODY_FONT);
            lbl.setForeground(MUTED);
            lbl.setPreferredSize(new Dimension(110, 20));
            panel.add(lbl, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(value, gbc);
        }

        private JPanel createCard(LayoutManager layout) {
            JPanel panel = new JPanel(layout);
            panel.setBackground(CARD_BG);
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    new EmptyBorder(16, 16, 16, 16)
            ));
            return panel;
        }

        private String buildAvatarText() {
            String display = safe(data == null ? null : data.hoTen, username);
            String[] parts = display.trim().split("\\s+");
            if (parts.length == 1) {
                return display.substring(0, Math.min(2, display.length())).toUpperCase();
            }
            String first = parts[parts.length - 2].substring(0, 1);
            String second = parts[parts.length - 1].substring(0, 1);
            return (first + second).toUpperCase();
        }
    }

    private static PersonalInfoData loadPersonalInfo(String tenDangNhap) {
        String sql =
                "SELECT tk.tenDangNhap, tk.vaiTro, tk.lanDangNhapCuoi, " +
                        "       nv.maNhanVien, nv.hoTen, nv.email, nv.soDienThoai " +
                        "FROM TaiKhoan tk " +
                        "JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien " +
                        "WHERE tk.tenDangNhap = ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PersonalInfoData data = new PersonalInfoData();
                    data.maNhanVien = rs.getInt("maNhanVien");
                    data.hoTen = rs.getString("hoTen");
                    data.vaiTro = rs.getString("vaiTro");
                    data.email = rs.getString("email");
                    data.soDienThoai = rs.getString("soDienThoai");
                    data.lanDangNhapCuoi = rs.getTimestamp("lanDangNhapCuoi");
                    return data;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String formatEmployeeCode(int maNhanVien) {
        return maNhanVien <= 0 ? "-" : "NV" + maNhanVien;
    }

    private static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "-";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(timestamp);
    }

    private static void prepareResponsiveDialog(JDialog dialog, int prefWidth, int prefHeight) {
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(prefWidth, (int) (bounds.width * 0.92));
        int height = Math.min(prefHeight, (int) (bounds.height * 0.88));

        dialog.setSize(width, height);
        dialog.setMinimumSize(new Dimension(Math.min(520, width), Math.min(420, height)));
        dialog.setLocation(
                bounds.x + (bounds.width - width) / 2,
                bounds.y + (bounds.height - height) / 2
        );
    }
}
