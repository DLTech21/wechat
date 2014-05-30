package config;

import tools.AppManager;
import tools.ImageUtils;
import android.graphics.Bitmap;

import com.donal.wechat.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

/**
 * wechat
 *
 * @author donal
 *
 */
public class CommonValue {
	public static String PackageName = "com.donal.wechat";
	
	public static String BASE_API = "http://115.29.150.153/api/";
	public static String BASE_URL = "http://115.29.150.153/";
	public static String BASE_XMPP_SERVER_NAME = "@hcios.com";
	
	public static int kWCMessageTypePlain = 0;
	public static int kWCMessageTypeImage = 1;
	public static int kWCMessageTypeVoice =2;
	public static int kWCMessageTypeLocation=3;
	
	public static int kWCMessageStatusWait = 1;
	public static int kWCMessageStatusSending = 2;
	
	// auil options
	public interface DisplayOptions {
		public DisplayImageOptions default_options = new DisplayImageOptions.Builder()
				.bitmapConfig(Bitmap.Config.RGB_565)
				.showImageOnLoading(R.drawable.content_image_loading)
				.showImageForEmptyUri(R.drawable.content_image_loading)
				.showImageOnFail(R.drawable.content_image_loading)
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) 
				.displayer(new BitmapDisplayer() {
					@Override
					public void display(Bitmap bitmap, ImageAware imageAware,
							LoadedFrom loadedFrom) {
						imageAware.setImageBitmap(bitmap);
					}
				})
				.build();
		
		public DisplayImageOptions avatar_options = new DisplayImageOptions.Builder()
		.bitmapConfig(Bitmap.Config.RGB_565)
		.showImageOnLoading(R.drawable.content_image_loading)
		.showImageForEmptyUri(R.drawable.content_image_loading)
		.showImageOnFail(R.drawable.content_image_loading)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) 
		.displayer(new RoundedBitmapDisplayer(ImageUtils.dip2px(AppManager.getAppManager().currentActivity(), 30)))
		.build();
	}
	
	public interface Operation {
		String addFriend = "加好友";
		String chatFriend = "发消息";
	}
	
	public static final String NEW_MESSAGE_ACTION = "chat.newmessage";

	/**
	 * USERINFO
	 */
	public static final String LOGIN_SET = "login_set";
	public static final String USERID = "userId";
	public static final String APIKEY = "apiKey";

	/**
	 * 重连接
	 */
	/**
	 * 重连接状态acttion
	 * 
	 */
	public static final String ACTION_RECONNECT_STATE = "action_reconnect_state";
	/**
	 * 描述重连接状态的关机子，寄放的intent的关键字
	 */
	public static final String RECONNECT_STATE = "reconnect_state";
	/**
	 * 描述重连接，
	 */
	public static final boolean RECONNECT_STATE_SUCCESS = true;
	public static final boolean RECONNECT_STATE_FAIL = false;
	
	/**
	 * 注册
	 */
	public static final int REQUEST_REGISTER_INFO = 1;
}
