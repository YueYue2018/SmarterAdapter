package github.com.smartadapter.ISmartInterface;

import android.view.View;
import github.com.smartadapter.BaseSmartViewHolder;


/**
 * 视图选中没有选中的变化
 * @param <T>
 */
public interface IOnCheckedChangeView<T> {
    //选中的视图
    void onCheckedViewChanged(BaseSmartViewHolder holder, View view, T obj, int position);
    //取消选中的视图
    void onCancelCheckViewChanged(BaseSmartViewHolder holder, View view, T obj, int position);
}
