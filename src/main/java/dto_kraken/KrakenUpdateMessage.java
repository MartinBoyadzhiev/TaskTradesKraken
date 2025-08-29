package dto_kraken;

import java.util.List;

public class KrakenUpdateMessage {
    private String channel;
    private String type;
    private List<UpdateData> data;

    public KrakenUpdateMessage() {
    }

    public String getType() {
        return type;
    }

    public List<UpdateData> getData() {
        return data;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setData(List<UpdateData> data) {
        this.data = data;
    }
}
