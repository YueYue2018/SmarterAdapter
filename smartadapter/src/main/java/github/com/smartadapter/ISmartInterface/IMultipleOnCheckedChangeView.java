package github.com.smartadapter.ISmartInterface;

import java.util.List;

//多选的回调
public interface IMultipleOnCheckedChangeView<T> {
    void onCheckedChanged(List<Integer> selectPositionList, List<T> selectDataList);
}
