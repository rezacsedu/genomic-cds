package com.example.myfirstapp.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.example.myfirstapp.exception.BadFormedBase64NumberException;
import com.example.myfirstapp.exception.NotInitializedPatientsGenomicDataException;
import com.example.myfirstapp.exception.VariantDoesNotMatchAnyAllowedVariantException;

public class RecommendationRulesMain {

	private String version;
	private String code;
	private Genotype patientGenotype;
	private OntologyManagement om;
	
	public RecommendationRulesMain(String version, String code){
		this.version	= version;
		this.code		= code;
		if(this.version.equals("v0.2")){
			om = new OntologyManagement();
			try{
				patientGenotype = readBase64ProfileString(this.code);
			}catch(Exception e){
				e.printStackTrace();
				this.version = e.getMessage()+"\n"; 
			}
		}
	}
	
	
	public String getHTMLRecommendations(){
		
		HashMap<String, ArrayList<DrugRecommendation>> list_recommendations=null;
		try {
			list_recommendations = obtainDrugRecommendations();
		} catch (NotInitializedPatientsGenomicDataException e) {
			String HTMLErrorPage = "<html><head><title>Error Page</title></head><body><h2>The application has generated an error: \""+e.getMessage()+"\"\n->code = "+code+";version = "+version+".</h2><h3>Please notify your help desk.</h3></body></html>";
			return HTMLErrorPage;
		}
							
		// Output inferred alleles
		String allelesHTML = "";
		
		ArrayList<GenotypeElement> listGenotypeElements = patientGenotype.getListGenotypeElements();
		for(GenotypeElement ge: listGenotypeElements){
			if(!ge.getCriteriaSyntax().contains("null;null")){
				allelesHTML+="<li>"+ge.getGeneticMarkerName()+" "+revert_label(ge.getCriteriaSyntax(),ge.getGeneticMarkerName())+"</li>\n";
			}
		}
				
		// Output recommendations
		String recommendationsHTML = "";
		String criticalRecommendationsHTML = "";
		
		if(!list_recommendations.isEmpty()){
			Comparator<DrugRecommendation> comparator = new Comparator<DrugRecommendation>(){
				public int compare(DrugRecommendation dr1,DrugRecommendation dr2){
					return dr1.getSource().compareTo(dr2.getSource());
				}
			};
			
			ArrayList<String> list_sorted_keys = new ArrayList<String>();
			list_sorted_keys.addAll(list_recommendations.keySet());
			Collections.sort(list_sorted_keys);
			for(String key : list_sorted_keys){
				boolean importance = false;
				String recommendation_html = "";
				recommendation_html += "<li>\n\t<div data-filtertext=\""+key+"\" data-role=\"collapsible\">\n";
				String recommendation_html_header ="";
				ArrayList<DrugRecommendation> list_data = list_recommendations.get(key);
				String recommendation_html_body = "";
				
				Collections.sort(list_data,comparator);
				for(DrugRecommendation dr: list_data){
					if(dr.getImportance().contains("Important")){
						importance = true;
					}
					
					String drug_reason=dr.getReason();
					String drug_url="";
					ArrayList<String> list_urls = dr.getSeeAlsoList();
					if(list_urls!=null && !list_urls.isEmpty()){
						drug_url = list_urls.get(0);
					}
					recommendation_html_body += "\t\t<fieldset style=\"margin-bottom:20px\">\n\t\t\t<legend>"+dr.getSource()+"</legend>\n\t\t\t<div class=\"ui-bar ui-bar-e\">\n\t\t\t\t<div class=\"recommendation-small-text\">Reason: "+drug_reason+"</div>\n\t\t\t\t"+dr.getCDSMessage()+"\n\t\t\t\t<div class=\"recommendation-small-text\">Last guideline update: "+dr.getLastUpdate()+"</div>\n\t\t\t</div>\n\t\t\t<div><a href=\""+drug_url+"\" data-role=\"button\" data-mini=\"true\" data-inline=\"true\" data-icon=\"info\" target=\"_blank\">Show guideline website</a></div>\n\t\t</fieldset>\n\n";
				}
				if(importance){
					if(criticalRecommendationsHTML.length() == 0){
						criticalRecommendationsHTML+="<li data-role=\"list-divider\">Critical</li>\n";
					}
					if(recommendationsHTML.length()==0){
						recommendationsHTML+="<li data-role=\"list-divider\">All</li>\n";
					}
					recommendation_html_header = "\t\t<h3>"+key+" (!)</h3>\n";
					recommendation_html +=recommendation_html_header+"\n"+recommendation_html_body+"\t</div>\n</li>\n";
					criticalRecommendationsHTML+=recommendation_html;
					recommendationsHTML+=recommendation_html;
				}else{
					if(recommendationsHTML.length()==0){
						recommendationsHTML+="<li data-role=\"list-divider\">All</li>\n";
					}
					recommendation_html_header = "\t\t<h3>"+key+"</h3>\n";
					recommendation_html +=recommendation_html_header+"\n"+recommendation_html_body+"\t</div>\n</li>\n";
					recommendationsHTML+=recommendation_html;
				}
			}
			if(criticalRecommendationsHTML.length() == 0){
				criticalRecommendationsHTML += "<li data-role=\"list-divider\">Critical</li>\n";
				String recommendation_html = "";
				recommendation_html += "<li>\n\t<fieldset style=\"margin-bottom:20px\"><div class=\"ui-bar ui-bar-e\"><label>There is not any matched rule related to a critical drug recommendation with the current genomic data.</label></div></fieldset>\n";
				criticalRecommendationsHTML += recommendation_html;
			}
		}else{
			recommendationsHTML += "<li data-role=\"list-divider\">All</li>\n";
			String recommendation_html = "";
			recommendation_html += "<li>\n\t<fieldset style=\"margin-bottom:20px\"><div class=\"ui-bar ui-bar-e\"><label>There is not any matched rule related to a drug recommendation with the current genomic data.</label></div></fieldset>\n";
			recommendationsHTML += recommendation_html;
		}
			
		String htmlResultPage = generateResultHTMLPage(criticalRecommendationsHTML, recommendationsHTML, allelesHTML);
		
		return htmlResultPage;
	}
		
	private HashMap<String, ArrayList<DrugRecommendation>> obtainDrugRecommendations() throws NotInitializedPatientsGenomicDataException {
		HashMap<String,ArrayList<DrugRecommendation>> mapDrugRecommendations = null;
		if(patientGenotype==null){
			throw new NotInitializedPatientsGenomicDataException("The patient's genotype was not initialized");
		}else{
			mapDrugRecommendations = new HashMap<String,ArrayList<DrugRecommendation>>();
			ArrayList<DrugRecommendation> listRecommendations = om.getListDrugRecommendations();
			for(DrugRecommendation dr: listRecommendations){
				if(dr.matchPatientProfile(patientGenotype)){
					String drug_name = dr.getDrugName();
					if(mapDrugRecommendations.containsKey(drug_name)){
						 mapDrugRecommendations.get(drug_name).add(dr);
					}else{
						ArrayList<DrugRecommendation> listMatchedRecommendations = new ArrayList<DrugRecommendation>();
						listMatchedRecommendations.add(dr);
						mapDrugRecommendations.put(drug_name, listMatchedRecommendations);
					}
				}
			}
		}
		return mapDrugRecommendations;
	}
		
	
	/**
	 * Create the patient model that is related to the base64Profile.
	 * 
	 * @param base64Profile Base 64 number that represent the binary codification of a patient genotype.
	 * @throws BadFormedBase64NumberException
	 * @throws VariantDoesNotMatchAnAllowedVariantException 
	 * */
	private Genotype readBase64ProfileString(String base64Profile) throws BadFormedBase64NumberException, VariantDoesNotMatchAnyAllowedVariantException {
		DecodingModule decod_mod = new DecodingModule(om.getListGeneticMarkerGroups());
		ArrayList<GenotypeElement> listGenotypeElements = decod_mod.decodeListGenotypeVariations(base64Profile);
		return new Genotype(listGenotypeElements);		
	}
	
	
	private String generateResultHTMLPage(String criticalRecommendations, String allRecommendations, String inferredAlleles){
		String resultHTML= "";
		resultHTML+="<!DOCTYPE html>\n";
		resultHTML+="<html>\n";
		resultHTML+="<head>\n";
		resultHTML+="<title>Medicine Safety Code</title>\n";
		resultHTML+="<meta charset=\"utf-8\">\n";
		resultHTML+="<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n";
		resultHTML+="<link rel=\"stylesheet\"	href=\"css/themes/default/jquery.mobile-1.3.2.min.css\">\n";
		resultHTML+="<link rel=\"stylesheet\" href=\"_assets/css/jqm-demos.css\">\n";
		resultHTML+="<link rel=\"shortcut icon\" href=\"images/favicon.png\">\n";
		//resultHTML+="<link rel=\"stylesheet\" href=\"http://fonts.googleapis.com/css?family=Open+Sans:300,400,700\">\n";
		resultHTML+="<link rel=\"stylesheet\" href=\"css/safety-code.css\">\n";
		resultHTML+="<script src=\"js/jquery.js\"></script>\n";
		resultHTML+="<script src=\"_assets/js/index.js\"></script>\n";
		resultHTML+="<script src=\"js/jquery.mobile-1.3.2.min.js\"></script>\n";
		resultHTML+="</head>\n";
		resultHTML+="<body class=\"ui-mobile-viewport ui-overlay-c\">\n";
		resultHTML+="<div data-role=\"page\" class=\"jqm-demos\">\n";
		resultHTML+="<div data-role=\"header\" class=\"jqm-header\" style=\"text-align: center; padding: 3px\">\n";
		resultHTML+="<img src=\"images/safety-code-logo-2014-without-slogan.png\" width=\"200\" height=\"37\" alt=\"safety-code.org\"/>\n";
		resultHTML+="</div>";
		resultHTML+="<div data-role=\"content\" class=\"jqm-content\">\n";
		resultHTML+="<div data-role=\"collapsible-set\">\n";
		resultHTML+="<ul data-role='listview' data-inset='true' data-filter-placeholder='Filter substance list...' data-filter='true'>\n";
		resultHTML+=criticalRecommendations+"\n";
		resultHTML+=allRecommendations+"\n";
		resultHTML+="</ul>\n";
		resultHTML+="</div>\n";
		resultHTML+="<div data-role=\"collapsible\" data-mini=\"true\">\n";
		resultHTML+="<h4>Show pharmacogenomic data</h4>\n";
		resultHTML+="<ul data-inset=\"true\">\n";
		resultHTML+=inferredAlleles+"\n";
		resultHTML+="<li>Version="+version+"</li>\n";
		resultHTML+="<li>Code="+code+"</li>\n";
		resultHTML+="</ul>\n";
		resultHTML+="</div>\n";
		resultHTML+="</div>\n";
		resultHTML+="<div data-role=\"footer\" style=\"text-align: center; padding: 5px;\">\n";
		resultHTML+="<div>\n";
		resultHTML+="This service is provided for research purposes only and comes without any warranty. (C)&nbsp;2012&nbsp;\n";
		resultHTML+="</div>\n";
		resultHTML+="</div>\n";
		resultHTML+="</div>\n";
		resultHTML+="</body>\n";
		resultHTML+="</html>\n";
		
		return resultHTML;
	}
	
	
	
	private String revert_label(String label,String id){
		if(id.matches("rs[0-9]+")){
			label = "("+label+")";
		}else{
			if(label.contains("star_")){
				label = label.replace("star_", "*").trim();
			}
			if(label.contains("hash_")){
				label = label.replace("hash_", "#").trim();
			}
			if(label.contains("duplicated_")){
				if(label.lastIndexOf("duplicated_")>=0 && label.indexOf(";")>=0 && label.lastIndexOf("duplicated_")>label.indexOf(";")){
					String repeat = label.substring(label.lastIndexOf("duplicated_")+11);
					label = label.substring(0,label.lastIndexOf("duplicated_"))+" "+repeat+" / "+repeat;
				}
				if(label.indexOf("duplicated_")>=0 && label.indexOf(";")>=0 && label.indexOf("duplicated_")<label.indexOf(";")){
					String repeat = label.substring(label.indexOf("duplicated_")+11,label.indexOf(";"));
					label = repeat+" / "+repeat+" "+label.substring(label.indexOf(";"));
				}
				label += " (note: copy number variation)";
			}
			if(label.contains("_")){
				label = label.replace("_", " ").trim();
			}
			if(label.contains(";")){
				label = label.replace(";"," / ");
			}
		}
		return label;
	}
}