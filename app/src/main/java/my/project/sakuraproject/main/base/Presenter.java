package my.project.sakuraproject.main.base;

import java.lang.ref.WeakReference;

public class Presenter<V> {
    //View的弱引用
    protected WeakReference<V> mViewRef;

    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public Presenter(V view) {
        //关联View
        attachView(view);
    }

    /**
     * 关联View
     *
     * @param view 需要关联的View
     */
    public void attachView(V view) {
        mViewRef = new WeakReference<>(view);
    }

    /**
     * 取消关联的View
     */
    public void detachView() {
        if (null != mViewRef) {
            mViewRef.clear();
        }
    }

    /**
     * 获取将当前关联的View
     *
     * @return 当前关联的View
     */
    public V getView() {
        if (null != mViewRef) {
            return mViewRef.get();
        }
        return null;
    }
}
