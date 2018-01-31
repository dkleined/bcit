package WebSocketClient.client;

import WebSocketClient.dao.Command;
import WebSocketClient.dao.Message;
import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static WebSocketClient.dao.Command.*;

/**
 * Created by danielklein on 2017-09-29.
 */
@Service
public class WebSocketClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private WebSocket webSocket;
    private String hostname;
    private int port;
    private String path;
    private Gson gson = new Gson();
    private Command currentState;
    private List<String> availableFiles;

    public WebSocketClient(@Value("${websocket.server.hostname}") String hostname,
                           @Value("${websocket.server.port}") int port,
                           @Value("${websocket.server.connect.path}") String path) throws InterruptedException {
        this.hostname = hostname;
        this.port = port;
        this.path = path;
        currentState = LIST;
    }

    public void openConnection() {
        try {
            webSocket = new WebSocketFactory()
                    .createSocket("ws://" + hostname + ":" + port + path)
                    .addListener(new WebSocketAdapter() {
                       @Override
                       public void onTextMessage(WebSocket websocket, String text) {
                           log.info(text);
                            Message message = gson.fromJson(text, Message.class);
                            currentState = message.getCommand();
                            switch(message.getCommand()) {
                                case ACKNOWLEDGE:
                                    currentState = ACKNOWLEDGE;
                                    break;
                                case LIST:
                                    availableFiles = Arrays.asList(gson.fromJson(message.getMessage(), String[].class));
                                    break;
                            }
                       }
                    })
                    .connect();
        } catch (WebSocketException | IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String filename) {
        Message message = new Message();
        message.setCommand(SEND);
        message.setMessage(filename);
        webSocket.sendText(gson.toJson(message));
        currentState = SEND;
    }

    public Command getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Command currentState) {
        this.currentState = currentState;
    }

    public List<String> getAvailableFiles() {
        return availableFiles;
    }

    public void get(String file) {
        Message message = new Message();
        message.setCommand(GET);
        message.setMessage(file);
        webSocket.sendText(gson.toJson(message));
    }

    public void updateAvailableFiles() {
        Message message = new Message();
        message.setCommand(LIST);
        webSocket.sendText(gson.toJson(message));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
