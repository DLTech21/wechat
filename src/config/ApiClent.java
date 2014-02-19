package config;

import java.io.Serializable;

import org.apache.http.Header;

import tools.AppException;
import tools.Logger;
import tools.StringUtils;

import bean.Update;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class ApiClent {
	public interface ClientCallback{
        abstract void onSuccess(Object data);
        abstract void onFailure(String message);
        abstract void onError(Exception e);
    }
	
	private static void saveCache(WeChatApplication appContext, String key, Serializable entity) {
    	appContext.saveObject(entity, key);
    }
	
	public static void login(WeChatApplication appContext, String mobile, String password, ClientCallback callback) {
		RequestParams params = new RequestParams();
		params.add("mobile", mobile);
		params.add("uPass", password);
		params.add("versionInfo", " ");
		params.add("deviceInfo", " ");
		QYRestClient.post("login.do", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				Logger.i(new String(responseBody));
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				
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
