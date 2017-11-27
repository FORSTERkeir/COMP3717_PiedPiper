package ca.bcit.comp3717.guardian.model;

public class LinkedUser implements Comparable<LinkedUser> {

    private int userIdMe;
    private int userIdTarget;
    private String nameTarget;
    private boolean alertMe;
    private boolean alertTarget;
    private boolean muteMe;
    private boolean muteTarget;
    private boolean deleted;
    private boolean addedMe;
    private boolean addedTarget;
    private int statusTarget;

    public LinkedUser() {}

    public String getNameTarget() {
        return nameTarget;
    }

    public void setNameTarget(String nameTarget) {
        this.nameTarget = nameTarget;
    }

    public int getUserIdMe() {
        return userIdMe;
    }

    public void setUserIdMe(int userIdMe) {
        this.userIdMe = userIdMe;
    }

    public int getUserIdTarget() {
        return userIdTarget;
    }

    public void setUserIdTarget(int userIdTarget) {
        this.userIdTarget = userIdTarget;
    }

    public boolean isAlertMe() {
        return alertMe;
    }

    public void setAlertMe(boolean alertMe) {
        this.alertMe = alertMe;
    }

    public boolean isAlertTarget() {
        return alertTarget;
    }

    public void setAlertTarget(boolean alertTarget) {
        this.alertTarget = alertTarget;
    }

    public boolean isMuteMe() {
        return muteMe;
    }

    public void setMuteMe(boolean muteMe) {
        this.muteMe = muteMe;
    }

    public boolean isMuteTarget() {
        return muteTarget;
    }

    public void setMuteTarget(boolean muteTarget) {
        this.muteTarget = muteTarget;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isAddedMe() {
        return addedMe;
    }

    public void setAddedMe(boolean addedMe) {
        this.addedMe = addedMe;
    }

    public boolean isAddedTarget() {
        return addedTarget;
    }

    public void setAddedTarget(boolean addedTarget) {
        this.addedTarget = addedTarget;
    }

    public int getStatusTarget() {
        return statusTarget;
    }

    public void setStatusTarget(int statusTarget) {
        this.statusTarget = statusTarget;
    }

    @Override
    public int compareTo(LinkedUser lu) {
        return this.getNameTarget().toLowerCase().compareTo(lu.getNameTarget().toLowerCase());
    }
}
