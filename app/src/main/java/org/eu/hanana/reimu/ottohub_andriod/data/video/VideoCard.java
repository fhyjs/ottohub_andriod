package org.eu.hanana.reimu.ottohub_andriod.data.video;

// VideoCard.java
public class VideoCard {
    private String pic_url;
    private String user_url;
    private String title;
    private String duration;
    private String author;
    private String views;
    private int vid;

    public VideoCard(String pic_url, String userUrl, String title, String duration, String author, String views, int vid) {
        this.pic_url = pic_url;
        user_url = userUrl;
        this.title = title;
        this.duration = duration;
        this.author = author;
        this.views = views;
        this.vid = vid;
    }

    // Getters

    public String getPic_url() {
        return pic_url;
    }
    public String getTitle() { return title; }
    public String getDuration() { return duration; }
    public String getAuthor() { return author; }
    public String getViews() { return views; }

    public String getUser_url() {
        return user_url;
    }

    public int getVid() {
        return vid;
    }
}