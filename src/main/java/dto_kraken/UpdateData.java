package dto_kraken;

import common.dto.OrderLevel;
import java.util.List;

public class UpdateData {
    private String symbol;
    private List<OrderLevel> asks;
    private List<OrderLevel> bids;
    private Long checksum;
    private String timestamp;

    public UpdateData() {
    }

    public List<OrderLevel> getAsks() {
        return asks;
    }

    public List<OrderLevel> getBids() {
        return bids;
    }
}
