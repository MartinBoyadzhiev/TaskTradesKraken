import com.google.gson.Gson;
import dto.KrakenSubscription;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class MainConnectionTest {

    private static final String BASE_WS_URL = "wss://ws.kraken.com/";
    private static final int DEPTH_LIMIT = 100;
    private static final Gson gson = new Gson();
    public static final Logger logger = Logger.getLogger("MainConnectionTest");
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws URISyntaxException, InterruptedException {

        LocalOrderBook orderBook = new LocalOrderBook(DEPTH_LIMIT);

        String pair = sc.nextLine();

        KrakenSubscription krakenSubscription = new KrakenSubscription(List.of(pair), DEPTH_LIMIT);
        String sub = gson.toJson(krakenSubscription);
        WebSocketConnector ws = new WebSocketConnector(BASE_WS_URL, sub, orderBook);
        ws.connect();

        while (true) {
            Thread.sleep(2000);

//            temp error handling on inaccurate user input
            if (ws.isClosed()) {
                return;
            }

            if (orderBook.getAsks().isEmpty() || orderBook.getBids().isEmpty()) {
                continue;
            }

            logger.info("Mid price is - " + orderBook.getMidPrice());

            try {
                System.out.println("VWAP for buying 10 BTC is " + orderBook.calculateVWAPBids(10));
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
            }

            try {
                System.out.println("VWAP for selling 10 BTC is " + orderBook.calculateVWAPAsks(10));
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }
}
