package gui.manager;

import model.Manager;
import model.CounterStaff;
import model.Technician;
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
 *   [1] View / Add / Edit / Delete users (managers, counter staff, technicians) (GUI Fixes Required)
 *   [2] Set prices for Normal service and Major service (Partially complete, title and duration cant update into TXT files)
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
            if (row == -1) { showThemedInfo("Please select a user to edit."); return; }
            openEditUserDialog((String) tableModel.getValueAt(row, 0), tableModel);
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { showThemedInfo("Please select a user to delete."); return; }
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
     * Opens the Add User dialog. Collects user details and creates a new user.
     */
    private void openAddUserDialog() {
        JDialog dialog = new JDialog(this, "Add New User", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Username
        panel.add(createFormLabel("Username"));
        JTextField usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        usernameField.setBackground(BG_CARD);
        usernameField.setForeground(TEXT_PRIMARY);
        usernameField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(12));

        // Password
        panel.add(createFormLabel("Password"));
        JPasswordField passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setBackground(BG_CARD);
        passwordField.setForeground(TEXT_PRIMARY);
        passwordField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(12));

        // Role
        panel.add(createFormLabel("Role"));
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Manager", "CounterStaff", "Technician"});
        roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        roleCombo.setBackground(BG_CARD);
        roleCombo.setForeground(TEXT_PRIMARY);
        panel.add(roleCombo);
        panel.add(Box.createVerticalStrut(12));

        // First Name
        panel.add(createFormLabel("First Name"));
        JTextField firstNameField = new JTextField();
        firstNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        firstNameField.setBackground(BG_CARD);
        firstNameField.setForeground(TEXT_PRIMARY);
        firstNameField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(firstNameField);
        panel.add(Box.createVerticalStrut(12));

        // Last Name
        panel.add(createFormLabel("Last Name"));
        JTextField lastNameField = new JTextField();
        lastNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        lastNameField.setBackground(BG_CARD);
        lastNameField.setForeground(TEXT_PRIMARY);
        lastNameField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(lastNameField);
        panel.add(Box.createVerticalStrut(12));

        // Email
        panel.add(createFormLabel("Email"));
        JTextField emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        emailField.setBackground(BG_CARD);
        emailField.setForeground(TEXT_PRIMARY);
        emailField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(12));

        // Phone
        panel.add(createFormLabel("Phone"));
        JTextField phoneField = new JTextField();
        phoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        phoneField.setBackground(BG_CARD);
        phoneField.setForeground(TEXT_PRIMARY);
        phoneField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(phoneField);
        panel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = makeSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = makePrimaryButton("Add User");
        saveBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleCombo.getSelectedItem();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            String error = validateUserInput(username, password, firstName, lastName, email, phone, null);
            if (error != null) {
                showThemedInfo(error);
                return;
            }

            // Generate new ID and create user
            String newID = FileHandler.generateNextUserID();
            User newUser = createUserFromRole(newID, username, password, role, firstName, lastName, email, phone);

            List<User> users = FileHandler.loadAllUsers();
            users.add(newUser);
            FileHandler.saveAllUsers(users);

            DefaultTableModel tableModel = (DefaultTableModel) ((JTable) ((JScrollPane) ((JPanel) contentPanel.getComponent(0)).getComponent(1)).getViewport().getComponent(0)).getModel();
            refreshUsersTable(tableModel);

            showThemedInfo("User added successfully!");
            dialog.dispose();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(null);

        // Add scrollPane and buttons to dialog
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(BG_DARK);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPane);
        dialog.setVisible(true);
    }

    /**
     * Opens the Edit User dialog. Pre-fills with existing data, role is locked.
     */
    private void openEditUserDialog(String userID, DefaultTableModel model) {
        List<User> users = FileHandler.loadAllUsers();
        User targetUser = users.stream().filter(u -> u.getUserID().equals(userID)).findFirst().orElse(null);
        
        if (targetUser == null) {
            showThemedInfo("User not found.");
            return;
        }

        JDialog dialog = new JDialog(this, "Edit User", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // User ID (read-only)
        panel.add(createFormLabel("User ID (locked)"));
        JTextField idField = new JTextField(targetUser.getUserID());
        idField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        idField.setBackground(BG_CARD2);
        idField.setForeground(TEXT_MUTED);
        idField.setBorder(new EmptyBorder(8, 8, 8, 8));
        idField.setEditable(false);
        panel.add(idField);
        panel.add(Box.createVerticalStrut(12));

        // Username
        panel.add(createFormLabel("Username"));
        JTextField usernameField = new JTextField(targetUser.getUsername());
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        usernameField.setBackground(BG_CARD);
        usernameField.setForeground(TEXT_PRIMARY);
        usernameField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(12));

        // Password
        panel.add(createFormLabel("Password"));
        JPasswordField passwordField = new JPasswordField(targetUser.getPassword());
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        passwordField.setBackground(BG_CARD);
        passwordField.setForeground(TEXT_PRIMARY);
        passwordField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(12));

        // Role (read-only)
        panel.add(createFormLabel("Role (locked)"));
        JTextField roleField = new JTextField(targetUser.getRole());
        roleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        roleField.setBackground(BG_CARD2);
        roleField.setForeground(TEXT_MUTED);
        roleField.setBorder(new EmptyBorder(8, 8, 8, 8));
        roleField.setEditable(false);
        panel.add(roleField);
        panel.add(Box.createVerticalStrut(12));

        // First Name
        panel.add(createFormLabel("First Name"));
        JTextField firstNameField = new JTextField(targetUser.getFirstName());
        firstNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        firstNameField.setBackground(BG_CARD);
        firstNameField.setForeground(TEXT_PRIMARY);
        firstNameField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(firstNameField);
        panel.add(Box.createVerticalStrut(12));

        // Last Name
        panel.add(createFormLabel("Last Name"));
        JTextField lastNameField = new JTextField(targetUser.getLastName());
        lastNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        lastNameField.setBackground(BG_CARD);
        lastNameField.setForeground(TEXT_PRIMARY);
        lastNameField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(lastNameField);
        panel.add(Box.createVerticalStrut(12));

        // Email
        panel.add(createFormLabel("Email"));
        JTextField emailField = new JTextField(targetUser.getEmail());
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        emailField.setBackground(BG_CARD);
        emailField.setForeground(TEXT_PRIMARY);
        emailField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(12));

        // Phone
        panel.add(createFormLabel("Phone"));
        JTextField phoneField = new JTextField(targetUser.getPhone());
        phoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        phoneField.setBackground(BG_CARD);
        phoneField.setForeground(TEXT_PRIMARY);
        phoneField.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(phoneField);
        panel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = makeSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = makePrimaryButton("Save Changes");
        saveBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            String error = validateUserInput(username, password, firstName, lastName, email, phone, targetUser.getUsername());
            if (error != null) {
                showThemedInfo(error);
                return;
            }

            // Update user
            targetUser.setUsername(username);
            targetUser.setPassword(password);
            targetUser.setFirstName(firstName);
            targetUser.setLastName(lastName);
            targetUser.setEmail(email);
            targetUser.setPhone(phone);

            FileHandler.saveAllUsers(users);
            refreshUsersTable(model);

            showThemedInfo("User updated successfully!");
            dialog.dispose();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(null);

        // Add scrollPane and buttons to dialog
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(BG_DARK);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPane);
        dialog.setVisible(true);
    }

    /**
     * Deletes a user with confirmation. Prevents self-deletion.
     */
    private void deleteUser(String userID, DefaultTableModel model) {
        // Prevent self-deletion
        if (userID.equals(currentManager.getUserID())) {
            showThemedInfo("You cannot delete your own account!");
            return;
        }

        int confirm = showThemedConfirm("Confirm Delete",
            "Are you sure you want to delete this user? This action cannot be undone.");

        if (confirm != JOptionPane.YES_OPTION) return;

        List<User> users = FileHandler.loadAllUsers();
        users.removeIf(u -> u.getUserID().equals(userID));
        FileHandler.saveAllUsers(users);

        refreshUsersTable(model);
        showThemedInfo("User deleted successfully!");
    }

    /**
     * Helper: Validates user input. Returns error message if invalid, null if valid.
     * skipUsername can be set to the current username to allow keeping the same username on edit.
     */
    private String validateUserInput(String username, String password, String firstName, 
                                     String lastName, String email, String phone, String skipUsername) {
        if (username.isEmpty()) return "Username cannot be empty.";
        if (password.isEmpty()) return "Password cannot be empty.";
        if (firstName.isEmpty()) return "First name cannot be empty.";
        if (lastName.isEmpty()) return "Last name cannot be empty.";
        if (email.isEmpty()) return "Email cannot be empty.";
        if (phone.isEmpty()) return "Phone cannot be empty.";

        // Email format validation (basic)
        if (!email.contains("@")) return "Please enter a valid email address.";

        // Phone must be numeric
        if (!phone.matches("\\d+")) return "Phone must contain only digits.";

        // Username uniqueness check (unless it's the same as current username)
        if (skipUsername == null || !skipUsername.equals(username)) {
            User existing = FileHandler.findUserByUsername(username);
            if (existing != null) return "Username already exists. Please choose a different one.";
        }

        return null;  // all valid
    }

    /**
     * Helper: Creates the correct User subclass based on role.
     */
    private User createUserFromRole(String userID, String username, String password, String role,
                                    String firstName, String lastName, String email, String phone) {
        switch (role) {
            case "Manager":
                return new Manager(userID, username, password, firstName, lastName, email, phone);
            case "CounterStaff":
                return new CounterStaff(userID, username, password, firstName, lastName, email, phone);
            case "Technician":
                return new Technician(userID, username, password, firstName, lastName, email, phone);
            default:
                return null;
        }
    }

    /**
     * Helper: Creates a styled form label.
     */
    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }


    //  PANEL 2 — SERVICE PRICES
    //  Manage and update service prices for Normal and Major services

    private JPanel buildPricesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 24));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Header with title and edit button
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel heading = new JLabel("Service Prices");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        JButton editBtn = makePrimaryButton("✎ Edit Prices");

        headerRow.add(heading, BorderLayout.WEST);
        headerRow.add(editBtn, BorderLayout.EAST);

        // Price cards container
        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(600, 280));

        // Get current service data
        String normalName = "Normal Service";
        String normalDuration = "1 hour";
        double normalPrice = FileHandler.getServicePrice("Normal");

        String majorName = "Major Service";
        String majorDuration = "3 hours";
        double majorPrice = FileHandler.getServicePrice("Major");

        JPanel normalCard = buildPriceCard(normalName, normalDuration, normalPrice, false);
        JPanel majorCard = buildPriceCard(majorName, majorDuration, majorPrice, false);

        cardsRow.add(normalCard);
        cardsRow.add(majorCard);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(cardsRow, BorderLayout.CENTER);

        // Edit button action
        editBtn.addActionListener(e -> {
            editBtn.setEnabled(false);

            // Switch to editable mode - store references to text fields
            cardsRow.removeAll();
            JPanel normalCardEditable = buildPriceCard(normalName, normalDuration, normalPrice, true);
            JPanel majorCardEditable = buildPriceCard(majorName, majorDuration, majorPrice, true);
            
            cardsRow.add(normalCardEditable);
            cardsRow.add(majorCardEditable);
            cardsRow.revalidate();
            cardsRow.repaint();

            // Extract field references from editable cards
            // Card structure: [0]=nameField, [1]=spacer, [2]=durationField, [3]=spacer, [4]=priceInputPanel
            JTextField normalNameField = (JTextField) normalCardEditable.getComponent(0);
            JTextField normalDurationField = (JTextField) normalCardEditable.getComponent(2);
            JPanel normalPricePanel = (JPanel) normalCardEditable.getComponent(4);
            JTextField normalPriceField = (JTextField) normalPricePanel.getComponent(1);

            JTextField majorNameField = (JTextField) majorCardEditable.getComponent(0);
            JTextField majorDurationField = (JTextField) majorCardEditable.getComponent(2);
            JPanel majorPricePanel = (JPanel) majorCardEditable.getComponent(4);
            JTextField majorPriceField = (JTextField) majorPricePanel.getComponent(1);

            // Show save/cancel buttons
            JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
            actionRow.setOpaque(false);

            JButton cancelBtn = makeSecondaryButton("Cancel");
            JButton saveBtn = makePrimaryButton("Save Changes");

            cancelBtn.addActionListener(ce -> {
                editBtn.setEnabled(true);
                cardsRow.removeAll();
                cardsRow.add(buildPriceCard(normalName, normalDuration,
                    FileHandler.getServicePrice("Normal"), false));
                cardsRow.add(buildPriceCard(majorName, majorDuration,
                    FileHandler.getServicePrice("Major"), false));
                cardsRow.revalidate();
                cardsRow.repaint();

                panel.remove(actionRow);
                panel.revalidate();
                panel.repaint();
            });

            saveBtn.addActionListener(se -> {
                try {
                    String newNormalName = normalNameField.getText().trim();
                    String newNormalDuration = normalDurationField.getText().trim();
                    double newNormalPrice = Double.parseDouble(normalPriceField.getText().trim());

                    String newMajorName = majorNameField.getText().trim();
                    String newMajorDuration = majorDurationField.getText().trim();
                    double newMajorPrice = Double.parseDouble(majorPriceField.getText().trim());

                    // Validation
                    if (newNormalName.isEmpty() || newMajorName.isEmpty()) {
                        showThemedInfo("Service names cannot be empty.");
                        return;
                    }
                    if (newNormalDuration.isEmpty() || newMajorDuration.isEmpty()) {
                        showThemedInfo("Durations cannot be empty.");
                        return;
                    }
                    if (newNormalPrice <= 0 || newMajorPrice <= 0) {
                        showThemedInfo("Prices must be greater than 0.");
                        return;
                    }

                    // Update services with title, price, and duration
                    FileHandler.updateService("Normal", newNormalPrice, newNormalDuration);
                    FileHandler.updateService("Major", newMajorPrice, newMajorDuration);

                    showThemedInfo("Services updated successfully!");

                    // Return to view mode
                    editBtn.setEnabled(true);
                    cardsRow.removeAll();
                    cardsRow.add(buildPriceCard(newNormalName, newNormalDuration, newNormalPrice, false));
                    cardsRow.add(buildPriceCard(newMajorName, newMajorDuration, newMajorPrice, false));
                    cardsRow.revalidate();
                    cardsRow.repaint();

                    panel.remove(actionRow);
                    panel.revalidate();
                    panel.repaint();

                } catch (NumberFormatException ex) {
                    showThemedInfo("Please enter valid price numbers.");
                }
            });

            actionRow.add(cancelBtn);
            actionRow.add(saveBtn);
            panel.add(actionRow, BorderLayout.SOUTH);
            panel.revalidate();
            panel.repaint();
        });

        return panel;
    }

    private JPanel buildPriceCard(String serviceName, String duration, double price, boolean editable) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(24, 24, 24, 24)
        ));
        card.setMaximumSize(new Dimension(260, 220));

        if (editable) {
            // Service Name (editable)
            JTextField nameField = new JTextField(serviceName, 18);
            nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            nameField.setBackground(BG_CARD2);
            nameField.setForeground(TEXT_PRIMARY);
            nameField.setFont(new Font("SansSerif", Font.BOLD, 14));
            nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 8, 6, 8)
            ));
            nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(nameField);
            card.add(Box.createVerticalStrut(8));

            // Duration (editable)
            JTextField durationField = new JTextField(duration, 15);
            durationField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            durationField.setBackground(BG_CARD2);
            durationField.setForeground(TEXT_PRIMARY);
            durationField.setFont(new Font("SansSerif", Font.PLAIN, 12));
            durationField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(4, 8, 4, 8)
            ));
            durationField.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(durationField);
            card.add(Box.createVerticalStrut(12));

            // Price (editable)
            JPanel priceInputPanel = new JPanel(new BorderLayout(8, 0));
            priceInputPanel.setOpaque(false);
            priceInputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel rmLabel = new JLabel("Price (RM):");
            rmLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            rmLabel.setForeground(TEXT_MUTED);

            JTextField priceField = new JTextField(String.format("%.2f", price), 12);
            priceField.setMaximumSize(new Dimension(100, 28));
            priceField.setBackground(BG_CARD2);
            priceField.setForeground(ACCENT);
            priceField.setFont(new Font("SansSerif", Font.BOLD, 14));
            priceField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 8, 6, 8)
            ));

            priceInputPanel.add(rmLabel, BorderLayout.WEST);
            priceInputPanel.add(priceField, BorderLayout.CENTER);

            card.add(priceInputPanel);
        } else {
            // View Mode
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
        }

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

        // Table with richer columns
        String[] columns = {"Apt ID", "Type", "From", "Status", "Service", "Content", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Load all necessary data
        var appointments = FileHandler.loadAllAppointments();
        var feedbacks = FileHandler.loadAllFeedbacks();
        var comments = FileHandler.loadAllComments();

        // Create a map for quick lookup
        var appointmentMap = new java.util.HashMap<String, model.Appointment>();
        appointments.forEach(a -> appointmentMap.put(a.getAppointmentID(), a));

        // Load feedbacks (from technicians) with enriched data
        feedbacks.forEach(f -> {
            model.Appointment apt = appointmentMap.get(f.getAppointmentID());
            String status = apt != null ? apt.getStatus() : "Unknown";
            String service = apt != null ? apt.getServiceType() : "Unknown";
            String date = apt != null ? apt.getDate() : "N/A";
            
            model.addRow(new Object[]{
                f.getAppointmentID(),
                "Feedback",
                f.getTechnicianID(),
                status,
                service,
                f.getFeedbackText(),
                date
            });
        });

        // Load comments (from customers) with enriched data
        comments.forEach(c -> {
            model.Appointment apt = appointmentMap.get(c.getAppointmentID());
            String status = apt != null ? apt.getStatus() : "Unknown";
            String service = apt != null ? apt.getServiceType() : "Unknown";
            String date = apt != null ? apt.getDate() : "N/A";
            
            model.addRow(new Object[]{
                c.getAppointmentID(),
                "Comment",
                c.getCustomerID(),
                status,
                service,
                c.getCommentText(),
                date
            });
        });

        JTable table = makeStyledTable(model);
        table.getColumnModel().getColumn(5).setPreferredWidth(300);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);

        // Setup row sorter with filtering capability
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Add double-click listener to view full content
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.convertRowIndexToModel(table.rowAtPoint(e.getPoint()));
                    if (row >= 0) {
                        String content = (String) model.getValueAt(row, 5);
                        String type = (String) model.getValueAt(row, 1);
                        String from = (String) model.getValueAt(row, 2);
                        showThemedInfo("<b>" + type + " from " + from + ":</b><br><br>" + content);
                    }
                }
            }
        });

        // Build toolbar with filters and search
        JPanel toolbar = buildFeedbacksToolbar(model, sorter, table);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(toolbar,           BorderLayout.NORTH);
        centerPanel.add(makeScrollPane(table), BorderLayout.CENTER);

        panel.add(heading,       BorderLayout.NORTH);
        panel.add(centerPanel,   BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the toolbar for feedback filtering and search
     */
    private JPanel buildFeedbacksToolbar(DefaultTableModel model, javax.swing.table.TableRowSorter<DefaultTableModel> sorter, JTable table) {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(BG_CARD);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(12, 16, 12, 16)
        ));

        // State variables for filter
        final String[] currentFilter = {"ALL"};

        // Left side: Filter buttons
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        filterLabel.setForeground(TEXT_MUTED);
        filterPanel.add(filterLabel);

        JButton allBtn = new JButton("All") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }
                super.paintComponent(g);
            }
        };
        allBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        allBtn.setForeground(ACCENT);
        allBtn.setBackground(new Color(0, 0, 0, 0));
        allBtn.setOpaque(false);
        allBtn.setBorderPainted(false);
        allBtn.setFocusPainted(false);
        allBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton feedbackBtn = new JButton("Feedbacks") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }
                super.paintComponent(g);
            }
        };
        feedbackBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        feedbackBtn.setForeground(TEXT_MUTED);
        feedbackBtn.setBackground(new Color(0, 0, 0, 0));
        feedbackBtn.setOpaque(false);
        feedbackBtn.setBorderPainted(false);
        feedbackBtn.setFocusPainted(false);
        feedbackBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton commentBtn = new JButton("Comments") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                }
                super.paintComponent(g);
            }
        };
        commentBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        commentBtn.setForeground(TEXT_MUTED);
        commentBtn.setBackground(new Color(0, 0, 0, 0));
        commentBtn.setOpaque(false);
        commentBtn.setBorderPainted(false);
        commentBtn.setFocusPainted(false);
        commentBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add filter button listeners
        allBtn.addActionListener(e -> {
            currentFilter[0] = "ALL";
            allBtn.setForeground(ACCENT);
            feedbackBtn.setForeground(TEXT_MUTED);
            commentBtn.setForeground(TEXT_MUTED);
            applyFeedbackFilters(sorter, model, currentFilter[0], "");
        });

        feedbackBtn.addActionListener(e -> {
            currentFilter[0] = "Feedback";
            allBtn.setForeground(TEXT_MUTED);
            feedbackBtn.setForeground(ACCENT);
            commentBtn.setForeground(TEXT_MUTED);
            applyFeedbackFilters(sorter, model, currentFilter[0], "");
        });

        commentBtn.addActionListener(e -> {
            currentFilter[0] = "Comment";
            allBtn.setForeground(TEXT_MUTED);
            feedbackBtn.setForeground(TEXT_MUTED);
            commentBtn.setForeground(ACCENT);
            applyFeedbackFilters(sorter, model, currentFilter[0], "");
        });

        filterPanel.add(allBtn);
        filterPanel.add(feedbackBtn);
        filterPanel.add(commentBtn);

        // Right side: Search box
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchPanel.setOpaque(false);

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setBackground(BG_DARK);
        searchField.setCaretColor(ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.setPreferredSize(new Dimension(200, 32));
        searchField.setText("Search by ID or content...");
        searchField.setForeground(TEXT_MUTED);

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (searchField.getText().equals("Search by ID or content...")) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search by ID or content...");
                    searchField.setForeground(TEXT_MUTED);
                }
            }
        });

        // Add search field listener
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            
            private void updateSearch() {
                String searchText = searchField.getText();
                if (searchText.equals("Search by ID or content...") || searchText.isEmpty()) {
                    applyFeedbackFilters(sorter, model, currentFilter[0], "");
                } else {
                    applyFeedbackFilters(sorter, model, currentFilter[0], searchText);
                }
            }
        });

        searchPanel.add(searchField);

        toolbar.add(filterPanel,  BorderLayout.WEST);
        toolbar.add(searchPanel,  BorderLayout.EAST);

        return toolbar;
    }

    /**
     * Helper method to apply filters to the feedback table
     */
    private void applyFeedbackFilters(javax.swing.table.TableRowSorter<DefaultTableModel> sorter, 
                                      DefaultTableModel model, String typeFilter, String searchText) {
        java.util.List<javax.swing.RowFilter<Object, Object>> filters = new java.util.ArrayList<>();

        // Type filter (Feedback/Comment/All)
        if (!typeFilter.equals("ALL")) {
            filters.add(javax.swing.RowFilter.regexFilter("^" + typeFilter + "$", 1));
        }

        // Search filter (Appointment ID or Content)
        if (!searchText.isEmpty()) {
            java.util.List<javax.swing.RowFilter<Object, Object>> searchFilters = new java.util.ArrayList<>();
            searchFilters.add(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(searchText), 0)); // Apt ID
            searchFilters.add(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(searchText), 5)); // Content
            filters.add(javax.swing.RowFilter.orFilter(searchFilters));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else if (filters.size() == 1) {
            sorter.setRowFilter(filters.get(0));
        } else {
            sorter.setRowFilter(javax.swing.RowFilter.andFilter(filters));
        }
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

    /**
     * Helper: Shows a themed info dialog matching the project color scheme.
     */
    private void showThemedInfo(String message) {
        JDialog dialog = new JDialog(this, "Info", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(20));

        JButton okBtn = makePrimaryButton("OK");
        okBtn.addActionListener(e -> dialog.dispose());
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(okBtn);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * Helper: Shows a themed confirmation dialog matching the project color scheme.
     */
    private int showThemedConfirm(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(420, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttonPanel.setOpaque(false);

        JButton noBtn = makeSecondaryButton("No");
        JButton yesBtn = makePrimaryButton("Yes");

        final int[] result = {JOptionPane.NO_OPTION};

        yesBtn.addActionListener(e -> {
            result[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });

        noBtn.addActionListener(e -> {
            result[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });

        buttonPanel.add(noBtn);
        buttonPanel.add(yesBtn);
        panel.add(buttonPanel);

        dialog.add(panel);
        dialog.setVisible(true);
        return result[0];
    }
}
