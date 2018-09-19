package org.openecomp.sdc.be.model;

public class Position {

    private Double origMinPosX = Double.MAX_VALUE;
    private Double origMaxPosX = Double.MIN_VALUE;

    private Double origMinPosY = Double.MAX_VALUE;
    private Double origMaxPosY = Double.MIN_VALUE;

    private Double pagePosX;
    private Double pagePosY;

    private Double newMinPosX = Double.MAX_VALUE;
    private Double newMaxPosX = Double.MIN_VALUE;

    private Double newMinPosY = Double.MAX_VALUE;
    private Double newMaxPosY = Double.MIN_VALUE;

    public Position() {}

    public Position(String posX, String posY) {
        this.pagePosX = Double.parseDouble(posX);
        this.pagePosY = Double.parseDouble(posY);
    }

    public Double getOrigMinPosX() {
        return origMinPosX;
    }

    public void setOrigMinPosX(Double origMinPosX) {
        this.origMinPosX = origMinPosX;
    }

    public Double getOrigMaxPosX() {
        return origMaxPosX;
    }

    public void setOrigMaxPosX(Double origMaxPosX) {
        this.origMaxPosX = origMaxPosX;
    }

    public Double getOrigMinPosY() {
        return origMinPosY;
    }

    public void setOrigMinPosY(Double origMinPosY) {
        this.origMinPosY = origMinPosY;
    }

    public Double getOrigMaxPosY() {
        return origMaxPosY;
    }

    public void setOrigMaxPosY(Double origMaxPosY) {
        this.origMaxPosY = origMaxPosY;
    }

    public Double getPagePosX() {
        return pagePosX;
    }

    public void setPagePosX(Double pagePosX) {
        this.pagePosX = pagePosX;
    }

    public Double getPagePosY() {
        return pagePosY;
    }

    public void setPagePosY(Double pagePosY) {
        this.pagePosY = pagePosY;
    }

    public Double getNewMinPosX() {
        return newMinPosX;
    }

    public void setNewMinPosX(Double newMinPosX) {
        this.newMinPosX = newMinPosX;
    }

    public Double getNewMaxPosX() {
        return newMaxPosX;
    }

    public void setNewMaxPosX(Double newMaxPosX) {
        this.newMaxPosX = newMaxPosX;
    }

    public Double getNewMinPosY() {
        return newMinPosY;
    }

    public void setNewMinPosY(Double newMinPosY) {
        this.newMinPosY = newMinPosY;
    }

    public Double getNewMaxPosY() {
        return newMaxPosY;
    }

    public void setNewMaxPosY(Double newMaxPosY) {
        this.newMaxPosY = newMaxPosY;
    }
}

