package meduniwien.msc;


import java.util.ArrayList;
import java.util.HashMap;

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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import meduniwien.msc.exception.VariantDoesNotMatchAnyAllowedVariantException;
import meduniwien.msc.model.*;
import meduniwien.msc.util.Common;
import meduniwien.msc.util.OntologyManagement;

public class ManualDefinitionProfileActivity extends ActionBarActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual_definition_profile);
				
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	public void onStart (){
		super.onStart();
		
		//CYP2C19
		Spinner spinner = (Spinner) findViewById(R.id.cyp2c19_spinner_1);		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.CYP2C19, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.cyp2c19_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.CYP2C9, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);		
		
		//CYP2C9
		spinner = (Spinner) findViewById(R.id.cyp2c9_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.CYP2C9, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.cyp2c9_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.CYP2C9, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		//CYP2D6
		spinner = (Spinner) findViewById(R.id.cyp2d6_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.CYP2D6, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.cyp2d6_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.CYP2D6, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		//CYP3A5
		spinner = (Spinner) findViewById(R.id.cyp3a5_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.CYP3A5, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.cyp3a5_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.CYP3A5, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
				
		//DPYD
		spinner = (Spinner) findViewById(R.id.dpyd_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.DPYD, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.dpyd_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.DPYD, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
				
		//HLA-A
		spinner = (Spinner) findViewById(R.id.hla_a_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.HLA_A, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.hla_a_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.HLA_A, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
				
		//HLA-B
		spinner = (Spinner) findViewById(R.id.hla_b_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.HLA_B, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.hla_b_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.HLA_B, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		//TPMT
		spinner = (Spinner) findViewById(R.id.tpmt_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.TPMT, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.tpmt_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.TPMT, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
			
		//UGT1A1
		spinner = (Spinner) findViewById(R.id.ugt1a1_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.UGT1A1, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.ugt1a1_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.UGT1A1, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
				
		//rs12979860
		spinner = (Spinner) findViewById(R.id.rs12979860_spinner_1);
		adapter = ArrayAdapter.createFromResource(this, R.array.rs12979860, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.rs12979860_spinner_2);
		adapter = ArrayAdapter.createFromResource(this, R.array.rs12979860, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
					
		//rs2297595
		spinner = (Spinner) findViewById(R.id.rs2297595_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs2297595, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.rs2297595_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs2297595, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
				
		//rs4149056
		spinner = (Spinner) findViewById(R.id.rs4149056_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs4149056, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.rs4149056_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs4149056, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		//rs6025
		spinner = (Spinner) findViewById(R.id.rs6025_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs6025, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.rs6025_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs6025, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		//rs67376798
		spinner = (Spinner) findViewById(R.id.rs67376798_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs67376798, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.rs67376798_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs67376798, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		//rs9923231
		spinner = (Spinner) findViewById(R.id.rs9923231_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs9923231, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.rs9923231_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs9923231, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		//rs9934438
		spinner = (Spinner) findViewById(R.id.rs9934438_spinner_1);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs9934438, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner = (Spinner) findViewById(R.id.rs9934438_spinner_2);		
		adapter = ArrayAdapter.createFromResource(this, R.array.rs9934438, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		/*getMenuInflater().inflate(R.menu.main, menu);
		return true;*/
		
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.manual_definition_profile, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_manual_definition_profile, container,
					false);
			return rootView;
		}
	}
	
	public void calculateCode(View view){
		/*
		CYP2C19
		CYP2C9
		CYP2D6
		CYP3A5
		DPYD
		HLA-A
		HLA-B
		TPMT
		UGT1A1
		rs12979860
		rs2297595
		rs4149056
		rs6025
		rs67376798		
		rs9923231
		rs9934438
		 */
		
		HashMap<String,String[]> listSelections = new HashMap<String,String[]>();
		
		Spinner spinner = (Spinner) findViewById(R.id.cyp2c19_spinner_1);
		String item_cyp2c19_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.cyp2c19_spinner_2);
		String item_cyp2c19_2 = spinner.getSelectedItem().toString();
		{
		String[] values = {item_cyp2c19_1,item_cyp2c19_2};
		listSelections.put("CYP2C19",values);
		}
		
		spinner = (Spinner) findViewById(R.id.cyp2c9_spinner_1);
		String item_cyp2c9_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.cyp2c9_spinner_2);
		String item_cyp2c9_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_cyp2c9_1,item_cyp2c9_2};
		listSelections.put("CYP2C9",values);}
		
		spinner = (Spinner) findViewById(R.id.cyp2d6_spinner_1);
		String item_cyp2d6_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.cyp2d6_spinner_2);
		String item_cyp2d6_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_cyp2d6_1,item_cyp2d6_2};
		listSelections.put("CYP2D6",values);}
		
		spinner = (Spinner) findViewById(R.id.cyp3a5_spinner_1);
		String item_cyp3a5_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.cyp3a5_spinner_2);
		String item_cyp3a5_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_cyp3a5_1,item_cyp3a5_2};
		listSelections.put("CYP3A5",values);}
		
		spinner = (Spinner) findViewById(R.id.dpyd_spinner_1);
		String item_dpyd_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.dpyd_spinner_2);
		String item_dpyd_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_dpyd_1,item_dpyd_2};
		listSelections.put("DPYD",values);}
		
		spinner = (Spinner) findViewById(R.id.hla_a_spinner_1);
		String item_hla_a_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.hla_a_spinner_2);
		String item_hla_a_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_hla_a_1,item_hla_a_2};
		listSelections.put("HLA-A",values);}
		
		spinner = (Spinner) findViewById(R.id.hla_b_spinner_1);
		String item_hla_b_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.hla_b_spinner_2);
		String item_hla_b_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_hla_b_1,item_hla_b_2};
		listSelections.put("HLA-B",values);}
		
		spinner = (Spinner) findViewById(R.id.tpmt_spinner_1);
		String item_tpmt_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.tpmt_spinner_2);
		String item_tpmt_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_tpmt_1,item_tpmt_2};
		listSelections.put("TPMT",values);}
		
		spinner = (Spinner) findViewById(R.id.ugt1a1_spinner_1);
		String item_ugt1a1_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.ugt1a1_spinner_2);
		String item_ugt1a1_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_ugt1a1_1,item_ugt1a1_2};
		listSelections.put("UGT1A1",values);}
		
		spinner = (Spinner) findViewById(R.id.rs12979860_spinner_1);
		String item_rs12979860_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.rs12979860_spinner_2);
		String item_rs12979860_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_rs12979860_1,item_rs12979860_2};
		listSelections.put("rs12979860",values);}
		
		spinner = (Spinner) findViewById(R.id.rs2297595_spinner_1);
		String item_rs2297595_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.rs2297595_spinner_2);
		String item_rs2297595_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_rs2297595_1,item_rs2297595_2};
		listSelections.put("rs2297595",values);}
		
		spinner = (Spinner) findViewById(R.id.rs4149056_spinner_1);
		String item_rs4149056_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.rs4149056_spinner_2);
		String item_rs4149056_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_rs4149056_1,item_rs4149056_2};
		listSelections.put("rs4149056",values);}
		
		spinner = (Spinner) findViewById(R.id.rs6025_spinner_1);
		String item_rs6025_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.rs6025_spinner_2);
		String item_rs6025_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_rs6025_1,item_rs6025_2};
		listSelections.put("rs6025",values);}
		
		spinner = (Spinner) findViewById(R.id.rs67376798_spinner_1);
		String item_rs67376798_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.rs67376798_spinner_2);
		String item_rs67376798_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_rs67376798_1,item_rs67376798_2};
		listSelections.put("rs67376798",values);}
		
		spinner = (Spinner) findViewById(R.id.rs9923231_spinner_1);
		String item_rs9923231_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.rs9923231_spinner_2);
		String item_rs9923231_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_rs9923231_1,item_rs9923231_2};
		listSelections.put("rs9923231",values);}
		
		spinner = (Spinner) findViewById(R.id.rs9934438_spinner_1);
		String item_rs9934438_1 = spinner.getSelectedItem().toString();
		spinner = (Spinner) findViewById(R.id.rs9934438_spinner_2);
		String item_rs9934438_2 = spinner.getSelectedItem().toString();
		{String[] values = {item_rs9934438_1,item_rs9934438_2};
		listSelections.put("rs9934438",values);}
		
		
		ArrayList<GenotypeElement> listGenotypeElements = new ArrayList<GenotypeElement>();
		ArrayList<GeneticMarkerGroup> listGroups = OntologyManagement.getOntologyManagement().getListGeneticMarkerGroups();
		for(GeneticMarkerGroup gmg: listGroups){
			String criteriaSyntax="null;null";
			if(listSelections.containsKey(gmg.getGeneticMarkerName())){
				String[] variants = listSelections.get(gmg.getGeneticMarkerName());
				if(!variants[0].equals("None") && !variants[1].equals("None")){
					criteriaSyntax = variants[0]+";"+variants[1];
				}
			}
			
			try{
				if(gmg.getPositionGeneticMarker(criteriaSyntax)>=0){
					listGenotypeElements.add(gmg.getGenotypeElement(gmg.getPositionGeneticMarker(criteriaSyntax)));
				}else{
					listGenotypeElements.add(gmg.getGenotypeElement(0));
				}
			} catch (VariantDoesNotMatchAnyAllowedVariantException e) {
				e.printStackTrace();
			}			
		}
		
		String code = "";
		String version ="";
		try{
			code = DecodingModule.codeListGeneticVariations(listGenotypeElements);
			version = Common.VERSION;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Intent new_intent = new Intent(this, DisplayRecommendationsActivity.class);
    	new_intent.putExtra(MainActivity.EXTRA_CODE, code);
    	new_intent.putExtra(MainActivity.EXTRA_VERSION, version);
		startActivity(new_intent);
	}
}
