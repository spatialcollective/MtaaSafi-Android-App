package com.sc.mtaasafi.android;

/**
 * Created by Agree on 9/4/2014.
 * Data class for passing data about posts
 */
public class PostData {
    String content;
    String timestamp;
    String user;

    PostData(){
        content = timestamp = user = "";
    }

    PostData(String user, String timestamp, String content){
        this.user = user;
        this.timestamp = timestamp;
        this.content = content;
    }

}
