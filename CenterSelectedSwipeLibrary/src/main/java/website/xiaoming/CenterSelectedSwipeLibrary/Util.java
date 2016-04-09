package website.xiaoming.CenterSelectedSwipeLibrary;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

/**
 * Created by wangxiaoming on 2016/4/8 0008,10:51.
 */
public class Util {
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point.x;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point.y;
    }
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2xp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
}
