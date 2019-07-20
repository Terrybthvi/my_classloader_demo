package com.example.bthvi.mycloassloaderapplication.xxx;

import com.example.bthvi.mycloassloaderapplication.Replace;

/**
 * bug测试类
 */
public class BugClass {

    @Replace(clazz = "com.example.bthvi.mycloassloaderapplication.xxx.BugClass",method = "test")
    public int test(){
//        //测试bug
        throw new RuntimeException("这是一个异常！");
//        return 10;
    }
}

