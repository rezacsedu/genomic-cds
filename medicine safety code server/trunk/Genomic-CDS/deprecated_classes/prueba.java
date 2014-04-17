
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import exception.BadFormedBase64NumberException;
import exception.BadFormedBinaryNumberException;
import exception.NotInitializedPatientsGenomicDataException;
import exception.NotPatientGenomicFileParsedException;
import exception.VariantDoesNotMatchAnAllowedVariantException;

import safetycode.DrugRecommendation;
import safetycode.FileParserFactory;
import safetycode.MedicineSafetyProfile_v2;


public class prueba {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InputStream my23AndMeFileStream=null;
		String fileIn = "D:/workspace/Genomic-CDS/resources/user8_file2_yearofbirth_unknown_sex_XX.23andme.txt";
		//String fileIn = "D:/workspace/Genomic-CDS/resources/user35_file210_yearofbirth_unknown_sex_XY.23andme-exome-vcf.txt";
		try {
			my23AndMeFileStream = new FileInputStream(fileIn);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		MedicineSafetyProfile_v2 msp2 = new MedicineSafetyProfile_v2("D:/workspace/Genomic CDS/knowledge-base/trunk/ontology/MSC_classes_new_v2.owl");
		try {
			String report = msp2.parseFileStream(my23AndMeFileStream, FileParserFactory.FORMAT_23ANDME_FILE);
			//String report = msp2.parseFileStream(my23AndMeFileStream, FileParserFactory.FORMAT_VCF_FILE);
			System.out.println("REPORT = "+report);
			//System.out.println("genotype="+msp2.getGenotype());
			String code = msp2.getBase64ProfileString();
			System.out.println("Inferred base64code = "+code);
			
			msp2 = new MedicineSafetyProfile_v2("D:/workspace/Genomic CDS/knowledge-base/trunk/ontology/MSC_classes_new_v2.owl");
			code="3DFaKWQSTOJJhenhRlF0eHBIbm0";
			msp2.readBase64ProfileString(code);
			//System.out.println("genotype="+msp2.getGenotype());
			System.out.println("Inferred base64code = "+msp2.getBase64ProfileString());
			
			HashMap<String,ArrayList<DrugRecommendation>> listInferredDrugRecommendations = msp2.obtainDrugRecommendations();
			Iterator<String> listKeys = listInferredDrugRecommendations.keySet().iterator();
			while(listKeys.hasNext()){
				String key = listKeys.next();
				System.out.println("["+key+"]");
				for(DrugRecommendation dr: listInferredDrugRecommendations.get(key)){
					System.out.println("\t"+dr.getRuleId());
				}
			}
		} catch (VariantDoesNotMatchAnAllowedVariantException e1) {
			e1.printStackTrace();
		} catch (NotPatientGenomicFileParsedException e) {
			e.printStackTrace();
		} catch (BadFormedBinaryNumberException e) {
			e.printStackTrace();
		} catch (NotInitializedPatientsGenomicDataException e) {
			e.printStackTrace();
		} catch (BadFormedBase64NumberException e) {
			e.printStackTrace();
		}
		
		/*msp2 = new MedicineSafetyProfile_v2("D:/workspace/Genomic CDS/knowledge-base/trunk/ontology/MSC_classes_new_v2.owl");
		System.out.println("msp2 was created!");
		msp2.initializeCodingModule();
		System.out.println("genotype="+msp2.getGenotype());
		try {
			String base64Profile = msp2.getBase64ProfileString();
			System.out.println("base64Profile -> "+base64Profile);
			msp2.readBase64ProfileString("2B5RchTUMhkO5FbCap4Pb7Fx3H0");
			System.out.println("genotype2="+msp2.getGenotype());
			base64Profile = msp2.getBase64ProfileString();
			System.out.println("base64Profile -> "+base64Profile);
			HashMap<String,ArrayList<DrugRecommendation>> listInferredDrugRecommendations = msp2.obtainDrugRecommendations();
			Iterator<String> listKeys = listInferredDrugRecommendations.keySet().iterator();
			while(listKeys.hasNext()){
				String key = listKeys.next();
				System.out.println("["+key+"]");
				for(DrugRecommendation dr: listInferredDrugRecommendations.get(key)){
					System.out.println("\t"+dr.getRuleId());
				}
			}
		} catch (NotInitializedPatientsGenomicDataException e) {
			e.printStackTrace();
		} catch (NotPatientGenomicFileParsedException e) {
			e.printStackTrace();
		} catch (BadFormedBase64NumberException e) {
			e.printStackTrace();
		} catch (BadFormedBinaryNumberException e) {
			e.printStackTrace();
		} catch (VariantDoesNotMatchAnAllowedVariantException e) {
			e.printStackTrace();
		}*/
		
		/*String fileName="D:/workspace/Genomic-CDS/resources/user35_file210_yearofbirth_unknown_sex_XY.23andme-exome-vcf.txt";
		
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		
		
		msp2.parseFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION, FileParserFactory.FORMAT_VCF_FILE);
		System.out.println("Input stream was parsed");
		try {
			String base64Profile = msp2.getBase64ProfileString();
			System.out.println("base64Profile -> "+base64Profile);
		} catch (NotPatientGenomicFileParsedException e) {
			e.printStackTrace();
		}*/
	}

}
