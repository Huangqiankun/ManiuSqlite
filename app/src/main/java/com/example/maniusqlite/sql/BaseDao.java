package com.example.maniusqlite.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.example.maniusqlite.User;
import com.example.maniusqlite.sql.annotion.DbFiled;
import com.example.maniusqlite.sql.annotion.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseDao<T> implements IBaseDao<T> {
    private static final String TAG = "david";
    /**]
     * 持有数据库操作类的引用
     */
    private SQLiteDatabase database;
    /**
     * 持有操作数据库表所对应的java类型
     * User
     */
    private Class<T> entityClass;
    /**
     * 保证实例化一次
     */
    private boolean isInit=false;
    private String tableName;

//    检查表
    private HashMap<String,Field> cacheMap;
    protected BaseDao() {
    }

    protected synchronized boolean init(Class<T> entity, SQLiteDatabase sqLiteDatabase) {

        if(!isInit)
        {
//初始化完了  自动建表
            entityClass=entity;
            database=sqLiteDatabase;
            if (entity.getAnnotation(DbTable.class) == null) {
                tableName=entity.getClass().getSimpleName();
            }else {
                tableName=entity.getAnnotation(DbTable.class).value();
            }
            if (!database.isOpen()) {
                return  false;
            }

            String sql = createTable();
            database.execSQL(sql);
//            try {
//                database.execSQL(sql);
//            } catch (Exception e) {
//                isInit = false;
//                return false;
//            }
//            String   java  String  sqlite  String   有1  没有2
//            tb_user    tb_name
            //建立好映射关系
            initCacheMap();
            isInit = true;
        }
        return isInit;
    }
//  将真实表中的列名  + 成员变量进行 映射
//    优化      提前   后面 Filed[]
    private void initCacheMap() {
        cacheMap = new HashMap<>();
//        有必要 请求数据   有  1 没有 2
        String sql = "select * from " + tableName+" limit 0";
//
        Cursor cursor = database.rawQuery(sql, null);
        String[] colmunNames= cursor.getColumnNames();
        Field[] colmunFields = entityClass.getDeclaredFields();
        for (String colmunName : colmunNames) {

            Field reslutField = null;
            for (Field field : colmunFields) {
                String fieldAnnotionName=field.getAnnotation(DbFiled.class).value();
                if (colmunName.equals(fieldAnnotionName)) {
                    reslutField = field;
                    break;
                }
            }
            if (reslutField != null) {
                cacheMap.put(colmunName, reslutField);
            }
        }
    }
    // create table  if not exists user( tb_name TEXT, age INTEGER  );
    protected     String createTable(){

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("create table if not exists ");
        stringBuffer.append(tableName + " (");
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            Class type = field.getType();
            if (type == String.class) {
                stringBuffer.append(field.getAnnotation(DbFiled.class).value() + " TEXT,");
            }else if(type==Double.class){
                stringBuffer.append(field.getAnnotation(DbFiled.class).value()+ "  DOUBLE,");
            }else  if(type==Integer.class)
            {
                stringBuffer.append(field.getAnnotation(DbFiled.class).value()+ "  INTEGER,");
            }else if(type==Long.class)
            {
                stringBuffer.append(field.getAnnotation(DbFiled.class).value()+ "  BIGINT,");
            }else  if(type==byte[].class)
            {
                stringBuffer.append(field.getAnnotation(DbFiled.class).value()+ "  BLOB,");

            }else {
                  /*
                不支持的类型
                 */
                continue;
            }
//            sqlite  bitmap-->byte[]

        }
        if (stringBuffer.charAt(stringBuffer.length() - 1) == ',') {
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        }
        stringBuffer.append(")");
        Log.i(TAG, "createTable: "+stringBuffer.toString());
        return stringBuffer.toString();
    }

    /**
     *
     ContentValues cv = new ContentValues();
//tb_name   --->  "david
     cv.put("tb_name", "david");
     cv.put("age", 11);
     result = mDB.insert(TABLE_NAME, "", cv);
     */
    @Override
    public Long insert(T entity) {
//        entity  - hashmap
//        hashmap--->ContentValues
        Map<String, String> map = getValues(entity);
        ContentValues contentValues=getContentValues(map);
        return database.insert(tableName, null, contentValues);
//        怎么办才能添加
    }

    private Map<String, String> getValues(T entity) {
        HashMap<String, String> map = new HashMap<>();
        Iterator<Field> fieldIterator = cacheMap.values().iterator();
        while (fieldIterator.hasNext()) {
            Field field = fieldIterator.next();
            field.setAccessible(true);
            try {
                Object object= field.get(entity);
                if (object == null) {
                    continue;
                }
                String value=object.toString();
                String key=field.getAnnotation(DbFiled.class).value();
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    map.put(key, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
//        数据库升级     1  没有2  大型  100版本   ---》 开发    101  手对    架构优势  名字
        return map;
    }

    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues=new ContentValues();
        Set keys=map.keySet();
        Iterator<String> iterator=keys.iterator();
        while (iterator.hasNext())
        {
            String key=iterator.next();
            String value=map.get(key);
            if(value!=null)
            {
                contentValues.put(key,value);
            }
        }

        return contentValues;
    }
//带插入    的数据         where 你要修改哪条记录     null   所有
    /**
     *
     ContentValues cv = new ContentValues();
     cv.put("tb_name", "david");
     cv.put("age", 11);
     database.update(tableName, cv, "  1=1 and name =  ? and age =? ", new String[]{"david"});
     */
    @Override
    public int update(T entity, T where) {
        Map values=getValues(entity);
        ContentValues contentValues=getContentValues(values);
//条件
        Map whereMap = getValues(where);
        Condition condition = new Condition(whereMap);
        database.update(tableName, contentValues, condition.whereClause, condition.whereArgs);
        return 0;
    }
    class Condition
    {
//        "name = ? "  一层 中屏蔽细节    底层还是那一套  ojhttp   socket
//         socket   1    okhttp 2
//         Glide   down-bitmap  1    Glide    原则
        String whereClause;
//        new String[]{"david"}
        String[] whereArgs;
        public Condition(Map<String ,String> whereClause) {
//
            boolean flag = false;
            if (true && flag) {

            }
            ArrayList list=new ArrayList();
            StringBuilder stringBuilder=new StringBuilder();
            Set keys=whereClause.keySet();
            Iterator iterator=keys.iterator();
            stringBuilder.append(" 1=1 ");
            while (iterator.hasNext())
            {
                String key= (String) iterator.next();
                String value=whereClause.get(key);

                if (value!=null)
                {
                    stringBuilder.append(" and " + key + " =?");
                    list.add(value);
                }

            }
            this.whereClause=stringBuilder.toString();
            this.whereArgs= (String[]) list.toArray(new String[list.size()]);
        }
    }

    @Override
    public int delete(T where) {
        Map map=getValues(where);
        Condition condition=new Condition(map);
        database.delete(tableName, condition.whereClause, condition.whereArgs);
        return 0;
    }
    @Override
    public List<T> query(T where) {
        return query(where, null, null, null, null, null
        );
    }
//所有  条件
    @Override
    public List<T> query(T where, String groupBy, String orderBy, String having,Integer startIndex,
                         Integer limit) {
        String limitString=null;
        if(startIndex!=null&&limit!=null)
        {
            limitString=startIndex+" , "+limit;
        }

        Map map=getValues(where);
        Condition condition=new Condition(map);
        Cursor cursor=  database.query(tableName, null, condition.whereClause,
                condition.whereArgs,
                groupBy, having,
                orderBy, limitString
        );
//        封装   --返回

        List<T> result = getResult(cursor, where);
        cursor.close();
        return result;
    }
    private List<T> getResult(Cursor cursor, T where) {
        ArrayList  list=new ArrayList();
        Object item;
        while (cursor.moveToNext()) {
            try {
//                cachmap        ---对象中的成员变量    Filed    annotion-- tb_name
//cacheMap    name  ---Filed       1
//            tb_name       ---Filed  2
                item=where.getClass().newInstance();
//                item.set Filed.set(item, "david");
                Iterator iterator=cacheMap.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry entry= (Map.Entry) iterator.next();
                    //tb_name
                    /**
                     * 得到列名
                     */
                    String colomunName= (String) entry.getKey();
//                    通过列名查找到游标的索性
                    Integer colmunIndex= cursor.getColumnIndex(colomunName);
//                    Filed
//反射的成员 cursor
                    Field field= (Field) entry.getValue();
                    Class type=field.getType();
                    if(colmunIndex!=-1)
                    {
//
                        if (type == String.class) {
                            field.set(item, cursor.getString(colmunIndex));
                        }else if(type==Double.class)
                        {
                            field.set(item,cursor.getDouble(colmunIndex));
                        }else  if(type==Integer.class)
                        {
                            field.set(item,cursor.getInt(colmunIndex));
                        }else if(type==Long.class)
                        {
                            field.set(item,cursor.getLong(colmunIndex));
                        }else  if(type==byte[].class)
                        {
                            field.set(item,cursor.getBlob(colmunIndex));
                            /*
                            不支持的类型
                             */
                        }else {
                            continue;
                        }

                    }

                }
                list.add(item);
            } catch ( Exception e) {
                e.printStackTrace();
            }


        }

        return list;
    }
}
