package dto;

public class KrakenBookUpdate extends BookUpdate {

    private final String type;

    public KrakenBookUpdate(String pairName, String type) {
        super(pairName);
        this.type = type;

    }

    public String getType() {
        return type;
    }
}
