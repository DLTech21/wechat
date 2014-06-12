/**
 * wechatdonal
 */
package ui;

import im.Chating;
import java.util.ArrayList;
import java.util.List;

import tools.UIHelper;
import ui.adapter.FriendCardAdapter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import bean.StrangerEntity;
import bean.UserInfo;

import com.donal.wechat.R;

import config.ApiClent;
import config.AppActivity;
import config.CommonValue;
import config.ApiClent.ClientCallback;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Friend extends AppActivity implements OnScrollListener, OnRefreshListener{
	
	private int lvDataState;
	private int currentPage;
	
	private ListView xlistView;
	private List<UserInfo> datas;
	private FriendCardAdapter mAdapter;
	private SwipeRefreshLayout swipeLayout;
	
	private FriendReceiver receiver = null;
	
	private void init() {
		receiver = new FriendReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonValue.ADD_FRIEND_ACTION);
		registerReceiver(receiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend);
		initUI();
		init();
		getFriendCardFromCache();
		
	}
	
	private void initUI() {
		swipeLayout = (SwipeRefreshLayout) findViewById(R.id.xrefresh);
		swipeLayout.setOnRefreshListener(this);
	    swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_orange_light, 
	            android.R.color.holo_red_light);
		xlistView = (ListView)findViewById(R.id.xlistview);
		xlistView.setOnScrollListener(this);
        xlistView.setDividerHeight(0);
        datas = new ArrayList<UserInfo>();
		mAdapter = new FriendCardAdapter(this, datas);
		xlistView.setAdapter(mAdapter);
	}
	
	public void show2OptionsDialog(final String[] arg ,final UserInfo model){
		new AlertDialog.Builder(context).setTitle(null).setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					try {
						ApiClent.deleteFriend(appContext, appContext.getLoginApiKey(), model.userId, null);
						datas.remove(model);
						mAdapter.notifyDataSetChanged();
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}).show();
	}
	
	private void getFriendCardFromCache() {
		currentPage = 1;
		getMyFriend(currentPage, UIHelper.LISTVIEW_ACTION_REFRESH);
	}
	
	private void getMyFriend(int page, final int action) {
		String apiKey = appContext.getLoginApiKey();
		ApiClent.getMyFriend(appContext, apiKey, page+"", UIHelper.LISTVIEW_COUNT+"", new ClientCallback() {
			@Override
			public void onSuccess(Object data) {
				StrangerEntity entity = (StrangerEntity)data;
				switch (entity.status) {
				case 1:
					handleFriends(entity, action);
					break;
				default:
					showToast(entity.msg);
					break;
				}
			}
			
			@Override
			public void onFailure(String message) {
				showToast(message);
			}
			
			@Override
			public void onError(Exception e) {
			}
		});
	}
	
	private void handleFriends(StrangerEntity entity, int action) {
		switch (action) {
		case UIHelper.LISTVIEW_ACTION_INIT:
		case UIHelper.LISTVIEW_ACTION_REFRESH:
			datas.clear();
			datas.addAll(entity.userList);
			break;
		case UIHelper.LISTVIEW_ACTION_SCROLL:
			datas.addAll(entity.userList);
			break;
		}
		if(entity.userList.size() == UIHelper.LISTVIEW_COUNT){					
			lvDataState = UIHelper.LISTVIEW_DATA_MORE;
			mAdapter.notifyDataSetChanged();
		}
		else {
			lvDataState = UIHelper.LISTVIEW_DATA_FULL;
			mAdapter.notifyDataSetChanged();
		}
		if(datas.isEmpty()){
			lvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
		}
		swipeLayout.setRefreshing(false);
	}

	@Override
	public void onBackPressed() {
		isExit();
	}
	
	public void createChat(String userId) {
		Intent intent = new Intent(context, Chating.class);
		intent.putExtra("to", userId);
		startActivity(intent);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (lvDataState != UIHelper.LISTVIEW_DATA_MORE) {
            return;
        }
        if (firstVisibleItem + visibleItemCount >= totalItemCount
                && totalItemCount != 0) {
        	lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
        	currentPage++;
        	getMyFriend(currentPage, UIHelper.LISTVIEW_ACTION_SCROLL);
        }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}

	@Override
	public void onRefresh() {
		if (lvDataState != UIHelper.LISTVIEW_DATA_LOADING) {
			lvDataState = UIHelper.LISTVIEW_DATA_LOADING;
			currentPage = 1;
			getMyFriend(currentPage, UIHelper.LISTVIEW_ACTION_REFRESH);
		}
		else {
			swipeLayout.setRefreshing(false);
		}
	}
	
	private class FriendReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (CommonValue.ADD_FRIEND_ACTION.equals(action)) {
				UserInfo user = (UserInfo) intent.getSerializableExtra("user");
				datas.add(0, user);
				mAdapter.notifyDataSetChanged();
			} 
		}
	}
}
