package github.com.smartadapter.ISmartInterface;

import android.view.View;

public interface IBindDataView<T> {
    void bindData(View view, T data);
}
