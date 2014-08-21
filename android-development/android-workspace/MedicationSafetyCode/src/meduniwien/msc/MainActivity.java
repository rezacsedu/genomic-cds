package meduniwien.msc;

import meduniwien.msc.util.Common;
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

/**
 * This class is the first activity in the application. It shows some description of the application and provides two buttons, one for scanning a QR code with genetic profile and another for a manual definition of a genetic profile.
 * 
 * @author Jose Antonio Miñarro Giménez
 * */
public class MainActivity extends ActionBarActivity {
	/**It represents the parameter of the code in base 64 related to a genomic profile.*/
	public final static String EXTRA_CODE = "meduniwien.msc.CODE";
	/**It represents the parameter of the version of the code. It should be used to handle different version of the code with the same app.*/
	public final static String EXTRA_VERSION = "meduniwien.msc.VERSION";
	
	//It represents the value provided by the response after scanning a QR code. It is used to recognize the type of request to the scan software.
	private static final int SCAN_QR_CODE_REQUEST = 0;
	//The code variable that contains the code value of the scanned QR code.
	private String code = null;
	//The version variable that contains the version value of the scanned QR code.
	private String version = null;
	
	@Override
	/**
	 * It initializes the interface of the Main activity with the layout defined in the file "activity_main.xml" and "fragment_main.xml".
	 * This is the first element to be executed when the application is launched in a mobile device, so we initialize the OntologyManagement class which contains the information from the genomic CDS ontology.
	 * */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
				
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		//We initialize the class that provides information from the ontology.
		
		
	}
	
	/**
	 * Initialize the interface with the corresponding message in the footer. This is needed to define a clickable hiperlink in the interface. 
	 * 
	 * */
	public void onStart (){
		super.onStart();
		String value = "© Copyright 2014  <a href=\"http://samwald.info/\">Matthias Samwald</a> All rights reserved unless stated otherwise.";
		final TextView text = (TextView) findViewById(R.id.footer);
		if(text != null){
			text.setText(Html.fromHtml(value));
			text.setMovementMethod(LinkMovementMethod.getInstance());
		}
		OntologyManagement.getOntologyManagement(this);
	}
	
	@Override
	/**
	 * It populates the action bar with the buttons defined in the file "main.xml" of the menu folder. 
	 * */
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	/**
	 * Handle the actions related to the buttons defined in the action bar. We basically display a warning about the research approach of the recommendations. 
	 * */
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
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

	/** 
	 * Called when the user clicks on the manual definition of a genetic profile button.
	 * It redirects the workflow to the activity defined in the class "ManualDefinitionProfileActivity"
	 * */
	public void customProfile(View view){
		Intent intent = new Intent(this,ManualDefinitionProfileActivity.class);
		startActivity(intent);
	}
	
	
	/** Called when the user clicks the Scan button.
	 *	This method launches the zxing scanning module. The response will be handled in the method "onActiviyResult" with the SCAN_QR_CODE_REQUEST value as parameter.
	 *  */
	public void scanCode(View view) {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN"); //We select the scan module from the zxing API.
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); //We indicate that we want to scan a QR CODE.
		startActivityForResult(intent, SCAN_QR_CODE_REQUEST); //We provide the SCAN_QR_CODE_REQUEST parameter to identity the type of response in the method "onActivityResult"
				
		/*
		//TESTING CODE WHEN AVOIDING SCANNING ZXING MODULE 
		code = "3Lka_efiRMZA25c9Wy6BOVUepOzWP_P8e0";//example 1
		//code = "2UYPe0zpay5riIiE-0VUeXMHt5sBFolC00";//example 2
		version = Common.VERSION;
		Intent new_intent = new Intent(this, DisplayRecommendationsActivity.class);
    	new_intent.putExtra(EXTRA_CODE, code);
    	new_intent.putExtra(EXTRA_VERSION, version);
    	startActivity(new_intent);
    	*/
	}
	
	
	/**
	 *	Called after the zxing module has scanned the QR code.
	 *	It processes the scanned code retrieved from the zxing activity and obtain the corresponding code and version values.
	 *	If there is not any error in the scanning process the activity "DisplayRecommendationsActivity" is triggered with the obtained code and version as parameters.
	 *	There is an error in the scanning when :
	 *		a) The scanning module does not retrieve any value.
	 *		b) The scanned value is not well formed and does not contains any code or version values.
	 *		c) The version value is not allowed for the current version of the application.
	 *	After an error is detected we show the information in the Main activity to warn the users.
	 *  */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		boolean error_code = true;
		String contents = "";
		if (requestCode == SCAN_QR_CODE_REQUEST) {//The scanning module has finished
			if (resultCode == RESULT_OK) {//the scanning result is OK.
				contents = intent.getStringExtra("SCAN_RESULT");
		        if(contents.contains("/")){
		        	code = contents.substring(contents.lastIndexOf("/")+1);
		        	String subcontents = contents.substring(0, contents.lastIndexOf("/"));
		        	if(contents.contains("/")){
			        	version = subcontents.substring(subcontents.lastIndexOf("/")+1);
			        	if(version.equals(Common.VERSION)){
			        		Intent new_intent = new Intent(this, DisplayRecommendationsActivity.class);
			        		new_intent.putExtra(EXTRA_CODE, code);
			        		new_intent.putExtra(EXTRA_VERSION, version);
			        		startActivity(new_intent);
			        		error_code = false;
			        		contents = "version = "+version+";code = "+code;
			        	}else{
			        		contents = "The scanned version value is not currently supported by the application.";
			        	}
		        	}else{//The QR code should follow this format <urlserver>/<version>/<code>
		        		contents = "Bad formed safetycode QR code.";
		        	}
		        }else{//The QR code should follow this format <urlserver>/<version>/<code>
		        	contents = "Bad formed safetycode QR code.";
		        }
			}else{//the user backed out or the operation failed for some reason
				contents = "The scanning process was cancelled.";
			}
		}else{//Other request is obtained
			contents = "The scanning process did not retrieve any QR code.";
		}
		
		if (error_code) {
			// Handle error
	    	TextView tv = (TextView) findViewById(R.id.error_message);
	    	tv.setText("ERROR: Please use a valid QR code generated through www.safety-code.org/\nProblem: "+contents);
	    }
	}
}
