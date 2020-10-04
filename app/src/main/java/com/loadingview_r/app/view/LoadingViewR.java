package com.loadingview_r.app.view;

import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.annotation.Nullable;

import com.loadingview_r.app.R;

/**
 * @author Yuki-r
 * @date on 2020/10/4
 * @describle  自定义View——加载界面
 */
public class LoadingViewR extends View {
    private FixedBlock mFixedBlock; //固定方块
    private MoveBlock mMoveBlock; //移动方块

    // 方块属性
    private float mHalfBlockWidth;
    private float mBlockInterval;
    private Paint mPaint;
    private boolean isClockWise;
    private int mInitPosition;
    private int mCurrEmptyPosition;
    private int mLineNumber;
    private int mBlockColor;

    // 方块的圆角半径
    private float mMoveBlockAngle;
    private float mFixBlockAngle;
    // 动画属性
    private float mRotateDegree;
    private boolean mAllowRoll = false;
    private boolean isMoving = false;
    private int mMoveSpeed = 250;

    // 动画插值器
    private Interpolator mMoveInterpolator;
    private AnimatorSet mAnimatorSet;

    public LoadingViewR(Context context) {
        super(context);
    }

    public LoadingViewR(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingViewR(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LoadingViewR(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * 初始化动画属性
     * @param context 上下文
     * @param attrs 布局属性
     */
    private void initAttrs(Context context,AttributeSet attrs){
        //控件资源名称
       TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingViewR);

    }



    /**
     * 内部类 ，固定方块类
     */
    private static class FixedBlock{
        RectF rectF;  //方块位置参数
        int index;  //方块对应序列号
        boolean isShow; //标志位,判断是否显示
        FixedBlock next ; //指向下一个方块
    }

    /**
     * 内部类，移动方块类
     */
    private  static class MoveBlock{
        RectF rectF; //位置参数
        int index; //对应序列号
        boolean isShow; //判断是否显示
        float mX; //旋转中心坐标
        float mY;
    }



}
