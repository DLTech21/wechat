/**
 * wechatdonal
 */
package bean;

import tools.AppException;
import tools.Logger;

import com.google.gson.Gson;

/**
 * wechat
 *
 * @author donal
 *
 */
public class JsonMessage {
	public String file;
	public String messageType;
	public String text;
	
	public static JsonMessage parse(String res)  {
		JsonMessage data = new JsonMessage();
		Gson gson = new Gson();
		data = gson.fromJson(res, JsonMessage.class);
		return data;
	}
}
