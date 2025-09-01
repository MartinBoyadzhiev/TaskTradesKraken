package config;

public class Constant {
    public static final int DEPTH_LIMIT = Integer.parseInt(System.getenv("DEPTH_LIMIT"));
    public static final String KRAKEN_PAIR = System.getenv("KRAKEN_PAIR");
    public static final String KRAKEN_WS_URL = System.getenv("KRAKEN_WS_URL");
}
