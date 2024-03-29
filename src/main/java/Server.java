import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

public class Server {

    private static final int PORT = 9002;
    public static Map<String, ClientHandler> clients = new HashMap<>();
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static ClientHandler clientHandler;
    public static FileHandler fileHandler;

    static {
        // Configure logger with file handler
        try {
            fileHandler = new FileHandler("server.log", true);
            fileHandler.setLevel(Level.ALL);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error setting up file handler: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            logger.info("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
                logger.info("Start new Thread for clientHandler");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error in the server: " + e.getMessage(), e);
        }

    }

    // add client and handler to the clients map
    public static void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
        logger.info("Client added: " + username);
    }

    // send message to relevant client
    public static void sendMessage(String type, String sender, String receiver, String message) {

        // check if sender send the broadcast message or not
        if (receiver.equals("BROADCAST")) {
            for (ClientHandler clientHandler : clients.values()) {
                // do not send the message again to sender
                if (clients.get(sender) != clientHandler) {
                    clientHandler.sendMessage(type + sender + ": " + message);
                    System.out.println(sender);
                    System.out.println(clientHandler);
                    System.out.println(clients.get(sender));
                }
            }
        } else {
            // get handler object from clients map
            clientHandler = clients.get(receiver);

            if (clientHandler != null) {
                // pass message to receiving client
                clientHandler.sendMessage(type + sender + ": " + message);
                logger.info("Message sent to " + receiver + " from " + sender + ": " + message);
            } else {
                logger.warning("User '" + receiver + "' not found !");

                // send response to sender
                ClientHandler clientSender = clients.get(sender);
                clientSender.sendMessage("0" + "user not found !");
                logger.warning("Response sent to " + sender + ": User not found !");
            }
        }

    }

    public static void sendClients(String req) {
        for (ClientHandler clientHandler : clients.values()) {
            clientHandler.sendMessage(req + clients.keySet());
        }
    }
}
