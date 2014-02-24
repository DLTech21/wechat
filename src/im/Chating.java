/**
 * wechatdonal
 */
package im;

import im.model.IMMessage;
import im.model.Notice;

import java.util.List;

import tools.DateUtil;
import tools.Logger;
import tools.StringUtils;

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
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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
	
	private int firstVisibleItem;
	private int currentPage = 1;
	
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
		
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case SCROLL_STATE_FLING:
					break;
				case SCROLL_STATE_IDLE:
					if (firstVisibleItem == 0) {
						int num = addNewMessage(++currentPage);
						if (num > 0) {
							adapter.refreshList(getMessages());
							listView.setSelection(num-1);
						}
					}
					break;
				case SCROLL_STATE_TOUCH_SCROLL:
					closeInput();
					break;
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				Chating.this.firstVisibleItem = firstVisibleItem;
			}
		});

		messageInput = (EditText) findViewById(R.id.chat_content);
		messageInput.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				listView.setSelection(getMessages().size()-1);
			}
		});
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
				listView.setSelection(getMessages().size()-1);
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
		listView.setSelection(getMessages().size()-1);
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
				msgView.setText(content);
			}
			String currentTime = message.getTime();
			String previewTime = (position - 1) >= 0 ? items.get(position-1).getTime() : "0";
			try {
				long time1 = Long.valueOf(currentTime);
				long time2 = Long.valueOf(previewTime);
				if ((time1-time2) >= 5 * 60 ) {
					dateView.setVisibility(View.VISIBLE);
					dateView.setText(DateUtil.wechat_time(message.getTime()));
				}
				else {
					dateView.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				Logger.i(e);
			}
			return convertView;
		}

	}
	
	@Override
	public void onBackPressed() {
		NoticeManager.getInstance(context).updateStatusByFrom(to, Notice.READ);
		super.onBackPressed();
	}
}
