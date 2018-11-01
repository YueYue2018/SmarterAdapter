package github.com.smartadapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import github.com.smartadapter.ISmartInterface.*;
import github.com.smartadapter.customViews.SwipeItemLayout;


public abstract class BaseSmartViewHolder<T> extends RecyclerView.ViewHolder {
    protected ViewDataBinding mBind;
    protected ItemProperty itemProperty;
    //要加载的数据
    protected T data;
    private SmartAdapter smartAdapter;

    public void setItemProperty(ItemProperty itemProperty) {
        this.itemProperty = itemProperty;
    }

    public ItemProperty getItemProperty() {
        return itemProperty;
    }

    public BaseSmartViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * 通过自定义来绑定数据
     *
     * @param object
     */
    public void bindData(T object) {
    }

    public void bindData(int dataBrId, T object, IBindDataView iBindDataView) {
        this.data = object;
        if(iBindDataView != null){
            iBindDataView.bindData(itemView,data);
            return;
        }
        //自定来绑定数据
        if (dataBrId == -1 || dataBrId == 0) {
            bindData(object);
            return;
        }
        //通过databing来绑定数据
        mBind = DataBindingUtil.bind(itemView);
        mBind.setVariable(dataBrId, object);
        mBind.executePendingBindings();
    }

    public <T extends View> T getView(int idResource) {
        return itemView.findViewById(idResource);
    }

    public ImageView getImageView(int idResource) {
        return getView(idResource);
    }

    public TextView getTextView(int idResource) {
        return getView(idResource);
    }

    public TextView setTextView(int idResource, String text) {
        TextView textView = getTextView(idResource);
        if (textView != null)
            textView.setText(text);
        return textView;
    }


    /**
     * 通过ID 来给item 来具体的某一个view设置点击事件
     *
     * @param itemClickListenerList viewID 与相应的点击事件的集合
     * @param position----item的位置
     */
    public void bindItemClickChild(final SparseArray<ItemClickListener> itemClickListenerList, final int position) {
        if (itemClickListenerList == null || itemClickListenerList.size() == 0) {
            return;
        }
        for (int i = 0; i < itemClickListenerList.size(); i++) {
            //得到点击事件的view
            View view = itemView.findViewById(itemClickListenerList.keyAt(i));
            final ItemClickListener itemClickListener = itemClickListenerList.valueAt(i);
            if (view == null || itemClickListener == null) {
                continue;
            } else {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (FastClickUtil.isCanClick() && itemClickListener != null) {
                            itemClickListener.onClick(v, position, data);
                        }
                    }
                });
            }
        }
    }
    /**
     * 绑定长按事件
     */
    public void bindLongListener(int layoutId, final int position){
        if(smartAdapter == null)
            return;
        //长按事件
        ItemLongListener longListener = null;
        if (smartAdapter.getIteLongListenerSparseArray() != null){
            SparseArray<ItemLongListener> iteLongListenerSparseArray = smartAdapter.getIteLongListenerSparseArray();
            longListener =  iteLongListenerSparseArray.get(layoutId);
        }

        if (longListener == null) {
            longListener = smartAdapter.getIteLongListener();
        }
        if(longListener == null)
            return;
        final ItemLongListener longListener1 = longListener;
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longListener1.onLongClick(itemView,position,data);
                return true;
            }
        });
    }
    /**
     * 单选
     *
     * @param position
     * @param singleOnCheckedChangeListener
     * @param onCheckedChangeView
     */
    protected void bindSingleChoiceListenerImp(final int position, final ISingleOnCheckedChangeListener singleOnCheckedChangeListener, final IOnCheckedChangeView onCheckedChangeView) {
        checkAdapter();
        View oldView = null;
        Object oldData = null;
        int oldPosition = -1;
        boolean isSelect = false;
        BaseSmartViewHolder oldViewHold = null;
        if (smartAdapter.getSelectPosition().isEmpty() || smartAdapter.getSelectData().isEmpty()) {//当前一条也没有选中
            addSelectData(position);
            isSelect = true;
        } else {//如果有选中的数据
            oldPosition = (int) smartAdapter.getSelectPosition().get(0);//选中的位置
            oldData = smartAdapter.getSelectData().get(0);//选中的数据
            clearSelect();
            RecyclerView.ViewHolder viewHolder = smartAdapter.getPositionViewHolder(position);
            if (viewHolder != null)
                oldView = viewHolder.itemView;
            if (oldPosition != position || data != oldData) {//未选中
                isSelect = true;
                addSelectData(position);
            }
        }
        if (singleOnCheckedChangeListener != null) {
            singleOnCheckedChangeListener.onCheckedChanged(oldView, oldData, itemView, data, isSelect);
        }
        if (onCheckedChangeView != null) {
            onCheckedChangeView.onCancelCheckViewChanged(oldViewHold, oldView, oldData, oldPosition);
            if (isSelect)
                onCheckedChangeView.onCheckedViewChanged(this, itemView, data, position);
        }
    }


    protected void bindMultipleChoiceListenerImp(int position, IMultipleOnCheckedChangeView multipleOnCheckedChangeView, IOnCheckedChangeView onCheckedChangeView) {
        if (multipleOnCheckedChangeView == null && onCheckedChangeView == null) {
            return;
        }
        checkAdapter();
        if (multipleOnCheckedChangeView != null) {
            if (smartAdapter.getSelectPosition().contains(position)) {//已选中
                removeSelectData(position);
                if (onCheckedChangeView != null) {
                    onCheckedChangeView.onCancelCheckViewChanged(BaseSmartViewHolder.this, itemView, data, position);
                }
            } else {//未选中
                addSelectData(position);
                if (onCheckedChangeView != null) {
                    onCheckedChangeView.onCheckedViewChanged(BaseSmartViewHolder.this, itemView, data, position);
                }
            }
            if (multipleOnCheckedChangeView != null)
                multipleOnCheckedChangeView.onCheckedChanged(smartAdapter.getSelectPosition(), smartAdapter.getSelectData());
        }
    }


    /**
     * 清除选中的数据
     */
    private void clearSelect() {
        checkAdapter();
        smartAdapter.getSelectPosition().clear();
        smartAdapter.getSelectData().clear();
    }

    /**
     * 检查adpater是不是为空
     */
    private void checkAdapter() {
        if (smartAdapter == null)
            throw new RuntimeException("smartAdapter is null ,you must setSmartAdapter frist");
    }

    /**
     * 增加选中的数据
     *
     * @param position
     */
    private void addSelectData(int position) {
        checkAdapter();
        smartAdapter.getSelectPosition().add(position);
        smartAdapter.getSelectData().add(data);
    }


    /**
     * 移除选中的数据
     *
     * @param position
     */
    private void removeSelectData(Integer position) {
        checkAdapter();
        smartAdapter.getSelectPosition().remove(position);
        smartAdapter.getSelectData().remove(data);
    }

    /**
     * 绑定头部的点击事件
     *
     * @param headerViewLinearLayout
     * @param headViewClickLister
     */
    public void bindHeadViewClickLister(LinearLayout headerViewLinearLayout, final View.OnClickListener headViewClickLister) {
        if (headerViewLinearLayout == null || headViewClickLister == null) {
            return;
        }
        int childCount = headerViewLinearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            headerViewLinearLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FastClickUtil.isCanClick()) {
                        headViewClickLister.onClick(v);
                    }
                }
            });
        }
    }

    /**
     * 绑定头部item 子view 点击事件
     *
     * @param mHeaderViewLinearLayout
     * @param itemHeaderChildListenerList---------子view点击事件的集合
     */
    public void bindDataHeaderChildListener(final LinearLayout mHeaderViewLinearLayout, final SparseArray<View.OnClickListener> itemHeaderChildListenerList) {
        if (mHeaderViewLinearLayout == null || itemHeaderChildListenerList == null || itemHeaderChildListenerList.size() == 0) {
            return;
        }
        for (int i = 0; i < itemHeaderChildListenerList.size(); i++) {
            //得到相应的view
            int id = itemHeaderChildListenerList.keyAt(i);
            View view = mHeaderViewLinearLayout.findViewById(id);
            //绑定相关的点击事件
            final View.OnClickListener onClickListener = itemHeaderChildListenerList.valueAt(i);
            if (onClickListener == null) {
                continue;
            }
            if (view != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (FastClickUtil.isCanClick()) {
                            onClickListener.onClick(v);
                        }
                    }
                });
            }
        }
    }

    public void bindFooterViewClickLister(LinearLayout mFooterViewLinearLayout, View.OnClickListener footerViewClickLister) {
        bindHeadViewClickLister(mFooterViewLinearLayout, footerViewClickLister);
    }

    public void bindDataFooterChildListener(LinearLayout mFooterViewLinearLayout, SparseArray<View.OnClickListener> itemFooterChildListenerList) {
        bindDataHeaderChildListener(mFooterViewLinearLayout, itemFooterChildListenerList);
    }

    public void setSmartAdapter(SmartAdapter smartAdapter) {
        this.smartAdapter = smartAdapter;
    }

    public void bindAllClick(final ItemClickListener itemClickListener, final ISingleOnCheckedChangeListener singleOnCheckedChangeListener, final IMultipleOnCheckedChangeView multipleOnCheckedChangeView, final IOnCheckedChangeView onCheckedChangeView, final int realPosition) {
        if (itemClickListener == null && singleOnCheckedChangeListener == null && multipleOnCheckedChangeView == null) {
            return;
        }
        View viewClick = itemView;
        if (itemView instanceof SwipeItemLayout) {
            viewClick = ((SwipeItemLayout) itemView).getChildAt(0);
        }
        viewClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAdapter();
                int position = BaseSmartViewHolder.this.getAdapterPosition();
                if (smartAdapter.hasHeader()) {
                    position++;
                }
                if (itemProperty.getItemClickListener() != null && FastClickUtil.isCanClick()) {
                    itemClickListener.onClick(itemView,position,data);
                } else if (itemClickListener != null && FastClickUtil.isCanClick()) {
                    itemClickListener.onClick(itemView, position, data);
                }
                if (smartAdapter.singleChoice && (singleOnCheckedChangeListener != null || onCheckedChangeView != null)) {
                    bindSingleChoiceListenerImp(position, singleOnCheckedChangeListener, onCheckedChangeView);
                }
                if (smartAdapter.multipleChoice && (multipleOnCheckedChangeView != null || onCheckedChangeView != null)) {
                    bindMultipleChoiceListenerImp(position, multipleOnCheckedChangeView, onCheckedChangeView);
                }
            }
        });
    }
}
