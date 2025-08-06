import com.google.gson.Gson;
import dto.KrakenSubscription;

import java.net.URISyntaxException;
import java.util.List;

public class MainConnectionTest {

    private static final String BASE_WS_URL = "wss://ws.kraken.com/";
    private static final Gson gson = new Gson();
    private static final int DEPTH_LIMIT = 100;

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

            System.out.println(orderBook.getAsks().firstKey());
            System.out.println(orderBook.getBids().firstKey());
            System.out.println(orderBook.getMidPrice());
            System.out.println();

//            System.out.println(orderBook.getMidPrice());
//            System.out.println();
//            System.out.println("Best ask is " + orderBook.getAsks().firstKey());
//            System.out.println("Best bid is " + orderBook.getBids().firstKey());
//            System.out.println();

        }
//            System.out.println();
//            System.out.println("Best ask is " + orderBook.getAsks().firstKey());
//            System.out.println("Best bid is " + orderBook.getBids().firstKey());
//            System.out.println();
//            Thread.sleep(2000);
//        }

//        System.out.println("Book ready: " + orderBook.getAsks().size());
//        System.out.println("Mid price at " + LocalDateTime.now() + " is " + orderBook.getMidPrice());
//        System.out.println("Best bid at " + LocalDateTime.now() + " is " + orderBook.getBids().firstKey());
//        Thread.sleep(8000);
//        System.out.println("Best bid at " + LocalDateTime.now() + " is " + orderBook.getBids().firstKey());
//        System.out.println("Mid price at " + LocalDateTime.now() + " is " + orderBook.getMidPrice());

    }
}
