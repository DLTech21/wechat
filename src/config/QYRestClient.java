package config;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * wechat
 *
 * @author donal
 *
 */
public class QYRestClient {

	  private static AsyncHttpClient client = new AsyncHttpClient();
	  
	  public static AsyncHttpClient getIntance() {
		  return client;
	  }

	  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	      client.get(getAbsoluteUrl(url), params, responseHandler);
	  }
	  
	  public static void getWeb(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	      client.get(context, url, params, responseHandler);
	  }

	  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	      client.post(getAbsoluteUrl(url), params, responseHandler);
	  }
	  
	  public static void post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	      client.post(context, getAbsoluteUrl(url), params, responseHandler);
	  }

	  private static String getAbsoluteUrl(String relativeUrl) {
		  client.setTimeout(10*1000);
		  client.setMaxConnections(5);
	      return CommonValue.BASE_API + relativeUrl;
	  }
}
