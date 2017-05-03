package com.example.myplugin;

public class ApkItem {
    private String name;
    private boolean installed;

    public ApkItem(String name, boolean installed) {
        this.name = name;
        this.installed = installed;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
}
