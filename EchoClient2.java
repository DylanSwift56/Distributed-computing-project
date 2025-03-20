import java.io.*;

public class EchoClient2 {
    static final String endMessage = ".";
    public static void main(String[] args) {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        try {
            System.out.println("Welcome to the SMP client.\n" +
                    "What is the name of the server host?");
            String hostName = br.readLine();
            if (hostName.length() == 0)
                hostName = "localhost";
            System.out.println("What is the port number of the server host?");
            String portNum = br.readLine();
            if (portNum.length() == 0)
                portNum = "7";
            EchoClientHelper2 helper = new EchoClientHelper2(hostName, portNum);
            boolean done = false;

            //Login Logic
            System.out.print("Enter username: ");
            String username = br.readLine();
            System.out.print("Enter password: ");
            String password = br.readLine();
            String loginResponse = helper.sendRequest("LOGIN:" + username + ":" + password);
            System.out.println(loginResponse);

            if (loginResponse.startsWith("101")) {
                while (!done) {
                    System.out.println("Enter a command (UPLOAD, DOWNLOAD_ALL, DOWNLOAD, LOGOUT) or a single period to quit:");
                    String command = br.readLine();
                    if (command.equals(endMessage)) {
                        done = true;
                        helper.done();
                    } else if (command.equals("UPLOAD".toLowerCase())) {
                        System.out.print("Enter message to upload: ");
                        String message = br.readLine();
                        String uploadResponse = helper.sendRequest("UPLOAD:" + username + ":" + message);
                        System.out.println(uploadResponse);
                    } else if (command.equals("DOWNLOAD_ALL".toLowerCase())) {
                        String downloadAllResponse = helper.sendRequest("DOWNLOAD_ALL:" + username);
                        System.out.println(downloadAllResponse);
                    } else if (command.equals("DOWNLOAD".toLowerCase())) {
                        System.out.print("Enter message ID to download: ");
                        String messageId = br.readLine();
                        String downloadResponse = helper.sendRequest("DOWNLOAD:" + username + ":" + messageId);
                        System.out.println(downloadResponse);
                    } else if (command.equals("LOGOUT".toLowerCase())) {
                        String logoutResponse = helper.sendRequest("LOGOUT:" + username);
                        System.out.println(logoutResponse);
                        done = true;
                        helper.done();
                    } else {
                        System.out.println("Invalid command.");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}