package website.xiaoming.CenterSelectedSwipeLibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by PeoceWang on 2016/4/9 0009,10:32.
 */
public class CenterSelectedSwipeLayout extends HorizontalScrollView implements View.OnTouchListener {

    final private Context mContext;

    final private static String UNSELECTED_ICON_TAG = "un_selected_icon";
    final private static String SELECT_ICON_TAG = "selected_icon";
    final private static String CATALOG_TAG = "catalog";

    final private int FIX_MID_ITEM_INDEX = 3;
    final private int FIX_FULL_COLOR = 255;

    private ArrayList<Integer> mUnSelectedIcons;
    private ArrayList<Integer> mSelectedIcons;
    private ArrayList<String> mCatalogs;

    private static int SCREEN_WIDTH;
    private static int SCREEN_HEIGHT;


    private static int VIEW_WIDTH;
    private static int VIEW_HEIGHT;

    private static int ITEM_WIDTH;
    private static int ITEM_HEIGHT;

    private static int sDefaultCenterBgColor = Color.parseColor("#6FB1E1");

    private int mBackGroundColor = Color.parseColor("#ffffff");

    private int[] itemsIndex;

    private int currentOffsetX = 0;

    private int originTouchX = 0;

    private int mVisibleFunctionCount = 5;

    private int mSlopeDistance = 0;

    private float mScaleRatio = 1.2f;

    private boolean isAlreadyMoveOutOfBoundary = false;

    private boolean allowContinueScroll = true;

    private boolean continueScrollFlag = false;

    private Bitmap mBitmap = null;

    private WeakHashMap<Float, SoftReference<Bitmap>> mCache = new WeakHashMap<>();

    private CENTER_BG mBgShape = CENTER_BG.CIRCLE;

    private LinearLayout linearLayout;

    private MotionEvent mMotionEvent;


    public boolean isAllowContinueScroll() {
        return allowContinueScroll;
    }

    public void setAllowContinueScroll(boolean allowContinueScroll) {
        this.allowContinueScroll = allowContinueScroll;
    }

    public int getVisibleFunctionCount() {
        return mVisibleFunctionCount;
    }


    public enum DIRECTION {
        LEFT, RIGHT
    }


    public enum CENTER_BG {
        CIRCLE(sDefaultCenterBgColor), RECTANGLE(sDefaultCenterBgColor), OVAL(sDefaultCenterBgColor);

        public int bgColor;

        public int getBgColor() {
            return bgColor;
        }

        CENTER_BG(int bgColor) {
            this.bgColor = bgColor;
        }
    }

    public void setBgShape(CENTER_BG mBgShape) {
        this.mBgShape = mBgShape;
    }


    public float getScaleRatio() {
        return mScaleRatio;
    }

    public void setScaleRatio(float mScaleRatio) {
        this.mScaleRatio = mScaleRatio;
    }

    /**
     * set idle icons
     *
     * @param mUnSelectedIcons idle icon array
     */
    public void setUnSelectedIcons(ArrayList<Integer> mUnSelectedIcons) {
        this.mUnSelectedIcons = processData(mUnSelectedIcons);
    }

    /**
     * set focus icons
     *
     * @param mSelectedIcons focus icon array
     */
    public void setSelectedIcons(ArrayList<Integer> mSelectedIcons) {
        this.mSelectedIcons = processData(mSelectedIcons);
    }

    /**
     * set catalogs
     *
     * @param catalogs catalogs array
     */
    public void setCatalogs(ArrayList<String> catalogs) {
        this.mCatalogs = processData(catalogs);
    }

    /**
     * set data
     *
     * @param mUnSelectedIcons idle icon array
     * @param mSelectedIcons   focus icon array
     * @param mCatalogs        catalogs array
     */
    public void setData(ArrayList<Integer> mUnSelectedIcons, ArrayList<Integer> mSelectedIcons, ArrayList<String> mCatalogs) {
        if (checkDate(mUnSelectedIcons, mSelectedIcons, mCatalogs)) {
            setUnSelectedIcons(mUnSelectedIcons);
            setSelectedIcons(mSelectedIcons);
            setCatalogs(mCatalogs);
            initData();
            refreshChildView();
        }
    }

    /**
     * add idle icons
     *
     * @param icons idle icons
     */
    public void addUnSelectedIcons(int... icons) {
        for (int icon : icons)
            mUnSelectedIcons.add(icon);
    }

    /**
     * add focus icons
     *
     * @param icons focus icons
     */
    public void addSelectedIcons(int... icons) {
        for (int icon : icons)
            mSelectedIcons.add(icon);
    }

    /**
     * add catalog
     *
     * @param catalogs catalogs
     */
    public void addCatalogs(String... catalogs) {
        Collections.addAll(mCatalogs, catalogs);
    }

    /**
     * add idle icons
     *
     * @param icons idle icons
     */
    public void addUnSelectedIcons(int icons) {
        mUnSelectedIcons.add(icons);
    }

    /**
     * add focus icons
     *
     * @param icons focus icons
     */
    public void addSelectedIcons(int icons) {
        mSelectedIcons.add(icons);
    }

    /**
     * add catalog
     *
     * @param catalogs catalogs
     */
    public void addCatalogs(String catalogs) {
        mCatalogs.add(catalogs);
    }


    private <T extends List<E>, E> T processData(T t) {
        if (t.isEmpty() || t.size() != mVisibleFunctionCount) {
            throw new IllegalArgumentException("Input date number error");
        }
        if (t.get(0) == t.get(t.size() - 2) && t.get(1) == t.get(t.size() - 1)) { //  just in case
            return t;
        }
        E e1 = t.get(0);
        E e2 = t.get(mVisibleFunctionCount - 1);
        t.add(e1);
        t.add(0, e2);

        return t;
    }


    /**
     * set visible function number count, should bigger or equal to 5
     *
     * @param visibleFunctionCount visible number
     */
    public void setVisibleFunctionCount(int visibleFunctionCount) {
        if (visibleFunctionCount >= 5) {
            mVisibleFunctionCount = visibleFunctionCount;
        } else {
            throw new IllegalArgumentException("Date items should more than 4");
        }
    }

    private boolean checkDate() {
        mVisibleFunctionCount = mSelectedIcons.size();
        return (mVisibleFunctionCount == mUnSelectedIcons.size() && (mVisibleFunctionCount == mCatalogs.size())) && (mVisibleFunctionCount != 0);
    }

    private boolean checkDate(ArrayList<Integer> mUnSelectedIcons, ArrayList<Integer> mSelectedIcons, ArrayList<String> mCatalogs) {
        setVisibleFunctionCount(mSelectedIcons.size());
        return (mVisibleFunctionCount == mUnSelectedIcons.size() && (mVisibleFunctionCount == mCatalogs.size())) && (mVisibleFunctionCount != 0);
    }

    public CenterSelectedSwipeLayout(Context context) {
        this(context, null);
    }

    public CenterSelectedSwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public int getBackGroundColor() {
        return mBackGroundColor;
    }

    public void setBackGroundColor(int mBackGroundColor) {
        this.mBackGroundColor = mBackGroundColor;
    }

    public CenterSelectedSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initSwiper();
    }

    private void initSwiper() {
        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        SCREEN_WIDTH = Util.getScreenWidth(mContext);
        SCREEN_HEIGHT = Util.getScreenHeight(mContext);

        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mSlopeDistance = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration) / 2;   // FIX: when finger swipe wandering left and right, then return to near by, exception click error

        if (mUnSelectedIcons == null) {

            mUnSelectedIcons = new ArrayList<>();
        }
        if (mSelectedIcons == null) {
            mSelectedIcons = new ArrayList<>();
        }
        if (mCatalogs == null) {
            mCatalogs = new ArrayList<>();
        }

        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                VIEW_WIDTH = getMeasuredWidth();
                VIEW_HEIGHT = getMeasuredHeight();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        ITEM_WIDTH = SCREEN_WIDTH / mVisibleFunctionCount;
        ITEM_HEIGHT = ITEM_WIDTH;


        setMainView();
        initData();

        currentOffsetX = getScrollX();


        if (checkDate()) {
            refreshChildView();
            post(scrollToCenter);
        }
        setOnTouchListener(this);
    }

    private Bitmap generateBitmap(CENTER_BG centerBg, float scaleRatio) {
        mBitmap = Bitmap.createBitmap(ITEM_WIDTH, ITEM_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setColor(centerBg.getBgColor());
        switch (centerBg) {
            case CIRCLE:
                canvas.drawCircle(ITEM_WIDTH / 2, ITEM_HEIGHT / 2, ITEM_WIDTH / 2 * scaleRatio, paint);
                break;
            case RECTANGLE:
                canvas.drawRect(0, 0, ITEM_WIDTH * scaleRatio, ITEM_HEIGHT * scaleRatio, paint);
                break;
            case OVAL:
                RectF rectF = new RectF(0, 0, ITEM_WIDTH * scaleRatio, ITEM_HEIGHT * scaleRatio);
                canvas.drawOval(rectF, paint);
                break;
        }
        paint.reset();
        final Bitmap cpBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        mCache.put(scaleRatio, new SoftReference<>(cpBitmap));
        return cpBitmap;
    }

    private Bitmap getBgBitmap(CENTER_BG centerBg, float scaleRatio) {

        if (mCache.containsKey(scaleRatio)) {
            SoftReference<Bitmap> reference = mCache.get(scaleRatio);
            Bitmap bitmap = reference.get();
            if (bitmap != null) {
                return bitmap;
            }
            return generateBitmap(centerBg,scaleRatio);
        }
        return generateBitmap(centerBg,scaleRatio);
    }

    private Drawable getBgDrawable() {
        return getBgDrawable(1.0f);
    }

    private Drawable getBgDrawable(float scaleRatio) {
        return new BitmapDrawable(mContext.getResources(), getBgBitmap(mBgShape, scaleRatio));
    }

    private void refreshChildView() {
        final int childCount = linearLayout.getChildCount();
        if (childCount == 0) {
            for (int i : itemsIndex) {
                FrameLayout f = new FrameLayout(mContext);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ITEM_WIDTH, ITEM_HEIGHT);
                lp.gravity = Gravity.CENTER;
                f.setLayoutParams(lp);
                f.setMinimumHeight(ITEM_WIDTH);
                f.setMinimumHeight(ITEM_HEIGHT);
                f.setBackground(getBgDrawable());
                if (i != FIX_MID_ITEM_INDEX) {
                    f.getBackground().setAlpha(0);
                }

                ImageView ivUnSelected = new ImageView(mContext);
                ivUnSelected.setTag(UNSELECTED_ICON_TAG);
                ivUnSelected.setImageResource(mUnSelectedIcons.get(i));
                if (i == FIX_MID_ITEM_INDEX) {
                    ivUnSelected.setVisibility(View.GONE);
                }

                ImageView ivFocused = new ImageView(mContext);
                ivFocused.setTag(SELECT_ICON_TAG);
                ivFocused.setVisibility(View.GONE);
                ivFocused.setImageResource(mSelectedIcons.get(i));
                if (i == FIX_MID_ITEM_INDEX) {
                    ivFocused.setVisibility(View.VISIBLE);
                    ivFocused.setScaleX(mScaleRatio);
                    ivFocused.setScaleY(mScaleRatio);
                }

                TextView tv = new TextView(mContext);
                tv.setTag(CATALOG_TAG);
                tv.setText(mCatalogs.get(i));

                if (i == FIX_MID_ITEM_INDEX) {
                    tv.setTextColor(Color.rgb(FIX_FULL_COLOR, FIX_FULL_COLOR, FIX_FULL_COLOR));
                } else {
                    tv.setTextColor(Color.BLACK);
                }

                FrameLayout.LayoutParams lp1 =
                        new FrameLayout.LayoutParams(ITEM_WIDTH / 3, ITEM_WIDTH / 3);
                FrameLayout.LayoutParams lp2 =
                        new FrameLayout.LayoutParams(ITEM_WIDTH / 2, ITEM_WIDTH / 2);
                FrameLayout.LayoutParams lp3 =
                        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp1.gravity = Gravity.CENTER;
                lp1.bottomMargin = ITEM_WIDTH / 6 - Util.dp2px(mContext, 5);
                f.addView(ivUnSelected, lp1);

                lp2.gravity = Gravity.CENTER;
                lp2.bottomMargin = ITEM_WIDTH / 6 - Util.dp2px(mContext, 5);
                f.addView(ivFocused, lp2);

                lp3.gravity = Gravity.CENTER;
                lp3.topMargin = ITEM_WIDTH / 4 + Util.dp2px(mContext, 3);
                f.addView(tv, lp3);

                linearLayout.addView(f);
            }
        } else {
            for (int i = 0; i < childCount; i++) {
                FrameLayout f = (FrameLayout) linearLayout.getChildAt(i);
                ImageView ivUnSelected = (ImageView) f.findViewWithTag(UNSELECTED_ICON_TAG);
                ImageView ivFocused = (ImageView) f.findViewWithTag(SELECT_ICON_TAG);
                TextView tv = (TextView) f.findViewWithTag(CATALOG_TAG);
                ivUnSelected.setImageResource(mUnSelectedIcons.get(itemsIndex[i]));
                ivUnSelected.setVisibility(View.VISIBLE);
                ivFocused.setImageResource(mSelectedIcons.get(itemsIndex[i]));
                ivFocused.setVisibility(View.GONE);
                ivUnSelected.setAlpha(1f);
                ivFocused.setAlpha(1f);
                if (i == FIX_MID_ITEM_INDEX) {
                    ivUnSelected.setVisibility(View.GONE);
                    ivFocused.setVisibility(View.VISIBLE);
                    ivFocused.setScaleX(mScaleRatio);
                    ivFocused.setScaleY(mScaleRatio);
                    tv.setTextColor(Color.rgb(FIX_FULL_COLOR, FIX_FULL_COLOR, FIX_FULL_COLOR));
                    f.setBackground(getBgDrawable());
                    f.getBackground().setAlpha(FIX_FULL_COLOR);
                } else {
                    tv.setTextColor(Color.rgb(0, 0, 0));
                    f.getBackground().setAlpha(0);
                }
                tv.setText(mCatalogs.get(itemsIndex[i]));
            }
        }
        post(scrollToCenter);
    }


    private void animation(float diff) {
        float fadeRation = diff * 1.6f;
        float showInRation = diff * 1.2f;

        FrameLayout mid = (FrameLayout) linearLayout.getChildAt(FIX_MID_ITEM_INDEX);
        ImageView midIV1 = (ImageView) mid.findViewWithTag(UNSELECTED_ICON_TAG);
        ImageView midIV2 = (ImageView) mid.findViewWithTag(SELECT_ICON_TAG);
        TextView midTV = (TextView) mid.findViewWithTag(CATALOG_TAG);
        if (diff > 0) {             // right swipe animation
            FrameLayout left = (FrameLayout) linearLayout.getChildAt(FIX_MID_ITEM_INDEX - 1);
            ImageView leftIV1 = (ImageView) left.findViewWithTag(UNSELECTED_ICON_TAG);
            ImageView leftIV2 = (ImageView) left.findViewWithTag(SELECT_ICON_TAG);
            TextView leftTV = (TextView) left.findViewWithTag(CATALOG_TAG);

            midIV1.setVisibility(View.VISIBLE);
            midIV2.setVisibility(View.VISIBLE);
            leftIV1.setVisibility(View.VISIBLE);
            leftIV2.setVisibility(View.VISIBLE);


            float scaleLargeRation = 1 + diff * (mScaleRatio - 1);
            float scaleSmallRation = mScaleRatio - diff * (mScaleRatio - 1);

            midIV1.setAlpha(showInRation);
            midIV2.setAlpha(1 - fadeRation);
            midIV2.setScaleX(scaleSmallRation);
            midIV2.setScaleY(scaleSmallRation);

            int w_2 = ITEM_WIDTH / 2;
            mid.getBackground().setAlpha(FIX_FULL_COLOR);
            left.getBackground().setAlpha(FIX_FULL_COLOR);
            float t_diff = diff * 1.2f; // scale down should quickly than scale up

            left.setBackground(getBgDrawable(diff));        // scale up
            mid.setBackground(getBgDrawable((1 - t_diff)));   // scale down


            leftIV1.setAlpha(1 - fadeRation);
            leftIV2.setAlpha(showInRation);
            leftIV2.setScaleX(scaleLargeRation);
            leftIV2.setScaleY(scaleLargeRation);

            leftTV.setTextColor(Color.rgb((int) (FIX_FULL_COLOR * diff), (int) (FIX_FULL_COLOR * diff), (int) (FIX_FULL_COLOR * diff)));
            midTV.setTextColor(Color.rgb((int) (FIX_FULL_COLOR * (1 - diff)), (int) (FIX_FULL_COLOR * (1 - diff)), (int) (FIX_FULL_COLOR * (1 - diff))));

        } else {                      // left swipe animation

            FrameLayout right = (FrameLayout) linearLayout.getChildAt(FIX_MID_ITEM_INDEX + 1);
            ImageView rightIV1 = (ImageView) right.findViewWithTag(UNSELECTED_ICON_TAG);
            ImageView rightIV2 = (ImageView) right.findViewWithTag(SELECT_ICON_TAG);
            TextView rightTV = (TextView) right.findViewWithTag(CATALOG_TAG);

            int w_2 = ITEM_WIDTH / 2;
            mid.getBackground().setAlpha(FIX_FULL_COLOR);
            right.getBackground().setAlpha(FIX_FULL_COLOR);
            float t_diff = diff * 1.2f; //  scale down should quickly than scale up

            right.setBackground(getBgDrawable(-diff));       // scale up
            mid.setBackground(getBgDrawable((1 + t_diff)));   // scale down

            midIV1.setVisibility(View.VISIBLE);
            midIV2.setVisibility(View.VISIBLE);
            rightIV1.setVisibility(View.VISIBLE);
            rightIV2.setVisibility(View.VISIBLE);

            float scaleLargeRation = 1 - diff * (mScaleRatio - 1);
            float scaleSmallRation = mScaleRatio + diff * (mScaleRatio - 1);

            midIV1.setAlpha(-showInRation);
            midIV2.setAlpha(1 + fadeRation);
            midIV2.setScaleX(scaleSmallRation);
            midIV2.setScaleY(scaleSmallRation);

            rightIV1.setAlpha(1 + fadeRation);
            rightIV2.setAlpha(-showInRation);
            rightIV2.setScaleX(scaleLargeRation);
            rightIV2.setScaleY(scaleLargeRation);

            rightTV.setTextColor(Color.rgb((int) (FIX_FULL_COLOR * -diff), (int) (FIX_FULL_COLOR * -diff), (int) (FIX_FULL_COLOR * -diff)));
            midTV.setTextColor(Color.rgb((int) (FIX_FULL_COLOR * (1 + diff)), (int) (FIX_FULL_COLOR * (1 + diff)), (int) (FIX_FULL_COLOR * (1 + diff))));

        }

    }

    private void initData() {
        itemsIndex = new int[mVisibleFunctionCount + 2];
        for (int i = 1; i <= mVisibleFunctionCount; i++) {
            itemsIndex[i] = i;
        }
        itemsIndex[0] = itemsIndex[mVisibleFunctionCount];
        itemsIndex[mVisibleFunctionCount + 1] = itemsIndex[1];
    }

    private void setMainView() {
        ViewGroup.LayoutParams layoutParamsH = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParamsH);

        setHorizontalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);

        linearLayout = new LinearLayout(mContext);
        ViewGroup.LayoutParams layoutParamsV = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Util.dp2px(mContext, 100));
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setBackgroundColor(mBackGroundColor);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        addView(linearLayout, layoutParamsV);
    }


    private OnHorizontalScrollListener mOnHorizontalScrollListener = null;

    public void setOnHorizontalScrollListener(OnHorizontalScrollListener mOnHorizontalScrollListener) {
        this.mOnHorizontalScrollListener = mOnHorizontalScrollListener;
    }

    protected onItemChangeListener mOnItemChangeListener = null;

    public void setOnItemChangeListener(onItemChangeListener mOnItemChangeListener) {
        this.mOnItemChangeListener = mOnItemChangeListener;
    }

    protected onSwipeChangeListener mOnSwipeChangeListener = null;

    public void setOnSwipeChangeListener(onSwipeChangeListener mOnSwipeChangeListener) {
        this.mOnSwipeChangeListener = mOnSwipeChangeListener;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        VIEW_HEIGHT = r - l;
        VIEW_WIDTH = b - t;
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (mOnHorizontalScrollListener != null) {
            mOnHorizontalScrollListener.onHorizontalScroll(l - oldl, l);
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

    private final Runnable scrollToCenter = new Runnable() {
        @Override
        public void run() {
            scrollTo(ITEM_WIDTH, 0);
        }
    };

    /**
     * scroll to next item without animation
     */
    final public void next() {
        scrollBy(-ITEM_WIDTH, 0);
        resortArray(DIRECTION.LEFT);
        scrollTo(ITEM_WIDTH, 0);
        refreshChildView();
        if (mOnItemChangeListener != null) {
            mOnItemChangeListener.onItemChange(DIRECTION.LEFT, false);
        }
    }

    /**
     * scroll to previous item without animation
     */
    final public void previous() {
        scrollBy(ITEM_WIDTH, 0);
        resortArray(DIRECTION.RIGHT);
        scrollTo(ITEM_WIDTH, 0);
        refreshChildView();
        if (mOnItemChangeListener != null) {
            mOnItemChangeListener.onItemChange(DIRECTION.RIGHT, false);
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                originTouchX = x;
                currentOffsetX = getScrollX();
                isAlreadyMoveOutOfBoundary = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int diff;
                if (allowContinueScroll) {
                    diff = (originTouchX - x) / 4 * 3;  // If allow continue scroll we should increase move scale (finger move distance)/(view actually move distance)
                } else {
                    diff = (originTouchX - x) / 3 * 2;
                }
                final int nowOffset = getScrollX();
                final int offset = currentOffsetX - nowOffset;
                originTouchX = x;
                isAlreadyMoveOutOfBoundary = isAlreadyMoveOutOfBoundary || checkMove(mMotionEvent.getX(), mMotionEvent.getY(), event.getX(), event.getY());
                if (abs(offset) < ITEM_WIDTH) {
                    scrollBy(diff, 0);
                    animation((float) (offset) / ITEM_WIDTH);
                } else if (allowContinueScroll && !continueScrollFlag) {    // allow continue move
                    MotionEvent UpEvent = MotionEvent.obtain(event);
                    UpEvent.setAction(MotionEvent.ACTION_UP);
                    continueScrollFlag = true;
                    onTouch(v, UpEvent);
                }
                break;
            case MotionEvent.ACTION_UP:
                final int upOffset = getScrollX();
                final int upDiff = upOffset - currentOffsetX;
                if (!checkMove(mMotionEvent.getX(), mMotionEvent.getY(), event.getX(), event.getY()) && !isAlreadyMoveOutOfBoundary) {
                    processClick(event);
                } else if (abs(upDiff) < ITEM_WIDTH / 3) {
                    smoothScrollBy(-upDiff, 0);
                } else {
                    if (upDiff > 0) {
                        scrollBy(abs(upDiff) < ITEM_WIDTH ? (ITEM_WIDTH - upDiff) : ITEM_WIDTH, 0);
                        resortArray(DIRECTION.LEFT);
                        if (mOnItemChangeListener != null) {
                            mOnItemChangeListener.onItemChange(DIRECTION.LEFT, true);
                        }
                    } else {
                        scrollBy(abs(upDiff) < ITEM_WIDTH ? (-ITEM_WIDTH - upDiff) : (-ITEM_WIDTH), 0);
                        resortArray(DIRECTION.RIGHT);
                        if (mOnItemChangeListener != null) {
                            mOnItemChangeListener.onItemChange(DIRECTION.RIGHT, true);
                        }
                    }
                    scrollTo(ITEM_WIDTH, 0);
                }
                if (mOnSwipeChangeListener != null && !continueScrollFlag) {
                    mOnSwipeChangeListener.onItemChange(upDiff > 0 ? DIRECTION.LEFT : DIRECTION.RIGHT, (itemsIndex[FIX_MID_ITEM_INDEX] - 1));
                }
                refreshChildView();
                originTouchX = 0;
                if (allowContinueScroll && continueScrollFlag) {
                    MotionEvent UpEvent = MotionEvent.obtain(event);
                    UpEvent.setAction(MotionEvent.ACTION_DOWN);
                    continueScrollFlag = false;
                    onTouch(v, UpEvent);
                    return true;
                }
                isAlreadyMoveOutOfBoundary = false;
                break;
        }
        return true;
    }

    private void resortArray(DIRECTION direction) {
        int[] tmpIndex = new int[mVisibleFunctionCount + 2];
        for (int i = 1; i <= mVisibleFunctionCount; i++) {
            tmpIndex[i] = itemsIndex[(i + (direction == DIRECTION.LEFT ? 1 : -1)
                    + mVisibleFunctionCount) % mVisibleFunctionCount];
        }
        tmpIndex[0] = tmpIndex[mVisibleFunctionCount];
        tmpIndex[mVisibleFunctionCount + 1] = tmpIndex[1];
        System.arraycopy(tmpIndex, 0, itemsIndex, 0, tmpIndex.length);
    }

    private void resortArray(int index) {
        int[] tmpIndex = new int[mVisibleFunctionCount + 2];
        for (int i = 1; i <= mVisibleFunctionCount; i++) {
            tmpIndex[i] = itemsIndex[(i + (index - (FIX_MID_ITEM_INDEX - 1))
                    + mVisibleFunctionCount) % mVisibleFunctionCount];
        }
        tmpIndex[0] = tmpIndex[mVisibleFunctionCount];
        tmpIndex[mVisibleFunctionCount + 1] = tmpIndex[1];
        System.arraycopy(tmpIndex, 0, itemsIndex, 0, tmpIndex.length);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        VIEW_WIDTH = w;
        VIEW_HEIGHT = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    // In order to obtain click event, we should check touch event moved distance
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                continueScrollFlag = false;
                isAlreadyMoveOutOfBoundary = false;
                mMotionEvent = MotionEvent.obtain(ev);
                break;
            default:
                mMotionEvent = null;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean checkMove(float x, float y, float oldX, float oldy) {
        return sqrt(pow(abs(x - oldX), 2) + pow(abs(y - oldy), 2)) > mSlopeDistance;
    }


    private void processClick(MotionEvent ev) {
        jumpToIndex((int) ev.getX() / ITEM_WIDTH);
    }


    /**
     * Jump to specifics item
     *
     * @param index ranges from [0, {@link #mVisibleFunctionCount}]
     */
    final public void jumpToIndex(int index) {
        if (index != FIX_MID_ITEM_INDEX - 1) {
            if (mOnItemChangeListener != null) {
                mOnItemChangeListener.onItemClick(itemsIndex[index + 1] - 1);
            }
            resortArray(index);
        }
    }

    /**
     * This function should under viewpager onPageScrolled listener and put positionOffset and positionOffsetPixels into param
     *
     * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    final public void setScroll(int positionOffset, int positionOffsetPixels) {
        positionOffset = positionOffsetPixels > 0 ? positionOffset : -positionOffset;
        scrollBy(positionOffset * ITEM_WIDTH, 0);
        resortArray(positionOffset > 0 ? DIRECTION.LEFT : DIRECTION.RIGHT);
        refreshChildView();
    }


    public interface OnHorizontalScrollListener {
        /**
         * Listener to get how many pixel are scrolled
         *
         * @param scrollDistanceX        The distance moved by X  since finger are down
         * @param currentScrollPositionX total scroll distance of this view
         */
        void onHorizontalScroll(int scrollDistanceX, int currentScrollPositionX);
    }

    public interface onItemChangeListener {
        /**
         * Listener to identify which direction user swipe. Use {@link onSwipeChangeListener} instead.
         * Case it can not detected whether finger is released when callback this function
         *
         * @param direction        swipe direction
         * @param performedBySwipe whether performed by user finger swipe
         */
        @Deprecated
        void onItemChange(CenterSelectedSwipeLayout.DIRECTION direction, boolean performedBySwipe);

        /**
         * Listen finger click on item
         *
         * @param index the item finger clicked
         */
        void onItemClick(int index);
    }

    public interface onSwipeChangeListener {
        /**
         * Listener to identify when user stop scroll (include once scroll and continue scroll) , which item are selected.
         *
         * @param direction swipe direction
         * @param index     the index selected when finger up screen
         */
        void onItemChange(CenterSelectedSwipeLayout.DIRECTION direction, int index);
    }


    /**
     * Simple implementation of the {@link onItemChangeListener} interface with stub
     * implementations of each method. Extend this if you do not intend to override
     * every method of {@link onItemChangeListener}.
     */
    public static class SimpleOnItemChangeListener implements onItemChangeListener {

        @Override
        public void onItemChange(DIRECTION direction, boolean performedBySwipe) {

        }

        @Override
        public void onItemClick(int index) {

        }
    }
}
