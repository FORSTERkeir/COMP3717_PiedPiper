package ca.bcit.comp3717.guardian.model;


public class LinkedUser {

    private int userIdMe;
    private int userIdTarget;
    private boolean alertMe;
    private boolean alertTarget;
    private boolean muteMe;
    private boolean muteTarget;
    private boolean deleted;
    private boolean addedMe;
    private boolean addedTarget;

    public LinkedUser() {}

    public LinkedUser(int userIdMe, int userIdTarget, boolean alertMe, boolean alertTarget,
                      boolean muteMe, boolean muteTarget, boolean deleted, boolean addedMe,
                      boolean addedTarget) {

        this.userIdMe = userIdMe;
        this.userIdTarget = userIdTarget;
        this.alertMe = alertMe;
        this.alertTarget = alertTarget;
        this.muteMe = muteMe;
        this.muteTarget = muteTarget;
        this.deleted = deleted;
        this.addedMe = addedMe;
        this.addedTarget = addedTarget;
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
}
