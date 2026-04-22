package gui.customer;

import model.Customer;
import model.Appointment;
import model.Feedback;
import model.Comment;
import util.FileHandler;
import util.Session;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

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
    private static final Color TEXT_PRIMARY = new Color(240, 241, 255);
    private static final Color TEXT_MUTED   = new Color(148, 151, 180);
    private static final Color BORDER_COLOR = new Color(55,  58,  80);
    private static final Color DANGER       = new Color(239, 68,  68);

    // STATE
    private Customer currentCustomer;

    // LAYOUT
    private CardLayout contentLayout;
    private JPanel     contentPanel;

    // PROFILE PANEL COMPONENTS (for refreshing after edit)
    private JLabel fullNameLabel;
    private JLabel emailLabel;
    private JLabel phoneLabel;
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

        sidebar.add(makeNavButton("👤  My Profile",       "PROFILE"));
        sidebar.add(makeNavButton("📋  Service History",  "HISTORY"));
        sidebar.add(makeNavButton("💬  Leave a Comment",  "COMMENT"));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    //  CONTENT PANELS
    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(BG_DARK);

        contentPanel.add(buildProfilePanel(), "PROFILE");
        contentPanel.add(buildHistoryPanel(), "HISTORY");
        contentPanel.add(buildCommentPanel(), "COMMENT");

        contentLayout.show(contentPanel, "HISTORY");
        return contentPanel;
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

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(28, 28, 28, 28)
        ));

        // Initialize labels for refreshing
        fullNameLabel = new JLabel();
        emailLabel = new JLabel();
        phoneLabel = new JLabel();

        card.add(makeEditableInfoRow("Full Name",  currentCustomer.getFullName(), fullNameLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(makeInfoRow("Username",   currentCustomer.getUsername()));
        card.add(Box.createVerticalStrut(12));
        card.add(makeEditableInfoRow("Email",      currentCustomer.getEmail(), emailLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(makeEditableInfoRow("Phone",      currentCustomer.getPhone(), phoneLabel));
        card.add(Box.createVerticalStrut(24));

        JButton editBtn = makePrimaryButton("✏  Edit Profile");
        editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        editBtn.addActionListener(e -> {
            JDialog editDialog = buildEditProfileDialog();
            editDialog.setVisible(true);
        });
        card.add(editBtn);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(card,    BorderLayout.CENTER);
        return panel;
    }


    //  PANEL 2 — SERVICE HISTORY
    //  Shows this customer's past appointments + payment status + feedback

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("My Service History");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        String[] cols = {"Appt ID", "Date", "Service Type", "Status", "Feedback", "Paid"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Load appointments for THIS customer only
        // TODO (Member 1): Cross-reference with customers.txt to get the customer ID
        List<Appointment> myAppts = FileHandler.loadAllAppointments()
            .stream()
            // Filter by customer ID — matches if the appointment's customerID
            // links back to this user's customer record
            .filter(a -> {
                // Simple approach: check if the customer's username matches
                // For a full implementation, join via customers.txt
                return a.getCustomerID().equals(currentCustomer.getCustomerID())
                    || a.getCustomerID().equals("C001"); // fallback for demo
            })
            .collect(java.util.stream.Collectors.toList());

        // Load feedbacks and payments for lookup
        List<Feedback> feedbacks = FileHandler.loadAllFeedbacks();
        List<model.Payment> payments = FileHandler.loadAllPayments();

        myAppts.forEach(a -> {
            // Find feedback for this appointment
            String feedbackText = feedbacks.stream()
                .filter(f -> f.getAppointmentID().equals(a.getAppointmentID()))
                .map(f -> f.getFeedbackText())
                .findFirst().orElse("No feedback yet");

            // Find payment for this appointment
            String payStatus = payments.stream()
                .filter(p -> p.getAppointmentID().equals(a.getAppointmentID()))
                .map(p -> "Paid (RM " + String.format("%.2f", p.getAmount()) + ")")
                .findFirst().orElse("Unpaid");

            model.addRow(new Object[]{
                a.getAppointmentID(), a.getDate(), a.getServiceType(),
                a.getStatus(), feedbackText, payStatus
            });
        });

        JTable table = makeStyledTable(model);
        table.getColumnModel().getColumn(4).setPreferredWidth(200); // wider feedback column
        JScrollPane scroll = makeScrollPane(table);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        return panel;
    }


    //  PANEL 3 — LEAVE A COMMENT
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
        FileHandler.loadAllAppointments()
            .stream()
            .filter(a -> "Completed".equals(a.getStatus()))
            .forEach(a -> comboModel.addElement(
                a.getAppointmentID() + " — " + a.getDate() + " (" + a.getServiceType() + ")"
            ));
        if (comboModel.getSize() == 0) comboModel.addElement("No completed appointments");

        JComboBox<String> apptCombo = new JComboBox<>(comboModel);
        apptCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        apptCombo.setBackground(BG_CARD2);
        apptCombo.setForeground(TEXT_PRIMARY);
        apptCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        apptCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        submitBtn.addActionListener(e -> {
            String selectedAppt = (String) apptCombo.getSelectedItem();
            String commentText  = commentArea.getText().trim();

            if (selectedAppt == null || selectedAppt.startsWith("No completed")) {
                errorLbl.setText("No appointment selected."); return;
            }
            if (commentText.isEmpty()) {
                errorLbl.setText("Comment cannot be empty."); return;
            }

            // Extract appointment ID from the combo string (e.g. "A001 — 2025-05-10...")
            String apptID = selectedAppt.split(" — ")[0].trim();

            // TODO (Member 1): Implement file write
            // Steps:
            //   1. Create: new Comment(apptID, currentCustomer.getCustomerID(), commentText)
            //   2. List<Comment> comments = FileHandler.loadAllComments()
            //   3. comments.add(newComment)
            //   4. FileHandler.saveAllComments(comments)
            //   5. Show success, clear the text area

            JOptionPane.showMessageDialog(this,
                "TODO (Member 1): Save comment to comments.txt\n\n" +
                "Appointment: " + apptID + "\n" +
                "Comment: " + commentText);
            commentArea.setText("");
            errorLbl.setText(" ");
        });

        formCard.add(apptLbl);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(apptCombo);
        formCard.add(Box.createVerticalStrut(20));
        formCard.add(commentLbl);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(commentScroll);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(errorLbl);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(submitBtn);

        panel.add(heading,  BorderLayout.NORTH);
        panel.add(formCard, BorderLayout.CENTER);
        return panel;
    }


    //  EDIT PROFILE DIALOG
    private JDialog buildEditProfileDialog() {
        JDialog dialog = new JDialog(this, "Edit Profile", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_CARD);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // First Name
        JLabel firstNameLbl = new JLabel("First Name:");
        firstNameLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        firstNameLbl.setForeground(TEXT_MUTED);
        firstNameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField firstNameField = new JTextField(currentCustomer.getFirstName());
        firstNameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        firstNameField.setBackground(BG_CARD2);
        firstNameField.setForeground(TEXT_PRIMARY);
        firstNameField.setCaretColor(TEXT_PRIMARY);
        firstNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 12, 8, 12)
        ));
        firstNameField.setMaximumSize(new Dimension(300, 36));
        firstNameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Last Name
        JLabel lastNameLbl = new JLabel("Last Name:");
        lastNameLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lastNameLbl.setForeground(TEXT_MUTED);
        lastNameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField lastNameField = new JTextField(currentCustomer.getLastName());
        lastNameField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lastNameField.setBackground(BG_CARD2);
        lastNameField.setForeground(TEXT_PRIMARY);
        lastNameField.setCaretColor(TEXT_PRIMARY);
        lastNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 12, 8, 12)
        ));
        lastNameField.setMaximumSize(new Dimension(300, 36));
        lastNameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Email
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        emailLbl.setForeground(TEXT_MUTED);
        emailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField emailField = new JTextField(currentCustomer.getEmail());
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        emailField.setBackground(BG_CARD2);
        emailField.setForeground(TEXT_PRIMARY);
        emailField.setCaretColor(TEXT_PRIMARY);
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 12, 8, 12)
        ));
        emailField.setMaximumSize(new Dimension(300, 36));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Phone
        JLabel phoneLbl = new JLabel("Phone:");
        phoneLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        phoneLbl.setForeground(TEXT_MUTED);
        phoneLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField phoneField = new JTextField(currentCustomer.getPhone());
        phoneField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        phoneField.setBackground(BG_CARD2);
        phoneField.setForeground(TEXT_PRIMARY);
        phoneField.setCaretColor(TEXT_PRIMARY);
        phoneField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 12, 8, 12)
        ));
        phoneField.setMaximumSize(new Dimension(300, 36));
        phoneField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel passwordLbl = new JLabel("Password:");
        passwordLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passwordLbl.setForeground(TEXT_MUTED);
        passwordLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField passwordField = new JPasswordField(currentCustomer.getPassword());
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passwordField.setBackground(BG_CARD2);
        passwordField.setForeground(TEXT_PRIMARY);
        passwordField.setCaretColor(TEXT_PRIMARY);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setMaximumSize(new Dimension(300, 36));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setForeground(DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cancelBtn.setForeground(TEXT_MUTED);
        cancelBtn.setBackground(new Color(0,0,0,0));
        cancelBtn.setOpaque(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = makePrimaryButton("Save Changes");
        saveBtn.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            // VALIDATION
            if (firstName.isEmpty()) {
                errorLabel.setText("First name cannot be empty.");
                return;
            }
            if (lastName.isEmpty()) {
                errorLabel.setText("Last name cannot be empty.");
                return;
            }
            if (email.isEmpty()) {
                errorLabel.setText("Email cannot be empty.");
                return;
            }
            if (!email.contains("@")) {
                errorLabel.setText("Invalid email format.");
                return;
            }
            if (phone.isEmpty()) {
                errorLabel.setText("Phone cannot be empty.");
                return;
            }
            if (password.isEmpty()) {
                errorLabel.setText("Password cannot be empty.");
                return;
            }
            if (password.length() < 6) {
                errorLabel.setText("Password must be at least 6 characters.");
                return;
            }

            // UPDATE CUSTOMER OBJECT
            currentCustomer.setFirstName(firstName);
            currentCustomer.setLastName(lastName);
            currentCustomer.setEmail(email);
            currentCustomer.setPhone(phone);
            currentCustomer.setPassword(password);

            // SAVE TO users.txt
            boolean usersSaved = FileHandler.updateUserProfile(currentCustomer);
            
            // SAVE TO customers.txt
            boolean customersSaved = FileHandler.updateCustomerProfile(currentCustomer);

            if (usersSaved && customersSaved) {
                // Update UI labels to reflect changes
                fullNameLabel.setText(currentCustomer.getFullName());
                emailLabel.setText(email);
                phoneLabel.setText(phone);
                userLabel.setText("🚗  " + currentCustomer.getFullName() + "  ·  Customer");

                JOptionPane.showMessageDialog(dialog, 
                    "Profile updated successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Error saving profile. Please try again.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        panel.add(firstNameLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(firstNameField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(lastNameLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lastNameField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(emailLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(phoneLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(phoneField);
        panel.add(Box.createVerticalStrut(12));
        panel.add(passwordLbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(errorLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonPanel);

        dialog.add(panel);
        return dialog;
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

    private JPanel makeEditableInfoRow(String label, String value, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(100, 20));
        valueLabel.setText(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        valueLabel.setForeground(TEXT_PRIMARY);
        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);
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
