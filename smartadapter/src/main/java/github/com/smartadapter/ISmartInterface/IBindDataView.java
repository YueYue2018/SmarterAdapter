package github.com.smartadapter.ISmartInterface;

import android.support.annotation.NonNull;
import android.view.View;
import github.com.smartadapter.BaseSmartViewHolder;
import github.com.smartadapter.SmartViewHolder;

public interface IBindDataView<T> {
    void bindData(@NonNull BaseSmartViewHolder smartViewHolder, @NonNull View view, T data);
}
