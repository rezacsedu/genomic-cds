package safetycode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Common;


/**
 * Parse the content of a VCF file to gather SNPs information of a patient's genotype.
 * 
 * @author Jose
 * @version 2.0
 * 
 * */
public class FileParser_VCF_Format implements FileParser{
	/**List of parsed SNPs variants.*/
	private HashMap<String, SNPElement>	listSNPs;
	/**List of SNP group information.*/
	private HashMap<String, SNPsGroup>	listSNPsGroups;
	
	/**
	 * Construct the VCF parser with the default list of SNPs and the information of SNP groups in the ontology.
	 * 
	 * @param listSNPs			List of default SNPs for a VCF genotype file.
	 * @param listSNPsGroups	List of SNP groups described in the MSC ontology.
	 * */
	public FileParser_VCF_Format(ArrayList<SNPElement> listSNPs, ArrayList<SNPsGroup> listSNPsGroups) {
		
		this.listSNPs		= new HashMap<String,SNPElement>();
		for(SNPElement snpe : listSNPs){
			this.listSNPs.put(snpe.getGeneticMarkerName(), snpe);
		}
		
		this.listSNPsGroups	= new HashMap<String,SNPsGroup>();
		for(SNPsGroup snpg : listSNPsGroups){
			this.listSNPsGroups.put(snpg.getGeneticMarkerName(), snpg);
		}
	}
		
	/**
	 * It parses the input file stream in VCF format and produce the list of SNPs using the corresponding strand orientation.
	 * 
	 * @param fileStream		The genotype input data in VCF file format.
	 * @param strandOrientation	The orientation of the strands in the VCF file format.
	 * @return	The report of the file processing that can be used for detecting inconsistencies in the file format.
	 * */
	public String parse(InputStream fileStream, String strandOrientation) {
		
		int processedLines 						= 0;	//Number of processed lines of strands from the input 23AndMe file.
		int processedMatchingLines				= 0;	//Number of processedLines that corresponds to markers in the model.
		int linesThatDidNotMatchAllowedVariant	= 0;	//Number of processedMatchingLines that could not be matched in the model.
		String processingReport					= "<ul>\n";	//Report that contains the missing matched criteria syntax and general statistics of the parser.
		
		
		try{
			String line		= "";	//Line of the parsed file.
			int id_col_pos		= -1;//Position of the column with SNPs id.
			int ref_col_pos		= -1;//Position of the column with the reference nucleotide.
			int alt_col_pos		= -1;//Position of the column with the alternative nucleotide.
			int qual_col_pos	= -1;//Position of the column with quality value.
			int format_col_pos	= -1;//Position of the column with the format elements and their positions.
			int info_col_pos	= -1;//Position of the column with the general info description.
			
			BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
			while((line=br.readLine())!=null){
				
				if(line.startsWith("##")|| line.isEmpty()){
					continue;
				}
				
				if(line.startsWith("#")){
					line=line.replace("#", "");					
					String [] header_line = line.split("\t");
					for(int i = 0; i < header_line.length; i++){
						if(header_line[i].equalsIgnoreCase("ID")){
							id_col_pos = i;
							continue;
						}						
						
						if(header_line[i].equalsIgnoreCase("REF")){
							ref_col_pos = i;
							continue;
						}
						
						if(header_line[i].equalsIgnoreCase("ALT")){
							alt_col_pos = i;
							continue;
						}
						
						if(header_line[i].equalsIgnoreCase("QUAL")){
							qual_col_pos = i;
							continue;
						}
						
						if(header_line[i].equalsIgnoreCase("FORMAT")){
							format_col_pos = i;
							continue;
						}
						if(header_line[i].equalsIgnoreCase("INFO")){
							info_col_pos = i;
							continue;
						}
					}
					continue;
 				}
				
				processedLines++;
				
				if(id_col_pos < 0 || ref_col_pos < 0 || alt_col_pos < 0 || qual_col_pos < 0 || format_col_pos < 0) continue;
				
				String rsid		= "";	//strand id
				//String snpCode	= "";	//char code that is related to snp nucleotides.
				
				String[] tokens = line.split("\t");
				if(tokens.length < format_col_pos+2) continue;
				if(id_col_pos>=0)		rsid = tokens[id_col_pos];
				
				if(listSNPs!=null && listSNPs.containsKey(rsid)){
					//String qual = "";
					//if(qual_col_pos>=0)	qual = tokens[qual_col_pos];//Do something with quality value
					String info		= "";
					if(info_col_pos>=0)		info = tokens[info_col_pos];
					String format	= "";
					if(format_col_pos>=0)	format = tokens[format_col_pos];
					String values	= "";
					if(format_col_pos>=0 && tokens.length>=(format_col_pos+1))		values = tokens[format_col_pos+1];
				
					int gt_pos		= -1;
					int gq_pos		= -1;
					
					//String base_nucleotides = tokens[ref_col_pos]+tokens[alt_col_pos];
	
					String[] format_items = format.split(":");
					for(int i = 0; i < format_items.length; i++){
						if(format_items[i].equalsIgnoreCase("GT")){
							gt_pos = i;
							continue;
						}
						
						if(format_items[i].equalsIgnoreCase("GQ")){
							gq_pos = i;
							continue;
						}
					}
						
					if(gt_pos < 0 || gq_pos < 0) continue;
					
					String[] values_items = values.split(":");
					if(values_items.length < gt_pos || values_items.length < gq_pos) continue;
					String encoded_genotype	= values_items[gt_pos];
					//quality_genotype = values_items[gq_pos];//Do something with quality value
					String[] variants = new String[2];
					variants[0]="-";
					variants[1]="-";
					
					String[] indexes = encoded_genotype.split("[/\\|]");
					for(int i = 0; i < indexes.length; i++){
						if(i>1){
							System.out.println("ERROR: VCF file cotains more than two nucleotides.");
							break;
						}
						if(indexes[i].equals("0")){
							variants[i] = tokens[ref_col_pos];
						}
						if(indexes[i].equals("1")){
							variants[i] = tokens[alt_col_pos];
						}
					}
				
				//if(listSNPs!=null && listSNPs.containsKey(rsid)){
					if(info.contains("IndelType=D")){
						System.out.println("linea ["+rsid+"] = "+line);
					}
					SNPElement	snpe = listSNPs.get(rsid);
					SNPsGroup	snpg = listSNPsGroups.get(rsid);
					
					processedMatchingLines++;
					variants = getVariants(variants,strandOrientation,snpg.getStrandOrientation()); //Obtain the correct code regarding orientation and alphabetical order.
					
					if(snpg.allowedVariants(variants[0], variants[1])){
						snpe.setVariants(variants[0], variants[1]);
					}else{
						linesThatDidNotMatchAllowedVariant++;
						processingReport+="<li>Warning: " + rsid + "(" + variants[0] + ";" + variants[1] + ") with orientation " + strandOrientation + " does not match any allowed genotype. Only genotypes listed in dbSNP are allowed. A possible reason for this could be that your data is not based on the same strand (+ or -) as dbSNP, and you did not choose the proper settings for strand orientation. This genotype will be reported as " + snpg.getVCFReference() + " in the resulting Medicine Safety Code.\n";
					}
					//System.out.println("snp="+snpe.getGeneticMarkerName()+"_"+snpe.getCriteriaSyntax());
				}
			}
			br.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		processingReport+=("</ul><p><b>Processed " + processedLines + " lines describing variants. Of these, " + processedMatchingLines + " lines matched allowed Medicine Safety Code RS numbers. Of these, " +   linesThatDidNotMatchAllowedVariant + " lines contained genotypes that did not match allowed Medicine Safety Code genotypes. </b></p>");
		return processingReport;
	}


	/**
	 * Get the list of gathered SNPs from the file in vcf format.
	 * 
	 * @return List of SNPs variant parsed from the vcf file.
	 * */
	public ArrayList<SNPElement> getListSNPElements() {
		ArrayList<SNPElement> listParsedSNPs = new ArrayList<SNPElement>();
		listParsedSNPs.addAll(listSNPs.values());
		/*for(SNPElement snpe: listParsedSNPs){
			String criteria = snpe.getCriteriaSyntax();
			if(criteria.contains("null")){
				SNPsGroup snpg = listSNPsGroups.get(snpe.getGeneticMarkerName());
				if(snpg.getListElements().size()>0){
					criteria = snpg.getListElements().get(1);
				}
			}
			String[] tokens = criteria.split(";");
			if(tokens!=null && tokens.length==2 && tokens[0].equals(tokens[1])){
				System.out.println("has exactly 2 "+snpe.getGeneticMarkerName()+"_"+tokens[0]+",");
			}else{
				System.out.println("(has some "+snpe.getGeneticMarkerName()+"_"+tokens[0]+") and (has some "+snpe.getGeneticMarkerName()+"_"+tokens[1]+"),");
			}
		}*/
		return listParsedSNPs;
	}
	
	/**
	 * It obtains the correct variant nucleotides when considering its SNP orientation from VCF file and the reference SNP orientation, and its alphabetical order.
	 * 
	 * @param snpCode			It contains the two nucleotide character directly from VCF file.
	 * @param orientation_file	It indicates the orientation of the 23AndMe file. Only "dbsnp-orientation" or "forward-orientation" are allowed.
	 * @param orientation_seq	It indicates the orientation of the reference SNP orientation. Only "reverse" or "forward" are allowed.
	 * @return		It returns the two dimensional array with the corresponding nucleotides with the correct orientation and alphabetical order.
	 * */
	private String[] getVariants(String[] variants,String orientation_file, String orientation_seq){
		
		if(variants.length!=2){
			System.out.println("Warning: getVariants("+variants+","+orientation_file+","+orientation_seq+")");
			return variants;
		}
				
		if ((orientation_file == Common.DBSNP_ORIENTATION) && (orientation_seq.equals("reverse"))) {
			for(int j=0;j<variants.length;j++){
				String val_1 = variants[j];
				if(val_1.length()>1){
					char [] variants_array = val_1.toCharArray();
					val_1="";
					for(int i=0; i<variants_array.length;i++){
						if(variants_array[i]=='A'){
							val_1 = "T"+val_1;
							continue;
						}
						if(variants_array[i]=='T'){
							val_1 = "A"+val_1;
							continue;
						}
						if(variants_array[i]=='C'){
							val_1 = "G"+val_1;
							continue;
						}
						if(variants_array[i]=='G'){
							val_1 = "C"+val_1;
							continue;
						}
					}
				}else{
					if(val_1.equals("A")) val_1 = "T";
					if(val_1.equals("T")) val_1 = "A";
					if(val_1.equals("C")) val_1 = "G";
					if(val_1.equals("G")) val_1 = "C";
				}
				variants[j] = val_1;
			}
		}
		
		
		if(variants[0].length()>variants[1].length() || (variants[0].length()==variants[1].length() && (variants[0].compareTo(variants[1])>0)) ){
			String aux = variants[1];
			variants[1] = variants[0];
			variants[0] = aux;
		}
		
		return variants;
	}
}
