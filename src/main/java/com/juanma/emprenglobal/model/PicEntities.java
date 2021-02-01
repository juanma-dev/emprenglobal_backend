package com.juanma.emprenglobal.model;

public enum PicEntities {
    Events("event_pic"),
    Galleries("gallery_pic"),
    News("news_pic"),
    Users("user_pic");

    private String tag;
    PicEntities(String tag) { this.tag = tag; }
    protected String getTag() {return this.tag;}
}
