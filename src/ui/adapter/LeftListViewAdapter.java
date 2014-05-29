package ui.adapter;

import java.util.List;

import com.donal.wechat.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LeftListViewAdapter extends BaseAdapter{
	private List<String> datas;
	private Context context;
	private LayoutInflater inflater;
	
	static class ViewHolder {
		TextView optionsTV;
	}
	
	public LeftListViewAdapter(List<String> datas, Context context) {
		super();
		this.datas = datas;
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.slide_draw_cell, null);
			viewHolder = new ViewHolder() ;
			viewHolder.optionsTV = (TextView) convertView.findViewById(R.id.optionName);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.optionsTV.setText(datas.get(position));
		return convertView;
	}


}
