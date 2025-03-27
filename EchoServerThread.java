import javax.net.ssl.*;
import java.io.*;

class EchoServerThread implements Runnable {
    private SSLSocket mySocket;
    private BufferedReader in;
    private PrintWriter out;

    EchoServerThread(SSLSocket mySocket) {
        this.mySocket = mySocket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
            out = new PrintWriter(mySocket.getOutputStream(), true);

            System.out.println("SSL connection established with: " + mySocket.getInetAddress());

            String message;
            while ((message = in.readLine()) != null) {
                if (message.trim().equals(".")) {
                    System.out.println("Session over.");
                    break;
                }
                System.out.println("Received: " + message);
                out.println("Echo: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mySocket.close();
            } catch (IOException ignored) {}
        }
    }
}
