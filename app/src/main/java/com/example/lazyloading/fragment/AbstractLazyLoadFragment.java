package com.example.lazyloading.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * 实现懒加载：
 * 继承这个类，重写onLazyLoadData方法，在onLazyLoadData方法里加载数据就可以
 * Create by 陈健宇 at 2018/7/15
 */
public abstract class AbstractLazyLoadFragment extends Fragment {

    private boolean isViewCreated = false;//布局是否被创建
    private boolean isLoadData = false;//数据是否加载
    private boolean isFirstVisible = true;//是否第一次可见

    abstract protected void onLazyLoadData();//子类实现这个方法，加载数据

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        isViewCreated = true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(isFragmentVisible(this) && this.isAdded()){
            if (this.getParentFragment() == null || isFragmentVisible(this.getParentFragment())) {
                onLazyLoadData();
                isLoadData = true;
                if(isFirstVisible)
                    isFirstVisible = false;
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isFragmentVisible(this) && !isLoadData && isViewCreated && this.isAdded()){
            onLazyLoadData();
            isLoadData = true;
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //onHiddenChanged调用在Resumed之前，所以此时可能fragment被add, 但还没resumed
        if(!hidden && !this.isResumed())
            return;
        //使用hide和show时，fragment的所有生命周期方法都不会调用，除了onHiddenChanged（）
        if(!hidden && isFirstVisible && this.isAdded()){
            onLazyLoadData();
            isFirstVisible = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewCreated = false;
        isLoadData = false;
        isFirstVisible = true;
    }

    /**
     * 当前Fragment是否对用户是否可见
     * @param fragment 要判断的fragment
     * @return true表示对用户可见
     */
    private boolean isFragmentVisible(Fragment fragment) {
        return !fragment.isHidden() && fragment.getUserVisibleHint();
    }
}
