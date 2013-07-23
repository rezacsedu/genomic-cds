package safetycode;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.text.StrSubstitutor;

import eu.trowl.jena.TrOWLJenaFactory;
import exception.VariantDoesNotMatchAnAllowedVariantException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Servlet implementation class SafetyCodeInterpreter
 */

public class SafetyCodeInterpreter extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private MedicineSafetyProfile bootstrapProfile;

	/**
	 * @throws IOException
	 * @see HttpServlet#HttpServlet()
	 */
	public SafetyCodeInterpreter() throws Exception {
		super();
		
		// initialize bootstrapProfile, which is used to speed up the creation of new MedicineSafetyProfiles
		bootstrapProfile = new MedicineSafetyProfile();
		Model model = bootstrapProfile.getRDFModel();
		OntModel reasonerModel = ModelFactory.createOntologyModel(TrOWLJenaFactory.THE_SPEC );
		reasonerModel.addSubModel(model,true);
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
		
		if ((base64ProfileString.equals("")) || (base64ProfileString == null)) 
			throw (new ServletException("Error: Submitted code is invalid or missing."));
		
		/*String queryString;
		Query query;
		QueryExecution qexec;
		ResultSet results;*/

		MedicineSafetyProfile myProfile = bootstrapProfile;

		try {
			myProfile.readBase64ProfileString(base64ProfileString);
		} catch (VariantDoesNotMatchAnAllowedVariantException e) {
			throw (new ServletException( e.getMessage()));
		}
		
		StringBuffer recommendationsHTML = new StringBuffer("");
		StringBuffer rawDataHTML = new StringBuffer("");
		
		
		// Output recommendations
		
		
		// Output raw data
				

		// Below, the Apache StrSubstitutor class is used as a very simple
		// templating engine
		
		StringReader myStringReader = new StringReader();
		String templateString = myStringReader.readFile("interpretation-template.html");
		
		Map<String,StringBuffer> valuesMap = new HashMap<String, StringBuffer>();
		valuesMap.put("recommendations", recommendationsHTML);
		valuesMap.put("raw_data", rawDataHTML);
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
