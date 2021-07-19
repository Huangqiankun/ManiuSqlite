package com.example.maniusqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.maniusqlite.sql.BaseDao;
import com.example.maniusqlite.sql.BaseDaoFactory;
import com.example.maniusqlite.sql.IBaseDao;
import com.example.maniusqlite.sql.tools.FileUtil;
import com.example.maniusqlite.sql.update.UpdateManager;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    IBaseDao<User> userDao;
    UpdateManager updateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        工厂架构师     你
        checkPermission(this);
    }

    public static boolean checkPermission(
            Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

        }
        return false;
    }

    //    查询   删除
    public void save(View view) {
        updateManager = new UpdateManager();
        userDao = BaseDaoFactory.getInstance().getUserDao(UserDao.class, User.class);
        String name = ((EditText) (findViewById(R.id.name))).getText().toString();
        String password = ((EditText) (findViewById(R.id.password))).getText().toString();
//        行  1 不行2
        userDao.insert(new User("3", name, password, 1));
//        User where = new User();
//        where.setAge(21);
//        userDao.update(new User("jett", 99), where);
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

    }

    public void inserPerson(View view) {
        IBaseDao<Photo> photoDao = BaseDaoFactory.getInstance().
                getAppDao(Photo.class);
        for (int i = 0; i < 100; i++) {
            photoDao.insert(new Photo("2021-7-12", "/sdcard/tencetmaniu/这是daivd老师的V003数据库" + i + ".jpg"));
        }
        //        List<Photo> photo=  photoDao.query(new Photo());
    }

    public void queryList(View view) {
    }

    // 下载apk    后台     v002    -----> v006  apk的文件下载下来
    public void dowloadApk(View view) {
//        xaizai
        FileUtil.saveVersionInfo(this, "V006");
    }

//    手动     按钮
//    服务器    url地址   下载 存在   apk  版本    版本
//    V003  apk   升级    V006 apk
//    数据库版本  V003       V006
    public void update(View view) {
        UpdateManager updateManager = new UpdateManager();
        updateManager.startUpdateDb(this);
    }
}