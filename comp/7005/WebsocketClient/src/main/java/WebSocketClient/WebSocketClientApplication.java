package WebSocketClient;

import WebSocketClient.client.WebSocketClient;
import WebSocketClient.handlers.CustomBinaryWebSocketHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by danielklein on 2017-09-29.
 */
@SpringBootApplication
@ComponentScan
@EnableAutoConfiguration
public class WebSocketClientApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WebSocketClientApplication.class, args);
        context.getBean(WebSocketClient.class).openConnection();
        context.getBean(CustomBinaryWebSocketHandler.class).sendFiles();
        context.getBean(CustomBinaryWebSocketHandler.class).getAvailableFiles();
    }
}
