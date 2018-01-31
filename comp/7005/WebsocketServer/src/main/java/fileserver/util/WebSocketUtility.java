package fileserver.util;

import com.google.gson.Gson;
import fileserver.dao.Command;
import fileserver.dao.SimpleMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Utility helper class for logic that is shared across the different websocket listeners.
 *
 * This class was designed mostly to make the websocket listener code more readable and
 * less cluttered.
 *
 * @author dklein
 */
public class WebSocketUtility {

    private static Gson gson = new Gson();

    /**
     * Closes the websocket connection. This is quite common logic and the try catch block
     * makes a lot of spaghetti code, so it has been moved here to make the rest of the server
     * code more readable.
     *
     * @param session the session being closed.
     */
    public static void closeConnection(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A simple message is the DAO for commands and payloads. This method wraps a message and payload
     * in the DAO and sends it. This is very common logic and was moved here to prevent unnecessary
     * spaghetti code elsewhere.
     *
     * @param session the session where the message is being sent
     * @param command the command being sent in the message
     * @param payload the payload of the message
     */
    public static void sendSimpleMessage(WebSocketSession session, Command command, String payload) {
        SimpleMessage simpleMessage = new SimpleMessage();
        simpleMessage.setCommand(command);
        simpleMessage.setMessage(payload);
        try {
            session.sendMessage(new TextMessage(gson.toJson(simpleMessage)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
