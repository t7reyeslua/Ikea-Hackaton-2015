package com.kien.luna.beacons;

public class BeaconObject {
    String UUID;
    int major;
    int minor;
    String region;

    public BeaconObject() {
    }

    public BeaconObject(String UUID, int major, int minor, String region) {
        this.UUID = UUID;
        this.major = major;
        this.minor = minor;
        this.region = region;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean compareBeacon(String uuid, int major, int minor){
        if((this.UUID.equals(uuid)) && (this.getMajor() == major) && (this.getMinor() == minor)){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "BeaconObject{" +
                "UUID='" + UUID + '\'' +
                ", major=" + major +
                ", minor=" + minor +
                ", region='" + region + '\'' +
                '}';
    }
}
