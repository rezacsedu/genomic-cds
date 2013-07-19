package safetycode;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import utils.Common;

/**
 * Servlet implementation class SafetyCodeGenerator 
 */
public class PatientOWLGenerator extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PatientOWLGenerator() {
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

		response.setContentType("text/turtle");
		Writer out = response.getWriter();
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
	                // Process form file field (input type="file").
	                //String fieldname = item.getFieldName();
	                //if (fieldname == "file") { // IF clause did not seem to match even though it should have
	                my23andMeFileItem = item;
	                //}
	            }
	        }
	        if (my23andMeFileItem == null) {
	        	throw new ServletException("File is missing.");
	        }
	        MedicineSafetyProfile myProfile = new MedicineSafetyProfile();
        	myProfile.read23AndMeFileStream(my23andMeFileItem.getInputStream(), strandOrientationOfInputData);
        	myProfile.getRDFModel().getWriter().write(myProfile.getRDFModel(), out, "http://safety-code.org/ont/default-namespace/");
	    } catch (FileUploadException e) {
	        throw new ServletException("Cannot parse multipart request.", e);
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}
}
