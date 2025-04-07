import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;

public class SMPClientController {
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;
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

            //Wait for the server to be ready before attempting a connection
            waitForServerToStart(2000); 
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    private void waitForServerToStart(int waitTime) {
        try {
            Thread.sleep(waitTime); 
            System.out.println("Waiting for server to start...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void connectToServer(String host, String port) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                //set some ssl properties
                System.setProperty("javax.net.ssl.trustStore", "client.truststore");
                System.setProperty("javax.net.ssl.trustStorePassword", "password");

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                SSLSocketFactory factory = sslContext.getSocketFactory();

                //attempt to connect  to the server
                socket = (SSLSocket) factory.createSocket(host, Integer.parseInt(port));
                socket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});

                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                System.out.println("Connected to SSL server on port " + port);
                return;
            } catch (Exception e) {
                attempts++;
                System.err.println("Connection attempt " + attempts + " failed: " + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        System.err.println("Could not connect to SSL server after 3 attempts");
    }

    public String sendRequest(String request) {
        if (socket == null || out == null || in == null) {
            return "Error: Not connected to the server.";
        }
        try {
            out.println(request);
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Communication failure - " + e.getMessage();
        }
    }

    public String login(String username, String password) {
        this.username = username;
        return sendRequest("LOGIN:" + username + ":" + password);
    }

    public String uploadMessage(String message) {
        return username != null ? sendRequest("UPLOAD:" + username + ":" + message) : "Error: Not logged in.";
    }

    public String downloadAllMessages() {
        return username != null ? sendRequest("DOWNLOAD_ALL:" + username) : "Error: Not logged in.";
    }

    public String downloadMessage(String messageId) {
        return username != null ? sendRequest("DOWNLOAD:" + username + ":" + messageId) : "Error: Not logged in.";
    }

    public String logout() {
        if (username == null) return "Error: Not logged in.";
        String response = sendRequest("LOGOUT:" + username);
        username = null;
        return response;
    }
}
