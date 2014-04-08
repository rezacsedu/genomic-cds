package safetycode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Common;

/**
 * Parse the content of a 23andme file to gather SNPs information of a patient's genotype
 * 
 * @author Jose
 * @version 2.0
 * 
 * */
public class FileParser_23andme_Format implements FileParser{
	/**List of parsed SNPs variants.*/
	private HashMap<String, SNPElement>	listSNPs;
	/**List of SNP group information.*/
	private HashMap<String, SNPsGroup>	listSNPsGroups;
	
	
	/**
	 * Construct the 23andme parser with the default list of SNPs and the information of SNP groups in the ontology.
	 * 
	 * @param listSNPs			List of default SNPs for a 23andme genotype file.
	 * @param listSNPsGroups	List of SNP groups described in the MSC ontology.
	 * */
	public FileParser_23andme_Format(ArrayList<SNPElement> listSNPs, ArrayList<SNPsGroup> listSNPsGroups) {
		
		this.listSNPs		= new HashMap<String,SNPElement>();
		for(SNPElement snpe : listSNPs){
			this.listSNPs.put(snpe.getGeneticMarkerName(), snpe);
		}
		
		this.listSNPsGroups		= new HashMap<String,SNPsGroup>();
		for(SNPsGroup snpg : listSNPsGroups){
			this.listSNPsGroups.put(snpg.getGeneticMarkerName(), snpg);
		}
	}
	
	
	/**
	 * It parses the input file stream in 23andme format and produce the list of SNPs using the corresponding strand orientation.
	 * 
	 * @param fileStream		The genotype input data in 23andme file format.
	 * @param strandOrientation	The orientation of the strands in the 23andme file format.
	 * @return	The report of the file processing that can be used for detecting inconsistencies in the file format.
	 * */
	public String parse(InputStream my23AndMeFileStream, String my23AndMeStrandOrientation) {
		
		int processedLines 						= 0;	//Number of processed lines of strands from the input 23AndMe file.
		int processedMatchingLines				= 0;	//Number of processedLines that corresponds to markers in the model.
		int linesThatDidNotMatchAllowedVariant	= 0;	//Number of processedMatchingLines that could not be matched in the model.
		String processingReport					= "<ul>\n";	//Report that contains the missing matched criteria syntax and general statistics of the parser.
		
		//Parsing the 23AndMe file
		try{
			String rsid 			= "";	//strand id
			String my23AndMeSNPCode = "";	//Two char code that is related to rsid.
			String line 			= "";	//Line of the parsed file.
			BufferedReader br = new BufferedReader(new InputStreamReader(my23AndMeFileStream));
			while((line=br.readLine())!=null){
				line=(line.replaceAll("#.*","")).trim();	//Avoid comments
				if(line.isEmpty())	continue;
				
				line=line.replaceAll(" ","\t");	
				my23AndMeSNPCode = "";
				processedLines++;
				
				String[] lineArray = line.split("\t");		//Obtain the columns of the strand
				if(lineArray.length>=4){
					rsid = lineArray[0].trim();				//Gather rsid of the strand
					for(int i=3;i<lineArray.length; i++){
						my23AndMeSNPCode += lineArray[i];	//Gather code of the strand
					}
				}else continue; //Skip this line because of wrong number of columns associated to the strand
				
				//Add the code to the related marker
				if(listSNPs!=null && listSNPs.containsKey(rsid)){//Check which is the related marker defined in the model for this strand
					SNPElement	snpe = listSNPs.get(rsid);
					SNPsGroup	snpg = listSNPsGroups.get(rsid);
					processedMatchingLines++;
					String[] variants = getVariants(my23AndMeSNPCode,my23AndMeStrandOrientation,snpg.getStrandOrientation()); //Obtain the correct code regarding orientation and alphabetical order.
					if(snpg.allowedVariants(variants[0], variants[1])){
						if(variants[0].equals("I")){
							variants[0] = snpg.getInsertionVariant();
						}
						if(variants[1].equals("I")){
							variants[1] = snpg.getInsertionVariant();
						}
						snpe.setVariants(variants[0],variants[1]);//Generate the new criteria syntax for this strand
						if(variants[0].equals("D") || variants[1].equals("D")) System.out.println("[1] snp="+snpe.getGeneticMarkerName()+"_"+snpe.getCriteriaSyntax());
					}else{
						linesThatDidNotMatchAllowedVariant++;
						processingReport+="<li>Warning: " + rsid + "(" + variants[0] + ";" + variants[1] + ") with orientation " + my23AndMeStrandOrientation + " does not match any allowed genotype. Only genotypes listed in dbSNP are allowed. A possible reason for this could be that your data is not based on the same strand (+ or -) as dbSNP, and you did not choose the proper settings for strand orientation. This genotype will be reported as 'null;null' in the resulting Medicine Safety Code.\n";
					}
				}
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		//End parsing 23AndMe file
		
		processingReport+=("</ul><p><b>Processed " + processedLines + " lines describing variants. Of these, " + processedMatchingLines + " lines matched allowed Medicine Safety Code RS numbers. Of these, " +   linesThatDidNotMatchAllowedVariant + " lines contained genotypes that did not match allowed Medicine Safety Code genotypes. </b></p>");
		
		return processingReport;
	}

	
	
	/**
	 * Get the list of gathered SNPs from the file in 23andme format.
	 * 
	 * @return List of SNPs variant parsed from the 23andme file.
	 * */
	public ArrayList<SNPElement> getListSNPElements() {
		ArrayList<SNPElement> listParsedSNPs = new ArrayList<SNPElement>();
		listParsedSNPs.addAll(listSNPs.values());
		return listParsedSNPs;
	}
	
	
	/**
	 * It obtains the correct variant nucleotides when considering its SNP orientation from 23AndMe file and the reference SNP orientation, and its alphabetical order
	 * 
	 * @param snpCode			It contains the two nucleotide character directly from 23AndMe file.
	 * @param orientation_file	It indicates the orientation of the 23AndMe file. Only "dbsnp-orientation" or "forward-orientation" are allowed.
	 * @param orientation_seq	It indicates the orientation of the reference SNP orientation. Only "reverse" or "forward" are allowed.
	 * @return		It returns the two dimensional array with the corresponding nucleotides with the correct orientation and alphabetical order.
	 * */
	private String[] getVariants(String snpCode,String orientation_file, String orientation_seq){
		String[] variant = new String[2];
		variant[0]="";
		variant[1]="";
		if(snpCode.length()<=1){
			//System.out.println("Warning: getVariants("+snpCode+","+orientation_file+","+orientation_seq+")");
			variant[0]="null";
			variant[1]="null";
			return variant;
		}
				
		if ((orientation_file == Common.DBSNP_ORIENTATION) && (orientation_seq.equals("reverse"))) {
			char [] variants_array = snpCode.toCharArray();
			snpCode="";
			for(int i=0; i<variants_array.length;i++){
				if(variants_array[i]=='A'){
					snpCode+="T";
					continue;
				}
				if(variants_array[i]=='T'){
					snpCode+="A";
					continue;
				}
				if(variants_array[i]=='C'){
					snpCode+="G";
					continue;
				}
				if(variants_array[i]=='G'){
					snpCode+="C";
					continue;
				}
			}
		}

		variant[0] = snpCode.substring(0,1);
		variant[1] = snpCode.substring(1);
		if(variant[0].compareTo(variant[1])>0){
			String aux = variant[0];
			variant[0] = variant[1];
			variant[1] = aux;
		}

		/*String val_1 = snpCode.substring(0,1);
		String val_2 = snpCode.substring(1);
		try{
			if(val_1.equals("D")) val_1 = "-";
			if(val_2.equals("D")) val_2 = "-";
			
			char [] variants_array = val_2.toCharArray();
			if ((orientation_file == Common.DBSNP_ORIENTATION) && (orientation_seq.equals("reverse"))) {
				if(val_1.equals("A")) val_1 = "T";
				if(val_1.equals("T")) val_1 = "A";
				if(val_1.equals("C")) val_1 = "G";
				if(val_1.equals("G")) val_1 = "C";
				
				for(int i=0; i<variants_array.length;i++){
					if(variants_array[i]=='A'){
						variants_array[i]= 'T';
						continue;
					}
					if(variants_array[i]=='T'){
						variants_array[i]= 'A';
						continue;
					}
					if(variants_array[i]=='C'){
						variants_array[i]= 'G';
						continue;
					}
					if(variants_array[i]=='G'){
						variants_array[i]= 'C';
						continue;
					}
				}
			}
			
			if(variants_array.length>1){
				variant[0] = val_1;
				for(int i=0;i<variants_array.length;i++){
					variant[1] += ""+variants_array[i];
				}
			}else{
				char char_val_1 = val_1.toCharArray()[0];
				if(char_val_1 > variants_array[0]){
					variant[0] = ""+variants_array[0];
					variant[1] = val_1;
				}else{
					variant[0] = val_1;
					variant[1] = ""+variants_array[0];
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}*/
		
		return variant;
	}
}
