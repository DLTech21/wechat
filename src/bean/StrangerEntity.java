/**
 * wechatdonal
 */
package bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import tools.AppException;
import tools.Logger;

import com.google.gson.Gson;

/**
 * wechat
 *
 * @author donal
 *
 */
public class StrangerEntity implements Serializable{
	public int status;
	public String msg;
	public List<UserInfo> userList;
	
	public static StrangerEntity parse(String res) throws IOException, AppException{
		StrangerEntity data = new StrangerEntity();
		try {
			Gson gson = new Gson();
			data = gson.fromJson(res, StrangerEntity.class);
		} catch (Exception e) {
			Logger.i(res);
			Logger.i(e);
			throw AppException.json(e);
		}
		return data;
	}
}
