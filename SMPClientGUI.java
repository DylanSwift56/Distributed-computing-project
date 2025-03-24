import javax.swing.*;
import java.awt.*;

public class SMPClientGUI {
    private JFrame frame;
    private JTextField usernameField, passwordField, messageField, messageIdField;
    private JTextArea responseArea;
    private JButton loginButton, uploadButton, downloadAllButton, downloadButton, logoutButton;
    private SMPClientController clientController;

    public SMPClientGUI() {
        frame = new JFrame("Secure Message Passing (SMP) Client");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        initializeComponents();

        //Start the server and connect to it
        startServerAndConnect();

        frame.setVisible(true);
    }

    private void initializeComponents() {
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Login"));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);

        JPanel messagePanel = new JPanel(new GridLayout(4, 2));
        messagePanel.setBorder(BorderFactory.createTitledBorder("Message Operations"));
        messageField = new JTextField();
        messageIdField = new JTextField();
        uploadButton = new JButton("Upload Message");
        downloadAllButton = new JButton("Download All Messages");
        downloadButton = new JButton("Download Message");
        logoutButton = new JButton("Logout");

        messagePanel.add(new JLabel("Message:"));
        messagePanel.add(messageField);
        messagePanel.add(uploadButton);
        messagePanel.add(downloadAllButton);
        messagePanel.add(new JLabel("Message ID:"));
        messagePanel.add(messageIdField);
        messagePanel.add(downloadButton);
        messagePanel.add(logoutButton);

        responseArea = new JTextArea();
        responseArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(responseArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Server Response"));

        frame.add(loginPanel, BorderLayout.NORTH);
        frame.add(messagePanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);

        addEventListeners();
        enableMessageOperations(false);
    }

    private void startServerAndConnect() {
        responseArea.append("Starting server...\n");
        try {
            Runtime.getRuntime().exec("java EchoServer3");
            Thread.sleep(2000);
            
            responseArea.append("Connecting to server...\n");
            clientController = new SMPClientController("localhost", "7");
            responseArea.append("Connection successful!\n");
        } catch (Exception e) {
            responseArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private void addEventListeners() {
        loginButton.addActionListener(e -> handleLogin());
        uploadButton.addActionListener(e -> handleUpload());
        downloadAllButton.addActionListener(e -> handleDownloadAll());
        downloadButton.addActionListener(e -> handleDownload());
        logoutButton.addActionListener(e -> handleLogout());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String response = clientController.login(username, password);
        SwingUtilities.invokeLater(() -> responseArea.append(response + "\n"));

        if (response.startsWith("101")) {
            enableMessageOperations(true);
        }
    }

    private void handleUpload() {
        String message = messageField.getText();
        if (message.isEmpty()) {
            SwingUtilities.invokeLater(() -> responseArea.append("Error: Message cannot be empty.\n"));
            return;
        }

        String response = clientController.uploadMessage(message);
        SwingUtilities.invokeLater(() -> responseArea.append(response + "\n"));
    }

    private void handleDownloadAll() {
        String response = clientController.downloadAllMessages();
        SwingUtilities.invokeLater(() -> responseArea.append(response + "\n"));
    }

    private void handleDownload() {
        String messageId = messageIdField.getText();
        if (messageId.isEmpty()) {
            SwingUtilities.invokeLater(() -> responseArea.append("Error: Message ID cannot be empty.\n"));
            return;
        }

        String response = clientController.downloadMessage(messageId);
        SwingUtilities.invokeLater(() -> responseArea.append(response + "\n"));
    }

    private void handleLogout() {
        String response = clientController.logout();
        SwingUtilities.invokeLater(() -> responseArea.append(response + "\n"));
        enableMessageOperations(false);
    }

    private void enableMessageOperations(boolean enable) {
        uploadButton.setEnabled(enable);
        downloadAllButton.setEnabled(enable);
        downloadButton.setEnabled(enable);
        logoutButton.setEnabled(enable);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SMPClientGUI());
    }
}
