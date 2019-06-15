package com.project.travel.story.Utility.Model;

import java.io.Serializable;

public class PhotoDetails implements Serializable {

   private String date;
   private String category;
   private String description;
   private String photoURL;
   private String key;
   private String id;
   private String lat;
   private String lng;

    public PhotoDetails(){}

    public PhotoDetails(String date, String category, String description, String photoURL, String id, String lat, String lng) {
        this.date = date;
        this.category = category;
        this.description = description;
        this.photoURL = photoURL;
        this.id = id;
        this.lat = lat;
        this.lng = lng;
    }

    public PhotoDetails(String date, String category, String description, String photoURL, String id, String lat, String lng, String key) {
        this.date = date;
        this.category = category;
        this.description = description;
        this.photoURL = photoURL;
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
