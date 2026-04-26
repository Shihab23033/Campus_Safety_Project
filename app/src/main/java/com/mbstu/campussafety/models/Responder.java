package com.mbstu.campussafety.models;

public class Responder {
    private String responderId;
    private String name;
    private String type;
    private double latitude;
    private double longitude;
    private String status;
    private long lastUpdated;

    public Responder() {}

    public Responder(String responderId, String name, String type) {
        this.responderId = responderId;
        this.name = name;
        this.type = type;
        this.status = "AVAILABLE";
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getResponderId() { return responderId; }
    public void setResponderId(String responderId) { this.responderId = responderId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}
