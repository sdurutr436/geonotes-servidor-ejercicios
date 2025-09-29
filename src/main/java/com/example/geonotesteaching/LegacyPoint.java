package com.example.geonotesteaching;

public class LegacyPoint {
    private double lat, lon;

    public LegacyPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "LegacyPoint{" +
                "lat=" + lat +
                ", lon=" + lon +
                '}';
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.equals(obj);
    }

}
