import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.OrderBookUpdate;
import dto.OrderLevel;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WebSocketConnector extends WebSocketClient {

    private final String subscriptionMessage;
    private final LocalOrderBook orderBook;

    public WebSocketConnector(String serverUrl, String message, LocalOrderBook book) throws URISyntaxException {
        super(new URI(serverUrl));
        this.subscriptionMessage = message;
        this.orderBook = book;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected");
        send(subscriptionMessage);
    }

    @Override
    public void onMessage(String message) {
        JsonElement root = JsonParser.parseString(message);

        if (root.isJsonObject()) {
            JsonObject object = root.getAsJsonObject();
            if (object.has("event")) {
                String event = object.get("event").getAsString();

                if ("subscriptionStatus".equals(event)) {
                    String status = object.get("status").getAsString();
                    if ("error".equals(status)) {
                        String errorMessage = object.get("errorMessage").getAsString();
                        MainConnectionTest.logger.warning(errorMessage);
                        close(1000, "Invalid subscription request");
                        return;
                    }
                }
            }
        }

        if (root.isJsonArray()) {
            JsonArray array = root.getAsJsonArray();
            JsonObject data = array.get(1).getAsJsonObject();

            if (data.has("as") && data.has("bs")) {

                List<OrderLevel> asks = parseLevel(data.getAsJsonArray("as"));
                List<OrderLevel> bids = parseLevel(data.getAsJsonArray("bs"));
                orderBook.initialize(asks, bids);
            } else {
                List<OrderLevel> asks;
                List<OrderLevel> bids;

                if (data.has("a")) {
                    asks = parseLevel(data.getAsJsonArray("a"));
                } else {
                    asks = Collections.emptyList();
                }

                if (data.has("b")) {
                    bids = parseLevel(data.getAsJsonArray("b"));
                } else {
                    bids = Collections.emptyList();
                }
//                System.out.println(asks.size() + " - " + bids.size());
                orderBook.applyUpdate(new OrderBookUpdate(asks, bids));
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        MainConnectionTest.logger.warning("WebSocket closed: " + code + " reason: " + reason + " remote: " + remote);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
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
}
