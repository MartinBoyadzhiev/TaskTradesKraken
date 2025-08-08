import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.OrderBookUpdate;
import dto.OrderLevel;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketConnector extends WebSocketClient {

    private final KrakenMessageHandler handler;
    private final String subscriptionMessage;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnector.class);

    public WebSocketConnector(String serverUrl, String message, KrakenMessageHandler handler) throws URISyntaxException {
        super(new URI(serverUrl));
        this.handler = handler;
        this.subscriptionMessage = message;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("Connection successful");
        send(subscriptionMessage);
        startPingScheduler();
    }

    @Override
    public void onMessage(String message) {
        JsonElement root = JsonParser.parseString(message);

        if (root.isJsonObject()) {
            JsonObject object = root.getAsJsonObject();

            if (object.has("event") && "heartbeat".equals(object.get("event").getAsString())) {
                return;
            }

            if (object.has("event") && "systemStatus".equals(object.get("event").getAsString())) {
                return;
            }

            if (object.has("event") && "subscriptionStatus".equals(object.get("event").getAsString())) {
                String status = object.get("status").getAsString();
                if ("error".equals(status)) {
                    String errorMsg = object.get("errorMessage").getAsString();
                    handler.onError(errorMsg);
                } else {
                    logger.info("Subscribed successfully: {}", object);
                }
                return;
            }
        }

        JsonArray arr = root.getAsJsonArray();
        JsonObject data = arr.get(1).getAsJsonObject();
        String pair = arr.get(3).getAsString();

        if (data.has("as") && data.has("bs")) {

            JsonArray asArr =  data.getAsJsonArray("as");
            JsonArray bsArr = data.getAsJsonArray("bs");
            handler.onSnapshot(pair, parseLevel(asArr), parseLevel(bsArr));
        } else {
            List<OrderLevel> asks = data.has("a") ? parseLevel(data.getAsJsonArray("a"))
                    : Collections.emptyList();
            List<OrderLevel> bids = data.has("b") ? parseLevel(data.getAsJsonArray("b"))
                    : Collections.emptyList();

            handler.onUpdate(pair, new OrderBookUpdate(asks, bids));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("WebSocket connection closed: {} reason: {} remote: {}", code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        logger.error("WebSocket error.", ex);
    }

    private List<OrderLevel> parseLevel(JsonArray array) {
        List<OrderLevel> levels = new ArrayList<>();

        for (JsonElement e : array) {
            JsonArray level = e.getAsJsonArray();
            double price = Double.parseDouble(level.get(0).getAsString());
            double volume = Double.parseDouble(level.get(1).getAsString());
            levels.add(new OrderLevel(price, volume));
        }
        return levels;
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
