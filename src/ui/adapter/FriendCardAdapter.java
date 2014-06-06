package ui.adapter;

import java.util.List;
import java.util.concurrent.ExecutionException;

import tools.StringUtils;
import ui.Friend;

import com.donal.wechat.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import config.CommonValue;
import config.XmppConnectionManager;
import bean.UserInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendCardAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<UserInfo> cards;
	
	static class CellHolder {
		TextView alpha;
		ImageView avatarImageView;
		TextView titleView;
		TextView desView;
	}
	
	public FriendCardAdapter(Context context, List<UserInfo> cards) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.cards = cards;
	}
	
	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public Object getItem(int arg0) {
		return cards.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		CellHolder cell = null;
		if (convertView == null) {
			cell = new CellHolder();
			convertView = inflater.inflate(R.layout.friend_card_cell, null);
			cell.alpha = (TextView) convertView.findViewById(R.id.alpha);
			cell.avatarImageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
			cell.titleView = (TextView) convertView.findViewById(R.id.title);
			cell.desView = (TextView) convertView.findViewById(R.id.des);
			convertView.setTag(cell);
		}
		else {
			cell = (CellHolder) convertView.getTag();
		}
		final UserInfo model = cards.get(position);
		ImageLoader.getInstance().displayImage(CommonValue.BASE_URL+model.userHead, cell.avatarImageView, CommonValue.DisplayOptions.default_options);
		cell.titleView.setText(model.nickName);
		cell.desView.setText( model.description);
		cell.alpha.setVisibility(View.GONE);
		convertView.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((Friend)context).createChat(model.userId+XmppConnectionManager.BASE_XMPP_SERVER_NAME);
			}
		});
		convertView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				((Friend)context).show2OptionsDialog(new String[]{"删除好友"}, model);
				return true;
			}
		});
		return convertView;
	}
	
}
