package github.com.smartadapter.itemDecoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import github.com.smartadapter.SmartAdapter;


/**
 * 垂直方向的分割线
 */
public class VItemDecoration extends RecyclerView.ItemDecoration {

    /**
     * item 宽度
     */
    private final Rect mBounds = new Rect();
    /**
     * 画笔，通过指定颜色做分割线的画笔
     */
    private Paint paint = new Paint();
    /**
     * 上下两个item的间距
     */
    private int space = 0;
    /**
     * 上下两个item的间距的颜色
     */
    private int mSpaceColor;
    /**
     * 是否指定了上下两个item的间距的颜色
     */
    private boolean mIsSetSpaceColor;
    /**
     * 左边的间距
     */
    private int mPaddindLeft;
    /**
     * 右边的间距
     */
    private int mPaddingRight;
    /**
     * 是否包括第一条的头部
     */
    private boolean mIncludeFristTop;

    /**
     * 分割的drawable;
     */
    private Drawable mDividerDrawable;


    public VItemDecoration setSpace(int space) {
        this.space = space;
        return this;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildLayoutPosition(view);
        boolean hasHeaderCount = hasHeaderCount(parent);
        if (hasHeaderCount && position == 0) {
            return;
        }
        if (hasHeaderCount) {
            position--;
        }
        if (position == 0 && !mIncludeFristTop) {
            return;
        }
        outRect.top = getSpace();
        /*if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = getSpace() + outRect.bottom;
        }*/

    }


    private int getSpace() {
        if (mDividerDrawable != null) {
            return mDividerDrawable.getIntrinsicHeight();
        } else {
            return space;
        }
    }


    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null) {
            return;
        }
        canvas.save();
        if (parent.getClipToPadding()) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        }
        boolean hasHeaderCount = hasHeaderCount(parent);
        int i = 0;
        if (hasHeaderCount) {
            i++;
        }
        if (!mIncludeFristTop) {
            i++;
        }
        int childCount = parent.getChildCount();
        for (; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            parent.getDecoratedBoundsWithMargins(child, mBounds);
            mBounds.bottom = mBounds.top + getSpace();
            mBounds.left = mBounds.left + mPaddindLeft;
            mBounds.right = mBounds.right - mPaddingRight;
            if (mDividerDrawable != null) {
                mDividerDrawable.setBounds(mBounds);
                mDividerDrawable.draw(canvas);
            } else {
                if (mIsSetSpaceColor)
                    paint.setColor(mSpaceColor);
                canvas.drawRect(mBounds, paint);
            }

        }
        canvas.restore();
    }


    /**
     * 有没有头
     *
     * @param recyclerView
     * @return
     */
    private boolean hasHeaderCount(RecyclerView recyclerView) {
        boolean headerCount = false;
        if (recyclerView != null && recyclerView.getAdapter() instanceof SmartAdapter) {
            SmartAdapter smartAdapter = (SmartAdapter) recyclerView.getAdapter();
            headerCount = smartAdapter.hasHeader();
        }

        return headerCount;
    }

    /**
     * 有没有脚
     *
     * @param recyclerView
     * @return
     */
    private boolean hasFooterCount(RecyclerView recyclerView) {
        boolean headerCount = false;
        if (recyclerView != null && recyclerView.getAdapter() instanceof SmartAdapter) {
            SmartAdapter smartAdapter = (SmartAdapter) recyclerView.getAdapter();
            headerCount = smartAdapter.hasHeader();
        }

        return headerCount;
    }


    /**
     * 有没有脚
     *
     * @param recyclerView
     * @return
     */
    private boolean haseFooterCount(RecyclerView recyclerView) {
        boolean headerCount = false;
        if (recyclerView != null && recyclerView.getAdapter() instanceof SmartAdapter) {
            SmartAdapter smartAdapter = (SmartAdapter) recyclerView.getAdapter();
            headerCount = smartAdapter.hasHeader();
        }

        return headerCount;
    }

    private VItemDecoration setSetSpaceColor(boolean setSpaceColor) {
        mIsSetSpaceColor = setSpaceColor;
        if(mIsSetSpaceColor){
            mDividerDrawable = null;
        }
        return this;
    }


    public VItemDecoration setSpaceColor(int spaceColor) {
        this.mSpaceColor = spaceColor;
        setSetSpaceColor(true);
        return this;
    }


    public VItemDecoration setIncludeFristTop(boolean includeFrist) {
        this.mIncludeFristTop = includeFrist;
        return this;
    }


    /**
     * 设置分割线
     */
    public VItemDecoration setDividerDrawable(Drawable dividerDrawable) {
        this.mDividerDrawable = dividerDrawable;
        return this;
    }

    /**
     * 设置分割线
     *
     * @param drawableResId--------drawable的id
     * @param context
     * @return
     */
    public VItemDecoration setDividerDrawableId(int drawableResId, @Nullable Context context) {
        if (context == null) {
            return this;
        }
        this.mDividerDrawable = ContextCompat.getDrawable(context.getApplicationContext(), drawableResId);
        return this;
    }


    /******************************************************************************/
    /************************************设置左右的间隔*****************************/
    /**
     * 根据dp,设置左边的间隔
     *
     * @param paddindLeft
     * @return
     */
    public VItemDecoration setPaddindLeftDip(int paddindLeft, Context context) {
        if (context == null || paddindLeft < 0)
            return this;

        this.mPaddindLeft = (int) (context.getResources().getDisplayMetrics().density * paddindLeft + 0.5f);
        return this;
    }

    /**
     * 根据像素，设置右边的间隔
     *
     * @param paddindLeft
     * @return
     */
    public VItemDecoration setPaddindLeftPx(int paddindLeft) {
        if (paddindLeft < 0)
            return this;
        this.mPaddindLeft = paddindLeft;
        return this;
    }


    /**
     * 根据像素设置右边的间隔
     *
     * @param paddingRight
     * @return
     */
    public VItemDecoration setPaddingRightPx(int paddingRight) {
        this.mPaddingRight = paddingRight;
        return this;
    }

    /**
     * 根据Dp ，设置右边的间隔
     *
     * @param paddingRight
     * @return
     */
    public VItemDecoration setPaddingRightDip(int paddingRight, Context context) {
        if (context == null || paddingRight < 0)
            return this;
        this.mPaddingRight = (int) (context.getResources().getDisplayMetrics().density * paddingRight + 0.5f);
        return this;
    }


    /**
     * 根据Dp ，设置左右边的间隔
     *
     * @param padding
     * @return
     */
    public VItemDecoration setPaddingPx(int padding) {
        if (padding < 0)
            return this;
        mPaddindLeft = padding;
        mPaddingRight = padding;

        return this;
    }


    /**
     * 根据像素 ，设置左右边的间隔
     *
     * @param padding
     * @return
     */
    public VItemDecoration setPaddingDip(int padding, Context context) {
        if (context == null || padding < 0)
            return this;
        this.mPaddingRight = (int) (context.getResources().getDisplayMetrics().density * padding + 0.5f);
        this.mPaddindLeft = mPaddingRight;
        return this;
    }


}
