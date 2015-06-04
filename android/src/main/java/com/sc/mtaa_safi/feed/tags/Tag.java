package com.sc.mtaa_safi.feed.tags;

import java.io.Serializable;

/**
 * Created by ishuah on 6/2/15.
 */
public class Tag implements Serializable {
    private String tagText;

    public Tag(String tagText){
        this.tagText = tagText;
    }

    public String getTagText(){
        return this.tagText;
    }

    @Override
    public String toString(){
        return this.tagText;
    }

}
