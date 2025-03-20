import java.io.*;
import java.net.*;

public class EchoServer3 {
    public static void main(String[] args) {
        int serverPort = 7;    // default port
        if (args.length == 1)
            serverPort = Integer.parseInt(args[0]);
        try {
            ServerSocket myConnectionSocket = new ServerSocket(serverPort);
            System.out.println("SMP server ready.");
            while (true) {
                System.out.println("Waiting for a connection.");
                MyStreamSocket myDataSocket = new MyStreamSocket(myConnectionSocket.accept());
                System.out.println("Connection accepted.");
                Thread theThread = new Thread(new SMPHandler(myDataSocket));
                theThread.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class SMPHandler implements Runnable {
    private MyStreamSocket myDataSocket;

    public SMPHandler(MyStreamSocket myDataSocket) {
        this.myDataSocket = myDataSocket;
    }

    public void run() {
        try {
            while (true) {
                String request = myDataSocket.receiveMessage();
                if (request == null || request.equals(".")) {
                    break; // End of session
                }

                String[] parts = request.split(":");
                String command = parts[0];
                String username = parts.length > 1 ? parts[1] : null;
                String response = "";

                try {
                    switch (command) {
                        case "LOGIN":
                            if (parts.length < 3) {
                                response = "400: Invalid LOGIN command format";
                            } else {
                                String password = parts[2];
                                response = handleLogin(username, password);
                            }
                            break;
                        case "UPLOAD":
                            if (parts.length < 3) {
                                response = "400: Invalid UPLOAD command format";
                            } else {
                                String messageContent = parts[2];
                                response = handleUpload(username, messageContent);
                            }
                            break;
                        case "DOWNLOAD_ALL":
                            response = handleDownloadAll(username);
                            break;
                        case "DOWNLOAD":
                            if (parts.length < 3) {
                                response = "400: Invalid DOWNLOAD command format";
                            } else {
                                String messageId = parts[2];
                                response = handleDownload(username, messageId);
                            }
                            break;
                        case "LOGOUT":
                            response = handleLogout(username);
                            break;
                        default:
                            response = "500: Invalid command";
                    }
                } catch (Exception e) {
                    response = "500: Internal server error";
                    e.printStackTrace();
                }

                myDataSocket.sendMessage(response);
            }
            myDataSocket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String handleLogin(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return "400: Username and password cannot be empty";
        }
        File userFolder = new File(username);
        if (!userFolder.exists()) {
            if (!userFolder.mkdir()) {
                return "102: Login failed";
            }
        }
        return "101: Login successful";
    }

    private String handleUpload(String username, String messageContent) {
        if (messageContent == null || messageContent.isEmpty()) {
            return "400: Message content cannot be empty";
        }
        String messageId = String.valueOf(System.currentTimeMillis());
        File messageFile = new File(username + "/" + messageId + ".txt");
        try (FileWriter writer = new FileWriter(messageFile)) {
            writer.write(messageContent);
            return "201: Message stored successfully with ID " + messageId;
        } catch (IOException e) {
            return "202: Message storage failed";
        }
    }

    private String handleDownloadAll(String username) {
        File userFolder = new File(username);
        if (!userFolder.exists() || userFolder.listFiles() == null || userFolder.listFiles().length == 0) {
            return "302: No messages found";
        }
        StringBuilder messageList = new StringBuilder();
        for (File file : userFolder.listFiles()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String content = reader.readLine();
                messageList.append(file.getName().replace(".txt", "")).append(": ").append(content).append("|");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "301: Messages retrieved | " + messageList.toString();
    }

    private String handleDownload(String username, String messageId) {
        File messageFile = new File(username + "/" + messageId + ".txt");
        if (!messageFile.exists()) {
            return "312: Message not found";
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(messageFile))) {
            String content = reader.readLine();
            return "311: Message retrieved | " + content;
        } catch (IOException e) {
            return "312: Message not found";
        }
    }

    private String handleLogout(String username) {
        return "401: Logout successful";
    }
}