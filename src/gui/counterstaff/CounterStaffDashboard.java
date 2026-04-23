package gui.counterstaff;

import model.CounterStaff;
import model.Customer;
import model.User;
import util.FileHandler;
import util.Session;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
    private static final Color BG_CARD = new Color(24, 27, 42);
    private static final Color BG_CARD2 = new Color(30, 34, 52);
    private static final Color ACCENT = new Color(20, 184, 166);
    private static final Color TEXT_PRIMARY = new Color(240, 241, 255);
    private static final Color TEXT_MUTED = new Color(148, 151, 180);
    private static final Color BORDER_COLOR = new Color(55, 58, 80);
    private static final Color DANGER = new Color(239, 68, 68);

    // STATE
    private CounterStaff currentStaff;

    // LAYOUT
    private CardLayout contentLayout;
    private JPanel contentPanel;

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
                new EmptyBorder(24, 0, 24, 0)));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel section = new JLabel("  COUNTER STAFF MENU");
        section.setFont(new Font("SansSerif", Font.BOLD, 10));
        section.setForeground(TEXT_MUTED);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(makeNavButton("👤  My Profile", "PROFILE"));
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

        contentPanel.add(buildProfilePanel(), "PROFILE");
        contentPanel.add(buildCustomersPanel(), "CUSTOMERS");
        contentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");
        contentPanel.add(buildPaymentsPanel(), "PAYMENTS");

        contentLayout.show(contentPanel, "CUSTOMERS");
        return contentPanel;
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
            JPanel nameFieldsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            nameFieldsPanel.setOpaque(false);

            firstNameField = makeEditableTextField(currentStaff.getFirstName());
            lastNameField = makeEditableTextField(currentStaff.getLastName());

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
            usernameField = makeEditableTextField(currentStaff.getUsername());
            usernameRow.add(usernameLbl, BorderLayout.WEST);
            usernameRow.add(usernameField, BorderLayout.CENTER);
            profileCard.add(usernameRow);
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

            JPanel passwordFieldPanel = new JPanel(new BorderLayout(8, 0));
            passwordFieldPanel.setOpaque(false);
            passwordFieldPanel.add(passwordField, BorderLayout.CENTER);

            JButton eyeToggle = new JButton("👁");
            eyeToggle.setFont(new Font("SansSerif", Font.PLAIN, 16));
            eyeToggle.setBackground(new Color(0, 0, 0, 0));
            eyeToggle.setOpaque(false);
            eyeToggle.setBorderPainted(false);
            eyeToggle.setFocusPainted(false);
            eyeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            eyeToggle.setPreferredSize(new Dimension(30, 28));
            eyeToggle.addActionListener(e -> {
                passwordVisible = !passwordVisible;
                String currentText = passwordField.getText();
                if (passwordVisible) {
                    eyeToggle.setText("🙈");
                    // Remove masking to show actual password
                    passwordField.setText(currentStaff.getPassword());
                } else {
                    eyeToggle.setText("👁");
                    // Apply masking
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
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        // Get password (it might be masked, so use the original from currentStaff if
        // masked)
        String password = passwordVisible ? passwordField.getText().trim() : currentStaff.getPassword();

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                email.isEmpty() || phone.isEmpty()) {
            errorMsg.setText("❌ All fields are required.");
            profileCard.revalidate();
            profileCard.repaint();
            return;
        }

        // Validate phone number: 10-11 digits only
        if (!phone.matches("\\d{10,11}")) {
            errorMsg.setText("❌ Phone must be 10-11 digits.");
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
        userToUpdate.setUsername(username);
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
        currentStaff.setUsername(username);
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
                errLbl.setText("❌ All fields are required.");
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
    // TODO (Member 3): Create and assign appointments; check availability
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
        newBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "TODO (Member 3): Open New Appointment dialog.\n\n" +
                        "Steps:\n" +
                        "  1. Select a customer (JComboBox from customers.txt)\n" +
                        "  2. Select service type: Normal (1hr) or Major (3hr)\n" +
                        "  3. Select date and time\n" +
                        "  4. Pick a technician — filter out those already booked\n" +
                        "     at the selected time slot\n" +
                        "  5. Save to appointments.txt with status = Pending",
                "New Appointment", JOptionPane.INFORMATION_MESSAGE));
        headerRow.add(heading, BorderLayout.WEST);
        headerRow.add(newBtn, BorderLayout.EAST);

        String[] cols = { "ID", "Customer", "Technician", "Date", "Time", "Service", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        FileHandler.loadAllAppointments().forEach(a -> model.addRow(new Object[] {
                a.getAppointmentID(), a.getCustomerID(), a.getTechnicianID(),
                a.getDate(), a.getTime(), a.getServiceType(), a.getStatus()
        }));

        JTable table = makeStyledTable(model);
        JScrollPane scroll = makeScrollPane(table);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // PANEL 4 — COLLECT PAYMENT
    // TODO (Member 3): Process payment and generate receipt
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

        String[] cols = { "Payment ID", "Appointment ID", "Amount (RM)", "Date", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        FileHandler.loadAllPayments().forEach(p -> model.addRow(new Object[] {
                p.getPaymentID(), p.getAppointmentID(),
                String.format("%.2f", p.getAmount()), p.getDate(), p.getStatus()
        }));

        JTable table = makeStyledTable(model);
        JScrollPane scroll = makeScrollPane(table);

        JButton payBtn = makePrimaryButton("💳  Collect & Generate Receipt");
        payBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "TODO (Member 3): Process payment for selected appointment.",
                "Collect Payment", JOptionPane.INFORMATION_MESSAGE));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(payBtn, BorderLayout.SOUTH);
        return panel;
    }

    // SHARED HELPERS
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
        btn.addActionListener(e -> {
            // Warn user if profile is in edit mode with unsaved changes
            if (profileEditMode) {
                int result = JOptionPane.showConfirmDialog(this,
                        "You have unsaved changes. Discard them?",
                        "Unsaved Changes",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION)
                    return;
                // Exit edit mode without saving
                exitProfileEditMode();
            }
            contentLayout.show(contentPanel, cardName);
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(TEXT_PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(TEXT_MUTED);
            }
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
