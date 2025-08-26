package dto;

import java.util.List;

public class KrakenBookUpdate extends BookUpdate {
    private String channel;
    private String type;
    private List<UpdateData> data;

    public KrakenBookUpdate() {
    }

    public String getType() {
        return type;
    }

    public List<UpdateData> getData() {
        return data;
    }
}
