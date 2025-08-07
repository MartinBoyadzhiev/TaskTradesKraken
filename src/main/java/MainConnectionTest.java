import com.google.gson.Gson;
import dto.KrakenSubscription;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class MainConnectionTest {

    private static final String BASE_WS_URL = "wss://ws.kraken.com/";
    private static final int DEPTH_LIMIT = 100;
    private static final Gson gson = new Gson();
    public static final Logger logger = Logger.getLogger("MainConnectionTest");

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        OrderBookManager bookManager = new OrderBookManager(DEPTH_LIMIT);
//        Hardcode test
        String pair1 = "ETH/USD";
        String pair2 = "SOL/USD";
        String pair3 = "XRP/USD";

        KrakenSubscription krakenSubscription = new KrakenSubscription(List.of(pair2, pair1, pair3), DEPTH_LIMIT);
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
                System.out.println(pair + " mid price is " + book.getMidPrice());
                System.out.println(pair + " asks VWAP price is " + book.calculateVWAPAsks(10));
                System.out.println(pair + " bids VWAP price is " + book.calculateVWAPAsks(10));
            }
        }
    }
}
