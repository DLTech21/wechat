package ui.view;

import java.util.ArrayList;
import java.util.List;

import ui.FindFriend;
import ui.Friend;
import ui.adapter.LeftListViewAdapter;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.donal.wechat.R;

public class SlideDrawerView {
	private final Activity activity;
//	SlidingMenu localSlidingMenu;
	
	private ListView leftListView;
	private LeftListViewAdapter leftAdapter;
	
	public SlideDrawerView(Activity activity) {
		this.activity = activity;
	}
	
//	public SlidingMenu initSlidingMenu() {
//		localSlidingMenu = new SlidingMenu(activity);
//		localSlidingMenu.setMode(SlidingMenu.LEFT);//设置左右滑菜单
//		localSlidingMenu.setTouchModeAbove(SlidingMenu.SLIDING_WINDOW);//设置要使菜单滑动，触碰屏幕的范围
////		localSlidingMenu.setTouchModeBehind(SlidingMenu.SLIDING_CONTENT);//设置了这个会获取不到菜单里面的焦点，所以先注释掉
//		localSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);//设置阴影图片的宽度
//		localSlidingMenu.setShadowDrawable(R.drawable.shadow);//设置阴影图片
//		localSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);//SlidingMenu划出时主页面显示的剩余宽度
//		localSlidingMenu.setFadeDegree(0.35F);//SlidingMenu滑动时的渐变程度
//		localSlidingMenu.attachToActivity(activity, SlidingMenu.RIGHT);//使SlidingMenu附加在Activity右边
////		localSlidingMenu.setBehindWidthRes(R.dimen.left_drawer_avatar_size);//设置SlidingMenu菜单的宽度
//		localSlidingMenu.setMenu(R.layout.left_slide_drawer);//设置menu的布局文件
////		localSlidingMenu.toggle();//动态判断自动关闭或开启SlidingMenu
//		localSlidingMenu.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
//					public void onOpened() {
//						
//					}
//				});
//		localSlidingMenu.setOnClosedListener(new OnClosedListener() {
//			
//			@Override
//			public void onClosed() {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		initView();
//		return localSlidingMenu;
//	}
//	
//	private void initView() {
//		leftListView = (ListView) localSlidingMenu.findViewById(R.id.leftListView);
//		List<String> options = new ArrayList<String>();
//		options.add("微信");
//		options.add("好友");
//		options.add("用户列表");
//		leftAdapter = new LeftListViewAdapter(options, activity);
//		leftListView.setAdapter(leftAdapter);
//		leftListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View convertView, int position,
//					long arg3) {
//				
//				switch (position) {
//				case 0:
//					localSlidingMenu.showContent();
//					break;
//
//				case 1:
//					activity.startActivity(new Intent(activity, Friend.class));
//					break;
//					
//				case 2:
//					activity.startActivity(new Intent(activity, FindFriend.class));
//					break;
//				}
//			}
//		});
//	}
}
