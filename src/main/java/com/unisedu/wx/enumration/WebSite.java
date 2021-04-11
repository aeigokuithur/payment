package com.unisedu.wx.enumration;

public enum WebSite{

    KPKH("科普科幻", 1, "kpkh"),
    JCXK("基础学科", 2, "jcxk"),
    CXSX("创新数学", 3, "cxsx"),
    CXWL("创新物理", 4, "cxwl"),
    STUDY("研究学习营",5,"study"),
    ACADEMIC("学术研究营",6,"academic");


    private String name;
    private int index;
    private String prefix;

    private WebSite(String name, int index, String prefix){
        this.name = name;
        this.index = index;
        this.prefix = prefix;
    }

    public static WebSite getInstance(String prefix){
        for (WebSite ws : WebSite.values()) {
            if (ws.getPrefix().equals(prefix)) {
                return ws;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
