package WebSocketClient.handlers;

import WebSocketClient.client.WebSocketClient;
import WebSocketClient.dao.Command;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

@Component
public class CustomBinaryWebSocketHandler extends BinaryWebSocketHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private byte[] currentFile;
    private WebSocketSession session;
    private WebSocketClient webSocketClient;
    private boolean waitForFile = true;

    public CustomBinaryWebSocketHandler(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info(session.getRemoteAddress() + " connected");
        session.setBinaryMessageSizeLimit(1024 * 1024);
        this.session = session;
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        currentFile = message.getPayload().array();
        waitForFile = false;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info(session.getRemoteAddress() + " disconnected");
    }

    public void sendFiles() {
        File syncFolder = new File("sync");
        Collection<File> files = FileUtils.listFiles(syncFolder, null, false);
        for (File file : files) {
            log.info("sending " + file.getName());
            webSocketClient.send(file.getName());
            if(waitForAcknowledge()) {
                try {
                    webSocketClient.setCurrentState(Command.SAVE);
                    byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
                    BinaryMessage message = new BinaryMessage(bytes);
                    session.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            waitForAcknowledge();
        }
        webSocketClient.updateAvailableFiles();
    }

    public void getAvailableFiles() {
        List<String> files = webSocketClient.getAvailableFiles();
        if (files == null) {
            webSocketClient.updateAvailableFiles();
        }
        File dir = new File("incoming");
        if (!dir.exists()) {
            dir.mkdir();
        }
        for (String filename : files) {
            log.info("getting " + filename);
            waitForFile = true;
            webSocketClient.get(filename);
            waitForFile();
            File file = new File("incoming/" + filename);
            try {
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                out.write(currentFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean waitForAcknowledge() {
        int counter = 0;
        while (!webSocketClient.getCurrentState().equals(Command.ACKNOWLEDGE)) {
            if (counter++ > 10) {
                return false;
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void waitForFile() {
        int counter = 0;
        while (waitForFile) {
            if (counter++ > 20) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sleepThread(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
