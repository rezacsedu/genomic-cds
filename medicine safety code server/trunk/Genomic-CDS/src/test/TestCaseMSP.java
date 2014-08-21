package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import exception.BadFormedBase64NumberException;
import exception.NotInitializedPatientsGenomicDataException;
import exception.VariantDoesNotMatchAnyAllowedVariantException;

import safetycode.DrugRecommendation;
import safetycode.Genotype;
import safetycode.MedicineSafetyProfile_v2;


/**
 * Test the methods implemented in class MedicineSafetyProfile_v2:
 * - parseFileStream(InputStream fileStream, String strandOrientationOfInputData, int typeFileFormat)
 * - parseFileStream(InputStream fileStream, int typeFileFormat)
 * - readBase64ProfileString(String base64Profile)
 * - obtainDrugRecommendations()
 * */
public class TestCaseMSP {
	private MedicineSafetyProfile_v2 msp2;
	private String ontologyURI;
	@Before
	public void initCodingModule(){
		ontologyURI = "MSC_classes_new_v2.owl";
		msp2 = new MedicineSafetyProfile_v2(ontologyURI);
	}
	
	@Test
	public void testreadBase64ProfileString() throws VariantDoesNotMatchAnyAllowedVariantException, BadFormedBase64NumberException, NotInitializedPatientsGenomicDataException{
		String base64Profile = "3O5gjSl1S-Az6GSmcClFaszmPTW7B-Yuv";
		msp2.readBase64ProfileString(base64Profile);
		Genotype genotype = msp2.getGenotype();
		
		assertEquals("We check if the generated genotype with the base64code \"3O5gjSl1S-Az6GSmcClFaszmPTW7B-Yuv\" has defined 45 genotype markers",45,genotype.getListGenotypeElements().size());
		assertEquals("We check if the first element in the generated genotype with the base64code \"2BsYEl9YUF4kFY81XTG2uiBG0l0\" is \"rs6025\"","rs6025",genotype.getListGenotypeElements().get(0).getGeneticMarkerName());
		assertEquals("We check if the last element in the generated genotype with the base64code \"2BsYEl9YUF4kFY81XTG2uiBG0l0\" is \"HLA-B\"","HLA-B",genotype.getListGenotypeElements().get(genotype.getListGenotypeElements().size()-1).getGeneticMarkerName());
	}
	
	
	@Test
	public void testobtainDrugRecommendations() throws VariantDoesNotMatchAnyAllowedVariantException, BadFormedBase64NumberException, NotInitializedPatientsGenomicDataException{
		String base64Profile = "3O5gjSl1S-Az6GSmcClFaszmPTW7B-Yuv";
		msp2.readBase64ProfileString(base64Profile);
		HashMap<String, ArrayList<DrugRecommendation>> listRecommendations = msp2.obtainDrugRecommendations();
		
		assertEquals("We check if the resulting drug recommendations with default genotype is related to 26 drugs",26, listRecommendations.keySet().size());
		assertEquals("We check if the default genotype triggers any Warfarin recommendation",true,listRecommendations.containsKey("Warfarin"));
		boolean rule_50_is_triggered = false;
		if(listRecommendations.containsKey("Simvastatin")){
			ArrayList<DrugRecommendation> list_dr = listRecommendations.get("Simvastatin");
			for(DrugRecommendation dr: list_dr){
				if(dr.getRuleId().contains("CDS rule 50")){
					rule_50_is_triggered=true;
					break;
				}
			}
		}
		assertEquals("We check if the default genotype triggers the CDS rule 50",true,rule_50_is_triggered);
	}
	
	@Test(expected=NotInitializedPatientsGenomicDataException.class)
	public void testNotInitializedPatientsGenomicDataException() throws NotInitializedPatientsGenomicDataException{
		msp2 = new MedicineSafetyProfile_v2(ontologyURI);
		msp2.obtainDrugRecommendations();
	}
}
