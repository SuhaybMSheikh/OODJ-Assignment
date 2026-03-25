package gui.counterstaff;

import model.CounterStaff;
import util.FileHandler;
import util.Session;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * GUI CLASS — CounterStaffDashboard
 * -----------------------------------
 * MEMBER 3 is responsible for implementing all features in this file.
 *
 * FEATURES TO IMPLEMENT:
 *   [1] Edit own profile
 *   [2] Create / Read / Update / Delete customers
 *   [3] Create and assign new appointments
 *       - Normal service = 1 hour, Major service = 3 hours
 *       - Check technician availability (no time overlap)
 *   [4] Collect payment and generate receipt
 */
public class CounterStaffDashboard extends JFrame {

    // COLOURS 
    private static final Color BG_DARK      = new Color(15,  17,  26);
    private static final Color BG_CARD      = new Color(24,  27,  42);
    private static final Color BG_CARD2     = new Color(30,  34,  52);
    private static final Color ACCENT       = new Color(20, 184, 166);
    private static final Color TEXT_PRIMARY = new Color(240, 241, 255);
    private static final Color TEXT_MUTED   = new Color(148, 151, 180);
    private static final Color BORDER_COLOR = new Color(55,  58,  80);
    private static final Color DANGER       = new Color(239, 68,  68);

    // STATE
    private CounterStaff currentStaff;

    // LAYOUT
    private CardLayout contentLayout;
    private JPanel     contentPanel;

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

        root.add(buildTopBar(),  BorderLayout.NORTH);
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
            new EmptyBorder(14, 24, 14, 24)
        ));

        JLabel title = new JLabel("APU Automotive Service Centre");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightSide.setOpaque(false);

        JLabel userLbl = new JLabel("👤  " + currentStaff.getFullName() + "  ·  Counter Staff");
        userLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userLbl.setForeground(TEXT_MUTED);

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

        rightSide.add(userLbl);
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

        JLabel section = new JLabel("  COUNTER STAFF MENU");
        section.setFont(new Font("SansSerif", Font.BOLD, 10));
        section.setForeground(TEXT_MUTED);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(makeNavButton("👤  My Profile",       "PROFILE"));
        sidebar.add(makeNavButton("💁‍♀️  Customers",       "CUSTOMERS"));
        sidebar.add(makeNavButton("📅  Appointments",     "APPOINTMENTS"));
        sidebar.add(makeNavButton("💳  Collect Payment",  "PAYMENTS"));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    //  CONTENT PANELS
    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(BG_DARK);

        contentPanel.add(buildProfilePanel(),      "PROFILE");
        contentPanel.add(buildCustomersPanel(),    "CUSTOMERS");
        contentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");
        contentPanel.add(buildPaymentsPanel(),     "PAYMENTS");

        contentLayout.show(contentPanel, "CUSTOMERS");
        return contentPanel;
    }


    //  PANEL 1 — MY PROFILE
    //  TODO (Member 3): Allow staff to edit their own details
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("My Profile");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(28, 28, 28, 28)
        ));
        card.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Display current info
        card.add(makeInfoRow("Full Name",  currentStaff.getFullName()));
        card.add(Box.createVerticalStrut(12));
        card.add(makeInfoRow("Username",   currentStaff.getUsername()));
        card.add(Box.createVerticalStrut(12));
        card.add(makeInfoRow("Email",      currentStaff.getEmail()));
        card.add(Box.createVerticalStrut(12));
        card.add(makeInfoRow("Phone",      currentStaff.getPhone()));
        card.add(Box.createVerticalStrut(24));

        JButton editBtn = makePrimaryButton("✏  Edit Profile");
        editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        editBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "TODO (Member 3): Open edit profile dialog.\n" +
                "Allow editing: first name, last name, email, phone, password.\n" +
                "Save by updating the user record in users.txt.",
                "Edit Profile", JOptionPane.INFORMATION_MESSAGE)
        );
        card.add(editBtn);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(card,    BorderLayout.CENTER);
        return panel;
    }


    //  PANEL 2 — CUSTOMERS
    //  TODO (Member 3): Full CRUD for customers
    private JPanel buildCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel heading = new JLabel("Manage Customers");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        JButton addBtn = makePrimaryButton("+ Add Customer");
        addBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "TODO (Member 3): Open Add Customer dialog.\n" +
                "Fields: first name, last name, email, phone.\n" +
                "Create a User account (role=Customer) in users.txt\n" +
                "AND a record in customers.txt.",
                "Add Customer", JOptionPane.INFORMATION_MESSAGE)
        );
        headerRow.add(heading, BorderLayout.WEST);
        headerRow.add(addBtn,  BorderLayout.EAST);

        String[] cols = {"Customer ID", "First Name", "Last Name", "Email", "Phone"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };


        // TODO (Member 3): Load from customers.txt — join with users.txt for names
        // SAMPLE placeholder row:
        model.addRow(new Object[]{"C001", "John", "Tan", "john@email.com", "0167654321"});

        JTable table = makeStyledTable(model);
        JScrollPane scroll = makeScrollPane(table);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        JButton editBtn   = makeSecondaryButton("✏ Edit");
        JButton deleteBtn = makeSecondaryButton("🗑 Delete");
        deleteBtn.setForeground(DANGER);
        editBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "TODO (Member 3): Edit selected customer.", "", JOptionPane.INFORMATION_MESSAGE));
        deleteBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "TODO (Member 3): Delete selected customer.", "", JOptionPane.INFORMATION_MESSAGE));
        actionRow.add(editBtn);
        actionRow.add(deleteBtn);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(scroll,    BorderLayout.CENTER);
        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }


    //  PANEL 3 — APPOINTMENTS
    //  TODO (Member 3): Create and assign appointments; check availability
    private JPanel buildAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JLabel heading = new JLabel("Appointments");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        JButton newBtn = makePrimaryButton("+ New Appointment");
        newBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "TODO (Member 3): Open New Appointment dialog.\n\n" +
                "Steps:\n" +
                "  1. Select a customer (JComboBox from customers.txt)\n" +
                "  2. Select service type: Normal (1hr) or Major (3hr)\n" +
                "  3. Select date and time\n" +
                "  4. Pick a technician — filter out those already booked\n" +
                "     at the selected time slot\n" +
                "  5. Save to appointments.txt with status = Pending",
                "New Appointment", JOptionPane.INFORMATION_MESSAGE)
        );
        headerRow.add(heading, BorderLayout.WEST);
        headerRow.add(newBtn,  BorderLayout.EAST);

        String[] cols = {"ID", "Customer", "Technician", "Date", "Time", "Service", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        FileHandler.loadAllAppointments().forEach(a ->
            model.addRow(new Object[]{
                a.getAppointmentID(), a.getCustomerID(), a.getTechnicianID(),
                a.getDate(), a.getTime(), a.getServiceType(), a.getStatus()
            })
        );

        JTable table = makeStyledTable(model);
        JScrollPane scroll = makeScrollPane(table);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(scroll,    BorderLayout.CENTER);
        return panel;
    }


    //  PANEL 4 — COLLECT PAYMENT
    //  TODO (Member 3): Process payment and generate receipt
    private JPanel buildPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Collect Payment");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        JLabel note = new JLabel(
            "<html>TODO (Member 3): Show a list of Completed appointments<br>" +
            "that have NOT yet been paid. Let the staff select one,<br>" +
            "confirm the amount (from services.txt), and write to payments.txt.<br>" +
            "Then show a receipt popup (JDialog) with all details.</html>");
        note.setFont(new Font("SansSerif", Font.PLAIN, 14));
        note.setForeground(TEXT_MUTED);
        note.setBorder(new EmptyBorder(20, 0, 0, 0));

        String[] cols = {"Payment ID", "Appointment ID", "Amount (RM)", "Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        FileHandler.loadAllPayments().forEach(p ->
            model.addRow(new Object[]{
                p.getPaymentID(), p.getAppointmentID(),
                String.format("%.2f", p.getAmount()), p.getDate(), p.getStatus()
            })
        );

        JTable table = makeStyledTable(model);
        JScrollPane scroll = makeScrollPane(table);

        JButton payBtn = makePrimaryButton("💳  Collect & Generate Receipt");
        payBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "TODO (Member 3): Process payment for selected appointment.",
                "Collect Payment", JOptionPane.INFORMATION_MESSAGE)
        );

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        panel.add(payBtn,  BorderLayout.SOUTH);
        return panel;
    }


    //  SHARED HELPERS
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
        btn.addActionListener(e -> contentLayout.show(contentPanel, cardName));
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
