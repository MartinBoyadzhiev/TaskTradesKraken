package dto;

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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setAsks(List<OrderLevel> asks) {
        this.asks = asks;
    }

    public void setBids(List<OrderLevel> bids) {
        this.bids = bids;
    }

    public Long getChecksum() {
        return checksum;
    }

    public void setChecksum(Long checksum) {
        this.checksum = checksum;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
