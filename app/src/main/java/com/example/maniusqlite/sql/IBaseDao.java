package com.example.maniusqlite.sql;

import java.util.List;

public interface IBaseDao<T> {
    //    zeng
    Long insert(T entity);

    int update(T entity, T where);
    /**
     * 删除数据
     * @param where
     * @return
     */
    int  delete(T where);


    /**
     * 查询数据
     */
    List<T> query(T where);

    public List<T> query(T where, String groupBy, String orderBy, String having, Integer startIndex,
                         Integer limit);
}
