import java.net.Socket;

public class HtmlServerHandler extends Thread {

    private Socket socket;

    private String directory;

    HtmlServerHandler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
    }
}
