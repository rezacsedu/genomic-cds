package meduniwien.msc;

import meduniwien.msc.model.RecommendationRulesMain;
import meduniwien.msc.util.OntologyManagement;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.webkit.WebView;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled") //This is needed to support javascript in the webview element.
/**
 * This class processes the scanned version and code values and shows the corresponding drug recommendations.
 * 
 * @author Jose Antonio Miñarro Giménez
 * */
public class DisplayRecommendationsActivity extends ActionBarActivity {
	//Contains the resulting HTML page generated from the scanned code and version values. It is needed to avoid problems such as changing screen orientation or changing the application.
	private String htmlPage = "" ;
	//Contains the scanned version value.
	private String version = "";
	//Contains the scanned code value.
	private String code = "";
	
	@Override
	/**
	 * It initializes the interface of the display recommendation activity. 
	 * Instead of using the layout defined in the file "fragment_display_recommendations.xml", we manually add the webview to the activity interface. 
	 * However, we use the "activity_display_recommendations.xml" to include the action bar element into the interface.
	 * 
	 * During the initialization of the interface, we process the scanned version and code parameters.
	 * The first time this method is executed it decodes the scanned code value, matches the suitable drug recommendations and generates the html page with the results.
	 * We have implemented the savedInstanceState, therefore if this method is called again it stores the resulting hmlt and it is not needed to decode the genotype profile again.
	 * */
   	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(savedInstanceState != null){//If this is not the first time the method is executed.
			
			//Create the WebView with enabled Javascript and access to local files, and insert it into the interface 
			final WebView webview = new WebView(this);
			webview.getSettings().setJavaScriptEnabled(true);
			webview.getSettings().setAllowUniversalAccessFromFileURLs(true);		
			setContentView(webview);
			
			//Get the variable that represents the resulting HTML web page.  
			String html = savedInstanceState.getString("html");
			
			if(html!=null && !html.isEmpty()){//If the HTML web page has been generated.
				htmlPage = html;				
				webview.loadDataWithBaseURL("file:///android_asset/", htmlPage, "text/html", "UTF-8", null);
			}else{//When the HTML web page has not been generated yet.
				
				//Obtain the scanned version and code values and  				
				final String version = savedInstanceState.getString("version");
				final String code = savedInstanceState.getString("code");
				final OntologyManagement om = OntologyManagement.getOntologyManagement(this);
				//Because of obtaining the suitable drug recommendations and generating the resulting HTML page could be a tough task we use a ProgressDialog to avoid black screen on mobile devices during the task. 
				final ProgressDialog ringProgressDialog = ProgressDialog.show(this, "Please wait ...", "Execution process ...", true);
				ringProgressDialog.setCancelable(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							
							htmlPage = RecommendationRulesMain.getHTMLRecommendations(version,code,om);
							webview.loadDataWithBaseURL("file:///android_asset/", htmlPage, "text/html", "UTF-8", null);
	    				} catch (Exception e) {
	    					e.printStackTrace();
	    				}
						ringProgressDialog.dismiss();
					}
				}).start();
			}
		}else{//If it is the first time this method is executed.
			
			// Get the version and code from the intent
			final Intent intent = getIntent();
			version = intent.getStringExtra(MainActivity.EXTRA_VERSION);
			code = intent.getStringExtra(MainActivity.EXTRA_CODE);
			
			//Create the WebView with enabled Javascript and access to local files, and insert it into the interface
			final WebView webview = new WebView(this);
			webview.getSettings().setJavaScriptEnabled(true);
			webview.getSettings().setAllowUniversalAccessFromFileURLs(true);		
			setContentView(webview);
			
			//Because of obtaining the suitable drug recommendations and generating the resulting HTML page could be a tough task we use a ProgressDialog to avoid black screen on mobile devices during the task.
			final ProgressDialog ringProgressDialog = ProgressDialog.show(this, "Please wait ...", "Execution process ...", true);
			final OntologyManagement om = OntologyManagement.getOntologyManagement(this);
			ringProgressDialog.setCancelable(true);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						htmlPage = RecommendationRulesMain.getHTMLRecommendations(version,code,om);
						webview.loadDataWithBaseURL("file:///android_asset/", htmlPage, "text/html", "UTF-8", null);
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
					ringProgressDialog.dismiss();
				}
			}).start();
		}
	}
	
	@Override
	/**
	 * This method is called when the application is not displayed in the mobile device or other events have interrupted its execution.
	 * We stored the resulting HTML web page, and the scanned code and version values.
	 * */
	public void onSaveInstanceState(Bundle outState) {
	   super.onSaveInstanceState(outState);
	   outState.putString("html", htmlPage);
	   outState.putString("version",version);
	   outState.putString("code",code);
	}

	
	@Override
	/**
	 * It populates the action bar with the buttons defined in the file "display_recommendations.xml" of the menu folder. 
	 * */
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.display_recommendations, menu);
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
        	CharSequence text = "This service is provided for research purposes only and comes without any warranty. (C) 2014";
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

        public PlaceholderFragment() { }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                  Bundle savedInstanceState) {
              View rootView = inflater.inflate(R.layout.fragment_display_recommendations,
                      container, false);
              return rootView;
        }
    }
}
