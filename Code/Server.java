import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
    public ArrayList<Participant> connections = new ArrayList<>();
    protected Socket socket;
    protected InetAddress ip;
    protected ServerSocket sSocket;

    public static void main(String[] args) {
        new Server();
    }

    /**
     * Function to set up the server and listen for connections
     */
    protected Server() {
        try {
            //Server's ip address
            ip = InetAddress.getLocalHost();
            System.out.println("Server is connected with ip: " + ip.getHostAddress());
            int port = 2222;
            sSocket = new ServerSocket(2222,40,ip);

            System.out.println("Server is waiting for connections on port: " + port);
            Client.main(null);
            while(1==1) {
                //Once accepted run the participant thread
                socket = sSocket.accept();

                Participant sThread = new Participant(this);
                connections.add(sThread);

                System.out.println(socket.getInetAddress() + " is connected!");

                sThread.start();
            }
        } catch (IOException e) {
            System.out.println("Error in server: " + e);
        }
    }

    /**
     * Function to transmit the messages over the connected users
     */
    protected void transmitMessage(String msg) {
        for (int i=0; i<connections.size(); i++) {
            connections.get(i).pw.println(msg);
        }
    }

    /**
     * Return connected participants
     */
    protected String getParticipants() {
        String msg = "";
        for (int i=0; i<connections.size(); i++) {
            msg = msg + " " + connections.get(i).id;
        }
        return msg;
    }

}

/**
 * Thread for client management
 */
class Participant extends  Thread {

    String id;
    Server server;
    PrintWriter pw;
    boolean checked;

    public Participant(Server server) {
        this.server = server;
        checked = false;
    }

    public void run() {
        try {

            BufferedReader bf;
            bf = new BufferedReader(new InputStreamReader(server.socket.getInputStream()));

            pw = new PrintWriter(server.socket.getOutputStream(),true);


            String str;

            while ((str= bf.readLine()) !=null) {
                if(str.equals(":check") && !checked ) {
                    server.connections.get(server.connections.indexOf(this)).pw.println(server.getParticipants());
                    String answer = bf.readLine();
                    if(answer.equals(":ok")) {
                        server.connections.get(server.connections.indexOf(this)).pw.println("ACK");
                        checked = true;
                        id = bf.readLine();
                        server.transmitMessage("--- " + id + " --- joined the chat!");

                    }

                } else if(str.equals(":show")) {
                    String message ="Participants are:\n";
                    for (int i=0; i<server.connections.size(); i++) {
                        message = message + server.connections.get(i).id + "\n";
                    }
                    pw.println(message);
                    pw.flush();
                } else {
                    server.transmitMessage(id + ": " + str);
                }
            }

            server.connections.remove(this);

            server.transmitMessage("--- " + id + " --- left the chat!");

        } catch (IOException e) {
            //If something goes wrong or client leaves remove the thread from the connection list
            server.connections.remove(this);

            server.transmitMessage("--- " + id + " --- left the chat!");

            System.out.println(e);
        }
    }
}