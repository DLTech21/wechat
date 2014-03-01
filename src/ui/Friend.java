/**
 * wechatdonal
 */
package ui;

import im.Chating;

import java.util.ArrayList;
import java.util.List;

import tools.UIHelper;
import ui.adapter.FriendCardAdapter;
import xlistview.XListView;
import xlistview.XListView.IXListViewListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import bean.StrangerEntity;
import bean.UserInfo;

import com.donal.wechat.R;

import config.ApiClent;
import config.AppActivity;
import config.ApiClent.ClientCallback;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Friend extends AppActivity implements IXListViewListener{
	
	private int lvDataState;
	private int currentPage;
	
	private XListView xlistView;
	private List<UserInfo> datas;
	private FriendCardAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend);
		initUI();
		Handler jumpHandler = new Handler();
		jumpHandler.postDelayed(new Runnable() {
			public void run() {
				getFriendCardFromCache();
			}
		}, 100);
	}
	
	private void initUI() {
		xlistView = (XListView)findViewById(R.id.xlistview);
		xlistView.setXListViewListener(this, 0);
        xlistView.setRefreshTime();
        xlistView.setPullLoadEnable(false);
        xlistView.setDividerHeight(0);
        datas = new ArrayList<UserInfo>();
		mAdapter = new FriendCardAdapter(this, datas);
		xlistView.setAdapter(mAdapter);
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
				xlistView.stopLoadMore();
				xlistView.stopRefresh();
				showToast(message);
			}
			
			@Override
			public void onError(Exception e) {
				xlistView.stopLoadMore();
				xlistView.stopRefresh();
			}
		});
	}
	
	private void handleFriends(StrangerEntity entity, int action) {
		xlistView.stopLoadMore();
		xlistView.stopRefresh();
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
			xlistView.setPullLoadEnable(true);
			mAdapter.notifyDataSetChanged();
		}
		else {
			lvDataState = UIHelper.LISTVIEW_DATA_FULL;
			xlistView.setPullLoadEnable(false);
			mAdapter.notifyDataSetChanged();
		}
		if(datas.isEmpty()){
			lvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
			xlistView.setPullLoadEnable(false);
		}
	}

	@Override
	public void onRefresh(int id) {
		currentPage = 1;
		getMyFriend(currentPage, UIHelper.LISTVIEW_ACTION_REFRESH);
	}

	@Override
	public void onLoadMore(int id) {
		if (lvDataState == UIHelper.LISTVIEW_DATA_EMPTY) {
			getMyFriend(currentPage, UIHelper.LISTVIEW_ACTION_INIT);
		}
		if (lvDataState == UIHelper.LISTVIEW_DATA_MORE) {
			currentPage ++;
			getMyFriend(currentPage, UIHelper.LISTVIEW_ACTION_SCROLL);
		}
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
}
