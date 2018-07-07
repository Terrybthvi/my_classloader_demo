# 手把手教你实现Android热修复
## 前言
最近一段时间看了一些关于Android热修复的知识，比如Andfix,Tinker,Sophix等，看了这些框架的原理，就想着自己能不能手撸一个简单的demo。下面我们就来自己动手实现Android热修复吧。
## 热修复实现原理
   JAVA虚拟机JVM在运行时，加载的是.classes的字节码文件。而Android也有自己的虚拟机Dalvik/ART虚拟机，不过他们加载的是dex文件，但是他们的工作原理都一样，都是经过ClassLoader类加载器。Android在ClassLoader的基础上又定义类PathClassLoader和DexClassLoader，两者都继承自BaseDexClassLoader，下面我们看下他们间的区别：
   * `BaseDexClassLoader`源代码位于`libcore\dalvik\src\main\java\dalvik\system\BaseDexClassLoader.java`。
   * `DexClassLoader`源代码位于`libcore\dalvik\src\main\java\dalvik\system\DexClassLoader.java`。他主要用来加载系统类和应用类。
   * `PathClassLoader`源代码位于`libcore\dalvik\src\main\Java\dalvik\system\PathClassLoader.java `.用来加载jar、apk、dex文件.加载jar、apk也是最终抽取里面的Dex文件进行加载.
   ![]()

