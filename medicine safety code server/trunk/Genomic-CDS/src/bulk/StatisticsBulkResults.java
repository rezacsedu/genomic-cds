package bulk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class StatisticsBulkResults {
	
	private String fileData;
	
	public StatisticsBulkResults(String fileData){
		this.fileData = fileData;
	}
	
	public void calculateStatistics (){
		HashMap<String,Integer> listRulesOccurrences		= new HashMap<String,Integer>();
		HashMap<String,Integer> listPhenotypesOccurrences	= new HashMap<String,Integer>();
		HashMap<String,Integer>	listHaplotypesOccurrences	= new HashMap<String,Integer>();
		int numberOfSNPs			= 0;
		int numberOfHaplotypes		= 0;
		int numberOfProcessedFiles	= 0;
		int numberOfPhenotypes		= 0;
		int numberOfCDSRules		= 0;
		int numberOfEmptyFiles		= 0;
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileData));
			try{
				String line = "";
				
				while((line = br.readLine()) != null){
					if(line.isEmpty() || line.contains("Statistics")) continue;
					
					if(line.contains("_yearofbirth_")){
						numberOfProcessedFiles++;
						continue;
					}
					
					if(line.contains("Number of SNP =")){
						Integer occurrences = Integer.parseInt(line.substring(line.indexOf("=")+1,line.indexOf(";")).trim());
						if(occurrences == 0) numberOfEmptyFiles++;
						numberOfSNPs += occurrences;
						continue;
					}
					
					if(line.contains("Number of Haplotypes =")){
						Integer occurrences = Integer.parseInt(line.substring(line.indexOf("=")+1,line.indexOf(";")).trim());
						numberOfHaplotypes += occurrences;
						line = line.substring(line.indexOf(";")+1).trim();
						if(line.contains(";")){
							String[] tokens = line.split(";");
							occurrences = 0;
							for(int i=0;i<tokens.length;i++){
								if(listHaplotypesOccurrences.containsKey(tokens[i])){
									occurrences = listHaplotypesOccurrences.get(tokens[i]);
								}
								listHaplotypesOccurrences.put(tokens[i], occurrences+1);
							}
						}
						continue;
					}
					
					if(line.contains("Number of phenotype rules =")){
						Integer occurrences = Integer.parseInt(line.substring(line.indexOf("=")+1,line.indexOf(";")).trim());
						numberOfPhenotypes += occurrences;
						line = line.substring(line.indexOf(";")+1).trim();
						if(line.contains(";")){
							String[] tokens = line.split(";");
							occurrences = 0;
							for(int i=0;i<tokens.length;i++){
								if(listPhenotypesOccurrences.containsKey(tokens[i])){
									occurrences = listPhenotypesOccurrences.get(tokens[i]);
								}
								listPhenotypesOccurrences.put(tokens[i], occurrences+1);
							}
						}
						continue;
					}

					if(line.contains("Number of CDS rules =")){
						Integer occurrences = Integer.parseInt(line.substring(line.indexOf("=")+1,line.indexOf(";")).trim());
						numberOfCDSRules += occurrences;
						line = line.substring(line.indexOf(";")+1).trim();
						if(line.contains(";")){
							String[] tokens = line.split(";");
							occurrences = 0;
							for(int i=0;i<tokens.length;i++){
								if(listRulesOccurrences.containsKey(tokens[i])){
									occurrences = listRulesOccurrences.get(tokens[i]);
								}
								listRulesOccurrences.put(tokens[i], occurrences+1);
							}
						}
						continue;
					}
				}
			}finally{
				br.close();
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileData,true));			
			try{
				bw.write("Number of genetic result files processed = "+numberOfProcessedFiles+"\n");
				bw.write("Number of genetic result files that have 0 SNPs in the ontology = "+numberOfEmptyFiles+"\n");				
				bw.write("Number of parsed SNPs = "+numberOfSNPs+"\n");
				
				bw.write("Number of inferred haplotypes = "+numberOfHaplotypes+"\n");
				Iterator<String> keys = listHaplotypesOccurrences.keySet().iterator();
				ArrayList<String> listKeys = new ArrayList<String>();
				ArrayList<Integer> listOccurrences = new ArrayList<Integer>();
				while(keys.hasNext()){
					String key = keys.next();
					Integer occurrence = listHaplotypesOccurrences.get(key);
					listKeys.add(key);
					listOccurrences.add(occurrence);
				}
				for(int i=0;i<listOccurrences.size()-1;i++){
					for(int j=i+1;j<listOccurrences.size();j++){
						if(listOccurrences.get(i) < listOccurrences.get(j)){
							Collections.swap(listOccurrences, i, j);
							Collections.swap(listKeys, i, j);
						}
					}
				}
				for(int i=0;i<listKeys.size();i++){
					bw.write("\t"+listKeys.get(i)+" = "+listOccurrences.get(i)+"\n");
				}
				
				
				bw.write("Number of inferred phenotypes = "+numberOfPhenotypes+"\n");
				keys = listPhenotypesOccurrences.keySet().iterator();
				listKeys = new ArrayList<String>();
				listOccurrences = new ArrayList<Integer>();
				while(keys.hasNext()){
					String key = keys.next();
					Integer occurrence = listPhenotypesOccurrences.get(key);
					listKeys.add(key);
					listOccurrences.add(occurrence);
				}
				for(int i=0;i<listOccurrences.size()-1;i++){
					for(int j=i+1;j<listOccurrences.size();j++){
						if(listOccurrences.get(i) < listOccurrences.get(j)){
							Collections.swap(listOccurrences, i, j);
							Collections.swap(listKeys, i, j);
						}
					}
				}
				for(int i=0;i<listKeys.size();i++){
					bw.write("\t"+listKeys.get(i)+" = "+listOccurrences.get(i)+"\n");
				}
				
				bw.write("Number of inferred CDS rules = "+numberOfCDSRules+"\n");
				keys = listRulesOccurrences.keySet().iterator();
				listKeys = new ArrayList<String>();
				listOccurrences = new ArrayList<Integer>();
				while(keys.hasNext()){
					String key = keys.next();
					Integer occurrence = listRulesOccurrences.get(key);
					listKeys.add(key);
					listOccurrences.add(occurrence);
				}
				for(int i=0;i<listOccurrences.size()-1;i++){
					for(int j=i+1;j<listOccurrences.size();j++){
						if(listOccurrences.get(i) < listOccurrences.get(j)){
							Collections.swap(listOccurrences, i, j);
							Collections.swap(listKeys, i, j);
						}
					}
				}
				for(int i=0;i<listKeys.size();i++){
					bw.write("\t"+listKeys.get(i)+" = "+listOccurrences.get(i)+"\n");
				}
				
			}finally{
				bw.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
