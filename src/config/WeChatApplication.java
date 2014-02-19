package config;

import java.util.List;
import java.util.Properties;

import org.apache.http.client.CookieStore;

import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.utils.L;

import tools.AppContext;
import tools.AppException;
import tools.ImageCacheUtil;
import tools.Logger;
import tools.StringUtils;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;

/**
 * wechat
 *
 * @author donal
 *
 */
public class WeChatApplication extends AppContext {
	private static WeChatApplication mApplication;
	
	private NotificationManager mNotificationManager;
	
	private boolean login = false;	//登录状态
	private String loginUid = "0";	//登录用户的id
	
	
	public synchronized static WeChatApplication getInstance() {
		return mApplication;
	}
	
	public NotificationManager getNotificationManager() {
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}
	
	public void onCreate() {
		mApplication = this;
		Logger.setDebug(true);
		Logger.getLogger().setTag("wechat");
		ImageCacheUtil.init(this);
		Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
		L.disableLogging();
		mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		CookieStore cookieStore = new PersistentCookieStore(this);  
		QYRestClient.getIntance().setCookieStore(cookieStore);
		
		Intent intent = new Intent();
        intent.setAction("tools.NetworkState.Service");
        startService(intent);
	}
	
	/**
	 * 用户是否登录
	 * @return
	 */
	public boolean isLogin() {
		try {
			String loginStr = getProperty("user.login");
			if (StringUtils.isEmpty(loginStr)) {
				login = false;
			}
			else {
				login = (loginStr.equals("1")) ? true : false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return login;
	}

	/**
	 * 保存登录信息
	 * @param username
	 * @param pwd
	 */
//	@SuppressWarnings("serial")
//	public void saveLoginInfo(final UserEntity user) {
//		this.loginUid = user.openid;
//		this.login = true;
//		setProperties(new Properties(){
//			{
//				setProperty("user.login","1");
//				setProperty("user.uid", user.openid);
//				setProperty("user.name", user.nickname);
//				setProperty("user.face", user.headimgurl);
//				setProperty("user.hash", user.hash);
//			}
//		});		
//	}

	/**
	 * 获取登录用户id
	 * @return
	 */
	public String getLoginUid() {
		return (getProperty("user.uid"));
	}
	
	public String getLoginHash() {
		return (getProperty("user.hash"));
	}

	/**
	 * 获取登录信息
	 * @return
	 */
//	public UserEntity getLoginInfo() {		
//		UserEntity lu = new UserEntity();		
//		lu.openid = (getProperty("user.uid"));
//		lu.nickname = (getProperty("user.name"));
//		lu.headimgurl = (getProperty("user.face"));
//		return lu;
//	}
	
	public String getNickname() {		
		return (getProperty("user.name"));
	}
	
	/**
	 * 退出登录
	 */
	public void setUserLogout() {
		this.login = false;
		setProperties(new Properties(){
			{
				setProperty("user.login","0");
			}
		});	
	}
	
	public boolean isNeedCheckLogin() {
		try {
			String loginStr = getProperty("user.needchecklogin");
			if (StringUtils.isEmpty(loginStr)) {
				return false;
			}
			else {
				return (loginStr.equals("1")) ? true : false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void setNeedCheckLogin() {
		setProperties(new Properties(){
			{
				setProperty("user.needchecklogin","1");
			}
		});
	}
	
	public void saveNotiWhen(final String when) {
		setProperties(new Properties(){
			{
				setProperty("noti.when",when);
			}
		});
	}
	
	public String getNotiWhen() {
		try {
			String loginStr = getProperty("noti.when");
			if (StringUtils.isEmpty(loginStr)) {
				return "0";
			}
			else {
				return loginStr;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "0";
	}
	
}
