package com.music.kotlinqq.base.fragment;

import android.view.View;
import com.music.kotlinqq.base.presenter.IPresenter;
import org.jetbrains.annotations.NotNull;

/**
 * @author cyl
 * @date 2020/9/21
 */
public abstract class BaseMvpFragment<T extends IPresenter> extends BaseFragment {

    protected abstract T getPresenter();
    protected T mPresenter;

    @Override
    protected void initView() {
        mPresenter = getPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void onDestroy() {
        if (mPresenter != null){
            mPresenter.detachView();
            mPresenter = null;
        }
        super.onDestroy();
    }

    @Override
    protected void initView(@NotNull View view) {

    }
}
