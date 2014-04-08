package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Prueba1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String linea = "";
		HashMap<String,String> alleleMap =  new HashMap<String,String>();
		HashSet<String> list_alleles = new HashSet<String>();
		try{
			HashMap<String,String[]> code_allele = new HashMap<String,String[]>();
			BufferedReader br = new BufferedReader(new FileReader("d:/patient_demo.txt"));
			while((linea=br.readLine())!=null){
				String[] linea_items = linea.split("_");
				String rsid = linea_items[0];
				list_alleles.add(rsid);
				String allele = linea_items[1];
				if(code_allele.containsKey(rsid)){
					String[] codes = code_allele.get(rsid);
					codes[1] = allele;
				}else{
					String[] codes = {allele,allele};
					code_allele.put(rsid, codes);
				}
			}
			br.close();
			Iterator<String> it_keys = code_allele.keySet().iterator();
			while(it_keys.hasNext()){
				String key = it_keys.next();
				String[] alleles = code_allele.get(key);
				alleleMap.put(key, alleles[0]+alleles[1]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			BufferedReader br = new BufferedReader(new FileReader("d:/workspace/Genomic-CDS/input/user26_file10_yearofbirth_unknown_sex_unknown.23andme.txt"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("d:/workspace/Genomic-CDS/patient_demo.23andme.txt"));
			while((linea=br.readLine())!=null){
				if(linea.startsWith("#")){
					bw.write(linea+"\n");
					continue;
				}
				
				String nuevaLinea = "";
				String[] tokens = linea.split("\t");
				if(alleleMap.containsKey(tokens[0])){
					list_alleles.remove(tokens[0]);
					nuevaLinea = tokens[0]+"\t"+tokens[1]+"\t"+tokens[2]+"\t"+alleleMap.get(tokens[0]);
					bw.write(nuevaLinea+"\n");
				}
			}
			br.close();
			
			Iterator<String> it_allele = list_alleles.iterator();
			while(it_allele.hasNext()){
				String allele = it_allele.next();
				bw.write(allele+"\t0\t0\t"+alleleMap.get(allele)+"\n");
			}			
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
