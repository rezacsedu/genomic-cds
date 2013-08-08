package bulk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.ArrayList;

import safetycode.MedicineSafetyProfileOWLAPI;

public class CoordinateExecution {

	private int pos=0;
	private File[] files;
	private String output;
	private String ontology;
	private ArrayList<String> sortedSNP;
	private ArrayList<String> sortedPoly;
	private ArrayList<String> sortedRule;
	private int nThreads;
	private ArrayList<BulkThread> list_threads;
	
	public CoordinateExecution(String output, File folder, String ontology, int nThreads){
		
		this.output=output;
		this.ontology=ontology;
		this.nThreads=nThreads;
		
		MedicineSafetyProfileOWLAPI msp = new MedicineSafetyProfileOWLAPI(ontology);
		sortedSNP		= msp.getSimplifiedListRsids();
		sortedPoly	= msp.getSimplifiedListPolymorphisms();
		sortedRule	= msp.getSimplifiedListRules();
		
		ArrayList<String> reportResult	= new ArrayList<String>();
		reportResult.add("Processed Lines");
		reportResult.add("Processed Matched Lines");
		reportResult.add("Not allowed variants");
		ArrayList<String> all = new ArrayList<String>();
		all.addAll(sortedSNP);
		all.addAll(sortedPoly);
		all.addAll(sortedRule);
		all.addAll(reportResult);
		
		compoundResults("Input File",all,false);
		
		files = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
               return pathname.getName().toLowerCase().endsWith("23andme.txt");
            }
         });
	}
	
	public void execute(){
		list_threads = new ArrayList<BulkThread>();
		for(int i=0;i<nThreads;i++){
			BulkThread bt = new BulkThread(this);
			bt.start();
			list_threads.add(bt);
		}
		
		for(int i=0;i<nThreads;i++){
			BulkThread bt = list_threads.get(i);
			try {
				bt.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getOntology(){
		return ontology;
	}
	
	public ArrayList<String> getSortedPoly(){
		return sortedPoly;
	}
	
	public ArrayList<String> getSortedSNP(){
		return sortedSNP;
	}
	
	public ArrayList<String> getSortedRule(){
		return sortedRule;
	}
	
	public synchronized File getNextFile(){
		if(files!=null && pos<files.length){
			return files[(pos++)];
		}
		return null;
	}
	
	public synchronized void compoundResults(String file_input, ArrayList<String> results,boolean append){
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(output,append));
			bw.write(file_input+";");
			for(int i=0;i<results.size();i++){
				String value=results.get(i);
				bw.write(value);
				if(i+1<results.size()){
					bw.write(";");
				}
			}
			bw.write("\n");
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	
}
