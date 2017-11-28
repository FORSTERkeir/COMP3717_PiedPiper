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
    private boolean confirmedByGui;
    private boolean muteModifiedByGui;
    private boolean alertModifiedByGui;

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

    public boolean isConfirmedByGui() {
        return confirmedByGui;
    }

    public void setConfirmedByGui(boolean confirmedByGui) {
        this.confirmedByGui = confirmedByGui;
    }

    public boolean isMuteModifiedByGui() {
        return muteModifiedByGui;
    }

    public void setMuteModifiedByGui(boolean muteModifiedByGui) {
        this.muteModifiedByGui = muteModifiedByGui;
    }

    public boolean isAlertModifiedByGui() {
        return alertModifiedByGui;
    }

    public void setAlertModifiedByGui(boolean alertModifiedByGui) {
        this.alertModifiedByGui = alertModifiedByGui;
    }

    @Override
    public int compareTo(LinkedUser lu) {
        return this.getNameTarget().toLowerCase().compareTo(lu.getNameTarget().toLowerCase());
    }
}
