package com.csci571.koy.hw9.model;

public class SearchResultItem {

    private String icon_url, place_name, place_id, place_lat, place_lng, vicinity;
    private String myLat, myLng, websiteUrl;
    private  boolean isFavorited;

    public SearchResultItem() { }

    public SearchResultItem(String i, String p_name, String p_id, String v, String m_lat, String m_lng, boolean f) {
        this.icon_url = i;
        this.place_name = p_name;
        this.place_id = p_id;
//        this.place_lat = p_lat;
//        this.place_lng = p_lng;
        this.myLat = m_lat;
        this.myLng = m_lng;
        this.vicinity = v;

        this.isFavorited = f;
    }

    public String getIconUrl() {
        return icon_url;
    }

    public void setIconUrl(String i) {
        this.icon_url = i;
    }

    public String getPlaceName() {
        return place_name;
    }

    public void setPlaceName(String p_name) {
        this.place_name = p_name;
    }

    public String getPlaceId() {
        return place_id;
    }

    public void setPlaceId(String p_id) {
        this.place_id = p_id;
    }

    public String getPlaceLat() {
        return place_lat;
    }

    public void setPlaceLat(String p_lat) {
        this.place_lat = p_lat;
    }

    public String getPlaceLng() {
        return place_lng;
    }

    public void setPlaceLng(String p_lng) {
        this.place_lng = p_lng;
    }

    public String getMyLng() {
        return myLng;
    }

    public void setMyLng(String m_lng) {
        this.myLng = m_lng;
    }

    public String getMyLat() { return myLat; }

    public void setMyLat(String m_lat) { this.myLat = m_lat; }

    public String getVicinity() {
        return vicinity;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setVicinity(String v) {
        this.vicinity = v;
    }

    public void setWebsiteUrl(String v) {
        this.websiteUrl = v;
    }

    public boolean getIsFavorited() {
        return isFavorited;
    }

    public void setIsFavorited(boolean f) {
        this.isFavorited = f;
    }
}
