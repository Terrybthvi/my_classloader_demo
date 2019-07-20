package com.example.bthvi.mycloassloaderapplication;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import dalvik.system.DexFile;
/**
 *@author bthvi
 *@time 2019/7/20
 *@desc 不用启动APP实现热修复
 */
public class FixDexManager {
    private final static String TAG = "FixDexUtil";
    private static final String DEX_SUFFIX = ".dex";
    private static final String APK_SUFFIX = ".apk";
    private static final String JAR_SUFFIX = ".jar";
    private static final String ZIP_SUFFIX = ".zip";
    public static final String DEX_DIR = "odex";
    private static final String OPTIMIZE_DEX_DIR = "optimize_dex";
    private Context context;

    public FixDexManager(Context context) {
        this.context = context;
    }

    public void isGoingToFix() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();

        // 遍历所有的修复dex , 因为可能是多个dex修复包
        File fileDir = externalStorageDirectory != null ?
                new File(externalStorageDirectory,"007"):
                new File(context.getFilesDir(), DEX_DIR);// data/data/包名/files/odex（这个可以任意位置）

        File[] listFiles = fileDir.listFiles();
        if (listFiles != null){
            System.out.println("TAG==目录下文件数量="+listFiles.length);
            for (File file : listFiles) {
                System.out.println("TAG==文件名称="+file.getName());
                if (file.getName().startsWith("classes") &&
                        (file.getName().endsWith(DEX_SUFFIX))) {
                    loadDex(file);// 开始修复
                    //有目标dex文件, 需要修复
                }
            }
        }
    }
    /**
     * 加载Dex文件
     * @param file
     */
    public void loadDex(File file) {
        try {
            DexFile dexFile = DexFile.loadDex(file.getAbsolutePath(),
                    new File(context.getCacheDir(), "opt").getAbsolutePath(), Context.MODE_PRIVATE);
            //当前的dex里面的class 类名集合
            Enumeration<String> entry=dexFile.entries();
            while (entry.hasMoreElements()) {
                //拿到Class类名
                String clazzName= entry.nextElement();
                //通过加载得到类  这里不能通过反射，因为当前的dex没有加载到虚拟机内存中
                Class realClazz= dexFile.loadClass(clazzName, context.getClassLoader());
                if (realClazz != null) {
                    fixClazz(realClazz);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 修复有bug的方法
     * @param realClazz
     */
    private void fixClazz(Class realClazz) {
        //得到类中所有方法
        Method[] methods=realClazz.getMethods();
        //遍历方法 通过注解 得到需要修复的方法
        for (Method rightMethod : methods) {
            //拿到注解
            Replace replace = rightMethod.getAnnotation(Replace.class);
            if (replace == null) {
                continue;
            }
            //得到类名
            String clazzName=replace.clazz();
            //得到方法名
            String methodName=replace.method();
            try {
                //反射得到本地的有bug的方法的类
                Class wrongClazz=  Class.forName(clazzName);
                //得到有bug的方法（注意修复包中的方法参数名和参数列表必须一致）
                Method wrongMethod = wrongClazz.getDeclaredMethod(methodName, rightMethod.getParameterTypes());
                //调用native方法替换有bug的方法
                replace(wrongMethod, rightMethod);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }

    public native static void replace(Method wrongMethod, Method rightMethod) ;
}
