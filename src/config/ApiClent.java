package config;

import java.io.IOException;
import java.io.Serializable;

import org.apache.http.Header;

import tools.AppException;
import tools.Logger;
import tools.StringUtils;

import bean.StrangerEntity;
import bean.Update;
import bean.UserDetail;
import bean.UserEntity;
import bean.UserInfo;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ApiClent {
	public final static String message_error = "";
	public interface ClientCallback{
        abstract void onSuccess(Object data);
        abstract void onFailure(String message);
        abstract void onError(Exception e);
    }
	
	private static void saveCache(WCApplication appContext, String key, Serializable entity) {
    	appContext.saveObject(entity, key);
    }
	
	public static void login(WCApplication appContext, String mobile, String password, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("mobile", mobile);
		params.add("uPass", password);
		params.add("versionInfo", " ");
		params.add("deviceInfo", " ");
		QYRestClient.post("login.do", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					UserEntity user = UserEntity.parse(new String(responseBody));
					callback.onSuccess(user);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (AppException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				callback.onFailure(message_error);
			}
		});
	}
	
	public static void findFriend(WCApplication appContext, String apiKey, String page, String pageSize, String nickName, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("apiKey", apiKey);
		params.add("pageIndex", page);
		params.add("pageSize", pageSize);
		if (!StringUtils.isEmpty(nickName)) {
			params.add("nickName", nickName);
		}
		QYRestClient.post("findFriend.do", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					StrangerEntity data = StrangerEntity.parse(new String(responseBody));
					callback.onSuccess(data);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (AppException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				callback.onFailure(message_error);
			}
		});
	}
	
	public static void addFriend(WCApplication appContext, String apiKey, String userId, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("apiKey", apiKey);
		params.add("userId", userId);
		QYRestClient.post("addFriend.do", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				Logger.i(new String(responseBody));
//				try {
//					StrangerEntity data = StrangerEntity.parse(new String(responseBody));
//					callback.onSuccess(data);
//				} catch (IOException e) {
//					e.printStackTrace();
//				} catch (AppException e) {
//					e.printStackTrace();
//				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				callback.onFailure(message_error);
			}
		});
	}
	
	public static void deleteFriend(WCApplication appContext, String apiKey, String userId, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("apiKey", apiKey);
		params.add("userId", userId);
		QYRestClient.post("deleteFriend.do", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				Logger.i(new String(responseBody));
//				try {
//					StrangerEntity data = StrangerEntity.parse(new String(responseBody));
//					callback.onSuccess(data);
//				} catch (IOException e) {
//					e.printStackTrace();
//				} catch (AppException e) {
//					e.printStackTrace();
//				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				callback.onFailure(message_error);
			}
		});
	}
	
	public static void getMyFriend(WCApplication appContext, String apiKey, String page, String pageSize, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("apiKey", apiKey);
//		params.add("pageIndex", page);
//		params.add("pageSize", pageSize);
		QYRestClient.post("getMyFriends.do", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					StrangerEntity data = StrangerEntity.parse(new String(responseBody));
					callback.onSuccess(data);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (AppException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				callback.onFailure(message_error);
			}
		});
	}
	
	public static void getUserInfo(String apiKey, String userId, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("apiKey", apiKey);
		params.add("userId", userId);
		QYRestClient.post("getUserDetail.do", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					UserDetail data = UserDetail.parse(new String(responseBody));
					callback.onSuccess(data);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				callback.onFailure(message_error);
			}
		});
	}
	
	public static void update(final ClientCallback callback) {
		RequestParams params = new RequestParams();
		QYRestClient.get("http://www.hdletgo.com/check.php", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				Update update;
				try {
					update = Update.parse(new String(responseBody));
					callback.onSuccess(update);
				} catch (AppException e) {
					Logger.i(e);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				
			}
		});
	}
}
