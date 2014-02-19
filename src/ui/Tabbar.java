package ui;


import com.donal.wechat.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.RadioGroup.OnCheckedChangeListener;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Tabbar extends TabActivity implements OnCheckedChangeListener{
	private RadioGroup mainTab;
	public static TabHost mTabHost;
	
	private Intent wechatIntent;
	private Intent friendIntent;
	private Intent findFriendIntent;
	private Intent meIntent;
	
	private final static String TAB_TAG_WECHAT = "tab_tag_wechat";
	private final static String TAB_TAG_FRIEND = "tab_tag_friend";
	private final static String TAB_TAG_ME = "tab_tag_me";
	private final static String TAB_TAG_FIND = "tab_tag_findfriend";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.index);
        mainTab=(RadioGroup)findViewById(R.id.main_tab);
        mainTab.setOnCheckedChangeListener(this);
        prepareIntent();
        setupIntent();
        RadioButton homebutton = (RadioButton)findViewById(R.id.radio_button1);
        homebutton.setChecked(true);
	}

	private void prepareIntent() {
		wechatIntent = new Intent(this, WeChat.class);
		friendIntent = new Intent(this, Friend.class);
		meIntent = new Intent(this, Me.class);
		findFriendIntent = new Intent(this, FindFriend.class);
	}
	
	private void setupIntent() {
		mTabHost = getTabHost();
		TabHost localTabHost = mTabHost;
		localTabHost.addTab(buildTabSpec(TAB_TAG_WECHAT, R.string.main_wechat, R.drawable.tabbar_button1, wechatIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_FRIEND, R.string.main_friend, R.drawable.tabbar_button2, friendIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_FIND, R.string.main_find_friend, R.drawable.tabbar_button3, findFriendIntent));
		localTabHost.addTab(buildTabSpec(TAB_TAG_ME, R.string.main_me, R.drawable.tabbar_button4, meIntent));
	}
	
	private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon,final Intent content) {
		return this.mTabHost.newTabSpec(tag).setIndicator(getString(resLabel),
				getResources().getDrawable(resIcon)).setContent(content);
	} 
	
	
	@Override
	public void onCheckedChanged(RadioGroup arg0, int checkedId) {
		switch(checkedId){
		case R.id.radio_button1:
			this.mTabHost.setCurrentTabByTag(TAB_TAG_WECHAT);
			break;
		case R.id.radio_button2:
			this.mTabHost.setCurrentTabByTag(TAB_TAG_FRIEND);
			break;
		case R.id.radio_button3:
			this.mTabHost.setCurrentTabByTag(TAB_TAG_FIND);
			break;
		case R.id.radio_button4:
			this.mTabHost.setCurrentTabByTag(TAB_TAG_ME);
			break;
		}
	}
	
	public void tabClick(View v) {
		
	}
}
