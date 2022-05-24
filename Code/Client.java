import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.ref.Cleaner;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client extends JFrame implements ActionListener, Runnable {
    PrintWriter pw;
    Socket socket;
    BufferedReader bf;

    JMenuBar bar = new JMenuBar();
    JTextField textField = new JTextField();

    JTextArea textArea = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(textArea);
    JPanel panel = new JPanel(new BorderLayout());
    JLabel label = new JLabel("Enter Message > ");

    JButton logButton = new JButton("Save messages to file");
    public static void main(String[] args) throws IOException {
        new Client().serverConnect();
    }

    /**
     * Function to create the GUI of the application
     */
    protected Client() {
        super("Advanced Networking Chat App");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Font font = new Font("White", Font.PLAIN, 14);
        setFont(font);

        Color foreColor = new Color(1,36,86);
        setForeground(Color.BLUE);


        textArea.setToolTipText("Message Area");
        Color textAreaForeColor = new Color(255,255,255);
        textArea.setForeground(textAreaForeColor);
        Font textAreaFont = new Font("Monospaced", Font.BOLD, 15);
        textArea.setFont(textAreaFont);
        textArea.setBackground(foreColor);
        textArea.setEditable(false);

        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String contents = textArea.getText();
                try {
                    logMessages(contents);
                } catch (IOException ex) {
                    System.out.println("Error while writing to the log file: " + ex);
                }
            }
        });
        bar.add(logButton);
        setJMenuBar(bar);



        textField.setCaretColor(new Color(255,255,255));
        textField.setForeground(new Color(255,255,255));

        Font textFieldFont = new Font("Tahoma", Font.BOLD,12);
        textField.setFont(textFieldFont);

        textField.setText("");
        Color textFieldBack = new Color(1,36,86);
        textField.setBackground(textFieldBack);
        textField.setToolTipText("Enter your message");
        textField.requestFocus();
        textField.addActionListener(this);

        getContentPane().add(scrollPane, "Center");
        label.setOpaque(true);
        label.setBackground(new Color(1,36,86));
        label.setForeground(new Color(255,255,255));
        panel.add(label,BorderLayout.WEST);
        panel.add(textField,BorderLayout.CENTER);

        getContentPane().add(panel, "South");
        setSize(450,500);
        setVisible(true);


    }
    /**
     * Function for creating the file to log the message history
     */
    protected void logMessages(String content) throws IOException {
        FileWriter fileWriter = new FileWriter("messageLog.txt");
        fileWriter.write(content);
        fileWriter.close();
        System.out.println("Content saved - File created");
    }

    /**
     * Function to connect the client with the server over TCP connection
     */
    protected void serverConnect() throws IOException {
        try {
            String addr = JOptionPane.showInputDialog(this, "Enter server IP address", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("Connection from: " + addr);
            socket = new Socket(addr,2222);

            bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(),true);
            pw.println(":check");  //send id to the server

            new Thread(this).start();

        } catch (IOException e) {
            System.out.println(e + " Socket Connection error");
        }
    }

    /**
     * Function to handle content transmission
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String message = textField.getText();
        //send message to the server
        pw.println(message);
        textField.setText("");
    }


    /**
     * Thread running and perform connection checks
     */
    @Override
    public void run() {
        String msg;
        boolean checked = false;
        try {
            while((msg = bf.readLine()) != null) {
                if(!checked ) {
                    List<String> ids = Arrays.asList(msg.split("\\s+"));

                    String id = JOptionPane.showInputDialog(this, "Enter user id: ", JOptionPane.INFORMATION_MESSAGE);

                    while(ids.contains(id) || id.contains(" ")) {
                        id = JOptionPane.showInputDialog(this, "This id is already taken or contains invalid whitespace, please choose a different one", JOptionPane.INFORMATION_MESSAGE);
                    }

                    pw.println(":ok");  //send id to the server
                    if(bf.readLine().equals("ACK")) {
                        checked = true;
                        pw.println(id);  //send id to the server
                    }
                } else {
                    textArea.append(msg +"\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }

            }
        } catch (IOException e) {
            System.out.println("Client failed: " + e );
        }

    }
}



