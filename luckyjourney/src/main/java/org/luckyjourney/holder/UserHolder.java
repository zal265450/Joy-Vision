package org.luckyjourney.holder;

public class UserHolder {
    private static ThreadLocal<Integer> userThreadLocal = new ThreadLocal<>();

    // 添加
    public static void set(Object id){
        userThreadLocal.set(Integer.valueOf(id.toString()));
    }
    // 获取
    public static Integer get(){
        return userThreadLocal.get();
    }
    // 删除
    public static void clear(){
        userThreadLocal.remove();
    }
}