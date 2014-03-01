/**
 * wechatdonal
 */
package im;

import im.model.IMMessage;
import im.model.Notice;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import com.google.gson.Gson;

import bean.JsonMessage;

import tools.DateUtil;
import tools.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import config.AppActivity;
import config.CommonValue;
import config.MessageManager;
import config.NoticeManager;
import config.XmppConnectionManager;

/**
 * wechat
 *
 * @author donal
 *
 */
public abstract class AChating extends AppActivity{
	private Chat chat = null;
	private List<IMMessage> message_pool = null;
	protected String to;// 聊天人
	private static int pageSize = 10;
	private List<Notice> noticeList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		to = getIntent().getStringExtra("to");
		if (to == null)
			return;
		chat = XmppConnectionManager.getInstance().getConnection()
				.getChatManager().createChat(to, null);
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		// 第一次查询
		message_pool = MessageManager.getInstance(context)
				.getMessageListByFrom(to, 1, pageSize);
		if (null != message_pool && message_pool.size() > 0)
			Collections.sort(message_pool);
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.NEW_MESSAGE_ACTION);
		registerReceiver(receiver, filter);
		// 跟新某人所有通知
		NoticeManager.getInstance(context).updateStatusByFrom(to, Notice.READ);
		super.onResume();

	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Notice notice = (Notice) intent.getSerializableExtra("notice");
			if (CommonValue.NEW_MESSAGE_ACTION.equals(action)) {
				IMMessage message = intent
						.getParcelableExtra(IMMessage.IMMESSAGE_KEY);
				message_pool.add(message);
				receiveNewMessage(message);
				refreshMessage(message_pool);
				receiveNotice(notice);
			}
		}

	};
	
	protected abstract void receiveNotice(Notice notice);
	
	protected abstract void receiveNewMessage(IMMessage message);

	protected abstract void refreshMessage(List<IMMessage> messages);
	
	protected List<IMMessage> getMessages() {
		return message_pool;
	}
	
	protected void sendMessage(String messageContent) throws Exception {
		JsonMessage msg = new JsonMessage();
		msg.file = "";
		msg.messageType = CommonValue.kWCMessageTypePlain;
		msg.text = messageContent;
		Gson gson = new Gson();
		String json = gson.toJson(msg);
		
		String time = (System.currentTimeMillis()/1000)+"";
		Message message = new Message();
		message.setProperty(IMMessage.KEY_TIME, time);
		message.setBody(json);
		chat.sendMessage(message);

		IMMessage newMessage = new IMMessage();
		newMessage.setMsgType(1);
		newMessage.setFromSubJid(chat.getParticipant());
		newMessage.setContent(json);
		newMessage.setTime(time);
		message_pool.add(newMessage);
		MessageManager.getInstance(context).saveIMMessage(newMessage);
		// 刷新视图
		refreshMessage(message_pool);

	}
	
	/**
	 * 下滑加载信息,true 返回成功，false 数据已经全部加载，全部查完了，
	 * 
	 * @param message
	 */
	protected Boolean addNewMessage() {
		List<IMMessage> newMsgList = MessageManager.getInstance(context)
				.getMessageListByFrom(to, message_pool.size(), pageSize);
		if (newMsgList != null && newMsgList.size() > 0) {
			message_pool.addAll(newMsgList);
			Collections.sort(message_pool);
			return true;
		}
		return false;
	}
	
	protected int addNewMessage(int currentPage) {
		List<IMMessage> newMsgList = MessageManager.getInstance(context)
				.getMessageListByFrom(to, currentPage, pageSize);
		if (newMsgList != null && newMsgList.size() > 0) {
			message_pool.addAll(newMsgList);
			Collections.sort(message_pool);
			return newMsgList.size();
		}
		return 0;
	}

	protected void resh() {
		// 刷新视图
		refreshMessage(message_pool);
	}
	
	class MsgListener implements MessageListener {

		@Override
		public void processMessage(Chat arg0, Message message) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
