package safetycode;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.text.StrSubstitutor;

import utils.Common;

/**
 * Servlet implementation class SafetyCodeGenerator 
 */
public class SafetyCodeGenerator extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SafetyCodeGenerator() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		StringBuffer contentHTML = new StringBuffer("");
		contentHTML.append("<h1>A Medicine Safety Code was generated for your data</h1>");
		String strandOrientationOfInputData = "dbsnp-orientation";
		FileItem my23andMeFileItem = null;
		
		try {
	        @SuppressWarnings("unchecked")
			List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
	        for (FileItem item : items) {
	            if (item.isFormField()) {
	                // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
	                String fieldname = item.getFieldName();
	                String fieldvalue = item.getString();
	                System.out.println("Fieldname: " + fieldname + ", Fieldvalue: " + fieldvalue);
	                if (fieldname.equals("strand-orientation") && fieldvalue.equals("forward-orientation")) strandOrientationOfInputData = Common.FORWARD_ORIENTATION;
	                if (fieldname.equals("strand-orientation") && fieldvalue.equals("dbsnp-orientation")) strandOrientationOfInputData = Common.DBSNP_ORIENTATION;
	            } else {
	               my23andMeFileItem = item;
	            }
	        }
	        if (my23andMeFileItem == null) {
	        	throw new ServletException("File is missing.");
	        }
	        MedicineSafetyProfile myProfile = new MedicineSafetyProfile();
        	String processingReport = myProfile.read23AndMeFileStream(my23andMeFileItem.getInputStream(), strandOrientationOfInputData);
        	String encodedProfileURL = URLEncoder.encode(Common.ROOT_URL+myProfile.getBase64ProfileString(), "UTF-8");
        	contentHTML.append("<p align='center'><img src='MSCImageGenerator?code=" + encodedProfileURL + "' alt='Medicine Safety Code' /></p>");
        	contentHTML.append("<p><a href='" + encodedProfileURL.toString() + "'>" + encodedProfileURL.toString() + "</a></p>");
        	contentHTML.append("<h3>Processing report</h3><p>\n" + processingReport + "\n</p>");
	    } catch (FileUploadException e) {
	        throw new ServletException("Cannot parse multipart request.", e);
	    } catch (Exception e) {
			e.printStackTrace();
		}
		
		// Below, the Apache StrSubstitutor class is used as a very simple
		// templating engine
		StringReader myStringReader = new StringReader();
		String templateString = myStringReader.readFile("general-template.html");
		Map<String,StringBuffer> valuesMap = new HashMap<String,StringBuffer>();
		valuesMap.put("content", contentHTML);
		StrSubstitutor sub = new StrSubstitutor(valuesMap);
		String resolvedString = sub.replace(templateString);
		out.println(resolvedString);
		
	}
}
