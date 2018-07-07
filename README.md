# 手把手教你实现Android热修复
## 前言
最近一段时间看了一些关于Android热修复的知识，比如Andfix,Tinker,Sophix等，看了这些框架的原理，就想着自己能不能手撸一个简单的demo。下面我们就来自己动手实现Android热修复吧。
## 热修复实现原理
   所谓热修复就是，在我们应用上线后出现小bug需要及时修复时，不用再发新的安装包，只需要发布补丁包，在客户不知不觉之间修复掉bug，JAVA虚拟机JVM在运行时，加载的是.classes的字节码文件。而Android也有自己的虚拟机Dalvik/ART虚拟机，不过他们加载的是dex文件，但是他们的工作原理都一样，都是经过ClassLoader类加载器。Android在ClassLoader的基础上又定义类PathClassLoader和DexClassLoader，两者都继承自BaseDexClassLoader，下面我们看下他们间的区别：
   * `BaseDexClassLoader`源代码位于`libcore\dalvik\src\main\java\dalvik\system\BaseDexClassLoader.java`。
   * `PathClassLoader`源代码位于`libcore\dalvik\src\main\Java\dalvik\system\PathClassLoader.java `。他主要用来加载系统类和应用类。
   * `DexClassLoader`源代码位于`libcore\dalvik\src\main\java\dalvik\system\DexClassLoader.java`。用来加载jar、apk、dex文件.加载jar、apk也是最终抽取里面的Dex文件进行加载.
   ![DexClassLoader](https://github.com/Terrybthvi/my_classloader_demo/blob/master/Image/20160630102448728.png)

## 手写Android热修复框架
下面我们一步一步来实现Android热修复。
### 写一个专门带有bug的类
既然要测试热修复，我们肯定要写一个带有bug的类。
```
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
    }
}
```
下面我们要写一个热修复的核心工具类。
### 热修复核心类
```
package com.example.bthvi.mycloassloaderapplication;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class FixDexUtil {

    private static final String DEX_SUFFIX = ".dex";
    private static final String APK_SUFFIX = ".apk";
    private static final String JAR_SUFFIX = ".jar";
    private static final String ZIP_SUFFIX = ".zip";
    public static final String DEX_DIR = "odex";
    private static final String OPTIMIZE_DEX_DIR = "optimize_dex";
    private static HashSet<File> loadedDex = new HashSet<>();

    static {
        loadedDex.clear();
    }

    /**
     * 加载补丁，使用默认目录：data/data/包名/files/odex
     *
     * @param context
     */
    public static void loadFixedDex(Context context) {
        loadFixedDex(context, null);
    }

    /**
     * 加载补丁
     *
     * @param context       上下文
     * @param patchFilesDir 补丁所在目录
     */
    public static void loadFixedDex(Context context, File patchFilesDir) {
        // dex合并之前的dex
        doDexInject(context, loadedDex);
    }

    /**
     *@author bthvi
     *@time 2018/6/25 0025 15:51
     *@desc 验证是否需要热修复
     */
    public static boolean isGoingToFix(@NonNull Context context) {
        boolean canFix = false;
        File externalStorageDirectory = Environment.getExternalStorageDirectory();

        // 遍历所有的修复dex , 因为可能是多个dex修复包
        File fileDir = externalStorageDirectory != null ?
                new File(externalStorageDirectory,"007"):
                new File(context.getFilesDir(), DEX_DIR);// data/data/包名/files/odex（这个可以任意位置）

        File[] listFiles = fileDir.listFiles();
        if (listFiles != null){
            for (File file : listFiles) {
                if (file.getName().startsWith("classes") &&
                        (file.getName().endsWith(DEX_SUFFIX)
                                || file.getName().endsWith(APK_SUFFIX)
                                || file.getName().endsWith(JAR_SUFFIX)
                                || file.getName().endsWith(ZIP_SUFFIX))) {

                    loadedDex.add(file);// 存入集合
                    //有目标dex文件, 需要修复
                    canFix = true;
                }
            }
        }
        return canFix;
    }

    private static void doDexInject(Context appContext, HashSet<File> loadedDex) {
        String optimizeDir = appContext.getFilesDir().getAbsolutePath() +
                File.separator + OPTIMIZE_DEX_DIR;
        // data/data/包名/files/optimize_dex（这个必须是自己程序下的目录）

        File fopt = new File(optimizeDir);
        if (!fopt.exists()) {
            fopt.mkdirs();
        }
        try {
            // 1.加载应用程序dex的Loader
            PathClassLoader pathLoader = (PathClassLoader) appContext.getClassLoader();
            for (File dex : loadedDex) {
                // 2.加载指定的修复的dex文件的Loader
                DexClassLoader dexLoader = new DexClassLoader(
                        dex.getAbsolutePath(),// 修复好的dex（补丁）所在目录
                        fopt.getAbsolutePath(),// 存放dex的解压目录（用于jar、zip、apk格式的补丁）
                        null,// 加载dex时需要的库
                        pathLoader// 父类加载器
                );
                // 3.开始合并
                // 合并的目标是Element[],重新赋值它的值即可

                /**
                 * BaseDexClassLoader中有 变量: DexPathList pathList
                 * DexPathList中有 变量 Element[] dexElements
                 * 依次反射即可
                 */

                //3.1 准备好pathList的引用
                Object dexPathList = getPathList(dexLoader);
                Object pathPathList = getPathList(pathLoader);
                //3.2 从pathList中反射出element集合
                Object leftDexElements = getDexElements(dexPathList);
                Object rightDexElements = getDexElements(pathPathList);
                //3.3 合并两个dex数组
                Object dexElements = combineArray(leftDexElements, rightDexElements);

                // 重写给PathList里面的Element[] dexElements;赋值
                Object pathList = getPathList(pathLoader);// 一定要重新获取，不要用pathPathList，会报错
                setField(pathList, pathList.getClass(), "dexElements", dexElements);
            }
            Toast.makeText(appContext, "修复完成", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 反射给对象中的属性重新赋值
     */
    private static void setField(Object obj, Class<?> cl, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cl.getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

    /**
     * 反射得到对象中的属性值
     */
    private static Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }


    /**
     * 反射得到类加载器中的pathList对象
     */
    private static Object getPathList(Object baseDexClassLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 反射得到pathList中的dexElements
     */
    private static Object getDexElements(Object pathList) throws NoSuchFieldException, IllegalAccessException {
        return getField(pathList, pathList.getClass(), "dexElements");
    }

    /**
     * 数组合并
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> clazz = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);// 得到左数组长度（补丁数组）
        int j = Array.getLength(arrayRhs);// 得到原dex数组长度
        int k = i + j;// 得到总数组长度（补丁数组+原dex数组）
        Object result = Array.newInstance(clazz, k);// 创建一个类型为clazz，长度为k的新数组
        System.arraycopy(arrayLhs, 0, result, 0, i);
        System.arraycopy(arrayRhs, 0, result, i, j);
        return result;
    }
}

```
我们这里暂且指定热修复目录`007`，下面我们看一下如何调用。
### Splash页面调用检查热修复
```
    private void init() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();

    // 遍历所有的修复dex , 因为可能是多个dex修复包
    File fileDir = externalStorageDirectory != null ?
            new File(externalStorageDirectory,"007"):
            new File(getFilesDir(), FixDexUtil.DEX_DIR);// data/user/0/包名/files/odex（这个可以任意位置）
        if (!fileDir.exists()){
        fileDir.mkdirs();
    }
        if (FixDexUtil.isGoingToFix(this)) {

        FixDexUtil.loadFixedDex(this, Environment.getExternalStorageDirectory());
        textView.setText("正在修复。。。。");

    }
     new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this,MainActivity.class));
            finish();
        }
    },3000);

}
```
下面我们先来看下有bug时的APP。
![bugAPP](https://github.com/Terrybthvi/my_classloader_demo/blob/master/Image/Screenshot_2018-07-07-14-05-58-285_com.example.bt.png)

### 在出bug的对应类修复bug
```
package com.example.bthvi.mycloassloaderapplication.xxx;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 * bug测试类
 */
public class BugClass {

    public BugClass(Context context){
        Toast.makeText(context,"你很优秀！bug已修复😯",Toast.LENGTH_SHORT).show();
    }
}
```
修改好bug之后我们需要打出补丁包。
### 打出热修复补丁包
#### 在AndroidStudio里面关闭掉Instant_Run
由于Android Studio的instan run的原理也是热修复，所以安装的时候不会安装完整的安装包，只会安装新改变的代码。
![tupian](https://github.com/Terrybthvi/my_classloader_demo/blob/master/Image/A84080D5-844F-4B00-8217-CA621A3F8E83.png)
#### 重新编译并拷贝出新修改的类
首先点击`Build->RebuildProject`来重新构建,构建完成之后,可以在`app/build/interintermediate/debug/包名/`找到你刚刚修改的class文件,将他拷贝出来,要连同包名路径一起拷贝出来。
![tupian2](https://github.com/Terrybthvi/my_classloader_demo/blob/master/Image/06984F32-6B81-47D3-8D4E-5FA2D7077189.png)
#### 将class文件打包成dex文件
我们前面知道热修复的原理是Dalvik/ART加载dex文件，所以接下来我们要将class文件打包成dex文件，首先我们找到AndroidSDK的build-tools 目录下，在控制台下进入该目录下的任意一个版本，执行dx命令，关于dx命令的使用帮助可以使用`dx -- help`,下面们通过 `dx --dex [指定输出路径]/classes.dex  [刚才拷贝的修复bug的类及包名的目录]`这样我们就得到了.dex文件。
#### 将打出来的dex文件放至我们指定的目录下
我们将打出来的dex文件放在我们指定的目录`007`下，当然这个目录也可以是包名。
![tupian](https://github.com/Terrybthvi/my_classloader_demo/blob/master/Image/Screenshot_2018-07-07-14-03-36-273_com.android.fi.png)
#### 重新启动有bug的APP
我们启动就会后发现bug已经修复了
![tupian](https://github.com/Terrybthvi/my_classloader_demo/blob/master/Image/Screenshot_2018-07-07-14-13-31-572_com.example.bt.png)

