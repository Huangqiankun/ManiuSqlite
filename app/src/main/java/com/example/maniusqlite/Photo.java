package com.example.maniusqlite;


import com.example.maniusqlite.sql.annotion.DbFiled;
import com.example.maniusqlite.sql.annotion.DbTable;

/**
 */
@DbTable("tb_photo")
public class Photo {
    /**
     *
  -------------------------
     @DbFiled("time")
     @DbFiled("path")
     *
     -------------------------
     @DbFiled("tb_time")
     @DbFiled("path")
     *
     */

//
    @DbFiled("time")
    public String time;
    @DbFiled("path")
    public String path;

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Photo( ) {
    }

    public Photo(String time, String path) {
        this.time = time;
        this.path = path;
    }
}
