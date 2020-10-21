package com.music.kotlinqq.event;

/**
 * <pre>
 *     desc   : 收藏歌曲事件
 * </pre>
 */

public class SongCollectionEvent {
    private boolean isLove;
    public SongCollectionEvent(boolean isLove){
        this.isLove = isLove;
    }

    public boolean isLove() {
        return isLove;
    }
}
