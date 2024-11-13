package wxy;

import java.util.List;

public class DataPackage {
    private String MAC;
    private String channelId;
    private long time;
    private String MACID;
    private int speed;
    private String unit;
    private List<Float> dynaValues;

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMACID() {
        return MACID;
    }

    public void setMACID(String MACID) {
        this.MACID = MACID;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<Float> getDynaValues() {
        return dynaValues;
    }

    public void setDynaValues(List<Float> dynaValues) {
        this.dynaValues = dynaValues;
    }
}
