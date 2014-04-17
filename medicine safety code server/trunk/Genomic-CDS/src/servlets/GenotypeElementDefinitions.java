package servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.lang.text.StrSubstitutor;

import safetycode.Genetic_Marker_Group;
import safetycode.MedicineSafetyProfile_v2;
import utils.Common;
import utils.StringReader;

/**
 * Servlet implementation class GenotypeElementDefinitions
 */
public class GenotypeElementDefinitions extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GenotypeElementDefinitions() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
		
		if(cache!=null && cache.isKeyInCache("GenotypeElementDefinitions")){	
			Element element = cache.get("GenotypeElementDefinitions");
			Object object = element.getObjectValue();
			if(String.class.isInstance(object)){
				out.println((String) object);
			}
			//manager.shutdown();		//Close the cache manager to reduce the memory use of the application.
		}else{	//If there is a cache miss or the cache is not initialized
			StringBuffer contentHTML = new StringBuffer("");
			Comparator<String> comparator = new Comparator<String>(){
				public int compare(String s1,String s2){
					if(s1.length()>s2.length()) return 1;
					if(s1.length() == s2.length()){
						return s1.compareTo(s2);
					}
					return -1;
				}
			};
			MedicineSafetyProfile_v2 myProfile = new MedicineSafetyProfile_v2(path+Common.ONT_NAME);
			ArrayList<Genetic_Marker_Group> listGroups = myProfile.getListGenotypeGroups();
			HashMap<String,ArrayList<String>> map_genotype = new HashMap<String,ArrayList<String>>(); 
			for(Genetic_Marker_Group gmg: listGroups){
				HashSet<String> listUniqueElements = new HashSet<String>(); 
				ArrayList<String> listElements = gmg.getListElements();
				for(String element: listElements){
					/*if(element.contains(";")){
						listUniqueElements.add(element.substring(0,element.indexOf(";")));
						listUniqueElements.add(element.substring(element.indexOf(";")+1));
					}else{*/
						listUniqueElements.add(element);
					//}
				}
				
				if(listUniqueElements.size()<=1){
					continue;
				}
				ArrayList<String> list_sorted = new ArrayList<String>();
				list_sorted.addAll(listUniqueElements);
				Collections.sort(list_sorted,comparator);
				map_genotype.put(gmg.getGeneticMarkerName(),list_sorted);
			}
			if(!map_genotype.isEmpty()){
				ArrayList<String> list_sorted_keys = new ArrayList<String>();
				list_sorted_keys.addAll(map_genotype.keySet());
				Collections.sort(list_sorted_keys);
				for(String key : list_sorted_keys){
					ArrayList<String> sorted_element = map_genotype.get(key);
					String group = "<fieldset class=\"ui-grid-b\"><div class=\"ui-block-a\"><label><strong>"+key+": </strong></label></div>\n";
					String controlGroup0="<div class=\"ui-block-b\"><legend for=\""+key+"-0\" class=\"select\"></legend><select name=\""+key+"-0\" id=\""+key+"-0\">";
					String controlGroup1="<div class=\"ui-block-c\"><legend for=\""+key+"-1\" class=\"select\"></legend><select name=\""+key+"-1\" id=\""+key+"-1\">";
					
					String groupElements = "<option value=\"Empty\"></option>";
					for(String element: sorted_element){
						if(!element.equals("null")&&!element.isEmpty()){
							groupElements += "<option value=\""+element+"\">"+element+"</option>";
						}
					}
					
					controlGroup0+=groupElements+"</select></div>";
					controlGroup1+=groupElements+"</select></div>";
					group+=controlGroup0+"\n"+controlGroup1+"\n</fieldset>\n";
					
					contentHTML.append(group);
				}
			}
			
			// Below, the Apache StrSubstitutor class is used as a very simple	
			Map<String,StringBuffer> valuesMap = new HashMap<String,StringBuffer>();
			valuesMap.put("content", contentHTML);
			StringReader myStringReader = new StringReader();
			String templateString = myStringReader.readFile(path+"AlleleGeneral.html");
			StrSubstitutor sub = new StrSubstitutor(valuesMap);
			String resolvedString = sub.replace(templateString);
			
			out.println(resolvedString);
			if(cache!=null){
				cache.put(new Element("GenotypeElementDefinitions",resolvedString)); //Add the new element into the cache.
				cache.flush(); //We flush the cache to make all element persistent on disk. This avoid the wrong insert of an element due to application errors. We can avoid this here if the performance of the application is penalized.
				//manager.shutdown();
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
