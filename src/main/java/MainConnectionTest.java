import com.google.gson.JsonElement;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;

public class MainConnectionTest {

    public static void main(String[] args) throws URISyntaxException {
//        Initiate connection and wire producer/consumer with buffer
//        TODO multiple stream queues

        LinkedBlockingQueue<JsonElement> buffer = new LinkedBlockingQueue<>();

        WebSocketConnector ws = new WebSocketConnector(buffer);
        ws.connect();

        DataConsumer consumer = new DataConsumer(buffer);
        consumer.start();
    }
}
