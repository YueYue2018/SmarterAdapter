package github.com.smartadapter.ISmartInterface;

/**
 * 差量刷新的回调
 *
 * @param <T>
 */
public interface IDiffCallBack<T> {
    boolean areItemsTheSame(T oldObj, T newObj);

    boolean areContentsTheSame(T oldObj, T newObj);
}
