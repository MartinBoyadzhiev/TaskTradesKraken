import com.google.gson.*;
import common.dto.BookUpdate;
import common.dto.OrderLevel;
import dto_kraken.*;
import dto_kraken.Message;
import dto_kraken.Params;
import config.Constant;
import common.QueueHandle;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class KrakenWebSocketConnector extends WebSocketClient {

    private final HashMap<String, Map<String, QueueHandle>> queueHandlerMapPerExchange;
    private final String subscriptionMessage;
    private final Logger logger = LoggerFactory.getLogger(KrakenWebSocketConnector.class);
    private final Gson gson = new Gson();

    public KrakenWebSocketConnector(HashMap<String, Map<String, QueueHandle>> queueHandlerMapPerExchange) throws URISyntaxException {
        super(new URI(Constant.KRAKEN_WS_URL));
        this.queueHandlerMapPerExchange = queueHandlerMapPerExchange;
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
        JsonObject data = JsonParser.parseString(message).getAsJsonObject();
        if (!data.has("channel") || !data.get("channel").getAsString().equals("book")) {
            handleSystemData(data);
        } else {
            String pairName = getPairName(data);
            KrakenUpdateMessage updateMessage = gson.fromJson(message, KrakenUpdateMessage.class);
            UpdateData updateData = updateMessage.getData().getFirst();

            BookUpdate update = new KrakenBookUpdate(updateMessage.getType());
            parseLevels(updateData.getAsks(), update.getAsks());
            parseLevels(updateData.getBids(), update.getBids());

            Map<String, QueueHandle> queueHandleMap = queueHandlerMapPerExchange.get("kraken");
            queueHandleMap.computeIfPresent(pairName, (k, handle) -> {
                handle.getStreamQueue().offer(update);
                return handle;
            });
        }
    }

    private String getPairName(JsonObject data) {
        JsonElement arrElement = data.get("data").getAsJsonArray().get(0);
        return arrElement.getAsJsonObject().get("symbol").getAsString();
    }

    private void handleSystemData(JsonObject object) {
        if (object.has("channel") && object.get("channel").getAsString().equals("heartbeat")) {
            logger.trace("Heartbeat: {}", object);
        } else if (object.has("channel") && object.get("channel").getAsString().equals("status")) {
            logger.debug("Status: {}", object);
        } else if (object.has("method") && object.get("method").getAsString().equals("pong")) {
            logger.debug("Ping response: {}", object);
        } else {
            logger.info("Subscribed successfully: {}", object);
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
        List<String> subscriptionPairs = Arrays.stream(Constant.KRAKEN_PAIR.split(",")).toList();
        Params params = new Params("book", subscriptionPairs, Constant.DEPTH_LIMIT, true);
        Message message = new Message("subscribe", params);
        return gson.toJson(message);
    }

    private void startPingScheduler() {
        try (ScheduledExecutorService pingScheduler = Executors.newSingleThreadScheduledExecutor()) {
            pingScheduler.scheduleAtFixedRate(() -> {
                try {
                    sendPing();
                } catch (Exception e) {
                    logger.error("WebSocket connection failed.", e);
                }
            }, 30, 60, TimeUnit.SECONDS);
        }
    }

    private void parseLevels(List<OrderLevel> data, List<OrderLevel> levels) {
        for (OrderLevel level : data) {
            levels.add(new OrderLevel(level.price(), level.qty()));
        }
    }
}