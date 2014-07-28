package meduniwien.msc.util;

import java.math.BigInteger;

public class Common {
	/** Digits used to represent a base 64 number for a safetycode number.*/
	public final static char[] BASE_DIGITS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','-','_'};
	/** The current version of the safetycode system.*/
	public final static String VERSION = "v0.2";
	/** The code that represents an important recommendation rule internally.*/
	public final static int HAS_IMPORTANT_RECOMMENDATION = 1;
	/** The code that represents a standard recommendation rule internally.*/
	public final static int HAS_STANDARD_RECOMMENDATION = 2;
		
	
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
