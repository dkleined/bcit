package WebSocketClient.dao;

/**
 * Created by danielklein on 2017-09-20.
 */
public class Message {
    private Command command;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}
