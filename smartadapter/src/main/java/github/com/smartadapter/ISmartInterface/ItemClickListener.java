package github.com.smartadapter.ISmartInterface;

import android.view.View;

public interface ItemClickListener<T> {
    void onClick(View view, int position, T object);
}
