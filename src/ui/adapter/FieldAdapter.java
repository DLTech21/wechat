package ui.adapter;

import java.util.List;

import bean.KeyValue;

import com.donal.wechat.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class FieldAdapter extends BaseAdapter {

	private List<KeyValue> datas;
	private Context context;
	private LayoutInflater inflater;
	
	static class ViewHolder {
		TextView title;
		TextView field;
	}

	public FieldAdapter(List<KeyValue> datas, Context context) {
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
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.des_cell, null);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(R.id.title);
			viewHolder.field = (TextView) convertView.findViewById(R.id.des);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		KeyValue model = datas.get(position);
		viewHolder.title.setText(model.key);
		viewHolder.field.setText(model.value);
		return convertView;
	}


}
