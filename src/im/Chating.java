/**
 * wechatdonal
 */
package im;

import im.model.IMMessage;
import im.model.Notice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import tools.AppManager;
import tools.AudioPlayManager;
import tools.AudioRecoderManager;
import tools.DateUtil;
import tools.ImageUtils;
import tools.Logger;
import tools.MD5Util;
import tools.StringUtils;
import tools.UIHelper;
import bean.JsonMessage;
import bean.UserInfo;

import com.crashlytics.android.Crashlytics;
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
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * wechat
 *
 * @author donal
 *
 */
public class Chating extends AChating implements OnTouchListener, OnItemClickListener, VoiceBubbleListener, OnEditorActionListener{
	private boolean isFace = false;
	private Button voiceOrTextButton;
	private Button faceOrTextButton;
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
	
	private AnimationDrawable leftAnimationDrawable;
	private AnimationDrawable rightAnimationDrawable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chating);
		initUI();
		user = FriendManager.getInstance(context).getFriend(to.split("@")[0]);
	}
	
	private void initUI() {
		faceOrTextButton = (Button) findViewById(R.id.faceOrTextButton);
		voiceOrTextButton = (Button) findViewById(R.id.voiceOrTextButton);
		voiceButton = (Button) findViewById(R.id.voiceButton);
		voiceButton.setOnTouchListener(this);
		listView = (ListView) findViewById(R.id.chat_list);
		listView.setCacheColorHint(0);
		adapter = new MessageListAdapter(Chating.this, getMessages(),
				listView);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
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
		messageInput.setOnEditorActionListener(this);
	}
	
	
	
	public void ButtonClick(View v) {
		switch (v.getId()) {
		case R.id.leftBarButton:
			closeInput();
			AppManager.getAppManager().finishActivity(this);
			break;
		case R.id.voiceOrTextButton:
			if (messageInput.getVisibility() == View.VISIBLE) {
				closeInput();
				messageInput.setVisibility(View.INVISIBLE);
				voiceButton.setVisibility(View.VISIBLE);
				voiceOrTextButton.setBackgroundResource(R.drawable.keyborad);
				faceOrTextButton.setBackgroundResource(R.drawable.face);
			}
			else if (messageInput.getVisibility() == View.INVISIBLE) {
				messageInput.setVisibility(View.VISIBLE);
				voiceButton.setVisibility(View.INVISIBLE);
				voiceOrTextButton.setBackgroundResource(R.drawable.voice);
				
			}
			break;
		case R.id.faceOrTextButton:
			if (messageInput.getVisibility() == View.VISIBLE) {
				if (!isFace) {
					isFace = true;
					closeInput();
					faceOrTextButton.setBackgroundResource(R.drawable.keyborad);
					//show face key borad
				}
				else {
					isFace = false;
					faceOrTextButton.setBackgroundResource(R.drawable.face);
					//hide face key borad
				}
				voiceOrTextButton.setBackgroundResource(R.drawable.voice);
			}
			else if (messageInput.getVisibility() == View.INVISIBLE) {
				messageInput.setVisibility(View.VISIBLE);
				voiceButton.setVisibility(View.INVISIBLE);
				voiceOrTextButton.setBackgroundResource(R.drawable.voice);
				faceOrTextButton.setBackgroundResource(R.drawable.keyborad);
			}
			break;

		case R.id.multiMediaButton:
			AudioPlayManager.getInstance(this, this).stopPlay();
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
					Crashlytics.logException(e);
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
				Crashlytics.logException(e);
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
							Intent intent = new Intent(Intent.ACTION_PICK,
									Media.EXTERNAL_CONTENT_URI);
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

		class ViewHolderLeftText {
			TextView timeTV;
			ImageView leftAvatar;
			TextView leftNickname;
			TextView leftText;
		}
		
		class ViewHolderLeftImage {
			TextView timeTV;
			ImageView leftAvatar;
			TextView leftNickname;
			ImageView leftPhoto;
		}
		
		class ViewHolderLeftVoice {
			TextView timeTV;
			ImageView leftAvatar;
			TextView leftNickname;
			ImageView leftVoice;
		}
		
		class ViewHolderRightText {
			TextView timeTV;
			ImageView rightAvatar;
			TextView rightNickname;
			TextView rightText;
			ProgressBar rightProgress;
		}
		
		class ViewHolderRightImage {
			TextView timeTV;
			ImageView rightAvatar;
			TextView rightNickname;
			ImageView rightPhoto;
			TextView photoProgress;
			ProgressBar rightProgress;
		}
		
		class ViewHolderRightVoice {
			TextView timeTV;
			ImageView rightAvatar;
			TextView rightNickname;
			ImageView rightVoice;
			ProgressBar rightProgress;
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
			if (this.items.size()>1) {
				listView.setSelection(items.size()-1);
			}
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
			ViewHolderRightText holderRightText = null;
			ViewHolderRightImage holderRightImg = null;
			ViewHolderRightVoice holderRightVoice = null;
			ViewHolderLeftText holderLeftText = null;
			ViewHolderLeftImage holderLeftImg = null;
			ViewHolderLeftVoice holderLeftVoice = null;
			try {
				IMMessage message = items.get(position);
				String content = message.getContent();
				JsonMessage msg = JsonMessage.parse(content);
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
							holderLeftVoice.leftVoice = (ImageView) convertView.findViewById(R.id.receiverVoiceNode);
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
							holderRightVoice.rightVoice = (ImageView) convertView.findViewById(R.id.senderVoiceNode);
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
								holderLeftVoice.leftVoice = (ImageView) convertView.findViewById(R.id.receiverVoiceNode);
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
								holderRightVoice.rightVoice = (ImageView) convertView.findViewById(R.id.senderVoiceNode);
								holderRightVoice.rightProgress = (ProgressBar) convertView.findViewById(R.id.view_progress_r);
								displayRightVoice(message, msg, holderRightVoice, position);
								convertView.setTag(holderRightVoice);
							}
							break;
						}
					}
				}
			}
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
			imageLoader.displayImage("file://"+stringtoBitmap(msg.file), viewHolderLeftImage.leftPhoto, photooptions);
			displayTime(position, viewHolderLeftImage.timeTV);
		}
		
		private void displayLeftVoice(JsonMessage msg, ViewHolderLeftVoice viewHolderLeftVoice, int position) {
			viewHolderLeftVoice.leftVoice.setTag(msg.file);
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
			Logger.i(msg.file);
			imageLoader.displayImage("file://"+stringtoBitmap(msg.file), viewHolderRightImage.rightPhoto, photooptions);
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
			viewHolderRightVoice.rightVoice.setTag(msg.file);
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
			String currentTime = items.get(position).getTime();
			String previewTime = (position - 1) >= 0 ? items.get(position-1).getTime() : "0";
			try {
				long time1 = Long.valueOf(currentTime);
				long time2 = Long.valueOf(previewTime);
				if ((time1-time2) >= 5 * 60 ) {
					timeTV.setVisibility(View.VISIBLE);
					timeTV.setText(DateUtil.wechat_time(currentTime));
				}
				else {
					timeTV.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				Logger.i(e);
			}
		}
		
		private void uploadImageToQiniu(final IMMessage message, String filePath, final ViewHolderRightImage cell, final int messageType) {
			try {
				JsonMessage msg = new JsonMessage();
				msg.file = Bitmap2StrByBase64(filePath);
				msg.messageType = CommonValue.kWCMessageTypeImage;
				msg.text = "[图片]";
				Gson gson = new Gson();
				String json = gson.toJson(msg);
				message.setContent(json);
				sendMediaMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
//			String bucketName = "dchat";
//	        PutPolicy putPolicy = new PutPolicy(bucketName);
//			Config.ACCESS_KEY = "ZJQmyoJNrIuXq81X-naqxYkMkjlvhNUiVgdQXBk6";
//	        Config.SECRET_KEY = "NrM8Fl1OmxqliLTtoP9m94JkIGf8vIuca7O_OG-9";
//	        Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
//	        String auploadToken = null;
//			try {
//				auploadToken = putPolicy.token(mac);
//				Logger.i(auploadToken);
//			} catch (Exception e) {
//				Logger.i(e);
//			}
//			String key = IO.UNDEFINED_KEY; 
//			PutExtra extra = new PutExtra();
//			extra.params = new HashMap<String, String>();
//			IO.putFile(auploadToken, key, new File(filePath), extra, new JSONObjectRet() {
//				@Override
//				public void onProcess(long current, long total) {
//					if (messageType == CommonValue.kWCMessageTypePlain) {
//						float percent = (float) (current*1.0/total)*100;
//						if ((int)percent < 100) {
//							cell.photoProgress.setText((int)percent+"%");
//						}
//						else if ((int)percent == 100) {
//							cell.photoProgress.setText("处理中...");
//						}
//					}
//				}
//
//				@Override
//				public void onSuccess(JSONObject resp) {
//					String key = resp.optString("hash", "");
//					try {
//						JsonMessage msg = new JsonMessage();
//						msg.file = "http://7xj66h.com1.z0.glb.clouddn.com/"+key;
//						Logger.i(msg.file);
//						switch (messageType) {
//						case CommonValue.kWCMessageTypeImage:
//							msg.messageType = CommonValue.kWCMessageTypeImage;
//							msg.text = "[图片]";
//							break;
//
//						case CommonValue.kWCMessageTypeVoice:
//							msg.messageType = CommonValue.kWCMessageTypeVoice;
//							msg.text = "[语音]";
//							break;
//						}
//						Gson gson = new Gson();
//						String json = gson.toJson(msg);
//						message.setContent(json);
//						sendMediaMessage(message);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//
//				@Override
//				public void onFailure(Exception ex) {
//					Logger.i(ex.toString());
//				}
//			});
		}
		
		private void uploadVoiceToQiniu(final IMMessage message, String filePath, final ViewHolderRightVoice cell, final int messageType) {
			
			try {
				JsonMessage msg = new JsonMessage();
				msg.file = encodeBase64File(filePath);
				msg.messageType = CommonValue.kWCMessageTypeVoice;
				msg.text = "[语音]";
				Gson gson = new Gson();
				String json = gson.toJson(msg);
				message.setContent(json);
				sendMediaMessage(message);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
//			String bucketName = "dchat";
//	        PutPolicy putPolicy = new PutPolicy(bucketName);
//			Config.ACCESS_KEY = "ZJQmyoJNrIuXq81X-naqxYkMkjlvhNUiVgdQXBk6";
//	        Config.SECRET_KEY = "NrM8Fl1OmxqliLTtoP9m94JkIGf8vIuca7O_OG-9";
//	        Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
//	        String auploadToken = null;
//			try {
//				auploadToken = putPolicy.token(mac);
//				Logger.i(auploadToken);
//			} catch (Exception e) {
//				Logger.i(e);
//			}
//			String key = IO.UNDEFINED_KEY; 
//			PutExtra extra = new PutExtra();
//			extra.params = new HashMap<String, String>();
//			IO.putFile(auploadToken, key, new File(filePath), extra, new JSONObjectRet() {
//				@Override
//				public void onProcess(long current, long total) {
//				}
//
//				@Override
//				public void onSuccess(JSONObject resp) {
//					String key = resp.optString("hash", "");
//					try {
//						JsonMessage msg = new JsonMessage();
//						msg.file = "http://7xj66h.com1.z0.glb.clouddn.com/"+key;
//						Logger.i(msg.file);
//						switch (messageType) {
//						case CommonValue.kWCMessageTypeImage:
//							msg.messageType = CommonValue.kWCMessageTypeImage;
//							msg.text = "[图片]";
//							break;
//
//						case CommonValue.kWCMessageTypeVoice:
//							msg.messageType = CommonValue.kWCMessageTypeVoice;
//							msg.text = "[语音]";
//							break;
//						}
//						Gson gson = new Gson();
//						String json = gson.toJson(msg);
//						message.setContent(json);
//						sendMediaMessage(message);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//
//				@Override
//				public void onFailure(Exception ex) {
//					Logger.i(ex.toString());
//				}
//			});
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
				AudioPlayManager.getInstance(this, this).stopPlay();
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
			AudioRecoderManager.destroy();
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
	
	protected void onDestroy() {
		AudioPlayManager.getInstance(this, null).stopPlay();
		AudioRecoderManager.destroy();
		AudioPlayManager.destroy();
		super.onDestroy();
	};
	
	
	private String currentVoice = "";
	private View currentConvertView;

	@Override
	public void onItemClick(AdapterView<?> arg0, View convertView, int position, long arg3) {
		if (convertView.getTag() instanceof im.Chating.MessageListAdapter.ViewHolderLeftVoice) {
			im.Chating.MessageListAdapter.ViewHolderLeftVoice viewHolderLeftVoice = (im.Chating.MessageListAdapter.ViewHolderLeftVoice) convertView.getTag();
			String voice = (String) viewHolderLeftVoice.leftVoice.getTag();
			if (currentVoice.equals(voice)) {
				AudioPlayManager audioPlayManager = AudioPlayManager.getInstance(this, this);
				audioPlayManager.setConvertView(convertView);
				audioPlayManager.setURL(voice);
				audioPlayManager.startStopPlay();
			}
			else {
				if (currentConvertView != null) {
					this.playStoped(currentConvertView);
				}
				AudioPlayManager audioPlayManager = AudioPlayManager.getInstance(this, this);
				audioPlayManager.stopPlay();
				audioPlayManager.setConvertView(convertView);
				audioPlayManager.setURL(voice);
				audioPlayManager.startStopPlay();
			}
			currentVoice = voice;
			currentConvertView = convertView;
		}
		else if (convertView.getTag() instanceof im.Chating.MessageListAdapter.ViewHolderRightVoice) {
			im.Chating.MessageListAdapter.ViewHolderRightVoice viewHolderRightVoice = (im.Chating.MessageListAdapter.ViewHolderRightVoice) convertView.getTag();
			String voice = (String) viewHolderRightVoice.rightVoice.getTag();
			if (currentVoice.equals(voice)) {
				AudioPlayManager audioPlayManager = AudioPlayManager.getInstance(this, this);
				audioPlayManager.setConvertView(convertView);
				audioPlayManager.setURL(voice);
				audioPlayManager.startStopPlay();
			}
			else {
				if (currentConvertView != null) {
					this.playStoped(currentConvertView);
				}
				AudioPlayManager audioPlayManager = AudioPlayManager.getInstance(this, this);
				audioPlayManager.stopPlay();
				audioPlayManager.setConvertView(convertView);
				audioPlayManager.setURL(voice);
				audioPlayManager.startStopPlay();
			}
			currentVoice = voice;
			currentConvertView = convertView;
		}
		else if (convertView.getTag() instanceof im.Chating.MessageListAdapter.ViewHolderLeftImage) {
			
		}
		else if (convertView.getTag() instanceof im.Chating.MessageListAdapter.ViewHolderRightImage) {
			
		}
	}

	@Override
	public void playFail(View messageBubble) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playStoped(View messageBubble) {
		if (messageBubble.getTag() instanceof im.Chating.MessageListAdapter.ViewHolderLeftVoice) {
			im.Chating.MessageListAdapter.ViewHolderLeftVoice viewHolderLeftVoice = (im.Chating.MessageListAdapter.ViewHolderLeftVoice) messageBubble.getTag();
			if (leftAnimationDrawable != null && leftAnimationDrawable.isRunning()) {
				leftAnimationDrawable.stop();
			}
			viewHolderLeftVoice.leftVoice.setImageResource(R.drawable.chatfrom_voice_playing);
		}
		else if (messageBubble.getTag() instanceof im.Chating.MessageListAdapter.ViewHolderRightVoice) {
			im.Chating.MessageListAdapter.ViewHolderRightVoice viewHolderRightVoice = (im.Chating.MessageListAdapter.ViewHolderRightVoice) messageBubble.getTag();
			if (rightAnimationDrawable != null && rightAnimationDrawable.isRunning()) {
				rightAnimationDrawable.stop();
			}
			viewHolderRightVoice.rightVoice.setImageResource(R.drawable.chatto_voice_playing_f3);
		}
	}

	@Override
	public void playStart(View messageBubble) {
		if (messageBubble.getTag() instanceof im.Chating.MessageListAdapter.ViewHolderLeftVoice) {
			im.Chating.MessageListAdapter.ViewHolderLeftVoice viewHolderLeftVoice = (im.Chating.MessageListAdapter.ViewHolderLeftVoice) messageBubble.getTag();
			viewHolderLeftVoice.leftVoice.setImageResource(R.anim.receiver_voice_node_playing);
			leftAnimationDrawable = (AnimationDrawable) viewHolderLeftVoice.leftVoice.getDrawable();
			leftAnimationDrawable.start();
		}
		else if (messageBubble.getTag() instanceof im.Chating.MessageListAdapter.ViewHolderRightVoice) {
			im.Chating.MessageListAdapter.ViewHolderRightVoice viewHolderRightVoice = (im.Chating.MessageListAdapter.ViewHolderRightVoice) messageBubble.getTag();
			viewHolderRightVoice.rightVoice.setImageResource(R.anim.sender_voice_node_playing);
			rightAnimationDrawable = (AnimationDrawable) viewHolderRightVoice.rightVoice.getDrawable();
			rightAnimationDrawable.start();
		}
	}

	@Override
	public void playCompletion(View messageBubble) {
		
		if (messageBubble.getTag() instanceof im.Chating.MessageListAdapter.ViewHolderLeftVoice) {
			im.Chating.MessageListAdapter.ViewHolderLeftVoice viewHolderLeftVoice = (im.Chating.MessageListAdapter.ViewHolderLeftVoice) messageBubble.getTag();
			if (leftAnimationDrawable != null && leftAnimationDrawable.isRunning()) {
				leftAnimationDrawable.stop();
			}
			viewHolderLeftVoice.leftVoice.setImageResource(R.drawable.chatfrom_voice_playing);
		}
		else if (messageBubble.getTag() instanceof im.Chating.MessageListAdapter.ViewHolderRightVoice) {
			im.Chating.MessageListAdapter.ViewHolderRightVoice viewHolderRightVoice = (im.Chating.MessageListAdapter.ViewHolderRightVoice) messageBubble.getTag();
			if (rightAnimationDrawable != null && rightAnimationDrawable.isRunning()) {
				rightAnimationDrawable.stop();
			}
			viewHolderRightVoice.rightVoice.setImageResource(R.drawable.chatto_voice_playing_f3);
		}
	}
	
	@Override
	public void playDownload(View messageBubble) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent arg2) {
		switch (actionId) {
		case EditorInfo.IME_ACTION_SEND:
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

		default:
			break;
		}
		return true;
	}
	
	public String Bitmap2StrByBase64(String filePath){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        ImageUtils.getBitmapByPath(filePath).compress(Bitmap.CompressFormat.JPEG, 40, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
  
    public String stringtoBitmap(String string) {
    	String bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = ImageUtils.saveBitmapByte(bitmapArray, MD5Util.getMD5String(string) + ".png");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    
    public static String encodeBase64File(String path) throws Exception {
        File  file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

        
}
