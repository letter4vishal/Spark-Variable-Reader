package com.example.sparkrc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FirstVarSet extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_var_set);
		setVars();		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_var_set, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case R.id.action_accept:
				saveVars();
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
		}
	}
	
	public void setVars(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		TextView tv = (TextView)findViewById(R.id.FirstVarSet);
		
		tv.setText("Grabbing Variable List...");
		
		String deviceId = settings.getString("deviceId", "");
		String accessToken = settings.getString("accessToken", "");
		String url = "https://api.spark.io/v1/devices/" + deviceId + "/" + "?access_token=" + accessToken;
		
		new RequestTask().execute(url);
	}
	
	public void saveVars(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();	
		
		int varLength = settings.getInt("variableLength", 0);
		String varNames = settings.getString("variableNames", "");
		
		JSONArray names = null;
		try{
			names = new JSONArray(varNames);
		} catch(JSONException e){
			e.printStackTrace();
		}
		
		for(int i=0; i<varLength; i++){
			String currentVar = null;
			try{
				currentVar = names.getString(i);
			} catch(JSONException e){
				e.printStackTrace();
			}
			
			Integer id = Integer.valueOf(i);
			CheckBox box = (CheckBox)findViewById(id);
			if(box.isChecked()) editor.putBoolean(currentVar, true);
			else editor.putBoolean(currentVar, false);			
		}
		editor.putBoolean("setupDone", true);
		editor.commit();
		
		Intent resultIntent = new Intent();
		setResult(Activity.RESULT_OK, resultIntent);
		finish();		
	}
	
	class RequestTask extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
            return responseString;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
    		JSONSparkVarParser(result);
        }
    }
	
	public void JSONSparkVarParser(String result){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		TextView tv = (TextView)findViewById(R.id.FirstVarSet);
		
		if(result != null){
			try{
				JSONObject jsonObj = new JSONObject(result);
				JSONObject variables = new JSONObject(jsonObj.getString("variables"));
				JSONArray names = variables.names();
				
				LinearLayout layout = (LinearLayout)findViewById(R.id.checkboxesVarSelection);
				for(int i = 0; i < names.length(); i++){
					CheckBox checkbox = new CheckBox(this);
					checkbox.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					checkbox.setText(names.getString(i));
					checkbox.setId(i);
					if(settings.getBoolean(names.getString(i), false)) checkbox.setChecked(true);
					layout.addView(checkbox);
				}
				editor.putString("variableNames", names.toString());
				editor.putInt("variableLength", names.length());
				editor.commit();
			} catch (JSONException e){
				e.printStackTrace();
			}
		}
		tv.setText("Check Variables You Want To Display");
	}
}
