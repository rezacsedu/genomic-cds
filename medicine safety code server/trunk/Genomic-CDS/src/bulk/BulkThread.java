package bulk;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import safetycode.FileParserFactory;
import safetycode.MedicineSafetyProfileOptimized;

public class BulkThread extends Thread {
	
	/**This instance represents the coordinator of all threads.*/
	private CoordinateExecution ce;
	private int index;
	
	
	/**
	 * Constructor of the class.
	 * 
	 * @param coordinate	It coordinates the execution of all threads, provides new input data and compound the final results.
	 * @param index			The unique number to identify the thread among others.
	 * */
	public BulkThread(CoordinateExecution coordinate, int index){
		super();
		ce=coordinate;
		this.index=index;
	}
	
	//Replaced because of a new version of MedicineSafetyProfile
	/*
	public void run(){
		File fileInput = ce.getNextFile();
		ArrayList<String> results = null;
		ArrayList<String> sortedSNP=ce.getSortedSNP();
		ArrayList<String> sortedPoly=ce.getSortedPoly();
		ArrayList<String> sortedRule=ce.getSortedRule();
		String ontology = ce.getOntology();
		
		try{System.out.println("File name = "+fileInput.getName());
			while(fileInput!=null){
				
				MedicineSafetyProfileOWLAPI msp = new MedicineSafetyProfileOWLAPI(ontology);
				InputStream input = new FileInputStream(fileInput);
				String report = msp.read23AndMeFileStream(input);
				input.close();
				ArrayList<String> reportStatistics = parseReportFile(report);
				Integer nLinesProcessed = Integer.parseInt(reportStatistics.get(0));
				if(reportStatistics.get(1)!="0" && nLinesProcessed>900000){
					results = msp.getStatistics(sortedSNP,sortedPoly, sortedRule);
					results.addAll(reportStatistics);
					ce.compoundResults(fileInput.getName(), results, true);
				}
				fileInput = ce.getNextFile();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of thread nº "+index);
	}*/
	
	
	/**
	 * It process the input files and generate the inferences and their corresponding statistics. This method will be executed in parallel. It will finish when there are no more input files to process. 
	 * 
	 * */
	public void run(){
		File fileInput = ce.getNextFile();
		
		ArrayList<String> results = null;
		ArrayList<String> sortedSNP=ce.getSortedSNP();
		ArrayList<String> sortedPoly=ce.getSortedPoly();
		ArrayList<String> sortedRule=ce.getSortedRule();
		String ontology = ce.getOntology();
		
		try{
			
			while(fileInput!=null){
				MedicineSafetyProfileOptimized msp = new MedicineSafetyProfileOptimized(ontology);
				msp.setDesc(" Thread "+index+" with file "+fileInput.getName()+" ");
				InputStream input = new FileInputStream(fileInput);
				String report = "";
				if(fileInput.getName().contains("exome-vcf.txt")){
					report = msp.parseFileStream(input, FileParserFactory.FORMAT_VCF_FILE);
				}else{
					report = msp.parseFileStream(input, FileParserFactory.FORMAT_23ANDME_FILE);
				}
				input.close();
				msp.readBase64ProfileString(msp.getBase64ProfileString());
				ArrayList<String> reportStatistics = parseReportFile(report);
				Integer nLinesProcessed = Integer.parseInt(reportStatistics.get(0));
				if(reportStatistics.get(1)!="0" && nLinesProcessed>90000){
					results = msp.getStatistics(sortedSNP,sortedPoly, sortedRule);
					results.addAll(reportStatistics);
					ce.compoundResults(fileInput.getName(), results, true);
				}else{
					System.out.println(index+" No results from "+fileInput.getName());
				}
				fileInput = ce.getNextFile();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("End of thread nº "+index);
	}
		
	/**
	 * It read the report of a parsed file to obtain their processing indicators.
	 * 
	 * @param report	The resulting report of the parsed file.
	 * @return	List of indicators provided in the resulting report.
	 * */
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
