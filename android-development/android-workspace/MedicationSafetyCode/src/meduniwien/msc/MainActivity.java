package meduniwien.msc;

import meduniwien.msc.model.RecommendationRulesMain;
import meduniwien.msc.util.OntologyManagement;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	public final static String EXTRA_CODE = "meduniwien.msc.CODE";
	public final static String EXTRA_VERSION = "meduniwien.msc.VERSION";
	public final static String EXTRA_HTML = "meduniwien.msc.HTML";
	private static final int SCAN_QR_CODE_REQUEST = 0;
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
		
		//We initialize the class that provides information from the ontology.
		OntologyManagement.getOntologyManagement();
	}
	
	public void onStart (){
		super.onStart();
		String value = "© Copyright 2014  <a href=\"http://samwald.info/\">Matthias Samwald</a> All rights reserved unless stated otherwise.";
		final TextView text = (TextView) findViewById(R.id.footer);
		if(text != null){
			text.setText(Html.fromHtml(value));
			text.setMovementMethod(LinkMovementMethod.getInstance());
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
		
		int id = item.getItemId();
        if (id == R.id.action_warning) {
        	Context context = getApplicationContext();
        	CharSequence text = "This service is provided for research purposes only and comes without any warranty. © 2014";
        	int duration = Toast.LENGTH_LONG;
        	Toast toast = Toast.makeText(context, text, duration);
        	toast.show();
            return true;
        }
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

	/** Called when the user clicks on the manual definition of a genetic profile button*/
	public void customProfile(View view){
		Intent intent = new Intent(this,ManualDefinitionProfile.class);
		startActivity(new_intent);
	}
	
	
	/** Called when the user clicks the Scan button */
	public void scanCode(View view) {
	    // Do something in response to button
		
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, SCAN_QR_CODE_REQUEST);
				
		/*
		//TESTING CODE WHEN AVOIDING SCANNING ZXING MODULE 
		code = "3Lka_efiRMZA25c9Wy6BOVUepOzWP_P8e0";//example 1
		//code = "2UYPe0zpay5riIiE-0VUeXMHt5sBFolC00";//example 2
		version = "v0.2";
		Intent new_intent = new Intent(this, DisplayRecommendationsActivity.class);
    	new_intent.putExtra(EXTRA_CODE, code);
    	new_intent.putExtra(EXTRA_VERSION, version);
    	//String htmlPage = RecommendationRulesMain.getHTMLRecommendations(version,code);
		//new_intent.putExtra(EXTRA_HTML, htmlPage);
    	startActivity(new_intent);*/
	}
	
	
	/** Called when the user clicks the Scan button. */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		boolean error_code = true;
		String contents = "";
		if (requestCode == SCAN_QR_CODE_REQUEST) {//The scanning module finish without an error
			if (resultCode == RESULT_OK) {//the scanning result is OK.
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
			        	
		        	}else{//The QR code should follow this format <urlserver>/<version>/<code>
		        		contents = "Bad formed safetycode QR code";
		        	}
		        }else{//The QR code should follow this format <urlserver>/<version>/<code>
		        	contents = "Bad formed safetycode QR code";
		        }
			}else{//the user backed out or the operation failed for some reason
				contents = "The scanning process was cancelled and the code ";
			}
		}else{//Other request is obtained
			contents = "The scanning process did not retrieve a well qr code";
		}
		
		if (error_code) {
			// Handle error
	    	TextView tv = (TextView) findViewById(R.id.error_message);
	    	tv.setText("ERROR: Please use a valid QR code generated through www.safety-code.org/\nProblem: "+contents);
	    }
	}
}
