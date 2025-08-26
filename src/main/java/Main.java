import dto.BookUpdate;
import enums.EnvVar;
import handles.QueueHandle;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        List<String> subscriptionPairs = Arrays.stream(EnvVar.KRAKEN_PAIR.get().split(",")).toList();

        ConcurrentHashMap<String, QueueHandle> queueHandlerMap = initiateQueueMap(subscriptionPairs);

        KrakenWebSocketConnector ws = new KrakenWebSocketConnector(queueHandlerMap);
        ws.connect();

        DataConsumer consumer = new DataConsumer(queueHandlerMap);
        consumer.start();
    }

    private static ConcurrentHashMap<String, QueueHandle> initiateQueueMap(List<String> subscriptionPairs) {
        ConcurrentHashMap<String, QueueHandle> queueHandlerMap = new ConcurrentHashMap<>();
        for (String subscriptionPair : subscriptionPairs) {
            LinkedBlockingQueue<BookUpdate> queue = new LinkedBlockingQueue<>();
            QueueHandle handler = new QueueHandle(subscriptionPair, queue);
            queueHandlerMap.put(subscriptionPair, handler);
        }
        return queueHandlerMap;
    }
}