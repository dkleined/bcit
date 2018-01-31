package fileserver.websocket.client;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import fileserver.dao.Command;
import fileserver.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static fileserver.util.WebSocketUtility.sendSimpleMessage;

/**
 * Websocket client class for the outgoing file connection with the client application. When the client sends a
 * file related command, an outgoing connection is established to handle file transfers. All binary related
 * communication is handled across this connection.
 *
 * For receiving a file, the filename is sent from the main websocket listener with the SEND command. If there
 * is no filename set, any data received on this connection will be ignored. After the file is saved, the filename
 * is set to null to prevent multiple files being saved with the same filename. It is the responsibility of the client
 * to respond to acknowledgement messages properly so that filenames and data are mapped properly.
 *
 * @author dklein
 */
@Component
public class BinaryWebSocketClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private WebSocket websocket;
    private FileService fileService;
    private String filename;

    public BinaryWebSocketClient(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Called after the client sends a GET/SEND command. If it is a SEND command, then the filename is
     * set by the main websocket listener. After a file is saved, the filename is reset to null. After
     * the file is saved, an acknowledgement is sent to the client.
     *
     * @param hostname hostname of the client (used for establishing connection)
     * @param commandSession the main websocket session used for client commands
     */
    public void openConnection(String hostname, WebSocketSession commandSession) {
        try {
            websocket = new WebSocketFactory()
                    .createSocket("ws://" + hostname + ":7006/file")
                    .addListener(new WebSocketAdapter() {
                        @Override
                        public void onBinaryMessage(WebSocket websocket, byte[] binary) {
                            if (filename != null && fileService.saveFile(hostname, filename, binary)) {
                                sendSimpleMessage(commandSession, Command.ACKNOWLEDGE, filename);
                                filename = null;
                            }
                        }
                    })
                    .connect();
        } catch (WebSocketException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convenience method for closing the connection from outside the class.
     */
    public void closeConnection() {
        websocket.sendClose();
    }

    /**
     * Convenience method for sending binary data across the session connection from outside the class. This
     * is used for the GET command, which is handled in the main websocket listener.
     *
     * @param bytes the binary data being sent
     */
    public void sendByteArray(byte[] bytes) {
        websocket.sendBinary(bytes);
    }

    /**
     * Convenience method to check if the file connection is open.
     * @return true if open, else false
     */
    public boolean isConnected() {
        return websocket.isOpen();
    }

    /**
     * Convenience method for setting the filename. The filename is included in the SEND command, and will allow
     * a file to be received over this connection.
     * @param fileName the filename of the data that is expected
     */
    public void setFileName(String fileName) {
        this.filename = fileName;
    }

}
