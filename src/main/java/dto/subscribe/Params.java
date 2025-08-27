package dto.subscribe;

import java.util.List;

public class Params {
    private final String channel;
    private final List<String> symbol;
    private final int depth;
    private final boolean snapshot;

    public Params(String channel, List<String> symbol, int depth, boolean snapshot) {
        this.channel = channel;
        this.symbol = symbol;
        this.depth = depth;
        this.snapshot = snapshot;
    }
}
