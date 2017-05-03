package com.yyh.autoplugin.hook;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * Created by Administrator on 2017/3/20.
 */
public class HostClassLoaderHookHelper {

    public static void patchClassLoader(ClassLoader classLoader, File dexFile,
                                        File optDexFile) throws Exception {

        Field pathListField = DexClassLoader.class.getSuperclass().getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathListObj = pathListField.get(classLoader);

        //获取PathList:Element[] dexElements
        Field dexElementArray = pathListObj.getClass().getDeclaredField("dexElements");
        dexElementArray.setAccessible(true);
        Object[] dexElements = (Object[]) dexElementArray.get(pathListObj);

        Class<?> elementClass = dexElements.getClass().getComponentType();

        //创建一个数组，用来替换原始数组
        Object[] newElements= (Object[]) Array.newInstance(elementClass, dexElements.length+1);

        // 构造插件Element(File file, boolean isDirectory, File zip, DexFile dexFile) 这个构造函数
        Constructor<?> constructor = elementClass.getConstructor(File.class,boolean.class,File.class,DexFile.class);
        Object o = constructor.newInstance(dexFile,false,dexFile,DexFile.loadDex(dexFile.getCanonicalPath(), optDexFile.getCanonicalPath(), 0));

        Object[] toAddElementArray = new Object[]{o};
        //把原始的elements复制进去。
        System.arraycopy(dexElements, 0, newElements, 0, dexElements.length);

        System.arraycopy(toAddElementArray, 0, newElements, dexElements.length, toAddElementArray.length);
        dexElementArray.set(pathListObj, newElements);


    }

}
