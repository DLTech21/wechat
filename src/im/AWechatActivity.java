/**
 * wechatdonal
 */
package im;

import im.model.Notice;

import java.util.List;


import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import config.AppActivity;
import config.CommonValue;
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
	protected int noticeNum = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	
	private void init() {
		receiver = new ChatterReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.NEW_MESSAGE_ACTION);
		filter.addAction(CommonValue.SEND_MESSAGE_ACTION);
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
//	@Override
//	protected void onPause() {
//		
//		super.onPause();
//	}
//	
//	@Override
//	protected void onResume() {
//		super.onResume();
//	}
	
	private class ChatterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (CommonValue.NEW_MESSAGE_ACTION.equals(action)) {
				Notice notice = (Notice) intent.getSerializableExtra("notice");
				msgReceive(notice);
			} 
			else if (CommonValue.ACTION_RECONNECT_STATE.equals(action)) {
				boolean isSuccess = intent.getBooleanExtra(
						CommonValue.RECONNECT_STATE, true);
				handReConnect(isSuccess);
			}
			else if (CommonValue.SEND_MESSAGE_ACTION.equals(action)) {
				String to = intent.getStringExtra("to");
				msgSend(to);
			}
		}
	}
	protected abstract void msgSend(String to);
	
	protected abstract void msgReceive(Notice notice);
	
	protected void createChat(String userId) {
		Intent intent = new Intent(context, Chating.class);
		intent.putExtra("to", userId);
		startActivity(intent);
	}

	protected abstract void handReConnect(boolean isSuccess);
}
