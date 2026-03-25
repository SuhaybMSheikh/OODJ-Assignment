package gui.technician;

import model.Technician;
import model.Appointment;
import model.Feedback;
import util.FileHandler;
import util.Session;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * GUI CLASS — TechnicianDashboard
 * ---------------------------------
 * MEMBER 4 is responsible for implementing all features in this file.
 *
 * FEATURES TO IMPLEMENT:
 *   [1] Edit own profile
 *   [2] View appointments assigned to THIS technician
 *   [3] Click an appointment to see full details + customer comments
 *   [4] Mark an appointment as "Completed"
 *   [5] Write feedback for a completed appointment
 */
public class TechnicianDashboard extends JFrame {

    // ─── COLOURS ─────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(15,  17,  26);
    private static final Color BG_CARD      = new Color(24,  27,  42);
    private static final Color BG_CARD2     = new Color(30,  34,  52);
    private static final Color ACCENT       = new Color(245, 158, 11);   // amber for technicians
    private static final Color TEXT_PRIMARY = new Color(240, 241, 255);
    private static final Color TEXT_MUTED   = new Color(148, 151, 180);
    private static final Color BORDER_COLOR = new Color(55,  58,  80);
    private static final Color DANGER       = new Color(239, 68,  68);
    private static final Color SUCCESS      = new Color(34, 197,  94);

    // ─── STATE ───────────────────────────────────────────────────────────
    private Technician currentTech;

    // ─── LAYOUT ──────────────────────────────────────────────────────────
    private CardLayout contentLayout;
    private JPanel     contentPanel;

    // ─────────────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────────────
    //  TOP BAR
    // ─────────────────────────────────────────────────────────────────────
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

        JLabel userLbl = new JLabel("🔧  " + currentTech.getFullName() + "  ·  Technician");
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

    // ─────────────────────────────────────────────────────────────────────
    //  SIDEBAR
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_CARD);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
            new EmptyBorder(24, 0, 24, 0)
        ));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel section = new JLabel("  TECHNICIAN MENU");
        section.setFont(new Font("SansSerif", Font.BOLD, 10));
        section.setForeground(TEXT_MUTED);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(makeNavButton("👤  My Profile",       "PROFILE"));
        sidebar.add(makeNavButton("📅  My Appointments",  "APPOINTMENTS"));

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  CONTENT PANELS
    // ─────────────────────────────────────────────────────────────────────
    private JPanel buildContent() {
        contentLayout = new CardLayout();
        contentPanel  = new JPanel(contentLayout);
        contentPanel.setBackground(BG_DARK);

        contentPanel.add(buildProfilePanel(),      "PROFILE");
        contentPanel.add(buildAppointmentsPanel(), "APPOINTMENTS");

        contentLayout.show(contentPanel, "APPOINTMENTS");
        return contentPanel;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  PANEL 1 — MY PROFILE
    //  TODO (Member 4): Allow technician to edit their own details
    // ═════════════════════════════════════════════════════════════════════
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

        card.add(makeInfoRow("Full Name",  currentTech.getFullName()));
        card.add(Box.createVerticalStrut(12));
        card.add(makeInfoRow("Username",   currentTech.getUsername()));
        card.add(Box.createVerticalStrut(12));
        card.add(makeInfoRow("Email",      currentTech.getEmail()));
        card.add(Box.createVerticalStrut(12));
        card.add(makeInfoRow("Phone",      currentTech.getPhone()));
        card.add(Box.createVerticalStrut(24));

        JButton editBtn = makePrimaryButton("✏  Edit Profile");
        editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        editBtn.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "TODO (Member 4): Open edit profile dialog.\n" +
                "Fields: first name, last name, email, phone, password.",
                "Edit Profile", JOptionPane.INFORMATION_MESSAGE)
        );
        card.add(editBtn);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(card,    BorderLayout.CENTER);
        return panel;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  PANEL 2 — MY APPOINTMENTS
    //  Split view: list on left, details on right
    //  TODO (Member 4): Load only THIS technician's appointments
    // ═════════════════════════════════════════════════════════════════════
    private JPanel buildAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // ── Left: appointment list ─────────────────────────────────────────
        JPanel leftPane = new JPanel(new BorderLayout(0, 12));
        leftPane.setOpaque(false);
        leftPane.setPreferredSize(new Dimension(420, 0));

        JLabel heading = new JLabel("My Appointments");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(TEXT_PRIMARY);

        String[] cols = {"ID", "Date", "Time", "Service", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Load only THIS technician's appointments
        List<Appointment> myAppointments = FileHandler.loadAllAppointments()
            .stream()
            .filter(a -> a.getTechnicianID().equals(currentTech.getUserID()))
            .collect(java.util.stream.Collectors.toList());

        myAppointments.forEach(a -> model.addRow(new Object[]{
            a.getAppointmentID(), a.getDate(), a.getTime(),
            a.getServiceType(), a.getStatus()
        }));

        JTable table = makeStyledTable(model);
        JScrollPane scroll = makeScrollPane(table);

        leftPane.add(heading, BorderLayout.NORTH);
        leftPane.add(scroll,  BorderLayout.CENTER);

        // ── Right: detail panel ────────────────────────────────────────────
        JPanel rightPane = new JPanel();
        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
        rightPane.setBackground(BG_CARD);
        rightPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel detailHeading = new JLabel("Appointment Details");
        detailHeading.setFont(new Font("SansSerif", Font.BOLD, 16));
        detailHeading.setForeground(TEXT_PRIMARY);
        detailHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detailContent = new JLabel(
            "<html><div style='color:#9497B4;'>" +
            "Select an appointment from the list<br>" +
            "to view its details." +
            "</div></html>");
        detailContent.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detailContent.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Action buttons (only active when an appointment is selected)
        JButton completeBtn = makePrimaryButton("✅  Mark as Completed");
        completeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        completeBtn.setEnabled(false);

        JButton feedbackBtn = makeSecondaryButton("📝  Write Feedback");
        feedbackBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        feedbackBtn.setEnabled(false);

        rightPane.add(detailHeading);
        rightPane.add(Box.createVerticalStrut(16));
        rightPane.add(detailContent);
        rightPane.add(Box.createVerticalStrut(24));
        rightPane.add(completeBtn);
        rightPane.add(Box.createVerticalStrut(10));
        rightPane.add(feedbackBtn);

        // ── Wire table selection to detail panel ───────────────────────────
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row == -1) return;

            String apptID = (String) model.getValueAt(row, 0);
            String status = (String) model.getValueAt(row, 4);

            // Show details (TODO: expand this to show customer name, comments)
            detailContent.setText(
                "<html><div style='line-height:1.8;'>" +
                "<b style='color:#F0F1FF;'>Appointment ID:</b>  " + apptID + "<br>" +
                "<b style='color:#F0F1FF;'>Date:</b>  " + model.getValueAt(row, 1) + "<br>" +
                "<b style='color:#F0F1FF;'>Time:</b>  " + model.getValueAt(row, 2) + "<br>" +
                "<b style='color:#F0F1FF;'>Service:</b>  " + model.getValueAt(row, 3) + "<br>" +
                "<b style='color:#F0F1FF;'>Status:</b>  " + status +
                "<br><br><i style='color:#9497B4;'>TODO: Show customer name and comments here.</i>" +
                "</div></html>"
            );

            // Only allow completing if still Pending
            completeBtn.setEnabled("Pending".equals(status));
            // Only allow feedback if Completed
            feedbackBtn.setEnabled("Completed".equals(status));
        });

        // Mark as Completed button action
        completeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String apptID = (String) model.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                "Mark appointment " + apptID + " as Completed?",
                "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            // TODO (Member 4): Update status in appointments.txt
            // Steps:
            //   1. FileHandler.loadAllAppointments()
            //   2. Find the appointment with matching ID
            //   3. appt.setStatus("Completed")
            //   4. FileHandler.saveAllAppointments(list)
            //   5. Refresh the table row

            model.setValueAt("Completed", row, 4);  // update table display
            completeBtn.setEnabled(false);
            feedbackBtn.setEnabled(true);
            JOptionPane.showMessageDialog(this,
                "TODO (Member 4): Implement file update in appointments.txt",
                "Completed", JOptionPane.INFORMATION_MESSAGE);
        });

        // Write Feedback button action
        feedbackBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            String apptID = (String) model.getValueAt(row, 0);
            openFeedbackDialog(apptID);
        });

        panel.add(leftPane,  BorderLayout.WEST);
        panel.add(rightPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * TODO (Member 4): Open a dialog to enter feedback text.
     * On confirm: write a new row to feedbacks.txt via FileHandler.
     */
    private void openFeedbackDialog(String apptID) {
        JTextArea textArea = new JTextArea(5, 30);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
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
            // TODO (Member 4): Implement file write
            // Steps:
            //   1. Create new Feedback(apptID, currentTech.getUserID(), text)
            //   2. List<Feedback> list = FileHandler.loadAllFeedbacks()
            //   3. list.add(newFeedback)
            //   4. FileHandler.saveAllFeedbacks(list)
            JOptionPane.showMessageDialog(this,
                "TODO (Member 4): Save feedback to feedbacks.txt\n" +
                "Text entered: " + text);
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ═════════════════════════════════════════════════════════════════════
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
        btn.setForeground(new Color(15, 17, 26));
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
