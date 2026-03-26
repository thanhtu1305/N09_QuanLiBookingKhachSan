package gui;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import gui.common.AppBranding;
import gui.common.AppFrame;
import utils.NavigationUtil;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;


public class DangNhapGUI extends JFrame {
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final int LOGO_MAX_WIDTH = 220;
    private static final int LOGO_MAX_HEIGHT = 96;
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPanel rootPanel;
    private JRadioButton rdoLeTan;
    private JRadioButton rdoQuanLi;
    private JButton btnLogin;

    public DangNhapGUI() {
        setTitle("Đăng nhập - " + AppBranding.APP_DISPLAY_NAME);
        setLocationRelativeTo(null);
        setSize(1000, 650);
        setMinimumSize(new java.awt.Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(24, 24, 16, 24));

        root.add(buildCenterContainer(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        rootPanel = root;
        setContentPane(root);
        getRootPane().setDefaultButton(btnLogin);
    }

    private JPanel buildCenterContainer() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(buildLoginCard());
        return center;
    }

    private JPanel buildLoginCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(26, 30, 26, 30)
        ));
        card.setPreferredSize(new Dimension(560, 440));

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG KHÁCH SẠN", SwingConstants.CENTER);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel logoPanel = buildLogoSection();
        JPanel formPanel = buildFormPanel();
        JPanel buttonPanel = buildButtonPanel();

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(logoPanel);
        card.add(formPanel);
        card.add(Box.createVerticalStrut(16));
        card.add(buttonPanel);

        return card;
    }

    private JPanel buildLogoSection() {
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(10, 0, 12, 0));
        logoPanel.setPreferredSize(new Dimension(LOGO_MAX_WIDTH, LOGO_MAX_HEIGHT + 8));

        logoPanel.add(createLogoLabel(), BorderLayout.CENTER);
        return logoPanel;
    }

    private JLabel createLogoLabel() {
        return AppBranding.createLogoLabel(
                LOGO_MAX_WIDTH,
                LOGO_MAX_HEIGHT,
                SwingConstants.CENTER,
                AppBranding.APP_DISPLAY_NAME,
                new Font("Segoe UI", Font.BOLD, 18),
                new Color(30, 64, 175)
        );
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(LABEL_FONT);
        lblUser.setForeground(TEXT_PRIMARY);

        txtUsername = new JTextField(20);
        txtUsername.setFont(LABEL_FONT);

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(LABEL_FONT);
        lblPass.setForeground(TEXT_PRIMARY);

        txtPassword = new JPasswordField(20);
        txtPassword.setFont(LABEL_FONT);

        JLabel lblRole = new JLabel("Vai trò:");
        lblRole.setFont(LABEL_FONT);
        lblRole.setForeground(TEXT_PRIMARY);

        rdoLeTan = new JRadioButton("Lễ tân", true);
        rdoQuanLi = new JRadioButton("Quản lí");
        rdoLeTan.setOpaque(false);
        rdoQuanLi.setOpaque(false);
        rdoLeTan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rdoQuanLi.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(rdoLeTan);
        roleGroup.add(rdoQuanLi);

        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        rolePanel.setOpaque(false);
        rolePanel.add(rdoLeTan);
        rolePanel.add(rdoQuanLi);

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(lblUser, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        form.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(lblPass, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        form.add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(lblRole, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        form.add(rolePanel, gbc);

        return form;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setOpaque(false);

        btnLogin = createActionButton("Đăng nhập", new Color(22, 163, 74));
        JButton btnClear = createActionButton("Xóa trắng", new Color(37, 99, 235));
        JButton btnForgot = createActionButton("Quên mật khẩu", new Color(245, 158, 11));
        JButton btnExit = createActionButton("Thoát", new Color(220, 38, 38));

        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
        txtPassword.addActionListener(e -> onLogin());
        btnLogin.addActionListener(e -> onLogin());
        btnClear.addActionListener(e -> onClear());
        btnForgot.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Chức năng quên mật khẩu sẽ làm sau.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        ));
        btnExit.addActionListener(e -> System.exit(0));

        panel.add(btnLogin);
        panel.add(btnClear);
        panel.add(btnForgot);
        panel.add(btnExit);

        return panel;
    }

    private JButton createActionButton(String text, Color borderColor) {
        JButton button = new JButton(text);
        button.setText(text);
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(TEXT_PRIMARY);
        button.setFont(BUTTON_FONT);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        return button;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 6, 0, 6));

        JLabel lblVersion = new JLabel("Phiên bản: v1.0");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblVersion.setForeground(new Color(107, 114, 128));

        JLabel lblSupport = new JLabel("Hỗ trợ kỹ thuật: ext 101", SwingConstants.RIGHT);
        lblSupport.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSupport.setForeground(new Color(107, 114, 128));

        footer.add(lblVersion, BorderLayout.WEST);
        footer.add(lblSupport, BorderLayout.EAST);

        return footer;
    }

    private void onLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.",
                    "Lỗi đăng nhập",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String role = rdoLeTan.isSelected() ? "Lễ tân" : "Quản lí";

        TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
        TaiKhoan tk = taiKhoanDAO.dangNhap(username, password, role);

        if (tk == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Sai tên đăng nhập, mật khẩu, vai trò hoặc tài khoản bị khóa.",
                    "Đăng nhập thất bại",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        AppFrame mainFrame = AppFrame.get();
        NavigationUtil.navigate(null, null, ScreenKey.DASHBOARD, tk.getTenDangNhap(), tk.getVaiTro());
        mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        this.setVisible(false);
    }

    private void onClear() {
        txtUsername.setText("");
        txtPassword.setText("");
        rdoLeTan.setSelected(true);
        txtUsername.requestFocusInWindow();
    }

    public JPanel buildPanel() {
        if (rootPanel == null) initUI();
        return rootPanel;
    }

}
