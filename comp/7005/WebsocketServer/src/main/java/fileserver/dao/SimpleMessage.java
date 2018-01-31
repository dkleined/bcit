package fileserver.dao;

/**
 * A simple message is the DAO for server command responses.
 *
 * For a list of commands @see fileserver.dao.Command
 *
 * The message is data associated with the command. i.e. the list command will include
 * a list of filenames.
 *
 * @author dklein
 */
public class SimpleMessage {
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

