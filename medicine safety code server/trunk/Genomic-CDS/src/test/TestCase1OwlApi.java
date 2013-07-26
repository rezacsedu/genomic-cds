package test;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import safetycode.MedicineSafetyProfileOWLAPI;
import utils.Common;
import exception.VariantDoesNotMatchAnAllowedVariantException;

public class TestCase1OwlApi {

	private static MedicineSafetyProfileOWLAPI msp;
	
	@BeforeClass
	public static void testInit(){
		msp = new MedicineSafetyProfileOWLAPI();
	}
	
	/**
	 * This method tests if the list of markers are correctly gathered from the ontology model.
	 * */
	@Test
	public void testGetListRsids(){
		ArrayList<String[]> listRsids = msp.getListRsids();
		String[] firstValue = listRsids.get(0);
		String[] lastValue = listRsids.get(listRsids.size()-1);
		assertEquals("We check the size of the list is equal to 385",385,listRsids.size());
		assertEquals("We check if the first genetic marker of the list is equals to rs1208", "rs1208", firstValue[0]);
		assertEquals("We check if the last genetic marker of the list is equals to rs147545709", "rs147545709", lastValue[0]);
	}	
	
	
	/**
	 * This method checks if the a 23AndMe file produces the correct code in dbsnp orientation. 
	 * */
	@Test
	public void testRead23AndMeFileStreamInputStreamStringDbsnp() {
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
		assertEquals("We check if the file "+fileName+" produces the correct code in dbsnp orientation","cCA100742P422e200g206b0gjW30K00G3zHG1b0e1Wg9SOG092c2y8VNWK0ny1v0040027ZaVm00KHvuUpTSLYEXYXICle3WWA00W06-8V1GAC80FtuCo9N9piVo0I2yH0-3Y0XmCC3KG0000",base64Profile);
	}
	
	
	/**
	 * This method checks if the a 23AndMe file produces the correct code in forward orientation. 
	 * */
	@Test
	public void testRead23AndMeFileStreamInputStreamStringForward() {
		InputStream my23AndMeFileStream=null;
		String fileName="D:/workspace/Genomic-CDS/1097.23andme.564";
		
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
				
		
		msp.read23AndMeFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION);
		String base64Profile = msp.getBase64ProfileString();
		assertEquals("We check if the file "+fileName+" produces the correct code in forward orientation","cCB3RLNS2vCXUgK5Gl3c2z12jrLzVjqND-AG3bH7jWhDSVG392k2SR_NWK0nzPwTq51Y27ZaVmMFKHvuUpTUrYFZYXICleZWeBgQYW--8V1GAC80Ftvio9N9piVo0I2yH0-3Y0vmFC3KG0000",base64Profile);
	}
		
	
	/**
	 * This method will throw an exception when parsing a wrong base64 patient profile.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * */
	@Test(expected=VariantDoesNotMatchAnAllowedVariantException.class)
	public void testReadBase64ProfileString() throws VariantDoesNotMatchAnAllowedVariantException {
		String base64Profile="cCB3RLNS2vCXUhq5Gl3c2z12jrLzVjqND-AG3bH7jWhDSVG392k2SR_NWK0nzPwTq51Y27ZaVmMFKHvuUpTUrYFZYXICleZWeBgQYW--8V1GAC80Ftvio9N9piVo0I2yH0-3Y0vmFC3KG0000";
		msp.readBase64ProfileString(base64Profile);
	}
	
	
	/**
	 * This method will not throw any exception when parsing a well formed base 64 patient profile.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * */
	@Test
	public void testReadBase64ProfileString2() throws VariantDoesNotMatchAnAllowedVariantException {
		
		msp=new MedicineSafetyProfileOWLAPI();
		String base64Profile="cCB3RLNS2vCXUgK5Gl3c2z12jrLzVjqND-AG3bH7jWhDSVG392k2SR_NWK0nzPwTq51Y27ZaVmMFKHvuUpTUrYFZYXICleZWeBgQYW--8V1GAC80Ftvio9N9piVo0I2yH0-3Y0vmFC3KG0000";
		msp.readBase64ProfileString(base64Profile);
	}
	
	
	/**
	 * This method will obtain the drug recommendations from a well formed base 64 patient profile.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * */
	@Test
	public void testObtainDrugRecommendations() throws VariantDoesNotMatchAnAllowedVariantException{
		msp = new MedicineSafetyProfileOWLAPI();
		String base64Profile="cCB3RLNS2vCXUgK5Gl3c2z12jrLzVjqND-AG3bH7jWhDSVG392k2SR_NWK0nzPwTq51Y27ZaVmMFKHvuUpTUrYFZYXICleZWeBgQYW--8V1GAC80Ftvio9N9piVo0I2yH0-3Y0vmFC3KG0000";
		msp.readBase64ProfileString(base64Profile);
		try {
			msp.calculateInferences();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		HashMap<String,HashSet<String>> drug_recommendations = msp.obtainDrugRecommendations();
		Iterator<String> it_keys = drug_recommendations.keySet().iterator();
		while(it_keys.hasNext()){
			String key = it_keys.next();
			System.out.println(key+":");
			Iterator<String> it_messages = drug_recommendations.get(key).iterator();
			while(it_messages.hasNext()){
				System.out.println("\t"+it_messages.next());
			}
		}
	}
	
	
	
	@AfterClass
	public static void testEnd(){
	
	}
	
}
