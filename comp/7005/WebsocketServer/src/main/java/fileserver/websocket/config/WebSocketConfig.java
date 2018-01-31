package fileserver.websocket.config;

import fileserver.service.FileService;
import fileserver.websocket.handlers.CustomWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuration for websockets. Contains all the beans and configuration for the server.
 *
 * There are two websocket listeners established:
 *
 * 1) Incoming - used for the client to initially connect to and send commands.
 * This connection is used to send/receive commands to the server.
 *
 * 2) Outgoing - used to transfer files to and from the client. When the client attempts
 * a file command, the outgoing connection will be established.
 *
 * Documentation on beans can be found in the bean classes.
 *
 * @author dklein
 */
@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${connect.path}")
    private String connectPath;
    @Value("${file.service.base.path}")
    private String basePath;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customBinaryWebSocketHandler(), connectPath);
    }

    @Bean
    public WebSocketHandler customBinaryWebSocketHandler() {
        return new CustomWebSocketHandler(fileService());
    }

    @Bean
    public FileService fileService() {
        return new FileService(basePath);
    }

}
