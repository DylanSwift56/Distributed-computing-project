import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class SMPClientGUI {
    private JFrame frame;
    private JTextField usernameField, passwordField, messageField, messageIdField;
    private JTextArea responseArea;
    private JButton loginButton, uploadButton, downloadAllButton, downloadButton, logoutButton;
    private EchoClientHelper2 helper;
    private String username = "";

    public SMPClientGUI() {
        frame = new JFrame("Secure Message Passing (SMP) Client");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Login Panel
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

        // Message Operations Panel
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

        // Response Area
        responseArea = new JTextArea();
        responseArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(responseArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Server Response"));

        frame.add(loginPanel, BorderLayout.NORTH);
        frame.add(messagePanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);

        addEventListeners();
        enableMessageOperations(false); // Disable buttons until login
        frame.setVisible(true);
    }

    private void addEventListeners() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUpload();
            }
        });

        downloadAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDownloadAll();
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDownload();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });
    }

    private void handleLogin() {
        String hostName = JOptionPane.showInputDialog("Enter server host:", "localhost");
        String portNum = JOptionPane.showInputDialog("Enter server port:", "7");

        if (hostName == null || portNum == null || hostName.isEmpty() || portNum.isEmpty()) {
            responseArea.append("Error: Host and port cannot be empty.\n");
            return;
        }

        try {
            helper = new EchoClientHelper2(hostName, portNum);
            username = usernameField.getText();
            String password = passwordField.getText();
            String response = helper.sendRequest("LOGIN:" + username + ":" + password);
            responseArea.append(response + "\n");

            if (response.startsWith("101")) {
                enableMessageOperations(true);
            }
        } catch (IOException e) {
            responseArea.append("Error connecting to server: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void handleUpload() {
        String message = messageField.getText();
        if (message.isEmpty()) {
            responseArea.append("Error: Message cannot be empty.\n");
            return;
        }

        try {
            String response = helper.sendRequest("UPLOAD:" + username + ":" + message);
            responseArea.append(response + "\n");
        } catch (IOException e) {
            responseArea.append("Error uploading message: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void handleDownloadAll() {
        try {
            String response = helper.sendRequest("DOWNLOAD_ALL:" + username);
            responseArea.append(response + "\n");
        } catch (IOException e) {
            responseArea.append("Error retrieving messages: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void handleDownload() {
        String messageId = messageIdField.getText();
        if (messageId.isEmpty()) {
            responseArea.append("Error: Message ID cannot be empty.\n");
            return;
        }

        try {
            String response = helper.sendRequest("DOWNLOAD:" + username + ":" + messageId);
            responseArea.append(response + "\n");
        } catch (IOException e) {
            responseArea.append("Error downloading message: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        try {
            String response = helper.sendRequest("LOGOUT:" + username);
            responseArea.append(response + "\n");
            helper.done();
            enableMessageOperations(false);
        } catch (IOException e) {
            responseArea.append("Error logging out: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
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
