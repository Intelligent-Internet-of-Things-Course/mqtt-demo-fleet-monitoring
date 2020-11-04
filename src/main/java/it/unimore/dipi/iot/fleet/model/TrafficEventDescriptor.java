package it.unimore.dipi.iot.fleet.model;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project mqtt-demo-fleet-monitoring
 * @created 04/11/2020 - 20:05
 */
public class TrafficEventDescriptor {

    public static final String JAM_TRAFFIC_EVENT = "jam_traffic_event";

    private String type;

    private double latitude;

    private double longitude;

    private long timestamp;

    public TrafficEventDescriptor() {
    }

    public TrafficEventDescriptor(String type, double latitude, double longitude, long timestamp) {
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TrafficEventDescriptor{");
        sb.append("type='").append(type).append('\'');
        sb.append(", latitude='").append(latitude).append('\'');
        sb.append(", longitude='").append(longitude).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
}
