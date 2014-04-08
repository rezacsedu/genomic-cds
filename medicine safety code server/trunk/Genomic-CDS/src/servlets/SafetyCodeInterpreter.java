package servlets;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.text.StrSubstitutor;

import safetycode.DrugRecommendation;
import safetycode.Genotype;
import safetycode.GenotypeElement;
import safetycode.MedicineSafetyProfile_v2;
import utils.StringReader;

import exception.BadFormedBase64NumberException;
import exception.NotInitializedPatientsGenomicDataException;
import exception.VariantDoesNotMatchAnAllowedVariantException;
import utils.Common;

/**
 * Servlet implementation class SafetyCodeInterpreter
 */

public class SafetyCodeInterpreter extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String base64ProfileString = request.getRequestURI();
		if(base64ProfileString.contains("/")){
			base64ProfileString = base64ProfileString.substring(base64ProfileString.lastIndexOf("/")+1);			
		}

		if ((base64ProfileString == null) || (base64ProfileString.equals(""))) 	throw (new ServletException("Error: Submitted code is invalid or missing."));
				
		String path = this.getServletContext().getRealPath("/");
		path=path.replaceAll("\\\\", "/");
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		CacheManager manager = null;
		Cache cache = null;
		try{
			InputStream fis = new FileInputStream(new File(path+Common.CACHE_FILE).getAbsolutePath());//We obtain the configuration file of the cache manager.
			manager = CacheManager.create(fis);//Singleton mode, avoid multiple manager with the same configuration.
			fis.close();
			cache = manager.getCache(Common.CACHE_NAME); //Obtain the instance of the configured cache.
		}catch(Exception e){
			cache=null;
			throw (new ServletException("The cache could not be initialized."));
		}
		
		if(cache!=null && cache.isKeyInCache(base64ProfileString)){	
			Element element = cache.get(base64ProfileString);
			Object object = element.getObjectValue();
			if(String.class.isInstance(object)){
				out.println((String) object);
			}
			
			//manager.shutdown();		//Close the cache manager to reduce the memory use of the application.
		}else{	//If there is a cache miss or the cache is not initialized
			// initialize patient profile
			MedicineSafetyProfile_v2 myProfile = new MedicineSafetyProfile_v2(path+Common.ONT_NAME);
			try {
				myProfile.readBase64ProfileString(base64ProfileString);
			} catch (VariantDoesNotMatchAnAllowedVariantException e) {
				throw (new ServletException("variant "+ e.getMessage()));
			} catch (BadFormedBase64NumberException e) {
				throw (new ServletException("number "+ e.getMessage()));
			}
			
			HashMap<String, ArrayList<DrugRecommendation>> list_recommendations=null;
			try {
				list_recommendations = myProfile.obtainDrugRecommendations();
			} catch (NotInitializedPatientsGenomicDataException e) {
				e.printStackTrace();
				return;
			}
			Map<String,StringBuffer> valuesMap = new HashMap<String, StringBuffer>();
						
			// Output inferred alleles
			StringBuffer allelesHTML = new StringBuffer("");
			Genotype genotype = myProfile.getGenotype();
			ArrayList<GenotypeElement> listGenotypeElements = genotype.getListGenotypeElements();
			for(GenotypeElement ge: listGenotypeElements){
				if(!ge.getCriteriaSyntax().contains("null;null")){
					allelesHTML.append("<li>"+ge.getGeneticMarkerName()+" "+revert_label(ge.getCriteriaSyntax(),ge.getGeneticMarkerName())+"</li>");
				}
			}
			valuesMap.put("inferred_alleles", allelesHTML);
			
			// Output recommendations
			StringBuffer recommendationsHTML = new StringBuffer("");
			StringBuffer criticalRecommendationsHTML = new StringBuffer("");
			if(!list_recommendations.isEmpty()){
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
						recommendation_html_header = "\t\t<h3>"+key+" (!)</h3>\n";
						recommendation_html +=recommendation_html_header+"\n"+recommendation_html_body+"\t</div>\n</li>\n";
						criticalRecommendationsHTML.append(recommendation_html);
						recommendationsHTML.append(recommendation_html);
					}else{
						recommendation_html_header = "\t\t<h3>"+key+"</h3>\n";
						recommendation_html +=recommendation_html_header+"\n"+recommendation_html_body+"\t</div>\n</li>\n";
						recommendationsHTML.append(recommendation_html);
					}
				}
			}
			valuesMap.put("critical_recommendations", criticalRecommendationsHTML);
			valuesMap.put("all_recommendations", recommendationsHTML);
			list_recommendations.clear();
						
			StringReader myStringReader = new StringReader();
			String templateString = myStringReader.readFile(path+"interpretation-template_v2.html");
			StrSubstitutor sub = new StrSubstitutor(valuesMap);
			String resolvedString = sub.replace(templateString);

			out.println(resolvedString);
			if(cache!=null){
				cache.put(new Element(base64ProfileString,resolvedString)); //Add the new element into the cache.
				cache.flush(); //We flush the cache to make all element persistent on disk. This avoid the wrong insert of an element due to application errors. We can avoid this here if the performance of the application is penalized.
				//manager.shutdown();
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	
	private String revert_label(String label,String id){
		//System.out.print("\tid="+id+" with label = "+label);
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
		//System.out.println(" produces label = "+label);
		return label;
	}
}