package test;



import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import safetycode.DrugRecommendation;
import safetycode.MedicineSafetyProfile_v2;
import safetycode.SNPElement;
import utils.OntologyManagement;
import exception.BadFormedBase64NumberException;
import exception.NotInitializedPatientsGenomicDataException;
import exception.VariantDoesNotMatchAnAllowedVariantException;

/**
 * This test class evaluate:
 *	- The list of SNP from 23andme.
 *	- The list of NSP from VCF.
 * 	- Test decode base64profile number.
 * 	- Test throw an exception when a wrong base64code is provided.
 * 	- The drug recommendations algorithm.
 * */

public class TestCase1 {
	
	/**
	 * This method tests if the list of 23andme base SNPs markers are correctly gathered from the ontology model.
	 * */
	@Test
	public void testGetListRsids(){
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		OntologyManagement om = OntologyManagement.getOntologyManagement(path+"resources/MSC_classes_new_v2.owl");
		ArrayList<SNPElement> listRsids = om.getList23andMeRsids();
		
		SNPElement firstValue = listRsids.get(0);
		SNPElement lastValue = listRsids.get(listRsids.size()-1);
		
		assertEquals("We check the size of the list is equal to 385",385,listRsids.size());
		assertEquals("We check if the first genetic marker of the list is equals to rs1208", "rs1208", firstValue.getGeneticMarkerName());
		assertEquals("We check if the last genetic marker of the list is equals to rs147545709", "rs147545709", lastValue.getGeneticMarkerName());
	}	
	
	/**
	 * This method tests if the list of vcf reference markers are correctly gathered from the ontology model.
	 * */
	@Test
	public void testGetVCFRefListRsids(){
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		OntologyManagement om = OntologyManagement.getOntologyManagement(path+"resources/MSC_classes_new_v2.owl");
		ArrayList<SNPElement> listRsids = om.getVCFRefListRsids();
		
		SNPElement firstValue = listRsids.get(0);
		SNPElement lastValue = listRsids.get(listRsids.size()-1);
		
		assertEquals("We check the size of the list is equal to 385",385,listRsids.size());
		assertEquals("We check if the first genetic marker of the list is equals to rs1208", "rs1208", firstValue.getGeneticMarkerName());
		assertEquals("We check if the last genetic marker of the list is equals to rs147545709", "rs147545709", lastValue.getGeneticMarkerName());
	}	
	
	/**
	 * This method will throw an exception when parsing a wrong base64 patient profile.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * @throws BadFormedBase64NumberException 
	 * @throws NotInitializedPatientsGenomicDataException 
	 * */
	@Test(expected=VariantDoesNotMatchAnAllowedVariantException.class)
	public void testReadBase64ProfileString() throws VariantDoesNotMatchAnAllowedVariantException, BadFormedBase64NumberException, NotInitializedPatientsGenomicDataException {
		String base64Profile="2B5RchTThGHBIUFuPw8hNxXB_mC";
		
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		MedicineSafetyProfile_v2 msp = new MedicineSafetyProfile_v2(path+"resources/MSC_classes_new_v2.owl");
		msp.readBase64ProfileString(base64Profile);
	}
	
	
	/**
	 * This method will not throw any exception when parsing a well formed base 64 patient profile.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * @throws BadFormedBase64NumberException 
	 * @throws NotInitializedPatientsGenomicDataException 
	 * */
	@Test
	public void testReadBase64ProfileString2() throws VariantDoesNotMatchAnAllowedVariantException, BadFormedBase64NumberException, NotInitializedPatientsGenomicDataException {
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		MedicineSafetyProfile_v2 msp = new MedicineSafetyProfile_v2(path+"resources/MSC_classes_new_v2.owl");
		
		String base64Profile="2B5RchTThGHBIUFuPw8hNxXB_mC";
		msp.readBase64ProfileString(base64Profile);		
	}
	
	
	/**
	 * This method will obtain the drug recommendations from a well formed base 64 patient profile.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * @throws BadFormedBase64NumberException 
	 * @throws NotInitializedPatientsGenomicDataException 
	 * */
	@Test
	public void testObtainDrugRecommendations() throws VariantDoesNotMatchAnAllowedVariantException, BadFormedBase64NumberException, NotInitializedPatientsGenomicDataException{
		String base64Profile="2B5RchTThGHBIUFuPw8hNxXB_mC";
		
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		MedicineSafetyProfile_v2 msp = new MedicineSafetyProfile_v2(path+"resources/MSC_classes_new_v2.owl");
		
		msp.readBase64ProfileString(base64Profile);		
		HashMap<String,ArrayList<DrugRecommendation>> drug_recommendations = msp.obtainDrugRecommendations();
		
		assertEquals("We check that the list of drug recommendations and raw data is not empty", false, drug_recommendations.isEmpty());
	}
}
