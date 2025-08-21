import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class DataConsumer extends Thread {
//    Initialize book managers, poll data, handle system data or pass update data to corresponding bookManager

//    TODO multiple stream queues
//    TODO Find a way to separate system data processing from this class and bring calculation invoking logic here

    private final LinkedBlockingQueue<JsonElement> buffer;
    private final HashMap<String, OrderBookManager> bookManagerMap = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(DataConsumer.class);

    public DataConsumer(LinkedBlockingQueue<JsonElement> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        initializeBookManagers();

        while (true) {
            try {
                JsonElement data = this.buffer.take();

                if (data.isJsonObject()) {
                    handleSystemData(data.getAsJsonObject());
                } else {
                    String pairName = data.getAsJsonArray().get(3).getAsString();
                    OrderBookManager bookManager = this.bookManagerMap.get(pairName);
                    bookManager.handleUpdateData(data);
                    bookManager.doCalculations();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

    private void initializeBookManagers() {
        List<String> subscriptionPairs = Arrays.stream(WebSocketConnector.SUBSCRIPTION_PAIRS.split(",")).toList();

        for (String pair : subscriptionPairs) {
            OrderBookManager manager = new OrderBookManager(pair, WebSocketConnector.DEPTH_LIMIT);
            this.bookManagerMap.put(pair, manager);
        }
    }
}
