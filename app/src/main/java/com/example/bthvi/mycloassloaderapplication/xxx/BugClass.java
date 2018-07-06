package com.example.bthvi.mycloassloaderapplication.xxx;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 * bug测试类
 */
public class BugClass {

    public BugClass(Context context){
        Toast.makeText(context,"这是一个优美的bug！",Toast.LENGTH_SHORT).show();
//        Toast.makeText(context,"你很优秀！bug已修复😯",Toast.LENGTH_SHORT).show();
    }
}
