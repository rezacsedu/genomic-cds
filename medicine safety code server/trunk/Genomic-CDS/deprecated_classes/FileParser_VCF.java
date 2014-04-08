package safetycode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

//import exception.BadFormedBinaryNumberException;

import utils.Common;

public class FileParser_VCF {

	private String myVCFBase64ProfileString;
	private ArrayList<SNPelement_old> listRsids;
	private HashMap<String,String> criteriaSyntax2bitcode;
	
	
	public FileParser_VCF(ArrayList<SNPelement_old> listRsids, HashMap<String, String> criteriaSyntax2bitcode) {
		this.listRsids = listRsids;
		this.criteriaSyntax2bitcode = criteriaSyntax2bitcode;
		myVCFBase64ProfileString	= "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
	}



	public String parse(InputStream fileStream, String strandOrientation) {
		int processedLines 						= 0;	//Number of processed lines of strands from the input 23AndMe file.
		int processedMatchingLines				= 0;	//Number of processedLines that corresponds to markers in the model.
		int linesThatDidNotMatchAllowedVariant	= 0;	//Number of processedMatchingLines that could not be matched in the model.
		String processingReport					= "<ul>\n";	//Report that contains the missing matched criteria syntax and general statistics of the parser.
		
		
		try{
			String line		= "";	//Line of the parsed file.
			BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
			
			int id_col_pos		= -1;//Position of the column with SNPs id.
			int ref_col_pos		= -1;//Position of the column with the reference nucleotide.
			int alt_col_pos		= -1;//Position of the column with the alternative nucleotide.
			int qual_col_pos	= -1;//Position of the column with quality value.
			int format_col_pos	= -1;//Position of the column with the format elements and their positions.
			
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
					}
					continue;
 				}
				
				processedLines++;
				
				if(id_col_pos < 0 || ref_col_pos < 0 || alt_col_pos < 0 || qual_col_pos < 0 || format_col_pos < 0) continue;
				
				String rsid		= "";	//strand id
				String snpCode	= "";	//char code that is related to snp nucleotides.
				
				String[] tokens = line.split("\t");
				if(tokens.length < format_col_pos+2) continue;
					
				rsid			= tokens[id_col_pos];
				//String qual		= tokens[qual_col_pos];//Do something with quality value
				String format	= tokens[format_col_pos];
				String values	= tokens[format_col_pos+1];
				int gt_pos		= -1;
				int gq_pos		= -1;
				String ref		= tokens[ref_col_pos];
				String alt		= tokens[alt_col_pos];
				String base_nucleotides = ref+alt;
				String encoded_genotype	= "";
				//String quality_genotype	= "";
					
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
				encoded_genotype = values_items[gt_pos];
				//quality_genotype = values_items[gq_pos];//Do something with quality value
				String[] indexes = encoded_genotype.split("[/\\|]");
				for(int i = 0; i < indexes.length; i++){
					Integer index = Integer.parseInt(indexes[i]);
					snpCode += base_nucleotides.substring(index,index+1);
				}
				
				if(listRsids!=null){
					for(int i = 0; i < listRsids.size(); i++){
						SNPelement_old genotype = listRsids.get(i);
						if(genotype.getRsid().equals(rsid)){
							processedMatchingLines++;
							
							String[] variants = getVariants(snpCode,strandOrientation,genotype.getOrientation()); //Obtain the correct code regarding orientation and alphabetical order.
							String criteriaSyntax=rsid+"("+variants[0]+";"+variants[1]+")";//Generate the new criteria syntax for this strand
							if(criteriaSyntax2bitcode!=null){
								String bit_code="";
								if(criteriaSyntax2bitcode.containsKey(criteriaSyntax)){
									bit_code=criteriaSyntax2bitcode.get(criteriaSyntax);//Get the corresponding bit code to the criteriaSyntax syntax obtained
									genotype.setBit_code(bit_code);
									genotype.setCriteriaSyntax(criteriaSyntax);
								}else{
									genotype.setCriteriaSyntax(rsid+"(null;null)");
									genotype.setBit_code(criteriaSyntax2bitcode.get(genotype.getCriteriaSyntax()));
									linesThatDidNotMatchAllowedVariant++;
									processingReport+="<li>Warning: " + criteriaSyntax + " does not match any allowed genotype. Only genotypes listed in dbSNP are allowed. A possible reason for this could be that your data is not based on the same strand (+ or -) as dbSNP, and you did not choose the proper settings for strand orientation. This genotype will be reported as 'NULL;NULL' in the resulting Medicine Safety Code.\n";
								}								
							}
							break;
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		processingReport+=("</ul><p><b>Processed " + processedLines + " lines describing variants. Of these, " + processedMatchingLines + " lines matched allowed Medicine Safety Code RS numbers. Of these, " +   linesThatDidNotMatchAllowedVariant + " lines contained genotypes that did not match allowed Medicine Safety Code genotypes. </b></p>");
		
		/*try{
			String base2ProfileString	= "";
			for(int i=0;i<listRsids.size();i++){
				base2ProfileString+=listRsids.get(i).getCriteriaSyntax();			
			}
			myVCFBase64ProfileString = Common.convertFrom2To64(base2ProfileString);
		}catch(BadFormedBinaryNumberException e){
			System.err.println("ERROR: "+e.getMessage());
		}*/
		
		return processingReport;
	}

	public String getBase64ProfileString() {
		return myVCFBase64ProfileString;
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
		variant[0]="-";
		variant[1]="-";
		
		if(snpCode.length()<=1){
			System.out.println("Warning: getVariants("+snpCode+","+orientation_file+","+orientation_seq+")");
			return variant;
		}
		
		String val_1 = snpCode.substring(0,1);
		String val_2 = snpCode.substring(1);
		
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
				
		return variant;
	}
	
	
	

	public String parse(FileReader fileStream, String strandOrientation) {
		int processedLines 						= 0;	//Number of processed lines of strands from the input 23AndMe file.
		int processedMatchingLines				= 0;	//Number of processedLines that corresponds to markers in the model.
		int linesThatDidNotMatchAllowedVariant	= 0;	//Number of processedMatchingLines that could not be matched in the model.
		String processingReport					= "<ul>\n";	//Report that contains the missing matched criteria syntax and general statistics of the parser.
				
		try{
			String line		= "";	//Line of the parsed file.
			BufferedReader br = new BufferedReader(fileStream);
			
			int id_col_pos		= -1;//Position of the column with SNPs id.
			int ref_col_pos		= -1;//Position of the column with the reference nucleotide.
			int alt_col_pos		= -1;//Position of the column with the alternative nucleotide.
			int qual_col_pos	= -1;//Position of the column with quality value.
			int format_col_pos	= -1;//Position of the column with the format elements and their positions.
			
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
					}
					continue;
 				}
				
				processedLines++;
				
				if(id_col_pos < 0 || ref_col_pos < 0 || alt_col_pos < 0 || qual_col_pos < 0 || format_col_pos < 0) continue;
				
				String rsid		= "";	//strand id
				String snpCode	= "";	//char code that is related to snp nucleotides.
				
				String[] tokens = line.split("\t");
				if(tokens.length < format_col_pos+2) continue;
					
				rsid			= tokens[id_col_pos];
				//String qual		= tokens[qual_col_pos];//Do something with quality value
				String format	= tokens[format_col_pos];
				String values	= tokens[format_col_pos+1];
				int gt_pos		= -1;
				int gq_pos		= -1;
				String ref		= tokens[ref_col_pos];
				String alt		= tokens[alt_col_pos];
				String base_nucleotides = ref+alt;
				String encoded_genotype	= "";
				//String quality_genotype	= "";
					
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
				encoded_genotype = values_items[gt_pos];
				//quality_genotype = values_items[gq_pos];//Do something with quality value
				String[] indexes = encoded_genotype.split("[/\\|]");
				for(int i = 0; i < indexes.length; i++){
					Integer index = Integer.parseInt(indexes[i]);
					snpCode += base_nucleotides.substring(index,index+1);
				}
				
				if(listRsids!=null){
					for(int i = 0; i < listRsids.size(); i++){
						SNPelement_old genotype = listRsids.get(i);
						if(genotype.getRsid().equals(rsid)){
							processedMatchingLines++;
							String[] variants = getVariants(snpCode,strandOrientation,genotype.getOrientation()); //Obtain the correct code regarding orientation and alphabetical order.
							String criteriaSyntax=rsid+"("+variants[0]+";"+variants[1]+")";//Generate the new criteria syntax for this strand
							System.out.println("Matched "+rsid+"\t"+snpCode+" <==> "+criteriaSyntax);
							if(criteriaSyntax2bitcode!=null){
								String bit_code="";
								if(criteriaSyntax2bitcode.containsKey(criteriaSyntax)){
									bit_code=criteriaSyntax2bitcode.get(criteriaSyntax);//Get the corresponding bit code to the criteriaSyntax syntax obtained
									genotype.setBit_code(bit_code);
									genotype.setCriteriaSyntax(criteriaSyntax);
								}else{
									genotype.setCriteriaSyntax(rsid+"(null;null)");
									genotype.setBit_code(criteriaSyntax2bitcode.get(genotype.getCriteriaSyntax()));
									linesThatDidNotMatchAllowedVariant++;
									processingReport+="<li>Warning: " + criteriaSyntax + " does not match any allowed genotype. Only genotypes listed in dbSNP are allowed. A possible reason for this could be that your data is not based on the same strand (+ or -) as dbSNP, and you did not choose the proper settings for strand orientation. This genotype will be reported as 'NULL;NULL' in the resulting Medicine Safety Code.\n";
								}								
							}
							break;
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		processingReport+=("</ul><p><b>Processed " + processedLines + " lines describing variants. Of these, " + processedMatchingLines + " lines matched allowed Medicine Safety Code RS numbers. Of these, " +   linesThatDidNotMatchAllowedVariant + " lines contained genotypes that did not match allowed Medicine Safety Code genotypes. </b></p>");
		
		/*try{
			String base2ProfileString	= "";
			for(int i=0;i<listRsids.size();i++){
				base2ProfileString+=listRsids.get(i).getBit_code();			
			}
			myVCFBase64ProfileString = Common.convertFrom2To64(base2ProfileString);
		}catch(BadFormedBinaryNumberException e){
			System.err.println("ERROR: "+e.getMessage());
		}*/
		
		return processingReport;
	}
	
	

}
