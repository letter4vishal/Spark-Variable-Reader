package com.example.sparkrc;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;

public class EditLogin extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_login);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		String deviceId = settings.getString("deviceId",  "");
		String accessToken = settings.getString("accessToken", "");
		boolean showAccessToken = settings.getBoolean("showAccessToken",  false);
		
		TextView d = (TextView)findViewById(R.id.EditLogin_deviceId);
		d.setText(deviceId);
		
		if(showAccessToken){
			TextView a = (TextView)findViewById(R.id.EditLogin_accessToken);
			a.setText(accessToken);
		}
		
		CheckBox s = (CheckBox)findViewById(R.id.EditLogin_showAccessToken);
		s.setChecked(showAccessToken);
	}
	
	public void saveLogin(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();	
		
		TextView d = (TextView)findViewById(R.id.EditLogin_deviceId);
		TextView a = (TextView)findViewById(R.id.EditLogin_accessToken);
		CheckBox s = (CheckBox)findViewById(R.id.EditLogin_showAccessToken);
		
		String deviceId = d.getText().toString();
		String accessToken = a.getText().toString();
		Boolean showAccessToken = s.isChecked();
		
		if(deviceId.length() != 24 || accessToken.length() != 40){
			TextView e = (TextView)findViewById(R.id.editLogin);
			e.setText("Error: Invalid credentials! (a missing character?)");
		}
		else{
			editor.putString("deviceId", deviceId);
			editor.putString("accessToken", accessToken);
			editor.putBoolean("showAccessToken",  showAccessToken);
			editor.commit();
			
			Intent resultIntent = new Intent();
    		setResult(Activity.RESULT_OK, resultIntent);
    		finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_save) {
			saveLogin();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
