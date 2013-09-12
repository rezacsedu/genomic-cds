package safetycode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Common;
import exception.BadFormedBinaryNumberException;


public class FileParser_23andme implements FileParser {
	
	private String my23AndMeBase64ProfileString;
	private ArrayList<String[]> listRsids;
	private HashMap<String,String> criteriaSyntax2bitcode;
	
	/**
	 * Constructor of the class that initializes the list of rsids supported by the ontology and the corresponding criteria syntax and bitcode of their variants.
	 * @param listRsids					Sorted list of strand markers from the ontology model.
	 * @param criteriaSyntax2bitcode	Map of each criteria syntax with its corresponding bit code in the ontology.
	 * */
	public FileParser_23andme(ArrayList<String[]> listRsids,HashMap<String,String> criteriaSyntax2bitcode){
		this.listRsids					= listRsids;
		this.criteriaSyntax2bitcode		= criteriaSyntax2bitcode;
		//The base64profile is initialized with the bit_codes of null variants.
		my23AndMeBase64ProfileString	= "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
	}
	
	/**
	 * It parses the 23AndMe file with the corresponding strand orientation.
	 * 
	 * @param listRsids		Sorted list of strand markers from the ontology model.
	 * @param strandOrientationOfInputData	The orientation of the input strand to parse.
	 * @return	The processing report of the 23AndMe file.
	 * */
	@Override
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
				for(int i=0;i<listRsids.size();i++){
					String[] genotype = listRsids.get(i);
					if(genotype[0].equalsIgnoreCase(rsid)){//Check which is the related marker defined in the model for this strand
						processedMatchingLines++;
						String[] variants = getVariants(my23AndMeSNPCode,my23AndMeStrandOrientation,genotype[2]); //Obtain the correct code regarding orientation and alphabetical order.
						genotype[3]=rsid+"("+variants[0]+";"+variants[1]+")";//Generate the new criteria syntax for this strand
					}
				}
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		//End parsing 23AndMe file
		
		String base2ProfileString=""; // binary code of patient variants
				
		//Process all patient variations
		for(int i=0;i<listRsids.size();i++){
			String[] genotype = listRsids.get(i);
			String criteriaSyntax=genotype[3];//Criteria syntax that will be related to the patient
			String bit_code=null;
			if(criteriaSyntax2bitcode.containsKey(criteriaSyntax)){
				bit_code=criteriaSyntax2bitcode.get(criteriaSyntax);//Get the corresponding bit code to the critieria syntax obtained
				genotype[4]=bit_code;
			}else{
				bit_code=genotype[4];
				linesThatDidNotMatchAllowedVariant++;
				processingReport+="<li>Warning: " + criteriaSyntax + " does not match any allowed genotype. Only genotypes listed in dbSNP are allowed. A possible reason for this could be that your data is not based on the same strand (+ or -) as dbSNP, and you did not choose the proper settings for strand orientation. This genotype will be reported as 'NULL;NULL' in the resulting Medicine Safety Code.\n";
			}
				
			base2ProfileString+=bit_code;
		}
		processingReport+=("</ul><p><b>Processed " + processedLines + " lines describing variants. Of these, " + processedMatchingLines + " lines matched allowed Medicine Safety Code RS numbers. Of these, " +   linesThatDidNotMatchAllowedVariant + " lines contained genotypes that did not match allowed Medicine Safety Code genotypes. </b></p>");
		
		try{		
			my23AndMeBase64ProfileString = Common.convertFrom2To64(base2ProfileString);
		}catch(BadFormedBinaryNumberException e){
			System.err.println("ERROR: "+e.getMessage());
		}
		
		return processingReport;
	}

	
	/**
	 * Get method to obtain the resulting patient profile coded using a number in base 64.
	 * @return	The base64 number of the patient's profile.
	 * */
	@Override
	public String getBase64ProfileString() {
		return my23AndMeBase64ProfileString;
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
		try{
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
		}
		
		return variant;
	}
}


