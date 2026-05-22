package gui.customer;

import model.Customer;
import model.Appointment;
import model.Feedback;
import model.Comment;
import model.Payment;
import model.User;
import util.FileHandler;
import util.Session;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GUI CLASS — CustomerDashboard
 * ------------------------------
 * MEMBER 1 is responsible for implementing all features in this file.
 *
 * FEATURES TO IMPLEMENT:
 *   [1] Edit own profile
 *   [2] View personal service and payment history
 *   [3] View technician feedback for each appointment
 *   [4] Leave a comment for counter staff and/or technician
 */
public class CustomerDashboard extends JFrame {

    // COLOURS
    private static final Color BG_DARK      = new Color(15,  17,  26);
    private static final Color BG_CARD      = new Color(24,  27,  42);
    private static final Color BG_CARD2     = new Color(30,  34,  52);
    private static final Color ACCENT       = new Color(236, 72, 153);
    private static final Color STAR_YELLOW  = new Color(255, 204, 0);
    private static final Color TEXT_PRIMARY = new Color(240, 241, 255);
    private static final Color TEXT_MUTED   = new Color(148, 151, 180);
    private static final Color BORDER_COLOR = new Color(55,  58,  80);
    private static final Color DANGER       = new Color(239, 68,  68);

    // STATE
    private Customer currentCustomer;

    // LAYOUT
    private CardLayout contentLayout;
    private JPanel     contentPanel;
    private JPanel     historyPanel;

    // PROFILE PANEL STATE
    private boolean profileEditMode = false;
    private JPanel profileCard;
    private JLabel errorMsg;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField passwordField;
    private boolean passwordVisible = false;
    private JLabel userLabel;

    // CONSTRUCTOR
    public CustomerDashboard(Customer customer) {
        this.currentCustomer = customer;

        setTitle("APU-ASC — Customer Dashboard");
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

        userLabel = new JLabel("🚗  " + currentCustomer.getFullName() + "  ·  Customer");
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userLabel.setForeground(TEXT_MUTED);
        userLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (profileEditMode) {
                    int result = JOptionPane.showConfirmDialog(CustomerDashboard.this,
                        "You have unsaved changes. Discard them?",
                        "Unsaved Changes",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                    exitProfileEditMode();
                }
                contentLayout.show(contentPanel, "PROFILE");
            }

            @Override public void mouseEntered(MouseEvent e) {
                userLabel.setForeground(TEXT_PRIMARY);
            }

            @Override public void mouseExited(MouseEvent e) {
                userLabel.setForeground(TEXT_MUTED);
            }
        });

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

        rightSide.add(userLabel);
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

        JLabel section = new JLabel("  CUSTOMER MENU");
        section.setFont(new Font("SansSerif", Font.BOLD, 10));
        section.setForeground(TEXT_MUTED);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(makeNavButton("🏠  Dashboard",        "DASHBOARD"));
        sidebar.add(makeNavButton("📋  Service History",  "HISTORY"));
        sidebar.add(makeNavButton("🛠  Technician Feedback", "TECH_FEEDBACK"));
        sidebar.add(makeNavButton("💬  Leave a Comment",  "COMMENT"));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    //  CONTENT PANELS
    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(BG_DARK);

        contentPanel.add(buildDashboardPanel(), "DASHBOARD");
        contentPanel.add(buildProfilePanel(), "PROFILE");
        historyPanel = buildHistoryPanel();
        contentPanel.add(historyPanel, "HISTORY");
        contentPanel.add(buildTechnicianFeedbackPanel(), "TECH_FEEDBACK");
        contentPanel.add(buildCommentPanel(), "COMMENT");

        contentLayout.show(contentPanel, "DASHBOARD");
        return contentPanel;
    }


    //  PANEL 0 — DASHBOARD OVERVIEW
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 24));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcomeLabel = new JLabel("Welcome back, " + currentCustomer.getFirstName() + " 👋");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setForeground(TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Here's your service overview");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(welcomeLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitleLabel);

        String customerID = resolveCustomerID();
        if (customerID == null || customerID.isBlank()) {
            JLabel warning = new JLabel(
                "<html>Unable to load your dashboard because your customer profile ID was not found.<br>" +
                "Please contact counter staff for assistance.</html>"
            );
            warning.setFont(new Font("SansSerif", Font.PLAIN, 14));
            warning.setForeground(DANGER);
            warning.setBorder(new EmptyBorder(20, 0, 0, 0));
            panel.add(headerPanel, BorderLayout.NORTH);
            panel.add(warning, BorderLayout.CENTER);
            return panel;
        }

        List<Appointment> allAppointments = FileHandler.loadAllAppointments();
        List<Appointment> myAppointments = allAppointments.stream()
            .filter(a -> customerID.equals(a.getCustomerID()))
            .collect(Collectors.toList());

        List<Payment> allPayments = FileHandler.loadAllPayments();

        int totalAppointments = myAppointments.size();

        Map<String, Payment> paymentByAppointment = new HashMap<>();
        allPayments.forEach(p -> paymentByAppointment.putIfAbsent(p.getAppointmentID(), p));

        double totalSpent = 0.0;
        double outstandingAmount = 0.0;
        for (Appointment appt : myAppointments) {
            Payment payment = paymentByAppointment.get(appt.getAppointmentID());
            if (payment != null && "Paid".equalsIgnoreCase(payment.getStatus())) {
                totalSpent += payment.getAmount();
            } else if ("Completed".equalsIgnoreCase(appt.getStatus())) {
                outstandingAmount += payment != null
                    ? payment.getAmount()
                    : FileHandler.getServicePrice(appt.getServiceType());
            }
        }

        String lastVisitDate = "No visits yet";
        String lastVisitDetails = "";
        Appointment lastCompletedAppt = myAppointments.stream()
            .filter(a -> "Completed".equals(a.getStatus()))
            .sorted((a1, a2) -> a2.getDate().compareTo(a1.getDate()))
            .findFirst().orElse(null);

        if (lastCompletedAppt != null) {
            lastVisitDate = lastCompletedAppt.getDate();
            double lastAmount = allPayments.stream()
                .filter(p -> p.getAppointmentID().equals(lastCompletedAppt.getAppointmentID()))
                .mapToDouble(Payment::getAmount)
                .findFirst().orElse(0.0);
            lastVisitDetails = String.format("%s · %s",
                lastCompletedAppt.getTime(),
                lastCompletedAppt.getServiceType());
        }

        String nextApptDate = "No appointment";
        String nextApptDetails = "";
        Appointment nextPendingAppt = myAppointments.stream()
            .filter(a -> "Pending".equals(a.getStatus()))
            .sorted((a1, a2) -> a1.getDate().compareTo(a2.getDate()))
            .findFirst().orElse(null);

        if (nextPendingAppt != null) {
            nextApptDate = nextPendingAppt.getDate();
            nextApptDetails = String.format("%s · %s",
                nextPendingAppt.getTime(),
                nextPendingAppt.getServiceType());
        }

        boolean hasNextAppointment = nextPendingAppt != null;

        JPanel statsGrid = new JPanel(new GridBagLayout());
        statsGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        Color BLUE_ACCENT = new Color(59, 130, 246);
        Color PINK_ACCENT = ACCENT;
        Color RED_ACCENT = new Color(239, 68, 68);
        Color GREEN_ACCENT = new Color(16, 185, 129);
        Color PURPLE_ACCENT = new Color(139, 92, 246);
        Color CYAN_ACCENT = new Color(6, 182, 212);

        addDashboardCard(statsGrid, createStatCard("\uD83D\uDCCA", "TOTAL APPOINTMENTS", String.valueOf(totalAppointments),
            "All time bookings", BLUE_ACCENT, false), gbc, 0, 0, 1);
        addDashboardCard(statsGrid, createStatCard("\uD83D\uDCB0", "TOTAL SPENT", String.format("RM %.2f", totalSpent),
            totalSpent > 0 ? "Lifetime total" : "No payments recorded yet", PINK_ACCENT, true), gbc, 1, 0, 1);

        Color outstandingColor = outstandingAmount > 0 ? RED_ACCENT : GREEN_ACCENT;
        String outstandingIcon = outstandingAmount > 0 ? "\u26A0" : "\u2705";
        addDashboardCard(statsGrid, createStatCard(outstandingIcon, "OUTSTANDING PAYMENTS",
            String.format("RM %.2f", outstandingAmount),
            outstandingAmount > 0 ? "Unpaid balance" : "All paid up!",
            outstandingColor, outstandingAmount > 0), gbc, 2, 0, 1);

        addDashboardCard(statsGrid, createStatCard("\uD83D\uDD52", "LAST VISIT DATE", lastVisitDate,
            lastVisitDetails, PURPLE_ACCENT, false), gbc, 0, 1, 1);
        addDashboardCard(statsGrid, createStatCard("\uD83D\uDCC5", "NEXT APPOINTMENT", nextApptDate,
            nextApptDetails, CYAN_ACCENT, false), gbc, 1, 1, hasNextAppointment ? 1 : 2);

        if (hasNextAppointment) {
            JPanel emptyCell = new JPanel();
            emptyCell.setOpaque(false);
            addDashboardCard(statsGrid, emptyCell, gbc, 2, 1, 1);
        }

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(statsGrid, BorderLayout.CENTER);
        return panel;
    }

    private void addDashboardCard(JPanel grid, Component card, GridBagConstraints gbc,
                                  int x, int y, int width) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.insets = new Insets(y == 0 ? 0 : 20, x == 0 ? 0 : 20, 0, 0);
        grid.add(card, gbc);
    }

    private JPanel createStatCard(String icon, String label, String value, String subtitle,
                                  Color accentColor, boolean highlightValue) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(24, 24, 24, 24)
            )
        ));

        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setPreferredSize(new Dimension(56, 56));
        iconPanel.setMaximumSize(new Dimension(56, 56));
        iconPanel.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 40));
        iconPanel.setOpaque(true);
        iconPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLabel.setForeground(new Color(255, 255, 255, 230));
        iconPanel.add(iconLabel, BorderLayout.CENTER);

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("SansSerif", Font.BOLD, 11));
        labelText.setForeground(TEXT_MUTED);
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueText = new JLabel(value);
        int valueFontSize = "No visits yet".equals(value) ? 30 : 36;
        valueText.setFont(new Font("SansSerif", Font.BOLD, valueFontSize));
        valueText.setForeground(highlightValue ? accentColor : TEXT_PRIMARY);
        valueText.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(iconPanel);
        card.add(Box.createVerticalStrut(16));
        card.add(labelText);
        card.add(Box.createVerticalStrut(12));
        card.add(valueText);
        if (subtitle != null && !subtitle.isBlank()) {
            JLabel subtitleText = new JLabel("<html><body style='width: 220px'>" + subtitle + "</body></html>");
            subtitleText.setFont(new Font("SansSerif", Font.PLAIN, 13));
            subtitleText.setForeground(TEXT_MUTED);
            subtitleText.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(Box.createVerticalStrut(8));
            card.add(subtitleText);
        }
        card.add(Box.createVerticalGlue());

        return card;
    }


    //  PANEL 1 — MY PROFILE
    //  TODO (Member 1): Allow customer to edit their own details
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
            profileCard.add(makeInfoRow("Full Name", currentCustomer.getFullName()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Username", currentCustomer.getUsername()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Email", currentCustomer.getEmail()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Phone", currentCustomer.getPhone()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Password", maskPassword(currentCustomer.getPassword())));
            profileCard.add(Box.createVerticalStrut(24));

            JButton editBtn = makePrimaryButton("✏  Edit Profile");
            editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            editBtn.addActionListener(e -> enterProfileEditMode());
            profileCard.add(editBtn);
        } else {
            JPanel fullNameRow = new JPanel(new BorderLayout(16, 0));
            fullNameRow.setOpaque(false);
            fullNameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel fullNameLbl = new JLabel("Full Name:");
            fullNameLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            fullNameLbl.setForeground(TEXT_MUTED);
            fullNameLbl.setPreferredSize(new Dimension(100, 20));

            JPanel nameFieldsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
            nameFieldsPanel.setOpaque(false);

            firstNameField = makeEditableTextField(currentCustomer.getFirstName());
            lastNameField = makeEditableTextField(currentCustomer.getLastName());

            Dimension nameFieldHeight = new Dimension(0, 28);
            firstNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            lastNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            firstNameField.setPreferredSize(nameFieldHeight);
            lastNameField.setPreferredSize(nameFieldHeight);

            nameFieldsPanel.add(firstNameField);
            nameFieldsPanel.add(lastNameField);

            fullNameRow.add(fullNameLbl, BorderLayout.WEST);
            fullNameRow.add(nameFieldsPanel, BorderLayout.CENTER);
            profileCard.add(fullNameRow);
            profileCard.add(Box.createVerticalStrut(12));

            JPanel usernameRow = new JPanel(new BorderLayout(16, 0));
            usernameRow.setOpaque(false);
            usernameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel usernameLbl = new JLabel("Username:");
            usernameLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            usernameLbl.setForeground(TEXT_MUTED);
            usernameLbl.setPreferredSize(new Dimension(100, 20));
            usernameField = makeLockedTextField(currentCustomer.getUsername());
            usernameRow.add(usernameLbl, BorderLayout.WEST);
            usernameRow.add(usernameField, BorderLayout.CENTER);
            profileCard.add(usernameRow);
            JLabel usernameNote = new JLabel("Username cannot be changed.");
            usernameNote.setFont(new Font("SansSerif", Font.PLAIN, 11));
            usernameNote.setForeground(TEXT_MUTED);
            usernameNote.setBorder(new EmptyBorder(0, 116, 0, 0));
            usernameNote.setAlignmentX(Component.LEFT_ALIGNMENT);
            profileCard.add(Box.createVerticalStrut(4));
            profileCard.add(usernameNote);
            profileCard.add(Box.createVerticalStrut(12));

            JPanel emailRow = new JPanel(new BorderLayout(16, 0));
            emailRow.setOpaque(false);
            emailRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel emailLbl = new JLabel("Email:");
            emailLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            emailLbl.setForeground(TEXT_MUTED);
            emailLbl.setPreferredSize(new Dimension(100, 20));
            emailField = makeEditableTextField(currentCustomer.getEmail());
            emailRow.add(emailLbl, BorderLayout.WEST);
            emailRow.add(emailField, BorderLayout.CENTER);
            profileCard.add(emailRow);
            profileCard.add(Box.createVerticalStrut(12));

            JPanel phoneRow = new JPanel(new BorderLayout(16, 0));
            phoneRow.setOpaque(false);
            phoneRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel phoneLbl = new JLabel("Phone:");
            phoneLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            phoneLbl.setForeground(TEXT_MUTED);
            phoneLbl.setPreferredSize(new Dimension(100, 20));
            phoneField = makeEditableTextField(currentCustomer.getPhone());
            phoneRow.add(phoneLbl, BorderLayout.WEST);
            phoneRow.add(phoneField, BorderLayout.CENTER);
            profileCard.add(phoneRow);
            profileCard.add(Box.createVerticalStrut(12));

            JPanel passwordRow = new JPanel(new BorderLayout(16, 0));
            passwordRow.setOpaque(false);
            passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel passwordLbl = new JLabel("Password:");
            passwordLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            passwordLbl.setForeground(TEXT_MUTED);
            passwordLbl.setPreferredSize(new Dimension(100, 20));

            passwordField = makeEditableTextField(maskPassword(currentCustomer.getPassword()));
            passwordField.setBorder(new EmptyBorder(0, 0, 0, 0));
            passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            passwordField.setPreferredSize(new Dimension(0, 28));

            JPanel passwordFieldPanel = new JPanel(new BorderLayout(8, 0));
            passwordFieldPanel.setBackground(BG_CARD2);
            passwordFieldPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            passwordFieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            passwordFieldPanel.setPreferredSize(new Dimension(0, 34));
            passwordFieldPanel.add(passwordField, BorderLayout.CENTER);

            JButton eyeToggle = new JButton(makeEyeIcon(TEXT_MUTED));
            eyeToggle.setForeground(TEXT_MUTED);
            eyeToggle.setBackground(BG_CARD2);
            eyeToggle.setOpaque(false);
            eyeToggle.setBorderPainted(false);
            eyeToggle.setFocusPainted(false);
            eyeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eyeToggle.setPreferredSize(new Dimension(38, 28));
            eyeToggle.addActionListener(e -> {
                passwordVisible = !passwordVisible;
                if (passwordVisible) {
                    eyeToggle.setForeground(TEXT_PRIMARY);
                    eyeToggle.setIcon(makeEyeIcon(TEXT_PRIMARY));
                    passwordField.setText(currentCustomer.getPassword());
                } else {
                    eyeToggle.setForeground(TEXT_MUTED);
                    eyeToggle.setIcon(makeEyeIcon(TEXT_MUTED));
                    passwordField.setText(maskPassword(currentCustomer.getPassword()));
                }
            });
            passwordFieldPanel.add(eyeToggle, BorderLayout.EAST);

            passwordRow.add(passwordLbl, BorderLayout.WEST);
            passwordRow.add(passwordFieldPanel, BorderLayout.CENTER);
            profileCard.add(passwordRow);
            profileCard.add(Box.createVerticalStrut(16));

            errorMsg = new JLabel();
            errorMsg.setForeground(DANGER);
            errorMsg.setFont(new Font("SansSerif", Font.PLAIN, 12));
            errorMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
            profileCard.add(errorMsg);
            profileCard.add(Box.createVerticalStrut(12));

            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            buttonRow.setOpaque(false);

            JButton doneBtn = makePrimaryButton("✓  Done");
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

    private JTextField makeEditableTextField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBackground(BG_CARD2);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        field.setMaximumSize(new Dimension(200, 28));
        field.setPreferredSize(new Dimension(200, 28));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createLineBorder(ACCENT, 2));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            }
        });
        return field;
    }

    private JTextField makeLockedTextField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBackground(BG_CARD2);
        field.setForeground(TEXT_MUTED);
        field.setEditable(false);
        field.setFocusable(false);
        field.setCursor(Cursor.getDefaultCursor());
        field.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        field.setMaximumSize(new Dimension(200, 28));
        field.setPreferredSize(new Dimension(200, 28));
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
        if (errorMsg != null) {
            errorMsg.setText("");
        }
        refreshProfileUI();
    }

    private void exitProfileEditMode() {
        profileEditMode = false;
        passwordVisible = false;
        if (errorMsg != null) {
            errorMsg.setText("");
        }
        refreshProfileUI();
    }

    private String maskPassword(String password) {
        return "*".repeat(password.length());
    }

    private boolean isAlphabeticName(String name) {
        return name.matches("[A-Za-z]+");
    }

    private void onProfileSave() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordVisible ? passwordField.getText().trim() : currentCustomer.getPassword();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorMsg.setText("❌ All fields are required.");
            profileCard.revalidate();
            profileCard.repaint();
            return;
        }

        if (!isAlphabeticName(firstName) || !isAlphabeticName(lastName)) {
            errorMsg.setText("Full name must contain alphabets only.");
            profileCard.revalidate();
            profileCard.repaint();
            return;
        }

        if (!phone.matches("\\d{10,11}")) {
            errorMsg.setText("❌ Phone must be 10-11 digits.");
            profileCard.revalidate();
            profileCard.repaint();
            return;
        }

        currentCustomer.setFirstName(firstName);
        currentCustomer.setLastName(lastName);
        currentCustomer.setEmail(email);
        currentCustomer.setPhone(phone);
        if (passwordVisible) {
            currentCustomer.setPassword(password);
        }

        boolean usersSaved = FileHandler.updateUserProfile(currentCustomer);
        boolean customersSaved = FileHandler.updateCustomerProfile(currentCustomer);
        if (!usersSaved || !customersSaved) {
            errorMsg.setText("❌ Error saving profile.");
            profileCard.revalidate();
            profileCard.repaint();
            return;
        }

        profileEditMode = false;
        passwordVisible = false;
        errorMsg.setText("");
        refreshProfileUI();
        updateTopBarLabel();
    }

    private void updateTopBarLabel() {
        if (userLabel != null) {
            userLabel.setText("🚗  " + currentCustomer.getFullName() + "  ·  Customer");
        }
    }


    //  PANEL 2 — SERVICE HISTORY
    //  Shows this customer's past appointments + service fee + payment details

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("My Service History");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        String[] cols = {
            "Appt ID", "Date", "Time", "Service Type", "Service Fee (RM)",
            "Status", "Comments", "Payment Status"
        };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        String resolvedCustomerID = resolveCustomerID();
        if (resolvedCustomerID == null || resolvedCustomerID.isBlank()) {
            JLabel warning = new JLabel(
                "<html>Unable to load your history because your customer profile ID was not found.<br>" +
                "Please contact counter staff for assistance.</html>"
            );
            warning.setFont(new Font("SansSerif", Font.PLAIN, 14));
            warning.setForeground(DANGER);
            warning.setBorder(new EmptyBorder(20, 0, 0, 0));
            panel.add(heading, BorderLayout.NORTH);
            panel.add(warning, BorderLayout.CENTER);
            return panel;
        }

        // Load appointments for THIS customer only (no hardcoded fallback).
        List<Appointment> myAppts = FileHandler.loadAllAppointments()
            .stream()
            .filter(a -> resolvedCustomerID.equals(a.getCustomerID()))
            .sorted(Comparator.comparing(Appointment::getDate).reversed())
            .collect(java.util.stream.Collectors.toList());

        // Build lookup maps once to avoid repeated scans for each row.
        List<Comment> comments = FileHandler.loadAllComments();
        List<model.Payment> payments = FileHandler.loadAllPayments();
        Map<String, String> commentByAppointment = new HashMap<>();
        Map<String, model.Payment> paymentByAppointment = new HashMap<>();

        comments.stream()
            .filter(c -> resolvedCustomerID.equals(c.getCustomerID()))
            .forEach(c -> commentByAppointment.put(c.getAppointmentID(), c.getCommentText()));
        payments.forEach(p -> paymentByAppointment.putIfAbsent(p.getAppointmentID(), p));

        myAppts.forEach(a -> {
            String customerComment = commentByAppointment.getOrDefault(
                a.getAppointmentID(), "No comment yet"
            );

            model.Payment payment = paymentByAppointment.get(a.getAppointmentID());
            double fee = payment != null
                ? payment.getAmount()
                : FileHandler.getServicePrice(a.getServiceType());
            String payStatus;
            if (payment == null) {
                payStatus = "Unpaid";
            } else if ("Paid".equalsIgnoreCase(payment.getStatus())) {
                payStatus = payment.getStatus() + " on " + payment.getDate();
            } else {
                payStatus = payment.getStatus();
            }

            model.addRow(new Object[]{
                a.getAppointmentID(),
                a.getDate(),
                a.getTime(),
                a.getServiceType(),
                String.format("%.2f", fee),
                a.getStatus(),
                customerComment,
                payStatus
            });
        });

        JTable table = makeStyledTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        setFixedColumnWidth(table, 0, 80);   // appt id
        setFixedColumnWidth(table, 1, 92);   // date
        setFixedColumnWidth(table, 2, 84);   // time
        setFixedColumnWidth(table, 3, 116);  // service type
        setFixedColumnWidth(table, 4, 126);  // service fee
        setFixedColumnWidth(table, 5, 96);   // status
        table.getColumnModel().getColumn(6).setPreferredWidth(220); // comments
        table.getColumnModel().getColumn(7).setPreferredWidth(176); // payment status
        JScrollPane scroll = makeScrollPane(table);

        panel.add(heading, BorderLayout.NORTH);
        if (myAppts.isEmpty()) {
            JLabel empty = new JLabel("No service history found yet.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 14));
            empty.setForeground(TEXT_MUTED);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(empty, BorderLayout.CENTER);
        } else {
            panel.add(scroll, BorderLayout.CENTER);
        }
        return panel;
    }

    private String resolveCustomerID() {
        String existing = currentCustomer.getCustomerID();
        if (existing != null && !existing.isBlank()) {
            return existing;
        }

        String userID = currentCustomer.getUserID();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/data/customers.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 6 && parts[5].equals(userID)) {
                    currentCustomer.setCustomerID(parts[0]);
                    return parts[0];
                }
            }
        } catch (IOException e) {
            System.err.println("Error resolving customer ID: " + e.getMessage());
        }
        return "";
    }

    private JPanel buildTechnicianFeedbackPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Technician Feedback");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        String resolvedCustomerID = resolveCustomerID();
        if (resolvedCustomerID == null || resolvedCustomerID.isBlank()) {
            JLabel warning = new JLabel(
                "<html>Unable to load technician feedback because your customer profile ID was not found.<br>" +
                "Please contact counter staff for assistance.</html>"
            );
            warning.setFont(new Font("SansSerif", Font.PLAIN, 14));
            warning.setForeground(DANGER);
            warning.setBorder(new EmptyBorder(20, 0, 0, 0));
            panel.add(heading, BorderLayout.NORTH);
            panel.add(warning, BorderLayout.CENTER);
            return panel;
        }

        String[] cols = {"Appt ID", "Date", "Service Type", "Technician", "Feedback"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Appointment> myAppts = FileHandler.loadAllAppointments()
            .stream()
            .filter(a -> resolvedCustomerID.equals(a.getCustomerID()))
            .sorted(Comparator.comparing(Appointment::getDate).reversed())
            .collect(java.util.stream.Collectors.toList());

        Map<String, Appointment> apptByID = new HashMap<>();
        myAppts.forEach(a -> apptByID.put(a.getAppointmentID(), a));

        List<User> users = FileHandler.loadAllUsers();
        Map<String, String> technicianNameByUserID = new HashMap<>();
        users.stream()
            .filter(u -> "Technician".equals(u.getRole()))
            .forEach(u -> technicianNameByUserID.put(u.getUserID(), u.getFullName()));

        List<Feedback> myFeedbacks = FileHandler.loadAllFeedbacks()
            .stream()
            .filter(f -> apptByID.containsKey(f.getAppointmentID()))
            .collect(java.util.stream.Collectors.toList());

        myFeedbacks.forEach(f -> {
            Appointment appt = apptByID.get(f.getAppointmentID());
            if (appt == null) return;

            String feedbackTechID = f.getTechnicianID();
            String feedbackTechName = FileHandler.getTechnicianNameByID(feedbackTechID);
            String apptTechID = appt.getTechnicianID();
            String apptTechName = technicianNameByUserID.getOrDefault(apptTechID, "Unknown Technician");

            String technicianDisplay;
            if (feedbackTechName != null && !feedbackTechName.isBlank() && !"Unknown Technician".equals(feedbackTechName)) {
                technicianDisplay = feedbackTechID + " - " + feedbackTechName;
            } else if (apptTechID != null && !apptTechID.isBlank()) {
                technicianDisplay = apptTechID + " - " + apptTechName;
            } else {
                technicianDisplay = "Unknown Technician";
            }

            model.addRow(new Object[]{
                appt.getAppointmentID(),
                appt.getDate(),
                appt.getServiceType(),
                technicianDisplay,
                f.getFeedbackText()
            });
        });

        JTable table = makeStyledTable(model);
        table.getColumnModel().getColumn(3).setPreferredWidth(180); // technician
        table.getColumnModel().getColumn(4).setPreferredWidth(360); // feedback
        table.setToolTipText("Double-click an appointment to view full feedback");
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int row = table.convertRowIndexToModel(table.getSelectedRow());
                    showTechnicianFeedbackDialog(
                        model.getValueAt(row, 3).toString(),
                        model.getValueAt(row, 4).toString()
                    );
                }
            }
        });
        JScrollPane scroll = makeScrollPane(table);

        panel.add(heading, BorderLayout.NORTH);
        if (model.getRowCount() == 0) {
            JLabel empty = new JLabel("No technician feedback available for your appointments yet.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 14));
            empty.setForeground(TEXT_MUTED);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(empty, BorderLayout.CENTER);
        } else {
            panel.add(scroll, BorderLayout.CENTER);
        }
        return panel;
    }


    //  PANEL 3 — LEAVE A COMMENT
    private void showTechnicianFeedbackDialog(String technician, String feedback) {
        JPanel message = new JPanel();
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        message.setBackground(BG_DARK);
        message.setBorder(new EmptyBorder(18, 18, 8, 18));
        message.setPreferredSize(new Dimension(420, 150));

        String technicianID = technician;
        String technicianName = technician;
        int separator = technician.indexOf(" - ");
        if (separator >= 0) {
            technicianID = technician.substring(0, separator).trim();
            technicianName = technician.substring(separator + 3).trim();
        }

        JLabel title = new JLabel("Feedback from " + technicianName + " (" + technicianID + "):");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea feedbackText = new JTextArea(feedback);
        feedbackText.setEditable(false);
        feedbackText.setLineWrap(true);
        feedbackText.setWrapStyleWord(true);
        feedbackText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        feedbackText.setForeground(TEXT_PRIMARY);
        feedbackText.setBackground(BG_DARK);
        feedbackText.setBorder(null);
        feedbackText.setAlignmentX(Component.LEFT_ALIGNMENT);

        message.add(title);
        message.add(Box.createVerticalStrut(22));
        message.add(feedbackText);

        showDarkDialog("Info", message);
    }

    private void showDarkDialog(String title, JPanel content) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel wrapper = new JPanel(new BorderLayout(0, 18));
        wrapper.setBackground(BG_DARK);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(22, 34, 14, 34)
        ));

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        okButton.setForeground(Color.WHITE);
        okButton.setBackground(new Color(99, 91, 235));
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setContentAreaFilled(true);
        okButton.setBorder(new EmptyBorder(8, 22, 8, 22));
        okButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);

        wrapper.add(content, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setContentPane(wrapper);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.getRootPane().setDefaultButton(okButton);
        dialog.setVisible(true);
    }

    private void showSuccessDialog(String messageText) {
        JPanel message = new JPanel();
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        message.setBackground(BG_DARK);
        message.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel messageLabel = new JLabel(messageText);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        message.add(messageLabel);

        showDarkDialog("Success", message);
    }

    //  TODO (Member 1): Allow customer to leave a comment on their appointments
    private JPanel buildCommentPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Leave a Comment");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(BG_CARD);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(28, 28, 28, 28)
        ));

        // Appointment selector
        JLabel apptLbl = new JLabel("Select Appointment:");
        apptLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        apptLbl.setForeground(TEXT_MUTED);
        apptLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Populate dropdown with this customer's completed appointments
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
        comboModel.addElement("Choose an appointment");
        String resolvedCustomerID = resolveCustomerID();
        if (resolvedCustomerID != null && !resolvedCustomerID.isBlank()) {
            FileHandler.loadAllAppointments()
                .stream()
                .filter(a -> resolvedCustomerID.equals(a.getCustomerID()))
                .filter(a -> "Completed".equals(a.getStatus()))
                .forEach(a -> comboModel.addElement(
                    a.getAppointmentID() + " — " + a.getDate() + " (" + a.getServiceType() + ")"
                ));
        }
        if (comboModel.getSize() == 1) comboModel.addElement("No completed appointments");

        JComboBox<String> apptCombo = new JComboBox<>(comboModel);
        apptCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        apptCombo.setBackground(BG_CARD2);
        apptCombo.setForeground(TEXT_PRIMARY);
        apptCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        apptCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        Map<String, String> existingCommentByAppointment = new HashMap<>();
        if (resolvedCustomerID != null && !resolvedCustomerID.isBlank()) {
            FileHandler.loadAllComments().stream()
                .filter(c -> resolvedCustomerID.equals(c.getCustomerID()))
                .forEach(c -> existingCommentByAppointment.put(c.getAppointmentID(), c.getCommentText()));
        }

        // Rating selector
        JLabel ratingLbl = new JLabel("Rating:");
        ratingLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ratingLbl.setForeground(TEXT_MUTED);
        ratingLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        int[] selectedRating = {0};
        JButton[] starButtons = new JButton[5];
        JLabel ratingHint = makeRatingHint(selectedRating[0]);
        JPanel ratingPanel = buildRatingPanel(selectedRating, starButtons, ratingHint);

        // Comment text area
        JLabel commentLbl = new JLabel("Your Comment:");
        commentLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        commentLbl.setForeground(TEXT_MUTED);
        commentLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea commentArea = new JTextArea(5, 30);
        commentArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        commentArea.setBackground(BG_CARD2);
        commentArea.setForeground(TEXT_PRIMARY);
        commentArea.setCaretColor(TEXT_PRIMARY);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        commentScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentScroll.setBorder(null);

        JLabel errorLbl = new JLabel(" ");
        errorLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLbl.setForeground(DANGER);
        errorLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton submitBtn = makePrimaryButton("Submit Comment");
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel commentDetailsPanel = new JPanel();
        commentDetailsPanel.setLayout(new BoxLayout(commentDetailsPanel, BoxLayout.Y_AXIS));
        commentDetailsPanel.setOpaque(false);
        commentDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentDetailsPanel.setVisible(false);

        apptCombo.addActionListener(e -> {
            String selected = (String) apptCombo.getSelectedItem();
            if (selected == null || selected.startsWith("Choose") || selected.startsWith("No completed")) {
                selectedRating[0] = 0;
                refreshRatingStars(starButtons, selectedRating[0], ratingHint);
                commentArea.setText("");
                errorLbl.setText(" ");
                commentDetailsPanel.setVisible(false);
                formCard.revalidate();
                formCard.repaint();
                return;
            }
            String selectedApptID = selected.split(" — ")[0].trim();
            String existingComment = existingCommentByAppointment.getOrDefault(selectedApptID, "");
            selectedRating[0] = extractRating(existingComment);
            refreshRatingStars(starButtons, selectedRating[0], ratingHint);
            commentArea.setText(extractCommentBody(existingComment));
            errorLbl.setText(" ");
            commentDetailsPanel.setVisible(true);
            formCard.revalidate();
            formCard.repaint();
        });
        if (comboModel.getSize() > 0) {
            String firstSelected = (String) comboModel.getSelectedItem();
            if (firstSelected != null && !firstSelected.startsWith("No completed")) {
                String firstApptID = firstSelected.split(" — ")[0].trim();
                String existingComment = existingCommentByAppointment.getOrDefault(firstApptID, "");
                selectedRating[0] = 0;
                refreshRatingStars(starButtons, selectedRating[0], ratingHint);
                commentArea.setText(extractCommentBody(existingComment));
            }
        }

        submitBtn.addActionListener(e -> {
            String selectedAppt = (String) apptCombo.getSelectedItem();
            String commentText  = commentArea.getText().trim();
            String customerID = resolveCustomerID();

            if (selectedAppt == null || selectedAppt.startsWith("Choose") || selectedAppt.startsWith("No completed")) {
                errorLbl.setText("No appointment selected."); return;
            }
            if (selectedRating[0] == 0) {
                errorLbl.setText("Please select a rating."); return;
            }
            if (commentText.isEmpty()) {
                errorLbl.setText("Comment cannot be empty."); return;
            }
            if (customerID == null || customerID.isBlank()) {
                errorLbl.setText("Unable to verify your customer profile."); return;
            }

            // Extract appointment ID from the combo string (e.g. "A001 — 2025-05-10...")
            String apptID = selectedAppt.split(" — ")[0].trim();
            boolean isOwnedCompletedAppointment = FileHandler.loadAllAppointments()
                .stream()
                .anyMatch(a -> a.getAppointmentID().equals(apptID)
                    && customerID.equals(a.getCustomerID())
                    && "Completed".equals(a.getStatus()));
            if (!isOwnedCompletedAppointment) {
                errorLbl.setText("Invalid appointment selection."); return;
            }

            // Keep one record per line in comments.txt
            String sanitizedComment = commentText
                .replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
            String ratedComment = selectedRating[0] + "/5 - " + sanitizedComment;

            List<Comment> comments = FileHandler.loadAllComments();
            Comment existingComment = comments.stream()
                .filter(c -> apptID.equals(c.getAppointmentID()) && customerID.equals(c.getCustomerID()))
                .findFirst()
                .orElse(null);
            if (existingComment != null) {
                existingComment.setCommentText(ratedComment);
            } else {
                comments.add(new Comment(apptID, customerID, ratedComment));
            }
            FileHandler.saveAllComments(comments);
            existingCommentByAppointment.put(apptID, ratedComment);

            showSuccessDialog("Comment saved successfully.");
            errorLbl.setText(" ");
        });

        commentDetailsPanel.add(Box.createVerticalStrut(20));
        commentDetailsPanel.add(ratingLbl);
        commentDetailsPanel.add(Box.createVerticalStrut(8));
        commentDetailsPanel.add(ratingPanel);
        commentDetailsPanel.add(Box.createVerticalStrut(20));
        commentDetailsPanel.add(commentLbl);
        commentDetailsPanel.add(Box.createVerticalStrut(8));
        commentDetailsPanel.add(commentScroll);
        commentDetailsPanel.add(Box.createVerticalStrut(8));
        commentDetailsPanel.add(errorLbl);
        commentDetailsPanel.add(Box.createVerticalStrut(8));
        commentDetailsPanel.add(submitBtn);

        formCard.add(apptLbl);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(apptCombo);
        formCard.add(commentDetailsPanel);

        panel.add(heading,  BorderLayout.NORTH);
        panel.add(formCard, BorderLayout.CENTER);
        return panel;
    }


    //  SHARED HELPERS
    private JPanel buildRatingPanel(int[] selectedRating, JButton[] starButtons, JLabel ratingHint) {
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ratingPanel.setOpaque(false);
        ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel starsBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        starsBox.setBackground(BG_CARD2);
        starsBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        for (int i = 0; i < starButtons.length; i++) {
            final int rating = i + 1;
            JButton star = new JButton("\u2606");
            star.setFont(new Font("Dialog", Font.BOLD, 28));
            star.setForeground(STAR_YELLOW);
            star.setBackground(new Color(0, 0, 0, 0));
            star.setOpaque(false);
            star.setBorderPainted(false);
            star.setFocusPainted(false);
            star.setContentAreaFilled(false);
            star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            star.setMargin(new Insets(0, 0, 0, 0));
            star.setPreferredSize(new Dimension(38, 34));
            star.addActionListener(e -> {
                selectedRating[0] = rating;
                refreshRatingStars(starButtons, selectedRating[0], ratingHint);
            });
            starButtons[i] = star;
            starsBox.add(star);
        }

        starsBox.add(ratingHint);
        ratingPanel.add(starsBox);
        return ratingPanel;
    }

    private JLabel makeRatingHint(int rating) {
        JLabel hint = new JLabel("  " + rating + " out of 5 selected");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(TEXT_MUTED);
        return hint;
    }

    private void refreshRatingStars(JButton[] starButtons, int rating, JLabel ratingHint) {
        for (int i = 0; i < starButtons.length; i++) {
            if (starButtons[i] != null) {
                starButtons[i].setText(i < rating ? "\u2605" : "\u2606");
                starButtons[i].setForeground(STAR_YELLOW);
            }
        }
        ratingHint.setText("  " + rating + " out of 5 selected");
    }

    private int extractRating(String comment) {
        if (comment == null) return 0;
        String trimmed = comment.trim();
        int marker = trimmed.indexOf("/5");
        if (marker == 1) {
            try {
                int rating = Integer.parseInt(trimmed.substring(0, 1));
                if (rating >= 1 && rating <= 5) return rating;
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private String extractCommentBody(String comment) {
        if (comment == null) return "";
        String trimmed = comment.trim();
        int marker = trimmed.indexOf("/5");
        if (marker == 1 && extractRating(trimmed) > 0) {
            String body = trimmed.substring(marker + 2).trim();
            if (body.startsWith("-")) {
                body = body.substring(1).trim();
            }
            return body;
        }
        return comment;
    }

    private JPanel makeInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
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
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
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
            if (profileEditMode) {
                int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Discard them?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
                exitProfileEditMode();
            }
            if ("HISTORY".equals(cardName)) {
                refreshHistoryPanel();
            }
            contentLayout.show(contentPanel, cardName);
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(TEXT_PRIMARY); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(TEXT_MUTED);   }
        });
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

    private void refreshHistoryPanel() {
        contentPanel.remove(historyPanel);
        historyPanel = buildHistoryPanel();
        contentPanel.add(historyPanel, "HISTORY");
        contentPanel.revalidate();
        contentPanel.repaint();
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

    private void setFixedColumnWidth(JTable table, int columnIndex, int width) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        column.setMinWidth(width);
        column.setPreferredWidth(width);
        column.setMaxWidth(width);
    }

    private JScrollPane makeScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        return sp;
    }
}
