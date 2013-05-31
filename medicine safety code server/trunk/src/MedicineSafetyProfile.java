
import java.io.*;
import java.math.BigInteger;
import java.nio.*;
import java.nio.charset.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.net.URLEncoder;

import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

/*
 * Example URL: http://safety-tag.org/v0/gYHLLLLKL4LAAgHLKLLL5LL4LKgggggggg8gbKL5LLLLL5LLL5L55LL5AgYggfLAggfLL5LKLKLAgggAggggebLLLL555LLLKLLLLLLLKggfLAagggfKgKgKf9Ab4f9KIgLLLLKggggggggbL
 * Example Base64: gYHLLLLKL4LAAgHLKLLL5LL4LKgggggggg8gbKL5LLLLL5LLL5L55LL5AgYggfLAggfLL5LKLKLAgggAggggebLLLL555LLLKLLLLLLLKggfLAagggfKgKgKf9Ab4f9KIgLLLLKggggggggbL
 * Example Base2: 101010100010010001010101010101010101010101010100010101000100010101001010001010101010010001010101010100010101010101010101000101010101010101000100010101010100101010101010101010101010101010101010101010101010001000101010100101010100010101000101010101010101010101010101010101000101010101010101010101000101010101000101000101010101010101000101001010101010100010101010101010101001010101001010101010101010101001010101010101000101010101010100010101010100010101001010101010101010101010001010101010101010101010101010101000100101010101010101010101010101000101000101000101010101010101010101010100010101010101010101010101010101010101010101010100101010101010101001010101001010100100101010101010101010101001010100101010010100101010010100101001001001001010100101000100101001001001010100010010101010010101010101010101010101010100101010101010101010101010101010101010101010101010100101010101
 */

public class MedicineSafetyProfile {
	public final static String DBSNP_ORIENTATION = "dbsnp-orientation";
	public final static String FORWARD_ORIENTATION = "forward-orientation";
	private final static Integer LENGTH_OF_BASE_0_PROFILE_STRING = 870;
	private final static String BASE_DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_";
	private final static String ROOT_URL = "http://safety-code.org/v0.2/";
	private String base64ProfileString;
	private String base2ProfileString;
	private Model codeModel;
	private RDFReader reader;

	public MedicineSafetyProfile() {
		super();
		codeModel = ModelFactory.createDefaultModel();
		reader = codeModel.getReader("N3");
		
		// initialize codeModel with core pharmacogenomic dataset
		initializeCodeModel();
	}
	
	public MedicineSafetyProfile(Model passedCodeModel) {		
		codeModel = ModelFactory.createDefaultModel();
		reader = codeModel.getReader("N3");
		
		// initialize codeModel from passedCodeModel (mainly used for performance gains when used in repeated web service calls)
		codeModel.add(passedCodeModel);
	}
	
	private static String fromDecimalToOtherBase(int base,
			BigInteger decimalNumber) {
		// TODO needs to be fixed?
		BigInteger bBase = new BigInteger(Integer.toString(base));
		BigInteger mod = new BigInteger("0");
		BigInteger zero = new BigInteger("0");
		BigInteger one = new BigInteger("1");
	
		String tempVal = (decimalNumber.compareTo(zero) == 0) ? "0" : "";
		while (decimalNumber.compareTo(zero) != 0) {
			mod = decimalNumber.remainder(bBase);
			tempVal = BASE_DIGITS.substring(Integer.parseInt(mod.toString()),
					Integer.parseInt((mod.add(one)).toString())) + tempVal;
			decimalNumber = decimalNumber.divide(bBase);
		}
		return tempVal;
	}

	private static BigInteger fromOtherBaseToDecimal(int base, String number) {
		int iterator = number.length();
		BigInteger returnValue = new BigInteger("0");
		BigInteger multiplier = new BigInteger("1");
		BigInteger bBase = new BigInteger(Integer.toString(base));
	
		while (iterator > 0) {
			returnValue = returnValue.add(new BigInteger(
								Integer.toString(BASE_DIGITS.indexOf(number.substring(iterator - 1, iterator)))).multiply(multiplier));
			multiplier = multiplier.multiply(bBase);
			--iterator;
		}
		return returnValue;
	}

	private void initializeCodeModel () {
		codeModel.removeAll();
		InputStream s = this.getClass().getResourceAsStream("MSC_classes.ttl");
		reader.read(codeModel, s,
				"http://www.genomic-cds.org/ont/genomic-cds.owl");
	}
	
	/**
	 * Update base2 and base64 strings from RDF model
	 */
	private void updateStringRepresentationsFromCodeModel() {
		String queryString = Common.SPARQL_NAME_SPACES
				+ " SELECT DISTINCT ?rank ?bit_code WHERE {?item sc:rank ?rank . " 
				+ "					       	sc:this_patient a ?variant . " 
				+ "							?variant rdfs:subClassOf ?item . "		
				+ "							?variant sc:bit_code ?bit_code . } " 
				+ " ORDER BY ?rank ";

		QueryExecution qexec = QueryExecutionFactory.create(
				QueryFactory.create(queryString), codeModel);

		try {
			ResultSet results = qexec.execSelect();
			//ResultSetFormatter.out(System.out, results);
			
			base2ProfileString = "1"; // "1" is prepended to all base2 strings (because leading zeros would be removed in conversion steps etc.) 
			
			int i = 0;
			
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				Literal bit_code = soln.getLiteral("bit_code");
				base2ProfileString += bit_code.getLexicalForm();
				System.out.println(i);
				i = i + 1;
			}
			
			BigInteger temporaryCodeInteger = fromOtherBaseToDecimal(2, base2ProfileString);
			base64ProfileString = fromDecimalToOtherBase(64, temporaryCodeInteger); 
			
		} finally {
			qexec.close();
		}
	}
	
	public void generateQRCode(String fileName) throws Exception {
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
	}

	public void readBase64ProfileString(String base64ProfileString) {
		// Generate temporary base2 representation
		BigInteger temporaryCodeInteger = fromOtherBaseToDecimal(64, base64ProfileString);
		String temporaryBase2ProfileString = fromDecimalToOtherBase(2, temporaryCodeInteger); 
		
		// Remove trailing "1" that is prepended to the base 2 string for uniform length
		temporaryBase2ProfileString = temporaryBase2ProfileString.substring(1); 
		
		// Create a list of expected chunks in a base2 profile string
		String queryString1 = Common.SPARQL_NAME_SPACES +
				" SELECT DISTINCT ?item ?rank ?position_in_base2_string ?bit_length WHERE {" +
				" ?item sc:rank ?rank ; " +
				"   sc:bit_length ?bit_length ; " +
				"   sc:position_in_base_2_string ?position_in_base2_string . " +
				" } " + 
				" ORDER BY ?rank ";
		
		QueryExecution qexec1 = QueryExecutionFactory.create(
				QueryFactory.create(queryString1), codeModel);
		
		try {
			ResultSet results1 = qexec1.execSelect();
			//ResultSetFormatter.out(System.out, results1); // TODO: Comment out
			
			String itemURI;
			Integer position_in_base2_string;
			Integer bit_length;
			String base2Chunk;
			QueryExecution qexec2;
			
			// Iterate through list of expected chunks and extract the values for the actual chunks in the base2 profile string
			for (; results1.hasNext();) {
				QuerySolution soln1 = results1.nextSolution();
				itemURI = soln1.getResource("item").getURI();
				position_in_base2_string = Integer.valueOf(soln1.getLiteral("position_in_base2_string").getLexicalForm());
				bit_length = Integer.valueOf(soln1.getLiteral("bit_length").getLexicalForm());
				base2Chunk = temporaryBase2ProfileString.substring(position_in_base2_string, position_in_base2_string + bit_length);
				
				// Construct RDF triples
				// TODO: Maybe change this to an INSERT query (perhaps this would improve performance)
				String queryString2 = Common.SPARQL_NAME_SPACES +
						"CONSTRUCT { sc:this_patient a ?variant . } WHERE { " +
						"  ?variant rdfs:subClassOf <" + itemURI + "> . "	+
						"  ?variant sc:bit_code '" + base2Chunk + "' . } ";
				qexec2 = QueryExecutionFactory.create(QueryFactory.create(queryString2), codeModel);		
				try {
					codeModel.add(qexec2.execConstruct());		
				} finally {
					qexec2.close();
				}
			}
			
		} finally {
			qexec1.close();
		}
			
		updateStringRepresentationsFromCodeModel();		
	}
	
	private Vector<String> getAcceptedRsids() {
		// Create vector of all rsids we want to accept (for filtering)
		String queryString;
		QueryExecution qexec;
		ResultSet results;
		Vector<String> acceptedRsids = new Vector<String>();

		queryString = Common.SPARQL_NAME_SPACES 
				+ "SELECT * \n"
				+ "WHERE { \n"
				+ "	 ?genotype pgx:rsid ?rsid . } ";
		qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), codeModel);
		
		try {
			results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				acceptedRsids.add(soln.getLiteral("rsid").getString());
			}
		} finally {
			qexec.close();
		}
		return acceptedRsids;
	}	
	
	private String getOrientationOnReferenceGenome(String rsid) throws Exception {
		// get the orientation of a SNP
		String queryString;
		QueryExecution qexec;
		ResultSet results;
		String result = "";

		queryString = Common.SPARQL_NAME_SPACES 
				+ " SELECT ?orientation_on_reference_genome "
				+ " WHERE { "
				+ "	 ?genotype pgx:rsid '" + rsid + "' ; "
				+ "      pgx:dbsnp_orientation_on_reference_genome ?orientation_on_reference_genome . } "
				+ " LIMIT 1 ";
		qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), codeModel);
		
		try {
			results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				result = soln.getLiteral("orientation_on_reference_genome").getString();
			}
		} finally {
			qexec.close();
		}
		if (result == "") throw new Exception("No data on orientation of SNP " + rsid + " found.");
		return result;
	}	
	
	public String read23AndMeFileStream(InputStream my23AndMeFileStream) throws IOException {
		// default to dbSNP orientation when strand orientation of input data is not given
		return read23AndMeFileStream(my23AndMeFileStream, DBSNP_ORIENTATION);
	}
	
	public String read23AndMeFileStream(InputStream my23AndMeFileStream, String strandOrientationOfInputData) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(my23AndMeFileStream));
		String line = null;
		String[] lineArray;
		String rsid;
		String my23AndMeSNPCode;
		StringBuffer processingReport = new StringBuffer("");
		initializeCodeModel();
		
		int processedLines = 0;
		int processedMatchingLines = 0;
		int linesThatDidNotMatchAllowedVariant = 0;
		
		Vector<String> acceptedRsids = getAcceptedRsids();
		
		// Iterate through lines in 23andMe file
		while ((line = reader.readLine()) != null) {
			
			// Skip comments
			if (line.matches(".*#.*")) continue;
			// Skip empty lines
			if (line.trim().length() == 0) continue;
			
			processedLines++;
			
			// Parse line
			lineArray = line.replace(" ", "").replace("\n","").split("\t");
			rsid = lineArray[0].trim();
			my23AndMeSNPCode = lineArray[3].trim();
			
			// Skip when rsid is not in Vector of accepted rsids, remove from Vector if it was matched
			if (acceptedRsids.contains(rsid) == false) continue;
			acceptedRsids.remove(rsid);
			
			processedMatchingLines++;
			
			// Skip when code does not have length of 2 (e.g., "C" instead of "CC")
			if (my23AndMeSNPCode.length() != 2) continue;
			
			// Generate criteria syntax representation for the SNP described in this line
			// criteriaSyntax = rsid + "(" + my23AndMeSNPCode.charAt(0) + ";" + my23AndMeSNPCode.charAt(1) + ")";
			
			// Generate triples representing matching variants
			try {
				addVariantToCodeModel(rsid, my23AndMeSNPCode.substring(0,1), my23AndMeSNPCode.substring(1,2), strandOrientationOfInputData);
			} 
			catch (VariantDoesNotMatchAnAllowedVariantException e) {
				linesThatDidNotMatchAllowedVariant++;
				processingReport.append("<p>" + e.getMessage() + "</p>\n");
			}
			catch (Exception e) {
				processingReport.append("<p>" + e.getMessage() + "</p>\n");
			}			
		}
		processingReport.append("<p><b>Processed " + processedLines + " lines describing variants. Of these, " + processedMatchingLines + " lines matched allowed Medicine Safety Code RS numbers. Of these, " + linesThatDidNotMatchAllowedVariant + " lines contained genotypes that did not match allowed Medicine Safety Code genotypes. </b></p>");
		
		// generate "NULL" genotypes for genotypes that are not yet described in codeModel
		repairCodeModel();
		
		updateStringRepresentationsFromCodeModel();	
		
		return processingReport.toString();
	}
	
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
	
	private void addVariantToCodeModel(String rsid, String variant1, String variant2, String strandOrientationOfInputData ) throws Exception {
		variant1 = variant1.toUpperCase();
		variant2 = variant2.toUpperCase();
		
		// flip strand orientation if parser is set to normalize to forward orientation, and a specific SNP in dbSNP happens to be in reverse orientation
		if ((strandOrientationOfInputData == DBSNP_ORIENTATION) 
				&& (getOrientationOnReferenceGenome(rsid).equals("reverse"))) {
			variant1 = flipOrientationOfStringRepresentation(variant1);
			variant2 = flipOrientationOfStringRepresentation(variant2);
		}
		
		// Sort alphabetically to match convention used by criteria syntax
		String[] haplotypeArray = {variant1, variant2};
		Arrays.sort(haplotypeArray);
		variant1 = haplotypeArray[0];
		variant2 = haplotypeArray[1];
		
		String criteriaSyntax = rsid + "(" + variant1 + ";" + variant2 + ")";
		String queryString = Common.SPARQL_NAME_SPACES 
				+ " CONSTRUCT { "
				+ "	 sc:this_patient a ?variant } "
				+ " WHERE { "
				+ "	 ?variant sc:criteria_syntax '" + criteriaSyntax + "' . "   
				+ "	 ?variant rdfs:subClassOf / pgx:rsid  '" + rsid + "' . "
				+ " } ";
		QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), codeModel);
		
		try {
			Model resultingTriples = qexec.execConstruct();
			if (resultingTriples.size() == 0) { throw new VariantDoesNotMatchAnAllowedVariantException(
					"Warning: " + criteriaSyntax + " does not match any allowed genotype. Only genotypes listed in dbSNP are allowed. A possible reason for this could be that your data is not based on the same strand (+ or -) as dbSNP, and you did not choose the proper settings for strand orientation. This genotype will be reported as 'NULL;NULL' in the resulting Medicine Safety Code."); 
			}
			if (resultingTriples.size() > 1) { throw new Exception(
					"Warning: " + criteriaSyntax + " matched more than one allowed genotype. Please contact the administrator and report this problem."); 
			}
			codeModel.add(resultingTriples);
		}
		catch (Exception e) {  
			throw e; 
		}
		finally {
			qexec.close();
		}
	}

	private String flipOrientationOfStringRepresentation(String inputString) {
		StringBuffer outputBuffer = new StringBuffer("");
		
		for (int i = 0; i < inputString.length(); i++){
		    char c = inputString.charAt(i);        
			switch (c) {
				case 'A': outputBuffer.append('T'); break;
				case 'T': outputBuffer.append('A'); break;
				case 'C': outputBuffer.append('G'); break;
				case 'G': outputBuffer.append('C'); break;
				default: outputBuffer.append(c);
			}
		}
		
		// reverse sequence (only relevant for variants with one than more 'letter')
		// TODO: Check if this works as expected
		outputBuffer = outputBuffer.reverse();
		return outputBuffer.toString();
	}

	/*
	private void addVariantBasedOnCriteriaSyntax(String criteriaSyntax) throws Exception {
		// TODO: Maybe change this to an INSERT query (perhaps this would improve performance)
		String queryString = Common.SPARQL_NAME_SPACES 
				+ " CONSTRUCT { "
				+ "	 sc:this_patient sc:has_variant ?variant } "
				+ " WHERE { "
				+ "	 ?variant genomic:criteria_syntax \"" + criteriaSyntax + "\" . } ";
		QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), codeModel);
		
		try {
			Model resultingTriples = qexec.execConstruct();
			if (resultingTriples.size() == 0) { throw new VariantDoesNotMatchAnAllowedVariantException(
					"Error: " + criteriaSyntax + " does not match any allowed genotype. Only genotypes listed in dbSNP are allowed. A possible reason for this could be that your data is not based on the same strand (+ or -) as dbSNP. This genotype will be reported as 'NULL;NULL' in the resulting Medicine Safety Code."); 
			}
			if (resultingTriples.size() > 1) { throw new Exception(
					"Error: " + criteriaSyntax + " matched more than one allowed genotype. Please contact the administrator and report this problem."); 
			}
			codeModel.add(resultingTriples);
		}
		catch (Exception e) {  
			throw e; 
		}
		finally {
			qexec.close();
		}
	}
	*/

	private void repairCodeModel() {
	
		String queryString;
		QueryExecution qexec;
		
		// Change undefined SNPs into explicit 'NULL' SNPs. 
		queryString = Common.SPARQL_NAME_SPACES 
				+ " CONSTRUCT { "
				+ "	 sc:this_patient a ?variant .} "
				+ " WHERE { "
				+ "  ?variant rdfs:subClassOf ?item . " 
				+ "  ?variant sc:decimal_code 0 . "	//TODO: Maybe change this filter to something more semantically explicit?
				+ "  FILTER NOT EXISTS { sc:this_patient a ?absent_variant . "
				+ "              ?absent_variant rdfs:subClassOf ?item . } "
				+ " }";
		qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), codeModel);
		try {
			codeModel.add(qexec.execConstruct());
		} finally {
			qexec.close();
		}
		
	}

	public Model getRDFModel () {
		return codeModel;
	}

	public String getBase64ProfileString() throws Exception {
		// TODO check if this also throws an error if string is not set at all
		if (base64ProfileString.isEmpty()) throw (new Exception("Profile is empty"));
		return base64ProfileString;
	}
	
	public String getProfileURL() throws Exception {
		// TODO check if this also throws an error if string is not set at all
		return ROOT_URL + getBase64ProfileString();
	}

	/*
	 * public static void
	 */

	public static void main(String[] args) throws Exception {
		
		/*
		MedicineSafetyProfile myProfile = new MedicineSafetyProfile();
		myProfile.readBase64ProfileString("gYHLLLLKL4LAAgHLKLLL5LL4LKgggggggg8gbKL5LLLLL5LLL5L55LL5AgYggfLAggfLL5LKLKLAgggAggggebLLLL555LLLKLLLLLLLKggfLAagggfKgKgKf9Ab4f9KIgLLLLKggggggggbL");
		//myProfile.read23AndMeFileStream(new FileInputStream("C:\\Users\\m\\Documents\\Doc\\Gesundheit\\Matthias SNPs\\genome_M_Sa_Full_20120217024430.txt"));		
		
		// myProfile.readCAMDAFileStream(new FileInputStream("C:\\Users\\m\\Documents\\Doc\\Publikationen, Konferenzen, Meetings\\CAMDA Project\\BarCode_SNPs_ind.csv"));
		
		// myProfile.getQRCode();
		
		//myProfile.getRDFModel().write(System.out, "N3");
		
		String queryString = Common.SPARQL_NAME_SPACES
				+ "SELECT DISTINCT ?rank ?criteria_syntax WHERE {?item sc:rank ?rank . "
				+ "					       	sc:this_patient a ?variant . "
				+ "							?variant rdfs:subClassOf ?item . "
				+ "							?variant sc:criteria_syntax ?criteria_syntax . } "
				+ "ORDER BY ?rank ";

		QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), myProfile.getRDFModel());
		try {
			ResultSet results = qexec.execSelect();
			ResultSetFormatter.out(System.out, results);
		} finally {
			qexec.close();
		}
		
		*/

		
	}
}

class VariantDoesNotMatchAnAllowedVariantException extends Exception {
	public VariantDoesNotMatchAnAllowedVariantException(String string) {
		super(string);
	}
}
