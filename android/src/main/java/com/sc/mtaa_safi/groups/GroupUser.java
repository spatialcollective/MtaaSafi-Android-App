package com.sc.mtaa_safi.groups;

/**
 * Created by ishuah on 9/10/15.
 */
public class GroupUser {
    private String name;
    private int _id, serverId;

    public GroupUser(String name, int serverId){
        this.name = name;
        this.serverId = serverId;
    }

    public GroupUser(String name, int serverId, int _id){
        this.name = name;
        this.serverId = serverId;
        this._id = _id;
    }

    public String getUserName(){
        return this.name;
    }

    public int getServerId(){
        return this.serverId;
    }

    public int get_id(){
        return this._id;
    }

    @Override
    public String toString(){
        return this.name;
    }

    
}
