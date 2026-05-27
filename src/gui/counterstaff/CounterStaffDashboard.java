package gui.counterstaff;

import model.Appointment;
import model.CounterStaff;
import model.Customer;
import model.Payment;
import model.User;
import util.FileHandler;
import util.Session;
import java.time.LocalDate;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * GUI CLASS — CounterStaffDashboard
 * -----------------------------------
 * MEMBER 3 is responsible for implementing all features in this file.
 *
 * FEATURES TO IMPLEMENT:
 * [1] Edit own profile
 * [2] Create / Read / Update / Delete customers
 * [3] Create and assign new appointments
 * - Normal service = 1 hour, Major service = 3 hours
 * - Check technician availability (no time overlap)
 * [4] Collect payment and generate receipt
 */
public class CounterStaffDashboard extends JFrame {

    // COLOURS
    private static final Color BG_DARK = new Color(15, 17, 26);
    private static final Color BG_CARD = new Color(0, 0, 0);
    private static final Color BG_CARD2 = new Color(30, 34, 52);
    private static final Color ACCENT = new Color(20, 184, 166);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_MUTED = new Color(148, 151, 180);
    private static final Color BORDER_COLOR = new Color(55, 58, 80);
    private static final Color DANGER = new Color(239, 68, 68);

    // STATE
    private CounterStaff currentStaff;

    // LAYOUT
    private CardLayout contentLayout;
    private JPanel contentPanel;
    private String activeCardName = "DASHBOARD";
    private List<JButton> navButtons = new ArrayList<>();
    private JPanel dashboardPanel;

    // TOP BAR COMPONENT (stored for updating)
    private JLabel topBarUserLabel;

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

    // CONSTRUCTOR
    public CounterStaffDashboard(CounterStaff staff) {
        this.currentStaff = staff;

        setTitle("APU-ASC — Counter Staff Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);

        setContentPane(root);
    }

    // TOP BAR
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(14, 24, 14, 24)));

        JLabel title = new JLabel("APU Automotive Service Centre");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightSide.setOpaque(false);

        topBarUserLabel = new JLabel("👤  " + currentStaff.getFullName() + "  ·  Counter Staff");
        topBarUserLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        topBarUserLabel.setForeground(TEXT_MUTED);
        topBarUserLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        topBarUserLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigateToPanel("PROFILE");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                topBarUserLabel.setForeground(TEXT_PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                topBarUserLabel.setForeground(TEXT_MUTED);
            }
        });

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        logoutBtn.setForeground(DANGER);
        logoutBtn.setBackground(new Color(0, 0, 0, 0));
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
        bar.add(title, BorderLayout.WEST);
        bar.add(rightSide, BorderLayout.EAST);
        return bar;
    }

    // SIDEBAR
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

        JLabel section = new JLabel("  COUNTER STAFF MENU");
        section.setFont(new Font("SansSerif", Font.BOLD, 10));
        section.setForeground(TEXT_MUTED);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(makeNavButton("🏠  Dashboard", "DASHBOARD"));
        sidebar.add(makeNavButton("💁‍♀️  Customers", "CUSTOMERS"));
        sidebar.add(makeNavButton("📅  Appointments", "APPOINTMENTS"));
        sidebar.add(makeNavButton("💳  Collect Payment", "PAYMENTS"));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    // CONTENT PANELS
    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(BG_DARK);

        dashboardPanel = buildDashboardPanel();
        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(buildProfilePanel(), "PROFILE");
        contentPanel.add(buildCustomersPanel(), "CUSTOMERS");
        contentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");
        contentPanel.add(buildPaymentsPanel(), "PAYMENTS");

        contentLayout.show(contentPanel, "DASHBOARD");
        return contentPanel;
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.setOpaque(false);
        JLabel heading = new JLabel("Welcome, " + currentStaff.getFirstName());
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subheading = new JLabel("Here is today's summary.");
        subheading.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subheading.setForeground(TEXT_MUTED);
        subheading.setAlignmentX(Component.LEFT_ALIGNMENT);

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(4));
        headingPanel.add(subheading);
        panel.add(headingPanel, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel(new BorderLayout(0, 12));
        bodyPanel.setOpaque(false);

        String totalCustomers = "0";
        String totalAppointments = "0";
        String pendingAppointments = "0";
        String revenueCollected = "RM 0.00";

        try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
            int count = 0;
            while (br.readLine() != null) {
                count++;
            }
            totalCustomers = String.valueOf(count);
        } catch (Exception ex) {
            totalCustomers = "N/A";
        }

        try {
            List<Appointment> appointments = FileHandler.loadAllAppointments();
            totalAppointments = String.valueOf(appointments.size());
            long pending = appointments.stream()
                    .filter(a -> "Ongoing".equals(a.getStatus()))
                    .count();
            pendingAppointments = String.valueOf(pending);
        } catch (Exception ex) {
            totalAppointments = "0";
            pendingAppointments = "0";
        }

        try {
            double total = 0.0;
            for (Payment payment : FileHandler.loadAllPayments()) {
                if ("Paid".equalsIgnoreCase(payment.getStatus())) {
                    total += payment.getAmount();
                }
            }
            revenueCollected = "RM " + String.format("%.2f", total);
        } catch (Exception ex) {
            revenueCollected = "RM 0.00";
        }

        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        statsGrid.setPreferredSize(new Dimension(0, 80));
        statsGrid.add(makeStatCard("Total Customers", totalCustomers, new Color(56, 130, 246)));
        statsGrid.add(makeStatCard("Total Appointments", totalAppointments, new Color(168, 85, 247)));
        statsGrid.add(makeStatCard("Ongoing Appointments", pendingAppointments, new Color(245, 158, 11)));
        statsGrid.add(makeStatCard("Revenue Collected", revenueCollected, new Color(34, 197, 94)));

        bodyPanel.add(statsGrid, BorderLayout.NORTH);

        java.util.function.Function<String, String> customerNameLookup = customerID -> {
            try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3 && parts[0].equals(customerID)) {
                        return parts[1] + " " + parts[2];
                    }
                }
            } catch (Exception ex) {
                return customerID;
            }
            return customerID;
        };

        java.util.function.Function<DefaultTableModel, JTable> tableBuilder = model -> {
            JTable table = new JTable(model);
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    c.setBackground(row % 2 == 0 ? BG_CARD : BG_CARD2);
                    c.setForeground(TEXT_PRIMARY);
                    if (c instanceof JComponent) {
                        ((JComponent) c).setBorder(new EmptyBorder(0, 6, 0, 6));
                    }
                    return c;
                }
            });
            table.setBackground(BG_CARD);
            table.setForeground(TEXT_PRIMARY);
            table.setFont(new Font("SansSerif", Font.PLAIN, 12));
            table.setRowHeight(32);
            table.setGridColor(BORDER_COLOR);
            table.setShowVerticalLines(false);
            table.setFillsViewportHeight(true);
            JTableHeader header = table.getTableHeader();
            header.setBackground(BG_CARD2);
            header.setForeground(TEXT_MUTED);
            header.setFont(new Font("SansSerif", Font.BOLD, 12));
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
            header.setReorderingAllowed(false);
            return table;
        };

        java.util.function.Function<JTable, JScrollPane> scrollBuilder = table -> {
            int calculatedHeight = Math.min(table.getRowHeight() * (table.getRowCount() + 1) + 4, 200);
            table.setPreferredScrollableViewportSize(new Dimension(0, calculatedHeight));
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBackground(BG_CARD);
            scroll.getViewport().setBackground(BG_CARD);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
            scroll.setPreferredSize(new Dimension(0, calculatedHeight));
            return scroll;
        };

        JPanel recentAppointmentsCard = new JPanel(new BorderLayout(0, 12));
        recentAppointmentsCard.setBackground(BG_CARD);
        recentAppointmentsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT)),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel recentAppointmentsTitle = new JLabel("Recent Appointments");
        recentAppointmentsTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        recentAppointmentsTitle.setForeground(TEXT_PRIMARY);
        DefaultTableModel recentAppointmentsModel = new DefaultTableModel(
                new String[] { "Appt ID", "Customer", "Service", "Date", "Status" }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try {
            List<Appointment> appointments = new ArrayList<>(FileHandler.loadAllAppointments());
            java.util.Collections.sort(appointments, (a, b) -> b.getDate().compareTo(a.getDate()));
            int limit = Math.min(5, appointments.size());
            for (int i = 0; i < limit; i++) {
                Appointment a = appointments.get(i);
                recentAppointmentsModel.addRow(new Object[] {
                        a.getAppointmentID(),
                        customerNameLookup.apply(a.getCustomerID()),
                        a.getServiceType(),
                        a.getDate(),
                        a.getStatus()
                });
            }
        } catch (Exception ex) {
            recentAppointmentsModel.setRowCount(0);
        }
        recentAppointmentsCard.add(recentAppointmentsTitle, BorderLayout.NORTH);
        recentAppointmentsCard.add(scrollBuilder.apply(tableBuilder.apply(recentAppointmentsModel)), BorderLayout.CENTER);

        JPanel paymentsCard = new JPanel(new BorderLayout(0, 12));
        paymentsCard.setBackground(BG_CARD);
        paymentsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(34, 197, 94))),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel paymentsTitle = new JLabel("Recent Payments");
        paymentsTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        paymentsTitle.setForeground(TEXT_PRIMARY);
        DefaultTableModel paymentsModel = new DefaultTableModel(
                new String[] { "Payment ID", "Appt ID", "Amount (RM)", "Date", "Status" }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try {
            List<Payment> payments = FileHandler.loadAllPayments();
            int start = Math.max(0, payments.size() - 5);
            for (int i = payments.size() - 1; i >= start; i--) {
                Payment p = payments.get(i);
                paymentsModel.addRow(new Object[] {
                        p.getPaymentID(),
                        p.getAppointmentID(),
                        String.format("%.2f", p.getAmount()),
                        p.getDate(),
                        p.getStatus()
                });
            }
        } catch (Exception ex) {
            paymentsModel.setRowCount(0);
        }
        paymentsCard.add(paymentsTitle, BorderLayout.NORTH);
        paymentsCard.add(scrollBuilder.apply(tableBuilder.apply(paymentsModel)), BorderLayout.CENTER);

        JPanel servicePricesCard = new JPanel(new BorderLayout(0, 12));
        servicePricesCard.setBackground(BG_CARD);
        servicePricesCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(245, 158, 11))),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel servicePricesTitle = new JLabel("Service Prices");
        servicePricesTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        servicePricesTitle.setForeground(TEXT_PRIMARY);
        JPanel servicePriceContent = new JPanel(new GridLayout(2, 1, 8, 8));
        servicePriceContent.setOpaque(false);

        try {
            String[] serviceNames = { "Normal", "Major" };
            for (String serviceName : serviceNames) {
                double price = FileHandler.getServicePrice(serviceName);
                int duration = FileHandler.getServiceDuration(serviceName);

                JPanel priceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                priceRow.setOpaque(false);
                priceRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel nameLabel = new JLabel(serviceName);
                nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
                nameLabel.setForeground(TEXT_MUTED);
                JLabel priceLabel = new JLabel("RM " + String.format("%.2f", price));
                priceLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                priceLabel.setForeground(ACCENT);
                JLabel durationLabel = new JLabel("( " + duration + " hour" + (duration > 1 ? "s" : "") + " )");
                durationLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
                durationLabel.setForeground(TEXT_MUTED);
                priceRow.add(nameLabel);
                priceRow.add(priceLabel);
                priceRow.add(durationLabel);
                servicePriceContent.add(priceRow);
            }
        } catch (Exception ex) {
            JLabel fallback = new JLabel("N/A");
            fallback.setFont(new Font("SansSerif", Font.PLAIN, 13));
            fallback.setForeground(TEXT_MUTED);
            servicePriceContent.add(fallback);
        }
        servicePricesCard.add(servicePricesTitle, BorderLayout.NORTH);
        servicePricesCard.add(servicePriceContent, BorderLayout.CENTER);

        JPanel myBookingsCard = new JPanel(new BorderLayout(0, 12));
        myBookingsCard.setBackground(BG_CARD);
        myBookingsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(56, 130, 246))),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel myBookingsTitle = new JLabel("My Bookings");
        myBookingsTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        myBookingsTitle.setForeground(TEXT_PRIMARY);
        DefaultTableModel myBookingsModel = new DefaultTableModel(
                new String[] { "Appt ID", "Customer", "Date", "Status" }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try {
            List<Appointment> appointments = new ArrayList<>(FileHandler.loadAllAppointments());
            appointments.removeIf(a -> !isBookedByCurrentStaff(a));
            java.util.Collections.sort(appointments, (a, b) -> b.getDate().compareTo(a.getDate()));
            for (Appointment a : appointments) {
                myBookingsModel.addRow(new Object[] {
                        a.getAppointmentID(),
                        customerNameLookup.apply(a.getCustomerID()),
                        a.getDate(),
                        a.getStatus()
                });
            }
        } catch (Exception ex) {
            myBookingsModel.setRowCount(0);
        }
        myBookingsCard.add(myBookingsTitle, BorderLayout.NORTH);
        myBookingsCard.add(scrollBuilder.apply(tableBuilder.apply(myBookingsModel)), BorderLayout.CENTER);

        JPanel contentGrid = new JPanel(new GridLayout(2, 2, 12, 12));
        contentGrid.setOpaque(false);
        contentGrid.add(recentAppointmentsCard);
        contentGrid.add(paymentsCard);
        contentGrid.add(servicePricesCard);
        contentGrid.add(myBookingsCard);

        bodyPanel.add(contentGrid, BorderLayout.CENTER);
        panel.add(bodyPanel, BorderLayout.CENTER);
        return panel;
    }

    // PANEL 1 — MY PROFILE
    // TODO (Member 3): Allow staff to edit their own details
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
                new EmptyBorder(28, 28, 28, 28)));
        profileCard.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Recreate the profile UI in view mode
        refreshProfileUI();

        panel.add(heading, BorderLayout.NORTH);
        panel.add(profileCard, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Refreshes the profile card UI based on current edit mode state.
     * In view mode: shows labels + Edit button.
     * In edit mode: shows text fields + Done/Cancel buttons + error label.
     */
    private void refreshProfileUI() {
        profileCard.removeAll();

        if (!profileEditMode) {
            // ===== VIEW MODE =====
            profileCard.add(makeInfoRow("Full Name", currentStaff.getFullName()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Username", currentStaff.getUsername()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Email", currentStaff.getEmail()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Phone", currentStaff.getPhone()));
            profileCard.add(Box.createVerticalStrut(12));
            profileCard.add(makeInfoRow("Password", maskPassword(currentStaff.getPassword())));
            profileCard.add(Box.createVerticalStrut(24));

            JButton editBtn = makePrimaryButton("✏  Edit Profile");
            editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            editBtn.addActionListener(e -> enterProfileEditMode());
            profileCard.add(editBtn);

        } else {
            // ===== EDIT MODE =====
            // Label row: "Full Name" label on the left
            JPanel fullNameRow = new JPanel(new BorderLayout(16, 0));
            fullNameRow.setOpaque(false);
            fullNameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel fullNameLbl = new JLabel("Full Name:");
            fullNameLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            fullNameLbl.setForeground(TEXT_MUTED);
            fullNameLbl.setPreferredSize(new Dimension(100, 20));

            // Split into first and last name fields
            JPanel nameFieldsPanel = new JPanel(new GridLayout(1, 2, 12, 0));
            nameFieldsPanel.setOpaque(false);

            firstNameField = makeEditableTextField(currentStaff.getFirstName());
            lastNameField = makeEditableTextField(currentStaff.getLastName());

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

            // Username field
            JPanel usernameRow = new JPanel(new BorderLayout(16, 0));
            usernameRow.setOpaque(false);
            usernameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel usernameLbl = new JLabel("Username:");
            usernameLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            usernameLbl.setForeground(TEXT_MUTED);
            usernameLbl.setPreferredSize(new Dimension(100, 20));
            usernameField = makeLockedTextField(currentStaff.getUsername());
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

            // Email field
            JPanel emailRow = new JPanel(new BorderLayout(16, 0));
            emailRow.setOpaque(false);
            emailRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel emailLbl = new JLabel("Email:");
            emailLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            emailLbl.setForeground(TEXT_MUTED);
            emailLbl.setPreferredSize(new Dimension(100, 20));
            emailField = makeEditableTextField(currentStaff.getEmail());
            emailRow.add(emailLbl, BorderLayout.WEST);
            emailRow.add(emailField, BorderLayout.CENTER);
            profileCard.add(emailRow);
            profileCard.add(Box.createVerticalStrut(12));

            // Phone field
            JPanel phoneRow = new JPanel(new BorderLayout(16, 0));
            phoneRow.setOpaque(false);
            phoneRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel phoneLbl = new JLabel("Phone:");
            phoneLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            phoneLbl.setForeground(TEXT_MUTED);
            phoneLbl.setPreferredSize(new Dimension(100, 20));
            phoneField = makeEditableTextField(currentStaff.getPhone());
            phoneRow.add(phoneLbl, BorderLayout.WEST);
            phoneRow.add(phoneField, BorderLayout.CENTER);
            profileCard.add(phoneRow);
            profileCard.add(Box.createVerticalStrut(12));

            // Password field with eye toggle
            JPanel passwordRow = new JPanel(new BorderLayout(16, 0));
            passwordRow.setOpaque(false);
            passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            JLabel passwordLbl = new JLabel("Password:");
            passwordLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            passwordLbl.setForeground(TEXT_MUTED);
            passwordLbl.setPreferredSize(new Dimension(100, 20));

            passwordField = makeEditableTextField(maskPassword(currentStaff.getPassword()));
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
                    passwordField.setText(currentStaff.getPassword());
                } else {
                    eyeToggle.setForeground(TEXT_MUTED);
                    eyeToggle.setIcon(makeEyeIcon(TEXT_MUTED));
                    passwordField.setText(maskPassword(currentStaff.getPassword()));
                }
            });
            passwordFieldPanel.add(eyeToggle, BorderLayout.EAST);

            passwordRow.add(passwordLbl, BorderLayout.WEST);
            passwordRow.add(passwordFieldPanel, BorderLayout.CENTER);
            profileCard.add(passwordRow);
            profileCard.add(Box.createVerticalStrut(16));

            // Error message label (initially empty, shown on validation failure)
            errorMsg = new JLabel();
            errorMsg.setForeground(DANGER);
            errorMsg.setFont(new Font("SansSerif", Font.PLAIN, 12));
            errorMsg.setAlignmentX(Component.LEFT_ALIGNMENT);
            profileCard.add(errorMsg);
            profileCard.add(Box.createVerticalStrut(12));

            // Action buttons
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

    /**
     * Creates an editable JTextField with the given value,
     * styled with border and colors as specified.
     */
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

    /**
     * Enter edit mode: clear the error message and refresh UI.
     */
    private void enterProfileEditMode() {
        profileEditMode = true;
        passwordVisible = false;
        if (errorMsg != null)
            errorMsg.setText("");
        refreshProfileUI();
    }

    /**
     * Exit edit mode without saving: discard text fields and return to view.
     */
    private void exitProfileEditMode() {
        profileEditMode = false;
        passwordVisible = false;
        if (errorMsg != null)
            errorMsg.setText("");
        refreshProfileUI();
    }

    /**
     * Creates a masked password string with asterisks.
     * One asterisk per character.
     */
    private String maskPassword(String password) {
        return "*".repeat(password.length());
    }

    /**
     * Validate profile form and save if valid.
     */
    private void onProfileSave() {
        // Validate: no field should be empty
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        // Get password (it might be masked, so use the original from currentStaff if
        // masked)
        String password = passwordVisible ? passwordField.getText().trim() : currentStaff.getPassword();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorMsg.setText("❌ All fields are required.");
            profileCard.revalidate();
            profileCard.repaint();
            return;
        }

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errorMsg.setText("Error: Please enter a valid email address, for example gg@gmail.com.");
            profileCard.revalidate();
            profileCard.repaint();
            return;
        }

        if (!phone.matches("\\d{10}")) {
            errorMsg.setText("Error: Phone must contain exactly 10 digits.");
            profileCard.revalidate();
            profileCard.repaint();
            return;
        }

        // Load all users from file
        List<User> users = FileHandler.loadAllUsers();

        // Find the current user in the list by userID
        User userToUpdate = null;
        for (User u : users) {
            if (u.getUserID().equals(currentStaff.getUserID())) {
                userToUpdate = u;
                break;
            }
        }

        if (userToUpdate == null) {
            errorMsg.setText("❌ Error: User not found in database.");
            profileCard.revalidate();
            profileCard.repaint();
            return;
        }

        // Update the user's details
        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);
        userToUpdate.setEmail(email);
        userToUpdate.setPhone(phone);
        // Update password if it was changed (not masked)
        if (passwordVisible) {
            userToUpdate.setPassword(password);
        }

        // Save the full list back to file
        FileHandler.saveAllUsers(users);

        // Update the currentStaff object in memory
        currentStaff.setFirstName(firstName);
        currentStaff.setLastName(lastName);
        currentStaff.setEmail(email);
        currentStaff.setPhone(phone);
        // Update password if it was changed (not masked)
        if (passwordVisible) {
            currentStaff.setPassword(password);
        }

        // Exit edit mode and show view mode with updated values
        profileEditMode = false;
        passwordVisible = false;
        errorMsg.setText("");
        refreshProfileUI();

        // Update the top bar user label
        updateTopBarLabel();
    }

    /**
     * Updates the top bar user label to reflect the latest name.
     */
    private void updateTopBarLabel() {
        if (topBarUserLabel != null) {
            topBarUserLabel.setText("👤  " + currentStaff.getFullName() + "  ·  Counter Staff");
        }
    }

    // PANEL 2 — CUSTOMERS
    private JPanel buildCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // --- Header row ---
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel heading = new JLabel("Manage Customers");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        JButton addBtn = makePrimaryButton("+ Add Customer");
        headerRow.add(heading, BorderLayout.WEST);
        headerRow.add(addBtn, BorderLayout.EAST);

        // --- Table ---
        String[] cols = { "Customer ID", "First Name", "Last Name", "Email", "Phone" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        refreshCustomersTable(model);

        JTable table = makeStyledTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = makeScrollPane(table);

        // --- Action row ---
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        JButton editBtn = makeSecondaryButton("✏ Edit");
        JButton deleteBtn = makeSecondaryButton("🗑 Delete");
        deleteBtn.setForeground(DANGER);

        // Buttons disabled until a row is selected
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean selected = table.getSelectedRow() != -1;
                editBtn.setEnabled(selected);
                deleteBtn.setEnabled(selected);
            }
        });

        // --- Button actions ---
        addBtn.addActionListener(e -> showAddCustomerDialog(model));

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String custId    = (String) model.getValueAt(row, 0);
            String firstName = (String) model.getValueAt(row, 1);
            String lastName  = (String) model.getValueAt(row, 2);
            String email     = (String) model.getValueAt(row, 3);
            String phone     = (String) model.getValueAt(row, 4);
            showEditCustomerDialog(model, custId, firstName, lastName, email, phone);
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String custId = (String) model.getValueAt(row, 0);
            deleteCustomer(model, custId);
        });

        actionRow.add(editBtn);
        actionRow.add(deleteBtn);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Reloads the customers table from customers.txt.
     */
    private void refreshCustomersTable(DefaultTableModel model) {
        model.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 6) continue;
                // p[0]=CustID, p[1]=FirstName, p[2]=LastName, p[3]=Email, p[4]=Phone, p[5]=UserID
                model.addRow(new Object[] { p[0], p[1], p[2], p[3], p[4] });
            }
        } catch (IOException ex) {
            System.err.println("Error reading customers.txt: " + ex.getMessage());
        }
    }

    /**
     * Generates the next Customer ID (e.g. C002 if C001 exists).
     */
    private String generateNextCustomerID() {
        int max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                try {
                    int num = Integer.parseInt(p[0].substring(1));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException ignored) {}
        return String.format("C%03d", max + 1);
    }

    /**
     * Opens a modal dialog to add a new customer.
     */
    private void showAddCustomerDialog(DefaultTableModel tableModel) {
        JDialog dialog = new JDialog(this, "Add Customer", true);
        dialog.setSize(420, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_CARD);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = new JLabel("New Customer");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(16));

        // Fields
        JTextField fFirstName = makeEditableTextField("");
        JTextField fLastName  = makeEditableTextField("");
        JTextField fEmail     = makeEditableTextField("");
        JTextField fPhone     = makeEditableTextField("");
        JTextField fUsername  = makeEditableTextField("");
        JTextField fPassword  = makeEditableTextField("");

        content.add(makeDialogFieldRow("First Name", fFirstName));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogFieldRow("Last Name",  fLastName));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogFieldRow("Email",      fEmail));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogFieldRow("Phone",      fPhone));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogFieldRow("Username",   fUsername));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogFieldRow("Password",   fPassword));
        content.add(Box.createVerticalStrut(12));

        // Error label
        JLabel errLbl = new JLabel(" ");
        errLbl.setForeground(DANGER);
        errLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(errLbl);
        content.add(Box.createVerticalStrut(8));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton saveBtn   = makePrimaryButton("Save");
        JButton cancelBtn = makeSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);
        content.add(btnRow);

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String fn = fFirstName.getText().trim();
            String ln = fLastName.getText().trim();
            String em = fEmail.getText().trim();
            String ph = fPhone.getText().trim();
            String un = fUsername.getText().trim();
            String pw = fPassword.getText().trim();

            if (fn.isEmpty() || ln.isEmpty() || em.isEmpty() || ph.isEmpty()
                    || un.isEmpty() || pw.isEmpty()) {
                errLbl.setText("All fields are required.");
                return;
            }
            if (!em.contains("@")) {
                errLbl.setText("Invalid email. Email must contain '@'.");
                return;
            }

            // Check username uniqueness
            List<User> users = FileHandler.loadAllUsers();
            for (User u : users) {
                if (u.getUsername().equalsIgnoreCase(un)) {
                    errLbl.setText("❌ Username already exists.");
                    return;
                }
            }

            // Generate IDs
            String newUserID = FileHandler.generateNextUserID();
            String newCustID = generateNextCustomerID();

            // Create Customer user and add to users list, then save
            Customer newUser = new Customer(newUserID, un, pw, fn, ln, em, ph);
            users.add(newUser);
            FileHandler.saveAllUsers(users);

            // Append to customers.txt
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/data/customers.txt", true))) {
                bw.write(newCustID + "|" + fn + "|" + ln + "|" + em + "|" + ph + "|" + newUserID);
                bw.newLine();
            } catch (IOException ex) {
                System.err.println("Error writing customers.txt: " + ex.getMessage());
            }

            refreshCustomersTable(tableModel);
            dialog.dispose();
        });

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    /**
     * Opens a modal dialog to edit an existing customer.
     */
    private void showEditCustomerDialog(DefaultTableModel tableModel,
                                        String custId, String firstName,
                                        String lastName, String email, String phone) {
        JDialog dialog = new JDialog(this, "Edit Customer", true);
        dialog.setSize(420, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_CARD);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = new JLabel("Edit Customer — " + custId);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(16));

        JTextField fFirstName = makeEditableTextField(firstName);
        JTextField fLastName  = makeEditableTextField(lastName);
        JTextField fEmail     = makeEditableTextField(email);
        JTextField fPhone     = makeEditableTextField(phone);

        content.add(makeDialogFieldRow("First Name", fFirstName));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogFieldRow("Last Name",  fLastName));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogFieldRow("Email",      fEmail));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogFieldRow("Phone",      fPhone));
        content.add(Box.createVerticalStrut(12));

        JLabel errLbl = new JLabel(" ");
        errLbl.setForeground(DANGER);
        errLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(errLbl);
        content.add(Box.createVerticalStrut(8));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton saveBtn   = makePrimaryButton("Save");
        JButton cancelBtn = makeSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);
        content.add(btnRow);

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String fn = fFirstName.getText().trim();
            String ln = fLastName.getText().trim();
            String em = fEmail.getText().trim();
            String ph = fPhone.getText().trim();

            if (fn.isEmpty() || ln.isEmpty() || em.isEmpty() || ph.isEmpty()) {
                errLbl.setText("❌ All fields are required.");
                return;
            }

            // Find the userID linked to this customer from customers.txt
            String linkedUserID = null;
            try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] p = line.split("\\|");
                    if (p.length >= 6 && p[0].equals(custId)) {
                        linkedUserID = p[5];
                        break;
                    }
                }
            } catch (IOException ex) {
                System.err.println("Error reading customers.txt: " + ex.getMessage());
            }

            if (linkedUserID == null) {
                errLbl.setText("❌ Customer record not found.");
                return;
            }

            // Update users.txt via FileHandler
            List<User> users = FileHandler.loadAllUsers();
            for (User u : users) {
                if (u.getUserID().equals(linkedUserID)) {
                    u.setFirstName(fn);
                    u.setLastName(ln);
                    u.setEmail(em);
                    u.setPhone(ph);
                    break;
                }
            }
            FileHandler.saveAllUsers(users);

            // Update customers.txt
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] p = line.split("\\|");
                    if (p.length >= 6 && p[0].equals(custId)) {
                        lines.add(custId + "|" + fn + "|" + ln + "|" + em + "|" + ph + "|" + linkedUserID);
                    } else {
                        lines.add(line);
                    }
                }
            } catch (IOException ex) {
                System.err.println("Error reading customers.txt: " + ex.getMessage());
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/data/customers.txt"))) {
                for (String l : lines) {
                    bw.write(l);
                    bw.newLine();
                }
            } catch (IOException ex) {
                System.err.println("Error writing customers.txt: " + ex.getMessage());
            }

            refreshCustomersTable(tableModel);
            dialog.dispose();
        });

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    /**
     * Deletes a customer after a confirmation dialog.
     */
    private void deleteCustomer(DefaultTableModel tableModel, String custId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete customer " + custId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Find the linked userID from customers.txt
        String linkedUserID = null;
        List<String> remainingLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 6 && p[0].equals(custId)) {
                    linkedUserID = p[5];
                    // Skip this line (delete it)
                } else {
                    remainingLines.add(line);
                }
            }
        } catch (IOException ex) {
            System.err.println("Error reading customers.txt: " + ex.getMessage());
        }

        // Rewrite customers.txt without the deleted line
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/data/customers.txt"))) {
            for (String l : remainingLines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException ex) {
            System.err.println("Error writing customers.txt: " + ex.getMessage());
        }

        // Remove user from users.txt
        final String userIDToRemove = linkedUserID;
        if (userIDToRemove != null) {
            List<User> users = FileHandler.loadAllUsers();
            users.removeIf(u -> u.getUserID().equals(userIDToRemove));
            FileHandler.saveAllUsers(users);
        }

        refreshCustomersTable(tableModel);
    }

    /**
     * Creates a label + field row for dialogs.
     */
    private JPanel makeDialogFieldRow(String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(90, 20));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // PANEL 3 — APPOINTMENTS
    private JPanel buildAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // --- Header row ---
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel heading = new JLabel("Appointments");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        JButton newBtn = makePrimaryButton("+ New Appointment");
        headerRow.add(heading, BorderLayout.WEST);
        headerRow.add(newBtn, BorderLayout.EAST);

        // --- Table ---
        String[] cols = { "Appt ID", "Customer Name", "Technician Name", "Date", "Time", "Service Type", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        refreshAppointmentsTable(model);

        JTable table = makeStyledTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = makeScrollPane(table);

        // --- Button action ---
        newBtn.addActionListener(e -> showNewAppointmentDialog(model));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;
                int modelRow = table.convertRowIndexToModel(row);
                String apptID = (String) model.getValueAt(modelRow, 0);
                showEditAppointmentDialog(model, apptID);
            }
        });

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Reloads the appointments table, resolving customer/technician IDs to names.
     */
    private void refreshAppointmentsTable(DefaultTableModel model) {
        model.setRowCount(0);

        // Build customer name lookup from customers.txt
        java.util.Map<String, String> customerNames = new java.util.HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 3) {
                    customerNames.put(p[0], p[1] + " " + p[2]);
                }
            }
        } catch (IOException ex) {
            System.err.println("Error reading customers.txt: " + ex.getMessage());
        }

        for (Appointment a : FileHandler.loadAllAppointments()) {
            String custName = customerNames.getOrDefault(a.getCustomerID(), a.getCustomerID());
            String techName = resolveTechnicianDisplayName(a.getTechnicianID());
            model.addRow(new Object[] {
                    a.getAppointmentID(), custName, techName,
                    a.getDate(), a.getTime(), a.getServiceType(), a.getStatus()
            });
        }
    }

    /**
     * Opens a modal dialog to create a new appointment.
     */
    private void showNewAppointmentDialog(DefaultTableModel tableModel) {
        JDialog dialog = new JDialog(this, "New Appointment", true);
        dialog.setSize(460, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_CARD);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = new JLabel("New Appointment");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(16));

        // --- Load customers from customers.txt ---
        List<String[]> customerData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 3) {
                    customerData.add(new String[] { p[0], p[1] + " " + p[2] });
                }
            }
        } catch (IOException ex) {
            System.err.println("Error reading customers.txt: " + ex.getMessage());
        }

        JComboBox<String> customerCombo = new JComboBox<>();
        for (String[] cd : customerData) {
            customerCombo.addItem(cd[1]);
        }
        styleComboBox(customerCombo);

        // --- Service type combo ---
        JComboBox<String> serviceCombo = new JComboBox<>(new String[] { "Normal", "Major" });
        styleComboBox(serviceCombo);

        // --- Date field with placeholder ---
        JTextField dateField = makeEditableTextField("");
        dateField.setText("YYYY-MM-DD");
        dateField.setForeground(TEXT_MUTED);
        dateField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (dateField.getText().equals("YYYY-MM-DD")) {
                    dateField.setText("");
                    dateField.setForeground(TEXT_PRIMARY);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (dateField.getText().trim().isEmpty()) {
                    dateField.setText("YYYY-MM-DD");
                    dateField.setForeground(TEXT_MUTED);
                }
            }
        });

        // --- Time slot combo ---
        JComboBox<String> timeCombo = new JComboBox<>();
        for (int hour = 8; hour <= 17; hour++) {
            timeCombo.addItem(String.format("%02d:00", hour));
            if (hour < 17) {
                timeCombo.addItem(String.format("%02d:30", hour));
            }
        }
        styleComboBox(timeCombo);

        // --- Technician combo ---
        JComboBox<String> techCombo = new JComboBox<>();
        styleComboBox(techCombo);

        List<User> allTechnicians = new ArrayList<>();
        for (User u : FileHandler.loadAllUsers()) {
            if ("Technician".equals(u.getRole())) {
                allTechnicians.add(u);
            }
        }

        // Parallel list tracks the userID of each item in techCombo
        List<String> availableTechIDs = new ArrayList<>();

        Runnable refreshTechs = () -> {
            techCombo.removeAllItems();
            availableTechIDs.clear();
            String d = dateField.getText().trim();
            String t = (String) timeCombo.getSelectedItem();
            String s = (String) serviceCombo.getSelectedItem();

            boolean validInput = !d.isEmpty() && !t.isEmpty()
                    && !d.equals("YYYY-MM-DD")
                    && d.matches("\\d{4}-\\d{2}-\\d{2}")
                    && t.matches("\\d{2}:\\d{2}");

            for (User tech : allTechnicians) {
                if (!validInput || isTechnicianAvailable(tech.getUserID(), d, t, s)) {
                    techCombo.addItem(tech.getFullName());
                    availableTechIDs.add(tech.getUserID());
                }
            }
        };
        refreshTechs.run();

        // Refresh technicians when date, time, or service type changes
        javax.swing.event.DocumentListener docListener = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshTechs.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshTechs.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshTechs.run(); }
        };
        dateField.getDocument().addDocumentListener(docListener);
        timeCombo.addActionListener(e -> refreshTechs.run());
        serviceCombo.addActionListener(e -> refreshTechs.run());

        // --- Build rows ---
        content.add(makeDialogComboRow("Customer", customerCombo));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogComboRow("Service Type", serviceCombo));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogFieldRow("Date", dateField));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogComboRow("Time", timeCombo));

        JButton suggestSlotBtn = makeSecondaryButton("⚡ Suggest Next Available Slot");
        suggestSlotBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        suggestSlotBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        suggestSlotBtn.addActionListener(e -> {
            List<Appointment> allAppointments = FileHandler.loadAllAppointments();

            Set<String> bookedDates = allAppointments.stream()
                    .map(Appointment::getDate)
                    .collect(Collectors.toSet());

            LocalDate checkDate = LocalDate.now().plusDays(1);

            while (bookedDates.contains(checkDate.toString())) {
                checkDate = checkDate.plusDays(1);
            }

            dateField.setText(checkDate.toString());
            dateField.setForeground(TEXT_PRIMARY);
            timeCombo.setSelectedItem("09:00");
            refreshTechs.run();

            JOptionPane.showMessageDialog(dialog,
                    "Next available date: " + checkDate.toString() + " at 09:00 AM\n" +
                    "This date has no appointments scheduled.\n" +
                    "You can modify if needed.",
                    "Slot Suggested",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        content.add(Box.createVerticalStrut(12));
        content.add(suggestSlotBtn);
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogComboRow("Technician", techCombo));
        content.add(Box.createVerticalStrut(12));

        // Error label
        JLabel errLbl = new JLabel(" ");
        errLbl.setForeground(DANGER);
        errLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(errLbl);
        content.add(Box.createVerticalStrut(8));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton saveBtn = makePrimaryButton("Save");
        JButton cancelBtn = makeSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);
        content.add(btnRow);

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            int custIdx = customerCombo.getSelectedIndex();
            if (custIdx < 0) { errLbl.setText("❌ Please select a customer."); return; }

            String date = dateField.getText().trim();
            String time = (String) timeCombo.getSelectedItem();

            if (date.isEmpty() || date.equals("YYYY-MM-DD")) {
                errLbl.setText("❌ Please enter a date (YYYY-MM-DD)."); return;
            }
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                errLbl.setText("❌ Date must be in YYYY-MM-DD format."); return;
            }
            if (time == null || time.isEmpty()) {
                errLbl.setText("❌ Please enter a time (HH:MM)."); return;
            }
            if (!time.matches("\\d{2}:\\d{2}")) {
                errLbl.setText("❌ Time must be in HH:MM format."); return;
            }
            if (timeToMinutes(time) < 0) {
                errLbl.setText("❌ Time must be a valid 24-hour time."); return;
            }

            int techIdx = techCombo.getSelectedIndex();
            if (techIdx < 0) {
                errLbl.setText("❌ No available technician for this slot."); return;
            }

            String serviceType = (String) serviceCombo.getSelectedItem();
            String customerID = customerData.get(custIdx)[0];
            String technicianUserID = availableTechIDs.get(techIdx);
            String technicianID = FileHandler.getTechnicianIDByUserID(technicianUserID);
            if (technicianID == null) {
                errLbl.setText("❌ Could not resolve technician ID.");
                return;
            }

            String apptID = FileHandler.generateNextAppointmentID();
            Appointment newAppt = new Appointment(apptID, customerID, technicianID,
                    date, time, serviceType, "Ongoing", currentStaff.getFullName());

            List<Appointment> appointments = FileHandler.loadAllAppointments();
            appointments.add(newAppt);
            FileHandler.saveAllAppointments(appointments);

            double serviceFee = FileHandler.getServicePrice(serviceType);
            List<Payment> payments = FileHandler.loadAllPayments();
            payments.add(new Payment(FileHandler.generateNextPaymentID(), apptID, serviceFee, date, "Pending"));
            FileHandler.saveAllPayments(payments);

            refreshAppointmentsTable(tableModel);
            dialog.dispose();
        });

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    /**
     * Checks whether a technician has no overlapping appointment on the given date/time.
     */
    private boolean isBookedByCurrentStaff(Appointment appointment) {
        String stored = appointment.getCounterStaffID();
        return currentStaff.getUserID().equals(stored)
                || currentStaff.getFullName().equals(stored);
    }

    private String resolveTechnicianDisplayName(String storedTechnicianRef) {
        if (storedTechnicianRef == null || storedTechnicianRef.isEmpty()) {
            return "Unassigned";
        }
        if (storedTechnicianRef.startsWith("T")) {
            String name = FileHandler.getTechnicianNameByID(storedTechnicianRef);
            if (name != null) {
                return name;
            }
        }
        for (User u : FileHandler.loadAllUsers()) {
            if ("Technician".equals(u.getRole()) && u.getUserID().equals(storedTechnicianRef)) {
                return u.getFullName();
            }
        }
        return "Unassigned";
    }

    private boolean isAppointmentForTechnicianUser(Appointment appointment, String technicianUserID) {
        String stored = appointment.getTechnicianID();
        if (technicianUserID.equals(stored)) {
            return true;
        }
        String technicianID = FileHandler.getTechnicianIDByUserID(technicianUserID);
        return technicianID != null && technicianID.equals(stored);
    }

    private boolean isTechnicianAvailable(String technicianUserID, String date, String time, String serviceType) {
        return isTechnicianAvailable(technicianUserID, date, time, serviceType, null);
    }

    private boolean isTechnicianAvailable(String technicianUserID, String date, String time, String serviceType, String ignoredAppointmentID) {
        int newDuration = FileHandler.getServiceDuration(serviceType);
        int newStart = timeToMinutes(time);
        if (newStart < 0) return false;
        int newEnd = newStart + (newDuration * 60);

        for (Appointment a : FileHandler.loadAllAppointments()) {
            if (ignoredAppointmentID != null && ignoredAppointmentID.equals(a.getAppointmentID())) continue;
            if (!isAppointmentForTechnicianUser(a, technicianUserID)) continue;
            if (!a.getDate().equals(date)) continue;

            int existStart = timeToMinutes(a.getTime());
            if (existStart < 0) continue;
            int existEnd = existStart + (FileHandler.getServiceDuration(a.getServiceType()) * 60);

            // Overlap: startA < endB && startB < endA
            if (newStart < existEnd && existStart < newEnd) {
                return false;
            }
        }
        return true;
    }

    private void showEditAppointmentDialog(DefaultTableModel tableModel, String appointmentID) {
        Appointment target = null;
        for (Appointment a : FileHandler.loadAllAppointments()) {
            if (a.getAppointmentID().equals(appointmentID)) {
                target = a;
                break;
            }
        }
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Appointment not found.");
            refreshAppointmentsTable(tableModel);
            return;
        }

        Appointment original = target;
        JDialog dialog = new JDialog(this, "Edit Appointment", true);
        dialog.setSize(460, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_CARD);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = new JLabel("Edit Appointment");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(16));

        List<String[]> customerData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 3) {
                    customerData.add(new String[] { p[0], p[1] + " " + p[2] });
                }
            }
        } catch (IOException ex) {
            System.err.println("Error reading customers.txt: " + ex.getMessage());
        }

        JComboBox<String> customerCombo = new JComboBox<>();
        int selectedCustomerIndex = -1;
        for (int i = 0; i < customerData.size(); i++) {
            customerCombo.addItem(customerData.get(i)[1]);
            if (customerData.get(i)[0].equals(original.getCustomerID())) {
                selectedCustomerIndex = i;
            }
        }
        if (selectedCustomerIndex >= 0) {
            customerCombo.setSelectedIndex(selectedCustomerIndex);
        }
        styleComboBox(customerCombo);

        JComboBox<String> serviceCombo = new JComboBox<>(new String[] { "Normal", "Major" });
        serviceCombo.setSelectedItem(original.getServiceType());
        styleComboBox(serviceCombo);

        JTextField dateField = makeEditableTextField(original.getDate());
        dateField.setForeground(TEXT_PRIMARY);

        JComboBox<String> timeCombo = new JComboBox<>();
        for (int hour = 8; hour <= 17; hour++) {
            timeCombo.addItem(String.format("%02d:00", hour));
            if (hour < 17) {
                timeCombo.addItem(String.format("%02d:30", hour));
            }
        }
        timeCombo.setSelectedItem(original.getTime());
        styleComboBox(timeCombo);

        JComboBox<String> techCombo = new JComboBox<>();
        styleComboBox(techCombo);

        List<User> allTechnicians = new ArrayList<>();
        for (User u : FileHandler.loadAllUsers()) {
            if ("Technician".equals(u.getRole())) {
                allTechnicians.add(u);
            }
        }

        List<String> availableTechIDs = new ArrayList<>();
        Runnable refreshTechs = () -> {
            String selectedTechUserID = null;
            int selectedIndex = techCombo.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < availableTechIDs.size()) {
                selectedTechUserID = availableTechIDs.get(selectedIndex);
            }
            if (selectedTechUserID == null) {
                selectedTechUserID = FileHandler.getUserIDByTechnicianID(original.getTechnicianID());
            }

            techCombo.removeAllItems();
            availableTechIDs.clear();
            String d = dateField.getText().trim();
            String t = (String) timeCombo.getSelectedItem();
            String s = (String) serviceCombo.getSelectedItem();

            boolean validInput = !d.isEmpty()
                    && d.matches("\\d{4}-\\d{2}-\\d{2}")
                    && t != null
                    && t.matches("\\d{2}:\\d{2}");

            int nextSelectedIndex = -1;
            for (User tech : allTechnicians) {
                if (!validInput || isTechnicianAvailable(tech.getUserID(), d, t, s, appointmentID)) {
                    techCombo.addItem(tech.getFullName());
                    availableTechIDs.add(tech.getUserID());
                    if (tech.getUserID().equals(selectedTechUserID)) {
                        nextSelectedIndex = availableTechIDs.size() - 1;
                    }
                }
            }
            if (nextSelectedIndex >= 0) {
                techCombo.setSelectedIndex(nextSelectedIndex);
            }
        };
        refreshTechs.run();

        javax.swing.event.DocumentListener docListener = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshTechs.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshTechs.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshTechs.run(); }
        };
        dateField.getDocument().addDocumentListener(docListener);
        timeCombo.addActionListener(e -> refreshTechs.run());
        serviceCombo.addActionListener(e -> refreshTechs.run());

        content.add(makeDialogComboRow("Customer", customerCombo));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogComboRow("Service Type", serviceCombo));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogFieldRow("Date", dateField));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogComboRow("Time", timeCombo));
        content.add(Box.createVerticalStrut(8));
        content.add(makeDialogComboRow("Technician", techCombo));
        content.add(Box.createVerticalStrut(12));

        JLabel errLbl = new JLabel(" ");
        errLbl.setForeground(DANGER);
        errLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(errLbl);
        content.add(Box.createVerticalStrut(8));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton saveBtn = makePrimaryButton("Save Changes");
        JButton deleteBtn = makeSecondaryButton("Delete");
        deleteBtn.setForeground(DANGER);
        JButton cancelBtn = makeSecondaryButton("Cancel");
        btnRow.add(saveBtn);
        btnRow.add(deleteBtn);
        btnRow.add(cancelBtn);
        content.add(btnRow);

        cancelBtn.addActionListener(e -> dialog.dispose());

        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Delete appointment " + appointmentID + "?",
                    "Delete Appointment",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            List<Appointment> appointments = FileHandler.loadAllAppointments();
            appointments.removeIf(a -> a.getAppointmentID().equals(appointmentID));
            FileHandler.saveAllAppointments(appointments);

            List<Payment> payments = FileHandler.loadAllPayments();
            payments.removeIf(p -> p.getAppointmentID().equals(appointmentID));
            FileHandler.saveAllPayments(payments);

            refreshAppointmentsTable(tableModel);
            dialog.dispose();
        });

        saveBtn.addActionListener(e -> {
            int custIdx = customerCombo.getSelectedIndex();
            if (custIdx < 0) { errLbl.setText("Please select a customer."); return; }

            String date = dateField.getText().trim();
            String time = (String) timeCombo.getSelectedItem();

            if (date.isEmpty()) {
                errLbl.setText("Please enter a date (YYYY-MM-DD)."); return;
            }
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                errLbl.setText("Date must be in YYYY-MM-DD format."); return;
            }
            if (time == null || time.isEmpty()) {
                errLbl.setText("Please enter a time (HH:MM)."); return;
            }
            if (!time.matches("\\d{2}:\\d{2}") || timeToMinutes(time) < 0) {
                errLbl.setText("Time must be a valid HH:MM time."); return;
            }

            int techIdx = techCombo.getSelectedIndex();
            if (techIdx < 0) {
                errLbl.setText("No available technician for this slot."); return;
            }

            String serviceType = (String) serviceCombo.getSelectedItem();
            String technicianUserID = availableTechIDs.get(techIdx);
            String technicianID = FileHandler.getTechnicianIDByUserID(technicianUserID);
            if (technicianID == null) {
                errLbl.setText("Could not resolve technician ID.");
                return;
            }

            List<Appointment> appointments = FileHandler.loadAllAppointments();
            for (Appointment a : appointments) {
                if (a.getAppointmentID().equals(appointmentID)) {
                    a.setCustomerID(customerData.get(custIdx)[0]);
                    a.setTechnicianID(technicianID);
                    a.setDate(date);
                    a.setTime(time);
                    a.setServiceType(serviceType);
                    break;
                }
            }
            FileHandler.saveAllAppointments(appointments);

            double serviceFee = FileHandler.getServicePrice(serviceType);
            List<Payment> payments = FileHandler.loadAllPayments();
            boolean paymentFound = false;
            for (Payment payment : payments) {
                if (payment.getAppointmentID().equals(appointmentID)) {
                    payment.setAmount(serviceFee);
                    payment.setDate(date);
                    paymentFound = true;
                    break;
                }
            }
            if (!paymentFound) {
                payments.add(new Payment(FileHandler.generateNextPaymentID(), appointmentID, serviceFee, date, "Pending"));
            }
            FileHandler.saveAllPayments(payments);

            refreshAppointmentsTable(tableModel);
            dialog.dispose();
        });

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    /**
     * Converts "HH:MM" to total minutes since midnight.
     */
    private int timeToMinutes(String time) {
        try {
            String[] parts = time.split(":");
            if (parts.length != 2) return -1;

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return -1;

            return hour * 60 + minute;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Styles a JComboBox to match the dark theme.
     */
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setBackground(BG_CARD2);
        combo.setForeground(TEXT_PRIMARY);
        combo.setMaximumSize(new Dimension(200, 28));
        combo.setPreferredSize(new Dimension(200, 28));
    }

    /**
     * Creates a label + combo box row for dialogs.
     */
    private JPanel makeDialogComboRow(String label, JComboBox<?> combo) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(90, 20));
        row.add(lbl, BorderLayout.WEST);
        row.add(combo, BorderLayout.CENTER);
        return row;
    }

    // PANEL 4 — COLLECT PAYMENT
    private JPanel buildPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Collect Payment");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        String[] cols = { "Payment ID", "Appointment ID", "Customer Name", "Service Type", "Date", "Amount (RM)", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        refreshPaymentsTable(model);

        JTable table = makeStyledTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = makeScrollPane(table);

        JButton payBtn = makePrimaryButton("💳  Collect & Generate Receipt");
        payBtn.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    String status = (String) table.getValueAt(row, 6);
                    payBtn.setEnabled(!"Paid".equalsIgnoreCase(status));
                } else {
                    payBtn.setEnabled(false);
                }
            }
        });

        payBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String apptID = (String) table.getValueAt(row, 1);
                String customerName = (String) table.getValueAt(row, 2);
                String serviceType = (String) table.getValueAt(row, 3);
                double amount = Double.parseDouble(table.getValueAt(row, 5).toString());
                Appointment selectedAppointment = null;
                for (Appointment a : FileHandler.loadAllAppointments()) {
                    if (a.getAppointmentID().equals(apptID)) {
                        selectedAppointment = a;
                        break;
                    }
                }
                if (selectedAppointment == null || !"Completed".equalsIgnoreCase(selectedAppointment.getStatus())) {
                    JOptionPane.showMessageDialog(this,
                            "Payment can only be collected after the technician completes the appointment.",
                            "Appointment Not Completed",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                showCollectPaymentDialog(model, apptID, customerName, serviceType, amount);
            }
        });

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(payBtn, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Reloads the payments table from payments.txt and adds unpaid Completed appointments.
     */
    private void refreshPaymentsTable(DefaultTableModel model) {
        model.setRowCount(0);

        // Build customer name lookup from customers.txt
        java.util.Map<String, String> custNames = new java.util.HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/data/customers.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 3) {
                    custNames.put(p[0], p[1] + " " + p[2]);
                }
            }
        } catch (IOException ex) {
            System.err.println("Error reading customers.txt: " + ex.getMessage());
        }

        List<Appointment> allAppts = FileHandler.loadAllAppointments();
        List<Payment> allPayments = FileHandler.loadAllPayments();
        Set<String> paymentAppointmentIDs = allPayments.stream()
                .map(Payment::getAppointmentID)
                .collect(Collectors.toSet());

        // 1. Load data from Payments
        for (Payment p : allPayments) {
            Appointment match = null;
            for (Appointment a : allAppts) {
                if (a.getAppointmentID().equals(p.getAppointmentID())) {
                    match = a;
                    break;
                }
            }
            String cName = (match != null) ? custNames.getOrDefault(match.getCustomerID(), match.getCustomerID()) : "-";
            String sType = (match != null) ? match.getServiceType() : "-";
            model.addRow(new Object[] {
                    p.getPaymentID(), p.getAppointmentID(), cName, sType,
                    p.getDate(), String.format("%.2f", p.getAmount()), p.getStatus()
            });
        }

        // 2. Load Completed (but not paid) appointments
        for (Appointment a : allAppts) {
            if ("Completed".equalsIgnoreCase(a.getStatus())
                    && !paymentAppointmentIDs.contains(a.getAppointmentID())) {
                String cName = custNames.getOrDefault(a.getCustomerID(), a.getCustomerID());
                double amt = FileHandler.getServicePrice(a.getServiceType());
                model.addRow(new Object[] {
                        "-", a.getAppointmentID(), cName, a.getServiceType(),
                        a.getDate(), String.format("%.2f", amt), "Completed"
                });
            }
        }
    }

    /**
     * Shows a dialog to confirm payment collection.
     */
    private void showCollectPaymentDialog(DefaultTableModel model, String apptID, String customerName, String serviceType, double amount) {
        JDialog dialog = new JDialog(this, "Collect Payment", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_CARD);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = new JLabel("Confirm Payment");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(16));

        content.add(makeInfoRow("Customer Name", customerName));
        content.add(Box.createVerticalStrut(8));
        content.add(makeInfoRow("Appointment ID", apptID));
        content.add(Box.createVerticalStrut(8));
        content.add(makeInfoRow("Service Type", serviceType));
        content.add(Box.createVerticalStrut(8));
        content.add(makeInfoRow("Amount", String.format("RM %.2f", amount)));
        content.add(Box.createVerticalStrut(24));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton confirmBtn = makePrimaryButton("Confirm Payment");
        JButton cancelBtn = makeSecondaryButton("Cancel");
        btnRow.add(confirmBtn);
        btnRow.add(cancelBtn);
        content.add(btnRow);

        cancelBtn.addActionListener(e -> dialog.dispose());

        confirmBtn.addActionListener(e -> {
            String date = LocalDate.now().toString();
            String paymentID = null;

            List<Payment> payments = FileHandler.loadAllPayments();
            boolean existingPaymentUpdated = false;
            for (Payment payment : payments) {
                if (payment.getAppointmentID().equals(apptID)) {
                    paymentID = payment.getPaymentID();
                    payment.setAmount(amount);
                    payment.setDate(date);
                    payment.setStatus("Paid");
                    existingPaymentUpdated = true;
                    break;
                }
            }
            if (!existingPaymentUpdated) {
                paymentID = FileHandler.generateNextPaymentID();
                payments.add(new Payment(paymentID, apptID, amount, date, "Paid"));
            }
            FileHandler.saveAllPayments(payments);

            List<Appointment> appts = FileHandler.loadAllAppointments();
            for (Appointment a : appts) {
                if (a.getAppointmentID().equals(apptID)) {
                    a.setStatus("Completed");
                    break;
                }
            }
            FileHandler.saveAllAppointments(appts);

            refreshPaymentsTable(model);
            dialog.dispose();

            showReceiptDialog(paymentID, apptID, customerName, serviceType, date, amount);
        });

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    /**
     * Shows the receipt after payment is confirmed.
     */
    private void showReceiptDialog(String paymentID, String apptID, String customerName, String serviceType, String date, double amount) {
        JDialog dialog = new JDialog(this, "Receipt", true);
        dialog.setSize(400, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_CARD);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titleLbl = new JLabel("Payment Receipt");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLbl.setForeground(ACCENT);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(titleLbl);
        content.add(Box.createVerticalStrut(16));

        content.add(makeInfoRow("Payment ID", paymentID));
        content.add(Box.createVerticalStrut(8));
        content.add(makeInfoRow("Appointment ID", apptID));
        content.add(Box.createVerticalStrut(8));
        content.add(makeInfoRow("Customer Name", customerName));
        content.add(Box.createVerticalStrut(8));
        content.add(makeInfoRow("Service Type", serviceType));
        content.add(Box.createVerticalStrut(8));
        content.add(makeInfoRow("Date", date));
        content.add(Box.createVerticalStrut(8));
        content.add(makeInfoRow("Amount", String.format("RM %.2f", amount)));
        content.add(Box.createVerticalStrut(24));

        JButton closeBtn = makeSecondaryButton("Close");
        closeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        closeBtn.addActionListener(e -> dialog.dispose());
        content.add(closeBtn);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    // SHARED HELPERS
    private JPanel makeStatCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                        BorderFactory.createMatteBorder(0, 3, 0, 0, valueColor)),
                new EmptyBorder(12, 12, 12, 12)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_MUTED);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel valueBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        valueBadge.setOpaque(true);
        valueBadge.setBackground(new Color(valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue(), 30));
        valueBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueBadge.add(valueLabel);

        card.add(titleLabel);
        card.add(valueBadge);
        return card;
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

    private void refreshDashboardPanel() {
        contentPanel.remove(dashboardPanel);
        dashboardPanel = buildDashboardPanel();
        contentPanel.add(dashboardPanel, "DASHBOARD");
    }

    private void refreshContentPanels() {
        contentPanel.removeAll();
        dashboardPanel = buildDashboardPanel();
        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(buildProfilePanel(), "PROFILE");
        contentPanel.add(buildCustomersPanel(), "CUSTOMERS");
        contentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");
        contentPanel.add(buildPaymentsPanel(), "PAYMENTS");
    }

    private void navigateToPanel(String cardName) {
        if (profileEditMode) {
            int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Discard them?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.YES_OPTION)
                return;
            exitProfileEditMode();
        }
        refreshContentPanels();
        contentLayout.show(contentPanel, cardName);
        activeCardName = cardName;
        updateNavButtonStyles();
    }

    private JButton makeNavButton(String label, String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 16));
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
        btn.addActionListener(e -> navigateToPanel(cardName));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(TEXT_PRIMARY);
                if (!activeCardName.equals(btn.getClientProperty("cardName"))) {
                    btn.setOpaque(true);
                    btn.setContentAreaFilled(true);
                    btn.setBackground(BG_CARD2);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateNavButtonStyle(btn);
            }
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
