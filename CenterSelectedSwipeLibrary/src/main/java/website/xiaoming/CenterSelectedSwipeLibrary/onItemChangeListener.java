package website.xiaoming.CenterSelectedSwipeLibrary;

/**
 * Created by wangxiaoming on 2016/4/9 0009,17:22.
 */
public interface onItemChangeListener {
    /**
     *  Listener to identify which direction user swipe
     * @param direction swipe direction
     * @param performedBySwip whether performed by user finger swipe
     */
    void onItemChange(CenterSelectedSwipeLayout.DIRECTION direction, boolean performedBySwip);
}
