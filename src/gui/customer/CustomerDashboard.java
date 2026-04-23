package gui.customer;

import model.Customer;
import model.Appointment;
import model.Feedback;
import model.Comment;
import model.User;
import util.FileHandler;
import util.Session;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        contentPanel.add(buildProfilePanel(), "PROFILE");
        contentPanel.add(buildHistoryPanel(), "HISTORY");
        contentPanel.add(buildTechnicianFeedbackPanel(), "TECH_FEEDBACK");
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
    //  Shows this customer's past appointments + service fee + payment details

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JLabel heading = new JLabel("My Service History");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        String[] cols = {
            "Appt ID", "Date", "Service Type", "Service Fee (RM)",
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
        Map<String, Double> serviceFees = FileHandler.loadAllServices();
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

            double fee = serviceFees.getOrDefault(a.getServiceType(), 0.0);
            model.Payment payment = paymentByAppointment.get(a.getAppointmentID());
            String payStatus = (payment == null)
                ? "Unpaid"
                : payment.getStatus() + " on " + payment.getDate();

            model.addRow(new Object[]{
                a.getAppointmentID(),
                a.getDate(),
                a.getServiceType(),
                String.format("%.2f", fee),
                a.getStatus(),
                customerComment,
                payStatus
            });
        });

        JTable table = makeStyledTable(model);
        table.getColumnModel().getColumn(2).setPreferredWidth(110); // service type
        table.getColumnModel().getColumn(3).setPreferredWidth(110); // service fee
        table.getColumnModel().getColumn(5).setPreferredWidth(220); // comments
        table.getColumnModel().getColumn(6).setPreferredWidth(170); // payment status
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
        if (comboModel.getSize() == 0) comboModel.addElement("No completed appointments");

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

        apptCombo.addActionListener(e -> {
            String selected = (String) apptCombo.getSelectedItem();
            if (selected == null || selected.startsWith("No completed")) {
                commentArea.setText("");
                return;
            }
            String selectedApptID = selected.split(" — ")[0].trim();
            commentArea.setText(existingCommentByAppointment.getOrDefault(selectedApptID, ""));
        });
        if (comboModel.getSize() > 0) {
            String firstSelected = (String) comboModel.getSelectedItem();
            if (firstSelected != null && !firstSelected.startsWith("No completed")) {
                String firstApptID = firstSelected.split(" — ")[0].trim();
                commentArea.setText(existingCommentByAppointment.getOrDefault(firstApptID, ""));
            }
        }

        JLabel errorLbl = new JLabel(" ");
        errorLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLbl.setForeground(DANGER);
        errorLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton submitBtn = makePrimaryButton("Submit Comment");
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.addActionListener(e -> {
            String selectedAppt = (String) apptCombo.getSelectedItem();
            String commentText  = commentArea.getText().trim();
            String customerID = resolveCustomerID();

            if (selectedAppt == null || selectedAppt.startsWith("No completed")) {
                errorLbl.setText("No appointment selected."); return;
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

            List<Comment> comments = FileHandler.loadAllComments();
            Comment existingComment = comments.stream()
                .filter(c -> apptID.equals(c.getAppointmentID()) && customerID.equals(c.getCustomerID()))
                .findFirst()
                .orElse(null);
            if (existingComment != null) {
                existingComment.setCommentText(sanitizedComment);
            } else {
                comments.add(new Comment(apptID, customerID, sanitizedComment));
            }
            FileHandler.saveAllComments(comments);
            existingCommentByAppointment.put(apptID, sanitizedComment);

            JOptionPane.showMessageDialog(this,
                "Comment saved successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
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
        btn.addActionListener(e -> {
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

    private void refreshHistoryPanel() {
        contentPanel.remove(1); // index 1 is always HISTORY panel in buildContent
        contentPanel.add(buildHistoryPanel(), "HISTORY", 1);
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

    private JScrollPane makeScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_CARD);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        return sp;
    }
}
