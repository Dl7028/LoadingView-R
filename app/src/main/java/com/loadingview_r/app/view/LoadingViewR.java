package com.loadingview_r.app.view;

import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.annotation.Nullable;

import com.loadingview_r.app.R;

/**
 * @author Yuki-r
 * @date on 2020/10/4
 * @describle  自定义View——加载界面
 */
public class LoadingViewR extends View {
    private FixedBlock[] mFixedBlock; //固定方块
    private MoveBlock mMoveBlock; //移动方块

    private float mHalfBlockWidth;  //半个方块的宽度
    private float mBlockInterval; //方块间隔
    private Paint mPaint; //
    private boolean isClockWise; //动画是否是顺时针旋转
    private int mInitPosition; //移动方块的初始位置（空白位置）
    private int mCurrEmptyPosition; //当前空白方块位置
    private int mLineNumber; //方块行数量，最少3行
    private int mBlockColor; //方块的颜色

    private float mMoveBlockAngle; //移动方块的圆角半径
    private float mFixBlockAngle; //固定方块的圆角半径
    // 动画属性
    private float mRotateDegree;
    private boolean mAllowRoll = false;
    private boolean isMoving = false;
    private int mMoveSpeed = 250; //移动方块的移动速度

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
        initAttrs(context, attrs); //初始化动画属性
        init(); //初始化自定义View
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
        mLineNumber = typedArray.getInteger(R.styleable.LoadingViewR_lineNumber,3);
        if(mLineNumber<3){
            mLineNumber = 3;
        }
        mHalfBlockWidth = typedArray.getDimension(R.styleable.LoadingViewR_half_BlockWidth,30);
        mBlockInterval = typedArray.getDimension(R.styleable.LoadingViewR_blockInterval,10);
        mMoveBlockAngle = typedArray.getFloat(R.styleable.LoadingViewR_move_blockAngle,10);
        mFixBlockAngle = typedArray.getFloat(R.styleable.LoadingViewR_fix_blockAngle,30);

        int defaultColor = context.getResources().getColor(R.color.colorAccent,null); //默认颜色
        mBlockColor = typedArray.getColor(R.styleable.LoadingViewR_blockColor,defaultColor);

        mInitPosition = typedArray.getInteger(R.styleable.LoadingViewR_initPosition,0); //设置移动方块的位置为0
//        if(isInsideBlock(mInitPosition,mLineNumber)){   //判断方块是否属于外部方块
//            mInitPosition = 0 ;
//        }

        isClockWise = typedArray.getBoolean(R.styleable.LoadingViewR_isClock_Wise,true);
        mMoveSpeed = typedArray.getInteger(R.styleable.LoadingViewR_moveSpeed,250);

        int mMoveInterpolatorResId = typedArray.getResourceId(R.styleable.LoadingViewR_move_Interpolator,android.R.anim.linear_interpolator); //线性插值器
        mMoveInterpolator = AnimationUtils.loadInterpolator(context,mMoveInterpolatorResId);

        mCurrEmptyPosition = mInitPosition; //实时更新的空白方块的位置
        typedArray.recycle(); //释放资源
    }

    /**
     * 判断一个方块是否在内部
     * @param position 位置参数
     * @param lineCount 方块行数
     * @return true在内部，false则不是
     */
    private boolean isInsideBlock(int position,int lineCount){

        if(position<lineCount){  //位置参数小于行数，在第一行，属于外部方块
            return false;
        }else if(position>(lineCount*lineCount-1-lineCount)){ //在最后一行
            return false;
        }else if((position+1)%lineCount==0){ //在第一列
            return false;
        }else                       //在最后一列
            return position % lineCount != 0;
    }

    /**
     * 初始化方块对象之间的关系
     */
    private void init(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mBlockColor);
        initBlocks(mInitPosition);
    }

    /**
     * 初始化方块之间的关系
     * @param initPosition 位置参数
     */
    private void initBlocks(int initPosition){
        mFixedBlock = new FixedBlock[mLineNumber*mLineNumber]; //方块总数量

        //创建固定方块并保存到数组
        for(int i =0;i<mFixedBlock.length;i++){
            mFixedBlock[i] = new FixedBlock();
            mFixedBlock[i].index = i; //赋值下标
            mFixedBlock[i].isShow = mInitPosition != i; //如果是移动方块则不显示
            mFixedBlock[i].rectF = new RectF(); //位置参数
        }

        //创建移动方块
        mMoveBlock = new MoveBlock();
        mMoveBlock.rectF = new RectF(); //构造一个无参矩形
        mMoveBlock.isShow = false;

        relateOuterBlock(mFixedBlock,isClockWise)

    }

    /**
     * 关联外部方块
     * @param fixedBlocks 方块数组
     * @param isClockWise 是否顺时针旋转
     */
    private void relateOuterBlock(FixedBlock[] fixedBlocks,boolean isClockWise){
        int lineCount = (int) Math.sqrt(fixedBlocks.length);

        //关联第一行
        for(int i = 0;i<lineCount;i++){
            //左上角第一个方块
            if (i % lineCount == 0){
                fixedBlocks[i].next = isClockWise?fixedBlocks[i+lineCount]:fixedBlocks[i+1];
            }
        }
    }


    /**
     * 内部类 ，固定方块类
     */
    private static class FixedBlock{
        RectF rectF;  // 存储方块的坐标位置参数
        int index;  //方块对应序列号
        boolean isShow; //标志位,判断是否显示
        FixedBlock next ; //指向下一个方块
    }

    /**
     * 内部类，移动方块类
     */
    private  static class MoveBlock{
        RectF rectF; // 存储方块的坐标位置参数
        int index; //对应序列号
        boolean isShow; //判断是否显示
        float mX; //旋转中心坐标
        float mY;
    }



}
