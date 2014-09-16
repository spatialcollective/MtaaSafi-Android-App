package com.sc.mtaasafi.android;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Agree on 9/4/2014.
 * Data class for passing data about posts
 */
public class PostData {
    String content;
    String timestamp;
    String userName;
    String userPicURL;
    String mediaURL;
    List<String> networksShared;
    double latitude;
    double longitude;
    byte[] picture;

    PostData(){
        content = timestamp = userName = userPicURL = mediaURL = "";
        latitude = longitude = 0;
    }

    PostData(String usn, String userPicURL, String timestamp, double lat, double lon,
             String content, String mediaURL, List<String> networksShared){
        this.userName = usn;
        this.userPicURL = userPicURL;
        this.timestamp = timestamp;
        this.content = content;
        latitude = lat;
        longitude = lon;
        this.networksShared = networksShared;
        this.mediaURL = mediaURL;
    }

    PostData(String usn, String timestamp, double lat, double lon,
             String content){
        this.userName = usn;
        this.userPicURL = userPicURL;
        this.timestamp = timestamp;
        this.content = content;
        latitude = lat;
        longitude = lon;
    }

    public void setPic(byte[] pic){
        picture = pic;
    }
}
