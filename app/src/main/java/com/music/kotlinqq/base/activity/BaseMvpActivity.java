package com.music.kotlinqq.base.activity;

import com.music.kotlinqq.base.presenter.IPresenter;

/**
 * @author cyl
 * @date 2020/9/21
 */
public abstract class BaseMvpActivity<T extends IPresenter> extends BaseActivity {

    protected abstract T getPresenter();
    protected T mPresenter;

    @Override
    protected void initView() {
        mPresenter = getPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null){
            mPresenter.detachView();
            mPresenter = null;
        }
    }
}
