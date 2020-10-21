package com.music.kotlinqq.bean;

import org.litepal.crud.LitePalSupport;



public class SearchHistory extends LitePalSupport {
    String history;
    int id;

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
