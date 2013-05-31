
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.text.StrSubstitutor;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Servlet implementation class SafetyCodeInterpreter
 */

public class SafetyCodeInterpreter extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Model simpleSPARQLRulesModel;
	private RDFReader simpleSPARQLRulesModelReader;
	private MedicineSafetyProfile bootstrapProfile;

	/**
	 * @throws IOException
	 * @see HttpServlet#HttpServlet()
	 */
	public SafetyCodeInterpreter() throws Exception {
		super();
		
		// initialize bootstrapProfile, which is used to speed up the creation of new MedicineSafetyProfiles
		// TODO: This should probably be re-implemented by implementing the clone() method in MedicineSafetyProfile
		bootstrapProfile = new MedicineSafetyProfile();
		
		// Load Simple SPARQL Rules file containing decision support logic
		InputStream s = this.getClass().getResourceAsStream("cds.ttl");
		simpleSPARQLRulesModel = ModelFactory.createDefaultModel();
		simpleSPARQLRulesModelReader = simpleSPARQLRulesModel.getReader("N3");
		simpleSPARQLRulesModelReader.read(simpleSPARQLRulesModel, s,
				"http://safety-code.org/ont/cds.ttl#>");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		// String base64ProfileString = request.getParameter("code");
		
		String base64ProfileString = request.getPathInfo().substring(1);
		System.out.println(base64ProfileString);
		
		if ((base64ProfileString.equals("")) || (base64ProfileString == null)) 
			throw (new ServletException("Error: Submitted code is invalid or missing."));
		
		String queryString;
		Query query;
		QueryExecution qexec;
		ResultSet results;
		Vector<String> sparqlConstructQueries = new Vector();

		MedicineSafetyProfile myProfile = new MedicineSafetyProfile(bootstrapProfile.getRDFModel());

		myProfile.readBase64ProfileString(base64ProfileString);
		
		Model myProfileRDFModel = myProfile.getRDFModel();

		StringBuffer recommendationsHTML = new StringBuffer("");
		StringBuffer rawDataHTML = new StringBuffer("");

		/*
		 * Generate inferences
		 */

		queryString = Common.SPARQL_NAME_SPACES
				+ "PREFIX ssr: <http://purl.org/zen/ssr.ttl#> \n"
				+ "SELECT ?query_code \n" 
				+ "WHERE { \n"
				+ "  ?query a ssr:SPARQL_query ; \n"
				+ "         ssr:query_code ?query_code . \n" 
				+ " }";
		
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, simpleSPARQLRulesModel);

		try {
			results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				// Collect SPARQL construct queries for rules in the vector
				sparqlConstructQueries.add(soln.getLiteral("query_code").getString());
			}
		} finally {
			qexec.close();
		}
		
		// Iterate through vector containing SPARQL construct queries and add results to the model.
		for (Enumeration el=sparqlConstructQueries.elements(); el.hasMoreElements(); ) {
			queryString = (String) el.nextElement();
			// TODO: Add check for skipping queries other than CONSTRUCT queries.
			
			query = QueryFactory.create(queryString);
			qexec = QueryExecutionFactory.create(query, myProfileRDFModel);
			
			try {
				// Add triples produced by SPARQL query to model
				myProfileRDFModel.add(qexec.execConstruct());
			} finally {
				qexec.close();
			}
		}
		
		/*
		 * Output CDS recommendations
		 */
		queryString = Common.SPARQL_NAME_SPACES
				+ "SELECT ?CDS_message_label \n"
				+ "WHERE { \n"
				+ "  ?this sc:CDS_message ?CDS_message . \n" 
				+ "  ?CDS_message rdfs:label ?CDS_message_label . \n"
				+ "} ";

		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, myProfileRDFModel);
		
		recommendationsHTML.append("<div data-filtertext=\"warfarin\" data-role=\"collapsible\"><h3>Warfarin</h3>");
		try {
			results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				recommendationsHTML.append("<div>"
						+ soln.getLiteral("CDS_message_label") + "</div>\n");
			}
		} finally {
			qexec.close();
			recommendationsHTML.append("</div>\n");
		}

		/*
		 * Output raw data
		 */
		
		/*
		queryString = Common.SPARQL_NAME_SPACES
				+ "SELECT DISTINCT ?variant WHERE {sc:this_patient a ?variant . } ";

		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, myProfileRDFModel);
		
		results = qexec.execSelect();
		ResultSetFormatter.out(System.out, results); 
		*/
		
		

		queryString = Common.SPARQL_NAME_SPACES
				+ "SELECT DISTINCT ?rank ?symbol_of_associated_gene ?criteria_syntax WHERE {?item sc:rank ?rank . " 
				+ "							OPTIONAL { ?item pgx:symbol_of_associated_gene ?symbol_of_associated_gene . } "
				+ "					       	sc:this_patient a ?variant . "
				+ "							?variant rdfs:subClassOf ?item . "
				+ "							?variant sc:criteria_syntax ?criteria_syntax . } "
				+ "ORDER BY ?symbol_of_associated_gene ";

		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, myProfileRDFModel);

		rawDataHTML.append("<ul>\n");
		try {
			results = qexec.execSelect();
			//ResultSetFormatter.out(System.out, results); 
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				if (soln.getLiteral("symbol_of_associated_gene") == null) {
					rawDataHTML.append("<li>No associated gene: "
							+ soln.getLiteral("criteria_syntax") + "</li>\n");
				} else {
					rawDataHTML.append("<li><b>" + soln.getLiteral("symbol_of_associated_gene") + ":</b> "
						+ soln.getLiteral("criteria_syntax") + "</li>\n");
				}
			}
			

			// ResultSetFormatter.out(System.out, results, query);

			/*
			 * base2ProfileString = "1"; // "1" is prepended to all base2
			 * strings (because leading zeros would be removed in conversion
			 * steps etc.)
			 * 
			 * for (; results.hasNext();) { QuerySolution soln =
			 * results.nextSolution(); Literal bit_code =
			 * soln.getLiteral("bit_code");
			 * System.out.println(bit_code.getLexicalForm()); base2ProfileString
			 * += bit_code.getLexicalForm(); }
			 */
			
		} 
		finally {
			qexec.close();
			rawDataHTML.append("</ul>\n");
		}

		// Below, the Apache StrSubstitutor class is used as a very simple
		// templating engine
		
		StringReader myStringReader = new StringReader();
		String templateString = myStringReader.readFile("interpretation-template.html");
		
		Map valuesMap = new HashMap();
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
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
