package github.com.smartadapter.ISmartInterface;

import android.view.View;


public interface ISingleOnCheckedChangeListener<T> {
    void onCheckedChanged(View oldView, T oldObj, View newView, T newObj, boolean isChecked);
}
