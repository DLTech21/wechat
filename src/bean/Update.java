/**
 * 
 */
package bean;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import tools.AppException;
import tools.Logger;

/**
 * @author Donal Tong 
 * momoka
 * 2013-1-12
 */
public class Update extends Entity {
	public String version_code;
	public String version_name;
	public String app_name;
	public String app_url;
	public String update_log;
	
	/**
	 * @param postRequest
	 * @return
	 */
	public static Update parse(String postRequest) throws AppException{
		Update update = new Update();
		try {
			
			Gson gson = new Gson();
			update = gson.fromJson(postRequest, Update.class);
		} catch (Exception e) {
			Logger.i(e);
			throw AppException.json(e);
		}
		return update;
	}
	
	
	
}
