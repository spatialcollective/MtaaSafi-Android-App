package com.sc.mtaasafi.android;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Agree on 9/4/2014.
 * Data class for passing data about posts
 */
public class PostData {
    public String title, details, timestamp, userName, proPicURL, mediaURL;
    public List<String> networksShared;
    public double latitude, longitude;
    public byte[] picture;

    public PostData(){
        title = details = timestamp = userName = proPicURL = mediaURL = "";
        latitude = longitude = 0;
    }

    // for PostData objects created with data sent from the server
    public PostData(String usn, String timestamp, double lat, double lon, String title,
             String details, String mediaURL, List<String> networksShared){
        this.userName = usn;
        this.timestamp = timestamp;
        this.title = title;
        this.details = details;
        latitude = lat;
        longitude = lon;
        if(networksShared != null)
            this.networksShared = networksShared;
        this.mediaURL = mediaURL;
    }

    // for PostData objects created by the user to send to server
    public PostData(String usn, String timestamp, double lat, double lon,
             String title, String details){
        this.userName = usn;
        this.timestamp = timestamp;
        this.title = title;
        this.details = details;
        latitude = lat;
        longitude = lon;
    }
    // for PostData objects created by the user *with pictures* to send to the server
    public PostData(String usn, String timestamp, double lat, double lon,
             String title, String details, byte[] picture){
        this.userName = usn;
        this.timestamp = timestamp;
        this.title = title;
        this.details = details;
        latitude = lat;
        longitude = lon;
        this.picture = picture;
    }

    // takes a timestamp in format "yyyy-MM-dd'T'H:mm:ss"
    public static String timeSincePosted(String timestamp) {
        Log.d(LogTags.BACKEND_W, "Received timestamp: " + timestamp);
        long second = 1000;
        long minute = 60 * second;
        long hour = 60* minute;
        long day = 24 * hour;
        long week = 7 * day;
        long year = 365 * day;
        SimpleDateFormat df = new SimpleDateFormat("H:mm:ss dd-MM-yyyy");
        String timeSinceStr = "";
        try {
            Date date = df.parse(timestamp);
            long currentTimeInEpoch = System.currentTimeMillis();
            long postTimeInEpoch = date.getTime();
            long timeSince = currentTimeInEpoch - postTimeInEpoch;
            if (timeSince > week){ // if greater than a year
                timeSinceStr = new SimpleDateFormat("dd/MM")
                        .format(date);
            }
            else if (timeSince > day + 12 * hour){
                int i = 1;
                while(timeSince > i*day){
                    i++;
                }
                timeSinceStr = i+1 + "d";
            }
            else if(timeSince > day && timeSince < day + 12 * hour){
                timeSinceStr = "1d";
            }
            else if (timeSince > hour) {
                int i = 1;
                while (timeSince > i*hour){
                    // this rounds up the hour by 10 minutes.
                    // n hours + 46m => return (n+1) + "h";
                    if(timeSince < (i+1)*hour - 15){
                        break;
                    }
                    i++;
                }
                timeSinceStr = i + "h";
            }
            else if(timeSince > minute){
                int i = 1;
                while(timeSince > i*minute){
                    i++;
                }
                timeSinceStr = i + "m";
            }
            else{
                timeSinceStr = "just now";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeSinceStr;
    }
}
