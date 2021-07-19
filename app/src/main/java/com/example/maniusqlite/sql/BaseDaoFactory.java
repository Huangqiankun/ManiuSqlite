package com.example.maniusqlite.sql;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.maniusqlite.R;
import com.example.maniusqlite.sql.annotion.DbTable;
import com.example.maniusqlite.sql.enums.PrivateDataBaseEnums;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BaseDaoFactory {


//    public synchronized <R extends BaseDao<T>, T> R createBaseDao() {
//
//    }

    private SQLiteDatabase sqLiteDatabase;
    private String sqliteDatabasePath;


//总数据  库  只包含用户表     登录状态
    private SQLiteDatabase userDatabase;


    //保存所有的dao层，实现单例
    protected Map<String, BaseDao> map = Collections.synchronizedMap(new HashMap<String, BaseDao>());

//对象    单例模式     业务层的      购物车 模块
    private static  BaseDaoFactory instance=new BaseDaoFactory();

    public  static  BaseDaoFactory getInstance()
    {
        return instance;
    }
    public BaseDaoFactory()
    {
//        总表
        File file = new File(Environment.getExternalStorageDirectory(), "tencentmaniu");
        if (!file.exists()) {
            file.mkdirs();
        }
        String userDatabasePath = file.getAbsolutePath() + "/user.db";
//        总数据库
        userDatabase = SQLiteDatabase.openOrCreateDatabase(userDatabasePath, null);
    }
//    个人数据库
    public synchronized <T> BaseDao<T> getAppDao(Class<T> entityClass) {
        BaseDao baseDao = null;
        if (sqLiteDatabase == null) {
            sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(PrivateDataBaseEnums.database.getValue(), null);
        }
        try {
            baseDao = BaseDao.class.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        baseDao.init(entityClass, sqLiteDatabase);
        return baseDao;
    }
//    userDao
    public synchronized <T extends BaseDao<M>, M> T getUserDao(Class<T> daoClass, Class<M> entityClass) {
        BaseDao baseDao = null;
        if (map.get(daoClass.getSimpleName()) != null) {
            return (T) map.get(daoClass.getSimpleName());
        }
//        userdao
        try {
            baseDao = daoClass.newInstance();
            baseDao.init(entityClass, userDatabase);
            map.put(daoClass.getSimpleName(), baseDao);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) baseDao;
    }


//    appDao








    public synchronized <R extends BaseDao<T>, T> R createBaseDao(Class<R> clazz,Class<T> entityClass) {
        BaseDao baseDao=null;
        try {
            baseDao=clazz.newInstance();
            baseDao.init(entityClass, sqLiteDatabase);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return (R) baseDao;
    }



}
