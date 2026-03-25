package gui.manager;

import model.Manager;
import model.User;
import util.FileHandler;
import util.Session;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * GUI CLASS — ManagerDashboard
 * -----------------------------
 * The main window for the Manager role.
 *
 * MEMBER 2 is responsible for implementing all features in this file.
 *
 * FEATURES TO IMPLEMENT:
 *   [1] View / Add / Edit / Delete users (managers, counter staff, technicians)
 *   [2] Set prices for Normal service and Major service
 *   [3] View all feedbacks and comments
 *   [4] Analysed reports (total revenue, appointments by type, etc.)
 *
 * LAYOUT:
 *   The window uses BorderLayout:
 *   - WEST:   Sidebar with navigation buttons
 *   - CENTER: Content panel using CardLayout (swaps panels when nav is clicked)
 *   - NORTH:  Top bar with title and logged-in user's name
 */
public class ManagerDashboard extends JFrame {

    // COLOURS
    protected static final Color BG_DARK      = new Color(15,  17,  26);
    protected static final Color BG_CARD      = new Color(24,  27,  42);
    protected static final Color BG_CARD2     = new Color(30,  34,  52);
    protected static final Color ACCENT       = new Color(99,  102, 241);
    protected static final Color TEXT_PRIMARY = new Color(240, 241, 255);
    protected static final Color TEXT_MUTED   = new Color(148, 151, 180);
    protected static final Color BORDER_COLOR = new Color(55,   58,  80);
    protected static final Color SUCCESS      = new Color(34,  197,  94);
    protected static final Color DANGER       = new Color(239,  68,  68);

    // STATE
    private Manager currentManager;

    // LAYOUT
    private CardLayout contentLayout;
    private JPanel     contentPanel;

    // PANELS (one per sidebar section)
    private JPanel usersPanel;
    private JPanel pricesPanel;
    private JPanel feedbacksPanel;
    private JPanel reportsPanel;

    // CONSTRUCTOR
    public ManagerDashboard(Manager manager) {
        this.currentManager = manager;

        setTitle("APU-ASC — Manager Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        buildUI();
    }

    //  BUILD FULL WINDOW
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        root.add(buildTopBar(),    BorderLayout.NORTH);
        root.add(buildSidebar(),   BorderLayout.WEST);
        root.add(buildContent(),   BorderLayout.CENTER);

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

        JLabel userLbl = new JLabel("👤  " + currentManager.getFullName() + "  ·  Manager");
        userLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userLbl.setForeground(TEXT_MUTED);

        JButton logoutBtn = makeTextButton("Logout", DANGER);
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

        JLabel section = new JLabel("  MANAGER MENU");
        section.setFont(new Font("SansSerif", Font.BOLD, 10));
        section.setForeground(TEXT_MUTED);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(12));

        // Each nav item shows a different panel in the content area
        sidebar.add(makeNavButton("👥  Manage Users",    "USERS"));
        sidebar.add(makeNavButton("💰  Service Prices",  "PRICES"));
        sidebar.add(makeNavButton("💬  Feedbacks",       "FEEDBACKS"));
        sidebar.add(makeNavButton("📊  Reports",         "REPORTS"));

        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    //  CONTENT AREA (card-switched panels)
    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(BG_DARK);

        // Build each panel
        usersPanel     = buildUsersPanel();
        pricesPanel    = buildPricesPanel();
        feedbacksPanel = buildFeedbacksPanel();
        reportsPanel   = buildReportsPanel();

        contentPanel.add(usersPanel,     "USERS");
        contentPanel.add(pricesPanel,    "PRICES");
        contentPanel.add(feedbacksPanel, "FEEDBACKS");
        contentPanel.add(reportsPanel,   "REPORTS");

        contentLayout.show(contentPanel, "USERS");  // default view
        return contentPanel;
    }


    //  PANEL 1 — MANAGE USERS
    //  TODO (Member 2): Implement full CRUD for users
    private JPanel buildUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Header row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel heading = new JLabel("Manage Users");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        JButton addBtn = makePrimaryButton("+ Add User");
        addBtn.addActionListener(e -> openAddUserDialog());

        headerRow.add(heading, BorderLayout.WEST);
        headerRow.add(addBtn,  BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Username", "Role", "First Name", "Last Name", "Email", "Phone"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = makeStyledTable(tableModel);

        // Load data from users.txt into the table
        refreshUsersTable(tableModel);

        JScrollPane scrollPane = makeScrollPane(table);

        // Action buttons row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);

        JButton editBtn   = makeSecondaryButton("✏ Edit Selected");
        JButton deleteBtn = makeSecondaryButton("🗑 Delete Selected");
        deleteBtn.setForeground(DANGER);

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showInfo("Please select a user to edit."); return; }
            openEditUserDialog((String) tableModel.getValueAt(row, 0), tableModel);
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showInfo("Please select a user to delete."); return; }
            String uid = (String) tableModel.getValueAt(row, 0);
            deleteUser(uid, tableModel);
        });

        actionRow.add(editBtn);
        actionRow.add(deleteBtn);

        panel.add(headerRow,  BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionRow,  BorderLayout.SOUTH);
        return panel;
    }

    /** Clears and reloads the users table from users.txt */
    private void refreshUsersTable(DefaultTableModel model) {
        model.setRowCount(0);
        List<User> users = FileHandler.loadAllUsers();
        for (User u : users) {
            // Managers can see all roles EXCEPT customers in user management
            model.addRow(new Object[]{
                u.getUserID(), u.getUsername(), u.getRole(),
                u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhone()
            });
        }
    }

    /**
     * TODO (Member 2): Build the Add User dialog.
     * It should collect: username, password, role, first name, last name, email, phone.
     * On confirm: generate a new ID, add to users.txt via FileHandler.
     */
    private void openAddUserDialog() {
        // PLACEHOLDER — Member 2 replaces this with a full JDialog form
        JOptionPane.showMessageDialog(this,
            "TODO: Open Add User dialog here.\n" +
            "Collect user details and call FileHandler.saveAllUsers().",
            "Add User — Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * TODO (Member 2): Build the Edit User dialog.
     * Pre-fill form with existing data, update the record on confirm.
     */
    private void openEditUserDialog(String userID, DefaultTableModel model) {
        // PLACEHOLDER
        JOptionPane.showMessageDialog(this,
            "TODO: Open Edit dialog for User ID: " + userID,
            "Edit User — Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * TODO (Member 2): Confirm and delete a user from users.txt.
     * Do not allow deleting yourself.
     */
    private void deleteUser(String userID, DefaultTableModel model) {
        // PLACEHOLDER
        JOptionPane.showMessageDialog(this,
            "TODO: Delete User ID: " + userID,
            "Delete User — Coming Soon", JOptionPane.INFORMATION_MESSAGE);
    }


    //  PANEL 2 — SERVICE PRICES
    //  TODO (Member 2): Implement price-setting for Normal and Major service

    private JPanel buildPricesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 24));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Service Prices");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        // Price cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(600, 200));

        cardsRow.add(buildPriceCard("Normal Service",
            "1 hour", FileHandler.getServicePrice("Normal")));
        cardsRow.add(buildPriceCard("Major Service",
            "3 hours", FileHandler.getServicePrice("Major")));

        JLabel note = new JLabel(
            "TODO (Member 2): Add input fields and a Save button to update prices " +
            "via FileHandler.updateServicePrice().");
        note.setFont(new Font("SansSerif", Font.ITALIC, 13));
        note.setForeground(TEXT_MUTED);

        panel.add(heading,  BorderLayout.NORTH);
        panel.add(cardsRow, BorderLayout.CENTER);
        panel.add(note,     BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPriceCard(String serviceName, String duration, double price) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(24, 24, 24, 24)
        ));
        card.setMaximumSize(new Dimension(260, 160));

        JLabel name = new JLabel(serviceName);
        name.setFont(new Font("SansSerif", Font.BOLD, 16));
        name.setForeground(TEXT_PRIMARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dur = new JLabel("Duration: " + duration);
        dur.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dur.setForeground(TEXT_MUTED);
        dur.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceLbl = new JLabel(String.format("RM %.2f", price));
        priceLbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        priceLbl.setForeground(ACCENT);
        priceLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(name);
        card.add(Box.createVerticalStrut(6));
        card.add(dur);
        card.add(Box.createVerticalStrut(16));
        card.add(priceLbl);
        return card;
    }


    //  PANEL 3 — FEEDBACKS & COMMENTS
    //  TODO (Member 2): Display all feedbacks + comments in a readable layout
    private JPanel buildFeedbacksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Feedbacks & Comments");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        String[] columns = {"Appointment ID", "Type", "From", "Content"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Load feedbacks (from technicians)
        FileHandler.loadAllFeedbacks().forEach(f ->
            model.addRow(new Object[]{
                f.getAppointmentID(), "Technician Feedback", f.getTechnicianID(), f.getFeedbackText()
            })
        );
        // Load comments (from customers)
        FileHandler.loadAllComments().forEach(c ->
            model.addRow(new Object[]{
                c.getAppointmentID(), "Customer Comment", c.getCustomerID(), c.getCommentText()
            })
        );

        JTable table = makeStyledTable(model);
        table.getColumnModel().getColumn(3).setPreferredWidth(300); // wider content column

        panel.add(heading,            BorderLayout.NORTH);
        panel.add(makeScrollPane(table), BorderLayout.CENTER);
        return panel;
    }


    //  PANEL 4 — REPORTS
    //  TODO (Member 2): Show summary stats — revenue, count by type, etc.
    private JPanel buildReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 24));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("Analysed Reports");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        // Quick summary cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);

        // Calculate stats from file data
        long totalAppointments = FileHandler.loadAllAppointments().size();
        long completed = FileHandler.loadAllAppointments().stream()
            .filter(a -> "Completed".equals(a.getStatus())).count();
        double totalRevenue = FileHandler.loadAllPayments().stream()
            .mapToDouble(p -> p.getAmount()).sum();

        statsRow.add(makeStatCard("Total Appointments", String.valueOf(totalAppointments), ACCENT));
        statsRow.add(makeStatCard("Completed",          String.valueOf(completed),         SUCCESS));
        statsRow.add(makeStatCard("Total Revenue",      String.format("RM %.2f", totalRevenue), new Color(245, 158, 11)));

        JLabel todo = new JLabel(
            "TODO (Member 2): Add a detailed breakdown table and charts below.");
        todo.setFont(new Font("SansSerif", Font.ITALIC, 13));
        todo.setForeground(TEXT_MUTED);

        panel.add(heading,  BorderLayout.NORTH);
        panel.add(statsRow, BorderLayout.CENTER);
        panel.add(todo,     BorderLayout.SOUTH);
        return panel;
    }

    private JPanel makeStatCard(String label, String value, Color valueColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 30));
        val.setForeground(valueColor);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lbl);
        card.add(Box.createVerticalStrut(8));
        card.add(val);
        return card;
    }


    //  SHARED COMPONENT BUILDERS
    /** Creates a styled sidebar navigation button */
    private JButton makeNavButton(String label, String cardName) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed() || getModel().isSelected()) {
                    g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
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
            btn.setForeground(TEXT_PRIMARY);
            contentLayout.show(contentPanel, cardName);
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(TEXT_PRIMARY); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(TEXT_MUTED);   }
        });
        return btn;
    }

    protected JButton makePrimaryButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    protected JButton makeSecondaryButton(String label) {
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

    private JButton makeTextButton(String label, Color color) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(color);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    protected JTable makeStyledTable(DefaultTableModel model) {
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

    protected JScrollPane makeScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        return sp;
    }

    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
