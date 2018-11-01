package github.com.smartadapter;


import github.com.smartadapter.ISmartInterface.IBindDataView;
import github.com.smartadapter.ISmartInterface.ItemClickListener;

/**
 * class的属性
 *
 * @param <T>
 */
class ItemProperty<T> {
    //自定义ViewHolder的路径，必须为全路径
    private String baseSmartViewHolderName;
    //布局文件的id
    private int layoutRes;
    //实体类的class
    private Class<T> mClazz;
    //实体类对应的BR id
    private int dataBRid = -1;
    //是否占一行
    private boolean isSpan = false;

    private ItemClickListener mItemClickListener;
    //数据的绑定
    private IBindDataView<T> mIBindDataView;


    public ItemProperty(int layoutRes, Class<T> tClass, boolean isSpan) {
        this(layoutRes, tClass, isSpan, -1, null, null, null);
    }

    public ItemProperty(int layoutRes, Class<T> tClass, boolean isSpan, int dataBRid) {
        this(layoutRes, tClass, isSpan, -1, null, null, null);
    }


    public ItemProperty(int layoutRes, Class<T> tClass, boolean isSpan, int dataBRid, String baseSmartViewHolderName) {
        this(layoutRes, tClass, isSpan, dataBRid, baseSmartViewHolderName, null, null);
    }

    public ItemProperty(int layoutRes, Class<T> clazz, boolean isSpan, int dataBRid, String baseSmartViewHolderName, ItemClickListener itemClickListener, IBindDataView iBindDataView) {
        this.layoutRes = layoutRes;
        this.mClazz = clazz;
        this.isSpan = isSpan;
        this.dataBRid = dataBRid;
        this.baseSmartViewHolderName = baseSmartViewHolderName;
        this.mItemClickListener = itemClickListener;
        this.mIBindDataView = iBindDataView;
    }


    public ItemClickListener getItemClickListener() {
        return mItemClickListener;
    }

    public int getLayoutRes() {
        return layoutRes;
    }

    public void setLayoutRes(int layoutRes) {
        this.layoutRes = layoutRes;
    }

    public Class<T> getClazz() {
        return mClazz;
    }

    public void setClazz(Class<T> mClazz) {
        this.mClazz = mClazz;
    }

    public boolean isSpan() {
        return isSpan;
    }

    public void setSpan(boolean span) {
        isSpan = span;
    }

    public int getDataBRid() {
        return dataBRid;
    }

    public String getBaseSmartViewHolderName() {
        return baseSmartViewHolderName;
    }

    public IBindDataView<T> getIBindDataView() {
        return mIBindDataView;
    }

    public void setLayoutRes(int layoutRes, boolean isSpan, int dataBrId, String viewHolderName, ItemClickListener itemClickListener, IBindDataView iBindDataView) {
        if (layoutRes != 0 && layoutRes != -1) {
            this.layoutRes = layoutRes;
        }
        this.isSpan = isSpan;
        if (dataBRid != 0 && dataBRid != -1) {
            this.dataBRid = dataBrId;
        }
        if (viewHolderName != null)
            this.baseSmartViewHolderName = viewHolderName;
        if (itemClickListener != null)
            this.mItemClickListener = itemClickListener;
        if (iBindDataView != null)
            this.mIBindDataView = iBindDataView;
    }
}
