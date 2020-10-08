# 一个加载动画界面

## 预览效果

![](https://gitee.com/yuki-r/blog-image/raw/master/img/GIF%202020-10-8%2022-47-27.gif)

## 使用

### 一、添加依赖

> 有两种方法：`Gradle`和`Maven`

1.`Gradle`

在项目的`gradle.build`中

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

添加依赖

```
dependencies {
	        implementation 'com.github.Yuki-r:LoadingView-R:1.0.1'
	}
```

2.使用`Maven`

```
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

```
<dependency>
	    <groupId>com.github.Yuki-r</groupId>
	    <artifactId>LoadingView-R</artifactId>
	    <version>1.0.1</version>
	</dependency>
```

### 二、在布局文件中引用

```
 <com.loadingview_r.loadingviewr.LoadingViewR
        android:id="@+id/loadingView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/textView"
        <!--界面设置，不设置则使用默认值-->
        app:lineNumber="3"       //设置方块行数
        app:fixBlock_Angle="5"   //设置固定方块的角度
        app:moveBlock_Angle="20" //设置移动方块的角度

        app:blockInterval="8dp"  //方块间隔
        app:half_BlockWidth="15dp" //方块大小
        app:initPosition="0"  //移动方块的初始位置

        app:isClock_Wise="false" //ture顺时针旋转，false逆时针旋转
        app:moveSpeed="500" //移动速度
        />
```

### 三、在代码中引用

```
private LoadingViewR loadingViewR;
...
loadingViewR = findViewById(R.id.LoadingView_r);
 loadingViewR.startMoving(); //启动
 loadingViewR.stopMoving(); //停止
```

### 结语

> 如果此文对你有帮助，欢迎star
