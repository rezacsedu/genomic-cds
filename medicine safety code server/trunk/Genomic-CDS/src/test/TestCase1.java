package test;

import static org.junit.Assert.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import safetycode.MedicineSafetyProfile;
import utils.Common;

public class TestCase1 {

	private static MedicineSafetyProfile msp;
	
	@BeforeClass
	public static void testInit(){
		msp = new MedicineSafetyProfile();
	}
	
	@Test
	public void testGetListRsids(){
		ArrayList<String[]> listRsids = msp.getListRsids();
		assertEquals("We check the size of the list is equal to 385",385,listRsids.size());
		String[] firstValue = listRsids.get(0);
		String[] lastValue = listRsids.get(listRsids.size()-1);
		assertEquals("We check if the first genetic marker of the list is equals to rs1208", "rs1208", firstValue[0]);
		assertEquals("We check if the last genetic marker of the list is equals to rs147545709", "rs147545709", lastValue[0]);
	}	
	
	@Test
	public void testRead23AndMeFileStreamInputStreamString() {
		InputStream my23AndMeFileStream=null;
		String fileName="D:/workspace/Genomic-CDS/1097.23andme.564";
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		msp.read23AndMeFileStream(my23AndMeFileStream, Common.DBSNP_ORIENTATION);
		String base64Profile = msp.getBase64ProfileString();
		assertEquals("We check if the file "+fileName+" produces the correct code in dbsnp orientation","p650W03Y1SY0fKA00N101IWXMm0WE0080_580oWW0mL4kC804XJ1ECFhmA0O-0yW020013noFu00A8yyFPkkQn7mnGf6Nq1mG500G03V4FWe56407xy6P4havsFv091U8WV1n0Gu661g80000",base64Profile);
		msp.closeModel(null);
		
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		msp.read23AndMeFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION);
		base64Profile = msp.getBase64ProfileString();		
		assertEquals("We check if the file "+fileName+" produces the correct code in forward orientation","p65Zdghc1Ckf3L12eLXH3MWLMvVhgpkEZ-ee3oeLUmLckDe3aXV1U7VhmA0O_y_pk3WJ13noFu92g8yyFPklAn7JnGf6NqnmS5V7JmtV4FWe56407xyMP4havsFv091U8WV1n0Ku6c1g80000",base64Profile);
		msp.closeModel(null);
	}
	
	
	/*@AfterClass
	public static void testEnd(){
		String fileName="D:/workspace/Genomic-CDS/model_1097.23andme.564.owl";
		msp.writeModel(fileName);
		System.out.println("End");
	}*/
}
