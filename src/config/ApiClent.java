package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.Logger;
import tools.StringUtils;
import android.content.Entity;
import bean.StrangerEntity;
import bean.Update;
import bean.UserDetail;
import bean.UserEntity;
import bean.UserInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ApiClent {
	public final static String message_error = "服务器连接有问题";
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
	
	public static void register(WCApplication appContext, String mobile, String password, String nickname, String intro, String avatar, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("mobile", mobile);
		params.add("uPass", password);
		params.add("nickName", nickname);
		params.add("description", intro);
		params.add("userHead", avatar);
		QYRestClient.post("register.do", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					bean.Entity data = new bean.Entity() {
					};
					JSONObject json = new JSONObject(new String(responseBody));
					data.setError_code(json.getInt("status"));
					data.setMessage(json.getString("msg"));
					callback.onSuccess(data);
				} catch (JSONException e) {
					e.printStackTrace();
					callback.onError(e);
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
		if (!StringUtils.empty(nickName)) {
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
				try {
					JSONObject js = new JSONObject(new String(responseBody));
					int status = js.getInt("status");
					if (status == 1) {
						callback.onSuccess(js.getString("msg"));
					} else {
						callback.onFailure(js.getString("msg"));
					}
				} catch (JSONException e) {
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
	
	public static void uploadFile(String apiKey, String filepath, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		File myFile = new File(filepath);  
		params.add("apiKey", apiKey);
		try {
			params.put("file", myFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		QYRestClient.post("uploadFile.do", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					JSONObject json = new JSONObject(new String(responseBody));
					if (json.getInt("status") == 1) {
						JSONArray file = json.getJSONArray("files");
						JSONObject f = file.getJSONObject(0);
						String head = f.getString("shortPath");
						callback.onSuccess(head);
					}
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
	
	public static void modifiedUser(final WCApplication appContext, String apiKey, String nickname, String head, String des, final ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("apiKey", apiKey);
		if (StringUtils.notEmpty(nickname)) {
			params.add("nickName", nickname);
		}
		if (StringUtils.notEmpty(head)) {
			params.add("userHead", head);
		}
		if (StringUtils.notEmpty(des)) {
			params.add("description", des);
		}
		QYRestClient.post("modUserInfo.do", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try {
					Logger.i(new String(responseBody));
					JSONObject json = new JSONObject(new String(responseBody));
					if (json.getInt("status") == 1) {
						JSONObject userDetail = json.getJSONObject("userDetail");
						String nickName = userDetail.getString("nickName");
						String description = userDetail.getString("description");
						String userHead = userDetail.getString("userHead");
						UserInfo user = new UserInfo();
						user.nickName = nickName;
						user.description = description;
						appContext.modifyLoginInfo(user);
						callback.onSuccess("修改成功");
					}
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
}
