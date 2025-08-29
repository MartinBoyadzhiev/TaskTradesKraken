package dto_kraken;

import common.dto.BookUpdate;

public class KrakenBookUpdate extends BookUpdate {
    private final String type;

    public KrakenBookUpdate(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
