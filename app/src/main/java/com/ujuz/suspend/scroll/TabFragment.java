package com.ujuz.suspend.scroll;

import androidx.fragment.app.Fragment;

import com.ameng.suspend.SuspendRecyclerView;

/**
 * @author ameng
 * Create on 2020-05-29 21:06
 * Link:amengnn1029@gmail.com
 */
public abstract class TabFragment extends Fragment {

    /**
     * 这里需要赋值 recyclerView
     */
    public SuspendRecyclerView tabRecyclerView;


    /**
     * 这里需要赋值 recyclerView
     * @param tabRecyclerView
     */
    protected void setTabRecyclerView(SuspendRecyclerView tabRecyclerView) {
        this.tabRecyclerView = tabRecyclerView;
    }
}
