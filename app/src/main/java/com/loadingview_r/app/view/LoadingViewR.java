package com.loadingview_r.app.view;

import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
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
    private boolean isClockWise; //动画是否是逆时针旋转
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

        relateOuterBlock(mFixedBlock,isClockWise);

    }

    /**
     * 关联外部方块
     * @param fixedBlocks 方块数组
     * @param isClockWise 是否逆时针旋转
     */
    private void relateOuterBlock(FixedBlock[] fixedBlocks,boolean isClockWise){
        int lineCount = (int) Math.sqrt(fixedBlocks.length);

        //关联第一行
        for(int i = 0;i<lineCount;i++){
            if (i % lineCount == 0){      //左上角第一个方块
                fixedBlocks[i].next = isClockWise ? fixedBlocks[i+lineCount]:fixedBlocks[i+1];
            }else if((i+1)%lineCount==0){ //右上角第一个方块
                fixedBlocks[i].next = isClockWise ? fixedBlocks[i-1]:fixedBlocks[i+lineCount];
            }else{       //中间方块
                fixedBlocks[i].next = isClockWise ? fixedBlocks[i-1]:fixedBlocks[i+1];
            }
        }
        //关联最后一行
        for(int i = (lineCount - 1)*lineCount;i<lineCount*lineCount;i++){
            if(i % lineCount == 0){  //位于最左边
                fixedBlocks[i].next = isClockWise ? fixedBlocks[i+1] : fixedBlocks[i-lineCount];
            }else if((i + 1) % lineCount == 0){ //位于最右边
                fixedBlocks[i].next = isClockWise ? fixedBlocks[i-lineCount] : fixedBlocks[i - 1];
            }else { //位于中间
                fixedBlocks[i].next = isClockWise ? fixedBlocks[i + 1 ] : fixedBlocks[i - 1];
            }
        }
        //关联第一列
        for (int i = lineCount; i<=lineCount*lineCount-1; i=lineCount+1){
            if(i == (lineCount-1)*lineCount){ //第一列最后一个
                fixedBlocks[i].next = isClockWise ? fixedBlocks[i+1] : fixedBlocks[i-lineCount];
                continue;
            }
            fixedBlocks[i].next = isClockWise ? fixedBlocks[i+lineCount] : fixedBlocks[i-lineCount];
        }
        //关联最后一列
        for (int i = 2 *lineCount - 1;i <= lineCount*lineCount-1;i = lineCount+1){
            if(i == lineCount*lineCount-1){ //最后一列的最后一个
                fixedBlocks[i].next = isClockWise ? fixedBlocks[i - lineCount] : fixedBlocks[i - 1];
                continue;
            }
            fixedBlocks[i].next = isClockWise ? fixedBlocks[i-lineCount] : fixedBlocks[i + lineCount];
        }
    }


    /**
     * 设置方块的初始位置
     *  调用时刻：onCreate之后onDraw之前调用；view的大小发生改变就会调用该方法
     *  使用场景：用于屏幕的大小改变时，需要根据屏幕宽高来决定的其他变量可以在这里进行初始化操作
     * @param w 当前View宽度
     * @param h 当前View高度
     * @param oldw 旧View的宽
     * @param oldh 旧View的高
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        int cx = measuredWidth/2;
        int cy = measuredHeight/2;
        //设置固定方块的位置
        fixedBlockPosition(mFixedBlock,cx,cy,mBlockInterval,mHalfBlockWidth);
        //设置移动方块的位置
        MoveBlockPosition(mFixedBlock,mMoveBlock,mInitPosition,isClockWise);
    }

    /**
     * 设置固定方块的位置
     * @param fixedBlocks 方块属猪
     * @param cx 横坐标
     * @param cy 纵坐标
     * @param dividerWidth 方块间隔
     * @param halfSquareWidth 方块一半长度
     */
    private void fixedBlockPosition(FixedBlock[] fixedBlocks, int cx, int cy, float dividerWidth, float halfSquareWidth){
        //确定第一个方块的位置
        float squareWidth = halfSquareWidth * 2;
        int lineCount = (int) Math.sqrt(fixedBlocks.length);
        float firstRectLeft = 0;
        float firstRectTop = 0;
        //当行数为偶数时
        if(lineCount%2 == 0){
            int squareCountInAline = lineCount / 2;
            int divieCountInAline = squareCountInAline - 1;
            float firstRectLeftTopFromCenter = squareCountInAline * squareWidth
                                                + divieCountInAline * dividerWidth
                                                + dividerWidth / 2;
            firstRectLeft = cx - firstRectLeftTopFromCenter;
            firstRectTop = cy - firstRectLeftTopFromCenter;
        }else {
            int squareCountInAline = lineCount / 2;
            int diviCountInAline = squareCountInAline;
            float firstRectLeftTopFromCenter = squareCountInAline * squareWidth
                    + diviCountInAline * dividerWidth
                    + halfSquareWidth;

            firstRectLeft = cx - firstRectLeftTopFromCenter;
            firstRectTop = cy - firstRectLeftTopFromCenter;
        }

        // 2. 确定剩下的方块位置
        // 思想：把第一行方块位置往下移动即可
        // 通过for循环确定：第一个for循环 = 行，第二个 = 列
        for (int i = 0; i < lineCount; i++) {//行
            for (int j = 0; j < lineCount; j++) {//列
                if (i == 0) {
                    if (j == 0) {
                        fixedBlocks[0].rectF.set(firstRectLeft, firstRectTop,
                                firstRectLeft + squareWidth, firstRectTop + squareWidth);
                    } else {
                        int currIndex = i * lineCount + j;
                        fixedBlocks[currIndex].rectF.set(fixedBlocks[currIndex - 1].rectF);
                        fixedBlocks[currIndex].rectF.offset(dividerWidth + squareWidth, 0);
                    }
                } else {
                    int currIndex = i * lineCount + j;
                    fixedBlocks[currIndex].rectF.set(fixedBlocks[currIndex - lineCount].rectF);
                    fixedBlocks[currIndex].rectF.offset(0, dividerWidth + squareWidth);
                }
            }
        }
    }

    /**
     * 设置移动方块的位置
     * @param fixedBlocks 方块数组
     * @param moveBlock 移动方块对象
     * @param initPosition 初始位置
     * @param isClockwise 是否是逆时针
     */
    private void MoveBlockPosition(FixedBlock[] fixedBlocks,
                                   MoveBlock moveBlock, int initPosition, boolean isClockwise) {

        // 移动方块位置 = 设置初始的空出位置 的下一个位置（next）
        // 下一个位置 通过 连接的外部方块位置确定
        FixedBlock fixedBlock = fixedBlocks[initPosition];
        moveBlock.rectF.set(fixedBlock.next.rectF);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制内部方块
        for (int i = 0;i<mFixedBlock.length;i++){
            if(mFixedBlock[i].isShow){
                //传入方块位置参数、圆角和画笔属性
                canvas.drawRoundRect(mFixedBlock[i].rectF,mFixBlockAngle,mFixBlockAngle,mPaint);
            }
        }
        //绘制移动方块
        if (mMoveBlock.isShow){
            canvas.rotate(isClockWise ? mRotateDegree : -mRotateDegree,mMoveBlock.mX,mMoveBlock.mY);
            canvas.drawRoundRect(mMoveBlock.rectF, mMoveBlockAngle, mMoveBlockAngle, mPaint);
        }
    }

    /**
     * 启动动画
     */
    private void startMoving(){
        if(isMoving || getVisibility()!=View.VISIBLE){
            return;
        }
        //设置是否停止动画的标志位
        isMoving = true;
        mAllowRoll = true;
        //获取移动方块当前位置，即固定方块的空位置
        FixedBlock currEmptyFixedBlock = mFixedBlock[mCurrEmptyPosition];
        // 3. 获取移动方块的到达位置，即固定方块当前空位置的下1个位置
        FixedBlock moveBlock = currEmptyFixedBlock.next;

        //设置平移动画
        mAnimatorSet = new AnimatorSet();

    }

    /**
     * 设置平移动画
     * @param currEmptyFixedBlock 固定方块的空位置
     * @param moveBlock 要移动的位置
     * @return
     */
    private ValueAnimator createTranslateValueAnimator(FixedBlock currEmptyFixedBlock,FixedBlock moveBlock){
        float startAnimValue = 0;
        float endAnimValue = 0;
        PropertyValuesHolder left = null;
        PropertyValuesHolder top = null;
        //设置移动速度
        ValueAnimator valueAnimator = new ValueAnimator().setDuration(mMoveSpeed);

        return valueAnimator;
    }



}
