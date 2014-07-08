package com.example.myfirstapp.util;

import java.math.BigInteger;

public class Common {
	public final static char[] BASE_DIGITS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','-','_'};
	public final static String VERSION = "v0.2";
	public final static int HAS_IMPORTANT_RECOMMENDATION = 1;
	public final static int HAS_STANDARD_RECOMMENDATION = 2;
	/**
	 * This method should process the file to identify the type of file format.
	 * 
	 * @param fileInput		File that contains a patient's genotype with a certain file format.
	 * @return	The type of file format.
	 * */
	
	
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
	
	public static BigInteger factorial(BigInteger n) {
	    BigInteger result = BigInteger.ONE;
	    
	    while (!n.equals(BigInteger.ZERO)) {
	        result = result.multiply(n);
	        n = n.subtract(BigInteger.ONE);
	    }

	    return result;
	}
}
