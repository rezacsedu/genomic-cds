package com.example.myfirstapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	public final static String EXTRA_CODE = "com.example.myfirstapp.CODE";
	public final static String EXTRA_VERSION = "com.example.myfirstapp.VERSION";
	private String code = null;
	private String version = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		/*getMenuInflater().inflate(R.menu.main, menu);
		return true;*/
		
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		/*switch (item.getItemId()){
		case R.id.action_search:
			openSearch();
			return true;
		case R.id.action_settings:
			openSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}*/
		/*int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);*/
		
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	
	/** Called when the user clicks the Scan button */
	public void scanCode(View view) {
	    // Do something in response to button
		
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, 0);
		
		/*code = "3Lka_efiRMZA25c9Wy6BOVUepOzWP_P8e0";//example 1
		//code = "2UYPe0zpay5riIiE-0VUeXMHt5sBFolC00";//example 2
		version = "v0.2";
		
		Intent new_intent = new Intent(this, DisplayRecommendationsActivity.class);
    	new_intent.putExtra(EXTRA_CODE, code);
    	new_intent.putExtra(EXTRA_VERSION, version);
    	startActivity(new_intent);*/
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		boolean error_code = true;
		String contents = "";
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				contents = intent.getStringExtra("SCAN_RESULT");
		        if(contents.contains("/")){
		        	code = contents.substring(contents.lastIndexOf("/")+1);
		        	String subcontents = contents.substring(0, contents.lastIndexOf("/"));
		        	if(contents.contains("/")){
			        	version = subcontents.substring(subcontents.lastIndexOf("/")+1);
			        	Intent new_intent = new Intent(this, DisplayRecommendationsActivity.class);
			        	new_intent.putExtra(EXTRA_CODE, code);
			        	new_intent.putExtra(EXTRA_VERSION, version);
			        	startActivity(new_intent);
			        	error_code = false;
			        	contents = "version = "+version+";code = "+code;
		        	}else{
		        		contents = "Bad formed safetycode QR code";
		        	}
		        }else{
		        	contents = "Bad formed safetycode QR code";
		        }
			}else{
				contents = "The scanning process was cancelled and the code ";
			}
		}else{
			contents = "The scanning process did not retrieve a well formed code";
		}
		
		if (error_code) {
			//code = "";
			//version = "";
			// Handle cancel
	    	TextView tv = (TextView) findViewById(R.id.error_message);
	    	tv.setText("ERROR: Please use a valid QR code generated through www.safety-code.org/\nProblem: "+contents);
	    }
	}
}
