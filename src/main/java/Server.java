import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final int PORT = 9002;
    public static Map<String, ClientHandler> clients = new HashMap<>();
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    static {
        // Configure logger
        logger.setLevel(Level.ALL);
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
    }

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // add client and handler to the clients map
    public static void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
    }

    // send message to relevant client
    public static void sendMessage(String type, String sender, String receiver, String message) {
        // get handler object from clients map
        ClientHandler clientHandler = clients.get(receiver);

        if (clientHandler != null) {
            // pass message to receiving client
            clientHandler.sendMessage(type + sender + ": " + message);
        } else {
            System.out.println("User '" + receiver + "' not found.");

            // send response to sender
            ClientHandler clientSender = clients.get(sender);
            clientSender.sendMessage("0" + "user not found !");
        }
    }
}
