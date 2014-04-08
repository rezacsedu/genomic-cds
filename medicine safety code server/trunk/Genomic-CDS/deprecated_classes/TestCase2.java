package test;

import static org.junit.Assert.*;

import org.junit.Test;

import exception.BadFormedBase64NumberException;
import exception.BadFormedBinaryNumberException;
import utils.Common;


/**
 * This JUnit test case check the results from the Common class which implements the methods to transform binary to base 64 digits and viceversa.
 * It evaluates:
 * 	- The transformation of a base 2 number into a base 64 number.
 *  - The exception when a bad base 2 number is provided for transformation into a base 64 number.
 *  - The transformation of a base 64 number into a binary number.
 *  - The exception when a bad base 64 number is provided for transformation into a base 2 number.
 * */
public class TestCase2 {

	/**
	 * This method tests if the translation from binary to base 64 number is correctly implemented.
	 * @throws BadFormedBinaryNumberException 
	 * */
	/*@Test
	public void testConvertFrom2To64() throws BadFormedBinaryNumberException {
		assertEquals("We check if the binary number 010001100110 is correctly transformed into a base 64 number","Hc",Common.convertFrom2To64("010001100110"));
	}*/

	
	/**
	 * This test will throw an exception when trying to transform a bad formed "binary number" into a base 64 number. 
	 * @throws BadFormedBinaryNumberException
	 * */
	/*@Test(expected=BadFormedBinaryNumberException.class)
	public void testConvertFrom2To64Exception() throws BadFormedBinaryNumberException {
		Common.convertFrom2To64("010201100a10");
	}*/
	
	
	/**
	 * This method tests if the translation from base 64 to binary number is correctly implemented. 
	 * @throws BadFormedBase64NumberException 
	 * */
	/*@Test
	public void testConvertFrom64To2() throws BadFormedBase64NumberException {
		assertEquals("We check if the base 64 number Matthias is correctly transformed into a binary number", "010110100100110111110111101011101100100100110110", Common.convertFrom64To2("Matthias"));
	}*/
	
	
	/**
	 * This test will throw an exception when trying to transform a bad formed "base 64 number" into a binary number. 
	 * @throws BadFormedBase64NumberException 
	 * */
	/*@Test(expected=BadFormedBase64NumberException.class)
	public void testConvertFrom64To2Exception() throws BadFormedBase64NumberException {
		Common.convertFrom64To2("Matthias?");
	}*/
}
