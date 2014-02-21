/**
 * wechatdonal
 */
package im;

import im.model.IMMessage;
import im.model.Notice;

import java.util.List;

import tools.Logger;

import bean.JsonMessage;
import bean.UserInfo;

import com.donal.wechat.R;

import config.CommonValue;
import config.Constant;
import config.FriendManager;
import config.MessageManager;
import config.NoticeManager;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Chating extends AChating{
	private MessageListAdapter adapter = null;
	private EditText messageInput = null;
	private Button messageSendBtn = null;
	private ListView listView;
	private int recordCount;
	private UserInfo user;// 聊天人
	private String to_name;
	private Notice notice;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chating);
		init();
		user = FriendManager.getInstance(context).getFriend(to.split("@")[0]);
	}
	
	private void init() {

		listView = (ListView) findViewById(R.id.chat_list);
		listView.setCacheColorHint(0);
		adapter = new MessageListAdapter(Chating.this, getMessages(),
				listView);
		listView.setAdapter(adapter);

		messageInput = (EditText) findViewById(R.id.chat_content);
		messageSendBtn = (Button) findViewById(R.id.chat_sendbtn);
		messageSendBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String message = messageInput.getText().toString();
				if ("".equals(message)) {
					Toast.makeText(Chating.this, "不能为空",
							Toast.LENGTH_SHORT).show();
				} else {

					try {
						sendMessage(message);
						messageInput.setText("");
					} catch (Exception e) {
						showToast("信息发送失败");
						messageInput.setText(message);
					}
					closeInput();
				}
			}
		});
	}

	@Override
	protected void receiveNotice(Notice notice) {
		this.notice = notice;
	}
	
	@Override
	protected void receiveNewMessage(IMMessage message) {
		
	}

	@Override
	protected void refreshMessage(List<IMMessage> messages) {
		adapter.refreshList(messages);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		recordCount = MessageManager.getInstance(context)
				.getChatCountWithSb(to);
		adapter.refreshList(getMessages());
	}
	
	private class MessageListAdapter extends BaseAdapter {

		private List<IMMessage> items;
		private Context context;
		private ListView adapterList;
		private LayoutInflater inflater;

		public MessageListAdapter(Context context, List<IMMessage> items,
				ListView adapterList) {
			this.context = context;
			this.items = items;
			this.adapterList = adapterList;
		}

		public void refreshList(List<IMMessage> items) {
			this.items = items;
			this.notifyDataSetChanged();
			adapterList.setSelection(items.size() - 1);
		}

		@Override
		public int getCount() {
			return items == null ? 0 : items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			IMMessage message = items.get(position);
			if (message.getMsgType() == 0) {
				convertView = this.inflater.inflate(R.layout.formclient_chat_in, null);
			} else {
				convertView = this.inflater.inflate(R.layout.formclient_chat_out, null);
			}
			ImageView avatar = (ImageView) convertView.findViewById(R.id.from_head);
			TextView useridView = (TextView) convertView.findViewById(R.id.formclient_row_userid);
			TextView dateView = (TextView) convertView.findViewById(R.id.formclient_row_date);
			TextView msgView = (TextView) convertView.findViewById(R.id.formclient_row_msg);
			
			useridView.setVisibility(View.GONE);
			String content = message.getContent();
			if (message.getMsgType() == 0) {
				imageLoader.displayImage(CommonValue.BASE_URL+user.userHead, avatar, CommonValue.DisplayOptions.default_options);
			} else {
				imageLoader.displayImage(CommonValue.BASE_URL+appContext.getLoginUserHead(), avatar, CommonValue.DisplayOptions.default_options);
			}
			try {
				JsonMessage msg = JsonMessage.parse(content);
				msgView.setText(msg.text);
			} catch (Exception e) {
				Logger.i(e);
				msgView.setText(content);
			}
			dateView.setText(message.getTime());
			return convertView;
		}

	}
	
	@Override
	public void onBackPressed() {
		NoticeManager.getInstance(context).updateStatusByFrom(to, Notice.READ);
		super.onBackPressed();
	}
}
