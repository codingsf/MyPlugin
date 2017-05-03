package com.yyh.autoplugin.hook;

import java.io.File;

import com.yyh.autoplugin.ApApplication;
import com.yyh.autoplugin.utils.ApkUtils;

import dalvik.system.DexClassLoader;

/**
 * 自定义的ClassLoader,
 * @author weishu
 * @date 16/3/29
 */
public class PluginClassLoader extends DexClassLoader {

    public PluginClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }
    
    /**
     * 获取插件的类加载器。
     * @param apkFile
     * @param packageName
     * @return
     */
    public static ClassLoader getPluginClassLoader(File apkFile,
			String packageName) {
		return new PluginClassLoader(apkFile.getPath(), ApkUtils.getPluginOptDexDir(packageName).getPath(), ApkUtils.getPluginLibDir(packageName).getPath(), ApApplication.getContext().getClassLoader());
	}
}
