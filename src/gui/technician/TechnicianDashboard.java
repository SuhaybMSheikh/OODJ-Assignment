package gui.technician;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
 * MEMBER 4 features:
 *   [1] Edit own profile (first/last name, email, phone, password)
 *       - Username LOCKED (group decision: usernames cannot change once created)
 *       - Validations: alphabets-only names, exactly 10-digit phone, valid email
 *   [2] View appointments assigned to THIS technician
 *   [3] Click an appointment to see full details + customer comment
 *   [4] Mark appointment Completed (and revert back to Ongoing)
 *   [5] Write feedback + View/Edit existing feedback for an appointment
 *   [6] feedbacks.txt saves the technician ID (T001, T002...) not userID
 * EXTRA:
 *   [7] Dashboard home screen with work statistics
 *   [8] Live search / filter on appointments
 *   [9] Customer Comments tab — dedicated view of all customer comments
 *
 * Styling matches ManagerDashboard / CounterStaffDashboard for team consistency
 * (plain nav highlight via paintComponent — no Windows rendering glitch).
 */
public class TechnicianDashboard extends JFrame {

    // COLOURS — same structure as Manager; amber accent is the Technician colour
    private static final Color BG_DARK      = new Color(15,  17,  26);
    private static final Color BG_CARD      = new Color(0, 0, 0);
    private static final Color BG_CARD2     = new Color(30,  34,  52);
    private static final Color ACCENT       = new Color(245, 158, 11);
    private static final Color TEXT_PRIMARY = new Color(240, 241, 255);
    private static final Color TEXT_MUTED   = new Color(148, 151, 180);
    private static final Color BORDER_COLOR = new Color(55,  58,  80);
    private static final Color DANGER       = new Color(239, 68,  68);
    private static final Color SUCCESS      = new Color(34, 197,  94);
    private static final Color INFO         = new Color(59, 130, 246);
    private static final String F = "SansSerif";

    // STATE
    private Technician currentTech;
    private boolean    profileEditMode = false;
    private boolean    passwordVisible = false;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField passwordField;
    private JLabel     errorMsg;
    private JPanel     profileCard;

    private CardLayout contentLayout;
    private JPanel     contentPanel;
    private JPanel     dashboardPanel;
    private JPanel     commentsPanel;
    private JLabel     topBarUserLabel;
    private String activeCardName = "DASHBOARD";
    private List<JButton> navButtons = new ArrayList<>();

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
            new EmptyBorder(14, 24, 14, 24)));

        JLabel title = new JLabel("APU Automotive Service Centre");
        title.setFont(new Font(F, Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightSide.setOpaque(false);

        topBarUserLabel = new JLabel("\uD83D\uDD27  " + currentTech.getFullName() + "  ·  Technician");
        topBarUserLabel.setFont(new Font(F, Font.PLAIN, 13));
        topBarUserLabel.setForeground(TEXT_MUTED);
        topBarUserLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topBarUserLabel.setToolTipText("Open My Profile");
        topBarUserLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (profileEditMode) {
                    int result = JOptionPane.showConfirmDialog(TechnicianDashboard.this,
                            "You have unsaved changes. Discard them?",
                            "Unsaved Changes",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (result != JOptionPane.YES_OPTION) return;
                    exitProfileEditMode();
                }
                contentLayout.show(contentPanel, "PROFILE");
                activeCardName = "PROFILE";
                updateNavButtonStyles();
            }
            @Override public void mouseEntered(MouseEvent e) {
                topBarUserLabel.setForeground(TEXT_PRIMARY);
            }
            @Override public void mouseExited(MouseEvent e) {
                topBarUserLabel.setForeground(TEXT_MUTED);
            }
        });

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font(F, Font.PLAIN, 13));
        logoutBtn.setForeground(DANGER);
        logoutBtn.setBackground(new Color(0, 0, 0, 0));
        logoutBtn.setOpaque(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
            new EmptyBorder(6, 0, 24, 0)));
        sidebar.setPreferredSize(new Dimension(220, 0));

        ImageIcon logoIcon = new ImageIcon("src/data/apu_logo_topPanel.png");
        Image scaledLogo = logoIcon.getImage().getScaledInstance(192, 128, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoPanel.setOpaque(false);
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        logoPanel.add(logoLabel);
        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(14));

        JLabel section = new JLabel("  TECHNICIAN MENU");
        section.setFont(new Font(F, Font.BOLD, 10));
        section.setForeground(TEXT_MUTED);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(makeNavButton("\uD83D\uDCCA  Dashboard",          "DASHBOARD"));
        sidebar.add(makeNavButton("\uD83D\uDCC5  My Appointments",    "APPOINTMENTS"));
        sidebar.add(makeNavButton("\uD83D\uDCAC  Customer Comments",  "COMMENTS"));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    //  CONTENT
    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(BG_DARK);
        dashboardPanel = buildDashboardPanel();
        commentsPanel  = buildCommentsPanel();
        contentPanel.add(dashboardPanel,           "DASHBOARD");
        contentPanel.add(buildProfilePanel(),      "PROFILE");
        contentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");
        contentPanel.add(commentsPanel,            "COMMENTS");
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
        JLabel heading = new JLabel("Welcome back, " + currentTech.getFirstName() + " \uD83D\uDC4B");
        heading.setFont(new Font(F, Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Here's an overview of your work at APU-ASC");
        subtitle.setFont(new Font(F, Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        headingBlock.add(heading);
        headingBlock.add(subtitle);
        JButton refreshBtn = makeSecondaryButton("\u27F3  Refresh");
        refreshBtn.addActionListener(e -> refreshDashboard());
        headerRow.add(headingBlock, BorderLayout.WEST);
        headerRow.add(refreshBtn,   BorderLayout.EAST);

        String myTechnicianID = FileHandler.getTechnicianIDByUserID(currentTech.getUserID());
        List<Appointment> myAppointments = FileHandler.loadAllAppointments()
            .stream()
            .filter(a -> {
                String stored = a.getTechnicianID();
                return stored.equals(currentTech.getUserID())
                        || (myTechnicianID != null && stored.equals(myTechnicianID));
            })
            .collect(java.util.stream.Collectors.toList());

        int total     = myAppointments.size();
        // Treat both "Pending" (legacy) and "Ongoing" (new) the same — both are ongoing work
        int ongoing   = (int) myAppointments.stream()
            .filter(a -> "Ongoing".equals(a.getStatus()) || "Pending".equals(a.getStatus()))
            .count();
        int completed = (int) myAppointments.stream().filter(a -> "Completed".equals(a.getStatus())).count();
        String todayStr = LocalDate.now().toString();
        int todayCount = (int) myAppointments.stream().filter(a -> todayStr.equals(a.getDate())).count();
        int normalCount = (int) myAppointments.stream().filter(a -> "Normal".equals(a.getServiceType())).count();
        int majorCount  = (int) myAppointments.stream().filter(a -> "Major".equals(a.getServiceType())).count();
        int completionRate = total == 0 ? 0 : (completed * 100) / total;

        // Count feedbacks by THIS tech's T-ID (not userID) — matches group standard
        long feedbackCount = FileHandler.loadAllFeedbacks().stream()
            .filter(f -> myTechnicianID != null && myTechnicianID.equals(f.getTechnicianID()))
            .count();

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        statsRow.add(makeStatCard("TOTAL APPOINTMENTS", String.valueOf(total),     "All bookings assigned", ACCENT));
        statsRow.add(makeStatCard("ONGOING",            String.valueOf(ongoing),   "Awaiting completion",   ACCENT));
        statsRow.add(makeStatCard("COMPLETED",          String.valueOf(completed), "Jobs finished",         SUCCESS));
        statsRow.add(makeStatCard("TODAY",              String.valueOf(todayCount),"On " + todayStr,        INFO));

        JPanel middleRow = new JPanel(new GridLayout(1, 2, 16, 0));
        middleRow.setOpaque(false);
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
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
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20))));
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font(F, Font.BOLD, 11));
        lblLabel.setForeground(TEXT_MUTED);
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font(F, Font.BOLD, 34));
        lblValue.setForeground(TEXT_PRIMARY);
        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font(F, Font.PLAIN, 11));
        lblSubtitle.setForeground(TEXT_MUTED);
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        lblLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(lblLabel);
        center.add(Box.createVerticalStrut(8));
        center.add(lblValue);
        center.add(Box.createVerticalStrut(6));
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
            new EmptyBorder(20, 22, 20, 22)));
        JLabel title = new JLabel("Service Type Breakdown");
        title.setFont(new Font(F, Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        int total = normalCount + majorCount;
        card.add(title);
        card.add(Box.createVerticalStrut(18));
        card.add(makeBarRow("Normal Service", normalCount, total, INFO));
        card.add(Box.createVerticalStrut(14));
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
        lbl.setFont(new Font(F, Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);
        int pct = total == 0 ? 0 : (count * 100) / total;
        JLabel countLbl = new JLabel(count + "  (" + pct + "%)");
        countLbl.setFont(new Font(F, Font.BOLD, 12));
        countLbl.setForeground(TEXT_PRIMARY);
        countLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        labelLine.add(lbl,      BorderLayout.WEST);
        labelLine.add(countLbl, BorderLayout.EAST);
        final int fc = count, ft = total;
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(BG_CARD2);
                g2.fillRoundRect(0, 0, w, h, h, h);
                int fw = ft == 0 ? 0 : (int) ((long) w * fc / ft);
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, fw, h, h, h);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        bar.setPreferredSize(new Dimension(0, 8));
        row.add(labelLine);
        row.add(Box.createVerticalStrut(7));
        row.add(bar);
        return row;
    }

    private JPanel makeCompletionRateCard(int rate, int completed, int total, int feedbackCount) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 22, 20, 22)));
        JLabel title = new JLabel("Performance");
        title.setFont(new Font(F, Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel rateLbl = new JLabel(rate + "%");
        rateLbl.setFont(new Font(F, Font.BOLD, 38));
        rateLbl.setForeground(rate >= 75 ? SUCCESS : (rate >= 40 ? ACCENT : DANGER));
        rateLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel rateSubtitle = new JLabel("Completion rate (" + completed + " of " + total + " jobs)");
        rateSubtitle.setFont(new Font(F, Font.PLAIN, 12));
        rateSubtitle.setForeground(TEXT_MUTED);
        rateSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        JLabel feedbackLbl = new JLabel("\uD83D\uDCDD  " + feedbackCount
            + " feedback report" + (feedbackCount == 1 ? "" : "s") + " submitted");
        feedbackLbl.setFont(new Font(F, Font.PLAIN, 12));
        feedbackLbl.setForeground(TEXT_MUTED);
        feedbackLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(12));
        card.add(rateLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(rateSubtitle);
        card.add(Box.createVerticalStrut(16));
        card.add(sep);
        card.add(Box.createVerticalStrut(16));
        card.add(feedbackLbl);
        return card;
    }

    private JPanel makeRecentActivityCard(List<Appointment> myAppointments) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 22, 20, 22)));
        JLabel title = new JLabel("Recent Activity");
        title.setFont(new Font(F, Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(16));
        if (myAppointments.isEmpty()) {
            JLabel empty = new JLabel("No appointments yet.");
            empty.setFont(new Font(F, Font.PLAIN, 13));
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
            String cn = customerNames.getOrDefault(a.getCustomerID(), a.getCustomerID());
            card.add(makeActivityRow(a, cn));
            if (i < recent.size() - 1) card.add(Box.createVerticalStrut(8));
        }
        return card;
    }

    private JPanel makeActivityRow(Appointment a, String customerName) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(BG_CARD2);
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        row.setPreferredSize(new Dimension(0, 68));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setBorder(new EmptyBorder(0, 0, 0, 8));
        JLabel mainLbl = new JLabel(a.getAppointmentID() + "   ·   " + customerName);
        mainLbl.setFont(new Font(F, Font.BOLD, 13));
        mainLbl.setForeground(TEXT_PRIMARY);
        JLabel subLbl = new JLabel(a.getDate() + "  " + a.getTime() + "  ·  " + a.getServiceType());
        subLbl.setFont(new Font(F, Font.PLAIN, 12));
        subLbl.setForeground(TEXT_MUTED);
        left.add(mainLbl);
        left.add(Box.createVerticalStrut(5));
        left.add(subLbl);
        boolean done = "Completed".equals(a.getStatus());
        JLabel statusPill = new JLabel(a.getStatus());
        statusPill.setFont(new Font(F, Font.BOLD, 11));
        statusPill.setForeground(done ? SUCCESS : ACCENT);
        statusPill.setBorder(new EmptyBorder(4, 10, 4, 10));
        statusPill.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(left,       BorderLayout.CENTER);
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
                if (p.length >= 3) map.put(p[0], p[1] + " " + p[2]);
            }
        } catch (java.io.IOException e) {
            System.err.println("Could not load customer names: " + e.getMessage());
        }
        return map;
    }

    private Map<String, String> loadComments() {
        Map<String, String> map = new java.util.HashMap<>();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader("src/data/comments.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 3) map.put(p[0], p[2]);
            }
        } catch (java.io.IOException e) {
            System.err.println("Could not load comments: " + e.getMessage());
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
        activeCardName = "DASHBOARD";
        updateNavButtonStyles();
    }

    private void refreshCommentsPanel() {
        contentPanel.remove(commentsPanel);
        commentsPanel = buildCommentsPanel();
        contentPanel.add(commentsPanel, "COMMENTS");
    }

    //  PANEL 1 — MY PROFILE  (plain style, same as Manager / CounterStaff)
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("My Profile");
        heading.setFont(new Font(F, Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        profileCard = new JPanel();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBackground(BG_CARD);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(28, 28, 28, 28)));
        profileCard.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        refreshProfileUI();

        panel.add(heading, BorderLayout.NORTH);
        panel.add(profileCard, BorderLayout.CENTER);
        return panel;
    }

    private void refreshProfileUI() {
        profileCard.removeAll();

        if (!profileEditMode) {
            profileCard.add(makeInfoRow("Full Name", currentTech.getFullName()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Username", currentTech.getUsername()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Email", currentTech.getEmail()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Phone", currentTech.getPhone()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Password", maskPassword(currentTech.getPassword())));
            profileCard.add(Box.createVerticalStrut(24));

            JButton editBtn = makePrimaryButton("\u270F  Edit Profile");
            editBtn.addActionListener(e -> enterProfileEditMode());
            JPanel editButtonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            editButtonRow.setOpaque(false);
            editButtonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            editButtonRow.add(editBtn);
            profileCard.add(editButtonRow);
        } else {
            JPanel fullNameRow = new JPanel(new BorderLayout(16, 0));
            fullNameRow.setOpaque(false);
            fullNameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel fullNameLbl = new JLabel("Full Name:");
            fullNameLbl.setFont(new Font(F, Font.PLAIN, 13));
            fullNameLbl.setForeground(TEXT_MUTED);
            fullNameLbl.setPreferredSize(new Dimension(100, 20));
            JPanel nameFieldsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
            nameFieldsPanel.setOpaque(false);
            firstNameField = makeEditableTextField(currentTech.getFirstName());
            lastNameField  = makeEditableTextField(currentTech.getLastName());
            Dimension nfh = new Dimension(0, 28);
            firstNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            lastNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            firstNameField.setPreferredSize(nfh);
            lastNameField.setPreferredSize(nfh);
            nameFieldsPanel.add(firstNameField);
            nameFieldsPanel.add(lastNameField);
            fullNameRow.add(fullNameLbl, BorderLayout.WEST);
            fullNameRow.add(nameFieldsPanel, BorderLayout.CENTER);
            profileCard.add(fullNameRow);
            profileCard.add(Box.createVerticalStrut(12));

            // Username — LOCKED per group decision (Suhayb's chat: usernames cannot change)
            profileCard.add(makeInfoRow("Username", currentTech.getUsername() + "  (cannot be changed)"));
            profileCard.add(Box.createVerticalStrut(12));

            JPanel emailRow = new JPanel(new BorderLayout(16, 0));
            emailRow.setOpaque(false);
            emailRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel emailLbl = new JLabel("Email:");
            emailLbl.setFont(new Font(F, Font.PLAIN, 13));
            emailLbl.setForeground(TEXT_MUTED);
            emailLbl.setPreferredSize(new Dimension(100, 20));
            emailField = makeEditableTextField(currentTech.getEmail());
            emailRow.add(emailLbl, BorderLayout.WEST);
            emailRow.add(emailField, BorderLayout.CENTER);
            profileCard.add(emailRow);
            profileCard.add(Box.createVerticalStrut(12));

            JPanel phoneRow = new JPanel(new BorderLayout(16, 0));
            phoneRow.setOpaque(false);
            phoneRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel phoneLbl = new JLabel("Phone:");
            phoneLbl.setFont(new Font(F, Font.PLAIN, 13));
            phoneLbl.setForeground(TEXT_MUTED);
            phoneLbl.setPreferredSize(new Dimension(100, 20));
            phoneField = makeEditableTextField(currentTech.getPhone());
            phoneRow.add(phoneLbl, BorderLayout.WEST);
            phoneRow.add(phoneField, BorderLayout.CENTER);
            profileCard.add(phoneRow);
            profileCard.add(Box.createVerticalStrut(12));

            JPanel passwordRow = new JPanel(new BorderLayout(16, 0));
            passwordRow.setOpaque(false);
            passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel passwordLbl = new JLabel("Password:");
            passwordLbl.setFont(new Font(F, Font.PLAIN, 13));
            passwordLbl.setForeground(TEXT_MUTED);
            passwordLbl.setPreferredSize(new Dimension(100, 20));
            passwordField = makeEditableTextField(maskPassword(currentTech.getPassword()));
            passwordField.setBorder(new EmptyBorder(0, 0, 0, 0));
            JPanel passwordFieldPanel = new JPanel(new BorderLayout(8, 0));
            passwordFieldPanel.setBackground(BG_CARD2);
            passwordFieldPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            passwordFieldPanel.setMaximumSize(new Dimension(200, 28));
            passwordFieldPanel.setPreferredSize(new Dimension(200, 28));
            passwordFieldPanel.add(passwordField, BorderLayout.CENTER);
            JButton eyeToggle = new JButton(makeEyeIcon(TEXT_MUTED));
            eyeToggle.setForeground(TEXT_MUTED);
            eyeToggle.setBackground(BG_CARD2);
            eyeToggle.setOpaque(false);
            eyeToggle.setContentAreaFilled(false);
            eyeToggle.setBorderPainted(false);
            eyeToggle.setFocusPainted(false);
            eyeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eyeToggle.setPreferredSize(new Dimension(38, 28));
            eyeToggle.setToolTipText("Show password");
            eyeToggle.getAccessibleContext().setAccessibleName("Show password");
            eyeToggle.addActionListener(e -> {
                passwordVisible = !passwordVisible;
                if (passwordVisible) {
                    eyeToggle.setForeground(TEXT_PRIMARY);
                    eyeToggle.setIcon(makeEyeIcon(TEXT_PRIMARY));
                    eyeToggle.setToolTipText("Hide password");
                    eyeToggle.getAccessibleContext().setAccessibleName("Hide password");
                    passwordField.setText(currentTech.getPassword());
                } else {
                    eyeToggle.setForeground(TEXT_MUTED);
                    eyeToggle.setIcon(makeEyeIcon(TEXT_MUTED));
                    eyeToggle.setToolTipText("Show password");
                    eyeToggle.getAccessibleContext().setAccessibleName("Show password");
                    passwordField.setText(maskPassword(currentTech.getPassword()));
                }
            });
            passwordFieldPanel.add(eyeToggle, BorderLayout.EAST);
            passwordRow.add(passwordLbl, BorderLayout.WEST);
            passwordRow.add(passwordFieldPanel, BorderLayout.CENTER);
            profileCard.add(passwordRow);
            profileCard.add(Box.createVerticalStrut(16));

            errorMsg = new JLabel();
            errorMsg.setForeground(DANGER);
            errorMsg.setFont(new Font(F, Font.PLAIN, 12));
            errorMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
            profileCard.add(errorMsg);
            profileCard.add(Box.createVerticalStrut(12));

            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            buttonRow.setOpaque(false);
            JButton doneBtn = makePrimaryButton("\u2713  Done");
            doneBtn.addActionListener(e -> onProfileSave());
            JButton cancelBtn = makeSecondaryButton("\u2715  Cancel");
            cancelBtn.addActionListener(e -> exitProfileEditMode());
            buttonRow.add(doneBtn);
            buttonRow.add(cancelBtn);
            profileCard.add(buttonRow);
        }
        profileCard.revalidate();
        profileCard.repaint();
    }

    private JTextField makeEditableTextField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font(F, Font.PLAIN, 13));
        field.setBackground(BG_CARD2);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        field.setMaximumSize(new Dimension(200, 28));
        field.setPreferredSize(new Dimension(200, 28));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createLineBorder(ACCENT, 2));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            }
        });
        return field;
    }

    private Icon makeEyeIcon(Color color) {
        return new Icon() {
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 12; }

            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.8f));
                g2.draw(new Arc2D.Double(x + 1, y + 1, 16, 10, 0, 180, Arc2D.OPEN));
                g2.draw(new Arc2D.Double(x + 1, y + 1, 16, 10, 180, 180, Arc2D.OPEN));
                g2.fillOval(x + 7, y + 4, 4, 4);
                g2.dispose();
            }
        };
    }

    private void enterProfileEditMode() {
        profileEditMode = true;
        passwordVisible = false;
        if (errorMsg != null) errorMsg.setText("");
        refreshProfileUI();
    }

    private void exitProfileEditMode() {
        profileEditMode = false;
        passwordVisible = false;
        if (errorMsg != null) errorMsg.setText("");
        refreshProfileUI();
    }

    private String maskPassword(String password) {
        return "*".repeat(password.length());
    }

    /**
     * Validates and saves edits to the technician's own profile.
     * Validations match the group standards from the WhatsApp chat (Jimmy + Basil):
     *   - All fields required
     *   - Names: alphabets only
     *   - Phone: numeric only, exactly 10 digits
     *   - Email: must be in format a@b.c
     *   - Username NOT editable (locked per Suhayb's chat decision)
     */
    private void onProfileSave() {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String email     = emailField.getText().trim();
        String phone     = phoneField.getText().trim();
        String password  = passwordVisible ? passwordField.getText().trim() : currentTech.getPassword();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorMsg.setText("\u274C All fields are required.");
            profileCard.revalidate(); profileCard.repaint();
            return;
        }
        // Names: alphabets only
        if (!firstName.matches("[A-Za-z]+") || !lastName.matches("[A-Za-z]+")) {
            errorMsg.setText("\u274C First and last name must contain only alphabets.");
            profileCard.revalidate(); profileCard.repaint();
            return;
        }
        // Phone: exactly 10 numeric digits
        if (!phone.matches("\\d{10}")) {
            errorMsg.setText("\u274C Phone must be exactly 10 digits (numbers only).");
            profileCard.revalidate(); profileCard.repaint();
            return;
        }
        // Email: must contain @ and a valid domain
        if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            errorMsg.setText("\u274C Email format is invalid (e.g. name@example.com).");
            profileCard.revalidate(); profileCard.repaint();
            return;
        }
        if (passwordVisible && password.isEmpty()) {
            errorMsg.setText("\u274C Password cannot be empty.");
            profileCard.revalidate(); profileCard.repaint();
            return;
        }

        List<User> users = FileHandler.loadAllUsers();
        User userToUpdate = null;
        for (User u : users) {
            if (u.getUserID().equals(currentTech.getUserID())) { userToUpdate = u; break; }
        }
        if (userToUpdate == null) {
            errorMsg.setText("\u274C Error: User not found in database.");
            profileCard.revalidate(); profileCard.repaint();
            return;
        }
        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);
        userToUpdate.setEmail(email);
        userToUpdate.setPhone(phone);
        if (passwordVisible) userToUpdate.setPassword(password);
        FileHandler.saveAllUsers(users);

        currentTech.setFirstName(firstName);
        currentTech.setLastName(lastName);
        currentTech.setEmail(email);
        currentTech.setPhone(phone);
        if (passwordVisible) currentTech.setPassword(password);

        profileEditMode = false;
        passwordVisible = false;
        errorMsg.setText("");
        refreshProfileUI();
        updateTopBarLabel();
        JOptionPane.showMessageDialog(this, "Profile updated successfully!",
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateTopBarLabel() {
        if (topBarUserLabel != null) {
            topBarUserLabel.setText("\uD83D\uDD27  " + currentTech.getFullName() + "  ·  Technician");
        }
    }

    //  PANEL 2 — MY APPOINTMENTS
    private JPanel buildAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel leftPane = new JPanel(new BorderLayout(0, 12));
        leftPane.setOpaque(false);
        leftPane.setPreferredSize(new Dimension(450, 0));

        JLabel heading = new JLabel("My Appointments");
        heading.setFont(new Font(F, Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] cols = {"ID", "Date", "Time", "Service", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        String myTechnicianID = FileHandler.getTechnicianIDByUserID(currentTech.getUserID());
        List<Appointment> myAppointments = FileHandler.loadAllAppointments()
            .stream()
            .filter(a -> {
                String stored = a.getTechnicianID();
                return stored.equals(currentTech.getUserID())
                        || (myTechnicianID != null && stored.equals(myTechnicianID));
            })
            .collect(java.util.stream.Collectors.toList());
        myAppointments.forEach(a -> model.addRow(new Object[]{
            a.getAppointmentID(), a.getDate(), a.getTime(),
            a.getServiceType(), a.getStatus()
        }));

        JTable table = makeStyledTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JTextField searchField = new JTextField();
        searchField.setFont(new Font(F, Font.PLAIN, 13));
        searchField.setBackground(BG_CARD2);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        searchField.setToolTipText("Search by ID, date, service or status");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void doFilter() {
                String q = searchField.getText().trim();
                if (q.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { doFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { doFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doFilter(); }
        });
        JLabel searchLbl = new JLabel("\uD83D\uDD0D  Search appointments");
        searchLbl.setFont(new Font(F, Font.PLAIN, 11));
        searchLbl.setForeground(TEXT_MUTED);
        searchLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchLbl.setBorder(new EmptyBorder(0, 2, 6, 0));
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JScrollPane scroll = makeScrollPane(table);

        JPanel listTop = new JPanel();
        listTop.setLayout(new BoxLayout(listTop, BoxLayout.Y_AXIS));
        listTop.setOpaque(false);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        listTop.add(heading);
        listTop.add(Box.createVerticalStrut(14));
        listTop.add(searchLbl);
        listTop.add(searchField);

        leftPane.add(listTop, BorderLayout.NORTH);
        leftPane.add(scroll,  BorderLayout.CENTER);

        JPanel rightPane = new JPanel();
        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
        rightPane.setBackground(BG_CARD);
        rightPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(24, 24, 24, 24)));

        JLabel detailHeading = new JLabel("Appointment Details");
        detailHeading.setFont(new Font(F, Font.BOLD, 16));
        detailHeading.setForeground(TEXT_PRIMARY);
        detailHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Details — ALL TEXT WHITE per group request (TEXT_PRIMARY = #F0F1FF)
        JLabel detailContent = new JLabel(
            "<html><div style='color:#F0F1FF;'>Select an appointment from the<br>"
            + "list to view its details.</div></html>");
        detailContent.setFont(new Font(F, Font.PLAIN, 13));
        detailContent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea commentArea = new JTextArea();
        commentArea.setEditable(false);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setFont(new Font(F, Font.PLAIN, 12));
        commentArea.setForeground(TEXT_PRIMARY);
        commentArea.setBackground(BG_CARD2);
        commentArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        commentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        commentScroll.setPreferredSize(new Dimension(0, 100));
        commentScroll.setVisible(false);
        JLabel commentTitle = new JLabel("\uD83D\uDCAC  Customer Comment");
        commentTitle.setFont(new Font(F, Font.BOLD, 12));
        commentTitle.setForeground(TEXT_PRIMARY);
        commentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentTitle.setVisible(false);

        JButton completeBtn = makePrimaryButton("\u2705  Mark as Completed");
        completeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        completeBtn.setEnabled(false);
        JButton feedbackBtn = makeSecondaryButton("\uD83D\uDCDD  Write Feedback");
        feedbackBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        feedbackBtn.setEnabled(false);
        JButton viewFeedbackBtn = makeSecondaryButton("\uD83D\uDC41  View/Edit Feedback");
        viewFeedbackBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewFeedbackBtn.setEnabled(false);
        viewFeedbackBtn.setVisible(false);

        rightPane.add(detailHeading);
        rightPane.add(Box.createVerticalStrut(16));
        rightPane.add(detailContent);
        rightPane.add(Box.createVerticalStrut(18));
        rightPane.add(commentTitle);
        rightPane.add(Box.createVerticalStrut(6));
        rightPane.add(commentScroll);
        rightPane.add(Box.createVerticalStrut(22));
        rightPane.add(completeBtn);
        rightPane.add(Box.createVerticalStrut(10));
        rightPane.add(feedbackBtn);
        rightPane.add(Box.createVerticalStrut(10));
        rightPane.add(viewFeedbackBtn);
        rightPane.add(Box.createVerticalGlue());

        Map<String, String> commentsMap = loadComments();
        Map<String, String> namesMap    = loadCustomerNames();

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            if (viewRow == -1) return;
            int row = table.convertRowIndexToModel(viewRow);
            String apptID  = (String) model.getValueAt(row, 0);
            String status  = (String) model.getValueAt(row, 4);
            String custName = "\u2014";
            for (Appointment a : myAppointments) {
                if (a.getAppointmentID().equals(apptID)) {
                    custName = namesMap.getOrDefault(a.getCustomerID(), a.getCustomerID());
                    break;
                }
            }
            // Details — ALL TEXT WHITE (TEXT_PRIMARY = #F0F1FF) per group request
            detailContent.setText(
                "<html><div style='line-height:1.8; color:#F0F1FF;'>"
                + "<b>Appointment ID:</b>  " + apptID + "<br>"
                + "<b>Customer:</b>  " + custName + "<br>"
                + "<b>Date:</b>  " + model.getValueAt(row, 1) + "<br>"
                + "<b>Time:</b>  " + model.getValueAt(row, 2) + "<br>"
                + "<b>Service:</b>  " + model.getValueAt(row, 3) + "<br>"
                + "<b>Status:</b>  " + status + "</div></html>");
            String comment = commentsMap.get(apptID);
            if (comment != null && !comment.isEmpty()) {
                commentArea.setText(comment);
                commentTitle.setVisible(true);
                commentScroll.setVisible(true);
            } else {
                commentArea.setText("");
                commentTitle.setVisible(false);
                commentScroll.setVisible(false);
            }

            // Treat both "Pending" (legacy data) and "Ongoing" (new) as "ongoing" work
            boolean isOngoing = "Ongoing".equals(status) || "Pending".equals(status);
            if (isOngoing) {
                completeBtn.setText("\u2705  Mark as Completed");
                completeBtn.setEnabled(true);
                feedbackBtn.setVisible(true);
                feedbackBtn.setEnabled(false);
                viewFeedbackBtn.setVisible(false);
                viewFeedbackBtn.setEnabled(false);
            } else { // Completed
                completeBtn.setText("\u21A9  Revert to Ongoing");
                completeBtn.setEnabled(true);
                // Check if feedback already exists for this appointment
                boolean hasFeedback = feedbackExistsFor(apptID);
                feedbackBtn.setVisible(!hasFeedback);
                feedbackBtn.setEnabled(!hasFeedback);
                viewFeedbackBtn.setVisible(hasFeedback);
                viewFeedbackBtn.setEnabled(hasFeedback);
            }
            rightPane.revalidate();
            rightPane.repaint();
        });

        completeBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow == -1) return;
            int row = table.convertRowIndexToModel(viewRow);
            String apptID = (String) model.getValueAt(row, 0);
            String currentStatus = (String) model.getValueAt(row, 4);
            // Both "Pending" (legacy) and "Ongoing" (new) mean the same thing
            boolean isCurrentlyOngoing = "Ongoing".equals(currentStatus) || "Pending".equals(currentStatus);
            String newStatus  = isCurrentlyOngoing ? "Completed" : "Ongoing";
            String actionWord = isCurrentlyOngoing ? "mark as Completed" : "revert to Ongoing";
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
                boolean hasFeedback = feedbackExistsFor(apptID);
                if ("Completed".equals(newStatus)) {
                    completeBtn.setText("\u21A9  Revert to Ongoing");
                    feedbackBtn.setVisible(!hasFeedback);
                    feedbackBtn.setEnabled(!hasFeedback);
                    viewFeedbackBtn.setVisible(hasFeedback);
                    viewFeedbackBtn.setEnabled(hasFeedback);
                } else {
                    completeBtn.setText("\u2705  Mark as Completed");
                    feedbackBtn.setVisible(true);
                    feedbackBtn.setEnabled(false);
                    viewFeedbackBtn.setVisible(false);
                    viewFeedbackBtn.setEnabled(false);
                }
                rightPane.revalidate(); rightPane.repaint();
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
            int viewRow = table.getSelectedRow();
            if (viewRow == -1) return;
            int row = table.convertRowIndexToModel(viewRow);
            String apptID = (String) model.getValueAt(row, 0);
            openFeedbackDialog(apptID, false);
            // Refresh button states based on whether feedback now exists
            boolean hasFeedback = feedbackExistsFor(apptID);
            feedbackBtn.setVisible(!hasFeedback);
            feedbackBtn.setEnabled(!hasFeedback);
            viewFeedbackBtn.setVisible(hasFeedback);
            viewFeedbackBtn.setEnabled(hasFeedback);
            rightPane.revalidate(); rightPane.repaint();
        });

        viewFeedbackBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow == -1) return;
            int row = table.convertRowIndexToModel(viewRow);
            String apptID = (String) model.getValueAt(row, 0);
            openFeedbackDialog(apptID, true);
        });

        panel.add(leftPane,  BorderLayout.WEST);
        panel.add(rightPane, BorderLayout.CENTER);
        return panel;
    }

    /** Returns true if there is a feedback row for the given appointment. */
    private boolean feedbackExistsFor(String apptID) {
        return FileHandler.loadAllFeedbacks().stream()
            .anyMatch(f -> f.getAppointmentID().equals(apptID));
    }

    /**
     * Opens a feedback dialog.
     * If editMode==true, pre-fills the existing feedback text so the
     * technician can update it.
     * Saves to feedbacks.txt using the technician's T-ID (T001, T002...) — NOT userID.
     */
    private void openFeedbackDialog(String apptID, boolean editMode) {
        // Look up existing feedback if editing
        List<Feedback> feedbacks = FileHandler.loadAllFeedbacks();
        Feedback existing = null;
        for (Feedback f : feedbacks) {
            if (f.getAppointmentID().equals(apptID)) { existing = f; break; }
        }

        JTextArea textArea = new JTextArea(6, 32);
        textArea.setFont(new Font(F, Font.PLAIN, 13));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        if (editMode && existing != null) {
            textArea.setText(existing.getFeedbackText());
        }
        JScrollPane sp = new JScrollPane(textArea);

        String dialogTitle = editMode ? "View / Edit Feedback" : "Write Feedback";
        String prompt = (editMode ? "Edit feedback for appointment " : "Enter feedback for appointment ")
                        + apptID + ":";

        int result = JOptionPane.showConfirmDialog(this,
            new Object[]{prompt, sp}, dialogTitle,
            JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String text = textArea.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Feedback cannot be empty.");
                return;
            }

            // Look up THIS technician's T-ID (using current user's userID)
            String techID = FileHandler.getTechnicianIDByUserID(currentTech.getUserID());
            if (techID == null) {
                JOptionPane.showMessageDialog(this,
                    "Could not find your technician record. Please contact admin.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (existing != null) {
                // Update existing feedback in place
                existing.setFeedbackText(text);
            } else {
                // Add new feedback — using T-ID (not userID) per group requirement
                feedbacks.add(new Feedback(apptID, techID, text));
            }
            FileHandler.saveAllFeedbacks(feedbacks);
            JOptionPane.showMessageDialog(this,
                editMode ? "Feedback updated successfully." : "Feedback saved successfully.");
        }
    }

    //  PANEL 3 — CUSTOMER COMMENTS (Group-requested tab)
    //  Shows all customer comments for appointments assigned to this technician.
    private JPanel buildCommentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);
        JLabel heading = new JLabel("Customer Comments");
        heading.setFont(new Font(F, Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("All customer comments on appointments assigned to you");
        sub.setFont(new Font(F, Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 0, 0));
        head.add(heading);
        head.add(sub);

        String myTechnicianID = FileHandler.getTechnicianIDByUserID(currentTech.getUserID());
        List<Appointment> myAppts = FileHandler.loadAllAppointments().stream()
            .filter(a -> {
                String stored = a.getTechnicianID();
                return stored.equals(currentTech.getUserID())
                        || (myTechnicianID != null && stored.equals(myTechnicianID));
            })
            .collect(java.util.stream.Collectors.toList());
        Map<String, String> comments      = loadComments();
        Map<String, String> customerNames = loadCustomerNames();

        JPanel listBody = new JPanel();
        listBody.setLayout(new BoxLayout(listBody, BoxLayout.Y_AXIS));
        listBody.setOpaque(false);

        int shown = 0;
        List<Appointment> sorted = myAppts.stream()
            .sorted((a, b) -> safeCompareDate(b.getDate(), a.getDate()))
            .collect(java.util.stream.Collectors.toList());
        for (Appointment a : sorted) {
            String c = comments.get(a.getAppointmentID());
            if (c == null || c.isEmpty()) continue;
            String cn = customerNames.getOrDefault(a.getCustomerID(), a.getCustomerID());
            listBody.add(makeCommentCard(a, cn, c));
            listBody.add(Box.createVerticalStrut(12));
            shown++;
        }

        if (shown == 0) {
            JLabel empty = new JLabel("No customer comments on your appointments yet.");
            empty.setFont(new Font(F, Font.PLAIN, 13));
            empty.setForeground(TEXT_MUTED);
            empty.setBorder(new EmptyBorder(40, 0, 0, 0));
            listBody.add(empty);
        }

        JScrollPane scroll = new JScrollPane(listBody);
        scroll.setBorder(null);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(head,   BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeCommentCard(Appointment a, String customerName, String comment) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(16, 18, 16, 18)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel header = new JLabel(a.getAppointmentID() + "   ·   " + customerName);
        header.setFont(new Font(F, Font.BOLD, 13));
        header.setForeground(TEXT_PRIMARY);
        JLabel dateLbl = new JLabel(a.getDate() + "  ·  " + a.getServiceType());
        dateLbl.setFont(new Font(F, Font.PLAIN, 11));
        dateLbl.setForeground(TEXT_MUTED);
        top.add(header,  BorderLayout.WEST);
        top.add(dateLbl, BorderLayout.EAST);

        JTextArea body = new JTextArea(comment);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(new Font(F, Font.PLAIN, 13));
        body.setForeground(TEXT_PRIMARY);
        body.setBackground(BG_CARD);
        body.setBorder(new EmptyBorder(8, 0, 0, 0));

        card.add(top,  BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    //  SHARED HELPERS
    private JPanel makeInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font(F, Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(100, 20));
        JLabel val = new JLabel(value);
        val.setFont(new Font(F, Font.BOLD, 13));
        val.setForeground(TEXT_PRIMARY);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    private JButton makePrimaryButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font(F, Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeSecondaryButton(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setColor(BG_CARD2);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font(F, Font.PLAIN, 13));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(ACCENT); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(TEXT_PRIMARY); }
        });
        return btn;
    }

    // NAV BUTTON — Manager-style highlight (glitch-free on Windows)
    private JButton makeNavButton(String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(new Font(F, Font.PLAIN, 16));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("cardName", cardName);
        navButtons.add(btn);
        updateNavButtonStyle(btn);
        btn.addActionListener(e -> {
            if (profileEditMode && !"PROFILE".equals(cardName)) {
                int result = JOptionPane.showConfirmDialog(this,
                        "You have unsaved changes. Discard them?",
                        "Unsaved Changes",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) return;
                exitProfileEditMode();
            }
            if ("DASHBOARD".equals(cardName)) refreshDashboard();
            else if ("COMMENTS".equals(cardName)) {
                refreshCommentsPanel();
                contentLayout.show(contentPanel, cardName);
                activeCardName = cardName;
                updateNavButtonStyles();
            }
            else {
                contentLayout.show(contentPanel, cardName);
                activeCardName = cardName;
                updateNavButtonStyles();
            }
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setForeground(TEXT_PRIMARY);
                if (!activeCardName.equals(btn.getClientProperty("cardName"))) {
                    btn.setOpaque(true);
                    btn.setContentAreaFilled(true);
                    btn.setBackground(BG_CARD2);
                }
            }
            @Override public void mouseExited(MouseEvent e)  { updateNavButtonStyle(btn); }
        });
        return btn;
    }

    private void updateNavButtonStyles() {
        for (JButton button : navButtons) {
            updateNavButtonStyle(button);
        }
    }

    private void updateNavButtonStyle(JButton button) {
        boolean active = activeCardName.equals(button.getClientProperty("cardName"));
        button.setForeground(active ? TEXT_PRIMARY : TEXT_MUTED);
        button.setOpaque(active);
        button.setContentAreaFilled(active);
        button.setBackground(active ? BG_CARD2 : new Color(0, 0, 0, 0));
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
    }

    private JTable makeStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font(F, Font.PLAIN, 13));
        table.setRowHeight(42);
        table.setGridColor(BORDER_COLOR);
        // Stronger selection (was alpha 60 = too dim)
        table.setSelectionBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 110));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        // Custom renderer: status column gets bright bold color
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 8));
                if (col == 4) { // Status column
                    setFont(new Font(F, Font.BOLD, 12));
                    String s = String.valueOf(v);
                    if ("Completed".equals(s)) {
                        setForeground(sel ? TEXT_PRIMARY : new Color(74, 222, 128)); // bright green
                    } else if ("Ongoing".equals(s) || "Pending".equals(s)) {
                        setForeground(sel ? TEXT_PRIMARY : new Color(255, 184, 28)); // bright amber
                    } else {
                        setForeground(TEXT_PRIMARY);
                    }
                } else {
                    setFont(new Font(F, Font.PLAIN, 13));
                    setForeground(sel ? TEXT_PRIMARY : TEXT_PRIMARY);
                }
                if (!sel) setBackground(BG_CARD);
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_CARD2);
        header.setForeground(TEXT_PRIMARY);
        header.setFont(new Font(F, Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);
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
