/**
 * wechatdonal
 */
package im;

import im.model.Notice;

import java.util.List;


import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import config.AppActivity;
import config.Constant;
import config.XmppConnectionManager;

import bean.UserInfo;

import tools.Logger;
import tools.StringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * wechat
 *
 * @author donal
 *
 */
public abstract class AWechatActivity extends AppActivity {

	private ChatterReceiver receiver = null;
	protected int noticeNum = 0;// 通知数量，未读消息数量
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	
	private void init() {
		receiver = new ChatterReceiver();
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();

		filter.addAction(Constant.ROSTER_ADDED);
		filter.addAction(Constant.ROSTER_DELETED);
		filter.addAction(Constant.ROSTER_PRESENCE_CHANGED);
		filter.addAction(Constant.ROSTER_UPDATED);
		filter.addAction(Constant.ROSTER_SUBSCRIPTION);
		// 好友请求
		filter.addAction(Constant.NEW_MESSAGE_ACTION);
		filter.addAction(Constant.ACTION_SYS_MSG);

		filter.addAction(Constant.ACTION_RECONNECT_STATE);
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	private class ChatterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			Notice notice = (Notice) intent.getSerializableExtra("notice");

			if (Constant.NEW_MESSAGE_ACTION.equals(action)) {
				msgReceive(notice);
			} else if (Constant.ACTION_RECONNECT_STATE.equals(action)) {
				boolean isSuccess = intent.getBooleanExtra(
						Constant.RECONNECT_STATE, true);
				handReConnect(isSuccess);
			}

		}
	}
	
	/**
	 * 有新消息进来
	 * 
	 * @param user
	 */
	protected abstract void msgReceive(Notice notice);
	
	/**
	 * 创建一个聊天
	 * 
	 * @param user
	 */
	protected void createChat(String userId) {
		Intent intent = new Intent(context, Chating.class);
		intent.putExtra("to", userId);
		startActivity(intent);
	}

	/**
	 * 冲连接返回
	 * 
	 * @param isSuccess
	 */
	protected abstract void handReConnect(boolean isSuccess);
}
