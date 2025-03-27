import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyStore;

public class MyStreamSocket {
    private SSLSocket socket;
    private BufferedReader input;
    private PrintWriter output;

    // Constructor for client connections
    public MyStreamSocket(InetAddress acceptorHost, int acceptorPort) throws IOException {
        try {
            // Load client truststore
            KeyStore ts = KeyStore.getInstance("JKS");
            ts.load(new FileInputStream("client.truststore"), "password".toCharArray());

            // Initialize SSL context
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ts);
            
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            
            // Create SSL socket
            SSLSocketFactory factory = sslContext.getSocketFactory();
            this.socket = (SSLSocket) factory.createSocket(acceptorHost, acceptorPort);
            configureSocket();
            setStreams();
        } catch (Exception e) {
            throw new IOException("SSL setup failed: " + e.getMessage());
        }
    }

    // Constructor for server-side accepted connections
    public MyStreamSocket(Socket socket) throws IOException {
        if (!(socket instanceof SSLSocket)) {
            throw new IllegalArgumentException("Socket must be an SSLSocket");
        }
        this.socket = (SSLSocket) socket;
        configureSocket();
        setStreams();
    }

    private void configureSocket() {
        // Enforce TLS 1.2+ and strong ciphers
        socket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
        socket.setEnabledCipherSuites(new String[]{
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
        });
    }

    private void setStreams() throws IOException {
        InputStream inStream = socket.getInputStream();
        this.input = new BufferedReader(new InputStreamReader(inStream));
        
        OutputStream outStream = socket.getOutputStream();
        this.output = new PrintWriter(new OutputStreamWriter(outStream), true);
    }

    public void sendMessage(String message) throws IOException {
        output.println(message);
    }

    public String receiveMessage() throws IOException {
        return input.readLine();
    }

    public void close() throws IOException {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
        } finally {
            if (socket != null) socket.close();
        }
    }

    public String getCipherSuite() {
        return socket.getSession().getCipherSuite();
    }
}