package website.xiaoming.CenterSelectedSwipeLibrary;

/**
 * Created by wangxiaoming on 2016/4/9 0009,10:34.
 */
public interface OnHorizontalScrollListener {
    /**
     * Listener to get how many pixel are scrolled
     * @param scrollDistanceX The distance moved by X  since finger are down
     * @param currentScrollPositionX total scroll distance of this view
     */
    void onHorizontalScroll(int scrollDistanceX, int currentScrollPositionX);
}
