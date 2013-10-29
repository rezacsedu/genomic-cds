package bulk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import safetycode.MedicineSafetyProfileOptimized;


/**
 * This class coordinates the execution of several threads to parser a large number of files with genotype information.
 * */
public class CoordinateExecution {

	private int pos=0;
	private File[] files;
	private String output;
	private String ontology;
	private ArrayList<String> sortedSNP;
	private ArrayList<String> sortedPoly;
	private ArrayList<String> sortedRule;
	private ArrayList<BulkThread> list_threads;
		
	/**
	 * Constructor of the class that initializes the bulk process.
	 * 
	 * @param ontology	The ontology location in the file system. 
	 * */
	public CoordinateExecution(String ontology){
		
		this.ontology=ontology;
		
		//MedicineSafetyProfileOWLAPI msp = new MedicineSafetyProfileOWLAPI(ontology); //Replaced because of a new version of MedicineSafetyProfile
		MedicineSafetyProfileOptimized msp = new MedicineSafetyProfileOptimized(ontology); 
		sortedSNP	= msp.getSimplifiedListRsids();
		sortedPoly	= msp.getSimplifiedListPolymorphisms();
		sortedRule	= msp.getSimplifiedListRules();
	}
	
	
	/**
	 * This method create the threads and coordinates their execution and finally compounds their returned results.
	 * 
	 * @param output	The file location where the statistics results will be stored.
	 * @param folder	The folder where the input files are located.
	 * @param nThreads	The number of threads that will be created to process the input files.
	 * */
	public void execute(String output, File folder, int nThreads ){

		this.output=output;
		
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
               return (pathname.getName().toLowerCase().endsWith("23andme.txt") || pathname.getName().toLowerCase().endsWith("exome-vcf.txt"));
            }
         });
		
		list_threads = new ArrayList<BulkThread>();
		for(int i=0;i<nThreads;i++){
			BulkThread bt = new BulkThread(this,i);
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
	
	/**
	 * It receives the raw data from the ontology reasoning process and generate the statistics results.
	 * 
	 * @param fileRawData	The file that contains the raw data from the reasoning results.
	 * */
	public String processRawData(String fileRawData){
		String results="";
		String prefix = fileRawData.substring(0,fileRawData.indexOf(".csv"));
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileRawData));

			HashMap<String,Integer> table_snp = new HashMap<String, Integer>();
			for(int i=0;i<sortedSNP.size();i++){
				table_snp.put(sortedSNP.get(i), 0);
			}
			HashMap<String,Integer> table_poly = new HashMap<String, Integer>();
			for(int i=0;i<sortedPoly.size();i++){
				table_poly.put(sortedPoly.get(i), 0);
			}
			HashMap<String,Integer> table_rule = new HashMap<String, Integer>();
			for(int i=0;i<sortedRule.size();i++){
				table_rule.put(sortedRule.get(i), 0);
			}
			
			String linesInfile="";
			String matchedLines="";
			String errorMatched="";
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(prefix+"_files_statistics.csv"));
			bw.write("File name;Lines in the file;Matched lines;Error matched lines;Number of matched SNPs;Number of Alleles matched;Number of rules triggered\n");
			int nFiles=0;
			String linea=br.readLine();//skip the header line of the .csv file
			while((linea=br.readLine())!=null){
				String[] values = linea.split(";");
				String fileName = "";
				nFiles++;
				int nSNP	= 0;
				int nPoly	= 0;
				int nRule	= 0;
				
				if(values.length>0){
					fileName=values[0];
				}
				for(int i=1;i<values.length;i++){
					String aux	= values[i];
					if(i<sortedSNP.size()+1){
						String type	= sortedSNP.get(i-1);
						if(aux.contains(type)&&!aux.contains("null_null")){
							if(table_snp.containsKey(type)){
								table_snp.put(type, table_snp.get(type)+1);
							}else{
								table_snp.put(type, 1);
							}
							nSNP++;
						}
						continue;
					}
					if((i>=(sortedSNP.size()+1)) && (i<sortedSNP.size()+sortedPoly.size()+1)){
						if(!aux.isEmpty()){
							String type	= sortedPoly.get(i-(sortedSNP.size()+1));
							if(table_poly.containsKey(type)){
								table_poly.put(type, table_poly.get(type)+1);
							}else{
								table_poly.put(type, 1);
							}
							nPoly++;
						}
						continue;
					}
					
					if((i>=(sortedSNP.size()+sortedPoly.size()+1)) && (i<sortedSNP.size()+sortedPoly.size()+sortedRule.size()+1)){
						if(!aux.isEmpty()){
							String type	= sortedRule.get(i-(sortedSNP.size()+sortedPoly.size()+1));
							if(table_rule.containsKey(type)){
								table_rule.put(type, table_rule.get(type)+1);
							}else{
								table_rule.put(type, 1);
							}
							nRule++;
						}
						continue;
					}
					if(i==values.length-3){
						linesInfile = aux;
					}
					if(i==values.length-2){
						matchedLines = aux;
					}
					if(i==values.length-1){
						errorMatched = aux;
					}
				}
				bw.write(fileName+";"+linesInfile+";"+matchedLines+";"+errorMatched+";"+nSNP+";"+nPoly+";"+nRule+"\n");
			}
			br.close();
			bw.close();
			
			BufferedWriter bw_warn = new BufferedWriter(new FileWriter(prefix+"_warnings.csv"));
			BufferedWriter bw_final = new BufferedWriter(new FileWriter(prefix+"_final_statistis.csv"));
			bw_final.write("Number of files processed is "+nFiles+"\n");
			bw = new BufferedWriter(new FileWriter(prefix+"_SNPs_statistics.csv"));
			bw.write("SNP name;Number of occurrences\n");
			int value = 0;
			int max=0;
			String max_label="";
			Iterator<String> itKeys = table_snp.keySet().iterator();
			while(itKeys.hasNext()){
				String key = itKeys.next();
				bw.write(key+";"+table_snp.get(key)+"\n");
				value+=table_snp.get(key);
				if(table_snp.get(key)==0){
					bw_warn.write("Warning: SNP = "+key+" is empty.\n");
				}
				if(table_snp.get(key)>max){
					max=table_snp.get(key);
					max_label = key;
				}
			}
			bw.close();
			bw_final.write("Matched SNPs = "+value+"\nMax SNP "+max_label+" -> value = "+max+"\n");
			bw_final.write("Mean matched SNPs = "+(value/(double)nFiles)+"\n");
			
			bw = new BufferedWriter(new FileWriter(prefix+"_Poly_statistics.csv"));
			bw.write("Polymorphism name;Number of occurrences\n");
			value = 0;
			max=0;
			max_label="";
			itKeys = table_poly.keySet().iterator();
			while(itKeys.hasNext()){
				String key = itKeys.next();
				bw.write(key+";"+table_poly.get(key)+"\n");
				value+=table_poly.get(key);
				if(table_poly.get(key)==0){
					bw_warn.write("Warning: Poly = "+key+" is empty.\n");
				}
				if(table_poly.get(key)>max){
					max=table_poly.get(key);
					max_label = key;
				}
			}
			bw.close();
			bw_final.write("Matched Poly = "+value+"\nMax poly "+max_label+"-> value = "+max+"\n");
			bw_final.write("Mean matched Polymorphisms = "+(value/(double)nFiles)+"\n");
			
			bw = new BufferedWriter(new FileWriter(prefix+"_Rules_statistics.csv"));
			bw.write("Rules name;Number of occurrences\n");
			value = 0;
			max=0;
			max_label="";
			itKeys = table_rule.keySet().iterator();
			while(itKeys.hasNext()){
				String key = itKeys.next();
				bw.write(key+";"+table_rule.get(key)+"\n");
				value+=table_rule.get(key);
				if(table_rule.get(key)==0){
					bw_warn.write("Warning: Rule = "+key+" is empty.\n");
				}
				if(table_rule.get(key)>max){
					max=table_rule.get(key);
					max_label = key;
				}
			}
			bw.close();
			bw_warn.close();
			bw_final.write("Matched rules = "+value+"\nMax rule "+max_label+" -> value = "+max+"\n");
			bw_final.write("Mean matched Rules = "+(value/(double)nFiles)+"\n");
			bw_final.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}
	
	
	/**Get method to return the ontology model.*/
	public String getOntology(){
		return ontology;
	}
	
	
	/**Get method to return the sorted list of polymorphisms.*/
	public ArrayList<String> getSortedPoly(){
		return sortedPoly;
	}
	
	
	/**Get method to return the sorted list of SNPs.*/
	public ArrayList<String> getSortedSNP(){
		return sortedSNP;
	}
	
	
	/**Get method to return the sorted list of Rules.*/
	public ArrayList<String> getSortedRule(){
		return sortedRule;
	}
	
	
	/**Get method to obtain the next file to parse during the bulk process or null if all files have been processed already. Only one thread can access this method.*/
	public synchronized File getNextFile(){
		if(files!=null && pos<files.length){
			return files[(pos++)];
		}
		return null;
	}
	
	
	/**Method to store the results from the statistical analysis of the raw data. Only one thread can access this method.*/
	public synchronized void compoundResults(String file_input, ArrayList<String> results, boolean append){
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
		System.out.println("File "+file_input+" processed.");
	}
	
}
