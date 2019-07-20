package com.example.bthvi.mycloassloaderapplication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Replace {
    //修复哪一个class
    String clazz();
    //修复哪一个方法
    String method();
}
