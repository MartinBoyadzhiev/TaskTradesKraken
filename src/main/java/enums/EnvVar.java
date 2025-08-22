package enums;

public enum EnvVar {
    DEPTH_LIMIT("DEPTH_LIMIT"),
    KRAKEN_PAIR("KRAKEN_PAIR"),
    KRAKEN_WS_URL("KRAKEN_WS_URL"),
    VWAP_AMOUNT("VWAP_AMOUNT");

    private final String key;

    EnvVar(String key) {
        this.key = key;
    }

    public String get() {
        return System.getenv(key);
    }

    public int getInt() {
        return Integer.parseInt(System.getenv(key));
    }
}
