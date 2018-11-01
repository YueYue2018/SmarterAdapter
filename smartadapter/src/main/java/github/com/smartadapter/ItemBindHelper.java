package github.com.smartadapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import github.com.smartadapter.ISmartInterface.IBindDataView;
import github.com.smartadapter.ISmartInterface.ItemClickListener;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ItemBindHelper {
    private HashMap<Integer, View.OnClickListener> headerViewClickLister;
    private HashMap<Integer, View.OnClickListener> footerViewClickLister;
    private List<ItemProperty> listItemBindHelper = null;
    private View.OnClickListener mHeadViewClickLister = null;
    private View.OnClickListener mFooterViewClickLister = null;
    private ItemProperty mCurrentItemProperty = null;
    private ItemProperty mDefaultItemProperty = null;
    private Object mSourceData;//数据源
    //加载失败的布局
    private int layOutErrorId;
    //加载中的布局
    private int layOutLoadingId;
    //没有网络的布局
    private int layOutNoNetId;
    //加载失败点击事件
    private View.OnClickListener listenerError;
    //加载中的点击事件
    private View.OnClickListener listenerLoading;
    //没有网络的点击事件
    private View.OnClickListener listenerNoNet;
    //没有数据的点击事件
    private View.OnClickListener listenerEmpty;
    //没有数据的布局
    private int layOutEmpty;

    /**************************************头部的相关操作****************************************************/
    //设置头部相关view的点击事件
    public ItemBindHelper setHeaderViewClickLister(int viewId, @NonNull View.OnClickListener listener) {
        if (listener == null) {
            return this;
        }
        if (headerViewClickLister == null) {
            headerViewClickLister = new HashMap<>();
        }
        headerViewClickLister.put(viewId, listener);
        return this;
    }

    //设置头部item的点击事件
    public HashMap<Integer, View.OnClickListener> getHeaderViewClickLister() {
        return headerViewClickLister;
    }

    //得到头部可点击view的集合
    public Set<Integer> getHeaderViewListenerID() {
        if (headerViewClickLister == null || headerViewClickLister.isEmpty()) {
            return null;
        } else {

            return headerViewClickLister.keySet();
        }
    }

    public void setHeadViewClickLister(View.OnClickListener onClickListener) {
        this.mHeadViewClickLister = onClickListener;
    }

    public View.OnClickListener getHeadViewClickLister() {
        return mHeadViewClickLister;
    }

    public View.OnClickListener getFooterViewClickLister() {
        return mFooterViewClickLister;
    }

    public void setFooterViewClickLister(View.OnClickListener mFooterViewClickLister) {
        this.mFooterViewClickLister = mFooterViewClickLister;
    }

    /********************************************************************************************************/


    /**************************************尾部的相关操作****************************************************/
    //设置尾部相关view的点击事件
    public ItemBindHelper setFooterViewClickLister(int viewId, @NonNull View.OnClickListener listener) {
        if (listener == null) {
            return this;
        }
        if (footerViewClickLister == null) {
            this.footerViewClickLister = new HashMap<>();
        }

        footerViewClickLister.put(viewId, listener);
        return this;
    }

    //得到尾部可点击view的事件
    public View.OnClickListener getHeaderViewListener(int rId) {
        if (headerViewClickLister == null || headerViewClickLister.isEmpty()) {
            return null;
        } else {
            return headerViewClickLister.get(rId);
        }
    }

    public View.OnClickListener getFooterViewClickLister(int rId) {
        if (headerViewClickLister == null || headerViewClickLister.isEmpty()) {
            return null;
        } else {
            return footerViewClickLister.get(rId);
        }
    }
    /********************************************************************************************************/

    /************************************************布局与Class文件绑定********************************************************/

    public <T> ItemBindHelper register(Class<T> clazz, int layoutRes, boolean isSpan, int dataBrId, String viewHolderName, ItemClickListener itemClickListener, IBindDataView iBindDataView) {
        if (listItemBindHelper == null) {
            listItemBindHelper = new ArrayList<>();
        }
        ItemProperty<T> itemProperty = null;
        if (clazz == null ) {
            if(mDefaultItemProperty == null){
                itemProperty = new ItemProperty(layoutRes, clazz, isSpan, dataBrId, viewHolderName, itemClickListener, iBindDataView);
                mDefaultItemProperty = itemProperty;
            }
            mDefaultItemProperty.setLayoutRes(layoutRes,isSpan,dataBrId,viewHolderName,itemClickListener,iBindDataView);
        } else {
            itemProperty = new ItemProperty(layoutRes, clazz, isSpan, dataBrId, viewHolderName, itemClickListener, iBindDataView);
        }
        if (itemProperty != null)
            listItemBindHelper.add(itemProperty);

        return this;
    }


    /************************************************出错相关的视图设置********************************************************/
    //加载中的视图
    public ItemBindHelper setLoadingView(int layOutId, @Nullable View.OnClickListener onClickListener) {
        this.layOutLoadingId = layOutId;
        this.listenerLoading = onClickListener;
        return this;
    }

    //加载中的视图
    public ItemBindHelper setLoadingView(int layOutId) {
        return setLoadingView(layOutId, null);
    }

    //没有网络的示图
    public ItemBindHelper setNoNetView(int layoutId, View.OnClickListener onClickListener) {
        this.layOutNoNetId = layoutId;
        this.listenerNoNet = onClickListener;
        return this;
    }

    //没有网络的示图
    public ItemBindHelper setNoNetView(int layoutId) {
        return setNoNetView(layoutId, null);
    }

    //加载出错的示图
    public ItemBindHelper setErrorView(int layoutId, View.OnClickListener onClickListener) {
        this.layOutErrorId = layoutId;
        this.listenerError = onClickListener;
        return this;
    }

    //加载出错的示图
    public ItemBindHelper setErrorView(int layoutId) {
        return setErrorView(layoutId, null);
    }

    //没有数据
    public ItemBindHelper setEmptyView(int layoutId) {
        return setEmptyView(layoutId, null);
    }


    //没有数据
    public ItemBindHelper setEmptyView(int layoutId, View.OnClickListener onClickListener) {
        this.layOutEmpty = layoutId;
        this.listenerEmpty = onClickListener;
        return this;
    }

    /********************************************************************************************************/
    public boolean isRegisterObj(Object object) {
        this.mSourceData = object;
        mCurrentItemProperty = mDefaultItemProperty;
        if (object == null || listItemBindHelper == null || listItemBindHelper.isEmpty()) {
            return false;
        } else {
            for (ItemProperty itemProperty : listItemBindHelper) {
                if (itemProperty.getClazz() != null && itemProperty.getClazz().isInstance(object)) {
                    mCurrentItemProperty = itemProperty;
                    return true;
                }
            }
            return false;
        }
    }


    public int getDataBRID(Object data) {
        for (ItemProperty itemProperty : listItemBindHelper) {
            if (itemProperty.getClazz().isInstance(data)) {
                return itemProperty.getDataBRid();
            }
        }
        throw new RuntimeException();
    }

    public ItemProperty getCurrentItemProperty() {

        return mCurrentItemProperty;
    }

    public ItemProperty getItemProperty(Object object) {
        this.mSourceData = object;
        if (object == null || listItemBindHelper == null || listItemBindHelper.isEmpty()) {
            return null;
        } else {
            for (ItemProperty itemProperty : listItemBindHelper) {
                if (itemProperty.getClazz() != null && itemProperty.getClazz().isInstance(object)) {
                    return itemProperty;
                }
            }
            return null;
        }
    }
    /********************************************************************************************************/

    /********************************************************************************************************/
    protected boolean bindData(BaseSmartViewHolder smartViewHolder, int realPosition, SmartAdapter smartAdapter) {
        boolean result;
        if (smartViewHolder == null) {
            throw new RuntimeException("SmartViewHolder not null at ItemBindHelper.bindData");
        }
        if (mSourceData == null) {
            result = true;
        } else if (mCurrentItemProperty == null || mCurrentItemProperty.getLayoutRes() == 0) {
            result = false;
        } else {
            smartViewHolder.bindData(mCurrentItemProperty.getDataBRid(), mSourceData, mCurrentItemProperty.getIBindDataView());
            result = true;
        }
        mCurrentItemProperty = null;

        return result;
    }

    public Object getSourceData() {
        return mSourceData;
    }
/********************************************************************************************************/
    /********************************************************************************************************/

    public boolean hastCustomSmartViewHolder() {
        boolean isHas = false;
        if (mCurrentItemProperty != null && mCurrentItemProperty.getBaseSmartViewHolderName() != null) {
            isHas = true;
        }
        return isHas;
    }

    public String getCustomSmartViewHolderName() {
        if (hastCustomSmartViewHolder())
            return mCurrentItemProperty.getBaseSmartViewHolderName();
        else
            return null;
    }

    //根据创建自定义的ViewHolder
    public BaseSmartViewHolder getBaseSmartViewHolder(View view) {
        if (listItemBindHelper == null || listItemBindHelper.isEmpty()) {
            return null;
        } else {
            String smartViewHolderName = mCurrentItemProperty.getBaseSmartViewHolderName();
            if (smartViewHolderName == null) {
                return null;
            }
            try {
                Class<?> clazz = Class.forName(smartViewHolderName);
                //根据参数类型获取相应的构造函数
                Class<?>[] parameterTypes = {View.class};
                Constructor constructor = clazz.getConstructor(parameterTypes);
                //参数数组
                Object[] parameters = {view};
                //根据获取的构造函数和参数，创建实例
                BaseSmartViewHolder o = (BaseSmartViewHolder) constructor.newInstance(parameters);
                return o;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //得到布局ID
    public int getItemLayoutId() {
        if (mCurrentItemProperty != null) {
            return mCurrentItemProperty.getLayoutRes();
        } else
            return 0;
    }

    public int getTipLayoutId(int type) {
        int layoutId;
        switch (type) {
            case SmartAdapter.TYPE_FAILED:
                layoutId = layOutErrorId;
                break;
            case SmartAdapter.TYPE_LOADING:
                layoutId = layOutLoadingId;
                break;
            case SmartAdapter.TYPE_NO_NET:
                layoutId = layOutNoNetId;
                break;
            case SmartAdapter.TYPE_EMPTY:
                layoutId = layOutEmpty;
                break;
            default:
                layoutId = getItemLayoutId();
        }

        return layoutId;
    }

    public <VH extends BaseSmartViewHolder> void bindDataNotNormalView(VH vh, int type) {
        View.OnClickListener onClickListener;
        switch (type) {
            case SmartAdapter.TYPE_FAILED:
                onClickListener = listenerError;
                break;
            case SmartAdapter.TYPE_LOADING:
                onClickListener = listenerLoading;
                break;
            case SmartAdapter.TYPE_NO_NET:
                onClickListener = listenerNoNet;
                break;
            case SmartAdapter.TYPE_EMPTY:
                onClickListener = listenerEmpty;
                break;
            default:
                onClickListener = null;
        }
        if (onClickListener != null && vh.itemView != null)
            vh.itemView.setOnClickListener(onClickListener);
    }
}
