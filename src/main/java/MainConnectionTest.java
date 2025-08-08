import com.google.gson.Gson;
import dto.KrakenSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MainConnectionTest {

    private static final String BASE_WS_URL = System.getenv("KRAKEN_WS_URL");
    private static final int DEPTH_LIMIT = Integer.parseInt(System.getenv("DEPTH_LIMIT"));
    private static final int VWAP_AMOUNT = Integer.parseInt(System.getenv("VWAP_AMOUNT"));
    private static final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(MainConnectionTest.class);

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        OrderBookManager bookManager = new OrderBookManager(DEPTH_LIMIT);

        List<String> subscriptionPairs = Arrays.stream(System.getenv("KRAKEN_PAIR").split(",")).toList();

        KrakenSubscription krakenSubscription = new KrakenSubscription(subscriptionPairs, DEPTH_LIMIT);
        String sub = gson.toJson(krakenSubscription);

        WebSocketConnector ws = new WebSocketConnector(BASE_WS_URL, sub, bookManager);
        ws.connect();

        while (true) {
            Thread.sleep(2000);

            if (ws.isClosed()) {
                return;
            }

            Set<String> pairs = bookManager.getAllPairs();

            for (String pair : pairs) {
                LocalOrderBook book = bookManager.getBook(pair);
                if (book.getAsks().isEmpty() || book.getBids().isEmpty()) {
                    continue;
                }

                logger.info("{} mid price is {}", pair, book.getMidPrice());
                logger.info("{} asks VWAP price is {}", pair, book.calculateVWAPAsks(VWAP_AMOUNT));
                logger.info("{} bids VWAP price is {}", pair, book.calculateVWAPBids(VWAP_AMOUNT));
            }
        }
    }
}
