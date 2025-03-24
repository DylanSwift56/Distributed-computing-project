import java.io.File;
import java.io.IOException;

public class SMPClientController {
    private EchoClientHelper2 clientHelper;
    private String username;
    private Process serverProcess;

    public SMPClientController(String host, String port) {
        startServer();
        connectToServer(host, port);
    }

    private void startServer() {
        try {
            serverProcess = new ProcessBuilder("java", "EchoServer3")
                           .directory(new File(System.getProperty("user.dir")))
                           .start();
            Thread.sleep(2000);
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    private void connectToServer(String host, String port) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                clientHelper = new EchoClientHelper2(host, port);
                return;
            } catch (IOException e) {
                attempts++;
                System.err.println("Connection attempt " + attempts + " failed");
                try { Thread.sleep(1000); } catch (InterruptedException ie) {}
            }
        }
        System.err.println("Could not connect to server after 3 attempts");
    }


    public String login(String username, String password) {
        if (clientHelper == null) {
            return "Error: Not connected to the server.";
        }
        this.username = username;
        try {
            return clientHelper.sendRequest("LOGIN:" + username + ":" + password);
        } catch (IOException e) {
            return "Error: Unable to login - " + e.getMessage();
        }
    }

    public String uploadMessage(String message) {
        if (clientHelper == null || username == null) {
            return "Error: Not logged in.";
        }
        try {
            return clientHelper.sendRequest("UPLOAD:" + username + ":" + message);
        } catch (IOException e) {
            return "Error: Upload failed - " + e.getMessage();
        }
    }

    public String downloadAllMessages() {
        if (clientHelper == null || username == null) {
            return "Error: Not logged in.";
        }
        try {
            return clientHelper.sendRequest("DOWNLOAD_ALL:" + username);
        } catch (IOException e) {
            return "Error: Failed to retrieve messages - " + e.getMessage();
        }
    }

    public String downloadMessage(String messageId) {
        if (clientHelper == null || username == null) {
            return "Error: Not logged in.";
        }
        try {
            return clientHelper.sendRequest("DOWNLOAD:" + username + ":" + messageId);
        } catch (IOException e) {
            return "Error: Failed to download message - " + e.getMessage();
        }
    }

    public String logout() {
        if (clientHelper == null || username == null) {
            return "Error: Not logged in.";
        }
        try {
            String response = clientHelper.sendRequest("LOGOUT:" + username);
            clientHelper.done();
            username = null;
            return response;
        } catch (IOException e) {
            return "Error: Logout failed - " + e.getMessage();
        }
    }
}
