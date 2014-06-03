package im;

import im.model.HistoryChatBean;
import im.model.IMMessage;
import im.model.Notice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;

import tools.Logger;
import ui.adapter.WeChatAdapter;
import ui.view.SlideDrawerView;
import xlistview.XListView;
import xlistview.XListView.IXListViewListener;

import com.donal.wechat.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import config.CommonValue;
import config.MessageManager;
import config.XmppConnectionManager;

/**
 * wechat
 *
 * @author donal
 *
 */
public class WeChat extends AWechatActivity {
	
	private ListView xlistView;
	private TextView titleBarView;
	private ImageView indicatorImageView;
	private Animation indicatorAnimation;
	
	private List<HistoryChatBean> inviteNotices;
	private WeChatAdapter noticeAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wechat);
		initUI();
		getHistoryChat();
		XMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
		if (!connection.isConnected()) {
			connect2xmpp();
		}
	}
	
	@Override
	protected void onResume() {
		
//		setPaoPao();
		super.onResume();
	}
	
	private void initUI() {
		titleBarView = (TextView) findViewById(R.id.titleBarView);
		indicatorImageView = (ImageView) findViewById(R.id.xindicator);
		indicatorAnimation = AnimationUtils.loadAnimation(this, R.anim.refresh_button_rotation);
		indicatorAnimation.setDuration(500);
		indicatorAnimation.setInterpolator(new Interpolator() {
		    private final int frameCount = 10;
		    @Override
		    public float getInterpolation(float input) {
		        return (float)Math.floor(input*frameCount)/frameCount;
		    }
		});
		
		xlistView = (ListView)findViewById(R.id.xlistview);
        inviteNotices = new ArrayList<HistoryChatBean>();
        inviteNotices = MessageManager.getInstance(context)
				.getRecentContactsWithLastMsg();
		noticeAdapter = new WeChatAdapter(this, inviteNotices);
		xlistView.setAdapter(noticeAdapter);
		noticeAdapter.setOnClickListener(contacterOnClickJ);
		noticeAdapter.setOnLongClickListener(contacterOnLongClickJ);
	}
	
	private void getHistoryChat() {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				noticeAdapter.setNoticeList(inviteNotices);
				noticeAdapter.notifyDataSetChanged();
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				inviteNotices = MessageManager.getInstance(context)
						.getRecentContactsWithLastMsg();
				handler.sendEmptyMessage(1);
			}
		});
	}
	
	private void connect2xmpp()  {
		indicatorImageView.startAnimation(indicatorAnimation);
		indicatorImageView.setVisibility(View.VISIBLE);
		titleBarView.setText("连线中...");
		final Handler handler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				switch(msg.what){
				case 1:
					indicatorImageView.setVisibility(View.INVISIBLE);
					indicatorImageView.clearAnimation();
					titleBarView.setText("微信");
					startService();
					break;
				case 2:
					indicatorImageView.setVisibility(View.INVISIBLE);
					indicatorImageView.clearAnimation();
					titleBarView.setText("未连接");
					Exception e = (Exception) msg.obj;
					Logger.i(e);
					break;
				default:
					break;
				}
			};
		};
		new Thread(new Runnable() {				
			@Override
			public void run() {
				Message msg = new Message();
				try {
					String password = appContext.getLoginPassword();
					String userId = appContext.getLoginUid();
					XMPPConnection connection = XmppConnectionManager.getInstance()
							.getConnection();
					connection.connect();
					connection.login(userId, password, "android"); 
					connection.sendPacket(new Presence(Presence.Type.available));
					Logger.i("XMPPClient Logged in as " +connection.getUser());
					msg.what = 1;
					
				} catch (Exception xee) {
					if (xee instanceof XMPPException) {
						XMPPException xe = (XMPPException) xee;
						final XMPPError error = xe.getXMPPError();
						int errorCode = 0;
						if (error != null) {
							errorCode = error.getCode();
						}
						msg.what = errorCode;
						msg.obj = xee;
					}
					
				}	
				handler.sendMessage(msg);
			}
		}).start();
	}

	@Override
	public void onBackPressed() {
		isExit();
	}

	@Override
	protected void msgReceive(final Notice notice) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				noticeAdapter.notifyDataSetChanged();
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				inviteNotices = MessageManager.getInstance(context).getRecentContactsWithLastMsg();
				for (HistoryChatBean ch : inviteNotices) {
					if (ch.getFrom().equals(notice.getFrom())) {
						ch.setContent(notice.getContent());
						ch.setNoticeTime(notice.getNoticeTime());
						Integer x = ch.getNoticeSum() == null ? 0 : ch.getNoticeSum();
						ch.setNoticeSum(x);
					}
				}
				noticeAdapter.setNoticeList(inviteNotices);
				handler.sendEmptyMessage(0);
			}
		});
	}
	

//	/**
//	 * 上面滚动条上的气泡设置 有新消息来的通知气泡，数量设置,
//	 */
//	private void setPaoPao() {
//		if (null != inviteNotices && inviteNotices.size() > 0) {
//			int paoCount = 0;
//			for (HistoryChatBean c : inviteNotices) {
//				Integer countx = c.getNoticeSum();
//				paoCount += (countx == null ? 0 : countx);
//			}
//			if (paoCount == 0) {
////				noticePaopao.setVisibility(View.GONE);
//				return;
//			}
//			Logger.i(paoCount+"");
////			noticePaopao.setText(paoCount + "");
////			noticePaopao.setVisibility(View.VISIBLE);
//		} else {
////			noticePaopao.setVisibility(View.GONE);
//		}
//	}
	
	@Override
	protected void handReConnect(boolean isSuccess) {
		if (CommonValue.RECONNECT_STATE_SUCCESS == isSuccess) {
			titleBarView.setText("微信");

		} else if (CommonValue.RECONNECT_STATE_FAIL == isSuccess) {
			titleBarView.setText("未连接");
		}
	}
	
	/**
	 * 通知点击
	 */
	private OnClickListener contacterOnClickJ = new OnClickListener() {

		@Override
		public void onClick(View v) {
			HistoryChatBean notice = (HistoryChatBean) v.findViewById(R.id.des).getTag();
			createChat(notice.getFrom());
			removeSingelChatPao(notice);
		}
	};
	
	private void removeSingelChatPao(final HistoryChatBean notice) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				noticeAdapter.notifyDataSetChanged();
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				notice.setNoticeSum(0);
				handler.sendEmptyMessage(0);
			}
		});
	}
	
	private OnLongClickListener contacterOnLongClickJ = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			HistoryChatBean notice = (HistoryChatBean) v.findViewById(R.id.des).getTag();
			showDelChatOptionsDialog(new String[]{"删除对话"}, notice);
			return false;
		}
	};
	
	public void showDelChatOptionsDialog(final String[] arg ,final HistoryChatBean notice){
		new AlertDialog.Builder(context).setTitle(null).setItems(arg, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					inviteNotices.remove(notice);
					noticeAdapter.notifyDataSetChanged();
					MessageManager.getInstance(context).delChatHisWithSb(notice.getFrom());
					break;
				}
			}
		}).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CommonValue.REQUEST_OPEN_CHAT:
			String to = data.getExtras().getString("to");
			sortChat(to);
			break;

		default:
			break;
		}
	}
	
	private void sortChat(final String to) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				noticeAdapter.notifyDataSetChanged();
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
//				List<IMMessage> chats = MessageManager.getInstance(context).getMessageListByFrom(to, 1, 1);
//				if (chats.size() < 1) {
//					return;
//				}
//				for (HistoryChatBean ch : inviteNotices) {
//					if (ch.getFrom().equals(chats.get(0).getFromSubJid())) {
//						ch.setContent(chats.get(0).g);
//						ch.setNoticeTime(notice.getNoticeTime());
//						Integer x = ch.getNoticeSum() == null ? 0 : ch.getNoticeSum();
//						ch.setNoticeSum(x);
//					}
//				}
//				noticeAdapter.setNoticeList(inviteNotices);
//				handler.sendEmptyMessage(0);
			}
		});
	}
}
