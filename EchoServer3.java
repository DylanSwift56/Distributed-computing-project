import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyStore;

public class EchoServer3 {
    private static final int PORT = 7;

    public static void main(String[] args) {
        try {
            // Set up SSL context
            System.setProperty("javax.net.ssl.keyStore", "server.keystore");
            System.setProperty("javax.net.ssl.keyStorePassword", "password");
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(getKeyManagers(), null, null);

            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);
            serverSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});

            System.out.println("Server started on port " + PORT);

            while (true) {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                System.out.println("Client connected from " + socket.getInetAddress());

                // Handle each client connection in a new thread
                new ClientHandler(socket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Returns an array of KeyManagers (you can load your key store here)
    private static KeyManager[] getKeyManagers() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream("server.keystore"), "password".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "password".toCharArray());
        return kmf.getKeyManagers();
    }

    static class ClientHandler extends Thread {
        private SSLSocket socket;
        private PrintWriter out;
        private BufferedReader in;
    
        public ClientHandler(SSLSocket socket) {
            this.socket = socket;
        }
    
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
    
                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    System.out.println("Received: " + clientMessage);
    
                    if (clientMessage.startsWith("LOGIN")) {
                        out.println("101: Login successful");  // Send login success
                        System.out.println("User logged in.");
                    } else if (clientMessage.startsWith("UPLOAD")) {
                        out.println("201: Message uploaded");
                        System.out.println("Message uploaded.");
                    } else if (clientMessage.startsWith("DOWNLOAD_ALL")) {
                        out.println("301: Here are all messages...");
                        System.out.println("Sent all messages.");
                    } else {
                        out.println("400: Invalid command"); 
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}
