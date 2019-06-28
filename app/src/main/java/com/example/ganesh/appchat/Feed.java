package com.example.ganesh.appchat;

/**
 * Created by Pasindu Sandaruwan on 9/24/2018.
 */

public class Feed {

    private String title;
    private String desc;
    private String image;
    private String username;
    private String date;
    private String time;

    public Feed(){


    }

    public Feed( String title, String desc , String image, String username ){
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.username = username;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public void setImage(String image){
        this.image = image;
    }

    public void setUsername(String username ){
        this.username = username;
    }

    public void setDate(String date){
        this.date = date;
    }

    public void setTime( String time ){
        this.time = time;
    }

    public String getTitle(){
        return title;
    }

    public String getDesc(){
        return desc;
    }

    public String getImage(){
        return image;
    }

    public String getUsername(){
        return username;
    }

    public String getDate(){
        return date;
    }

    public String getTime(){
        return time;
    }
}
