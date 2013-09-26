package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import exception.BadFormedBase64NumberException;
import exception.VariantDoesNotMatchAnAllowedVariantException;

import safetycode.FileParserFactory;
import safetycode.MedicineSafetyProfile;
import utils.Common;

public class RefineBulkProcess {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		//String fileIn = "d:/workspace/Genomic-CDS/input3/user158_file67_yearofbirth_unknown_sex_unknown.23andme.txt";
		//String fileIn = "d:/workspace/Genomic-CDS/input3/user1013_file503_yearofbirth_unknown_sex_unknown.23andme.txt";
		//String fileIn = "d:/workspace/Genomic-CDS/input3/user1022_file508_yearofbirth_unknown_sex_unknown.23andme.txt";
		//String fileIn = "d:/workspace/Genomic-CDS/input/user77_file40_yearofbirth_1981_sex_XY.23andme.txt";
		String fileIn = "d:/workspace/Genomic-CDS/input/user1002_file497_yearofbirth_unknown_sex_unknown.23andme.txt";
		//String fileIn = "d:/workspace/Genomic-CDS/input/user1005_file541_yearofbirth_1981_sex_XY.23andme.txt";
		//String fileIn = "d:/workspace/Genomic-CDS/patient_demo.23andme.txt";
		
		//String fileOut = "d:/workspace/Genomic-CDS/output_1/patient_1013_model.owl";
		//String fileOut = "d:/workspace/Genomic-CDS/output_1/patient_158_model.owl";
		//String fileOut = "d:/workspace/Genomic-CDS/output_1/patient_77_model.owl";
		String fileOut = "d:/workspace/Genomic-CDS/output_1/patient_1002_model.owl";
		//String fileOut = "d:/workspace/Genomic-CDS/output_1/patient_1005_model.owl";
		//String fileOut = "d:/workspace/Genomic-CDS/output_1/patient_example_model.owl";		
		
		/*String fileOut = "d:/workspace/Genomic-CDS/input2/user158_file67_yearofbirth_unknown_sex_unknown.23andme.txt";
		BufferedReader br = new BufferedReader(new FileReader(fileIn));
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut));
		String linea = "";
		while((linea=br.readLine())!=null){
			if(!linea.contains("#")){
				linea = linea.replaceAll(" ", "\t");
			}			
			bw.write(linea+"\n");
		}
		br.close();
		bw.close();*/
		
		MedicineSafetyProfile msp = new MedicineSafetyProfile("d:/MSC_classes_1.owl");
		File file = new File(fileIn);
		InputStream my23AndMeFileStream = new FileInputStream(file);
		
		
		
		String report = msp.parseFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION, FileParserFactory.FORMAT_23ANDME_FILE);
		my23AndMeFileStream.close();
		System.out.println("report="+report);
		String base64Profile = msp.getBase64ProfileString();
		System.out.println("code = "+msp.getBase64ProfileString());
		
		try {
			msp.readBase64ProfileString(base64Profile);
		} catch (VariantDoesNotMatchAnAllowedVariantException e) {
			e.printStackTrace();
		} catch (BadFormedBase64NumberException e) {
			e.printStackTrace();
		}
		OutputStream onto = new FileOutputStream(new File(fileOut));
		msp.writeModel(onto);
		onto.close();
		
		msp.testInconsistencies();
		
		/*ArrayList<String> sortedSNP	= msp.getSimplifiedListRsids();
		ArrayList<String> sortedPoly	= msp.getSimplifiedListPolymorphisms();
		ArrayList<String> sortedRule	= msp.getSimplifiedListRules();
		ArrayList<String> results = msp.getStatistics(sortedSNP, sortedPoly, sortedRule);
		for(String res: results){
			System.out.println(res);
		}*/
		
	}

}
