package github.com.smartadapter.ISmartInterface;

import java.util.List;

/**
 * 分组或者折叠实现的接口
 */
public interface IExpandDataInterface<T>  {

    /**
     * 是否展开
     * @return
     */
     boolean isExpand();

     void setExpand(boolean expand);

    /**
     * 孩子的集合
     * @return
     */
     List<T> getChildrenList();

    /**
     * 孩子的个数
     * @return
     */
    int getChildrenCount();
}
