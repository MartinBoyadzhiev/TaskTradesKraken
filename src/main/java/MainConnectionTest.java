import com.google.gson.Gson;
import dto.KrakenSubscription;

import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

public class MainConnectionTest {

    private static final String BASE_WS_URL = "wss://ws.kraken.com/";
    private static final int DEPTH_LIMIT = 100;
    private static final Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger("MainConnectionTest");

    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        LocalOrderBook orderBook = new LocalOrderBook(DEPTH_LIMIT);

        KrakenSubscription krakenSubscription = new KrakenSubscription(List.of("XRP/USD"), DEPTH_LIMIT);
        String sub = gson.toJson(krakenSubscription);
        WebSocketConnector ws = new WebSocketConnector(BASE_WS_URL, sub, orderBook);
        ws.connect();

        while (true) {
            Thread.sleep(2000);

            if (orderBook.getAsks().isEmpty() || orderBook.getBids().isEmpty()) {
                continue;
            }

            System.out.println("Best ASK is - " + orderBook.getAsks().firstKey());
            System.out.println("Best BID is - " + orderBook.getBids().firstKey());
            System.out.println("MID price is - " + orderBook.getMidPrice());
            System.out.println();
        }
    }
}
