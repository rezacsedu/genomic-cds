package meduniwien.msc.util;

import java.math.BigInteger;

/**
 * This class centralizes variable values used through different classes.
 * 
 * @author Jose Antonio Miñarro Giménez
 * */
public class Common {
	public final static String localPath = "./";
	
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
	//public final static String ROOT_URL = "http://safety-code.org/Genomic-CDS";
	//public final static String ROOT_URL = "http://owl.msi.meduniwien.ac.at:8080/Genomic-CDS";
	//public final static String ROOT_URL = "http://localhost:8080/Genomic-CDS";//URL for testing in my local server
	public final static String ROOT_URL = "http://safety-code.org";
	
	/** The name of the ontology file.*/
	public final static String ONT_NAME=localPath+"MSC_textual_rules.owl";
	
	/** The name of the cache that is needed to identify the cache in file where it is serialized.*/
	public final static String CACHE_NAME="safetycodecache1";
	
	/** The name of the cache file where the cached web pages are stored.*/
	public final static String CACHE_FILE="ehcache.xml";
		
	/** The name of the file with the information about SNP groups.*/
	public final static String tabSeparatedSNPGroups = localPath+"ontinfo/snpGroups.txt";
	
	/** The name of the file with the information about haplotype groups.*/
	public final static String tabSeparatedAlleleGroups = localPath+"ontinfo/alleleGroups.txt";
	
	/** The name of the file with the information about phenotype rules.*/
	public final static String tabSeparatedPhenotypeRules = localPath+"ontinfo/phenotypeRules.txt";
	
	/** The name of the file with the information about cds rules.*/
	public final static String tabSeparatedCDSRules = localPath+"ontinfo/drugRecommendations.txt";
			
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
