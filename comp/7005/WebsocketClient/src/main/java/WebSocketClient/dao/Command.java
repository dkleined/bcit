package WebSocketClient.dao;

/**
 * Created by danielklein on 2017-09-22.
 */
public enum Command {
    SEND("send"),
    SAVE("save"),
    GET("get"),
    ACKNOWLEDGE("ack"),
    LIST("list");

    private String text;

    Command(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

