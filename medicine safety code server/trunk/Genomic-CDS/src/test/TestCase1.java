package test;



import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import safetycode.FileParserFactory;
import safetycode.MedicineSafetyProfileOptimized;
import utils.Common;
import utils.OntologyManagement;
import exception.BadFormedBase64NumberException;
import exception.NotInitializedPatientsGenomicDataException;
import exception.NotPatientGenomicFileParsedException;
import exception.VariantDoesNotMatchAnAllowedVariantException;

public class TestCase1 {

	/**
	 * This method tests if the list of 23andme base markers are correctly gathered from the ontology model.
	 * */
	@Test
	public void testGetListRsids(){
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		OntologyManagement om = OntologyManagement.getOntologyManagement(path+"resources/MSC_classes_1.owl");
		ArrayList<String[]> listRsids = om.getListRsids();
		String[] firstValue = listRsids.get(0);
		String[] lastValue = listRsids.get(listRsids.size()-1);
		
		assertEquals("We check the size of the list is equal to 385",385,listRsids.size());
		assertEquals("We check if the first genetic marker of the list is equals to rs1208", "rs1208", firstValue[0]);
		assertEquals("We check if the last genetic marker of the list is equals to rs147545709", "rs147545709", lastValue[0]);
	}	
	
	/**
	 * This method tests if the list of vcf reference markers are correctly gathered from the ontology model.
	 * */
	@Test
	public void testGetVCFRefListRsids(){
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		OntologyManagement om = OntologyManagement.getOntologyManagement(path+"resources/MSC_classes_1.owl");
		ArrayList<String[]> listRsids = om.getVCFRefListRsids();
		String[] firstValue = listRsids.get(0);
		String[] lastValue = listRsids.get(listRsids.size()-1);
		
		assertEquals("We check the size of the list is equal to 385",385,listRsids.size());
		assertEquals("We check if the first genetic marker of the list is equals to rs1208", "rs1208", firstValue[0]);
		assertEquals("We check if the last genetic marker of the list is equals to rs147545709", "rs147545709", lastValue[0]);
	}	
	
	
	
	/**
	 * This method checks if the a 23AndMe file produces the correct code in dbsnp orientation. 
	 * @throws NotPatientGenomicFileParsedException 
	 * */
	@Test
	public void testRead23AndMeFileStreamInputStreamStringDbsnp() throws NotPatientGenomicFileParsedException {
		InputStream my23AndMeFileStream=null;
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		String fileName=path+"resources/patient_demo.23andme.txt";
		
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		
		MedicineSafetyProfileOptimized msp = new MedicineSafetyProfileOptimized(path+"resources/MSC_classes_1.owl");
		msp.parseFileStream(my23AndMeFileStream, Common.DBSNP_ORIENTATION, FileParserFactory.FORMAT_23ANDME_FILE);
		String base64Profile = msp.getBase64ProfileString();
		
		assertEquals("We check if the file "+fileName+" produces the correct code in dbsnp orientation","QX0G01H2903vY02e80AE0hY20K00G2y40PW0628bw402QY927xWI0dw1uW04002UACs002ZlF3QPxYv3eOuQtj0S4500G03U26ma2Z203z-3CqhavsFv0a5uY1q770Gu6Vdg80000",base64Profile);
	}
	
	/**
	 * This method checks if the a vcf file produces the correct code. 
	 * @throws NotPatientGenomicFileParsedException 
	 * */
	@Test
	public void testReadVCFFileStreamInputStreamStringDbsnp() throws NotPatientGenomicFileParsedException {
		InputStream my23AndMeFileStream=null;
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		String fileName=path+"resources/user35_file210_yearofbirth_unknown_sex_XY.23andme-exome-vcf.txt";
		
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		
		MedicineSafetyProfileOptimized msp = new MedicineSafetyProfileOptimized(path+"resources/MSC_classes_1.owl");
		msp.parseFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION, FileParserFactory.FORMAT_VCF_FILE);
		String base64Profile = msp.getBase64ProfileString();
		
		assertEquals("We check if the file "+fileName+" produces the correct code in dbsnp orientation","cbCizLlQhhUqqyzSkqBqgrrLzVzsVTwdb_kqtDCxwzgoogjE_xbIkjyPwTtrPxx-xiT3pyhghBUphinp-wgwtjrVbLrTLHVUBdyaAxJ7pz-RDqhavsFvSabuYf_NNTC___dgkw-nV",base64Profile);
	}
	
	
	/**
	 * This method checks if the a 23AndMe file produces the correct code in forward orientation. 
	 * @throws NotPatientGenomicFileParsedException 
	 * */
	@Test
	public void testRead23AndMeFileStreamInputStreamStringForward() throws NotPatientGenomicFileParsedException {
		InputStream my23AndMeFileStream=null;
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		String fileName=path+"resources/patient_demo.23andme.txt";
		
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}

		MedicineSafetyProfileOptimized msp = new MedicineSafetyProfileOptimized(path+"resources/MSC_classes_1.owl");
		msp.parseFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION,FileParserFactory.FORMAT_23ANDME_FILE);
		String base64Profile = msp.getBase64ProfileString();
		
		assertEquals("We check if the file "+fileName+" produces the correct code in forward orientation","QXGqnLF2h8xuqzIyCGJE2hzPzVzqND_q0vtKk2Erxy0gQgD6lxWI0dzPwTq41w2UACs2nwZlF3QPxkv3uuuQtj4S55rDHGVU26ma2Z203z-RCqhavsFv0a5uY1q770Su7_dg80000",base64Profile);
	}
		
	
	/**
	 * This method will throw an exception when parsing a wrong base64 patient profile.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * @throws BadFormedBase64NumberException 
	 * @throws NotInitializedPatientsGenomicDataException 
	 * */
	@Test(expected=VariantDoesNotMatchAnAllowedVariantException.class)
	public void testReadBase64ProfileString() throws VariantDoesNotMatchAnAllowedVariantException, BadFormedBase64NumberException, NotInitializedPatientsGenomicDataException {
		String base64Profile="cCB3RLNS2vCXUhq5Gl3c2z12jrLzVjqND-AG3bH7jWhDSVG392k2SR_NWK0nzPwTq51Y27ZaVmMFKHvuUpTUrYFZYXICleZWeBgQYW--8V1GAC80Ftvio9N9piVo0I2yH0-3Y0vmFC3KG0000";
		
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		MedicineSafetyProfileOptimized msp = new MedicineSafetyProfileOptimized(path+"resources/MSC_classes_1.owl");
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
		MedicineSafetyProfileOptimized msp = new MedicineSafetyProfileOptimized(path+"resources/MSC_classes_1.owl");
		
		String base64Profile="QXGqnLF2h8xuqzIyCGJE2hzPzVzqND_q0vtKk2Erxy0gQgD6lxWI0dzPwTq41w2UACs2nwZlF3QPxkv3uuuQtj4S55rDHGVU26ma2Z203z-RCqhavsFv0a5uY1q770Su7_dg80000";
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
		String base64Profile="QXGqnLF2h8xuqzIyCGJE2hzPzVzqND_q0vtKk2Erxy0gQgD6lxWI0dzPwTq41w2UACs2nwZlF3QPxkv3uuuQtj4S55rDHGVU26ma2Z203z-RCqhavsFv0a5uY1q770Su7_dg80000";
		
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		MedicineSafetyProfileOptimized msp = new MedicineSafetyProfileOptimized(path+"resources/MSC_classes_1.owl");
		
		msp.readBase64ProfileString(base64Profile);		
		HashMap<String,ArrayList<String>> drug_recommendations = msp.obtainDrugRecommendations();
		
		assertEquals("We check that the list of drug recommendations and raw data is not empty", false, drug_recommendations.isEmpty());
	}
}
