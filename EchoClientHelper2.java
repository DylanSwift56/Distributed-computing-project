import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyStore;

public class EchoClientHelper2 {

    static final String endMessage = ".";
    private SSLSocket sslSocket;
    private PrintWriter out;
    private BufferedReader in;

    public EchoClientHelper2(String hostName, String portNum) throws Exception {
        int serverPort = Integer.parseInt(portNum);

        // Initialize SSL Context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null); // Use default TrustStore

        SSLSocketFactory factory = sslContext.getSocketFactory();
        sslSocket = (SSLSocket) factory.createSocket(hostName, serverPort);

        // Enable the same TLS versions as the server
        sslSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});

        // Set up input and output streams
        out = new PrintWriter(sslSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

        System.out.println("Securely connected to SSL server on port " + portNum);
    }

    /**
     * Sends a request to the server and returns the server's response.
     *
     * @param request The request message to send to the server.
     * @return The server's response as a String.
     * @throws IOException If an I/O error occurs.
     */
    public String sendRequest(String request) throws IOException {
        out.println(request); // Send request
        out.flush();
        return in.readLine(); // Read server's response
    }

    public String getEcho(String message) throws IOException {
        out.println(message);
        out.flush();
        return in.readLine(); // Receive the echoed message
    }

    public void done() throws IOException {
        out.println(endMessage);
        out.flush();
        sslSocket.close();
    }
}
