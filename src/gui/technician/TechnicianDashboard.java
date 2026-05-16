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
 * MEMBER 4 features:
 *   [1] Edit own profile (first/last name, username, email, phone, password)
 *   [2] View appointments assigned to THIS technician
 *   [3] Click an appointment to see full details + customer comments
 *   [4] Mark appointment Completed (and revert back to Pending)
 *   [5] Write feedback for a completed appointment
 * EXTRA:
 *   [6] Dashboard home screen with work statistics
 *   [7] Live search / filter on appointments
 * NOTE: Edit Profile + password show/hide logic copied from CounterStaffDashboard.java.
 */
public class TechnicianDashboard extends JFrame {

    private static final Color BG_DARK      = new Color(13,  15,  23);
    private static final Color BG_CARD      = new Color(22,  25,  37);
    private static final Color BG_CARD2     = new Color(30,  34,  50);
    private static final Color BG_HOVER     = new Color(38,  43,  62);
    private static final Color ACCENT       = new Color(245, 158, 11);
    private static final Color ACCENT_SOFT  = new Color(245, 158, 11, 38);
    private static final Color TEXT_PRIMARY = new Color(241, 243, 255);
    private static final Color TEXT_MUTED   = new Color(143, 148, 178);
    private static final Color TEXT_FAINT   = new Color(99, 104, 132);
    private static final Color BORDER_COLOR = new Color(44,  48,  68);
    private static final Color DANGER       = new Color(244, 86,  86);
    private static final Color SUCCESS      = new Color(46, 204, 113);
    private static final Color INFO         = new Color(70, 142, 250);
    private static final String F = "SansSerif";

    private Technician currentTech;
    private boolean    profileEditMode = false;
    private boolean    passwordVisible = false;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField passwordField;
    private JLabel     errorMsg;
    private JPanel     profileCard;

    private CardLayout contentLayout;
    private JPanel     contentPanel;
    private JPanel     dashboardPanel;
    private JLabel     topBarUserLabel;
    private JButton    activeNavButton;

    public TechnicianDashboard(Technician tech) {
        this.currentTech = tech;
        setTitle("APU-ASC — Technician Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(980, 640));
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

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(16, 28, 16, 28)));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);
        JLabel logoDot = new JLabel("\u25C8");
        logoDot.setFont(new Font(F, Font.BOLD, 20));
        logoDot.setForeground(ACCENT);
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("APU Automotive Service Centre");
        title.setFont(new Font(F, Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);
        JLabel tagline = new JLabel("Technician Workspace");
        tagline.setFont(new Font(F, Font.PLAIN, 11));
        tagline.setForeground(TEXT_FAINT);
        titleBlock.add(title);
        titleBlock.add(tagline);
        brand.add(logoDot);
        brand.add(titleBlock);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        rightSide.setOpaque(false);
        topBarUserLabel = new JLabel("\uD83D\uDD27  " + currentTech.getFullName() + "  ·  Technician");
        topBarUserLabel.setFont(new Font(F, Font.PLAIN, 13));
        topBarUserLabel.setForeground(TEXT_MUTED);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font(F, Font.BOLD, 12));
        logoutBtn.setForeground(DANGER);
        logoutBtn.setBackground(BG_CARD);
        logoutBtn.setOpaque(true);
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(DANGER.getRed(), DANGER.getGreen(), DANGER.getBlue(), 90), 1, true),
            new EmptyBorder(7, 16, 7, 16)));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(new Color(DANGER.getRed(), DANGER.getGreen(), DANGER.getBlue(), 28));
            }
            @Override public void mouseExited(MouseEvent e) { logoutBtn.setBackground(BG_CARD); }
        });
        logoutBtn.addActionListener(e -> {
            Session.clearSession();
            dispose();
            new main.LoginFrame().setVisible(true);
        });

        rightSide.add(topBarUserLabel);
        rightSide.add(logoutBtn);
        bar.add(brand,     BorderLayout.WEST);
        bar.add(rightSide, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_CARD);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
            new EmptyBorder(28, 14, 28, 14)));
        sidebar.setPreferredSize(new Dimension(232, 0));

        JLabel section = new JLabel("MENU");
        section.setFont(new Font(F, Font.BOLD, 10));
        section.setForeground(TEXT_FAINT);
        section.setBorder(new EmptyBorder(0, 8, 0, 0));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(14));

        sidebar.add(makeNavButton("\uD83D\uDCCA   Dashboard",       "DASHBOARD"));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(makeNavButton("\uD83D\uDC64   My Profile",      "PROFILE"));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(makeNavButton("\uD83D\uDCC5   My Appointments", "APPOINTMENTS"));
        sidebar.add(Box.createVerticalGlue());

        JLabel sig = new JLabel("APU-ASC v2.0");
        sig.setFont(new Font(F, Font.PLAIN, 10));
        sig.setForeground(TEXT_FAINT);
        sig.setBorder(new EmptyBorder(0, 8, 0, 0));
        sig.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sig);
        return sidebar;
    }

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

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(32, 36, 32, 36));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JPanel headingBlock = new JPanel();
        headingBlock.setLayout(new BoxLayout(headingBlock, BoxLayout.Y_AXIS));
        headingBlock.setOpaque(false);
        JLabel heading = new JLabel("Welcome back, " + currentTech.getFirstName());
        heading.setFont(new Font(F, Font.BOLD, 25));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Here's an overview of your work at APU-ASC");
        subtitle.setFont(new Font(F, Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(6, 0, 0, 0));
        headingBlock.add(heading);
        headingBlock.add(subtitle);
        JButton refreshBtn = makeSecondaryButton("\u27F3  Refresh");
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
        int todayCount = (int) myAppointments.stream().filter(a -> todayStr.equals(a.getDate())).count();
        int normalCount = (int) myAppointments.stream().filter(a -> "Normal".equals(a.getServiceType())).count();
        int majorCount  = (int) myAppointments.stream().filter(a -> "Major".equals(a.getServiceType())).count();
        int completionRate = total == 0 ? 0 : (completed * 100) / total;
        long feedbackCount = FileHandler.loadAllFeedbacks().stream()
            .filter(f -> f.getTechnicianID().equals(currentTech.getUserID())).count();

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 18, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        statsRow.add(makeStatCard("TOTAL APPOINTMENTS", String.valueOf(total),     "Assigned to you",   ACCENT));
        statsRow.add(makeStatCard("PENDING",            String.valueOf(pending),   "Awaiting service",  INFO));
        statsRow.add(makeStatCard("COMPLETED",          String.valueOf(completed), "Jobs finished",     SUCCESS));
        statsRow.add(makeStatCard("TODAY",              String.valueOf(todayCount),"On " + todayStr,    DANGER));

        JPanel middleRow = new JPanel(new GridLayout(1, 2, 18, 0));
        middleRow.setOpaque(false);
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        middleRow.add(makeServiceBreakdownCard(normalCount, majorCount));
        middleRow.add(makeCompletionRateCard(completionRate, completed, total, (int) feedbackCount));

        JPanel recentCard = makeRecentActivityCard(myAppointments);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(statsRow);
        body.add(Box.createVerticalStrut(18));
        body.add(middleRow);
        body.add(Box.createVerticalStrut(18));
        body.add(recentCard);
        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scroll);
        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(scroll,    BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeStatCard(String label, String value, String subtitle, Color accent) {
        RoundedPanel card = new RoundedPanel(14, BG_CARD);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font(F, Font.BOLD, 11));
        lblLabel.setForeground(TEXT_FAINT);
        JPanel dot = new JPanel();
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setBorder(BorderFactory.createLineBorder(accent, 5, true));
        top.add(lblLabel, BorderLayout.WEST);
        top.add(dot,      BorderLayout.EAST);
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font(F, Font.BOLD, 36));
        lblValue.setForeground(TEXT_PRIMARY);
        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font(F, Font.PLAIN, 11));
        lblSubtitle.setForeground(TEXT_MUTED);
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(lblValue);
        center.add(Box.createVerticalStrut(3));
        center.add(lblSubtitle);
        card.add(top,    BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(0, 3));
        card.add(accentBar, BorderLayout.SOUTH);
        return card;
    }

    private JPanel makeServiceBreakdownCard(int normalCount, int majorCount) {
        RoundedPanel card = new RoundedPanel(14, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));
        JLabel title = new JLabel("Service Type Breakdown");
        title.setFont(new Font(F, Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        int total = normalCount + majorCount;
        card.add(title);
        card.add(Box.createVerticalStrut(20));
        card.add(makeBarRow("Normal Service", normalCount, total, INFO));
        card.add(Box.createVerticalStrut(16));
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
        JLabel countLbl = new JLabel(count + "  ·  " + pct + "%");
        countLbl.setFont(new Font(F, Font.BOLD, 12));
        countLbl.setForeground(TEXT_PRIMARY);
        countLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        labelLine.add(lbl,      BorderLayout.WEST);
        labelLine.add(countLbl, BorderLayout.EAST);
        final int fcount = count, ftotal = total;
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(BG_CARD2);
                g2.fillRoundRect(0, 0, w, h, h, h);
                int fillW = ftotal == 0 ? 0 : (int) ((long) w * fcount / ftotal);
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, fillW, h, h, h);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 9));
        bar.setPreferredSize(new Dimension(0, 9));
        row.add(labelLine);
        row.add(Box.createVerticalStrut(8));
        row.add(bar);
        return row;
    }

    private JPanel makeCompletionRateCard(int rate, int completed, int total, int feedbackCount) {
        RoundedPanel card = new RoundedPanel(14, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));
        JLabel title = new JLabel("Performance");
        title.setFont(new Font(F, Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel rateLbl = new JLabel(rate + "%");
        rateLbl.setFont(new Font(F, Font.BOLD, 42));
        rateLbl.setForeground(rate >= 75 ? SUCCESS : (rate >= 40 ? ACCENT : DANGER));
        rateLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel rateSubtitle = new JLabel("Completion rate · " + completed + " of " + total + " jobs");
        rateSubtitle.setFont(new Font(F, Font.PLAIN, 12));
        rateSubtitle.setForeground(TEXT_MUTED);
        rateSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        JLabel feedbackLbl = new JLabel("\uD83D\uDCDD   " + feedbackCount
            + " feedback report" + (feedbackCount == 1 ? "" : "s") + " submitted");
        feedbackLbl.setFont(new Font(F, Font.PLAIN, 12));
        feedbackLbl.setForeground(TEXT_MUTED);
        feedbackLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(14));
        card.add(rateLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(rateSubtitle);
        card.add(Box.createVerticalStrut(18));
        card.add(sep);
        card.add(Box.createVerticalStrut(18));
        card.add(feedbackLbl);
        return card;
    }

    private JPanel makeRecentActivityCard(List<Appointment> myAppointments) {
        RoundedPanel card = new RoundedPanel(14, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));
        JLabel title = new JLabel("Recent Activity");
        title.setFont(new Font(F, Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(18));
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
            String customerName = customerNames.getOrDefault(a.getCustomerID(), a.getCustomerID());
            card.add(makeActivityRow(a, customerName));
            if (i < recent.size() - 1) card.add(Box.createVerticalStrut(10));
        }
        return card;
    }

    private JPanel makeActivityRow(Appointment a, String customerName) {
        RoundedPanel row = new RoundedPanel(10, BG_CARD2);
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        JLabel mainLbl = new JLabel(a.getAppointmentID() + "   ·   " + customerName);
        mainLbl.setFont(new Font(F, Font.BOLD, 13));
        mainLbl.setForeground(TEXT_PRIMARY);
        JLabel subLbl = new JLabel(a.getDate() + "  " + a.getTime() + "  ·  " + a.getServiceType());
        subLbl.setFont(new Font(F, Font.PLAIN, 11));
        subLbl.setForeground(TEXT_MUTED);
        left.add(mainLbl);
        left.add(Box.createVerticalStrut(3));
        left.add(subLbl);
        row.add(left, BorderLayout.WEST);
        row.add(makeStatusPill(a.getStatus()), BorderLayout.EAST);
        return row;
    }

    private JComponent makeStatusPill(String status) {
        boolean done = "Completed".equals(status);
        Color c = done ? SUCCESS : ACCENT;
        JLabel pill = new JLabel(status);
        pill.setFont(new Font(F, Font.BOLD, 11));
        pill.setForeground(c);
        pill.setOpaque(false);
        pill.setHorizontalAlignment(SwingConstants.CENTER);
        pill.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(c.getRed(), c.getGreen(), c.getBlue(), 110), 1, true),
            new EmptyBorder(5, 14, 5, 14)));
        return pill;
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
    }

    // PANEL 1 — MY PROFILE (Edit Profile + show/hide copied from CounterStaffDashboard.java)
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(32, 36, 32, 36));
        JPanel headBlock = new JPanel();
        headBlock.setLayout(new BoxLayout(headBlock, BoxLayout.Y_AXIS));
        headBlock.setOpaque(false);
        JLabel heading = new JLabel("My Profile");
        heading.setFont(new Font(F, Font.BOLD, 25));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Manage your account information");
        sub.setFont(new Font(F, Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(6, 0, 0, 0));
        headBlock.add(heading);
        headBlock.add(sub);

        profileCard = new RoundedPanel(16, BG_CARD);
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(new EmptyBorder(32, 34, 32, 34));
        profileCard.setMaximumSize(new Dimension(560, Integer.MAX_VALUE));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshProfileUI();

        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.add(profileCard);
        wrap.add(Box.createVerticalGlue());
        panel.add(headBlock, BorderLayout.NORTH);
        panel.add(wrap, BorderLayout.CENTER);
        return panel;
    }

    private void refreshProfileUI() {
        profileCard.removeAll();
        if (!profileEditMode) {
            JLabel avatar = new JLabel(initials(currentTech.getFullName()), SwingConstants.CENTER);
            avatar.setFont(new Font(F, Font.BOLD, 22));
            avatar.setForeground(BG_DARK);
            RoundedPanel avatarWrap = new RoundedPanel(32, ACCENT);
            avatarWrap.setLayout(new BorderLayout());
            avatarWrap.setMaximumSize(new Dimension(64, 64));
            avatarWrap.setPreferredSize(new Dimension(64, 64));
            avatarWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
            avatarWrap.add(avatar, BorderLayout.CENTER);
            profileCard.add(avatarWrap);
            profileCard.add(Box.createVerticalStrut(12));

            JLabel nameLbl = new JLabel(currentTech.getFullName());
            nameLbl.setFont(new Font(F, Font.BOLD, 19));
            nameLbl.setForeground(TEXT_PRIMARY);
            nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel roleLbl = new JLabel("Technician  ·  " + currentTech.getUsername());
            roleLbl.setFont(new Font(F, Font.PLAIN, 12));
            roleLbl.setForeground(TEXT_MUTED);
            roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            roleLbl.setBorder(new EmptyBorder(4, 0, 0, 0));
            profileCard.add(nameLbl);
            profileCard.add(roleLbl);
            profileCard.add(Box.createVerticalStrut(24));

            JSeparator sep = new JSeparator();
            sep.setForeground(BORDER_COLOR);
            sep.setBackground(BORDER_COLOR);
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            profileCard.add(sep);
            profileCard.add(Box.createVerticalStrut(24));

            profileCard.add(makeInfoRow("Full Name", currentTech.getFullName()));
            profileCard.add(Box.createVerticalStrut(14));
            profileCard.add(makeInfoRow("Username", currentTech.getUsername()));
            profileCard.add(Box.createVerticalStrut(14));
            profileCard.add(makeInfoRow("Email", currentTech.getEmail()));
            profileCard.add(Box.createVerticalStrut(14));
            profileCard.add(makeInfoRow("Phone", currentTech.getPhone()));
            profileCard.add(Box.createVerticalStrut(14));
            profileCard.add(makeInfoRow("Password", maskPassword(currentTech.getPassword())));
            profileCard.add(Box.createVerticalStrut(28));

            JButton editBtn = makePrimaryButton("\u270E   Edit Profile");
            editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            editBtn.addActionListener(e -> enterProfileEditMode());
            profileCard.add(editBtn);
        } else {
            JLabel editTitle = new JLabel("Edit Profile");
            editTitle.setFont(new Font(F, Font.BOLD, 17));
            editTitle.setForeground(TEXT_PRIMARY);
            editTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            profileCard.add(editTitle);
            profileCard.add(Box.createVerticalStrut(22));

            JPanel fullNameRow = new JPanel(new BorderLayout(16, 0));
            fullNameRow.setOpaque(false);
            fullNameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            JLabel fullNameLbl = new JLabel("Full Name");
            fullNameLbl.setFont(new Font(F, Font.PLAIN, 13));
            fullNameLbl.setForeground(TEXT_MUTED);
            fullNameLbl.setPreferredSize(new Dimension(100, 20));
            JPanel nameFieldsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
            nameFieldsPanel.setOpaque(false);
            firstNameField = makeEditableTextField(currentTech.getFirstName());
            lastNameField  = makeEditableTextField(currentTech.getLastName());
            Dimension nfh = new Dimension(0, 34);
            firstNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            lastNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            firstNameField.setPreferredSize(nfh);
            lastNameField.setPreferredSize(nfh);
            nameFieldsPanel.add(firstNameField);
            nameFieldsPanel.add(lastNameField);
            fullNameRow.add(fullNameLbl, BorderLayout.WEST);
            fullNameRow.add(nameFieldsPanel, BorderLayout.CENTER);
            profileCard.add(fullNameRow);
            profileCard.add(Box.createVerticalStrut(14));

            JPanel usernameRow = new JPanel(new BorderLayout(16, 0));
            usernameRow.setOpaque(false);
            usernameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            JLabel usernameLbl = new JLabel("Username");
            usernameLbl.setFont(new Font(F, Font.PLAIN, 13));
            usernameLbl.setForeground(TEXT_MUTED);
            usernameLbl.setPreferredSize(new Dimension(100, 20));
            usernameField = makeEditableTextField(currentTech.getUsername());
            usernameRow.add(usernameLbl, BorderLayout.WEST);
            usernameRow.add(usernameField, BorderLayout.CENTER);
            profileCard.add(usernameRow);
            profileCard.add(Box.createVerticalStrut(14));

            JPanel emailRow = new JPanel(new BorderLayout(16, 0));
            emailRow.setOpaque(false);
            emailRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            JLabel emailLbl = new JLabel("Email");
            emailLbl.setFont(new Font(F, Font.PLAIN, 13));
            emailLbl.setForeground(TEXT_MUTED);
            emailLbl.setPreferredSize(new Dimension(100, 20));
            emailField = makeEditableTextField(currentTech.getEmail());
            emailRow.add(emailLbl, BorderLayout.WEST);
            emailRow.add(emailField, BorderLayout.CENTER);
            profileCard.add(emailRow);
            profileCard.add(Box.createVerticalStrut(14));

            JPanel phoneRow = new JPanel(new BorderLayout(16, 0));
            phoneRow.setOpaque(false);
            phoneRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            JLabel phoneLbl = new JLabel("Phone");
            phoneLbl.setFont(new Font(F, Font.PLAIN, 13));
            phoneLbl.setForeground(TEXT_MUTED);
            phoneLbl.setPreferredSize(new Dimension(100, 20));
            phoneField = makeEditableTextField(currentTech.getPhone());
            phoneRow.add(phoneLbl, BorderLayout.WEST);
            phoneRow.add(phoneField, BorderLayout.CENTER);
            profileCard.add(phoneRow);
            profileCard.add(Box.createVerticalStrut(14));

            JPanel passwordRow = new JPanel(new BorderLayout(16, 0));
            passwordRow.setOpaque(false);
            passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            JLabel passwordLbl = new JLabel("Password");
            passwordLbl.setFont(new Font(F, Font.PLAIN, 13));
            passwordLbl.setForeground(TEXT_MUTED);
            passwordLbl.setPreferredSize(new Dimension(100, 20));
            passwordField = makeEditableTextField(maskPassword(currentTech.getPassword()));
            JPanel passwordFieldPanel = new JPanel(new BorderLayout(8, 0));
            passwordFieldPanel.setOpaque(false);
            passwordFieldPanel.add(passwordField, BorderLayout.CENTER);
            JButton eyeToggle = new JButton("\uD83D\uDC41");
            eyeToggle.setFont(new Font(F, Font.PLAIN, 16));
            eyeToggle.setBackground(BG_CARD2);
            eyeToggle.setOpaque(true);
            eyeToggle.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            eyeToggle.setFocusPainted(false);
            eyeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eyeToggle.setPreferredSize(new Dimension(38, 34));
            eyeToggle.addActionListener(e -> {
                passwordVisible = !passwordVisible;
                if (passwordVisible) {
                    eyeToggle.setText("\uD83D\uDE48");
                    passwordField.setText(currentTech.getPassword());
                } else {
                    eyeToggle.setText("\uD83D\uDC41");
                    passwordField.setText(maskPassword(currentTech.getPassword()));
                }
            });
            passwordFieldPanel.add(eyeToggle, BorderLayout.EAST);
            passwordRow.add(passwordLbl, BorderLayout.WEST);
            passwordRow.add(passwordFieldPanel, BorderLayout.CENTER);
            profileCard.add(passwordRow);
            profileCard.add(Box.createVerticalStrut(18));

            errorMsg = new JLabel();
            errorMsg.setForeground(DANGER);
            errorMsg.setFont(new Font(F, Font.PLAIN, 12));
            errorMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
            profileCard.add(errorMsg);
            profileCard.add(Box.createVerticalStrut(14));

            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            buttonRow.setOpaque(false);
            buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            JButton doneBtn = makePrimaryButton("\u2713   Done");
            doneBtn.addActionListener(e -> onProfileSave());
            JButton cancelBtn = makeSecondaryButton("\u2715   Cancel");
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
        field.setCaretColor(ACCENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(6, 10, 6, 10)));
        field.setMaximumSize(new Dimension(260, 34));
        field.setPreferredSize(new Dimension(260, 34));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT, 2),
                    new EmptyBorder(5, 9, 5, 9)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(6, 10, 6, 10)));
            }
        });
        return field;
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

    private void onProfileSave() {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String username  = usernameField.getText().trim();
        String email     = emailField.getText().trim();
        String phone     = phoneField.getText().trim();
        String password  = passwordVisible ? passwordField.getText().trim() : currentTech.getPassword();

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty()
                || email.isEmpty() || phone.isEmpty()) {
            errorMsg.setText("\u26A0  All fields are required.");
            profileCard.revalidate(); profileCard.repaint();
            return;
        }
        if (!phone.matches("\\d{10,11}")) {
            errorMsg.setText("\u26A0  Phone must be 10-11 digits.");
            profileCard.revalidate(); profileCard.repaint();
            return;
        }
        List<User> users = FileHandler.loadAllUsers();
        User userToUpdate = null;
        for (User u : users) {
            if (u.getUserID().equals(currentTech.getUserID())) { userToUpdate = u; break; }
        }
        if (userToUpdate == null) {
            errorMsg.setText("\u26A0  Error: User not found in database.");
            profileCard.revalidate(); profileCard.repaint();
            return;
        }
        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);
        userToUpdate.setUsername(username);
        userToUpdate.setEmail(email);
        userToUpdate.setPhone(phone);
        if (passwordVisible) userToUpdate.setPassword(password);
        FileHandler.saveAllUsers(users);

        currentTech.setFirstName(firstName);
        currentTech.setLastName(lastName);
        currentTech.setUsername(username);
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

    // PANEL 2 — MY APPOINTMENTS
    private JPanel buildAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(32, 36, 32, 36));

        JPanel leftPane = new JPanel(new BorderLayout(0, 16));
        leftPane.setOpaque(false);
        leftPane.setPreferredSize(new Dimension(480, 0));

        JPanel headBlock = new JPanel();
        headBlock.setLayout(new BoxLayout(headBlock, BoxLayout.Y_AXIS));
        headBlock.setOpaque(false);
        JLabel heading = new JLabel("My Appointments");
        heading.setFont(new Font(F, Font.BOLD, 25));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Click a row to view details");
        sub.setFont(new Font(F, Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(6, 0, 0, 0));
        headBlock.add(heading);
        headBlock.add(sub);

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
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JTextField searchField = new JTextField();
        searchField.setFont(new Font(F, Font.PLAIN, 13));
        searchField.setBackground(BG_CARD2);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(9, 12, 9, 12)));
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
        searchLbl.setForeground(TEXT_FAINT);
        searchLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchLbl.setBorder(new EmptyBorder(0, 2, 6, 0));
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        searchWrap.add(searchField, BorderLayout.CENTER);

        JScrollPane scroll = makeScrollPane(table);

        JPanel listTop = new JPanel();
        listTop.setLayout(new BoxLayout(listTop, BoxLayout.Y_AXIS));
        listTop.setOpaque(false);
        headBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        listTop.add(headBlock);
        listTop.add(Box.createVerticalStrut(16));
        listTop.add(searchLbl);
        listTop.add(searchWrap);

        leftPane.add(listTop, BorderLayout.NORTH);
        leftPane.add(scroll,  BorderLayout.CENTER);

        RoundedPanel rightPane = new RoundedPanel(16, BG_CARD);
        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
        rightPane.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel detailHeading = new JLabel("Appointment Details");
        detailHeading.setFont(new Font(F, Font.BOLD, 17));
        detailHeading.setForeground(TEXT_PRIMARY);
        detailHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel detailContent = new JLabel(
            "<html><div style='color:#8F94B2;'>Select an appointment from the<br>"
            + "list to view its details.</div></html>");
        detailContent.setFont(new Font(F, Font.PLAIN, 13));
        detailContent.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea commentArea = new JTextArea();
        commentArea.setEditable(false);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setFont(new Font(F, Font.PLAIN, 12));
        commentArea.setForeground(TEXT_MUTED);
        commentArea.setBackground(BG_CARD2);
        commentArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        commentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        commentScroll.setPreferredSize(new Dimension(0, 110));
        commentScroll.setVisible(false);
        JLabel commentTitle = new JLabel("\uD83D\uDCAC  Customer Comment");
        commentTitle.setFont(new Font(F, Font.BOLD, 12));
        commentTitle.setForeground(TEXT_PRIMARY);
        commentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentTitle.setVisible(false);

        JButton completeBtn = makePrimaryButton("\u2705   Mark as Completed");
        completeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        completeBtn.setEnabled(false);
        JButton feedbackBtn = makeSecondaryButton("\uD83D\uDCDD   Write Feedback");
        feedbackBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        feedbackBtn.setEnabled(false);

        rightPane.add(detailHeading);
        rightPane.add(Box.createVerticalStrut(18));
        rightPane.add(detailContent);
        rightPane.add(Box.createVerticalStrut(20));
        rightPane.add(commentTitle);
        rightPane.add(Box.createVerticalStrut(8));
        rightPane.add(commentScroll);
        rightPane.add(Box.createVerticalStrut(24));
        rightPane.add(completeBtn);
        rightPane.add(Box.createVerticalStrut(12));
        rightPane.add(feedbackBtn);
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
            detailContent.setText(
                "<html><div style='line-height:1.9;'>"
                + "<span style='color:#8F94B2;'>Appointment ID</span><br>"
                + "<b style='color:#F1F3FF;font-size:14px;'>" + apptID + "</b><br><br>"
                + "<span style='color:#8F94B2;'>Customer</span><br>"
                + "<b style='color:#F1F3FF;'>" + custName + "</b><br><br>"
                + "<span style='color:#8F94B2;'>Date &amp; Time</span><br>"
                + "<b style='color:#F1F3FF;'>" + model.getValueAt(row, 1)
                    + " at " + model.getValueAt(row, 2) + "</b><br><br>"
                + "<span style='color:#8F94B2;'>Service</span><br>"
                + "<b style='color:#F1F3FF;'>" + model.getValueAt(row, 3) + "</b><br><br>"
                + "<span style='color:#8F94B2;'>Status</span><br>"
                + "<b style='color:" + ("Completed".equals(status) ? "#2ECC71" : "#F59E0B")
                    + ";'>" + status + "</b></div></html>");
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
            if ("Pending".equals(status)) completeBtn.setText("\u2705   Mark as Completed");
            else                          completeBtn.setText("\u21A9   Revert to Pending");
            completeBtn.setEnabled(true);
            feedbackBtn.setEnabled("Completed".equals(status));
            rightPane.revalidate();
            rightPane.repaint();
        });

        completeBtn.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow == -1) return;
            int row = table.convertRowIndexToModel(viewRow);
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
                    completeBtn.setText("\u21A9   Revert to Pending");
                    feedbackBtn.setEnabled(true);
                } else {
                    completeBtn.setText("\u2705   Mark as Completed");
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
            int viewRow = table.getSelectedRow();
            if (viewRow == -1) return;
            int row = table.convertRowIndexToModel(viewRow);
            String apptID = (String) model.getValueAt(row, 0);
            openFeedbackDialog(apptID);
        });

        panel.add(leftPane,  BorderLayout.WEST);
        panel.add(rightPane, BorderLayout.CENTER);
        return panel;
    }

    private void openFeedbackDialog(String apptID) {
        JTextArea textArea = new JTextArea(5, 30);
        textArea.setFont(new Font(F, Font.PLAIN, 13));
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

    private String initials(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private JPanel makeInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(F, Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(110, 20));
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
        btn.setForeground(BG_DARK);
        btn.setBackground(ACCENT);
        btn.setBorder(new EmptyBorder(11, 20, 11, 20));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(255, 178, 44));
            }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(ACCENT); }
        });
        return btn;
    }

    private JButton makeSecondaryButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font(F, Font.PLAIN, 13));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_CARD2);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 16, 10, 16)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(BG_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(BG_CARD2); }
        });
        return btn;
    }

    private JButton makeNavButton(String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(new Font(F, Font.PLAIN, 14));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(BG_CARD);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(13, 18, 13, 18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (activeNavButton == null) setNavActive(btn);
        btn.addActionListener(e -> {
            setNavActive(btn);
            if ("DASHBOARD".equals(cardName)) refreshDashboard();
            else contentLayout.show(contentPanel, cardName);
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeNavButton) { btn.setBackground(BG_HOVER); btn.setForeground(TEXT_PRIMARY); }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeNavButton) { btn.setBackground(BG_CARD); btn.setForeground(TEXT_MUTED); }
            }
        });
        return btn;
    }

    private void setNavActive(JButton btn) {
        if (activeNavButton != null) {
            activeNavButton.setBackground(BG_CARD);
            activeNavButton.setForeground(TEXT_MUTED);
            activeNavButton.setBorder(new EmptyBorder(13, 18, 13, 18));
        }
        activeNavButton = btn;
        btn.setBackground(ACCENT_SOFT);
        btn.setForeground(ACCENT);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT),
            new EmptyBorder(13, 15, 13, 18)));
    }

    private JTable makeStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font(F, Font.PLAIN, 13));
        table.setRowHeight(46);
        table.setGridColor(BG_CARD);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(ACCENT_SOFT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBorder(new EmptyBorder(0, 14, 0, 8));
                if (!sel) setBackground(r % 2 == 0 ? BG_CARD : new Color(26, 29, 43));
                if (c == 4) {
                    setForeground("Completed".equals(v) ? SUCCESS : ACCENT);
                    setFont(new Font(F, Font.BOLD, 12));
                } else {
                    setForeground(sel ? TEXT_PRIMARY : TEXT_MUTED);
                    setFont(new Font(F, Font.PLAIN, 13));
                }
                return comp;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cr);
        }
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_CARD2);
        header.setForeground(TEXT_FAINT);
        header.setFont(new Font(F, Font.BOLD, 11));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);
        return table;
    }

    private JScrollPane makeScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        styleScrollBar(sp);
        return sp;
    }

    private void styleScrollBar(JScrollPane sp) {
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = BG_HOVER;
                this.trackColor = BG_DARK;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                return b;
            }
        });
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}