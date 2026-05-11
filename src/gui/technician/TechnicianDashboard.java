package gui.technician;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import model.Appointment;
import model.Feedback;
import model.Technician;
import model.User;
import util.FileHandler;
import util.Session;

/**
 * GUI CLASS — TechnicianDashboard
 * ---------------------------------
 * MEMBER 4 is responsible for implementing all features in this file.
 *
 * FEATURES:
 *   [1] Edit own profile (first name, last name, email, phone, password)
 *   [2] View appointments assigned to THIS technician
 *   [3] Click an appointment to see full details
 *   [4] Mark appointment as Completed (and revert back to Pending)
 *   [5] Write feedback for a completed appointment
 *
 * EXTRA FEATURE:
 *   [6] Dashboard home screen with work statistics overview
 */
public class TechnicianDashboard extends JFrame {

    // COLOURS
    private static final Color BG_DARK      = new Color(15,  17,  26);
    private static final Color BG_CARD      = new Color(24,  27,  42);
    private static final Color BG_CARD2     = new Color(30,  34,  52);
    private static final Color ACCENT       = new Color(245, 158, 11);
    private static final Color TEXT_PRIMARY = new Color(240, 241, 255);
    private static final Color TEXT_MUTED   = new Color(148, 151, 180);
    private static final Color BORDER_COLOR = new Color(55,  58,  80);
    private static final Color DANGER       = new Color(239, 68,  68);
    private static final Color SUCCESS      = new Color(34, 197,  94);
    private static final Color INFO         = new Color(59, 130, 246);

    // STATE
    private Technician currentTech;
    private boolean    profileEditMode = false;
    private boolean    passwordVisible = false;

    // PROFILE FIELDS (used in edit mode)
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField passwordField;
    private JLabel     errorMsg;
    private JPanel     profileCard;

    // LAYOUT
    private CardLayout contentLayout;
    private JPanel     contentPanel;
    private JPanel     dashboardPanel;
    private JLabel     topBarUserLabel;

    // CONSTRUCTOR
    public TechnicianDashboard(Technician tech) {
        this.currentTech = tech;

        setTitle("APU-ASC — Technician Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(buildTopBar(),  BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);
        setContentPane(root);
    }

    //  TOP BAR
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(14, 24, 14, 24)
        ));

        JLabel title = new JLabel("APU Automotive Service Centre");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightSide.setOpaque(false);

        topBarUserLabel = new JLabel("🔧  " + currentTech.getFullName() + "  ·  Technician");
        topBarUserLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        topBarUserLabel.setForeground(TEXT_MUTED);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        logoutBtn.setForeground(DANGER);
        logoutBtn.setBackground(new Color(0,0,0,0));
        logoutBtn.setOpaque(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            Session.clearSession();
            dispose();
            new main.LoginFrame().setVisible(true);
        });

        rightSide.add(topBarUserLabel);
        rightSide.add(logoutBtn);
        bar.add(title,     BorderLayout.WEST);
        bar.add(rightSide, BorderLayout.EAST);
        return bar;
    }

    //  SIDEBAR
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_CARD);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
            new EmptyBorder(24, 0, 24, 0)
        ));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel section = new JLabel("  TECHNICIAN MENU");
        section.setFont(new Font("SansSerif", Font.BOLD, 10));
        section.setForeground(TEXT_MUTED);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(makeNavButton("📊  Dashboard",        "DASHBOARD"));
        sidebar.add(makeNavButton("👤  My Profile",       "PROFILE"));
        sidebar.add(makeNavButton("📅  My Appointments",  "APPOINTMENTS"));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    //  CONTENT PANELS
    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(BG_DARK);

        dashboardPanel = buildDashboardPanel();
        contentPanel.add(dashboardPanel,           "DASHBOARD");
        contentPanel.add(buildProfilePanel(),      "PROFILE");
        contentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");

        contentLayout.show(contentPanel, "DASHBOARD");
        return contentPanel;
    }


    //  PANEL 0 — DASHBOARD (EXTRA FEATURE)
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel headingBlock = new JPanel();
        headingBlock.setLayout(new BoxLayout(headingBlock, BoxLayout.Y_AXIS));
        headingBlock.setOpaque(false);

        JLabel heading = new JLabel("Welcome back, " + currentTech.getFirstName() + " 👋");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Here's an overview of your work at APU-ASC");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        headingBlock.add(heading);
        headingBlock.add(Box.createVerticalStrut(4));
        headingBlock.add(subtitle);

        JButton refreshBtn = makeSecondaryButton("⟳  Refresh");
        refreshBtn.addActionListener(e -> refreshDashboard());

        headerRow.add(headingBlock, BorderLayout.WEST);
        headerRow.add(refreshBtn,   BorderLayout.EAST);

        List<Appointment> myAppointments = FileHandler.loadAllAppointments()
            .stream()
            .filter(a -> a.getTechnicianID().equals(currentTech.getUserID()))
            .collect(java.util.stream.Collectors.toList());

        int total     = myAppointments.size();
        int pending   = (int) myAppointments.stream().filter(a -> "Pending".equals(a.getStatus())).count();
        int completed = (int) myAppointments.stream().filter(a -> "Completed".equals(a.getStatus())).count();

        String todayStr = LocalDate.now().toString();
        int todayCount = (int) myAppointments.stream()
            .filter(a -> todayStr.equals(a.getDate()))
            .count();

        int normalCount = (int) myAppointments.stream().filter(a -> "Normal".equals(a.getServiceType())).count();
        int majorCount  = (int) myAppointments.stream().filter(a -> "Major".equals(a.getServiceType())).count();

        int completionRate = total == 0 ? 0 : (completed * 100) / total;

        long feedbackCount = FileHandler.loadAllFeedbacks().stream()
            .filter(f -> f.getTechnicianID().equals(currentTech.getUserID()))
            .count();

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        statsRow.add(makeStatCard("📋  Total Appointments", String.valueOf(total),    "Assigned to you",   ACCENT));
        statsRow.add(makeStatCard("⏳  Pending",            String.valueOf(pending),  "Awaiting service",  INFO));
        statsRow.add(makeStatCard("✅  Completed",          String.valueOf(completed), "Jobs finished",    SUCCESS));
        statsRow.add(makeStatCard("📅  Today",              String.valueOf(todayCount), "On " + todayStr,  DANGER));

        JPanel middleRow = new JPanel(new GridLayout(1, 2, 16, 0));
        middleRow.setOpaque(false);
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        middleRow.add(makeServiceBreakdownCard(normalCount, majorCount));
        middleRow.add(makeCompletionRateCard(completionRate, completed, total, (int) feedbackCount));

        JPanel recentCard = makeRecentActivityCard(myAppointments);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        body.add(statsRow);
        body.add(Box.createVerticalStrut(16));
        body.add(middleRow);
        body.add(Box.createVerticalStrut(16));
        body.add(recentCard);
        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(scroll,    BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeStatCard(String label, String value, String subtitle, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 1, BORDER_COLOR),
                new EmptyBorder(16, 18, 16, 18)
            )
        ));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblLabel.setForeground(TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblValue.setForeground(TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSubtitle.setForeground(TEXT_MUTED);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        lblLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(lblLabel);
        center.add(Box.createVerticalStrut(4));
        center.add(lblValue);
        center.add(Box.createVerticalStrut(2));
        center.add(lblSubtitle);

        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel makeServiceBreakdownCard(int normalCount, int majorCount) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Service Type Breakdown");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        int total = normalCount + majorCount;

        card.add(title);
        card.add(Box.createVerticalStrut(14));
        card.add(makeBarRow("Normal Service", normalCount, total, INFO));
        card.add(Box.createVerticalStrut(10));
        card.add(makeBarRow("Major Service",  majorCount,  total, ACCENT));

        return card;
    }

    private JPanel makeBarRow(String label, int count, int total, Color barColor) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel labelLine = new JPanel(new BorderLayout());
        labelLine.setOpaque(false);
        labelLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);

        int pct = total == 0 ? 0 : (count * 100) / total;
        JLabel countLbl = new JLabel(count + "  (" + pct + "%)");
        countLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        countLbl.setForeground(TEXT_PRIMARY);
        countLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        labelLine.add(lbl,      BorderLayout.WEST);
        labelLine.add(countLbl, BorderLayout.EAST);

        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(BG_CARD2);
                g2.fillRoundRect(0, 0, w, h, h, h);
                int fillW = total == 0 ? 0 : (int) ((long) w * count / total);
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, fillW, h, h, h);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setPreferredSize(new Dimension(0, 8));

        row.add(labelLine);
        row.add(Box.createVerticalStrut(6));
        row.add(bar);
        return row;
    }

    private JPanel makeCompletionRateCard(int rate, int completed, int total, int feedbackCount) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Performance");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel rateLbl = new JLabel(rate + "%");
        rateLbl.setFont(new Font("SansSerif", Font.BOLD, 36));
        rateLbl.setForeground(rate >= 75 ? SUCCESS : (rate >= 40 ? ACCENT : DANGER));
        rateLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel rateSubtitle = new JLabel("Completion rate (" + completed + " of " + total + " jobs)");
        rateSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rateSubtitle.setForeground(TEXT_MUTED);
        rateSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel feedbackLbl = new JLabel("📝  " + feedbackCount + " feedback report" + (feedbackCount == 1 ? "" : "s") + " submitted");
        feedbackLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        feedbackLbl.setForeground(TEXT_MUTED);
        feedbackLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(rateLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(rateSubtitle);
        card.add(Box.createVerticalStrut(14));
        card.add(sep);
        card.add(Box.createVerticalStrut(14));
        card.add(feedbackLbl);

        return card;
    }

    private JPanel makeRecentActivityCard(List<Appointment> myAppointments) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel title = new JLabel("Recent Activity");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(14));

        if (myAppointments.isEmpty()) {
            JLabel empty = new JLabel("No appointments yet.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
            empty.setForeground(TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(empty);
            return card;
        }

        Map<String, String> customerNames = loadCustomerNames();

        List<Appointment> recent = myAppointments.stream()
            .sorted((a, b) -> safeCompareDate(b.getDate(), a.getDate()))
            .limit(5)
            .collect(java.util.stream.Collectors.toList());

        for (int i = 0; i < recent.size(); i++) {
            Appointment a = recent.get(i);
            String customerName = customerNames.getOrDefault(a.getCustomerID(), a.getCustomerID());
            card.add(makeActivityRow(a, customerName));
            if (i < recent.size() - 1) {
                card.add(Box.createVerticalStrut(8));
            }
        }
        return card;
    }

    private JPanel makeActivityRow(Appointment a, String customerName) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(BG_CARD2);
        row.setBorder(new EmptyBorder(10, 14, 10, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        JLabel mainLbl = new JLabel(a.getAppointmentID() + "  ·  " + customerName);
        mainLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        mainLbl.setForeground(TEXT_PRIMARY);
        JLabel subLbl = new JLabel(a.getDate() + "  " + a.getTime() + "  ·  " + a.getServiceType());
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subLbl.setForeground(TEXT_MUTED);
        left.add(mainLbl);
        left.add(Box.createVerticalStrut(2));
        left.add(subLbl);

        JLabel statusPill = new JLabel(a.getStatus());
        statusPill.setFont(new Font("SansSerif", Font.BOLD, 11));
        boolean done = "Completed".equals(a.getStatus());
        statusPill.setForeground(done ? SUCCESS : ACCENT);
        statusPill.setBorder(new EmptyBorder(4, 10, 4, 10));
        statusPill.setHorizontalAlignment(SwingConstants.CENTER);

        row.add(left,       BorderLayout.WEST);
        row.add(statusPill, BorderLayout.EAST);
        return row;
    }

    private Map<String, String> loadCustomerNames() {
        Map<String, String> map = new java.util.HashMap<>();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader("src/data/customers.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 3) {
                    map.put(p[0], p[1] + " " + p[2]);
                }
            }
        } catch (java.io.IOException e) {
            System.err.println("Could not load customer names for dashboard: " + e.getMessage());
        }
        return map;
    }

    private int safeCompareDate(String d1, String d2) {
        try {
            return LocalDate.parse(d1).compareTo(LocalDate.parse(d2));
        } catch (DateTimeParseException ex) {
            return d1.compareTo(d2);
        }
    }

    private void refreshDashboard() {
        contentPanel.remove(dashboardPanel);
        dashboardPanel = buildDashboardPanel();
        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentLayout.show(contentPanel, "DASHBOARD");
        contentPanel.revalidate();
        contentPanel.repaint();
    }


    //  PANEL 1 — MY PROFILE (with inline edit mode)
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("My Profile");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        profileCard = new JPanel();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBackground(BG_CARD);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(28, 28, 28, 28)
        ));

        refreshProfileUI();

        panel.add(heading, BorderLayout.NORTH);
        panel.add(profileCard, BorderLayout.CENTER);
        return panel;
    }

    private void refreshProfileUI() {
        profileCard.removeAll();

        if (!profileEditMode) {
            // VIEW MODE
            profileCard.add(makeInfoRow("Full Name", currentTech.getFullName()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Username",  currentTech.getUsername()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Email",     currentTech.getEmail()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Phone",     currentTech.getPhone()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Password",  maskPassword(currentTech.getPassword())));
            profileCard.add(Box.createVerticalStrut(24));

            JButton editBtn = makePrimaryButton("✏  Edit Profile");
            editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            editBtn.addActionListener(e -> enterProfileEditMode());
            profileCard.add(editBtn);

        } else {
            // EDIT MODE
            profileCard.add(makeEditRow("First Name", firstNameField = makeEditableTextField(currentTech.getFirstName())));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeEditRow("Last Name",  lastNameField  = makeEditableTextField(currentTech.getLastName())));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Username", currentTech.getUsername()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeEditRow("Email", emailField = makeEditableTextField(currentTech.getEmail())));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeEditRow("Phone", phoneField = makeEditableTextField(currentTech.getPhone())));
            profileCard.add(Box.createVerticalStrut(12));

            // Password with eye toggle
            JPanel passwordRow = new JPanel(new BorderLayout(16, 0));
            passwordRow.setOpaque(false);
            passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            passwordRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel passwordLbl = new JLabel("Password:");
            passwordLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            passwordLbl.setForeground(TEXT_MUTED);
            passwordLbl.setPreferredSize(new Dimension(100, 20));

            passwordField = makeEditableTextField(maskPassword(currentTech.getPassword()));

            JPanel passwordFieldPanel = new JPanel(new BorderLayout(8, 0));
            passwordFieldPanel.setOpaque(false);
            passwordFieldPanel.add(passwordField, BorderLayout.CENTER);

            JButton eyeToggle = new JButton(passwordVisible ? "🙈" : "👁");
            eyeToggle.setFont(new Font("SansSerif", Font.PLAIN, 16));
            eyeToggle.setBackground(new Color(0, 0, 0, 0));
            eyeToggle.setOpaque(false);
            eyeToggle.setBorderPainted(false);
            eyeToggle.setFocusPainted(false);
            eyeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eyeToggle.setPreferredSize(new Dimension(30, 28));
            eyeToggle.addActionListener(e -> {
                passwordVisible = !passwordVisible;
                if (passwordVisible) {
                    eyeToggle.setText("🙈");
                    passwordField.setText(currentTech.getPassword());
                } else {
                    eyeToggle.setText("👁");
                    passwordField.setText(maskPassword(currentTech.getPassword()));
                }
            });
            passwordFieldPanel.add(eyeToggle, BorderLayout.EAST);

            passwordRow.add(passwordLbl,         BorderLayout.WEST);
            passwordRow.add(passwordFieldPanel,  BorderLayout.CENTER);
            profileCard.add(passwordRow);
            profileCard.add(Box.createVerticalStrut(16));

            // Error message label
            errorMsg = new JLabel(" ");
            errorMsg.setForeground(DANGER);
            errorMsg.setFont(new Font("SansSerif", Font.PLAIN, 12));
            errorMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
            profileCard.add(errorMsg);
            profileCard.add(Box.createVerticalStrut(12));

            // Action buttons
            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            buttonRow.setOpaque(false);
            buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            JButton doneBtn = makePrimaryButton("✓  Save");
            doneBtn.addActionListener(e -> onProfileSave());

            JButton cancelBtn = makeSecondaryButton("✕  Cancel");
            cancelBtn.addActionListener(e -> exitProfileEditMode());

            buttonRow.add(doneBtn);
            buttonRow.add(cancelBtn);
            profileCard.add(buttonRow);
        }

        profileCard.revalidate();
        profileCard.repaint();
    }

    private JPanel makeEditRow(String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(100, 20));
        row.add(lbl,   BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JTextField makeEditableTextField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBackground(BG_CARD2);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        field.setMaximumSize(new Dimension(300, 30));
        field.setPreferredSize(new Dimension(300, 30));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT, 2),
                    new EmptyBorder(3, 7, 3, 7)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(4, 8, 4, 8)
                ));
            }
        });
        return field;
    }

    private void enterProfileEditMode() {
        profileEditMode = true;
        passwordVisible = false;
        refreshProfileUI();
    }

    private void exitProfileEditMode() {
        profileEditMode = false;
        passwordVisible = false;
        refreshProfileUI();
    }

    private String maskPassword(String password) {
        if (password == null || password.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < password.length(); i++) sb.append('*');
        return sb.toString();
    }

    private void onProfileSave() {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String email     = emailField.getText().trim();
        String phone     = phoneField.getText().trim();
        String password  = passwordVisible ? passwordField.getText().trim() : currentTech.getPassword();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorMsg.setText("❌ All fields are required.");
            return;
        }

        if (!phone.matches("\\d{10,11}")) {
            errorMsg.setText("❌ Phone must be 10-11 digits (numbers only).");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            errorMsg.setText("❌ Email format is invalid.");
            return;
        }

        if (passwordVisible && password.length() < 4) {
            errorMsg.setText("❌ Password must be at least 4 characters.");
            return;
        }

        List<User> users = FileHandler.loadAllUsers();
        User userToUpdate = null;
        for (User u : users) {
            if (u.getUserID().equals(currentTech.getUserID())) {
                userToUpdate = u;
                break;
            }
        }

        if (userToUpdate == null) {
            errorMsg.setText("❌ Error: User not found in database.");
            return;
        }

        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);
        userToUpdate.setEmail(email);
        userToUpdate.setPhone(phone);
        if (passwordVisible) {
            userToUpdate.setPassword(password);
        }

        FileHandler.saveAllUsers(users);

        currentTech.setFirstName(firstName);
        currentTech.setLastName(lastName);
        currentTech.setEmail(email);
        currentTech.setPhone(phone);
        if (passwordVisible) {
            currentTech.setPassword(password);
        }

        profileEditMode = false;
        passwordVisible = false;
        refreshProfileUI();

        if (topBarUserLabel != null) {
            topBarUserLabel.setText("🔧  " + currentTech.getFullName() + "  ·  Technician");
        }

        JOptionPane.showMessageDialog(this,
            "Profile updated successfully!",
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }


    //  PANEL 2 — MY APPOINTMENTS
    private JPanel buildAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel leftPane = new JPanel(new BorderLayout(0, 12));
        leftPane.setOpaque(false);
        leftPane.setPreferredSize(new Dimension(420, 0));

        JLabel heading = new JLabel("My Appointments");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        String[] cols = {"ID", "Date", "Time", "Service", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Appointment> myAppointments = FileHandler.loadAllAppointments()
            .stream()
            .filter(a -> a.getTechnicianID().equals(currentTech.getUserID()))
            .collect(java.util.stream.Collectors.toList());

        myAppointments.forEach(a -> model.addRow(new Object[]{
            a.getAppointmentID(), a.getDate(), a.getTime(),
            a.getServiceType(), a.getStatus()
        }));

        JTable table = makeStyledTable(model);
        JScrollPane scroll = makeScrollPane(table);

        leftPane.add(heading, BorderLayout.NORTH);
        leftPane.add(scroll,  BorderLayout.CENTER);

        JPanel rightPane = new JPanel();
        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
        rightPane.setBackground(BG_CARD);
        rightPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel detailHeading = new JLabel("Appointment Details");
        detailHeading.setFont(new Font("SansSerif", Font.BOLD, 16));
        detailHeading.setForeground(TEXT_PRIMARY);
        detailHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detailContent = new JLabel(
            "<html><div style='color:#9497B4;'>" +
            "Select an appointment from the list<br>" +
            "to view its details." +
            "</div></html>");
        detailContent.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detailContent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton completeBtn = makePrimaryButton("✅  Mark as Completed");
        completeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        completeBtn.setEnabled(false);

        JButton feedbackBtn = makeSecondaryButton("📝  Write Feedback");
        feedbackBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        feedbackBtn.setEnabled(false);

        rightPane.add(detailHeading);
        rightPane.add(Box.createVerticalStrut(16));
        rightPane.add(detailContent);
        rightPane.add(Box.createVerticalStrut(24));
        rightPane.add(completeBtn);
        rightPane.add(Box.createVerticalStrut(10));
        rightPane.add(feedbackBtn);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row == -1) return;

            String apptID = (String) model.getValueAt(row, 0);
            String status = (String) model.getValueAt(row, 4);

            detailContent.setText(
                "<html><div style='line-height:1.8;'>" +
                "<b style='color:#F0F1FF;'>Appointment ID:</b>  " + apptID + "<br>" +
                "<b style='color:#F0F1FF;'>Date:</b>  " + model.getValueAt(row, 1) + "<br>" +
                "<b style='color:#F0F1FF;'>Time:</b>  " + model.getValueAt(row, 2) + "<br>" +
                "<b style='color:#F0F1FF;'>Service:</b>  " + model.getValueAt(row, 3) + "<br>" +
                "<b style='color:#F0F1FF;'>Status:</b>  " + status +
                "</div></html>"
            );

            if ("Pending".equals(status)) {
                completeBtn.setText("✅  Mark as Completed");
            } else {
                completeBtn.setText("↩  Revert to Pending");
            }
            completeBtn.setEnabled(true);
            feedbackBtn.setEnabled("Completed".equals(status));
        });

        completeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String apptID = (String) model.getValueAt(row, 0);
            String currentStatus = (String) model.getValueAt(row, 4);

            String newStatus  = "Pending".equals(currentStatus) ? "Completed" : "Pending";
            String actionWord = "Pending".equals(currentStatus) ? "mark as Completed" : "revert to Pending";

            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + actionWord + " appointment " + apptID + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            List<Appointment> appointments = FileHandler.loadAllAppointments();
            boolean saved = false;
            for (Appointment appt : appointments) {
                if (appt.getAppointmentID().equals(apptID)) {
                    appt.setStatus(newStatus);
                    saved = true;
                    break;
                }
            }
            if (saved) {
                FileHandler.saveAllAppointments(appointments);
                model.setValueAt(newStatus, row, 4);

                if ("Completed".equals(newStatus)) {
                    completeBtn.setText("↩  Revert to Pending");
                    feedbackBtn.setEnabled(true);
                } else {
                    completeBtn.setText("✅  Mark as Completed");
                    feedbackBtn.setEnabled(false);
                }

                JOptionPane.showMessageDialog(this,
                    "Appointment " + apptID + " is now " + newStatus + ".",
                    "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Unable to update appointment status. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        feedbackBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String apptID = (String) model.getValueAt(row, 0);
            openFeedbackDialog(apptID);
        });

        panel.add(leftPane,  BorderLayout.WEST);
        panel.add(rightPane, BorderLayout.CENTER);
        return panel;
    }

    private void openFeedbackDialog(String apptID) {
        JTextArea textArea = new JTextArea(5, 30);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(textArea);

        int result = JOptionPane.showConfirmDialog(this,
            new Object[]{"Enter feedback for appointment " + apptID + ":", sp},
            "Write Feedback", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String text = textArea.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Feedback cannot be empty.");
                return;
            }

            Feedback newFeedback = new Feedback(apptID, currentTech.getUserID(), text);
            List<Feedback> feedbacks = FileHandler.loadAllFeedbacks();
            feedbacks.add(newFeedback);
            FileHandler.saveAllFeedbacks(feedbacks);

            JOptionPane.showMessageDialog(this, "Feedback saved successfully.");
        }
    }

    //  SHARED HELPERS
    private JPanel makeInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(100, 20));
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 13));
        val.setForeground(TEXT_PRIMARY);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    private JButton makePrimaryButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(new Color(15, 17, 26));
        btn.setBackground(ACCENT);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeSecondaryButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(BG_CARD2);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 14, 8, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeNavButton(String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if ("DASHBOARD".equals(cardName)) {
                refreshDashboard();
            } else {
                contentLayout.show(contentPanel, cardName);
            }
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(TEXT_PRIMARY); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(TEXT_MUTED);   }
        });
        return btn;
    }

    private JTable makeStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 60));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_CARD2);
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);
        return table;
    }

    private JScrollPane makeScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        return sp;
    }
}