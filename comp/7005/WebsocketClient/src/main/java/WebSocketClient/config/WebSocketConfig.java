package WebSocketClient.config;

import WebSocketClient.client.WebSocketClient;
import WebSocketClient.handlers.CustomBinaryWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 *
 * @author dklein
 */
@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${connect.path}")
    private String connectPath;
    @Autowired
    WebSocketClient webSocketClient;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customBinaryWebSocketHandler(), connectPath);
    }

    @Bean
    public WebSocketHandler customBinaryWebSocketHandler() {
        return new CustomBinaryWebSocketHandler(webSocketClient);
    }




}
