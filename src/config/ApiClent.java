package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.AudioRecoderManager;
import tools.Logger;
import tools.MD5Util;
import tools.StringUtils;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import bean.StrangerEntity;
import bean.Update;
import bean.UserDetail;
import bean.UserEntity;
import bean.UserInfo;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
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
					Logger.i(new String(responseBody));
					JSONObject json = new JSONObject(new String(responseBody));
					if (json.getInt("status") == 1) {
						JSONArray file = json.getJSONArray("files");
						JSONObject f = file.getJSONObject(0);
						String head = f.getString("fileId");
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
						user.userHead = userHead;
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
	
	//下载
	public static void downVoiceFromQiniu(Context context, final String url, final String format, final ClientCallback callback) {
		QYRestClient.getWeb(context, url, null, new BinaryHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] binaryData) {
				handleDownloadFile(binaryData, callback, url, format);
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				callback.onFailure("语音地址有错");
			}
		});
	}
	public static void handleDownloadFile(final byte[] binaryData, final ClientCallback callback, final String url, final String format) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					callback.onSuccess((String)msg.obj);
					break;

				default:
					callback.onError((Exception)msg.obj);
					break;
				}
			}
		};
		ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				String storageState = Environment.getExternalStorageState();	
				String savePath = null;
				if(storageState.equals(Environment.MEDIA_MOUNTED)){
					savePath = AudioRecoderManager.CACHE_VOICE_FILE_PATH;
					File dir = new File(savePath);
					if(!dir.exists()){
						dir.mkdirs();
					}
				}
				String md5FilePath = savePath + MD5Util.getMD5String(url) + format;
				File ApkFile = new File(md5FilePath);
				if(ApkFile.exists()){
					ApkFile.delete();
				}
				File tmpFile = new File(md5FilePath);
				try {
					FileOutputStream fos = new FileOutputStream(tmpFile);
					fos.write(binaryData);
					fos.close();
					msg.what = 1;
					msg.obj = md5FilePath;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				} 
				handler.sendMessage(msg);
			}
		});
	}
}
