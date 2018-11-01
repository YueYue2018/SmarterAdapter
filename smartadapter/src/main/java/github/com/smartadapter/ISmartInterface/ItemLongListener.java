package github.com.smartadapter.ISmartInterface;

import android.view.View;

public interface ItemLongListener<T> {
    void onLongClick(View view, int position, T object);
}
