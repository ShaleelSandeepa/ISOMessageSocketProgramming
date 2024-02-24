import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private FileHandler fileHandler;

    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.writer = new PrintWriter(clientSocket.getOutputStream(), true);


        // Configure logger with file handler
        this.fileHandler = Server.fileHandler;
        fileHandler.setLevel(Level.ALL);
        logger.addHandler(fileHandler);
    }

    @Override
    public void run() {
        try {
            String username = reader.readLine();

            // check name if exist or null
            if (username == null) {
                return;
            } else if (!Server.clients.containsKey(username)){

                // add valid username with handler object to clients map
                Server.addClient(username, this);
                if (Server.clients.containsKey(username)) {
                    writer.println("ACCEPTED");
                    logger.info("User '" + username + "' connected.");

                }
            }

            if (reader.readLine().equals("REQUEST")) {
                Server.sendClients("USER");
            }

            while (true) {

                String message = reader.readLine();
                if (message == null) {
                    break;
                }

                String type = message.substring(0, 1);
                int separatorIndex = message.indexOf(":");
                String receiver = message.substring(1, separatorIndex);
                String content = message.substring(separatorIndex + 2);

                // send message from server to receiver
                Server.sendMessage(type, username, receiver, content);
                logger.info("Message from " + username + " to " + receiver + ": " + content);

            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error in client handling: " + e.getMessage(), e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing client socket: " + e.getMessage(), e);
            }
        }
    }

    // send message by server to receiving client
    public void sendMessage(String message) {
        writer.println(message);
        logger.info("Send clients list to all clients");
    }

}
