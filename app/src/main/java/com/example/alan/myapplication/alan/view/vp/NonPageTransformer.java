package com.example.alan.myapplication.alan.view.vp;

/**
 * Created by Alan on 2018/1/22.
 */

import android.support.v4.view.ViewPager;
import android.view.View;

public class NonPageTransformer implements ViewPager.PageTransformer
{
    @Override
    public void transformPage(View page, float position)
    {
        page.setScaleX(0.999f);//hack
    }

    public static final ViewPager.PageTransformer INSTANCE = new NonPageTransformer();
}
