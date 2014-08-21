package bulk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import exception.VariantDoesNotMatchAnyAllowedVariantException;

import safetycode.FileParserFactory;
import safetycode.MedicineSafetyProfile_v2;


/**
 * This class coordinates the execution of several threads to parser a large number of files with genotype information.
 * */
public class CoordinateExecution {
	private String ontology;
	private MedicineSafetyProfile_v2 msp;	
	
	/**
	 * Constructor of the class that initializes the bulk process.
	 * 
	 * @param ontology	The ontology location in the file system. 
	 * */
	public CoordinateExecution(String ontology){
		
		this.ontology=ontology;
		msp = new MedicineSafetyProfile_v2(ontology);
	}
	
	
	/**
	 * This method create the threads and coordinates their execution and finally compounds their returned results.
	 * 
	 * @param output	The file where the statistics results will be stored.
	 * @param folder	The folder where the input files are located.
	 * */
	public void execute(String output, String folder){
		
		File inputFolder = new File(folder);
		File[] files = inputFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
               return (pathname.getName().toLowerCase().endsWith("23andme.txt") || pathname.getName().toLowerCase().endsWith("exome-vcf.txt"));
            }
        });
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.write("Statistics ("+ontology+")\n");
			
			for(int i=0;i<files.length;i++){
				File inputFile = files[i];
				int fileFormat = FileParserFactory.FORMAT_23ANDME_FILE;
				if(inputFile.getName().endsWith("exome-vcf.txt")){
					fileFormat = FileParserFactory.FORMAT_VCF_FILE;
				}
						
				try {
					FileInputStream fis = new FileInputStream(inputFile);
					/*String report = */msp.parseFileStream(fis, fileFormat);
					
					ArrayList<String> results = msp.getGenotypeStatistics();
					String resultsData = inputFile.getName()+"\n";
					for(String line: results){
						resultsData+="\t"+line+"\n";
					}
					bw.write(resultsData+"\n");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (VariantDoesNotMatchAnyAllowedVariantException e) {
					e.printStackTrace();
				}
			}
			
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * It receives the raw data from the ontology reasoning process and generate the statistics results.
	 * 
	 * @param inputFolder	The folder that contains the raw data from the reasoning results.
	 * */
	/*public String processRawData(String inputFolder, String outputFolder){
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
	}*/
}
