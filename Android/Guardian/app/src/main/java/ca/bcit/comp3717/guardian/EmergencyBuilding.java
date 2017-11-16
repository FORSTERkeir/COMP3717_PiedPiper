package ca.bcit.comp3717.guardian;

/**
 * Created by Haley on 11/10/2017.
 */

public class EmergencyBuilding {
    private String latitutde;
    private String longitude;
    private String BldgName;

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
}