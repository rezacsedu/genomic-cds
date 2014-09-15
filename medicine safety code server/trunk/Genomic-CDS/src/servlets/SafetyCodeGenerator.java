package servlets;

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

import safetycode.FileParserFactory;
import safetycode.MedicineSafetyProfile_v2;
import utils.Common;
import utils.StringReader;

/**
 * Servlet implementation class SafetyCodeGenerator.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
		response.setContentType("text/html");
		
		PrintWriter out;
		try {
			out = response.getWriter();
		} catch (IOException e1) {
			throw new ServletException("Error when processing the response.");
		}
		StringBuffer contentHTML = new StringBuffer("");
		contentHTML.append("<h1>A Medicine Safety Code was generated for your data</h1>");
		String strandOrientationOfInputData = Common.DBSNP_ORIENTATION;
		int fileformat = FileParserFactory.FORMAT_23ANDME_FILE;
		FileItem file2Parse = null;
		String path =  this.getServletContext().getRealPath("/");
		path=path.replaceAll("\\\\", "/");

		try {
			@SuppressWarnings("unchecked")
			List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
	        for (FileItem item : items) {
	            if (item.isFormField()) {
	                // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
	                String fieldname = item.getFieldName();
	                String fieldvalue = item.getString();
	                //The option forward_orientation is the default value.
	                if(fieldname.equals("strand-orientation") && fieldvalue.equals("forward-orientation")) strandOrientationOfInputData = Common.FORWARD_ORIENTATION;
	                if(fieldname.equals("file-format") && fieldvalue.equals("vcf-format")) fileformat = FileParserFactory.FORMAT_VCF_FILE;
	            } else {
	            	file2Parse = item;
	            }
	        }
	        if (file2Parse == null) {
	        	throw new ServletException("File is missing.");
	        }
	        	         
	        MedicineSafetyProfile_v2 myProfile = new MedicineSafetyProfile_v2(path);
	        String processingReport = myProfile.parseFileStream(file2Parse.getInputStream(), strandOrientationOfInputData,fileformat);
        	String code = myProfile.getBase64ProfileString();
	        String encodedProfileURL = URLEncoder.encode(Common.ROOT_URL+"/"+Common.VERSION+"/"+code, "UTF-8");
        	contentHTML.append("<p align='center'><img src='"+Common.ROOT_URL+"/MSCImageGenerator?url=" + encodedProfileURL + "' alt='Medicine Safety Code' /></p>");
        	contentHTML.append("<p>You can visit the generated profile <a href='" + Common.ROOT_URL+"/"+Common.VERSION+"/"+code+ "'> here</a>.</p>");
        	contentHTML.append("<h3>Processing report</h3><p>\n" + processingReport + "\n</p>");
        	
	    } catch (FileUploadException e) {
	        throw new ServletException("Cannot parse multipart request.");
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	throw new ServletException("Unexpected error has occurred.");
		}
		
		// Below, the Apache StrSubstitutor class is used as a very simple templating engine
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
}
