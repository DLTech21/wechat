/**
 * wechatdonal
 */
package ui;

import java.util.ArrayList;
import java.util.List;

import tools.UIHelper;
import ui.adapter.StrangerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
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
public class FindFriend extends AppActivity implements OnScrollListener{
	
	private int lvDataState;
	private int currentPage;
	
	private ListView xlistView;
	private List<UserInfo> datas;
	private StrangerAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.findfriend);
		initUI();
		getFriendCardFromCache();
	}
	
	private void initUI() {
		xlistView = (ListView)findViewById(R.id.xlistview);
		xlistView.setOnScrollListener(this);
        xlistView.setDividerHeight(0);
        datas = new ArrayList<UserInfo>();
		mAdapter = new StrangerAdapter(this, datas);
		xlistView.setAdapter(mAdapter);
	}
	
	private void getFriendCardFromCache() {
//		String key = String.format("%s-%s", CommonValue.CacheKey.FriendCardList1, appContext.getLoginUid());
//		FriendCardListEntity entity = (FriendCardListEntity) appContext.readObject(key);
//		if(entity == null){
//			currentPage = 1;
//			lvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
//			xlistView.startLoadMore();
//			return;
//		}
//		handleFriends(entity, UIHelper.LISTVIEW_ACTION_INIT);
		currentPage = 1;
		findFriend(currentPage, "", UIHelper.LISTVIEW_ACTION_REFRESH);
	}
	
	private void findFriend(int page, String nickName, final int action) {
		String apiKey = appContext.getLoginApiKey();
		ApiClent.findFriend(appContext, apiKey, page+"", UIHelper.LISTVIEW_COUNT+"", nickName, new ClientCallback() {
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
	}

	@Override
	public void onBackPressed() {
		isExit();
	}
	
	public void show2OptionsDialog(final String[] arg ,final UserInfo model){
		new AlertDialog.Builder(context).setTitle(null).setItems(arg,
				new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				switch(which){
				case 0:
					addFriend(model);
					break;
				}
			}
		}).show();
	}
	
	private void addFriend(UserInfo user) {
		ApiClent.addFriend(appContext, appContext.getLoginApiKey(), user.userId, new ClientCallback() {
			
			@Override
			public void onSuccess(Object data) {
				showToast((String)data);
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
        	findFriend(currentPage, "", UIHelper.LISTVIEW_ACTION_SCROLL);
        }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
	}
}
