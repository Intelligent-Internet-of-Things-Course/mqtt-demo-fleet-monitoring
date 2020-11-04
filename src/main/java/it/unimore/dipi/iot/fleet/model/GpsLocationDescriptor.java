package it.unimore.dipi.iot.fleet.model;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project mqtt-demo-fleet-monitoring
 * @created 04/11/2020 - 15:03
 */
public class GpsLocationDescriptor {

    public static final String FILE_LOCATION_PROVIDER = "location_provider_file";

    public static final String GPS_LOCATION_PROVIDER = "location_provider_gps";

    public static final String NETWORK_LOCATION_PROVIDER = "location_provider_network";

    private double latitude;

    private double longitude;

    private double elevation;

    private String provider;

    public GpsLocationDescriptor() {
    }

    public GpsLocationDescriptor(double latitude, double longitude, double elevation, String provider) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.provider = provider;
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

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GpsLocationDescriptor{");
        sb.append("latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append(", elevation=").append(elevation);
        sb.append(", provider='").append(provider).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
