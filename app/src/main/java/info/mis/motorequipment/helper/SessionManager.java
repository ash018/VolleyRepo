package info.mis.motorequipment.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
	// LogCat tag
	private static String TAG = SessionManager.class.getSimpleName();

	// Shared Preferences
	SharedPreferences pref;

	Editor editor;
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Shared preferences file name
	private static final String PREF_NAME = "EMPLogin";
	
	private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}


	public void setRoleId(String RoleId) {
		pref.edit().putString("RoleId", RoleId).commit();
	}

	public void setUserName(String UserName) {
		pref.edit().putString("UserName", UserName).commit();
	}

	public void setUserId(String UserId) {
		pref.edit().putString("UserId", UserId).commit();
	}

	public String getRoleId() {
		String RoleId = pref.getString("RoleId","");
		return RoleId;
	}

	public String getUserName() {
		String UserName = pref.getString("UserName","");
		return UserName;
	}

	public String getUserId() {
		String UserId = pref.getString("UserId","");
		return UserId;
	}

	public void setLogin(boolean isLoggedIn) {

		editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);

		// commit changes
		editor.commit();

		Log.d(TAG, "User login session modified!");
	}
	
	public boolean isLoggedIn(){
		return pref.getBoolean(KEY_IS_LOGGED_IN, false);
	}
}
