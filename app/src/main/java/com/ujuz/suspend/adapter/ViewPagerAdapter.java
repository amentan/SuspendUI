package com.ujuz.suspend.adapter;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ujuz.suspend.BaseFragment;

import java.util.List;

/**
 * Created by ameng
 * 左右切换fragment的viewPager的适配器
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<? extends BaseFragment> list;
    private List<String> title;

    public ViewPagerAdapter(FragmentManager fm, List<? extends BaseFragment> list, List<String> title) {
        super(fm);
        this.list = list;
        this.title = title;
    }

    public List<? extends BaseFragment> getFragmentList() {
        return list;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title.get(position);
    }

}
