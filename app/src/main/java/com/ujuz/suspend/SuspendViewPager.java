package com.ujuz.suspend;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.ujuz.suspend.adapter.ViewPagerAdapter;
import com.ujuz.suspend.scroll.ScrollHeaderListener;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能：实现了悬浮窗置顶效果，
 * 滑动头部可实现所有recyclerView的联动
 * 滑动当前RecyclerView可实现头部及其他RecyclerView的联动
 *
 * 使用：
 * 1、左右滑动的ViewPager需要继承ViewPagerAdapter
 * 2、recyclerView中的Adapter要使用BaseQuickAdapter,方便设置头部布局
 * 3、每个Fragment需要继承TabFragment 并赋值tabRecyclerView 或者在自己的fragment中命名 tabRecyclerView 并赋值
 * 4、在整个布局中设置头部即可 eg: SuspendViewPager 布局文件中的整个子布局是可以滑出屏幕的头部(父布局是上下结构布局)
 *   <SuspendViewPager
 *          android:id="@+id/suspendViewPager"
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent">
 *
 *          <View
 *              android:layout_width="match_parent"
 *              android:layout_height="80dp"
 *              android:background="@mipmap/shops_banner_default"
 *              android:layout_marginTop="10dp"
 *              />
 *
 *          <androidx.recyclerview.widget.RecyclerView
 *              android:id="@+id/rv_menu"
 *              android:layout_width="match_parent"
 *              android:layout_height="wrap_content"
 *              android:layout_marginTop="20dp"
 *              android:paddingLeft="20dp"
 *              android:paddingRight="20dp"
 *              app:itemBinding="@{viewModel.shopsMenuItemBinding}"
 *              app:items="@{viewModel.shopsMenuList}"
 *              app:layoutManager="@{LayoutManagers.grid(4)}"/>
 *   </SuspendViewPager>
 * 5、然后依次调用addStickHeaderView 和 setViewPagerAdapter即可
 *
 * @Link mBinding.suspendViewPager
 *                 .addStickHeaderView(stickView)
 *                 .setViewPagerAdapter(viewPagerAdapter);
 *
 * stickView : 自定义的悬浮窗试图,需要包含TabLayout 会遍历两层寻找
 * viewPagerAdapter : 设置ViewPager的Adapter
 *
 * @author ameng
 * Create on 2020-05-29 18:24
 * Link:amengnn1029@gmail.com
 */
public class SuspendViewPager extends RelativeLayout implements ViewPager.OnPageChangeListener, ScrollHeaderListener {
    private static final int VIEWPAGER_ID = 1;

    private Context mContext;

    protected ViewPager mViewPager;
    protected ViewPagerAdapter viewPagerAdapter;
    protected HeaderContainer mHeaderContainer;// 整个视图的头部
    protected View mStickHeaderView;// 悬浮的头部视图
    protected TabLayout tabLayout;
    protected List<View> mHeaderViewChildList;

    protected LinearLayoutManager currentLayoutManagers;
    protected RecyclerView currentRecyclerView;

    private int mHeaderContainerHeight;
    private int mMinHeaderTranslation;
    private int mStickHeaderViewHeight;

    // 监听头部滑动触发事件
    private int eventAction;
    private int recyclerViewTranslationY;

    private boolean isRecyclerViewScroll;

    private List<? extends BaseFragment> fragmentList;
    private List<RecyclerView> recyclerViewList = new ArrayList<>();

    private RecyclerView.OnScrollListener recyclerViewScrollListener;

    public SuspendViewPager(Context context) {
        super(context);
        initView(context);
    }

    public SuspendViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }


    private void initView(Context context) {
        mContext = context;
        mHeaderViewChildList = new ArrayList<>();

        // add viewpager
        mViewPager = new ViewPager(context);
        mViewPager.setId(VIEWPAGER_ID);
        addView(mViewPager, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // add headerContainer
        mHeaderContainer = new HeaderContainer(context);
        mHeaderContainer.setOrientation(LinearLayout.VERTICAL);
        addView(mHeaderContainer, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


        mHeaderContainer.setScrollHeaderListener(this);
        mViewPager.addOnPageChangeListener(this);

        recyclerViewScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (eventAction == MotionEvent.ACTION_MOVE || isRecyclerViewScroll) return;

                int scrollY = getScrollY(recyclerView);
//                scrollHeader(scrollY);
                scrollViewByRecyclerView(scrollY);
            }
        };
    }


    /**
     * 静态添加头部容器中的子布局
     * @param child
     * @param index
     * @param params
     */
    @Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
        // 添加mViewPager和mHeaderContainer
        if (getChildCount() < 2) {
            super.addView(child, index, params);
        } else {
            // 填充头部布局
            mHeaderContainer.addView(child, params);
            mHeaderViewChildList.add(child);
        }
    }


    /**
     * 动态添加头部容器中的子布局
     * @param view
     * @return
     */
    public SuspendViewPager addHeaderView(View view) {
        if (view != null) {
            mHeaderContainer.addView(view);
            mHeaderViewChildList.add(view);
        }
        return this;
    }

    /**
     * 添加浮顶试图,要添加在试图容器的最后一个,这里需要动态添加
     * @return
     */
    public SuspendViewPager addStickHeaderView(View stickViewVar) {

        if (stickViewVar instanceof TabLayout) {
            mStickHeaderView = stickViewVar;
            mHeaderContainer.addView(stickViewVar,mHeaderContainer.getChildCount());
            mHeaderViewChildList.add(stickViewVar);
            initStickView((TabLayout) stickViewVar);
        } else {
            try {
                TabLayout tabLayout = null;
                // 携带ViewPager的浮窗 stickView 必须为ViewGroup类型
                ViewGroup stickView = (ViewGroup) stickViewVar;
                // 只做两层遍历查找tabLayout
                outLoop:
                for (int i = 0; i < stickView.getChildCount(); i++) {
                    View view = stickView.getChildAt(i);
                    if (view instanceof TabLayout) {
                        tabLayout = (TabLayout) stickView.getChildAt(i);

                        break;
                    } else if (view instanceof ViewGroup) {
                        ViewGroup stickViewChild = (ViewGroup) view;
                        for (int j = 0; j < stickViewChild.getChildCount(); j++) {
                            if (stickViewChild.getChildAt(j) instanceof TabLayout) {
                                tabLayout = (TabLayout) stickViewChild.getChildAt(j);
                                break outLoop;
                            }
                        }
                    }
                }
                if (tabLayout != null) {
                    mStickHeaderView = stickViewVar;
                    mHeaderContainer.addView(stickViewVar,mHeaderContainer.getChildCount());
                    mHeaderViewChildList.add(stickViewVar);
                } else {
                    // 如果没有tabLayout会使用默认的
                    tabLayout = (TabLayout) LayoutInflater.from(mContext).inflate(R.layout.suspend_stick_header_layout,null,false);
                    mStickHeaderView = tabLayout;
                    mHeaderContainer.addView(tabLayout);
                    mHeaderViewChildList.add(tabLayout);
                }
                initStickView(tabLayout);
            } catch (ClassCastException e) {
                throw new ClassCastException("浮窗需要添加TabLayout");
            }
        }

        return this;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


    /**
     * ViewPager 关联 tabLayout
     * @param tabLayout
     */
    private void initStickView(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
        tabLayout.setupWithViewPager(mViewPager);
    }


    /**
     * 初始化数据
     */
    private void initHeight() {
        mHeaderContainerHeight = mHeaderContainer.getMeasuredHeight();
        mStickHeaderViewHeight = tabLayout.getMeasuredHeight();
        mMinHeaderTranslation = -mHeaderContainerHeight + mStickHeaderViewHeight;
    }


    /**
     * 设置viewPagerAdapter
     * @param adapter
     * @return
     */
    public SuspendViewPager setViewPagerAdapter(ViewPagerAdapter adapter) {
        try {
            mViewPager.setOffscreenPageLimit(adapter.getCount() - 1);

            fragmentList =  adapter.getFragmentList();
            viewPagerAdapter = adapter;
            mViewPager.setAdapter(adapter);

            mViewPager.post(()->{
                initHeight();
                addRecyclerViewPlaceHolder();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }


    /**
     * 为每个recyclerView添加头部
     * @return
     */
    public SuspendViewPager addRecyclerViewPlaceHolder() {
        try {
            // 这里需要获取recyclerView做监听
            for (int i = 0; i < fragmentList.size(); i++) {
                RecyclerView recyclerView = fragmentList.get(i).tabRecyclerView;
                recyclerViewList.add(recyclerView);

                recyclerView.addOnScrollListener(recyclerViewScrollListener);

                // placeHolder
                View view = new View(mContext);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeaderContainerHeight);
                view.setLayoutParams(layoutParams);

                BaseQuickAdapter adapter = (BaseQuickAdapter) recyclerView.getAdapter();
                adapter.addHeaderView(view);
                adapter.notifyDataSetChanged();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentRecyclerView = recyclerViewList.get(position);
        currentLayoutManagers = (LinearLayoutManager) currentRecyclerView.getLayoutManager();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    private int getScrollY(RecyclerView recyclerView) {
        View child = recyclerView.getChildAt(0);
        if (child == null) {
            return 0;
        }

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
        int top = child.getTop();

        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = mHeaderContainer.getMeasuredHeight();
        }

        return -top + firstVisiblePosition * child.getHeight() + headerHeight;
    }

    /**
     * recyclerView触发的头部布局滑动
     * @param scrollY
     */
    private void scrollHeader(int scrollY) {
        int translationY = Math.max(-scrollY, mMinHeaderTranslation);
        LayoutParams layoutParams = (LayoutParams) mHeaderContainer.getLayoutParams();
        layoutParams.topMargin = translationY;
        mHeaderContainer.setLayoutParams(layoutParams);
    }

    /**
     * 判断recyclerView 是否可滑动
     * @return 0 可向上不可往下滑 滑动 1 可向下不可往上滑动 2 可同时向下或向上滑动 其他不可滑动
     */
    private int isRecyclerScrollable() {
        if (currentRecyclerView == null) currentRecyclerView = recyclerViewList.get(mViewPager.getCurrentItem());
        if (currentLayoutManagers == null) currentLayoutManagers = (LinearLayoutManager) currentRecyclerView.getLayoutManager();

        RecyclerView.Adapter adapter = currentRecyclerView.getAdapter();
        if (currentLayoutManagers == null || adapter == null) return 3;

        boolean lastUp = currentLayoutManagers.findLastCompletelyVisibleItemPosition() < adapter.getItemCount() - 1;
        boolean fistDown = currentLayoutManagers.findFirstCompletelyVisibleItemPosition() > 0;

        if (lastUp && !fistDown) return 0;
        if (fistDown && !lastUp) return 1;
        if (lastUp && fistDown) return 2;

        return 3;
    }

    @Override
    public void onScrollListener(int scrollY) {
        if (currentRecyclerView == null) currentRecyclerView = recyclerViewList.get(mViewPager.getCurrentItem());
        recyclerViewTranslationY = mHeaderContainerHeight + (mHeaderContainer.getTop() + scrollY);

//        if (isRecyclerScrollable() != 3) scrollViewByHeader(scrollY);

        // 往上滑 可滑时
        if (scrollY < 0 && (isRecyclerScrollable() == 0 || isRecyclerScrollable() == 2)) {
            scrollViewByHeader(scrollY);
        }
        // 往下滑 可滑时
        else if (scrollY > 0 && (isRecyclerScrollable() == 1 || isRecyclerScrollable() == 2)) {
            scrollViewByHeader(scrollY);
        }
        // 往下滑 不可滑动时 归位
        else if (scrollY > 0 && isRecyclerScrollable() == 0) {
            scrollViewByHeader(-mHeaderContainer.getTop());
        }
    }

    /**
     * 滑动头部布局
     * @param scrollY
     */
    public void scrollHeaderLayoutParams(int scrollY) {
        LayoutParams layoutParams = (LayoutParams) mHeaderContainer.getLayoutParams();
        layoutParams.topMargin = mHeaderContainer.getTop() + scrollY;
        mHeaderContainer.setLayoutParams(layoutParams);
    }


    /**
     * 滑动头部时联动
     * @param scrollY
     */
    private void scrollViewByHeader(int scrollY) {
//        KLog.v("by header scrollY:" + scrollY + " mHeaderContainer.getTop:" + mHeaderContainer.getTop() + " mHeaderContainerHeight:" + mHeaderContainerHeight + " recyclerViewTranslationY:" +recyclerViewTranslationY);
        scrollHeaderLayoutParams(scrollY);
        for (int i = 0; i < recyclerViewList.size(); i++) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewList.get(i).getLayoutManager();
            layoutManager.scrollToPositionWithOffset(1,recyclerViewTranslationY);
        }
    }

    /**
     * 滑动recyclerView时联动
     */
    private void scrollViewByRecyclerView(int scrollY) {
        recyclerViewTranslationY = mHeaderContainerHeight - scrollY;
        isRecyclerViewScroll = true;
//        KLog.v("by recyclerView scrollY:" + scrollY + " mHeaderContainer.getTop:" + mHeaderContainer.getTop() + " mHeaderContainerHeight:" + mHeaderContainerHeight + " recyclerViewTranslationY:" + recyclerViewTranslationY);
        scrollHeader(scrollY);
        for (int i = 0; i < recyclerViewList.size(); i++) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewList.get(i).getLayoutManager();
            if (layoutManager != currentLayoutManagers)
                layoutManager.scrollToPositionWithOffset(1,recyclerViewTranslationY);
        }
        isRecyclerViewScroll = false;
    }

    @Override
    public void setMotionEventAction(int action) {
        eventAction = action;
    }
}
