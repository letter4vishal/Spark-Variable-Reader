/*
 * Spark Variable Reader
 * Version 1.0
 * Sydenth (Yeah, a nickname. Not so sure about real names.)
 * 22.09.2014
 */

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
import android.app.AlertDialog;
import android.app.ActionBar.LayoutParams;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends Activity {
	public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);	
        if(!settings.getBoolean("setupDone", false)){
        	runSetup();
        }
        else{
        	drawVars();
        }
    }

    public void runSetup(){
    	startActivityForResult(new Intent(getBaseContext(), FirstLogin.class), 1);
    }
    
    public void drawVars(){
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		LinearLayout layout = (LinearLayout)findViewById(R.id.layout_main);
    	
    	int varLength = settings.getInt("variableLength", 0);
    	String varNames = settings.getString("variableNames", "");
    	
    	String deviceId = settings.getString("deviceId", "");
    	String accessToken = settings.getString("accessToken", "");
		
		layout.removeAllViews();
		Integer idx = Integer.valueOf(999);
		TextView t = new TextView(this);
		t.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		t.setId(idx);
        t.setTextSize(15);
        t.setTypeface(Typeface.DEFAULT_BOLD);
		t.setPadding(0, 20, 0, 20);
        t.setText("Grabbing vars...");
		layout.addView(t);

		int activeVars = 0;
    	try{
    		JSONArray names = new JSONArray(varNames);
    		for(int i = 0; i<varLength; i++){
    			if(settings.getBoolean(names.getString(i), false)){
    				activeVars++;
	    			Integer id = Integer.valueOf(i);
	    			TextView tv = new TextView(this);
	    			tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					tv.setId(id);
					tv.setPadding(0, 20, 0, 0);
					layout.addView(tv);
					
					String url = "https://api.spark.io/v1/devices/" + deviceId + "/" + names.getString(i) + "?access_token=" + accessToken;
			    	new RequestTask().execute(url);
    			}
    		}
    	}catch(JSONException e){
    		e.printStackTrace();
    	}
		editor.putInt("activeVars", activeVars);
		editor.putInt("pendingVars", activeVars);
		editor.commit();
		if(activeVars == 0){
			t.setText("No variables selected.");
			return;
		}
    }
    
    public void editLogin(){    	
    	startActivityForResult(new Intent(getBaseContext(), EditLogin.class), 2);
    }
    
    public void editVars(){
    	startActivityForResult(new Intent(getBaseContext(), EditVars.class), 3);
    }
    
    public void resetAll() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Reset To Factory Settings?");
        alertDialogBuilder
                .setMessage("This will delete deviceId, accessToken and variable selections, and ask you to complete the setup again.")
                .setCancelable(false)
                .setPositiveButton("I know what I'm doing",
                	new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.clear();
                            editor.commit();
                            runSetup();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 1:
            	//runSetup() has finished
            	drawVars();
                break;
            case 2:
            	//editLogin() has finished
            	drawVars();
                break;
            case 3:
            	//editVars() has finished
            	drawVars();
                break;
            default:
            	break;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_login) {
        	editLogin();
            return true;
        }
        else if(id == R.id.action_varSelection){
        	editVars();
        	return true;
        }
        else if(id == R.id.action_refresh){
        	drawVars();
        	return true;
        }
        else if(id == R.id.action_reset){
        	resetAll();
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
            TextView t = (TextView)findViewById(999);
            
            super.onPostExecute(result);
        	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    		SharedPreferences.Editor editor = settings.edit();
            
            if(result != null){
                try {
    				JSONObject jsonObj = new JSONObject(result);
    				String message = null;
    				if(jsonObj.has("result")){
    					message = "Variable Name: " + jsonObj.getString("name");
    					
    					if(jsonObj.getString("result").startsWith("{")){
    						try{
    							JSONObject resultSpark = new JSONObject(jsonObj.getString("result"));
    							JSONArray vars = resultSpark.names();
    							for(int i = 0; i < vars.length(); i++){
    								message += "\n\t\t\t\t\t" + vars.getString(i) + ": " + resultSpark.getString(vars.getString(i));
    							}
    						} catch (JSONException e){
    							e.printStackTrace();
    						}
    					}
    					else message += "\n\t\t\t\t\t" + "Result: " + jsonObj.getString("result");
                	}
    				///////////////////////////
    				String namesJson = settings.getString("variableNames", "");
    				JSONArray names = new JSONArray(namesJson);
    				
    				for(int i = 0; i<names.length(); i++){
    					if(names.getString(i).trim().compareTo(jsonObj.getString("name").trim()) == 0){
        					Integer id = Integer.valueOf(i);
        					TextView tv = (TextView)findViewById(id);
    						tv.setText(message);
    					}
    				}
    			} catch (JSONException e) {
    				e.printStackTrace();
    			}
            }
            int activeVars = settings.getInt("activeVars", 0);
            int pendingVars = settings.getInt("pendingVars", 0);
            pendingVars--;
            editor.putInt("pendingVars", pendingVars);
            editor.commit();
            
            t.setText("Got results (" + (activeVars-pendingVars) + "/" + activeVars + ")");
            
        }
    }
}
