import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
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
                }
            }
            System.out.println("User '" + username + "' connected.");

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
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // send message by server to receiving client
    public void sendMessage(String message) {
        writer.println(message);
    }

}
