package bean;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import tools.AppException;
import tools.Logger;

public class UserEntity extends Entity{
	public String openid;
	public String nickname;
	public String sex;
	public String headimgurl;
	public String hash;
	
	public static UserEntity parse(String res) throws IOException, AppException {
		UserEntity data = new UserEntity();
		try {
			JSONObject js = new JSONObject(res);
			if (js.getInt("status") == 1) {
				data.error_code = Result.RESULT_OK;
				JSONObject info = js.getJSONObject("info");
				data.openid = info.getString("openid");
				data.sex = info.getString("sex");
				data.headimgurl = info.getString("headimgurl");
				data.nickname = info.getString("nickname");
				data.hash = info.getString("hash");
			}
			else {
				data.error_code = 11;
				data.message = js.getString("info");
			}
			
		} catch (JSONException e) {
			Logger.i(e);
			throw AppException.json(e);
		}
		return data;
	}
}
