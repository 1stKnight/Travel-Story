package com.project.travel.story.Utility.Model;

import java.io.Serializable;

public class AlbumDetails implements Serializable {

    private String title;
    private String description;
    private String photoURL;
    private String key;
    private String date;

    public AlbumDetails(){}

    public AlbumDetails (String date, String title,String description, String photoURL, String key){

        this.title = title;
        this.description = description;
        this.date = date;
        this.photoURL = photoURL;
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
