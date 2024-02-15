import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 9002;

    JFrame frame = new JFrame("Chatter App");
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JTextField textField = new JTextField(40);
    JTextField messageSendTo = new JTextField("Receiver's name",20);
    JTextArea messageArea = new JTextArea(30,40);
    JButton btnSendTo = new JButton("Send To");
    private JComboBox<String> dropdown = new JComboBox<>(new String[]{"text", "VISA", "MASTER"});

    BufferedReader consoleReader;
    BufferedReader serverReader;
    PrintWriter writer;

    String sendMessage;

    public Client() {

        topPanel.add(btnSendTo);
        topPanel.add(messageSendTo);
        topPanel.add(dropdown);
        textField.setEditable(false);
        messageArea.setEditable(false);
        messageSendTo.setEditable(false);
        frame.getContentPane().add(topPanel, "North");
        frame.getContentPane().add(textField, "Center");
        frame.getContentPane().add(new JScrollPane(messageArea), "South");
        frame.pack();

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage = textField.getText();
                String receiver = messageSendTo.getText();

                // send typed message to the server
                writer.println(dropdown.getSelectedIndex() + receiver + ": " + sendMessage);
                // append sent message to the message area
                if (!messageSendTo.getText().equals("Receiver's name")) {
                    messageArea.append("To " + messageSendTo.getText() + ": " + sendMessage + "\n");
                }
                textField.setText("");
            }
        });

    }

    private String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter the IP address of the server : ",
                "Welcome to Chatter App by Shaleel",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name : ",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private String getAllClients() throws IOException {

        writer.println("REQUESTCLIENTS");
        String clients = serverReader.readLine();
        return clients;
    }

    private void openClientsFrame() throws IOException {
        JFrame newFrame = new JFrame("Users");
        JTextField clients = new JTextField("",20);
        clients.setEditable(false);
        newFrame.setSize(300, 200);

        clients.setText(getAllClients());
        System.out.println(getAllClients());

        newFrame.getContentPane().add(new JScrollPane(clients), "North");
        newFrame.pack();

        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        newFrame.setVisible(true);
    }

    public static void main(String[] args) {

        // create new client object for trigger swing UI
        Client client = new Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);

        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT);

            client.consoleReader = new BufferedReader(new InputStreamReader(System.in));
            client.serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            client.writer = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Enter your name : ");
            String userName = client.getName();
            // pass username to server for validate
            client.writer.println(userName);

            // check username accepted by the server
            if (client.serverReader.readLine().equals("ACCEPTED")) {
                System.out.println("Username '" + userName + "' accepted by server");
                client.frame.setTitle("Chatter App | " + userName);
                client.textField.setEditable(true);
                client.messageSendTo.setEditable(true);
            }

            // create new thread for listening receiving messages
            new Thread(()->{
                try {
                    while (true) {
                        String message = client.serverReader.readLine();
                        if (message == null) {
                            break;
                        }
                        if (message.startsWith("0")) {
                            client.messageArea.append(message.substring(1) + "\n");

                        } else {
                            // get message start index from input message
                            int messageStarts = message.indexOf(":") + 2;
                            // split senders name
                            String sender = message.substring(1,messageStarts);
                            if (message.startsWith("1")) {
                                Decoder visa = new Decoder("visapack.xml", message.substring(messageStarts));
                                String decodedVisa = visa.getDecodedMsg().toString();
                                client.messageArea.append(sender + decodedVisa + "\n");

                            } else if (message.startsWith("2")) {
                                Decoder master = new Decoder("mastercard.xml", message.substring(messageStarts));
                                String decodedMaster = master.getDecodedMsg().toString();
                                client.messageArea.append(sender + decodedMaster + "\n");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            System.out.println("Type Exit to disconnect");
            while (true) {
                String message = client.consoleReader.readLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
