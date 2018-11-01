package github.com.smarteradapter

import android.databinding.BaseObservable
import android.databinding.Bindable


class Person : BaseObservable() {
    @Bindable
    var name: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }
    @Bindable
    var address: String = ""
    set(value) {
        field = value
        notifyPropertyChanged(BR.address)
    }
}