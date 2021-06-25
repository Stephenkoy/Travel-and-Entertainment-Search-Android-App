package com.csci571.koy.hw9.model;

public class PlaceReviewItem {
    // yelp unique
    private String id;

    // shared
    private String authorName, profilePic, reviewText, timeCreated, url;
    private double rating;
    // google places
    private String authorUrl;

    public PlaceReviewItem() { }

    // for google review
    public PlaceReviewItem(String a_name, String a_url, String pic, double rate, String text, String time) {
        this.authorName = a_name;
        this.url = a_url;
        this.profilePic = pic;
        this.rating = rate;
        this.reviewText = text;
        this.timeCreated = time;
    }

    // for Yelp Review
    public PlaceReviewItem(String id, String url, String text, double rating, String time, String pic, String a_name) {
        this.id = id;
        this.url = url;
        this.reviewText = text;
        this.rating = rating;
        this.timeCreated = time;
        this.profilePic = pic;
        this.authorName = a_name;
    }

    public void setAuthorName(String a) { this.authorName = a; }
    public void setUrl(String a) { this.url = a; }
    public void setProfilePic(String a) { this.profilePic = a; }
    public void setReviewRating(double a) { this.rating = a; }
    public void setReviewText(String a) { this.reviewText = a; }
    public void setTimeCreated(String a) { this.timeCreated = a; }
    public void setReviewId(String a) { this.id = a; }

    public String getAuthorName() { return authorName; }
    public String getProfilePic() { return profilePic; }
    public double getreviewRating() { return rating; }
    public String getReviewText() { return reviewText; }
    public String getTimeCreated() { return timeCreated; }
    public String getUrl() { return url; }
    public String getReviewId() { return id; }



}
