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

import com.example.sparkrc.FirstVarSet.RequestTask;

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

public class EditVars extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_vars);
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		int varLength = settings.getInt("variableLength", 0);
		String varNames = settings.getString("variableNames", "");
		LinearLayout layout = (LinearLayout)findViewById(R.id.checkboxesVarSelection_edit);
		
		JSONArray names = null;
		try{
			names = new JSONArray(varNames);
		} catch(JSONException e){
			e.printStackTrace();
		}
		
		for(int i = 0; i < varLength; i++){
			CheckBox checkbox = new CheckBox(this);
			checkbox.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			try {
				checkbox.setText(names.getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				if(settings.getBoolean(names.getString(i), false)) checkbox.setChecked(true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			checkbox.setId(i);
			layout.addView(checkbox);
		}
	}
	
	public void saveVars(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		String varNames = settings.getString("variableNames", "");
		int varLength = settings.getInt("variableLength", 0);
		JSONArray names = null;
		
		try{
			names = new JSONArray(varNames);
			for(int i = 0; i<varLength; i++){
				String curName = names.getString(i);						
				Integer id = Integer.valueOf(i);
				CheckBox box = (CheckBox)findViewById(id);
				if(box.isChecked()) editor.putBoolean(curName, true);
				else editor.putBoolean(curName, false);
			}
			editor.commit();
	    	Intent resultIntent = new Intent();
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
			
		} catch(JSONException e){
			e.printStackTrace();
		}
	}
	
	public void refreshVars(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		LinearLayout layout = (LinearLayout)findViewById(R.id.checkboxesVarSelection_edit);
		TextView tv = (TextView)findViewById(R.id.FirstVarSet);
				
		tv.setText("Grabbing Variable List...");
		
		int varLength = settings.getInt("variableLength",  0);
		
		for(int i = 0; i<varLength; i++){
			Integer id = Integer.valueOf(i);
			CheckBox box = (CheckBox)findViewById(id);
			layout.removeView(box);
		}
		
		String deviceId = settings.getString("deviceId", "");
		String accessToken = settings.getString("accessToken", "");
		String url = "https://api.spark.io/v1/devices/" + deviceId + "/" + "?access_token=" + accessToken;
		
		new RequestTask().execute(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit_vars, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_save) {
			saveVars();
			return true;
		}
		if(id == R.id.action_refresh){
			refreshVars();
			return true;
		}
		return super.onOptionsItemSelected(item);
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
    		JSONSparkVarParser_edit(result);
        }
    }
	
	public void JSONSparkVarParser_edit(String result){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		TextView tv = (TextView)findViewById(R.id.FirstVarSet);
		LinearLayout layout = (LinearLayout)findViewById(R.id.checkboxesVarSelection_edit);
				
		String varNames = settings.getString("variableNames", "");
		int varLength = settings.getInt("variableLength", 0);
		
		if(result != null){
			try{
				JSONObject jsonObj = new JSONObject(result);
				JSONObject variables = new JSONObject(jsonObj.getString("variables"));
				JSONArray names = variables.names();
				JSONArray namesOld = new JSONArray(varNames);
				
				for(int i = 0; i<varLength; i++){
					if(!names.toString().contains(namesOld.getString(i))) editor.remove(namesOld.getString(i));
				}
				editor.commit();
				
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
