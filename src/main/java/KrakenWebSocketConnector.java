import com.google.gson.*;
import dto.KrakenSubscription;
import enums.EnvVar;
import handles.QueueHandle;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class KrakenWebSocketConnector extends WebSocketClient {

    private final ConcurrentHashMap<String, QueueHandle> queueHandlerMap;
    private final String subscriptionMessage;
    private final Logger logger = LoggerFactory.getLogger(KrakenWebSocketConnector.class);
    private final Gson gson = new Gson();

    public KrakenWebSocketConnector(ConcurrentHashMap<String, QueueHandle> queueHandlerMap) throws URISyntaxException {
        super(new URI(EnvVar.KRAKEN_WS_URL.get()));
        this.queueHandlerMap = queueHandlerMap;
        this.subscriptionMessage = formatWebSocketURL();
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("Connection successful");
        send(subscriptionMessage);
        startPingScheduler();
    }

    @Override
    public void onMessage(String message) {
//       TODO implement parsing with dto
        JsonElement data = JsonParser.parseString(message);
        if (data.isJsonObject()) {
            handleSystemData(data.getAsJsonObject());
        } else {
            String pairName = data.getAsJsonArray().get(3).getAsString();
            queueHandlerMap.computeIfPresent(pairName, (k, handle) ->  {
                handle.getStreamQueue().offer(data);
                return handle;
            });
        }
    }

    private void handleSystemData(JsonObject object) {
        if (object.has("event") && "heartbeat".equals(object.get("event").getAsString())) {
            logger.trace("Heartbeat received from Kraken WS");
        }

        if (object.has("event") && "systemStatus".equals(object.get("event").getAsString())) {
            logger.debug("Kraken WS systemStatus: status={}, version={}, connectionID={}",
                    object.get("status").getAsString(),
                    object.get("version").getAsString(),
                    object.get("connectionID").getAsLong()
            );
        }

        if (object.has("event") && "subscriptionStatus".equals(object.get("event").getAsString())) {
            String status = object.get("status").getAsString();
            if ("error".equals(status)) {
                String errorMsg = object.get("errorMessage").getAsString();
                logger.error("Error in WebSocket connection: {}", errorMsg);
            } else {
                logger.info("Subscribed successfully: {}", object);
            }
        }
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

    private String formatWebSocketURL() {
        List<String> subscriptionPairs = Arrays.stream(EnvVar.KRAKEN_PAIR.get().split(",")).toList();
        KrakenSubscription krakenSubscription = new KrakenSubscription(subscriptionPairs, EnvVar.DEPTH_LIMIT.getInt());
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
