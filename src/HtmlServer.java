import java.io.IOException;
import java.net.ServerSocket;

public class HtmlServer {

    private int port;
    private String directory;

    public HtmlServer(int port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    void start() {
        try (var server = new ServerSocket(this.port)) {
            while (true) {
                var socket = server.accept();
                var thread = new HtmlServerHandler(socket, this.directory);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConfigLoader.loadConfigENV("src\\resources\\config.properties");
        new HtmlServer(Integer.parseInt(System.getenv().get("port")), System.getenv().get("directory")).start();

    }
}



