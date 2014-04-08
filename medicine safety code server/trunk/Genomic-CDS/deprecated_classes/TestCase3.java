package test;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;

import safetycode.FileParserFactory;
import safetycode.MedicineSafetyProfile_v2;
import utils.Common;
import exception.BadFormedBinaryNumberException;
import exception.NotPatientGenomicFileParsedException;
import exception.VariantDoesNotMatchAnAllowedVariantException;


/**
 * This test case checks the methods implemented in FileParser_23andMe class and FileParser_VCF
 * 
 * */
public class TestCase3 {
	
	/**
	 * This method checks if the a vcf file produces the correct code. 
	 * @throws NotPatientGenomicFileParsedException 
	 * @throws VariantDoesNotMatchAnAllowedVariantException 
	 * @throws BadFormedBinaryNumberException 
	 * */
	@Test
	public void testReadVCFFileStreamInputStreamStringDbsnp() throws NotPatientGenomicFileParsedException, VariantDoesNotMatchAnAllowedVariantException, BadFormedBinaryNumberException {
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
		
		MedicineSafetyProfile_v2 msp = new MedicineSafetyProfile_v2(path+"resources/MSC_classes_new_v2.owl");
		msp.parseFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION, FileParserFactory.FORMAT_VCF_FILE);
		String base64Profile = msp.getBase64ProfileString();
		
		assertEquals("We check if the file "+fileName+" produces the correct code in dbsnp orientation","cbCizLlQhhUqqyzSkqBqgrrLzVzsVTwdb_kqtDCxwzgoogjE_xbIkjyPwTtrPxx-xiT3pyhghBUphinp-wgwtjrVbLrTLHVUBdyaAxJ7pz-RDqhavsFvSabuYf_NNTC___dgkw-nV",base64Profile);
	}
	
	
	/**
	 * This method checks if the a 23AndMe file produces the correct code in forward orientation. 
	 * @throws NotPatientGenomicFileParsedException 
	 * @throws VariantDoesNotMatchAnAllowedVariantException 
	 * @throws BadFormedBinaryNumberException 
	 * */
	@Test
	public void testRead23AndMeFileStreamInputStreamStringForward() throws NotPatientGenomicFileParsedException, VariantDoesNotMatchAnAllowedVariantException, BadFormedBinaryNumberException {
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

		MedicineSafetyProfile_v2 msp = new MedicineSafetyProfile_v2(path+"resources/MSC_classes_new_v2.owl");
		msp.parseFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION,FileParserFactory.FORMAT_23ANDME_FILE);
		String base64Profile = msp.getBase64ProfileString();
		
		assertEquals("We check if the file "+fileName+" produces the correct code in forward orientation","QXGqnLF2h8xuqzIyCGJE2hzPzVzqND_q0vtKk2Erxy0gQgD6lxWI0dzPwTq41w2UACs2nwZlF3QPxkv3uuuQtj4S55rDHGVU26ma2Z203z-RCqhavsFv0a5uY1q770Su7_dg80000",base64Profile);
	}
	
	/**
	 * This method checks if the a 23AndMe file produces the correct code in dbsnp orientation. 
	 * @throws NotPatientGenomicFileParsedException 
	 * @throws VariantDoesNotMatchAnAllowedVariantException 
	 * @throws BadFormedBinaryNumberException 
	 * */
	@Test
	public void testRead23AndMeFileStreamInputStreamStringDbsnp() throws NotPatientGenomicFileParsedException, VariantDoesNotMatchAnAllowedVariantException, BadFormedBinaryNumberException {
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
		
		MedicineSafetyProfile_v2 msp = new MedicineSafetyProfile_v2(path+"resources/MSC_classes_new_v2.owl");
		msp.parseFileStream(my23AndMeFileStream, Common.DBSNP_ORIENTATION, FileParserFactory.FORMAT_23ANDME_FILE);
		String base64Profile = msp.getBase64ProfileString();
		
		assertEquals("We check if the file "+fileName+" produces the correct code in dbsnp orientation","QX0G01H2903vY02e80AE0hY20K00G2y40PW0628bw402QY927xWI0dw1uW04002UACs002ZlF3QPxYv3eOuQtj0S4500G03U26ma2Z203z-3CqhavsFv0a5uY1q770Gu6Vdg80000",base64Profile);
	}
}
