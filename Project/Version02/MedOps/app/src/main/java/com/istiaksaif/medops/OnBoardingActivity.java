package com.istiaksaif.medops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.istiaksaif.medops.Activity.LogInActivity;

public class OnBoardingActivity extends AppCompatActivity {
    public Boolean checkFirstTime;
    TextView[] dots;
    LinearLayout mDotLayout;
    ViewPager mSLideViewPager;
    TextView nextbtn;
    public SharedPreferences sharedPreferences;
    TextView skipbtn;
    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onPageSelected(int position) {
            OnBoardingActivity.this.setUpindicator(position);
        }

        public void onPageScrollStateChanged(int state) {
        }
    };
//    ViewPagerAdapter viewPagerAdapter;

    public void onCreate(Bundle savedInstanceState) {
        OnBoardingActivity.super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
        SharedPreferences sharedPreferences2 = getSharedPreferences("CheckFirstTime", 0);
        this.sharedPreferences = sharedPreferences2;
        this.checkFirstTime = Boolean.valueOf(sharedPreferences2.getBoolean("CheckFirstTime", true));
//        this.nextbtn = (TextView) findViewById(R.id.nextbtn);
//        this.skipbtn = (TextView) findViewById(R.id.skipButton);
        if (this.checkFirstTime.booleanValue()) {
            this.nextbtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (OnBoardingActivity.this.getitem(0) < 3) {
                        OnBoardingActivity.this.mSLideViewPager.setCurrentItem(OnBoardingActivity.this.getitem(1), true);
                        return;
                    }
                    SharedPreferences.Editor editor = OnBoardingActivity.this.sharedPreferences.edit();
                    Boolean unused = OnBoardingActivity.this.checkFirstTime = false;
                    editor.putBoolean("CheckFirstTime", OnBoardingActivity.this.checkFirstTime.booleanValue());
                    editor.apply();
                    OnBoardingActivity.this.startActivity(new Intent(OnBoardingActivity.this, LogInActivity.class));
                    OnBoardingActivity.this.finish();
                }
            });
            this.skipbtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    SharedPreferences.Editor editor = OnBoardingActivity.this.sharedPreferences.edit();
                    Boolean unused = OnBoardingActivity.this.checkFirstTime = false;
                    editor.putBoolean("CheckFirstTime", OnBoardingActivity.this.checkFirstTime.booleanValue());
                    editor.apply();
                    OnBoardingActivity.this.startActivity(new Intent(OnBoardingActivity.this, LogInActivity.class));
                    OnBoardingActivity.this.finish();
                }
            });
//            this.mSLideViewPager = findViewById(R.id.slideViewPager);
//            this.mDotLayout = (LinearLayout) findViewById(R.id.indicator_layout);
//            ViewPagerAdapter viewPagerAdapter2 = new ViewPagerAdapter(this);
//            this.viewPagerAdapter = viewPagerAdapter2;
//            this.mSLideViewPager.setAdapter(viewPagerAdapter2);
            if (Build.VERSION.SDK_INT >= 23) {
                setUpindicator(0);
            }
            this.mSLideViewPager.addOnPageChangeListener(this.viewListener);
            return;
        }
        startActivity(new Intent(this, SplashScreen.class));
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setUpindicator(int position) {
        this.dots = new TextView[4];
        this.mDotLayout.removeAllViews();
        int i = 0;
        while (true) {
            TextView[] textViewArr = this.dots;
            if (i < textViewArr.length) {
                textViewArr[i] = new TextView(this);
                this.dots[i].setText(Html.fromHtml("&#8226"));
                this.dots[i].setTextSize(35.0f);
                this.dots[i].setTextColor(getResources().getColor(R.color.green_white, getApplicationContext().getTheme()));
                this.mDotLayout.addView(this.dots[i]);
                i++;
            } else {
                textViewArr[position].setTextColor(getResources().getColor(R.color.pink, getApplicationContext().getTheme()));
                return;
            }
        }
    }

    public int getitem(int i) {
        return this.mSLideViewPager.getCurrentItem() + i;
    }
}
