package com.example.maniusqlite.sql.enums;

import android.os.Environment;

import com.example.maniusqlite.User;
import com.example.maniusqlite.UserDao;
import com.example.maniusqlite.sql.BaseDaoFactory;

import java.io.File;

public enum PrivateDataBaseEnums {
    //App 路径    没有   static  String path  路径  状态
    database("Msg3.0.db");
    private String value;
    PrivateDataBaseEnums(String value )
    {
        this.value = value;
    }
    public String getValue()
    {
        UserDao userDao= BaseDaoFactory.getInstance().getUserDao(UserDao.class, User.class);
        User currentUser=userDao.getCurrentUser();
        if (currentUser != null) {
            File file=new File(Environment.getExternalStorageDirectory(),
                    "tencentmaniu");
            if(!file.exists())
            {
                file.mkdirs();
            }
            File userFile = new File(file, currentUser.getName());
            if(!userFile.exists())
            {
                userFile.mkdirs();
            }
            String userDatabasePath= userFile.getAbsolutePath()+"/"+value;
            return userDatabasePath;
        }

        return null;

    }
}
