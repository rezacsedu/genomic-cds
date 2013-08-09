package bulk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import safetycode.MedicineSafetyProfileOWLAPI;

public class BulkThread extends Thread {
	
	
	private CoordinateExecution ce;
	
	public BulkThread(CoordinateExecution coordinate){
		super();
		ce=coordinate;
	}
	
	public void run(){
		File fileInput = ce.getNextFile();
		ArrayList<String> results = null;
		ArrayList<String> sortedSNP=ce.getSortedSNP();
		ArrayList<String> sortedPoly=ce.getSortedPoly();
		ArrayList<String> sortedRule=ce.getSortedRule();
		String ontology = ce.getOntology();
		try{
			while(fileInput!=null){
				System.out.println("Start parsing file "+fileInput.getName());
				MedicineSafetyProfileOWLAPI msp = new MedicineSafetyProfileOWLAPI(ontology);
				InputStream input = new FileInputStream(fileInput);
				String report = msp.read23AndMeFileStream(input);
				input.close();
				ArrayList<String> reportStatistics = parseReportFile(report);
				if(reportStatistics.get(1)!="0"){
					results = msp.getStatistics(sortedSNP,sortedPoly, sortedRule);
					results.addAll(reportStatistics);
					ce.compoundResults(fileInput.getName(), results, true);
				}
				fileInput = ce.getNextFile();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private ArrayList<String> parseReportFile(String report){
		ArrayList<String> results = new ArrayList<String>();
		
		String processedLines						= "";
		String processedMatchingLines				= "";
		String linesThatDidNotMatchAllowedVariant	= "";
		if(report.indexOf("<b>Processed ")>=0){
			report = report.substring(report.indexOf("<b>Processed ")+13);
			processedLines = report.substring(0,report.indexOf(" lines describing variants.")).trim();
			report = report.substring(report.indexOf("variants.")+9);
		}
		if(report.indexOf("Of these, ")>=0){
			report = report.substring(report.indexOf("Of these, ")+10);
			processedMatchingLines = report.substring(0,report.indexOf(" lines matched allowed")).trim();
			report = report.substring(report.indexOf("RS numbers.")+11);
		}
		if(report.indexOf("Of these, ")>=0){
			report = report.substring(report.indexOf("Of these, ")+10);
			linesThatDidNotMatchAllowedVariant = report.substring(0,report.indexOf(" lines contained genotypes")).trim();
		}
		
		results.add(processedLines);
		results.add(processedMatchingLines);
		results.add(linesThatDidNotMatchAllowedVariant);
			
		return results;
	}
	

}
