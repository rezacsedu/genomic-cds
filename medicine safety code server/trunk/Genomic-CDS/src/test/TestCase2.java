package test;

import static org.junit.Assert.*;

import org.junit.Test;

import utils.Common;

public class TestCase2 {

	@Test
	public void testConvertFrom2To64() {
		assertEquals("We check if the binary number 010001100110 is correctly transformed into a base 64 number","Hc",Common.convertFrom2To64("010001100110"));
	}

	@Test
	public void testConvertFrom64To2() {
		assertEquals("We check if the base 64 number Matthias is correctly transformed into a binary number", "010110100100110111110111101011101100100100110110", Common.convertFrom64To2("Matthias"));
	}
}
