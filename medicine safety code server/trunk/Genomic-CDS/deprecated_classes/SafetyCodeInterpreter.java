package servlets;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
			InputStream fis = new FileInputStream(new File(path+"ehcache.xml").getAbsolutePath());//We obtain the configuration file of the cache manager.
			manager = CacheManager.create(fis);//Singleton mode, avoid multiple manager with the same configuration.
			fis.close();
			cache = manager.getCache("safetycodecache1"); //Obtain the instance of the configured cache.
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
			MedicineSafetyProfile_v2 myProfile = new MedicineSafetyProfile_v2(path+"MSC_classes_new_v2.owl");
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
			allelesHTML.append("<ul data-inset='true'>");
			Genotype genotype = myProfile.getGenotype();
			ArrayList<GenotypeElement> listGenotypeElements = genotype.getListGenotypeElements();
			for(GenotypeElement ge: listGenotypeElements){
				if(!ge.getCriteriaSyntax().contains("null;null")){
					allelesHTML.append("<li>"+ge.getGeneticMarkerName()+" "+ge.getCriteriaSyntax()+"</li>");
				}
			}
			allelesHTML.append("</ul>");
			valuesMap.put("inferred_alleles", allelesHTML);
					
			
			// Output recommendations
			StringBuffer recommendationsHTML = new StringBuffer("");
			
			if(!list_recommendations.isEmpty()){
				recommendationsHTML.append("<ul data-role='listview' data-inset='true' data-filter-placeholder='Type medication name...' data-filter='true'>");
				Iterator<String> it_keys = list_recommendations.keySet().iterator();
				while(it_keys.hasNext()){
					String key = it_keys.next();
					String drug_recommendations = "";
					int nRecommendations=0;
					int recommendation_importance = 0;
					Iterator<DrugRecommendation> list_data = list_recommendations.get(key).iterator();
					while(list_data.hasNext()){
						DrugRecommendation data_structure = list_data.next();
						nRecommendations++;
						if(recommendation_importance==0){
							if(data_structure.getImportance().equalsIgnoreCase("important")){
								recommendation_importance = Common.HAS_IMPORTANT_RECOMMENDATION;
							}
							if(data_structure.getImportance().equalsIgnoreCase("standard")){
								recommendation_importance = Common.HAS_STANDARD_RECOMMENDATION;
							}
						}
						String url = "";
						if(data_structure.getSeeAlsoList()!=null && !data_structure.getSeeAlsoList().isEmpty()){
							url = data_structure.getSeeAlsoList().get(0);
						}
						drug_recommendations +="<fieldset style='margin-bottom:20px'><legend>Recommendation</legend><div class='ui-bar ui-bar-e'><h4>"+data_structure.getImportance()+"</h4><a href='"+url+"'>"+data_structure.getRuleId()/*getSource()*/+"</a><p>"+data_structure.getCDSMessage()+"</p></div></fieldset>";
					}
					String new_drug="";
					if(recommendation_importance==Common.HAS_IMPORTANT_RECOMMENDATION){
						new_drug = "<li><div data-filtertext='"+key+"' data-role='collapsible'><h3>"+key+" <span style='padding-left:10em'><span class='ui-li-count' style='color:#bb0000'>"+nRecommendations+"</span></span></h3>"+drug_recommendations+"</div></li>";
					}
					if(recommendation_importance==Common.HAS_STANDARD_RECOMMENDATION){
						new_drug = "<li><div data-filtertext='"+key+"' data-role='collapsible'><h3>"+key+" <span style='padding-left:10em'><span class='ui-li-count' style='color:#009900'>"+nRecommendations+"</span></span></h3>"+drug_recommendations+"</div></li>";
					
					}
					if(recommendation_importance==0){
						new_drug = "<li><div data-filtertext='"+key+"' data-role='collapsible'><h3>"+key+"</h3>"+drug_recommendations+"</div></li>";
					}					
					recommendationsHTML.append(new_drug);
				}
				recommendationsHTML.append("</ul>");
			}
			
			valuesMap.put("recommendations", recommendationsHTML);
			list_recommendations.clear();
						
			// Below, the Apache StrSubstitutor class is used as a very simple
			// templating engine		
			StringReader myStringReader = new StringReader();
			String templateString = myStringReader.readFile(path+"interpretation-template.html");
			StrSubstitutor sub = new StrSubstitutor(valuesMap);
			String resolvedString = sub.replace(templateString);

			out.println(resolvedString);
			if(cache!=null){
				//System.out.println("Cache: insert -> CODE="+base64ProfileString);
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

}