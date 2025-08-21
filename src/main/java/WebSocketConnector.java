import com.google.gson.*;
import dto.KrakenSubscription;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketConnector extends WebSocketClient {
//    Subscribe to given pairs, send pings periodically, pass data to buffer

//    TODO multiple stream queues

    public static final int DEPTH_LIMIT = Integer.parseInt(System.getenv("DEPTH_LIMIT"));
    public static final String SUBSCRIPTION_PAIRS = System.getenv("KRAKEN_PAIR");
    private static final String KRAKEN_WS_URL = System.getenv("KRAKEN_WS_URL");
    private final LinkedBlockingQueue<JsonElement> buffer;
    private final String subscriptionMessage;
    private final Logger logger = LoggerFactory.getLogger(WebSocketConnector.class);
    private final Gson gson = new Gson();

    public WebSocketConnector(LinkedBlockingQueue<JsonElement> buffer) throws URISyntaxException {
        super(new URI(KRAKEN_WS_URL));
        this.buffer = buffer;
        this.subscriptionMessage = getSubscriptionMessage();
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("Connection successful");
        send(subscriptionMessage);
        startPingScheduler();
    }

    @Override
    public void onMessage(String message) {
        JsonElement data = JsonParser.parseString(message);
        this.buffer.offer(data);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
//        TODO Reconnection logic
        logger.info("WebSocket connection closed: {} reason: {} remote: {}", code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
//        TODO Reconnection logic
        logger.error("WebSocket error.", ex);
    }

    private String getSubscriptionMessage() {
        List<String> subscriptionPairs = Arrays.stream(SUBSCRIPTION_PAIRS.split(",")).toList();
        KrakenSubscription krakenSubscription = new KrakenSubscription(subscriptionPairs, DEPTH_LIMIT);
        return gson.toJson(krakenSubscription);
    }

    private void startPingScheduler() {
        ScheduledExecutorService pingScheduler = Executors.newSingleThreadScheduledExecutor();
        pingScheduler.scheduleAtFixedRate(() -> {
            try {
                sendPing();
            } catch (Exception e) {
                logger.error("WebSocket connection failed.", e);
            }
        }, 30, 60, TimeUnit.SECONDS);
    }
}
