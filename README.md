# cube-pager-master
呈现3D翻转效果的仿ViewPager
该控件直接继承自ViewGroup,具有以下特点：
1. 可以无限向左或向右翻滚，该控件始终包含左中右三个子View，向左和向右滑包括回收View和新生View两个动作；
2. 3D翻转效果由Camera和Matrix实现；
3. 使用Timer实现画面的定时翻转，发生触摸事件时，该线程会终止，直到触摸事件终止；
4. 下方的小圆点容器又DotsLayout实现。

使用时注意：
1. 目前子View的数目要大于3，以后会改进；

参考链接：
https://www.ibm.com/developerworks/cn/opensource/os-cn-android-anmt2/index.html?ca=drs-
http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0928/3531.html
http://blog.csdn.net/mr_immortalz/article/details/51918560
