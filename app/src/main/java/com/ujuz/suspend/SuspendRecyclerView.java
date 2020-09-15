package com.ujuz.suspend;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

/**
 * @author ameng
 * Create on 2020-06-01 10:30
 * Link:amengnn1029@gmail.com
 */
public class SuspendRecyclerView extends RecyclerView {
    protected BaseQuickAdapter rvAdapter;


    public SuspendRecyclerView(@NonNull Context context) {
        super(context);
    }

    public SuspendRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SuspendRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


//    @Nullable
//    @Override
//    public Adapter getAdapter() {
////        if (rvAdapter == null) new BaseQuickAdapter<>()
//
//        return super.getAdapter();
//    }
//




}
