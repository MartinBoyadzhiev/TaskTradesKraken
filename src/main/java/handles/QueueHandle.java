package handles;

import com.google.gson.JsonElement;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueHandle {

    private final String pairName;
    private final LinkedBlockingQueue<JsonElement> streamQueue;

    public QueueHandle(String pairName, LinkedBlockingQueue<JsonElement> streamQueue) {
        this.pairName = pairName;
        this.streamQueue = streamQueue;
    }

    public String getPairName() {
        return pairName;
    }

    public LinkedBlockingQueue<JsonElement> getStreamQueue() {
        return streamQueue;
    }
}