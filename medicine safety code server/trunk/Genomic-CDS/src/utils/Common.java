package utils;

import java.io.File;
import java.math.BigInteger;

import safetycode.FileParserFactory;


/**
 * This class provides the common variables to all classes in the application.
 * The content must be maintained in every change, i.e. when VERSION or ROOT_URL of the server changes.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 * */
public class Common {
	
	/** Digits used to represent a base 64 number for a safetycode number.*/
	public final static char[] BASE_DIGITS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','-','_'};
	
	/** The current version of the safetycode system.*/
	public final static String VERSION = "v0.2";
	
	/** The code that represents an important recommendation rule internally.*/
	public final static int HAS_IMPORTANT_RECOMMENDATION = 1;
	
	/** The code that represents a standard recommendation rule internally.*/
	public final static int HAS_STANDARD_RECOMMENDATION = 2;
		
	/** Name spaces for querying the ontology content with SPARQL.*/
	public final static String SPARQL_NAME_SPACES = "" 
			+ "PREFIX sc: <http://www.genomic-cds.org/ont/MSC_classes.owl#>\n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX pgx: <http://www.genomic-cds.org/ont/genomic-cds.owl#>\n"
			+ "PREFIX ssr: <http://purl.org/zen/ssr.ttl#>\n";
	
	/** The strand orientation of a SNP. It is related to the reverse orientation.*/
	public final static String DBSNP_ORIENTATION = "dbsnp-orientation";
	
	/** The strand orientation of a SNP. It is related to the forward orientation.*/
	public final static String FORWARD_ORIENTATION = "forward-orientation";
	
	/** The URL root that is used to compound the web server URL.*/
	//public final static String ROOT_URL = "http://localhost:8080/Genomic-CDS";//URL for testing on local server
	public final static String ROOT_URL = "http://safety-code.org";
	
	/** The name of the cache that is needed to identify the cache in file where it is serialized.*/
	public final static String CACHE_NAME="safetycodecache1";
	
	/** The name of the cache file where the cached web pages are stored.*/
	public final static String CACHE_FILE="ehcache.xml";
	
	/** The name of the image that is used to frame the QR code.*/
	public final static String QR_FRAME="images/safety-code-frame-2014.png";
	
	/** The name of the file with the information about SNP groups.*/
	public final static String tabSeparatedSNPGroups = "ontinfo/snpGroups.txt";
	
	/** The name of the file with the information about haplotype groups.*/
	public final static String tabSeparatedAlleleGroups = "ontinfo/alleleGroups.txt";
	
	/** The name of the file with the information about phenotype rules.*/
	public final static String tabSeparatedPhenotypeRules = "ontinfo/phenotypeRules.txt";
	
	/** The name of the file with the information about cds rules.*/
	public final static String tabSeparatedCDSRules = "ontinfo/drugRecommendations.txt";
	
	/** The name of the file with the information about allele rules.*/
	public final static String tabSeparatedAlleleRules = "ontinfo/alleleRules.txt";
	
	/**
	 * This method should process the file to identify the type of file format.
	 * 
	 * @param fileInput		File that contains a patient's genotype with a certain file format.
	 * @return	The type of file format.
	 * */
	public static int getTypeFileFormat(File fileInput){
		return FileParserFactory.FORMAT_23ANDME_FILE;
	}
	
	/** Function used to code/decode a safetycode number. It calculates the combination of some elements in a particular group size.
	 * @param element		The number of elements to be combined.
	 * @param group_size	The number of elements allowed in each group.
	 * @return	The number of combinations.
	 */
	public static int get_kCombinations(int elements, int group_size){
		int val = 0;
		if(elements<=1) return 1;
		try{
			BigInteger numerador	= BigInteger.valueOf(elements+group_size-1);
			BigInteger denominador1	= BigInteger.valueOf(group_size);
			BigInteger denominador2	= BigInteger.valueOf(elements-1);
			val = (factorial(numerador).divide(factorial(denominador1).multiply(factorial(denominador2)))).intValue();
		}catch(Exception e){
			e.printStackTrace();
		}
		return val;
	}
	
	/**
	 * Function that calculates the factorial of a number using BigInteger class.
	 * @param n		The base number to calculate it factorial number.
	 * @return		The resulting factorial number.
	 * */
	public static BigInteger factorial(BigInteger n) {
	    BigInteger result = BigInteger.ONE;
	    
	    while (!n.equals(BigInteger.ZERO)) {
	        result = result.multiply(n);
	        n = n.subtract(BigInteger.ONE);
	    }

	    return result;
	}
}
