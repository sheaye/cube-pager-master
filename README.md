# cube-pager 呈现3D翻转效果的仿ViewPager
![演示图](/screenshots/demo.gif)

## 特征
该控件直接继承自ViewGroup，具有以下特点：
1. 立体三维的翻转效果；
2. 可以无限循环地向左或者向右翻转；
3. 可以设置定时翻转；
4. 支持任意张图片；
5. CubePager始终只维持3个子View，支持回收复用；
6. CubePagerAdapter支持notifyDataSetChanged;
7. 集成小圆点容器控件，关联CubePager即可。

## 使用
1. 暂时没有将library放到Jcenter上，同志们可以直接import module或者引用cube-pager/build/outputs/aar下面的的aar文件

2. 在布局文件中定义CubePager:
```
<com.sheaye.widget.CubePager
    android:id="@+id/m_cube_pager"
    android:layout_width="match_parent"
    android:layout_height="250dp"/>
```
3. 继承CubePagerAdapter<T>，T为bean的类型，声明构造方法，重写getItemView方法，例如：
```
class PicAdapter extends CubePagerAdapter<Integer> {

        public PicAdapter(Context mContext, List<Integer> mData) {
            super(mContext, mData);
        }

        
        @Override
        public View getItemView(int position, ViewGroup parent, View convertView, Integer item) {
            ImageView imageView;
            if (convertView != null) {
                imageView = (ImageView) convertView;
            } else {
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            imageView.setImageResource(item);
            return imageView;
        }
}
```
4. 设置CubePager的特性，设置适配器：
```
mPagerAdapter = new PicAdapter(this, mPicList);
        mCubePager
                .setWith3D(true) // 设置是否显示3D效果
                .setAutoMove(true) // 设置自动翻页
                .setMaxRotate(50) // 设置旋转的最大夹角
                .setInterval(4000) // 设置翻页间隔时间，仅自动翻页为true时有效
                .setDuration(2000) // 设置翻页持续时间，仅自动翻页为true时有效
                .setAdapter(mPagerAdapter);

        mDotsLayout.setUpWithCubePager(mCubePager);
```
5. 设置DotsLayout,添加小圆点容器

xml布局：dot_selector可以为小圆点设置selector
```
<com.sheaye.widget.DotsLayout
    android:id="@+id/m_dots_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingBottom="10dp"
    android:layout_gravity="bottom"
    app:dot_selector="@drawable/selector_dot"/>
```
关联CubePager：需要在CubePager设置Adapter之后关联
```
mDotsLayout.setUpWithCubePager(mCubePager);
```
6. 更多使用详情，请参照sample

## 参考链接：

https://www.ibm.com/developerworks/cn/opensource/os-cn-android-anmt2/index.html?ca=drs-
http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0928/3531.html
http://blog.csdn.net/mr_immortalz/article/details/51918560
