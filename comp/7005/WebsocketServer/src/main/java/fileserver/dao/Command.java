package fileserver.dao;

/**
 * All the possible commands for the server.
 *
 * SEND
 * ---
 * Used to upload a file to the server - the file is uploaded on the outgoing socket.
 * Payload: the filename to be uploaded
 * Return: acknowledgement of the filename
 *
 * GET
 * ----
 * Used to download a server from the file
 * Payload: filename being requested - the file is returned on the outgoing socket.
 *
 * Acknowledge
 * ---
 * Used by the server to acknowledge requests.
 * Payload: one of several other commands
 * Return: acknowledgement that the command has been processed.
 *
 * List
 * ---
 * Used to list all files visible to user.
 * Payload: NULL
 * Return: list of files available to user.
 *
 * @author dklein
 */
public enum Command {
    SEND("send"),
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
