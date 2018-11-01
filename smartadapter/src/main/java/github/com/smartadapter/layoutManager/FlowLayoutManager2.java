package github.com.smartadapter.layoutManager;

import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

public class FlowLayoutManager2 extends RecyclerView.LayoutManager {

    //TODO  子位在每一行的状态
    private SparseArray<RectF> mViewsPostion;
    private int mTotalScroll;
    private int mLastMeasurePosition = 0;

    public FlowLayoutManager2() {
        mViewsPostion = new SparseArray<>();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        mLastMeasurePosition = 0;
        removeAllViews();
        measure(recycler, state);
        fill(recycler, state, 0);
    }

    /**
     * 对子view的放置
     *
     * @param recycler
     * @param state
     * @param dy
     */
    protected void fill(RecyclerView.Recycler recycler, RecyclerView.State state, int dy) {
        int totalScroll = this.mTotalScroll + dy;
        int start = 0;
        int last = getItemCount();
        removeAllViews();
        for (int i = start; i < last; i++) {
            RectF rectF = getRectF(recycler, state, i);
            if (rectF.top - totalScroll >= getPaddingTop() && rectF.bottom - totalScroll <= getHeight() - getPaddingBottom()
                    || (rectF.top - totalScroll > getPaddingTop() && rectF.top - totalScroll <= getHeight() - getPaddingBottom())
                    || ((rectF.bottom - totalScroll) >= getPaddingTop() && rectF.bottom - totalScroll < getHeight() - getPaddingBottom())) {
                View viewForPosition = recycler.getViewForPosition(i);
                recycler.bindViewToPosition(viewForPosition, i);
                addView(viewForPosition);
                measureChildWithMargins(viewForPosition, 0, 0);
                layoutDecoratedWithMargins(viewForPosition, (int) rectF.left, (int) rectF.top - this.mTotalScroll, (int) rectF.right, (int) rectF.bottom - this.mTotalScroll);
            }
            if (rectF.top - totalScroll > getHeight() - getPaddingBottom()) {
                break;
            }
        }
    }


    protected RectF getRectF(RecyclerView.Recycler recycler, RecyclerView.State state, int position) {
        if (position < 0 || position >= getItemCount())
            return null;
        RectF rectF = mViewsPostion.get(position);
        if (rectF == null) {
            measure(recycler, state);
            return getRectF(recycler, state, position);
        }
        return rectF;
    }


    /**
     * 测量,只测量一当前可见的
     */
    protected void measure(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0)
            return;
        int leftOffset = getPaddingLeft();
        int topOffset = getPaddingTop();
        int maxLineHeight = 0;

        //得到上次测量的最后结果
        if (mLastMeasurePosition > 0 && mViewsPostion.get(mLastMeasurePosition - 1) != null) {
            leftOffset = (int) mViewsPostion.get(mLastMeasurePosition - 1).right;
            topOffset = (int) mViewsPostion.get(mLastMeasurePosition - 1).top;
            maxLineHeight = (int) mViewsPostion.get(mLastMeasurePosition - 1).height();
        }
        for (int i = mLastMeasurePosition; i < getItemCount(); i++) {
            //检查是否测量过
            if (mViewsPostion.get(i) != null) {
                leftOffset = (int) mViewsPostion.get(i).right;
                topOffset = (int) mViewsPostion.get(i).top;
                maxLineHeight = (int) mViewsPostion.get(i).height();
                continue;
            }
            View view = recycler.getViewForPosition(i);
            measureChildWithMargins(view, 0, 0);
            mLastMeasurePosition++;
            //同一行的测量
            if (leftOffset + getDecoratedMeasuredWidth(view) < getWidth() - getPaddingRight()) {
                maxLineHeight = Math.max(maxLineHeight, getDecoratedMeasuredHeight(view));
                RectF rectF = new RectF();
                rectF.left = leftOffset;
                rectF.right = leftOffset + getDecoratedMeasuredWidth(view);
                rectF.top = topOffset;
                rectF.bottom = topOffset + maxLineHeight;
                mViewsPostion.put(i, rectF);
                leftOffset = (int) rectF.right;
            } else {//换行
                leftOffset = getPaddingLeft();
                topOffset = topOffset + maxLineHeight;
                maxLineHeight = getDecoratedMeasuredHeight(view);
                RectF rectF = new RectF();
                rectF.left = leftOffset;
                rectF.right = leftOffset + getDecoratedMeasuredWidth(view);
                rectF.top = topOffset;
                rectF.bottom = topOffset + maxLineHeight;
                mViewsPostion.put(i, rectF);
                leftOffset = (int) rectF.right;
                //只测量能在屏幕显示的内容
                if (rectF.top - mTotalScroll > getHeight() - getPaddingBottom()) {
                    break;
                }
            }

        }

    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.scrollVerticallyBy(dy, recycler, state);
        int childCount = getChildCount();
        if (childCount == 0 || dy == 0) {
            return 0;
        }
        if (mTotalScroll + dy < getPaddingTop()) {//上边界
            dy = -mTotalScroll;
        }
        if (dy > 0) {//下边界
            View view = getChildAt(childCount - 1);
            if (getItemCount() - 1 == getPosition(view)) {
                int gas = getHeight() - getDecoratedBottom(view) - getPaddingBottom();
                if (gas > 0) {
                    int itemCount = getItemCount();
                    if (itemCount == childCount) {//没有满一屏的情况
                        dy = 0;
                    } else {
                        dy = -gas;
                    }
                } else if (gas == 0) {
                    dy = 0;
                } else {
                    dy = Math.min(-gas, dy);
                }
            }
        }
        //先放置在平移
        fill(recycler, state, dy);
        mTotalScroll += dy;
        offsetChildrenVertical(-dy);
        return dy;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }
}
