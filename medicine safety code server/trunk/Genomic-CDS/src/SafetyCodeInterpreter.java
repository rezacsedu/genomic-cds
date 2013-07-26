
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.text.StrSubstitutor;

import safetycode.MedicineSafetyProfileOWLAPI;
import safetycode.StringReader;

import exception.VariantDoesNotMatchAnAllowedVariantException;


/**
 * Servlet implementation class SafetyCodeInterpreter
 */

public class SafetyCodeInterpreter extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private MedicineSafetyProfileOWLAPI bootstrapProfile;

	/**
	 * @throws IOException
	 * @see HttpServlet#HttpServlet()
	 */
	public SafetyCodeInterpreter() throws Exception {
		super();
		
		// initialize bootstrapProfile, which is used to speed up the creation of new MedicineSafetyProfiles
		bootstrapProfile = new MedicineSafetyProfileOWLAPI();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		String base64ProfileString = request.getPathInfo().substring(1);
		
		if ((base64ProfileString.equals("")) || (base64ProfileString == null)) 	throw (new ServletException("Error: Submitted code is invalid or missing."));
		
		try {
			bootstrapProfile.readBase64ProfileString(base64ProfileString);
		} catch (VariantDoesNotMatchAnAllowedVariantException e) {
			throw (new ServletException( e.getMessage()));
		}
		
		HashMap<String,HashSet<String>> list_recommendations = bootstrapProfile.obtainDrugRecommendations();
		Map<String,StringBuffer> valuesMap = new HashMap<String, StringBuffer>();
		
		// Output raw data
		StringBuffer rawDataHTML = new StringBuffer("");
		if(list_recommendations.containsKey("raw_data")){
			rawDataHTML.append("<ul>");
			Iterator<String> list_data = list_recommendations.get("raw_data").iterator();
			while(list_data.hasNext()){
				rawDataHTML.append("<li>"+list_data.next()+"</li>");
			}
			rawDataHTML.append("</ul>");
			list_recommendations.remove("raw_data");
		}
		valuesMap.put("raw_data", rawDataHTML);
		
		// Output recommendations
		
		if(!list_recommendations.isEmpty()){
			Iterator<String> it_keys = list_recommendations.keySet().iterator();
			while(it_keys.hasNext()){
				String key = it_keys.next();
				StringBuffer recommendationsHTML = new StringBuffer("");
				recommendationsHTML.append("<ul>");
				Iterator<String> list_data = list_recommendations.get(key).iterator();
				while(list_data.hasNext()){
					recommendationsHTML.append("<li>"+list_data.next()+"</li>");
				}
				recommendationsHTML.append("</ul>");
				valuesMap.put("recommendations_"+key.toLowerCase(), recommendationsHTML);
			}
			list_recommendations.clear();
		}
		
		
		// Below, the Apache StrSubstitutor class is used as a very simple
		// templating engine		
		StringReader myStringReader = new StringReader();
		String templateString = myStringReader.readFile("interpretation-template.html");
		StrSubstitutor sub = new StrSubstitutor(valuesMap);
		String resolvedString = sub.replace(templateString);

		out.println(resolvedString);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
