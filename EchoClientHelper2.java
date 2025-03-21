import java.net.*;
import java.io.*;

/**
 * This class is a module which provides the application logic
 * for an Echo client using stream-mode socket.
 * @author M. L. Liu
 */

public class EchoClientHelper2 {

    static final String endMessage = ".";
    private MyStreamSocket mySocket;
    private InetAddress serverHost;
    private int serverPort;

    EchoClientHelper2(String hostName, String portNum) throws SocketException,
            UnknownHostException, IOException {

        this.serverHost = InetAddress.getByName(hostName);
        this.serverPort = Integer.parseInt(portNum);
        // Instantiates a stream-mode socket and wait for a connection.
        this.mySocket = new MyStreamSocket(this.serverHost, this.serverPort);
        System.out.println("Connection request made");
    } // end constructor

    /**
     * Sends a request to the server and returns the server's response.
     *
     * @param request The request message to send to the server.
     * @return The server's response as a String.
     * @throws SocketException If a socket error occurs.
     * @throws IOException If an I/O error occurs.
     */
    public String sendRequest(String request) throws SocketException, IOException {
        mySocket.sendMessage(request); // Send the request to the server
        return mySocket.receiveMessage(); // Receive the server's response
    }

    public String getEcho(String message) throws SocketException, IOException {
        String echo = "";
        mySocket.sendMessage(message);
        // now receive the echo
        echo = mySocket.receiveMessage();
        return echo;
    } // end getEcho

    public void done() throws SocketException, IOException {
        mySocket.sendMessage(endMessage);
        mySocket.close();
    } // end done
} //end class