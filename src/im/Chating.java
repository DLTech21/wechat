/**
 * wechatdonal
 */
package im;

import im.model.IMMessage;
import im.model.Notice;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import qiniu.auth.JSONObjectRet;
import qiniu.io.IO;
import qiniu.io.PutExtra;
import qiniu.utils.Config;
import qiniu.utils.Mac;
import qiniu.utils.PutPolicy;
import tools.AudioRecoderManager;
import tools.DateUtil;
import tools.ImageUtils;
import tools.Logger;
import tools.StringUtils;
import tools.UIHelper;
import bean.JsonMessage;
import bean.UserInfo;

import com.donal.wechat.R;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import config.CommonValue;
import config.FriendManager;
import config.NoticeManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Chating extends AChating implements OnTouchListener{
	private Button voiceButton;
	private MessageListAdapter adapter = null;
	private EditText messageInput = null;
	private ListView listView;
	private int recordCount;
	private UserInfo user;// 聊天人
	private String to_name;
	private Notice notice;
	
	private int firstVisibleItem;
	private int currentPage = 1;
	private int objc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chating);
		init();
		user = FriendManager.getInstance(context).getFriend(to.split("@")[0]);
	}
	
	private void init() {
		voiceButton = (Button) findViewById(R.id.voiceButton);
		voiceButton.setOnTouchListener(this);
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
	}
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.chat_sendbtn:
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
			break;

		case R.id.cameraButton:
			PhotoChooseOption();
			break;
		}
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
		
//		recordCount = MessageManager.getInstance(context)
//				.getChatCountWithSb(to);
		adapter.refreshList(getMessages());
		listView.setSelection(getMessages().size()-1);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		String newPhotoPath;
		switch (requestCode) {
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
			if (StringUtils.notEmpty(theLarge)) {
				File file = new File(theLarge);
				File dir = new File( ImageUtils.CACHE_IMAGE_FILE_PATH);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + file.getName();
				try {
					ExifInterface sourceExif = new ExifInterface(theLarge);
					String orientation = sourceExif.getAttribute(ExifInterface.TAG_ORIENTATION);
					ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(theLarge, 200), 80);
					ExifInterface exif = new ExifInterface(imagePathAfterCompass);
					exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
				    exif.saveAttributes();
					newPhotoPath = imagePathAfterCompass;
					uploadPhotoToQiniu(newPhotoPath);
				} catch (IOException e) {
//					Crashlytics.logException(e);
				}
			}
			break;
		case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
			if(data == null)  return;
			Uri thisUri = data.getData();
        	String thePath = ImageUtils.getAbsolutePathFromNoStandardUri(thisUri);
        	if(StringUtils.empty(thePath)) {
        		newPhotoPath = ImageUtils.getAbsoluteImagePath(this,thisUri);
        	}
        	else {
        		newPhotoPath = thePath;
        	}
        	File file = new File(newPhotoPath);
			File dir = new File( ImageUtils.CACHE_IMAGE_FILE_PATH);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String imagePathAfterCompass = ImageUtils.CACHE_IMAGE_FILE_PATH + file.getName();
			try {
				ExifInterface sourceExif = new ExifInterface(newPhotoPath);
				String orientation = sourceExif.getAttribute(ExifInterface.TAG_ORIENTATION);
				ImageUtils.saveImageToSD(imagePathAfterCompass, ImageUtils.getSmallBitmap(newPhotoPath, 200), 80);
				ExifInterface exif = new ExifInterface(imagePathAfterCompass);
				exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
			    exif.saveAttributes();
				newPhotoPath = imagePathAfterCompass;
				uploadPhotoToQiniu(newPhotoPath);
			} catch (IOException e) {
//				Crashlytics.logException(e);
			}
			break;
		}
	}
	
	private String theLarge;
	private void PhotoChooseOption() {
		closeInput();
		CharSequence[] item = {"相册", "拍照"};
		AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle(null).setIcon(android.R.drawable.btn_star).setItems(item,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int item)
					{
						//手机选图
						if( item == 0 )
						{
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
							intent.addCategory(Intent.CATEGORY_OPENABLE); 
							intent.setType("image/*"); 
							startActivityForResult(Intent.createChooser(intent, "选择图片"),ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD); 
						}
						//拍照
						else if( item == 1 )
						{	
							String savePath = "";
							//判断是否挂载了SD卡
							String storageState = Environment.getExternalStorageState();		
							if(storageState.equals(Environment.MEDIA_MOUNTED)){
								savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + ImageUtils.DCIM;//存放照片的文件夹
								File savedir = new File(savePath);
								if (!savedir.exists()) {
									savedir.mkdirs();
								}
							}
							//没有挂载SD卡，无法保存文件
							if(StringUtils.empty(savePath)){
								UIHelper.ToastMessage(Chating.this, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_SHORT);
								return;
							}
							String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
							String fileName = "c_" + timeStamp + ".jpg";//照片命名
							File out = new File(savePath, fileName);
							Uri uri = Uri.fromFile(out);
							theLarge = savePath + fileName;//该照片的绝对路径
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
							startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
						}   
					}}).create();
			 imageDialog.show();
	}
	
	private class MessageListAdapter extends BaseAdapter {

		class ViewHoler {
			TextView timeTV;
			
			RelativeLayout leftLayout;
			ImageView leftAvatar;
			TextView leftNickname;
			TextView leftText;
			ImageView leftPhoto;
			TextView leftVoice;
			
			RelativeLayout rightLayout;
			RelativeLayout rightFrame;
			ImageView rightAvatar;
			TextView rightNickname;
			TextView rightText;
			ImageView rightPhoto;
			TextView photoProgress;
			ProgressBar rightProgress;
			TextView rightVoice;
		}
		
		private List<IMMessage> items;
		private Context context;
		private ListView adapterList;
		private LayoutInflater inflater;

		DisplayImageOptions options;
		DisplayImageOptions photooptions;
		
		public MessageListAdapter(Context context, List<IMMessage> items,
				ListView adapterList) {
			this.context = context;
			this.items = items;
			this.adapterList = adapterList;
			inflater = LayoutInflater.from(context);
			options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.avatar_placeholder)
			.showImageForEmptyUri(R.drawable.avatar_placeholder)
			.showImageOnFail(R.drawable.avatar_placeholder)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
			photooptions = new DisplayImageOptions.Builder()
//			.showImageOnLoading(R.drawable.content_image_loading)
//			.showImageForEmptyUri(R.drawable.content_image_loading)
//			.showImageOnFail(R.drawable.content_image_loading)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
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
			ViewHoler cell = null;
			if (convertView == null) {
				cell = new ViewHoler();
				convertView = inflater.inflate(R.layout.listviewcell_chat_normal, null);
				cell.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
				cell.leftLayout = (RelativeLayout) convertView.findViewById(R.id.layout_left);
				cell.leftAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_l);
				cell.leftNickname = (TextView) convertView.findViewById(R.id.textview_name_l);
				cell.leftText = (TextView) convertView.findViewById(R.id.textview_content_l);
				cell.leftPhoto = (ImageView) convertView.findViewById(R.id.photo_content_l);
				cell.leftVoice = (TextView) convertView.findViewById(R.id.receiverVoiceNode);		
				
				cell.rightLayout = (RelativeLayout) convertView.findViewById(R.id.layout_right);
				cell.rightFrame = (RelativeLayout) convertView.findViewById(R.id.layout_content_r);
				cell.rightAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_r);
				cell.rightNickname = (TextView) convertView.findViewById(R.id.textview_name_r);
				cell.rightText = (TextView) convertView.findViewById(R.id.textview_content_r);
				cell.rightPhoto = (ImageView) convertView.findViewById(R.id.photo_content_r);
				cell.photoProgress = (TextView) convertView.findViewById(R.id.photo_content_progress);
				cell.rightProgress = (ProgressBar) convertView.findViewById(R.id.view_progress_r);
				cell.rightVoice = (TextView) convertView.findViewById(R.id.senderVoiceNode);
				convertView.setTag(cell);
			}
			else {
				cell = (ViewHoler) convertView.getTag();
			}
			IMMessage message = items.get(position);
			cell.leftLayout.setVisibility(message.getMsgType() == 0? View.VISIBLE:View.INVISIBLE);
			cell.rightLayout.setVisibility(message.getMsgType() == 0? View.INVISIBLE:View.VISIBLE);
			String content = message.getContent();
			try {
				imageLoader.displayImage(CommonValue.BASE_URL+ user.userHead, cell.leftAvatar, options);
				imageLoader.displayImage(CommonValue.BASE_URL+ appContext.getLoginUserHead(), cell.rightAvatar, options);
			} catch (Exception e) {
				
			}
			try {
				JsonMessage msg = JsonMessage.parse(content);
<<<<<<< HEAD
				if (msg.messageType == CommonValue.kWCMessageTypePlain) {
					cell.leftText.setVisibility(View.VISIBLE);
					cell.rightText.setVisibility(View.VISIBLE);
					cell.leftText.setText(msg.text);
					cell.rightText.setText(msg.text);
					cell.leftPhoto.setVisibility(View.GONE);
					cell.rightPhoto.setVisibility(View.GONE);
					cell.leftVoice.setVisibility(View.GONE);
					cell.rightVoice.setVisibility(View.GONE);
				}
				else if (msg.messageType == CommonValue.kWCMessageTypeImage) {
					cell.leftText.setVisibility(View.GONE);
					cell.rightText.setVisibility(View.GONE);
					cell.leftPhoto.setVisibility(View.VISIBLE);
					cell.rightPhoto.setVisibility(View.VISIBLE);
					cell.leftVoice.setVisibility(View.GONE);
					cell.rightVoice.setVisibility(View.GONE);
					imageLoader.displayImage(msg.file, cell.leftPhoto, photooptions);
					imageLoader.displayImage(msg.file, cell.rightPhoto, photooptions);
					if (message.getType() == CommonValue.kWCMessageStatusWait) {
						message.setType(CommonValue.kWCMessageStatusSending);
						cell.photoProgress.setVisibility(View.VISIBLE);
						imageLoader.displayImage("file:///"+msg.file, cell.rightPhoto, photooptions);
						uploadQiniu(message, msg.file, cell, CommonValue.kWCMessageTypeImage);
					}
					else if (message.getType() == 0) {
						cell.photoProgress.setVisibility(View.GONE);
					}
				}
				else if (msg.messageType == CommonValue.kWCMessageTypeVoice) {
					cell.leftText.setVisibility(View.GONE);
					cell.rightText.setVisibility(View.GONE);
					cell.leftPhoto.setVisibility(View.GONE);
					cell.rightPhoto.setVisibility(View.GONE);
					cell.leftVoice.setVisibility(View.VISIBLE);
					cell.rightVoice.setVisibility(View.VISIBLE);
					if (message.getType() == CommonValue.kWCMessageStatusWait) {
						message.setType(CommonValue.kWCMessageStatusSending);
						cell.photoProgress.setVisibility(View.VISIBLE);
						uploadQiniu(message, msg.file, cell, CommonValue.kWCMessageTypeVoice);
					}
					else if (message.getType() == 0) {
						cell.photoProgress.setVisibility(View.GONE);
=======
				if (convertView == null) {
					if (message.getMsgType() == 0) {
						switch (msg.messageType) {
						case CommonValue.kWCMessageTypePlain:
							holderLeftText = new ViewHolderLeftText();
							convertView = inflater.inflate(R.layout.chat_left_text,null);
							holderLeftText.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
							holderLeftText.leftAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_l);
							holderLeftText.leftNickname = (TextView) convertView.findViewById(R.id.textview_name_l);
							holderLeftText.leftText = (TextView) convertView.findViewById(R.id.textview_content_l);
							displayLeftText(msg, holderLeftText, position);
							convertView.setTag(holderLeftText);
							break;
						case CommonValue.kWCMessageTypeImage:
							holderLeftImg = new ViewHolderLeftImage();
							convertView = inflater.inflate(R.layout.chat_left_image,null);
							holderLeftImg.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
							holderLeftImg.leftAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_l);
							holderLeftImg.leftNickname = (TextView) convertView.findViewById(R.id.textview_name_l);
							holderLeftImg.leftPhoto = (ImageView) convertView.findViewById(R.id.photo_content_l);
							displayLeftImage(msg, holderLeftImg, position);
							convertView.setTag(holderLeftImg);
							break;
						case CommonValue.kWCMessageTypeVoice:
							holderLeftVoice = new ViewHolderLeftVoice();
							convertView = inflater.inflate(R.layout.chat_left_voice,null);
							holderLeftVoice.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
							holderLeftVoice.leftAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_l);
							holderLeftVoice.leftNickname = (TextView) convertView.findViewById(R.id.textview_name_l);
							holderLeftVoice.leftVoice = (TextView) convertView.findViewById(R.id.receiverVoiceNode);
							displayLeftVoice(msg, holderLeftVoice, position);
							convertView.setTag(holderLeftVoice);
							break;
						}
					}
					else {
						switch (msg.messageType) {
						case CommonValue.kWCMessageTypePlain:
							holderRightText = new ViewHolderRightText();
							convertView = inflater.inflate(R.layout.chat_right_text, null);
							holderRightText.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
							holderRightText.rightAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_r);
							holderRightText.rightNickname = (TextView) convertView.findViewById(R.id.textview_name_r);
							holderRightText.rightText = (TextView) convertView.findViewById(R.id.textview_content_r);
							displayRightText(msg, holderRightText, position);
							convertView.setTag(holderRightText);
							break;
						case CommonValue.kWCMessageTypeImage:
							holderRightImg = new ViewHolderRightImage();
							convertView = inflater.inflate(R.layout.chat_right_image, null);
							holderRightImg.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
							holderRightImg.rightAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_r);
							holderRightImg.rightNickname = (TextView) convertView.findViewById(R.id.textview_name_r);
							holderRightImg.rightPhoto = (ImageView) convertView.findViewById(R.id.photo_content_r);
							holderRightImg.photoProgress = (TextView) convertView.findViewById(R.id.photo_content_progress);
							holderRightImg.rightProgress = (ProgressBar) convertView.findViewById(R.id.view_progress_r);
							displayRightImage(message, msg, holderRightImg, position);
							convertView.setTag(holderRightImg);
							break;
							
						case CommonValue.kWCMessageTypeVoice:
							holderRightVoice = new ViewHolderRightVoice();
							convertView = inflater.inflate(R.layout.chat_right_voice, null);
							holderRightVoice.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
							holderRightVoice.rightAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_r);
							holderRightVoice.rightNickname = (TextView) convertView.findViewById(R.id.textview_name_r);
							holderRightVoice.rightVoice = (TextView) convertView.findViewById(R.id.senderVoiceNode);
							holderRightVoice.rightProgress = (ProgressBar) convertView.findViewById(R.id.view_progress_r);
							displayRightVoice(message, msg, holderRightVoice, position);
							convertView.setTag(holderRightVoice);
							break;
						}
					}
				}
				else {
					if (message.getMsgType() == 0) {
						switch (msg.messageType) {
						case CommonValue.kWCMessageTypePlain:
							if (convertView.getTag() instanceof ViewHolderLeftText) {
								holderLeftText = (ViewHolderLeftText) convertView.getTag();
								displayLeftText(msg, holderLeftText, position);
							}
							else {
								holderLeftText = new ViewHolderLeftText();
								convertView = inflater.inflate(R.layout.chat_left_text,null);
								holderLeftText.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
								holderLeftText.leftAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_l);
								holderLeftText.leftNickname = (TextView) convertView.findViewById(R.id.textview_name_l);
								holderLeftText.leftText = (TextView) convertView.findViewById(R.id.textview_content_l);
								displayLeftText(msg, holderLeftText, position);
								convertView.setTag(holderLeftText);
							}
							break;
						case CommonValue.kWCMessageTypeImage:
							if (convertView.getTag() instanceof ViewHolderLeftImage) {
								holderLeftImg = (ViewHolderLeftImage) convertView.getTag();
								displayLeftImage(msg, holderLeftImg, position);
							}
							else {
								holderLeftImg = new ViewHolderLeftImage();
								convertView = inflater.inflate(R.layout.chat_left_image,null);
								holderLeftImg.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
								holderLeftImg.leftAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_l);
								holderLeftImg.leftNickname = (TextView) convertView.findViewById(R.id.textview_name_l);
								holderLeftImg.leftPhoto = (ImageView) convertView.findViewById(R.id.photo_content_l);
								displayLeftImage(msg, holderLeftImg, position);
								convertView.setTag(holderLeftImg);
							}
							break;
						case CommonValue.kWCMessageTypeVoice:
							if (convertView.getTag() instanceof ViewHolderLeftVoice) {
								holderLeftVoice = (ViewHolderLeftVoice) convertView.getTag();
								displayLeftVoice(msg, holderLeftVoice, position);
							}
							else {
								holderLeftVoice = new ViewHolderLeftVoice();
								convertView = inflater.inflate(R.layout.chat_left_voice,null);
								holderLeftVoice.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
								holderLeftVoice.leftAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_l);
								holderLeftVoice.leftNickname = (TextView) convertView.findViewById(R.id.textview_name_l);
								holderLeftVoice.leftVoice = (TextView) convertView.findViewById(R.id.receiverVoiceNode);
								displayLeftVoice(msg, holderLeftVoice, position);
								convertView.setTag(holderLeftVoice);
							}
							break;
						}
					}
					else {
						switch (msg.messageType) {
						case CommonValue.kWCMessageTypePlain:
							if (convertView.getTag() instanceof ViewHolderRightText) {
								holderRightText = (ViewHolderRightText) convertView.getTag();
								displayRightText(msg, holderRightText, position);
							}
							else {
								holderRightText = new ViewHolderRightText();
								convertView = inflater.inflate(R.layout.chat_right_text, null);
								holderRightText.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
								holderRightText.rightAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_r);
								holderRightText.rightNickname = (TextView) convertView.findViewById(R.id.textview_name_r);
								holderRightText.rightText = (TextView) convertView.findViewById(R.id.textview_content_r);
								displayRightText(msg, holderRightText, position);
								convertView.setTag(holderRightText);
							}
							break;
						case CommonValue.kWCMessageTypeImage:
							if (convertView.getTag() instanceof ViewHolderRightImage) {
								holderRightImg = (ViewHolderRightImage) convertView.getTag();
								displayRightImage(message, msg, holderRightImg, position);
							}
							else {
								holderRightImg = new ViewHolderRightImage();
								convertView = inflater.inflate(R.layout.chat_right_image, null);
								holderRightImg.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
								holderRightImg.rightAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_r);
								holderRightImg.rightNickname = (TextView) convertView.findViewById(R.id.textview_name_r);
								holderRightImg.rightPhoto = (ImageView) convertView.findViewById(R.id.photo_content_r);
								holderRightImg.photoProgress = (TextView) convertView.findViewById(R.id.photo_content_progress);
								holderRightImg.rightProgress = (ProgressBar) convertView.findViewById(R.id.view_progress_r);
								displayRightImage(message, msg, holderRightImg, position);
								convertView.setTag(holderRightImg);
							}
							break;
						case CommonValue.kWCMessageTypeVoice:
							if (convertView.getTag() instanceof ViewHolderRightVoice) {
								holderRightVoice = (ViewHolderRightVoice) convertView.getTag();
								displayRightVoice(message, msg, holderRightVoice, position);
							}
							else {
								holderRightVoice = new ViewHolderRightVoice();
								convertView = inflater.inflate(R.layout.chat_right_voice, null);
								holderRightVoice.timeTV = (TextView) convertView.findViewById(R.id.textview_time);
								holderRightVoice.rightAvatar = (ImageView) convertView.findViewById(R.id.image_portrait_r);
								holderRightVoice.rightNickname = (TextView) convertView.findViewById(R.id.textview_name_r);
								holderRightVoice.rightVoice = (TextView) convertView.findViewById(R.id.senderVoiceNode);
								holderRightVoice.rightProgress = (ProgressBar) convertView.findViewById(R.id.view_progress_r);
								displayRightVoice(message, msg, holderRightVoice, position);
								convertView.setTag(holderRightVoice);
							}
							break;
						}
>>>>>>> dev
					}
				}
			} catch (Exception e) {
				cell.leftText.setText(content);
				cell.rightText.setText(content);
			}
<<<<<<< HEAD
			String currentTime = message.getTime();
			String previewTime = (position - 1) >= 0 ? items.get(position-1).getTime() : "0";
			try {
				long time1 = Long.valueOf(currentTime);
				long time2 = Long.valueOf(previewTime);
				if ((time1-time2) >= 5 * 60 ) {
					cell.timeTV.setVisibility(View.VISIBLE);
					cell.timeTV.setText(DateUtil.wechat_time(message.getTime()));
				}
				else {
					cell.timeTV.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				Logger.i(e);
			}
			return convertView;
=======
			catch (Exception e) {
				Logger.i(e);
			}
			return convertView;
		}
		
		private void displayLeftText(JsonMessage msg, ViewHolderLeftText viewHolderLeftText, int position) {
			imageLoader.displayImage(CommonValue.BASE_URL+ user.userHead, viewHolderLeftText.leftAvatar, options);
			viewHolderLeftText.leftText.setText(msg.text);
			displayTime(position, viewHolderLeftText.timeTV);
		}
		
		private void displayLeftImage(JsonMessage msg, ViewHolderLeftImage viewHolderLeftImage, int position) {
			imageLoader.displayImage(CommonValue.BASE_URL+ user.userHead, viewHolderLeftImage.leftAvatar, options);
			imageLoader.displayImage(msg.file, viewHolderLeftImage.leftPhoto, photooptions);
			displayTime(position, viewHolderLeftImage.timeTV);
		}
		
		private void displayLeftVoice(JsonMessage msg, ViewHolderLeftVoice viewHolderLeftVoice, int position) {
			imageLoader.displayImage(CommonValue.BASE_URL+ user.userHead, viewHolderLeftVoice.leftAvatar, options);
			displayTime(position, viewHolderLeftVoice.timeTV);
		}
		
		private void displayRightText(JsonMessage msg, ViewHolderRightText viewHolderRightText, int position) {
			imageLoader.displayImage(CommonValue.BASE_URL+ appContext.getLoginUserHead(), viewHolderRightText.rightAvatar, options);
			viewHolderRightText.rightText.setText(msg.text);
			displayTime(position, viewHolderRightText.timeTV);
		}
		
		private void displayRightImage(IMMessage message, JsonMessage msg, ViewHolderRightImage viewHolderRightImage, int position) {
			imageLoader.displayImage(CommonValue.BASE_URL+ appContext.getLoginUserHead(), viewHolderRightImage.rightAvatar, options);
			imageLoader.displayImage(msg.file, viewHolderRightImage.rightPhoto, photooptions);
			if (message.getType() == CommonValue.kWCMessageStatusWait) {
				message.setType(CommonValue.kWCMessageStatusSending);
				viewHolderRightImage.photoProgress.setVisibility(View.VISIBLE);
				imageLoader.displayImage("file:///"+msg.file, viewHolderRightImage.rightPhoto, photooptions);
				uploadImageToQiniu(message, msg.file, viewHolderRightImage, CommonValue.kWCMessageTypeImage);
			}
			else if (message.getType() == 0) {
				viewHolderRightImage.photoProgress.setVisibility(View.GONE);
			}
			displayTime(position, viewHolderRightImage.timeTV);
		}
		
		private void displayRightVoice(IMMessage message, JsonMessage msg, ViewHolderRightVoice viewHolderRightVoice, int position) {
			imageLoader.displayImage(CommonValue.BASE_URL+ appContext.getLoginUserHead(), viewHolderRightVoice.rightAvatar, options);
			if (message.getType() == CommonValue.kWCMessageStatusWait) {
				message.setType(CommonValue.kWCMessageStatusSending);
				viewHolderRightVoice.rightProgress.setVisibility(View.VISIBLE);
				uploadVoiceToQiniu(message, msg.file, viewHolderRightVoice, CommonValue.kWCMessageTypeVoice);
			}
			else if (message.getType() == 0) {
				viewHolderRightVoice.rightProgress.setVisibility(View.GONE);
			}
			displayTime(position, viewHolderRightVoice.timeTV);
		}
		
		private void displayTime(int position, TextView timeTV) {
//			String currentTime = items.get(position).getTime();
//			String previewTime = (position - 1) >= 0 ? items.get(position-1).getTime() : "0";
//			try {
//				long time1 = Long.valueOf(currentTime);
//				long time2 = Long.valueOf(previewTime);
//				if ((time1-time2) >= 5 * 60 ) {
//					timeTV.setVisibility(View.VISIBLE);
//					timeTV.setText(DateUtil.wechat_time(currentTime));
//				}
//				else {
//					timeTV.setVisibility(View.GONE);
//				}
//			} catch (Exception e) {
//				Logger.i(e);
//			}
>>>>>>> dev
		}
		
		private void uploadQiniu(final IMMessage message, String filePath, final ViewHoler cell, final int messageType) {
			String bucketName = "dchat";
	        PutPolicy putPolicy = new PutPolicy(bucketName);
			Config.ACCESS_KEY = "5e71GMRBlrPS5pjETWcgElaH-uvhGRsWRGMR_Pfs";
	        Config.SECRET_KEY = "cqzLJe_hA4YO33Oobp7AF0Fhca4q3EQ2rAfwS2YB";
	        Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
	        String auploadToken = null;
			try {
				auploadToken = putPolicy.token(mac);
				Logger.i(auploadToken);
			} catch (Exception e) {
				Logger.i(e);
			}
			String key = IO.UNDEFINED_KEY; 
			PutExtra extra = new PutExtra();
			extra.params = new HashMap<String, String>();
			IO.putFile(auploadToken, key, new File(filePath), extra, new JSONObjectRet() {
				@Override
				public void onProcess(long current, long total) {
					if (messageType == CommonValue.kWCMessageTypePlain) {
						float percent = (float) (current*1.0/total)*100;
						if ((int)percent < 100) {
							cell.photoProgress.setText((int)percent+"%");
						}
						else if ((int)percent == 100) {
							cell.photoProgress.setText("处理中...");
						}
					}
				}

				@Override
				public void onSuccess(JSONObject resp) {
					String key = resp.optString("hash", "");
					try {
						JsonMessage msg = new JsonMessage();
						msg.file = "http://dchat.qiniudn.com/"+key;
						Logger.i(msg.file);
						switch (messageType) {
						case CommonValue.kWCMessageTypeImage:
							msg.messageType = CommonValue.kWCMessageTypeImage;
							msg.text = "[图片]";
							break;

						case CommonValue.kWCMessageTypeVoice:
							msg.messageType = CommonValue.kWCMessageTypeVoice;
							msg.text = "[语音]";
							break;
						}
						Gson gson = new Gson();
						String json = gson.toJson(msg);
						message.setContent(json);
						sendMediaMessage(message);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(Exception ex) {
					Logger.i(ex.toString());
				}
			});
		}
	}
	
	@Override
	public void onBackPressed() {
		NoticeManager.getInstance(context).updateStatusByFrom(to, Notice.READ);
		super.onBackPressed();
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (view.getId()) {
		case R.id.voiceButton:
			voiceTouch(event);
			break;

		default:
			break;
		}
		return false;
	}
	
	private double voiceValue;
	private Dialog voiceDialog;
	private ImageView voiceImage;
	private static int MIN_TIME = 1;
	private static float recodeTime = 0.0f;
	private Thread recordThread;
	private boolean isRecording = false;
	void showVoiceDialog(){
		voiceDialog = new Dialog(this,R.style.VoiceDialogStyle);
		voiceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		voiceDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		voiceDialog.setContentView(R.layout.voice_dialog);
		voiceImage = (ImageView)voiceDialog.findViewById(R.id.dialog_img);
		voiceDialog.show();
	}
	void mythread(){
		recordThread = new Thread(ImgThread);
		recordThread.start();
	}
	void setDialogImage(){
		if (voiceValue < 200.0) {
			voiceImage.setImageResource(R.drawable.record_animate_01);
		}else if (voiceValue > 200.0 && voiceValue < 400) {
			voiceImage.setImageResource(R.drawable.record_animate_02);
		}else if (voiceValue > 400.0 && voiceValue < 800) {
			voiceImage.setImageResource(R.drawable.record_animate_03);
		}else if (voiceValue > 800.0 && voiceValue < 1600) {
			voiceImage.setImageResource(R.drawable.record_animate_04);
		}else if (voiceValue > 1600.0 && voiceValue < 3200) {
			voiceImage.setImageResource(R.drawable.record_animate_05);
		}else if (voiceValue > 3200.0 && voiceValue < 5000) {
			voiceImage.setImageResource(R.drawable.record_animate_06);
		}else if (voiceValue > 5000.0 && voiceValue < 7000) {
			voiceImage.setImageResource(R.drawable.record_animate_07);
		}else if (voiceValue > 7000.0 && voiceValue < 10000.0) {
			voiceImage.setImageResource(R.drawable.record_animate_08);
		}else if (voiceValue > 10000.0 && voiceValue < 14000.0) {
			voiceImage.setImageResource(R.drawable.record_animate_09);
		}else if (voiceValue > 14000.0 && voiceValue < 17000.0) {
			voiceImage.setImageResource(R.drawable.record_animate_10);
		}else if (voiceValue > 17000.0 && voiceValue < 20000.0) {
			voiceImage.setImageResource(R.drawable.record_animate_11);
		}else if (voiceValue > 20000.0 && voiceValue < 24000.0) {
			voiceImage.setImageResource(R.drawable.record_animate_12);
		}else if (voiceValue > 24000.0 && voiceValue < 28000.0) {
			voiceImage.setImageResource(R.drawable.record_animate_13);
		}else if (voiceValue > 28000.0) {
			voiceImage.setImageResource(R.drawable.record_animate_14);
		}
	}
	private void voiceTouch(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			try {
				isRecording = true;
				showVoiceDialog();
				AudioRecoderManager.getInstance(this).start();
				mythread();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			try {
				isRecording = false;
				if (voiceDialog.isShowing()) {
					voiceDialog.dismiss();
				}
				String voicePath = AudioRecoderManager.getInstance(this).stop();
				voiceValue = 0.0;
				if (recodeTime < MIN_TIME) {
					
				}
				else {
					uploadVoiceToQiniu(voicePath);
				}
			} catch (IOException e) {
					e.printStackTrace();
			}
			break;
		}
	}
	private Runnable ImgThread = new Runnable() {
		@Override
		public void run() {
			recodeTime = 0.0f;
			while (isRecording) {
				try {
					Thread.sleep(200);
					recodeTime += 0.2;
					voiceValue = AudioRecoderManager.getInstance(context).getAmplitude();
					imgHandle.sendEmptyMessage(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Handler imgHandle = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					setDialogImage();
					break;
				default:
					break;
				}
				
			}
		};
	};
}
