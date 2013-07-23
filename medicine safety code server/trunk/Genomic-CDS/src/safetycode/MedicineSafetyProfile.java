package safetycode;
import java.io.*;
import java.util.ArrayList;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import utils.Common;
import exception.BadFormedBase64NumberException;
import exception.BadFormedBinaryNumberException;
import exception.VariantDoesNotMatchAnAllowedVariantException;

/**
 * This class represents the genomics dataset related to an user.
 * 
 * */
public class MedicineSafetyProfile {
	/**
	 * 64 base representation of the variant codes of the associated patient. 
	 * */
	private String base64ProfileString; 
	/**
	 * Ontology model that contains the patient conceptualization and pharmacogenomics semantic rules. 
	 * */
	private OntModel owlReader;

	
	/**
	 * Constructor of the class. It initializes the model of the pharmacogenomics dataset.
	 * */
	public MedicineSafetyProfile() {
		super();
		initializeModel();
	}
	
	
	/**
	 * Constructor of the class. It initializes the owl model with a given model.
	 * */
	public MedicineSafetyProfile(OntModel passedOntModel) {		
		owlReader=passedOntModel;
	}
	
	
	/**
	 * initialize model with core pharmacogenomic dataset.
	 * */
	private void initializeModel () {
		String ontologyFile="D:/workspace/Genomic-CDS/MSC_classes.ttl";
		String namespace="http://www.genomic-cds.org/ont/genomic-cds.owl";
		
		try{
			owlReader = ModelFactory.createOntologyModel();
			InputStream s =  FileManager.get().open(ontologyFile);
			if (s==null) {
			    throw new IllegalArgumentException(
			                                 "File: " + "D:/workspace/Genomic-CDS/MSC_classes.ttl" + " not found");
			}
			RDFReader reader = owlReader.getReader("N3");
			reader.read(owlReader, s,namespace);
						
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * It parsers the 23AndMe file with the default strand orientation (dbnsp_orientation).
	 * 
	 * @param my23AndMeFileStream	The 23AndMe file to parse.
	 * @return		The processing report of the 23AndMe file.
	 * */
	public String read23AndMeFileStream(InputStream my23AndMeFileStream){
		// default to dbSNP orientation when strand orientation of input data is not given
		return read23AndMeFileStream(my23AndMeFileStream, Common.DBSNP_ORIENTATION);
	}
	
	
	/**
	 * It parses the 23AndMe file with the corresponding strand orientation.
	 * 
	 * @param my23AndMeFileStream			The 23AndMe file to parse.
	 * @param strandOrientationOfInputData	The orientation of the input strand to parse.
	 * @return	The processing report of the 23AndMe file.
	 * */
	public String read23AndMeFileStream(InputStream my23AndMeFileStream, String strandOrientationOfInputData){
		
		int processedLines 						= 0;	//Number of processed lines of strands from the input 23AndMe file.
		int processedMatchingLines				= 0;	//Number of processedLines that corresponds to markers in the model.
		int linesThatDidNotMatchAllowedVariant	= 0;	//Number of processedMatchingLines that could not be matched in the model.
		String processingReport					= "<ul>\n";	//Report that contains the missing matched criteria syntax and general statistics of the parser.
		ArrayList<String[]> listRsids 			= getListRsids();	//Sorted list of strand markers.
		int numberOfRsids = listRsids.size();
		//Parsing the 23AndMe file
		try{
			String rsid 			= "";	//strand id
			String my23AndMeSNPCode = "";	//Two char code that is related to rsid.
			String line 			= "";	//Line of the parsed file.
			
			BufferedReader br = new BufferedReader(new InputStreamReader(my23AndMeFileStream));
			while((line=br.readLine())!=null){
				
				line=(line.replaceAll("#.*","")).trim();	//Avoid comments
				line=line.replaceAll(" ","");				//Avoid empty spaces (not \t or \n)
				if(line.isEmpty())	continue;
				
				processedLines++;
				
				String[] lineArray = line.split("\t");		//Obtain the columns of the strand
				if(lineArray.length>=4){
					rsid = lineArray[0].trim();				//Gather rsid of the strand
					my23AndMeSNPCode = lineArray[3].trim();	//Gather code of the strand
					if (my23AndMeSNPCode.length() != 2) continue; //Skip this line because of wrong code length
				}else continue; //Skip this line because of wrong number of columns associated to the strand
				
				//Add the code to the related marker
				for(int i=0;i<listRsids.size();i++){
					String[] genotype = listRsids.get(i);
					if(genotype[0].equalsIgnoreCase(rsid)){//Check which is the related marker defined in the model for this strand
						processedMatchingLines++;
						String[] variants = getVariants(my23AndMeSNPCode,strandOrientationOfInputData,genotype[2]); //Obtain the correct code regarding orientation and alphabetical order.
						genotype[3]=rsid+"("+variants[0]+";"+variants[1]+")";//Generate the new criteri syntax for this strand
					}
				}
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		//End parsing 23AndMe file

	
		String base2ProfileString=""; // binary code of patient variants
		OntClass humanClass = owlReader.getOntClass("http://www.genomic-cds.org/ont/genomic-cds.owl#human");		//Human class definition
		Individual patient = humanClass.createIndividual("http://www.genomic-cds.org/ont/genomic-cds.owl#patient");	//Individual that represents the patient conceptualization
		
		//Process all patient variations
		for(int i=0;i<listRsids.size();i++){
			String[] genotype = listRsids.get(i);
			String criteriaSyntax=genotype[3];//Criteria syntax that will be related to the patient
			String bit_code=null;
			try{
				bit_code=addVariantToPatient(patient,criteriaSyntax);//Create rdf:type link between patient individual and matched variant class with the criteria syntax
			}catch(VariantDoesNotMatchAnAllowedVariantException e){
				processingReport+="<li>"+e.getMessage();
			}catch(Exception e){
				processingReport+="<li>"+e.getMessage();
			}
			if(bit_code==null){//The criteria syntax did not match any not "null;null" variant
				bit_code=genotype[4];
				linesThatDidNotMatchAllowedVariant++;
				processingReport+="<li>Warning: " + criteriaSyntax + " does not match any allowed genotype. Only genotypes listed in dbSNP are allowed. A possible reason for this could be that your data is not based on the same strand (+ or -) as dbSNP, and you did not choose the proper settings for strand orientation. This genotype will be reported as 'NULL;NULL' in the resulting Medicine Safety Code.";
			}else{
				genotype[4]=bit_code;
			}
			base2ProfileString+=bit_code;
		}
		processingReport+=("</ul><p><b>Processed " + processedLines + " lines describing variants. Of these, " + processedMatchingLines + " lines matched allowed Medicine Safety Code RS numbers. Of these, " + (numberOfRsids - linesThatDidNotMatchAllowedVariant - processedMatchingLines) + " lines contained genotypes that did not match allowed Medicine Safety Code genotypes. </b></p>");
		try{		
			base64ProfileString = Common.convertFrom2To64(base2ProfileString);
		}catch(BadFormedBinaryNumberException e){
			System.err.println("ERROR: "+e.getMessage());
		}
		return processingReport;
	}
	
	
	/**
	 * This method obtains the sorted list of markers defined in the model with its associated annotations bit_lenth, orientation and the associated annotations of its null;null variant for criteria syntax and bit code.  
	 *
	 * @return Sorted list of the genetics markers that consists of an string array with element {rsid,bit_length,orientation,criteria_syntax,bit_code}
	 * */
	public ArrayList<String[]> getListRsids() {
		ArrayList<String[]> listRsids = new ArrayList<String[]>();
		
		// Each String array of the list contains:
		// [0] -> rsid
		// [1] -> bit_length
		// [2] -> orientation
		// [3] -> criteria_syntax
		// [4] -> bit_code
		
		String queryString = Common.SPARQL_NAME_SPACES
				+ " SELECT ?rsid ?bit_length ?orientation" 
				+ " WHERE { " 
				+ "     ?item pgx:rsid ?rsid . "
				+ "		?item sc:rank ?rank . "  
				+ "		?item sc:bit_length ?bit_length . "
				+ "     ?item pgx:dbsnp_orientation_on_reference_genome ?orientation ."
				+ " } ORDER BY ?rank ";
		
		QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), owlReader);
		try{
			ResultSet results = qexec.execSelect();//The markers will be obtained in order by rank of markers
			while (results.hasNext()) {
				QuerySolution soln	= results.nextSolution();
				String rsid			= soln.getLiteral("rsid").getString();
				int bit_length		= soln.getLiteral("bit_length").getInt();
				String orientation	= soln.getLiteral("orientation").getString();
				
				String criteria_syntax = rsid+"(null;null)"; //Generate the criteria syntax of "null;null" variant of this marker
				String bit_code = "";						 //Generate the bit code of "null;null" variant of this marker
				for(int i=0;i<bit_length;i++){
					bit_code+="0";
				}
				String[] genotype = {rsid,""+bit_length,orientation,criteria_syntax,bit_code}; //create the corresponding string array of this markers
				listRsids.add(genotype); //Insert the string array to the list of markers
			}
		}catch(Exception e){
			e.printStackTrace();
		}		
		return listRsids;
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
		String[] variant={snpCode.substring(0,1),snpCode.substring(1,2)};
		if ((orientation_file == Common.DBSNP_ORIENTATION) 
				&& (orientation_seq.equals("reverse"))) {
			for(int i=0;i<2;i++){
				if(variant[i].equalsIgnoreCase("A")){
					variant[i]= "T";
					continue;
				}
				if(variant[i].equalsIgnoreCase("T")){
					variant[i]= "A";
					continue;
				}
				if(variant[i].equalsIgnoreCase("C")){
					variant[i]= "G";
					continue;
				}
				if(variant[i].equalsIgnoreCase("G")){
					variant[i]= "C";
					continue;
				}
			}
		}
		if(((int)variant[0].toCharArray()[0]) > ((int)variant[1].toCharArray()[0])){
			String aux = variant[0];
			variant[0]=variant[1];
			variant[1]=aux;
		}
		return variant;
	}
	
	
	/**
	 * It associate the classes of variants that match the criteria syntax to the patient instance in the model.
	 * 
	 * @param patient			Individual that corresponds to the instance of the patient in the model.
	 * @param criteriaSyntax	It represents the criteria syntax of a variant in the model.
	 * @throws	A VariantDoesNotMatchAnAllowedVariantException when an error is detected in the model definition.
	 * @return	It returns the bit code associated to the user or null if the criteria syntax could not be matched in the model.
	 * */
	private String addVariantToPatient(Individual patient, String criteriaSyntax) throws VariantDoesNotMatchAnAllowedVariantException{
		String bit_code=null;
		
		String criteriaSyntax_def=criteriaSyntax.substring(0,criteriaSyntax.indexOf("("))+"(null;null)";
		String queryString = Common.SPARQL_NAME_SPACES 
				+ "	SELECT ?variant ?bit_code ?variant_def"
				+ "	WHERE { "
				+ "		{" 
				+ "			?variant sc:criteria_syntax \""+criteriaSyntax+"\" . " 
				+ "			?variant sc:bit_code ?bit_code . "
				+ "		} UNION { "
				+ "			?variant_def sc:criteria_syntax \""+criteriaSyntax_def+"\" . "
				+ "     } "
				+ "	} ";
		
		QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), owlReader);
		try {
			ResultSet results = qexec.execSelect();
			if(results.hasNext()){
				QuerySolution soln	= results.nextSolution();
				Resource resource	= soln.getResource("variant");
				if(resource!=null){
					patient.addOntClass(resource);
					bit_code=soln.getLiteral("bit_code").getString();
				}else{
					resource 		= soln.getResource("variant_def");
					if(resource==null) throw new VariantDoesNotMatchAnAllowedVariantException("<p>ERROR: "+criteriaSyntax_def+" was not found in the model</p>");
					patient.addOntClass(resource);				
				}
			}
		}catch (Exception e) {
			throw new VariantDoesNotMatchAnAllowedVariantException("<p>ERROR: Cannot query the model with "+criteriaSyntax_def+"</p>");
		}finally {
			qexec.close();
		}
		return bit_code;
	}
		
	
	/**
	 * It associate the classes of variants that match the criteria syntax to the patient instance in the model.
	 * 
	 * @param patient			Individual that corresponds to the instance of the patient in the model.
	 * @param criteriaSyntax	It represents the criteria syntax of a variant in the model.
	 * @throws	A VariantDoesNotMatchAnAllowedVariantException when an error is detected in the model definition.
	 * @return	It returns the bit code associated to the user or null if the criteria syntax could not be matched in the model.
	 * */
	private boolean addVariantToPatient(Individual patient, String rsid, String bit_code) throws VariantDoesNotMatchAnAllowedVariantException{
		boolean added=false;	
		String queryString = Common.SPARQL_NAME_SPACES 
				+ "	SELECT ?variant \n"
				+ "	WHERE { \n"
				+ " 	?marker pgx:rsid	\""+rsid+"\" . \n"
				+ "		?variant rdfs:subClassOf ?marker . \n" 
				+ "		?variant sc:bit_code \""+bit_code+"\" . \n"
				+ "	} ";
		
		QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), owlReader);
		try {
			ResultSet results = qexec.execSelect();
			if(results.hasNext()){
				QuerySolution soln	= results.nextSolution();
				Resource resource	= soln.getResource("variant");
				if(resource!=null){
					patient.addOntClass(resource);
					added=true;
				}
			}
		}catch (Exception e) {
			throw new VariantDoesNotMatchAnAllowedVariantException("<p>ERROR: Cannot query the model with bit_code "+bit_code+" for marker "+rsid+"</p>");
		}finally {
			qexec.close();
		}
		return added;
	}
	
	/*public void generateQRCode(String fileName) throws Exception {////No se llama nunca a este método porque está implementado en el servlet MSCImageGenerator
		Charset charset = Charset.forName("ISO-8859-1");
		CharsetEncoder encoder = charset.newEncoder();
		byte[] b = null;
		try {
			// Convert string of ProfileURL to ISO-8859-1 bytes in a ByteBuffer
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(this.getProfileURL()));
			b = bbuf.array();
		} catch (CharacterCodingException e) {
			System.out.println(e.getMessage());
		}

		String data;
		try {
			data = new String(b, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
			return;
		}

		// get a BitMatrix for the data
		BitMatrix matrix = null;
		int h = 200;
		int w = 200;
		com.google.zxing.Writer writer = new QRCodeWriter();
		try {
			matrix = writer.encode(data,
					com.google.zxing.BarcodeFormat.QR_CODE, w, h);
		} catch (com.google.zxing.WriterException e) {
			System.out.println(e.getMessage());
			return;
		}

		File file = new File(fileName);
		try {
			MatrixToImageWriter.writeToFile(matrix, "PNG", file);
			System.out.println("printing to " + file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}*/

	/*
	public void readCAMDAFileStream(InputStream my23AndMeFileStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(my23AndMeFileStream));
		String line = null;
		String criteriaSyntax;
		String[] lineArray;
		String rsid;
			
		initializeCodeModel();
		
		Vector<String> acceptedRsids = getAcceptedRsids();
		
		// Skip first line
		reader.readLine();
		
		// Iterate through lines in 23andMe file
		while ((line = reader.readLine()) != null) {

			// Parse line
			lineArray = line.split(",");
			rsid = lineArray[2];
			String[] haplotypeArray = lineArray[5].split("/");
		
			// Skip when rsid is not in Vector of accepted rsids, remove from Vector if it was matched
			if (acceptedRsids.contains(rsid) == false) continue;
			acceptedRsids.remove(rsid);
			
			// Skip when code does not have length of 2 (e.g., "C" instead of "CC")
			if (haplotypeArray.length != 2) continue;
			
			// Sort alphabetically to match convention used by criteria syntax
			Arrays.sort(haplotypeArray);
			
			// Generate criteria syntax representation for the SNP described in this line
			criteriaSyntax = rsid + "(" + haplotypeArray[0] + ";" + haplotypeArray[1] + ")";
			
			// Generate triples representing matching variants
			try {
				addVariantBasedOnCriteriaSyntax(criteriaSyntax);
			}
			catch (Exception e)
			{
				
			}
		}
		
		// generate "NULL" genotypes for genotypes that are not yet described in codeModel
		repairCodeModel();
		
		updateStringRepresentationsFromCodeModel();	
	}
	*/
	
	/**
	 * Get method to obtain the generated model.
	 * @return	The generated model of the patient.
	 * */
	public Model getRDFModel () {
		return owlReader;
	}

	/**
	 * Get method to obtain the 64 base representation of the patient variants of the markers.
	 * @return	The string associated to the variants of the patient or null if it is not yet defined. 
	 * */
	public String getBase64ProfileString() {
		if (base64ProfileString.isEmpty()) return null;
		return base64ProfileString;
	}
	
	/**
	 * Create the patient model that is related to the base64Profile.
	 * 
	 * @param base64Profile		Base 64 number that represent the binary codification of a patient genotype. 
	 * @throws VariantDoesNotMatchAnAllowedVariantException 
	 * */
	public void readBase64ProfileString(String base64Profile) throws VariantDoesNotMatchAnAllowedVariantException{
		base64ProfileString				= base64Profile;
		String binaryProfile			="";
		try{
			binaryProfile				= Common.convertFrom64To2(base64ProfileString);	
		}catch(BadFormedBase64NumberException e){
			System.err.println("ERROR: "+e.getMessage());
		}
		
		ArrayList<String[]> listRsids	= getListRsids();
		
		OntClass humanClass = owlReader.getOntClass("http://www.genomic-cds.org/ont/genomic-cds.owl#human");		//Human class definition
		Individual patient = humanClass.createIndividual("http://www.genomic-cds.org/ont/genomic-cds.owl#patient");	//Individual that represents the patient conceptualization
		
		for(int position = 0, i = 0;i<listRsids.size();i++){
			String[] genotype = listRsids.get(i);
			int bit_length = Integer.parseInt(genotype[1]);
			if(binaryProfile.length()<position+bit_length) {
				throw new VariantDoesNotMatchAnAllowedVariantException("<p>Warning: the length of the patient profile is shorter than the defined in the model</p>");
			}
			String bit_code = binaryProfile.substring(position,position+bit_length);
			genotype[4]=bit_code;
			position+=bit_length;
			if(!addVariantToPatient(patient, genotype[0],genotype[4])){
				throw new VariantDoesNotMatchAnAllowedVariantException("<p>Warning: the genotype mark \""+genotype[0]+"\" or its corresponding code variant were not found in the model</p>");
			}
		}
	}
	
	/**
	 * Close the model and write it into a file if the parameter is not null.
	 * @param fileOut	The file that will contain the model of the patient or null.
	 * */
	public void closeModel(String fileOut){
		try{
			if(fileOut!=null){
				writeModel(fileOut);
			}
			owlReader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Write the model into a file.
	 * @param fileOut	The file that will contain the model of the patient.
	 * */
	public void writeModel(String fileOut){
		BufferedWriter bw=null;
		try {
			bw = new BufferedWriter(new FileWriter(fileOut));
			owlReader.write(bw,"RDF/XML");
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(bw!=null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
