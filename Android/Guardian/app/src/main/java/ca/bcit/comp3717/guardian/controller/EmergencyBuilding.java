package ca.bcit.comp3717.guardian.controller;

/**
 * Created by Haley on 11/10/2017.
 */

public class EmergencyBuilding {
    private String latitutde;
    private String longitude;
    private String BldgName;
    private int category;
    private long phone;

    public void setBldgName(String BldgName) {
        this.BldgName = BldgName;
    }

    public String getBldgName() {
        return this.BldgName;
    }

    void setLatitutde(String latitutde) {
        this.latitutde = latitutde;
    }

    String getLatitutde() {
        return this.latitutde;
    }

    void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    String getLongitude() {
        return this.longitude;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getCategory() {
        return category;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }
    public long getPhone() {
        return phone;
    }
}