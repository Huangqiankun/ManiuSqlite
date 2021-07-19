package com.example.maniusqlite;


import com.example.maniusqlite.sql.annotion.DbFiled;
import com.example.maniusqlite.sql.annotion.DbTable;

@DbTable("tb_user")
public class User {
    @DbFiled("_id")
    public String id;
    @DbFiled("name")
    public String name;
    @DbFiled("password")
    public String password;
//    1登录状态
//    0 未登录
    @DbFiled("status")
    public Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public User() {
    }

    public User(String id, String name, String password, Integer status) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.status = status;
    }

    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
