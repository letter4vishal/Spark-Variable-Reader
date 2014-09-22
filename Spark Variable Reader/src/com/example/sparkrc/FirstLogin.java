package com.example.sparkrc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;

public class FirstLogin extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_login);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        
        if(settings.getString("deviceId", "").length() == 24){
        	TextView d = (TextView)findViewById(R.id.FirstLogin_deviceId);
        	d.setText(settings.getString("deviceId", ""));
        }
        if(settings.getString("accessToken", "").length() == 40){
        	TextView d = (TextView)findViewById(R.id.FirstLogin_accessToken);
        	d.setText(settings.getString("accessToken", ""));
        }		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case R.id.action_accept:
				acceptCredentials();
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
		}
	}
	
	public void acceptCredentials(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		TextView d = (TextView)findViewById(R.id.FirstLogin_deviceId);
		String deviceId = d.getText().toString();
		TextView a = (TextView)findViewById(R.id.FirstLogin_accessToken);
		String accessToken = a.getText().toString();
		CheckBox s = (CheckBox)findViewById(R.id.FirstLogin_showAccessToken);
		boolean showAccessToken = s.isChecked();
		
		if(  (settings.getString("deviceId", "").length() != 24 || settings.getString("accessToken",  "").length() != 40)   &&   (deviceId.length() != 24 || accessToken.length() != 40)   ){
			TextView tv = (TextView)findViewById(R.id.firstLogin);
			tv.setText("Error: Invalid credentials! (a missing character?)");
		}
		else{
			if(deviceId != "" && accessToken != ""){
				editor.putString("deviceId", deviceId);
				editor.putString("accessToken", accessToken);
				editor.putBoolean("showAccessToken", showAccessToken);
				editor.commit();
			}
			startActivityForResult(new Intent(getBaseContext(), FirstVarSet.class), 9);
		}	
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 9:
            	Intent resultIntent = new Intent();
        		setResult(Activity.RESULT_OK, resultIntent);
        		finish();
                break;
            default:
            	break;
        }
    }
	
	public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Exit Application?");
        alertDialogBuilder
                //.setMessage("Click yes to exit!")
                .setCancelable(false)
                .setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            moveTaskToBack(true);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);
                        }
                    })
                .setNegativeButton("No",
                	new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog, int id) {
                    		dialog.cancel();
                    	}
                	});

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
