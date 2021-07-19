package com.example.maniusqlite.sql.update;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.maniusqlite.User;
import com.example.maniusqlite.UserDao;
import com.example.maniusqlite.sql.BaseDaoFactory;
import com.example.maniusqlite.sql.tools.DomUtils;
import com.example.maniusqlite.sql.tools.FileUtil;
import com.example.maniusqlite.sql.update.bean.CreateDb;
import com.example.maniusqlite.sql.update.bean.CreateVersion;
import com.example.maniusqlite.sql.update.bean.UpdateDb;
import com.example.maniusqlite.sql.update.bean.UpdateDbXml;
import com.example.maniusqlite.sql.update.bean.UpdateStep;

import java.io.File;
import java.util.List;

public class UpdateManager {
    private File parentFile = new File(Environment.getExternalStorageDirectory(),
            "tencentmaniu");
    private File bakFile = new File(parentFile, "backDb");
//App  2年 记录 数据  用户
    private List<User> userList;

    //    dom
    public void startUpdateDb(Context context) {
        UpdateDbXml updateDbxml =  DomUtils.readDbXml(context);
//    下载 上一个版本  --》下一个版本
        String[] versions= FileUtil.getLocalVersionInfo(new File(parentFile,
                "update.txt"));
        //拿到上一个版本
        String lastVersion = versions[0];  //拿到当前版本
        String thisVersion = versions[1];

        String userfile = parentFile.getAbsolutePath() + "/user.db";
        String user_bak = bakFile.getAbsolutePath() + "/user.db";
        FileUtil.CopySingleFile(userfile, user_bak);
//        根据上一个版本  和最新版本
        userList = BaseDaoFactory.getInstance().getUserDao(UserDao.class, User.class).query(new User());
        //更新每个用户的数据库
        for (User user : userList) {
            String loginDbDir = parentFile.getAbsolutePath() + "/" + user.getName() + "/Msg3.0.db";
            String loginCopy = bakFile.getAbsolutePath() + "/" + user.getName() + "/Msg3.0.db";
            FileUtil.CopySingleFile(loginDbDir, loginCopy);
        }

        UpdateStep updateStep= DomUtils.findStepByVersion(updateDbxml, lastVersion, thisVersion);
        if (updateStep == null) {
            return;
        }
//
        List<UpdateDb> updateDbs = updateStep.getUpdateDbs();
        try {
            //    第二步   将原始数据库中所有的表名 更改成 bak_表名(数据还在)
            executeBeforesSql(updateDbs);

            // 第三步:检查新表，创建新表
            CreateVersion createVersion = DomUtils.findCreateByVersion(updateDbxml, thisVersion);
            executeCreateVersion(createVersion);
//          第四步  将原来bak_表名  的数据迁移到 新表中
            executeAftersSql(updateDbs);
        } catch (Exception e) {
            e.printStackTrace();
        }





    }


    private void executeCreateVersion(CreateVersion createVersion) throws Exception {
        if (createVersion == null || createVersion.getCreateDbs() == null) {
            throw new Exception("createVersion or createDbs is null;");
        }
        for (CreateDb cd : createVersion.getCreateDbs()) {
            if (cd == null || cd.getName() == null) {
                throw new Exception("db or dbName is null when createVersion;");
            }
            // 创建数据库表sql
            List<String> sqls = cd.getSqlCreates();
//            1次    N次
            SQLiteDatabase sqlitedb = null;
            if (userList != null && !userList.isEmpty()) {
                for (int i = 0; i < userList.size(); i++) {
                    sqlitedb = getDb(cd.getName(), userList.get(i).getName());
                    executeSql(sqlitedb, sqls);
                    sqlitedb.close();
                }
            }

        }

    }

    private void executeAftersSql(List<UpdateDb> updateDbs) throws Exception {
        for (UpdateDb db : updateDbs) {
            if (db == null || db.getName() == null) {
                throw new Exception("db or dbName is null;");
            }
            List<String> sqls = db.getSqlAfters();
            SQLiteDatabase sqlitedb = null;
//            1次       N次  2

            if (userList != null && !userList.isEmpty()) {
                // 多用户表升级
                for (int i = 0; i < userList.size(); i++) {
                    sqlitedb = getDb(db.getName(), userList.get(i).getName());
//之心数据库语句
                    executeSql(sqlitedb, sqls);
                    sqlitedb.close();
                }
            }
        }
    }
    //所有的表名 更改成 bak_表名(数据还在)
    private void executeBeforesSql(List<UpdateDb> updateDbs) throws Exception {
        for (UpdateDb db : updateDbs) {
            if (db == null || db.getName() == null) {
                throw new Exception("db or dbName is null;");
            }
            List<String> sqls = db.getSqlBefores();
            SQLiteDatabase sqlitedb = null;
//            1次       N次  2

            if (userList != null && !userList.isEmpty()) {
                // 多用户表升级
                for (int i = 0; i < userList.size(); i++) {
                    sqlitedb = getDb(db.getName(), userList.get(i).getName());
//之心数据库语句
                    executeSql(sqlitedb, sqls);
                    sqlitedb.close();
                }
            }

        }


    }

    private void executeSql(SQLiteDatabase sqlitedb, List<String> sqls) {
        // 检查参数
        if (sqls == null || sqls.size() == 0) {
            return;
        }
        sqlitedb.beginTransaction();
        for (String sql : sqls) {
            sql = sql.replaceAll("\r\n", " ");
            sql = sql.replaceAll("\n", " ");
            if (!"".equals(sql.trim())) {
                try {
                    // Logger.i(TAG, "执行sql：" + sql, false);
                    sqlitedb.execSQL(sql);
                } catch (SQLException e) {
                }
            }
        }

        sqlitedb.setTransactionSuccessful();
        sqlitedb.endTransaction();
    }

    private SQLiteDatabase getDb(String dbname, String userName) {
        String dbfilepath = null;
        SQLiteDatabase sqlitedb = null;
        File file = new File(parentFile, userName);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (dbname.equalsIgnoreCase("Msg3.0")) {
            dbfilepath = file.getAbsolutePath() + "/Msg3.0.db";// logic对应的数据库路径

        }

        if (dbfilepath != null) {
            File f = new File(dbfilepath);
            f.mkdirs();
            if (f.isDirectory()) {
                f.delete();
            }
            sqlitedb = SQLiteDatabase.openOrCreateDatabase(dbfilepath, null);
        }
        return sqlitedb;
    }

}
