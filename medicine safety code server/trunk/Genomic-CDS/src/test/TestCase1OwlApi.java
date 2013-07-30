package test;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import safetycode.MedicineSafetyProfileOWLAPI;
import utils.Common;
import exception.BadFormedBase64NumberException;
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
		String fileName="test_patient_23andme.txt";
		
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		msp.read23AndMeFileStream(my23AndMeFileStream, Common.DBSNP_ORIENTATION);
		String base64Profile = msp.getBase64ProfileString();
		
		assertEquals("We check if the file "+fileName+" produces the correct code in dbsnp orientation","fWE1001a5160ny200g202ZWgtX30G00G3uOG1b0e1og9SOG882YQu8PImK06y1n0040027WaPG00aHruVZSCLa8W41GEle1W0A00W06sWI1GAC80FtmCm0000000000000000000000000000",base64Profile);
	}
	
	
	/**
	 * This method checks if the a 23AndMe file produces the correct code in forward orientation. 
	 * */
	@Test
	public void testRead23AndMeFileStreamInputStreamStringForward() {
		InputStream my23AndMeFileStream=null;
		String fileName="test_patient_23andme.txt";
		
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
				
		
		msp.read23AndMeFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION);
		String base64Profile = msp.getBase64ProfileString();
		
		assertEquals("We check if the file "+fileName+" produces the correct code in forward orientation","fWFJK3uySfF1z-K3GkZ66xX2tcLvGzqRDwbG1bH7hohhSVmAe2owOQvImK06zPoTq51ZY7WaPGOFaHruVZSDra9Y41GEle1W8AA2gW-sWI1GAC80Ftnim0000000000000000000000000000",base64Profile);
	}
		
	
	/**
	 * This method will throw an exception when parsing a wrong base64 patient profile.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * @throws BadFormedBase64NumberException 
	 * */
	@Test(expected=VariantDoesNotMatchAnAllowedVariantException.class)
	public void testReadBase64ProfileString() throws VariantDoesNotMatchAnAllowedVariantException, BadFormedBase64NumberException {
		String base64Profile="cCB3RLNS2vCXUhq5Gl3c2z12jrLzVjqND-AG3bH7jWhDSVG392k2SR_NWK0nzPwTq51Y27ZaVmMFKHvuUpTUrYFZYXICleZWeBgQYW--8V1GAC80Ftvio9N9piVo0I2yH0-3Y0vmFC3KG0000";
		msp.readBase64ProfileString(base64Profile);
	}
	
	
	/**
	 * This method will not throw any exception when parsing a well formed base 64 patient profile.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * @throws BadFormedBase64NumberException 
	 * */
	@Test
	public void testReadBase64ProfileString2() throws VariantDoesNotMatchAnAllowedVariantException, BadFormedBase64NumberException {
		
		msp=new MedicineSafetyProfileOWLAPI("MSC_classes.ttl");
		String base64Profile="fWFJK3uySfF1z-K3GkZ66xX2tcLvGzqRDwbG1bH7hohhSVmAe2owOQvImK06zPoTq51ZY7WaPGOFaHruVZSDra9Y41GEle1W8AA2gW-sWI1GAC80Ftnim0000000000000000000000000000";
		msp.readBase64ProfileString(base64Profile);
	}
	
	
	/**
	 * This method will obtain the drug recommendations from a well formed base 64 patient profile.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * @throws BadFormedBase64NumberException 
	 * */
	@Test
	public void testObtainDrugRecommendations() throws VariantDoesNotMatchAnAllowedVariantException, BadFormedBase64NumberException{
		msp = new MedicineSafetyProfileOWLAPI("MSC_classes.ttl");
		String base64Profile="fWFJK3uySfF1z-K3GkZ66xX2tcLvGzqRDwbG1bH7hohhSVmAe2owOQvImK06zPoTq51ZY7WaPGOFaHruVZSDra9Y41GEle1W8AA2gW-sWI1GAC80Ftnim0000000000000000000000000000";
		msp.readBase64ProfileString(base64Profile);
		
		HashMap<String,ArrayList<String>> drug_recommendations = msp.obtainDrugRecommendations();
				
		assertEquals("We check that the list of drug recommendations and raw data is not empty", false, drug_recommendations.isEmpty());
	}
	
	
	
	@AfterClass
	public static void testEnd(){
	
	}
	
}
