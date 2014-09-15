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

import exception.NotInitializedPatientsGenomicDataException;

import safetycode.DrugRecommendation;
import safetycode.Genotype;
import safetycode.MedicineSafetyProfile_v2;
import utils.StringReader;
import utils.Common;

/**
 * Servlet implementation class AlleleSafetyCodeInterpreter
 * @deprecated
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 */
public class AlleleSafetyCodeInterpreter extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String allele_1= request.getParameter("1-TPMT");
		String allele_2= request.getParameter("2-TPMT");
		String drug_name = request.getParameter("drug");
		
		if(allele_1==null || allele_2 == null){
			throw (new ServletException("Error: Submitted alleles are invalid or missing."));
		}
		
		if(allele_1.compareTo(allele_2)>0){
			String allele = allele_1;
			allele_1 = allele_2;
			allele_2 = allele;
		}
		
		String parameters  = "drug="+drug_name+"&1-TPMT="+allele_1+"&2-TPMT="+allele_2;
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
			//System.out.println("Cache was not initialized with the configuration file = "+path+"ehcache.xml");
			cache=null;
			throw (new ServletException("The cache could not be initialized."));
		}
		
		if(cache!=null && cache.isKeyInCache(parameters)){
			Element element = cache.get(parameters);
			Object object = element.getObjectValue();
			if(String.class.isInstance(object)){
				out.println((String) object);
			}
		}else{
			String alleles_names = "";
			HashMap<String,ArrayList<DrugRecommendation>> list_recommendations = null;
			MedicineSafetyProfile_v2 myProfile = new MedicineSafetyProfile_v2(path);
			myProfile.setGenotype(null);
			Genotype myGenotype = myProfile.getGenotype();
			//String uriClass = "http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_homozygous_TPMT_"+allele_1;
			try {
				myGenotype.modifyGenotypeElement("TPMT", allele_1+";"+allele_2);
				myProfile.setGenotype(myGenotype);
				list_recommendations = myProfile.obtainDrugRecommendations();
				
			} catch (NotInitializedPatientsGenomicDataException e) {
				throw (new ServletException("Error: "+e.getMessage()));
			}
			/*if(allele_1.equals(allele_2)){//homozygous
				MedicineSafetyProfile_v2 myProfile = new MedicineSafetyProfile_v2(path+"MSC_classes_new_2.owl");
				myProfile.setGenotype(null);
				Genotype myGenotype = myProfile.getGenotype();
				String uriClass = "http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_homozygous_TPMT_"+allele_1;
				try {
					myGenotype.modifyGenotypeElement("TPMT", allele_1+";"+allele_2);
					myProfile.setGenotype(myGenotype);
					list_recommendations = myProfile.obtainDrugRecommendations();
					
				} catch (NotInitializedPatientsGenomicDataException e) {
					throw (new ServletException("Error: "+e.getMessage()));
				}
			}else{
				
				MedicineSafetyProfile_v2 myProfile = new MedicineSafetyProfile_v2(path+"MSC_classes.owl");
				String uriClass1 = "http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_TPMT_"+allele_1;
				String uriClass2 = "http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_TPMT_"+allele_2;
				
				try{
					myProfile.addPatientAllele(uriClass1);
					myProfile.addPatientAllele(uriClass2);
					list_recommendations = myProfile.obtainDrugRecommendations();
					alleles_names = myProfile.getResourceLabel("http://www.genomic-cds.org/ont/genomic-cds.owl#TPMT_"+allele_1);
					alleles_names += ", "+myProfile.getResourceLabel("http://www.genomic-cds.org/ont/genomic-cds.owl#TPMT_"+allele_2);
				}catch(NotInitializedPatientsGenomicDataException e){
					throw (new ServletException("Error: "+e.getMessage()));
				}
			}*/
			
			if(list_recommendations!=null && list_recommendations.containsKey("raw_data")) list_recommendations.remove("raw_data");
			if(list_recommendations!=null && list_recommendations.containsKey("inferred_alleles")) list_recommendations.remove("inferred_alleles");
			
			StringBuffer recommendationsHTML = new StringBuffer("");
			
			if(list_recommendations.containsKey(drug_name)){
				recommendationsHTML.append("<p>Matching recommendations for <b>"+drug_name+"</b> are shown below. Recommendations are based on assuming <b>"+alleles_names+"</b></p>");
				String drug_recommendations="";
				int nRecommendations=0;
				int recommendation_importance = 0;
				Iterator<DrugRecommendation> list_dr = list_recommendations.get(drug_name).iterator();
				/* list_data
				 * 	[0] cds_message
				 *  [1] recommendation_importance
				 *  [2] source
				 *  [3] rule_URL
				 * */					
				while(list_dr.hasNext()){
					DrugRecommendation data = list_dr.next();
					nRecommendations++;
					if(recommendation_importance==0){
						if(data.getImportance().contains("important")){
							recommendation_importance = Common.HAS_IMPORTANT_RECOMMENDATION;
						}else{
							recommendation_importance = Common.HAS_STANDARD_RECOMMENDATION;
						}
					}
					String url_seeAlso = "";
					if(data.getSeeAlsoList()!=null && !data.getSeeAlsoList().isEmpty()){
						url_seeAlso = data.getSeeAlsoList().get(0);
					}
					drug_recommendations +="<fieldset style='margin-bottom:20px'><legend>Recommendation</legend><div class='ui-bar ui-bar-e'><h4>"+data.getImportance()+"</h4><a href='"+url_seeAlso+"'>"+data.getSource()+"</a><p>"+data.getCDSMessage()+"</p></div></fieldset>";
				}		
				
				String new_drug="";
				if(recommendation_importance==Common.HAS_IMPORTANT_RECOMMENDATION){
					new_drug = "<li><div data-filtertext='"+drug_name+"' data-role='collapsible'><h3>"+drug_name+" <span style='padding-left:10em'><span class='ui-li-count' style='color:#bb0000'>"+nRecommendations+"</span></span></h3>"+drug_recommendations+"</div></li>";
				}
				if(recommendation_importance==Common.HAS_STANDARD_RECOMMENDATION){
					new_drug = "<li><div data-filtertext='"+drug_name+"' data-role='collapsible'><h3>"+drug_name+" <span style='padding-left:10em'><span class='ui-li-count' style='color:#009900'>"+nRecommendations+"</span></span></h3>"+drug_recommendations+"</div></li>";
				
				}
				if(recommendation_importance==0){
					new_drug = "<li><div data-filtertext='"+drug_name+"' data-role='collapsible'><h3>"+drug_name+"</h3>"+drug_recommendations+"</div></li>";
				}					
				recommendationsHTML.append(new_drug);
				
				/*while(list_data.hasNext()){
					drug_recommendations+="<fieldset style='margin-bottom:20px'><legend>Recommendation</legend><div class='ui-bar ui-bar-e'>"+list_data.next()+"</div></fieldset>\n";
				}
				recommendationsHTML.append(drug_recommendations);*/
			}else{
				recommendationsHTML.append("<h3>There are no matching recommendations for <b>"+drug_name+"</b> based on assuming <b>"+alleles_names+"</b></h3>");
			}
			
			Map<String,StringBuffer> valuesMap = new HashMap<String,StringBuffer>();
			valuesMap.put("recommendations", recommendationsHTML);
			StringReader myStringReader = new StringReader();
			String templateString = myStringReader.readFile(path+"results-from-manual-data-entry.html");
			StrSubstitutor sub = new StrSubstitutor(valuesMap);
			String resolvedString = sub.replace(templateString);
			
			out.println(resolvedString);
			if(cache!=null){
				cache.put(new Element(parameters,resolvedString)); //Add the new element into the cache.
				cache.flush(); //We flush the cache to make all element persistent on disk. This avoid the wrong insert of an element due to application errors. We can avoid this here if the performance of the application is penalized.
				
			}
		}
	}

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
