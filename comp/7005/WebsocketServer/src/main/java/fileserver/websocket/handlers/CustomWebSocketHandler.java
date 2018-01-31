package fileserver.websocket.handlers;

import com.google.gson.Gson;
import fileserver.dao.SimpleMessage;
import fileserver.service.FileService;
import fileserver.util.LogUtil;
import fileserver.websocket.client.BinaryWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

import static fileserver.dao.Command.ACKNOWLEDGE;
import static fileserver.dao.Command.LIST;
import static fileserver.util.WebSocketUtility.closeConnection;
import static fileserver.util.WebSocketUtility.sendSimpleMessage;
/**
 * Main websocket listener for the server. This client connects to this listener and
 * sends commends. For a full list of commands, @see fileserver.dao.Command
 *
 * This listener is a text based socket handler and cannot accept files. Files are
 * transferred on an outgoing connection from server to client. This connection is
 * established by the client sending a connect request to the server with an address.
 * The outgoing connection is for file transfers only, and all commands are handled
 * in this listener.
 *
 * If a command is sent that is not recognized by the server, the connection is closed.
 *
 * @author ダニエル　クライン
 */
@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private Gson gson = new Gson();
    private FileService fileService;
    private BinaryWebSocketClient client;

    public CustomWebSocketHandler(FileService fileService) {
        this.fileService = fileService;
        this.client = new BinaryWebSocketClient(fileService);
    }

    /**
     * After the connection is established, this method is called. On successful connection, the
     * server returns a list of files that are visible to the user. If no files are visible to the
     * user, then an empty list is returned.
     *
     * @param session websocket connection that just opened
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = session.getLocalAddress().getHostName();
        log.info(LogUtil.logStr(username, "connected"));
        client.openConnection(username, session);
        List<String> paths = fileService.getAllFileNames(username);
        sendSimpleMessage(session, LIST, paths.toString());
    }

    /**
     * Listener for commands. A user can execute the following commands:
     * -SEND
     * -LIST
     * -GET
     *
     * Any command that is not recognized by the server will result in the server closing
     * the connection.
     *
     * For explanation of commands, @see fileserver.dao.Command
     *
     * @param session
     * @param message
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        SimpleMessage json = gson.fromJson(message.getPayload(), SimpleMessage.class);
        String username = session.getRemoteAddress().getHostName();
        log.info(LogUtil.logStr(json.getCommand().toString(), json.getMessage()));
        switch(json.getCommand()) {
            case SEND:
                if(!client.isConnected()) {
                    client.openConnection(username, session);
                }
                client.setFileName(json.getMessage());
                sendSimpleMessage(session, ACKNOWLEDGE, json.getMessage());
                break;
            case LIST:
                List<String> paths = fileService.getAllFileNames(username);
                sendSimpleMessage(session, LIST, paths.toString());
                break;
            case GET:
                if(!client.isConnected()) {
                    client.openConnection(username, session);
                }
                client.sendByteArray(fileService.getFile(username, json.getMessage()));
                break;
            default:
                closeConnection(session);
        }


    }

    /**
     * Called after the websocket connection is closed. The user data is cleaned up from the
     * user file service.
     *
     * @param session websocket status that was closed
     * @param status websocket close status (used by java, we don't)
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        client.closeConnection();
    }

}
