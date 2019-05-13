package github.com.smartadapter;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import github.com.smartadapter.ISmartInterface.*;
import github.com.smartadapter.animation.*;


import java.util.ArrayList;
import java.util.List;

public class SmartAdapter<VH extends BaseSmartViewHolder> extends RecyclerView.Adapter<VH> {
    /*********************************************数据及相关的处理*****************************************************/
    //数据源
    protected List<Object> mDates;
    //adapter 设置的帮助类
    protected ItemBindHelper itemBindHelper;
    protected Context mContext;
    private LayoutInflater mLayoutInflater;
    //数据差量更新回调
    private IDiffCallBack mDiffCallBack;
    //开启单选模式
    protected boolean singleChoice = false;
    //开启多选模式
    protected boolean multipleChoice = false;
    //boolean ------true 表示item如果开启了多选或者单选，点击事件只能响应多选或单选。反之，能同时响应点击事件与单选或多选
    private boolean mOnlyItemClickOrChoice = true;
    //视图变化的回调
    private IOnCheckedChangeView onCheckedChangeView;
    //单选数据的变化回调
    private ISingleOnCheckedChangeListener singleOnCheckedChangeListener;

    //多选数据的变化回调
    private IMultipleOnCheckedChangeView multipleOnCheckedChangeView;
    //选中的数据
    private List<Object> mSelectData;
    //选中的Position;
    private List<Integer> mSelectPosition;
    //所有的viewHolder
    private SparseArray<BaseSmartViewHolder> baseSmartViewHolderAll = new SparseArray();
    protected int checkBoxId;

    /*********************************************************************************************************/
    /*********************************************点击事件*****************************************************/
    //点击事件
    private ItemClickListener itemClickListener;
    //item 中view对应的点击事  key-->view id;
    private SparseArray<ItemClickListener> itemEveryViewClickListenerList;
    //长按事件
    private SparseArray<ItemLongListener> itemLongListenerSparseArray;
    private ItemLongListener mItemLongListener;
    /*********************************************************************************************************/

    /*********************************************头与脚布局*****************************************************/
    //头布局
    protected LinearLayout mHeaderViewLinearLayout;
    //脚布局
    protected LinearLayout mFooterViewLinearLayout;
    //头
    private final int TYPE_HEADER = 0x112233;
    //脚
    private final int TYPE_FOOTER = 0x112234;
    //头view对应的点击事  key-->view id;
    private SparseArray<View.OnClickListener> itemHeaderChildListenerList;
    private SparseArray<View.OnClickListener> itemFooterChildListenerList;
    /*********************************************************************************************************/

    /*********************************************加载过程中的过度布局*****************************************************/
    //加载中
    protected static final int TYPE_LOADING = 0x112235;
    //加载失败
    protected static final int TYPE_FAILED = 0x112236;
    //没有有网络
    protected static final int TYPE_NO_NET = 0x112237;
    //正常的视图
    protected static final int TYPE_NORMAL = 0x112238;
    //没有数据
    protected static final int TYPE_EMPTY = 0x112239;
    //当前的状态
    protected int type_current = TYPE_NORMAL;
    /*********************************************************************************************************/


    /*********************************************动画相关的设置*****************************************************/
    //透明动画
    public static final int ANIMATION_ALPHA = 0x1;
    //缩放动画
    public static final int ANIMATION_SCALE = 0x2;
    //从下边进入的动画
    public static final int ANIMATION_SLIDE_IN_BOTTOM = 0x4;
    //从右边进入的动画
    public static final int ANIMATION_SLIDE_IN_RIGHT = 0x8;
    //从左边进入的动画
    public static final int ANIMATION_SLIDE_IN_LEFT = 0x16;
    /**
     * 当前的动画的类型{@link #ANIMATION_ALPHA,#ANIMATION_SCALE,#ANIMATION_SLIDE_IN_BOTTOM,#ANIMATION_SLIDE_IN_RIGHT}
     */
    protected int typeAnimation;
    //当前使用的动画
    protected BaseAnimation currentAnimation;
    //是否执行动画
    protected boolean isEnableAnimation;
    //动画执行的时间 单位：ms
    protected int animationDuring = 300;
    //动画只执行一次
    protected boolean isEnableAnimationOnce = true;
    //上次执行到动画的位置
    protected int mLastAnimationPosition;

    /**************************************************************************************************/

    private RecyclerView recyclerView;

    public SmartAdapter(Context context) {
        mDates = new ArrayList<>();
        mContext = context;
    }

    public SmartAdapter(Context context, int layoutId) {
        this(context, null, layoutId);
    }


    public SmartAdapter(Context context, @NonNull List<Object> dates) {
        this(context, dates, 0);
    }


    public SmartAdapter(Context context, @NonNull List<Object> dates, int layoutId) {
        mContext = context;
        mDates = new ArrayList<>();
        if (dates != null) {
            mDates.addAll(dates);
        }
        register(layoutId);

    }


    /**************************************************************************************************/
    /*****************************************创建viewHolder*********************************************************/
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        VH viewHolder;
        if (mLayoutInflater == null)
            mLayoutInflater = LayoutInflater.from(mContext);
        if (type == TYPE_HEADER) {//头
            viewHolder = createHeaderViewHolder(viewGroup);
        } else if (type == TYPE_FOOTER) {//脚
            viewHolder = createSmartFooterViewHolder(viewGroup);
        } else if (itemBindHelper.hastCustomSmartViewHolder()) {//自定义的viewHolder
            viewHolder = createCustomSmartViewHolder(viewGroup, type, itemBindHelper.getCustomSmartViewHolderName());
        } else {//默认的viewHolder
            viewHolder = createSmartViewHolder(viewGroup, type);
        }
        //给viewHolder绑定相关的item的属性
        viewHolder.setItemProperty(itemBindHelper.getCurrentItemProperty());
        return viewHolder;
    }

    //创建默认的ViewHolder
    protected VH createSmartViewHolder(@NonNull ViewGroup viewGroup, int layoutId) {
        return (VH) new SmartViewHolder(mLayoutInflater.inflate(layoutId, viewGroup, false));
    }

    //创建自定的ViewHolder
    protected VH createCustomSmartViewHolder(@NonNull ViewGroup viewGroup, int layoutId, String viewHolderName) {
        return (VH) itemBindHelper.getBaseSmartViewHolder(mLayoutInflater.inflate(layoutId, viewGroup, false));
    }

    /**
     * 创建头的viewHolder
     *
     * @param viewGroup
     * @return
     */
    protected VH createHeaderViewHolder(@NonNull ViewGroup viewGroup) {
        return (VH) new SmartViewHolder(mHeaderViewLinearLayout);
    }

    /**
     * 创建脚的viewHolder
     *
     * @param viewGroup
     * @return
     */
    protected VH createSmartFooterViewHolder(@NonNull ViewGroup viewGroup) {
        return (VH) new SmartViewHolder(mFooterViewLinearLayout);
    }
    /**************************************************************************************************/
    /******************************************数据的绑定********************************************************/
    @Override
    final public void onBindViewHolder(@NonNull VH vh, int position) {
        if (type_current != TYPE_NORMAL) {//异常视图 如加载中，没有网络
            itemBindHelper.bindDataNotNormalView(vh, type_current);
            return;
        }
        if (isHeader(position)) {//头布局
            vh.bindHeadViewClickLister(mHeaderViewLinearLayout, itemBindHelper.getHeadViewClickLister());
            vh.bindDataHeaderChildListener(mHeaderViewLinearLayout, itemHeaderChildListenerList);
            return;
        } else if (isFooter(position)) {//脚布局
            vh.bindFooterViewClickLister(mFooterViewLinearLayout, itemBindHelper.getFooterViewClickLister());
            vh.bindDataFooterChildListener(mFooterViewLinearLayout, itemFooterChildListenerList);
            return;
        }
        int realPosition = getRealPosition(position);//得到没有头的位置
        baseSmartViewHolderAll.put(realPosition, vh);
        vh.setSmartAdapter(this);

        //屏蔽checkBox的点击事件
        if (checkBoxId != 0) {
            View checkBox = vh.getView(checkBoxId);
            if (checkBox != null && checkBox instanceof CheckBox) {
                checkBox.setClickable(false);
            }
        }

        itemBindHelper.bindData(vh, realPosition, this);//数据的绑定

        if (itemClickListener != null)
            //点击事件的绑定
            vh.bindAllClick(itemClickListener, singleOnCheckedChangeListener, multipleOnCheckedChangeView, onCheckedChangeView, realPosition);
        else
            vh.bindAllClick(itemBindHelper.getItemProperty(getDataObj(position)).getItemClickListener(), singleOnCheckedChangeListener, multipleOnCheckedChangeView, onCheckedChangeView, realPosition);
        //子view的点击事件
        vh.bindItemClickChild(itemEveryViewClickListenerList, realPosition);

        //选中数据的变化
        if (getSelectData().contains(vh.data) && onCheckedChangeView != null) {
            onCheckedChangeView.onCheckedViewChanged(vh, vh.itemView, vh.data, realPosition);
            addSelectPosition(realPosition);
        } else if (onCheckedChangeView != null) {
            onCheckedChangeView.onCancelCheckViewChanged(vh, vh.itemView, vh.data, realPosition);
            getSelectPosition().remove((Integer) realPosition);
            getSelectData().remove(vh.data);
        }

        //绑定长按事件
        vh.bindLongListener(itemBindHelper.getItemLayoutId(), realPosition);

    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        this.onBindViewHolder(holder, position);
    }

    /**
     * 得到真正的数据位置
     *
     * @param position
     * @return
     */
    protected int getRealPosition(int position) {
        if (hasHeader()) {
            position--;
        }
        return position;
    }


    @Override
    final public int getItemViewType(int position) {
        int type;
        if (type_current != TYPE_NORMAL) {
            type = itemBindHelper.getTipLayoutId(type_current);
        } else if (isHeader(position)) {
            type = TYPE_HEADER;
        } else if (isFooter(position)) {
            type = TYPE_FOOTER;
        } else if (itemBindHelper.isRegisterObj(getDataObj(position))) {
            type = itemBindHelper.getItemLayoutId();
        } else if (itemBindHelper.getCurrentItemProperty() != null
                && itemBindHelper.getCurrentItemProperty().getLayoutRes() != 0) {
            type = itemBindHelper.getCurrentItemProperty().getLayoutRes();
        } else {
            type = super.getItemViewType(position);
        }
        return type;
    }
    /**************************************************************************************************/

    /***************************************************单选多选的设置***********************************************/

    public boolean isOnlyItemClickOrChoice() {
        return mOnlyItemClickOrChoice;
    }

    /**
     * boolean ------true 表示item如果开启了多选或者单选，点击事件只能响应多选或单选。反之，能同时响应点击事件与单选或多选
     *
     * @param mOnlyItemClickOrChoice
     * @return
     */
    public SmartAdapter<VH> setOnlyItemClickOrChoice(boolean mOnlyItemClickOrChoice) {
        this.mOnlyItemClickOrChoice = mOnlyItemClickOrChoice;
        return this;
    }


    //是否开启单选
    public SmartAdapter setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
        if (singleChoice)
            this.multipleChoice = false;
        return this;
    }


    public SmartAdapter setCheckBoxId(int checkBoxId) {
        this.checkBoxId = checkBoxId;
        return this;
    }


    //是否开启多选
    public SmartAdapter setMultipleChoice(boolean multipleChoice) {
        this.multipleChoice = multipleChoice;
        if (multipleChoice)
            singleChoice = false;
        return this;
    }

    //单选的监听
    public SmartAdapter setSingleOnCheckedChangeListener(ISingleOnCheckedChangeListener singleOnCheckedChangeListener) {
        this.singleOnCheckedChangeListener = singleOnCheckedChangeListener;
        setSingleChoice(true);
        return this;
    }

    //多选的监听
    public SmartAdapter setMultipleOnCheckedChangeView(IMultipleOnCheckedChangeView multipleOnCheckedChangeView) {
        this.multipleOnCheckedChangeView = multipleOnCheckedChangeView;
        setMultipleChoice(true);
        return this;
    }

    /**
     * 设置不同布局的长按事件
     *
     * @return
     */
    public SmartAdapter addItemLongListenerSparseArray(int layOutId, ItemLongListener itemLongListener) {
        if (itemLongListenerSparseArray == null) {
            itemLongListenerSparseArray = new SparseArray<>();
        }
        itemLongListenerSparseArray.put(layOutId, itemLongListener);
        return this;
    }


    public SparseArray<ItemLongListener> getIteLongListenerSparseArray() {
        return itemLongListenerSparseArray;
    }

    public ItemLongListener getIteLongListener() {
        return mItemLongListener;
    }

    /**
     * 设置全局的长按事件
     */
    public SmartAdapter setIteLongListener(ItemLongListener itemLongListener) {
        this.mItemLongListener = itemLongListener;
        return this;
    }

    /**
     * 全选
     */
    public SmartAdapter selectAll() {
        if (!multipleChoice) {
            return this;
        }
        for (int i = 0; i < getDates().size(); i++) {
            int position = i;
            addSelectData(getDates().get(position));
            addSelectPositionList(position);
            BaseSmartViewHolder baseSmartViewHolder = baseSmartViewHolderAll.get(position);
            if (onCheckedChangeView != null && baseSmartViewHolder != null) {
                onCheckedChangeView.onCheckedViewChanged(baseSmartViewHolder, baseSmartViewHolder.itemView
                        , getDates().get(position), position);
            }

        }

        if (multipleOnCheckedChangeView != null) {
            multipleOnCheckedChangeView.onCheckedChanged(getSelectPosition(), getSelectData());
        }
        return this;
    }

    //反选
    public SmartAdapter selectInvertAll() {
        if (!multipleChoice || recyclerView == null) {
            return this;
        }
        List<Integer> selectPosition = getSelectPosition();
        for (int position = 0; position < getDates().size(); position++) {
            if (selectPosition.contains(position)) {
                getSelectData().remove(getDates().get(position));
                getSelectPosition().remove((Integer) position);
            } else {
                addSelectData(getDates().get(position));
                addSelectPositionList(position);
            }
            notifyDataSetChanged();
        }

        return this;
    }


    //取消选择
    public SmartAdapter selectCancelAll() {
        List<Integer> selectPosition = getSelectPosition();
        for (int position : selectPosition) {
            BaseSmartViewHolder baseSmartViewHolder = baseSmartViewHolderAll.get(position);
            if (selectPosition.contains(position) && baseSmartViewHolder != null) {
                if (onCheckedChangeView != null && baseSmartViewHolder != null) {
                    onCheckedChangeView.onCancelCheckViewChanged(baseSmartViewHolder, baseSmartViewHolder.itemView
                            , getDates().get(position), position);
                }
                if (singleChoice && singleOnCheckedChangeListener != null) {
                    singleOnCheckedChangeListener.onCheckedChanged(baseSmartViewHolder.itemView, getDates().get(0), null, null, false);
                }
            }
        }
        getSelectPosition().clear();
        getSelectData().clear();
        if (multipleOnCheckedChangeView != null) {
            multipleOnCheckedChangeView.onCheckedChanged(getSelectPosition(), getSelectData());
        }

        return this;
    }


    public View getSingleSelectView() {
        View singleSelectView = null;
        if (singleChoice && getSelectData().size() > 0) {
            singleSelectView = recyclerView.getLayoutManager().getChildAt(getSelectPosition().get(0));
        }
        return singleSelectView;
    }


    //设置选中的位置
    public SmartAdapter setSelectPosition(List<Integer> selectPosition) {
        getSelectPosition().clear();
        if (selectPosition != null) {
            getSelectPosition().addAll(selectPosition);
            for (int position : selectPosition) {
                if (position < 0 || position >= mDates.size()) {
                    getSelectPosition().remove((Integer) (position));
                } else {
                    getSelectData().add(mDates.get(position));
                }
            }
        }
        notifyDataSetChanged();
        return this;
    }

    //选中的数据
    public List<Object> getSelectData() {
        if (mSelectData == null)
            mSelectData = new ArrayList<>();
        return mSelectData;
    }


    /**
     * 选中的位置，当调用{@link #removePosition}删除位置后，取得的位置可能不是你所期望的
     *
     * @return
     */
    public List<Integer> getSelectPosition() {
        if (mSelectPosition == null)
            mSelectPosition = new ArrayList<>();
        return mSelectPosition;
    }

    /**
     * 增加选择的位置
     *
     * @param position
     */
    public SmartAdapter addSelectPosition(int position, boolean onSelectCallBack) {
        if (position < 0 || position >= mDates.size() || !multipleChoice) {
            return this;
        }
        if (addSelectPositionList(position)) {
            BaseSmartViewHolder positionViewHolder = getPositionViewHolder(position);
            if (onCheckedChangeView != null && positionViewHolder != null) {
                onCheckedChangeView.onCheckedViewChanged(positionViewHolder, positionViewHolder.itemView, getDataObj(position), position);
            }
            if (multipleOnCheckedChangeView != null && onSelectCallBack) {
                multipleOnCheckedChangeView.onCheckedChanged(getSelectPosition(), getSelectData());
            }
        }
        return this;
    }


    /**
     * 增加选择的位置
     *
     * @param position
     */
    public SmartAdapter addSelectPosition(int position) {
        return addSelectPosition(position, false);
    }


    /**
     * 设置选中的位置
     *
     * @param position
     * @return
     */
    public SmartAdapter setSigleSelectPostion(int position) {
        if (position < 0 || position >= mDates.size()) {
            return this;
        }
        if (getSelectPosition().size() > 0) {
            int integer = getSelectPosition().get(0);
            BaseSmartViewHolder positionViewHolder = getPositionViewHolder(integer);
            addSelectData(mDates.get(position));
            if (addSelectPositionList(position)) {
                if (onCheckedChangeView != null && positionViewHolder != null) {
                    onCheckedChangeView.onCancelCheckViewChanged(positionViewHolder, positionViewHolder.itemView, getDataObj(position), integer);
                }
                positionViewHolder = getPositionViewHolder(position);
                if (onCheckedChangeView != null && positionViewHolder != null) {
                    onCheckedChangeView.onCheckedViewChanged(positionViewHolder, positionViewHolder.itemView, getDataObj(position), position);
                }
            }
        } else {
            BaseSmartViewHolder positionViewHolder = getPositionViewHolder(position);
            if (onCheckedChangeView != null && positionViewHolder != null) {
                onCheckedChangeView.onCheckedViewChanged(positionViewHolder, positionViewHolder.itemView, getDataObj(position), position);
            }
        }


        return this;
    }


    //检查相应单选多选的集合是否为空
    private void checkSelectPostion() {
        if (mSelectPosition == null)
            mSelectPosition = new ArrayList<>();
        if (mSelectPosition == null)
            mSelectPosition = new ArrayList<>();

    }


    protected boolean addSelectPositionList(int position) {
        checkSelectPostion();
        boolean addView = false;
        if (position >= 0 && !getSelectPosition().contains(position)) {
            addView = true;
            getSelectPosition().add(position);
        }
        return addView;
    }

    protected boolean addSelectData(Object data) {
        boolean addData = false;
        if (!getSelectData().contains(data)) {
            getSelectData().add(data);
            addData = true;
        }
        return addData;
    }


    public SmartAdapter setOnCheckedChangeView(IOnCheckedChangeView onCheckedChangeView) {
        this.onCheckedChangeView = onCheckedChangeView;
        return this;
    }


    public BaseSmartViewHolder getPositionViewHolder(int readDataPosition) {
        BaseSmartViewHolder viewHolder = null;
        if (recyclerView != null && readDataPosition < mDates.size()) {
            if (hasHeader())
                readDataPosition++;
            viewHolder = (BaseSmartViewHolder) recyclerView.findViewHolderForLayoutPosition(readDataPosition);
        }
        return viewHolder;

    }


    /**************************************************************************************************/
    /*********************************************动画相关的设置*****************************************************/
    /**
     * 设置动画的类型
     *
     * @param typeAnimation
     * @return
     */
    public SmartAdapter setTypeAnimation(int typeAnimation) {
        isEnableAnimation = true;
        this.typeAnimation = typeAnimation;
        return this;
    }

    /**
     * 设置动画
     *
     * @param baseAnimation
     * @return
     */
    public SmartAdapter setAnimation(BaseAnimation baseAnimation) {
        this.currentAnimation = baseAnimation;
        return this;
    }

    /**
     * 是否执行动画
     *
     * @param enableAnimation
     * @return
     */
    public SmartAdapter setEnableAnimation(boolean enableAnimation) {
        isEnableAnimation = enableAnimation;
        return this;
    }

    /**
     * 动画是否只执行一次
     *
     * @param enableAnimationOnce
     * @return
     */
    public SmartAdapter setEnableAnimationOnce(boolean enableAnimationOnce) {
        if (enableAnimationOnce) {
            isEnableAnimation = enableAnimationOnce;
        }

        isEnableAnimationOnce = enableAnimationOnce;
        return this;
    }

    /**
     * 动画执行的时间 单位：ms
     *
     * @param animationDuring
     * @return
     */
    public SmartAdapter AnimationDuring(int animationDuring) {
        this.animationDuring = animationDuring;
        return this;
    }

    /**
     * 设置动画
     *
     * @param vh
     */
    private void addAnimation(VH vh, int viewHolderType) {
        if (!isEnableAnimation || viewHolderType == TYPE_HEADER || TYPE_FOOTER == viewHolderType)
            return;
        if (isEnableAnimationOnce && mLastAnimationPosition > vh.getAdapterPosition())
            return;

        switch (typeAnimation) {
            case ANIMATION_ALPHA:
                currentAnimation = new AlphaInAnimation();
                break;
            case ANIMATION_SCALE:
                currentAnimation = new ScaleInAnimation();
                break;
            case ANIMATION_SLIDE_IN_BOTTOM:
                currentAnimation = new SlideInBottomAnimation();
                break;
            case ANIMATION_SLIDE_IN_RIGHT:
                currentAnimation = new SlideInRightAnimation();
                break;
            case ANIMATION_SLIDE_IN_LEFT:
                currentAnimation = new SlideInLeftAnimation();
        }
        if (currentAnimation != null) {
            Animator[] animators = currentAnimation.getAnimators(vh.itemView);
            if (animators == null)
                throw new RuntimeException("Animator don't allow null.See BaseAnimation.getAnimators(View view) please");
            for (Animator animation : animators) {
                animation.setDuration(animationDuring);
                animation.start();
            }
        }
        mLastAnimationPosition = vh.getLayoutPosition();
    }
    /**************************************************************************************************/
    /*************************************************设置整行*************************************************/
    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (type_current != TYPE_NORMAL)
                        return gridManager.getSpanCount();
                    else {
                        int type = getItemViewType(position);
                        if (type == TYPE_HEADER || type == TYPE_FOOTER) {
                            return gridManager.getSpanCount();
                        }
                        ItemProperty itemProperty = itemBindHelper.getItemProperty(getDataObj(position));
                        if (itemProperty == null) {
                            return 1;
                        }
                        if (itemProperty.isSpan()) {
                            return gridManager.getSpanCount();
                        }
                        return 1;
                    }

                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VH holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == TYPE_HEADER || type == TYPE_FOOTER || type_current != TYPE_NORMAL
                || (holder.getItemProperty() != null && holder.getItemProperty().isSpan())) {
            if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder
                        .itemView.getLayoutParams();
                params.setFullSpan(true);
            }
        }
        if (type_current == TYPE_NORMAL)
            addAnimation(holder, type);//设置动画
    }


    protected Object getDataObj(int position) {
        Object data;
        if (hasHeader()) {
            data = mDates.get(position - 1);
        } else {
            data = mDates.get(position);
        }
        return data;
    }

    public int getHeaderCount() {
        int count = 0;
        if (mHeaderViewLinearLayout != null) {
            count = mHeaderViewLinearLayout.getChildCount();
        }
        return count;
    }

    public SmartAdapter removeAllHeaderView(){
        if(mHeaderViewLinearLayout != null){
            mHeaderViewLinearLayout.removeAllViews();
        }
        return this;
    }
    public SmartAdapter removeAllFooterView(){
        if(mFooterViewLinearLayout != null){
            mFooterViewLinearLayout.removeAllViews();
        }
        return this;
    }


    public int getFooterCount() {
        return mFooterViewLinearLayout == null ? 0 : mFooterViewLinearLayout.getChildCount();
    }

    public boolean hasHeader() {
        return !(mHeaderViewLinearLayout == null || mHeaderViewLinearLayout.getChildCount() == 0);
    }

    public boolean hasFooter() {
        return !(mFooterViewLinearLayout == null || mFooterViewLinearLayout.getChildCount() == 0);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (type_current != TYPE_NORMAL) {
            count = 1;
        } else {
            if (hasHeader()) {
                count++;
            }
            if (hasFooter()) {
                count++;
            }
            count += mDates.size();
        }
        return count;
    }


    /************************************************************************************************/
    /*******************************************头脚布局*****************************************************/

    /**
     * 判断指定位置是否为头布局
     *
     * @param position
     * @return
     */
    protected boolean isHeader(int position) {
        if (position == 0 && hasHeader()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查头布局的容器
     *
     * @return
     */
    protected SmartAdapter checkHeadViewLinearLayout() {
        if (mHeaderViewLinearLayout == null) {
            mHeaderViewLinearLayout = new LinearLayout(mContext);
            mHeaderViewLinearLayout.setOrientation(LinearLayout.VERTICAL);
            mHeaderViewLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        return this;
    }


    public SmartAdapter addHeadView(@NonNull View view, boolean isToTop) {
        boolean header = hasHeader();
        checkHeadViewLinearLayout();
        mHeaderViewLinearLayout.addView(view);
        if (!header) {
            notifyItemRangeInserted(0, 1);
        }
        if (isToTop && recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }
        return this;
    }

    public SmartAdapter addHeadView(@NonNull View view) {
        return addHeadView(view, true);
    }

    public SmartAdapter removeHeadView(@NonNull int position) {
        if (position < 0)
            throw new RuntimeException("removeHeadView position must more than 0");
        if (hasHeader() && mHeaderViewLinearLayout.getChildCount() > position) {
            mHeaderViewLinearLayout.removeViewAt(position);
            if (!hasHeader()) {
                notifyItemRangeRemoved(0, 1);
            }
        }
        return this;
    }

    public SmartAdapter removeHeadView(@NonNull View view) {
        if (hasHeader()) {
            mHeaderViewLinearLayout.removeView(view);
            if (!hasHeader()) {
                notifyItemRangeRemoved(0, 1);
            }
        }
        return this;
    }

    public SmartAdapter setHeadViewClickLister(View.OnClickListener onClickListener) {
        checkItemBindHelper();
        itemBindHelper.setHeadViewClickLister(onClickListener);
        return this;
    }


    /**
     * 判断指定位置是否为脚布局
     *
     * @param position
     * @return
     */
    public boolean isFooter(int position) {
        position = getRealPosition(position);
        return position >= mDates.size();
    }

    /**
     * 检查头布局的容器
     *
     * @return
     */
    protected SmartAdapter checkFooterViewLinearLayout() {
        if (mFooterViewLinearLayout == null) {
            mFooterViewLinearLayout = new LinearLayout(mContext);
            mFooterViewLinearLayout.setOrientation(LinearLayout.VERTICAL);
            mFooterViewLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        return this;
    }


    public SmartAdapter addFooterView(@NonNull View view) {
        checkFooterViewLinearLayout();
        mFooterViewLinearLayout.addView(view);
        return this;
    }


    public SmartAdapter removeFooterView(int position) {
        if (position < 0)
            throw new RuntimeException("removeFooterView position must more than 0");
        if (hasFooter() && mFooterViewLinearLayout.getChildCount() > position) {
            mFooterViewLinearLayout.removeViewAt(position);
            if (!hasFooter()) {
                if (hasHeader())
                    notifyItemRemoved(mDates.size() + 1);
                else
                    notifyItemRemoved(mDates.size());
            }
        }
        return this;
    }

    public SmartAdapter removeFooterView(@NonNull View view) {
        if (hasFooter()) {
            mHeaderViewLinearLayout.removeView(view);
            if (!hasFooter()) {
                if (hasHeader())
                    notifyItemRemoved(mDates.size() + 1);
                else
                    notifyItemRemoved(mDates.size());
            }
        }
        return this;
    }

    public SmartAdapter setFooterViewClickLister(View.OnClickListener onClickListener) {
        checkItemBindHelper();
        itemBindHelper.setFooterViewClickLister(onClickListener);
        return this;
    }

    /**********************************************************************************************/
    public void setItemBindHelper(@NonNull ItemBindHelper itemBindHelper) {
        if (itemBindHelper == null) {
            throw new RuntimeException("itemBindHelper can't  null");
        }
        this.itemBindHelper = itemBindHelper;
    }

    protected void checkItemBindHelper() {
        if (itemBindHelper == null) {
            itemBindHelper = new ItemBindHelper();
        }
    }

    public SmartAdapter setHeaderViewClickLister(int viewId, @NonNull View.OnClickListener listener) {
        checkItemBindHelper();
        itemBindHelper.setHeaderViewClickLister(viewId, listener);
        return this;
    }

    public SmartAdapter setFooterViewClickLister(int viewId, @NonNull View.OnClickListener listener) {
        checkItemBindHelper();
        itemBindHelper.setFooterViewClickLister(viewId, listener);
        return this;
    }


    /**************************************注册帮定事件****************************************************/
    /******************************************************************************************/
    public SmartAdapter register(int layoutId) {
        return register(null, layoutId, -1, false, null, null, null);
    }

    public <T> SmartAdapter register(IBindDataView<T> iBindDataView) {
        return register(null, 0, -1, false, null, null, iBindDataView);

    }


    public <T> SmartAdapter register(int layoutId, IBindDataView<T> iBindDataView) {
        return register(null, layoutId, -1, false, null, null, iBindDataView);
    }

    public SmartAdapter register(int layoutId, int brId) {
        return register(null, layoutId, brId, false, null, null, null);
    }


    public <T> SmartAdapter register(int layoutId, ItemClickListener<T> itemClickListener, IBindDataView<T> iBindDataView) {
        return register(null, layoutId, -1, false, null, itemClickListener, iBindDataView);
    }

    public <T> SmartAdapter register(int layoutId, ItemClickListener<T> itemClickListener) {
        return register(null, layoutId, -1, false, null, itemClickListener, null);
    }

    public <T> SmartAdapter register(int layoutId, int brId, ItemClickListener<T> itemClickListener) {
        return register(null, layoutId, brId, false, null, itemClickListener, null);
    }

    public <T> SmartAdapter register(int layoutId, IBindDataView<T> iBindDataView, ItemClickListener<T> itemClickListener) {
        return register(null, layoutId, -1, false, null, itemClickListener, iBindDataView);
    }

    public <T> SmartAdapter register(Class<T> clazz, int layoutRes) {
        return register(clazz, layoutRes, -1, false, null, null, null);
    }

    public <T> SmartAdapter register(Class<T> clazz, int layoutRes, IBindDataView<T> iBindDataView) {
        return register(clazz, layoutRes, -1, false, null, null, iBindDataView);
    }

    public <J> SmartAdapter register(Class clazz, int layoutRes, ItemClickListener<J> itemClickListener) {
        return register(clazz, layoutRes, -1, false, null, itemClickListener, null);
    }


    public <J> SmartAdapter register(Class clazz, int layoutRes, IBindDataView<J> iBindDataView, ItemClickListener<J> itemClickListener) {
        return register(clazz, layoutRes, -1, false, null, itemClickListener, iBindDataView);
    }


    public SmartAdapter register(Class clazz, int layoutRes, boolean isSpan) {
        return register(clazz, layoutRes, -1, isSpan, null, null, null);
    }

    public <T> SmartAdapter register(Class clazz, int layoutRes, boolean isSpan, IBindDataView<T> iBindDataView) {
        return register(clazz, layoutRes, -1, isSpan, null, null, iBindDataView);
    }


    public <J> SmartAdapter register(Class clazz, int layoutRes, boolean isSpan, IBindDataView iBindDataView, ItemClickListener<J> itemClickListener) {
        return register(clazz, layoutRes, -1, isSpan, null, itemClickListener, iBindDataView);
    }

    public SmartAdapter register(Class clazz, int layoutRes, boolean isSpan, String baseSmartViewHolderName) {
        return register(clazz, layoutRes, -1, isSpan, baseSmartViewHolderName, null, null);
    }

    public <T> SmartAdapter register(Class clazz, int layoutRes, boolean isSpan, String baseSmartViewHolderName, IBindDataView<T> iBindDataView) {
        return register(clazz, layoutRes, -1, isSpan, baseSmartViewHolderName, null, iBindDataView);
    }


    public <J> SmartAdapter register(Class clazz, int layoutRes, boolean isSpan, String baseSmartViewHolderName, ItemClickListener<J> itemClickListener) {
        return register(clazz, layoutRes, -1, isSpan, baseSmartViewHolderName, itemClickListener, null);
    }

    public SmartAdapter register(Class clazz, int layoutRes, int dataBrId) {
        return register(clazz, layoutRes, dataBrId, false, null, null, null);
    }

    public <T> SmartAdapter register(Class clazz, int layoutRes, int dataBrId, ItemClickListener<T> itemClickListener) {
        return register(clazz, layoutRes, dataBrId, false, null, itemClickListener, null);
    }

    public SmartAdapter register(Class clazz, int layoutRes, int dataBrId, boolean isSpan) {
        return register(clazz, layoutRes, dataBrId, isSpan, null, null, null);
    }


    public <J> SmartAdapter register(Class clazz, int layoutRes, int dataBrId, boolean isSpan, ItemClickListener<J> itemClickListener) {
        return register(clazz, layoutRes, dataBrId, isSpan, null, itemClickListener, null);
    }


    public <J> SmartAdapter register(Class clazz, int layoutRes, int dataBrId, boolean isSpan, String viewHolderName, ItemClickListener<J> itemClickListener, IBindDataView iBindDataView) {
        checkItemBindHelper();
        itemBindHelper.register(clazz, layoutRes, isSpan, dataBrId, viewHolderName, itemClickListener, iBindDataView);
        return this;
    }


    /******************************************************************************************/
    public SmartAdapter<VH> addData(List dataList) {
        if (dataList != null && !dataList.isEmpty()) {
            mDates.addAll(dataList);
            notifyDataSetChanged();
        }

        return this;
    }

    public SmartAdapter<VH> addDataWithExpand(List dataList) {
        if (dataList != null && !dataList.isEmpty()) {
            mDates.addAll(dataList);
            expandAll();
        }

        return this;
    }

    public <T> SmartAdapter setItemClickListener(ItemClickListener<T> itemClickListener) {
        this.itemClickListener = itemClickListener;
        return this;
    }

    public <T> SmartAdapter addItemClickListenerChild(int id, ItemClickListener<T> itemClickListener) {
        if (itemClickListener != null) {
            if (itemEveryViewClickListenerList == null) {
                itemEveryViewClickListenerList = new SparseArray<>();
            }
            itemEveryViewClickListenerList.put(id, itemClickListener);
        }
        return this;
    }

    public SmartAdapter addHeaderClickListenerChild(int id, View.OnClickListener itemClickListener) {
        if (itemClickListener != null) {
            if (itemHeaderChildListenerList == null) {
                itemHeaderChildListenerList = new SparseArray<>();
            }
            itemHeaderChildListenerList.put(id, itemClickListener);
        }
        return this;
    }

    public SmartAdapter addFooterClickListenerChild(int id, View.OnClickListener itemClickListener) {
        if (itemClickListener != null) {
            if (itemFooterChildListenerList == null) {
                itemFooterChildListenerList = new SparseArray<>();
            }
            itemFooterChildListenerList.put(id, itemClickListener);
        }
        return this;
    }


    /*********************************************数据更新*******************************************************/


    public Object getAdapterPostionData(Integer adapterposition) {
        if (hasHeader()) {
            adapterposition--;
        }
        return mDates.size() >= adapterposition || adapterposition < 0 ? null : mDates.get(adapterposition);

    }


    public List<Object> getDates() {
        return mDates;
    }


    protected boolean checkLastStateNormal(boolean notify, boolean changeToNormal) {
        boolean state = type_current == TYPE_NORMAL;
        if (changeToNormal && !state) {
            type_current = TYPE_NORMAL;
        }
        if (notify && !state) {
            notifyDataSetChanged();
        }
        return state;
    }

    /**
     * 设置差量的更新回调
     *
     * @param diffCallBack
     * @return
     */
    public SmartAdapter<VH> setDiffCallBack(IDiffCallBack diffCallBack) {
        this.mDiffCallBack = diffCallBack;
        return this;
    }


    /**
     * 清除原有的数据，并加新的数据
     *
     * @param data
     * @return
     */
    public SmartAdapter clearDataAndData(List data) {
        checkLastStateNormal(false, true);
        if (data == null || data.isEmpty()) {
            mDates.clear();
            notifyDataSetChanged();
        } else {
            mDates.clear();
            mDates.addAll(data);
            notifyDataSetChanged();
        }
        return this;
    }

    /**
     * 在顶部添加数据
     *
     * @param data
     * @return
     */
    public SmartAdapter addDataToTop(List data) {
        if (data != null && !data.isEmpty()) {
            mDates.addAll(0, data);
            if (checkLastStateNormal(true, true)) {
                return this;
            }
            if (hasHeader()) {
                notifyItemRangeInserted(1, data.size());
            } else {
                notifyItemRangeInserted(0, data.size());
            }
        }
        return this;
    }

    /**
     * 在底部添加数据
     *
     * @param data
     * @return
     */
    public SmartAdapter addDataToBottom(List data) {
        if (data != null && !data.isEmpty()) {
            mDates.addAll(data);
            if (!checkLastStateNormal(true, true)) {
                return this;
            }
            if (hasHeader()) {
                notifyItemRangeInserted(mDates.size() + 1, data.size());
            } else {
                notifyItemRangeInserted(mDates.size(), data.size());
            }
        }
        return this;
    }


    /**
     * 差量更新{@link #setDiffCallBack}
     *
     * @param newData
     * @return
     */
    public SmartAdapter<VH> diffUpdate(final List newData) {
        if (newData == null || newData.size() == 0)
            return this;
        checkLastStateNormal(true, true);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mDates.size();
            }

            @Override
            public int getNewListSize() {
                return newData.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPosition, int newPosition) {
                if (mDiffCallBack == null) {
                    return false;
                } else {
                    return mDiffCallBack.areItemsTheSame(mDates.get(oldPosition), newData.get(newPosition));
                }
            }

            @Override
            public boolean areContentsTheSame(int oldPosition, int newPosition) {
                if (mDiffCallBack == null) {
                    return false;
                } else {
                    return mDiffCallBack.areContentsTheSame(mDates.get(oldPosition), newData.get(newPosition));
                }
            }
        });
        diffResult.dispatchUpdatesTo(this);
        mDates = newData;
        return this;
    }


    /**
     * 异步差量更新{@link #setDiffCallBack}
     *
     * @param newData
     */
    @SuppressLint("StaticFieldLeak")
    public SmartAdapter asyncDiffUpdate(final List newData) {
        if (newData == null || newData.size() == 0) {
            mDates.clear();
            notifyDataSetChanged();
            return this;
        }
        checkLastStateNormal(false, true);
        new AsyncTask<List, Integer, DiffUtil.DiffResult>() {
            @Override
            protected DiffUtil.DiffResult doInBackground(List... lists) {
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return mDates.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return newData.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldPosition, int newPosition) {
                        if (mDiffCallBack == null) {
                            return false;
                        } else {
                            return mDiffCallBack.areItemsTheSame(mDates.get(oldPosition), newData.get(newPosition));
                        }
                    }

                    @Override
                    public boolean areContentsTheSame(int oldPosition, int newPosition) {
                        if (mDiffCallBack == null) {
                            return false;
                        } else {
                            return mDiffCallBack.areContentsTheSame(mDates.get(oldPosition), newData.get(newPosition));
                        }
                    }
                });
                return diffResult;
            }

            @Override
            protected void onPostExecute(DiffUtil.DiffResult diffResult) {
                super.onPostExecute(diffResult);
                mDates = newData;
                diffResult.dispatchUpdatesTo(SmartAdapter.this);
            }
        }.execute(newData);
        return this;
    }


    /****************************************************************************************************/

    /************************************************出错相关的视图********************************************************/
    //加载中的视图
    public SmartAdapter setLoadingView(int layOutId, @Nullable View.OnClickListener onClickListener) {
        checkItemBindHelper();
        itemBindHelper.setLoadingView(layOutId, onClickListener);
        return this;
    }

    //加载中的视图
    public SmartAdapter setLoadingView(int layOutId) {
        return setLoadingView(layOutId, null);
    }

    //没有网络的示图
    public SmartAdapter setNoNetView(int layoutId, View.OnClickListener onClickListener) {
        checkItemBindHelper();
        itemBindHelper.setNoNetView(layoutId, onClickListener);
        return this;
    }

    //没有网络的示图
    public SmartAdapter setNoNetView(int layoutId) {
        return setNoNetView(layoutId, null);
    }

    //没有网络的示图
    public SmartAdapter setErrorView(int layoutId, View.OnClickListener onClickListener) {
        checkItemBindHelper();
        itemBindHelper.setErrorView(layoutId, onClickListener);
        return this;
    }

    //没有网络的示图
    public SmartAdapter setErrorView(int layoutId) {
        return setErrorView(layoutId, null);
    }

    //没有数据的示图
    public SmartAdapter setEmptyView(int layoutId, View.OnClickListener onClickListener) {
        checkItemBindHelper();
        itemBindHelper.setEmptyView(layoutId, onClickListener);
        return this;
    }

    //没有数据的示图
    public SmartAdapter setEmpty(int layoutId) {
        return setEmptyView(layoutId, null);
    }

    //检查是否设置了相应的布局ID
    protected void checkView() {
        if (itemBindHelper.getTipLayoutId(type_current) == 0) {
            throw new RuntimeException("please set no normal layoutId frist");
        }
    }

    //显示正在加载的视图
    public SmartAdapter showLoadingView() {
        if (type_current == TYPE_LOADING)
            return this;
        this.type_current = TYPE_LOADING;
        checkView();
        notifyDataSetChanged();
        return this;
    }

    //显示加载错误的视图
    public SmartAdapter showErrorView() {
        if (type_current == TYPE_FAILED)
            return this;
        this.type_current = TYPE_FAILED;
        checkView();
        notifyDataSetChanged();
        return this;
    }

    //显示没有网络的视图
    public SmartAdapter showNoNetView() {
        if (type_current == TYPE_NO_NET)
            return this;
        this.type_current = TYPE_NO_NET;
        checkView();
        notifyDataSetChanged();
        return this;
    }

    //显示没有数据的视图
    public SmartAdapter showEmpty() {
        if (type_current == TYPE_EMPTY)
            return this;
        this.type_current = TYPE_EMPTY;
        checkView();
        notifyDataSetChanged();
        return this;
    }

    //显示正常的视图
    public SmartAdapter showNormalView() {
        checkLastStateNormal(true, true);
        return this;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    /**
     * 删除指定位置的数据
     *
     * @param position------item的位置
     */
    public void removePosition(int position) {
        if (hasHeader()) {
            position++;
        }
        getSelectData().remove(getDates().remove(position));
        getSelectPosition().remove((Integer) position);
        notifyItemRemoved(position);
    }


    public SmartAdapter expandAll() {
        ArrayList<Object> objects = new ArrayList<>();
        objects.addAll(mDates);
        boolean isNotify = false;
        for (Object object : objects) {
            IExpandDataInterface iExpandDataInterface = (IExpandDataInterface) object;
            iExpandDataInterface.setExpand(true);
            if (iExpandDataInterface.getChildrenCount() > 0 && !mDates.contains(iExpandDataInterface.getChildrenList())) {
                int i = mDates.indexOf(object);
                if (i < mDates.size()) {
                    mDates.addAll(i + 1, iExpandDataInterface.getChildrenList());
                } else {
                    mDates.addAll(iExpandDataInterface.getChildrenList());
                }
                isNotify = true;
            }
        }
        if (isNotify)
            notifyDataSetChanged();
        return this;
    }

    public SmartAdapter expandOrShrink(Object object) {
        if (object != null && mDates.contains(object) && object instanceof IExpandDataInterface) {
            IExpandDataInterface iExpandDataInterface = (IExpandDataInterface) object;
            if (iExpandDataInterface.isExpand()) {
                shrink(object);
            } else {
                expand(object);
            }
        }
        return this;
    }

    public SmartAdapter expand(Object object) {
        if (object == null || !(object instanceof IExpandDataInterface) || !mDates.contains(object)) {
            return this;
        } else {
            IExpandDataInterface iExpandDataInterface = (IExpandDataInterface) object;
            if (iExpandDataInterface.isExpand())
                return this;
            iExpandDataInterface.setExpand(true);
            if (iExpandDataInterface.getChildrenCount() > 0 && !mDates.contains(iExpandDataInterface.getChildrenList())) {
                int i = mDates.indexOf(object);
                mDates.addAll(i + 1, iExpandDataInterface.getChildrenList());
                notifyItemRangeInserted(i + 1, iExpandDataInterface.getChildrenCount());
            }
            return this;
        }
    }

    public SmartAdapter shrink(Object object) {
        if (object == null || !(object instanceof IExpandDataInterface) || !mDates.contains(object)) {
            return this;
        } else {
            IExpandDataInterface iExpandDataInterface = (IExpandDataInterface) object;
            if (!iExpandDataInterface.isExpand())
                return this;
            iExpandDataInterface.setExpand(false);
            if (iExpandDataInterface.getChildrenCount() > 0 && mDates.containsAll(iExpandDataInterface.getChildrenList())) {
                int i = mDates.indexOf(object);
                mDates.removeAll(iExpandDataInterface.getChildrenList());
                notifyItemRangeRemoved(i + 1, iExpandDataInterface.getChildrenCount());
            }
            return this;
        }
    }
}
