package main;

import model.User;
import util.FileHandler;
import util.Session;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * GUI CLASS — LoginFrame
 */
public class LoginFrame extends JFrame {

    // APU COLOUR PALETTE
    static final Color BG_DARK      = new Color(6,  14,  40);
    static final Color BG_CARD      = new Color(10, 22,  58);
    static final Color BG_CARD2     = new Color(14, 30,  74);
    static final Color ACCENT       = new Color(245, 168,  0);
    static final Color ACCENT_HOVER = new Color(220, 148,  0);
    static final Color ACCENT2      = new Color(200,  16, 46);
    static final Color TEXT_PRIMARY = new Color(235, 238, 255);
    static final Color TEXT_MUTED   = new Color(140, 155, 200);
    static final Color BORDER_COLOR = new Color(30,  50, 100);

    // Role card accent colours
    private static final Color COL_MANAGER  = new Color(245, 168,   0); // gold
    private static final Color COL_STAFF    = new Color( 20, 184, 166); // teal
    private static final Color COL_TECH     = new Color(200,  16,  46); // crimson
    private static final Color COL_CUSTOMER = new Color( 56, 130, 246); // sky blue

    // STATE
    private String        selectedRole = "";
    private BufferedImage logoImage    = null;

    // LAYOUT
    private CardLayout cardLayout;
    private JPanel     mainPanel;

    // Login form fields
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         roleLabel;
    private JLabel         errorLabel;

    // CONSTRUCTOR
    public LoginFrame() {
        setTitle("APU Automotive Service Centre");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 900, 620, 20, 20));

        // Load APU logo — path is relative to the project root (where you run java from)
        try {
            logoImage = ImageIO.read(new File("src/data/logo_apu.jpg"));
        } catch (IOException e) {
            System.err.println("Logo not found at src/data/logo_apu.jpg — using fallback.");
        }

        buildUI();
    }
    im gay
    private void buildUI() {
        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);
        mainPanel.setBackground(BG_DARK);
        mainPanel.add(buildRoleSelectScreen(), "ROLE_SELECT");
        mainPanel.add(buildLoginScreen(),      "LOGIN");
        setContentPane(mainPanel);
        cardLayout.show(mainPanel, "ROLE_SELECT");
    }

    //  SCREEN 1 — ROLE SELECTION
    private JPanel buildRoleSelectScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG_DARK);

        // Left branding panel
        JPanel leftPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0,           new Color(10, 22, 68),
                    0, getHeight(), new Color(4,  10, 30));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Gold glow top-left
                g2.setColor(new Color(245, 168, 0, 20));
                g2.fillOval(-80, -80, 340, 340);
                // Crimson glow bottom
                g2.setColor(new Color(200, 16, 46, 12));
                g2.fillOval(30, 340, 240, 240);
            }
        };
        leftPanel.setPreferredSize(new Dimension(320, 0));
        leftPanel.setLayout(new GridBagLayout());
        addDragSupport(leftPanel);

        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);

        JPanel logo = buildLogo(96);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel("APU – ASC");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLbl.setForeground(TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Automotive Service Centre");
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subLbl.setForeground(TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(245, 168, 0, 70));
        sep.setMaximumSize(new Dimension(200, 1));

        JLabel tagline = makeWrappedLabel(
            "<html><div style='text-align:center;width:200px;'>" +
            "Manage appointments, services, and payments, all in one place." +
            "</div></html>", TEXT_MUTED, 13);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandBox.add(logo);
        brandBox.add(Box.createVerticalStrut(18));
        brandBox.add(titleLbl);
        brandBox.add(Box.createVerticalStrut(6));
        brandBox.add(subLbl);
        brandBox.add(Box.createVerticalStrut(24));
        brandBox.add(sep);
        brandBox.add(Box.createVerticalStrut(24));
        brandBox.add(tagline);
        leftPanel.add(brandBox);

        // Right role-select panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_CARD);
        rightPanel.setBorder(new EmptyBorder(48, 48, 48, 48));

        JButton closeBtn = makeCloseButton();
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRow.setOpaque(false);
        topRow.add(closeBtn);
        rightPanel.add(topRow, BorderLayout.NORTH);

        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setOpaque(false);

        JLabel heading = new JLabel("Who are you?");
        heading.setFont(new Font("SansSerif", Font.BOLD, 28));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subheading = new JLabel("Select your role to continue");
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subheading.setForeground(TEXT_MUTED);
        subheading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(500, 260));
        grid.add(makeRoleCard("Manager",      "Manage users & reports", COL_MANAGER,  "M"));
        grid.add(makeRoleCard("CounterStaff", "Bookings & payments",    COL_STAFF,    "C"));
        grid.add(makeRoleCard("Technician",   "Service appointments",   COL_TECH,     "T"));
        grid.add(makeRoleCard("Customer",     "Your service history",   COL_CUSTOMER, "K"));

        centre.add(heading);
        centre.add(Box.createVerticalStrut(6));
        centre.add(subheading);
        centre.add(Box.createVerticalStrut(32));
        centre.add(grid);

        rightPanel.add(centre, BorderLayout.CENTER);
        screen.add(leftPanel,  BorderLayout.WEST);
        screen.add(rightPanel, BorderLayout.CENTER);
        return screen;
    }


    //  SCREEN 2 — LOGIN FORM
    private JPanel buildLoginScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG_DARK);

        JPanel leftPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                    0, 0,           new Color(10, 22, 68),
                    0, getHeight(), new Color(4,  10, 30));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(245, 168, 0, 20));
                g2.fillOval(-80, -80, 340, 340);
            }
        };
        leftPanel.setPreferredSize(new Dimension(320, 0));
        leftPanel.setLayout(new GridBagLayout());
        addDragSupport(leftPanel);

        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);

        JLabel back = new JLabel("← Back to roles");
        back.setFont(new Font("SansSerif", Font.PLAIN, 13));
        back.setForeground(ACCENT);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                clearLoginForm();
                cardLayout.show(mainPanel, "ROLE_SELECT");
            }
        });

        JPanel logoSmall = buildLogo(64);
        logoSmall.setAlignmentX(Component.CENTER_ALIGNMENT);

        roleLabel = new JLabel("Logging in as ...");
        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        roleLabel.setForeground(TEXT_PRIMARY);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tip = makeWrappedLabel(
            "<html><div style='text-align:center;width:200px;'>" +
            "Enter your credentials to access your dashboard." +
            "</div></html>", TEXT_MUTED, 13);
        tip.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandBox.add(back);
        brandBox.add(Box.createVerticalStrut(20));
        brandBox.add(logoSmall);
        brandBox.add(Box.createVerticalStrut(16));
        brandBox.add(roleLabel);
        brandBox.add(Box.createVerticalStrut(12));
        brandBox.add(tip);
        leftPanel.add(brandBox);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_CARD);
        rightPanel.setBorder(new EmptyBorder(48, 64, 48, 64));

        JButton closeBtn = makeCloseButton();
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRow.setOpaque(false);
        topRow.add(closeBtn);
        rightPanel.add(topRow, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(new Font("SansSerif", Font.BOLD, 28));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subheading = new JLabel("Sign in to your account");
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subheading.setForeground(TEXT_MUTED);
        subheading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLbl = makeFieldLabel("Username");
        usernameField  = makeTextField("Enter your username");
        JLabel passLbl = makeFieldLabel("Password");
        passwordField  = makePasswordField("Enter your password");

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        errorLabel.setForeground(ACCENT2);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = makeLoginButton();

        formPanel.add(heading);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(subheading);
        formPanel.add(Box.createVerticalStrut(36));
        formPanel.add(userLbl);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(passLbl);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(errorLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(loginBtn);

        getRootPane().setDefaultButton(loginBtn);
        rightPanel.add(formPanel, BorderLayout.CENTER);
        screen.add(leftPanel,  BorderLayout.WEST);
        screen.add(rightPanel, BorderLayout.CENTER);
        return screen;
    }


    //  APU LOGO
    //  Clips logo_apu.jpg into a square with a gold ring border
    private JPanel buildLogo(int size) {
    JPanel square = new JPanel() {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            int w = getWidth();
            int h = getHeight();

            if (logoImage != null) {
                // Step 1: Draw the image as a square
                g2.drawImage(logoImage, 0, 0, w, h, null);
            } else {
                // Fallback: navy square with "APU" text
                g2.setColor(BG_CARD);
                g2.fillRect(0, 0, w, h);
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(new Font("SansSerif", Font.BOLD, size / 4));
                FontMetrics fm = g2.getFontMetrics();
                String t = "APU";
                g2.drawString(t, (w - fm.stringWidth(t)) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            }

            // Step 2: gold square border
            g2.setColor(ACCENT);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRect(0, 0, w - 1, h - 1);
        }
        @Override public Dimension getPreferredSize() { return new Dimension(size, size); }
        @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
        @Override public Dimension getMaximumSize()   { return getPreferredSize(); }
    };
    square.setOpaque(false);
    return square;
}


    //  LOGIN LOGIC
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password."); return;
        }
        User user = FileHandler.findUserByUsername(username);
        if (user == null) {
            showError("Username not found."); return;
        }
        if (!user.getPassword().equals(password)) {
            showError("Incorrect password."); return;
        }
        if (!user.getRole().equals(selectedRole)) {
            showError("This account is not a " + getRoleDisplayName(selectedRole) + "."); return;
        }

        Session.setCurrentUser(user);
        dispose();
        user.showDashboard();
    }

    private void showError(String msg) { errorLabel.setText(msg); }

    private void clearLoginForm() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
    }


    //  COMPONENT BUILDERS
    private JPanel makeRoleCard(String role, String subtitle, Color accent, String initial) {
        JPanel card = new JPanel() {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        hovered = true; repaint();
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        hovered = false; repaint();
                        setCursor(Cursor.getDefaultCursor());
                    }
                    @Override public void mouseClicked(MouseEvent e) {
                        selectedRole = role;
                        roleLabel.setText("Logging in as " + getRoleDisplayName(role));
                        clearLoginForm();
                        cardLayout.show(mainPanel, "LOGIN");
                    }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? BG_CARD2 : BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                g2.setColor(hovered ? accent : BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
            }
        };
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setOpaque(false);

        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(accent);
                g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial,
                    (40 - fm.stringWidth(initial)) / 2,
                    (40 - fm.getHeight()) / 2 + fm.getAscent());
            }
            @Override public Dimension getPreferredSize() { return new Dimension(40, 40); }
            @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
        };
        iconCircle.setOpaque(false);

        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setOpaque(false);

        JLabel roleLbl = new JLabel(getRoleDisplayName(role));
        roleLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        roleLbl.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLbl.setForeground(TEXT_MUTED);

        textBox.add(roleLbl);
        textBox.add(Box.createVerticalStrut(3));
        textBox.add(subLbl);

        card.add(iconCircle, BorderLayout.WEST);
        card.add(textBox,    BorderLayout.CENTER);
        return card;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(80, 100, 150));
                    g2.setFont(getFont());
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                }
            }
        };
        styleInputField(field);
        return field;
    }

    private JPasswordField makePasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(new Color(80, 100, 150));
                    g2.setFont(getFont());
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                }
            }
        };
        styleInputField(field);
        return field;
    }

    private void styleInputField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_CARD2);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT, 1, true),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }
        });
    }

    private JButton makeLoginButton() {
        JButton btn = new JButton("Sign In") {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? ACCENT_HOVER : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BG_DARK);  // dark text on gold button
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t,
                    (getWidth()  - fm.stringWidth(t)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> handleLogin());
        return btn;
    }

    private JButton makeCloseButton() {
        JButton btn = new JButton("✕") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                String t = "✕";
                g2.drawString(t, (28 - fm.stringWidth(t)) / 2,
                    (28 - fm.getHeight()) / 2 + fm.getAscent());
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> System.exit(0));
        return btn;
    }

    private JLabel makeWrappedLabel(String html, Color color, int size) {
        JLabel lbl = new JLabel(html);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, size));
        lbl.setForeground(color);
        return lbl;
    }

    private void addDragSupport(JPanel panel) {
        final Point[] dragStart = {null};
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragStart[0] = e.getLocationOnScreen();
            }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragStart[0] != null) {
                    Point cur = e.getLocationOnScreen();
                    Point loc = getLocation();
                    setLocation(loc.x + cur.x - dragStart[0].x,
                                loc.y + cur.y - dragStart[0].y);
                    dragStart[0] = cur;
                }
            }
        });
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "Manager":      return "Manager";
            case "CounterStaff": return "Counter Staff";
            case "Technician":   return "Technician";
            case "Customer":     return "Customer";
            default:             return role;
        }
    }
}