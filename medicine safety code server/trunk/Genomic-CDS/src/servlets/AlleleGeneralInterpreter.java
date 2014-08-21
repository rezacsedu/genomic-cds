package servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.text.StrSubstitutor;

import exception.BadFormedBinaryNumberException;
import exception.NotPatientGenomicFileParsedException;
import exception.VariantDoesNotMatchAnyAllowedVariantException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import safetycode.GeneticMarkerGroup;
import safetycode.Genotype;
import safetycode.GenotypeElement;
import safetycode.MedicineSafetyProfile_v2;
import utils.Common;
import utils.StringReader;

/**
 * Servlet implementation class AlleleGeneralInterpreter
 */
public class AlleleGeneralInterpreter extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AlleleGeneralInterpreter() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String path = this.getServletContext().getRealPath("/");
		path=path.replaceAll("\\\\", "/");
		
		String code = "0";
		StringBuffer contentHTML = new StringBuffer("");
		
		MedicineSafetyProfile_v2 myProfile = new MedicineSafetyProfile_v2(path+Common.ONT_NAME);
		ArrayList<GeneticMarkerGroup> listGroups = myProfile.getListGenotypeGroups();
		String selection="";
		for(GeneticMarkerGroup gmg: listGroups){
			if(!selection.isEmpty()) selection+=",";
			selection+=request.getParameter(gmg.getGeneticMarkerName()+"-0")+";"+request.getParameter(gmg.getGeneticMarkerName()+"-1");			
		}
		
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
		
		if(cache!=null && cache.isKeyInCache(selection)){
			Element element = cache.get(selection);
			Object object = element.getObjectValue();
			if(String.class.isInstance(object)){
				code = (String) object;
			}
		}else{
			ArrayList<GenotypeElement> listGenotypeElements = new ArrayList<GenotypeElement>();
			for(GeneticMarkerGroup gmg: listGroups){
				String variant1=request.getParameter(gmg.getGeneticMarkerName()+"-0");
				String variant2=request.getParameter(gmg.getGeneticMarkerName()+"-1");
				
				if(variant1==null || variant2==null){
					variant1="null";
					variant2="null";
				}
				
				if(variant1.compareTo(variant2)>0){
					String aux = variant1;
					variant1=variant2;
					variant2=aux;
				}
				
				String criteriaSyntax=variant1+";"+variant2;
				
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
			
			Genotype genotype = new Genotype(listGenotypeElements);
			myProfile.setGenotype(genotype);
			try {
				code = myProfile.getBase64ProfileString();
				if(cache!=null){
					cache.put(new Element(selection,code)); //Add the new element into the cache.
					cache.flush(); //We flush the cache to make all element persistent on disk. This avoid the wrong insert of an element due to application errors. We can avoid this here if the performance of the application is penalized.
				}
			} catch (NotPatientGenomicFileParsedException e1) {
				e1.printStackTrace();
			} catch (BadFormedBinaryNumberException e1) {
				e1.printStackTrace();
			}
		}
		
		response.setContentType("text/html");
		PrintWriter out;
		try {
			out = response.getWriter();
		} catch (IOException e1) {
			throw new ServletException("Error when processing the response.");
		}
		
		String encodedProfileURL = URLEncoder.encode(Common.ROOT_URL+"/"+Common.VERSION+"/"+code, "UTF-8");
    	contentHTML.append("<p align='center'><img src='"+Common.ROOT_URL+"/MSCImageGenerator?url=" + encodedProfileURL + "' alt='Medicine Safety Code' /></p>");
    	contentHTML.append("<p>You can visit the generated profile <a href='" + Common.ROOT_URL+"/"+Common.VERSION+"/"+code+ "'> here</a>.</p>");
    	StringReader myStringReader = new StringReader();
		String templateString;
		try {			
	        templateString = myStringReader.readFile(path+"general-template.html");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("Error when processing the reponse template");
		}
		Map<String,StringBuffer> valuesMap = new HashMap<String,StringBuffer>();
		valuesMap.put("content", contentHTML);
		StrSubstitutor sub = new StrSubstitutor(valuesMap);
		String resolvedString = sub.replace(templateString);
		out.println(resolvedString);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

}
