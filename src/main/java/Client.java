import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 9002;
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    JFrame frame = new JFrame("Chatter App");
    //panel 1
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JPanel topPanelField1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JPanel topPanelField2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton btnSendTo = new JButton("Send To :");
    JButton btnMessageType = new JButton("Message Type :");
    private JComboBox<String> dropdownMessageType = new JComboBox<>(new String[]{"Text", "VISA", "MASTER"});
    private JComboBox<String> clientsDropdown = new JComboBox<>(new String[]{"Receiver"});
    // panel 2
    JPanel secondPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JPanel secondPanelField2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JCheckBox checkBoxBroadcast = new JCheckBox("Broadcast");
    JTextField textField = new JTextField(45);
    // panel 3
    JPanel thirdPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JTextArea messageArea = new JTextArea(20,50);

    BufferedReader consoleReader;
    BufferedReader serverReader;
    PrintWriter writer;

    String sendMessage;
    private static String userName;
    public static FileHandler fileHandler;

    public Client() {

        try {
            fileHandler = new FileHandler("client.log", true); // true for append mode
            fileHandler.setLevel(Level.ALL);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up file handler: " + e.getMessage(), e);
        }

        topPanel.setLayout(new BorderLayout());
        topPanelField1.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanelField1.add(btnSendTo);
        topPanelField1.add(clientsDropdown);
        topPanelField2.setLayout(new FlowLayout(FlowLayout.RIGHT));
        topPanelField2.add(btnMessageType);
        topPanelField2.add(dropdownMessageType);
        topPanel.add(topPanelField1, BorderLayout.WEST);
        topPanel.add(topPanelField2, BorderLayout.EAST);
        clientsDropdown.setPreferredSize(new Dimension(150, 25));

        secondPanel.setLayout(new BorderLayout());
        secondPanelField2.setLayout(new FlowLayout(FlowLayout.LEFT));
        secondPanelField2.add(textField);
        textField.setPreferredSize(new Dimension(0, 25));
        textField.setEditable(false);
        secondPanel.add(checkBoxBroadcast, BorderLayout.WEST);
        secondPanel.add(secondPanelField2, BorderLayout.EAST);

        thirdPanel.setPreferredSize(new Dimension((messageArea.getPreferredSize().width+12), (messageArea.getPreferredSize().height+12)));
        thirdPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        messageArea.setEditable(false);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(secondPanel, BorderLayout.CENTER);
        frame.add(thirdPanel, BorderLayout.SOUTH);
        frame.pack();

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage = textField.getText();
                String receiver = clientsDropdown.getItemAt(clientsDropdown.getSelectedIndex());

                // send typed message to the server
                if (checkBoxBroadcast.isSelected()) {
                    writer.println(dropdownMessageType.getSelectedIndex() + "BROADCAST" + ": " + sendMessage);
                } else {
                    writer.println(dropdownMessageType.getSelectedIndex() + receiver + ": " + sendMessage);
                }
                // append sent message to the message area
                if (!receiver.equals("Receiver") || !receiver.equals(userName)) {
                    if (checkBoxBroadcast.isSelected()) {
                        messageArea.append("To " + "all" + ": " + sendMessage + "\n");
                    } else {
                        messageArea.append("To " + receiver + ": " + sendMessage + "\n");
                    }
                }
                textField.setText("");
                logger.info("Sent message to " + receiver + ": " + sendMessage);
            }
        });

        checkBoxBroadcast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkBoxBroadcast.isSelected()) {
                    logger.info("Broadcast selected");
                    clientsDropdown.setEnabled(false);
                } else {
                    logger.info("Broadcast unselected");
                    clientsDropdown.setEnabled(true);
                }
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
        String name = JOptionPane.showInputDialog(
                frame,
                "Choose a screen name : ",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE
        );
        logger.info("Username chosen: " + name);
        return name;
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

            userName = client.getName();
            // pass username to server for validate
            client.writer.println(userName);
            logger.info("Pass username '" + userName + "' to server for validate");

            // check if username accepted by the server
            if (client.serverReader.readLine().startsWith("ACCEPTED")) {
                logger.info("Username '" + userName + "' accepted by server");
                client.frame.setTitle("Chatter App | " + userName);
                client.textField.setEditable(true);

                client.writer.println("REQUEST");

            }

            // create new thread for listening receiving messages
            new Thread(()->{
                try {
                    while (true) {
                        String message = client.serverReader.readLine();
                        if (message == null) {
                            logger.warning("Null message !");
                            break;
                        }
                        if (message.startsWith("0")) {
                            client.messageArea.append(message.substring(1) + "\n");
                            logger.info("Append message to message area");

                        } else {
                            // get message start index from input message
                            int messageStarts = message.indexOf(":") + 2;
                            // split senders name
                            String sender = message.substring(1,messageStarts);
                            if (message.startsWith("1")) {
                                Decoder visa = new Decoder("visapack.xml", message.substring(messageStarts));
                                String decodedVisa = visa.getDecodedMsg().toString();
                                client.messageArea.append(sender + decodedVisa + "\n");
                                logger.info("VISA ISO8583 Message successfully decoded");

                            } else if (message.startsWith("2")) {
                                Decoder master = new Decoder("mastercard.xml", message.substring(messageStarts));
                                String decodedMaster = master.getDecodedMsg().toString();
                                client.messageArea.append(sender + decodedMaster + "\n");
                                logger.info("MASTER ISO8583 Message successfully decoded");

                            } else if (message.startsWith("USER")) {
                                client.clientsDropdown.removeAllItems();
                                String users = message.substring(message.indexOf("[") + 1, message.indexOf("]"));
                                // Split the string based on comma
                                String[] usersArray = users.split(", ");

                                for (String user : usersArray) {
                                    client.clientsDropdown.addItem(user);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error received message: " + e.getMessage(), e);
                }
            }).start();

//            new Thread(() -> {
//                try {
//                    logger.info("Receiver Thread started");
//                    while (true) {
//                        String clientList = client.serverReader.readLine();
//                        if (clientList.startsWith("USER")) {
////                           client.clientsDropdown.addItem(clientList.substring(8));
////                           client.messageArea.append(clientList.substring(4));
//                            logger.info(clientList);
//                        }
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }).start();

            System.out.println("Type Exit to disconnect");
            while (true) {
                String message = client.consoleReader.readLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            socket.close();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error starting message listening Thread: " + e.getMessage(), e);
        }

    }
}
