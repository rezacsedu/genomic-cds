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
import utils.Common;


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
		ontologyURI = Common.ONT_NAME;
		msp2 = new MedicineSafetyProfile_v2(ontologyURI);
	}
	
	/**
	 * We test if the MedicineSafetyProfile is able to call the codingModule.
	 * */
	@Test
	public void testreadBase64ProfileString() throws VariantDoesNotMatchAnyAllowedVariantException, BadFormedBase64NumberException, NotInitializedPatientsGenomicDataException{
		String base64Profile = "HflRkW-6GrNFmRhFhcd";
		msp2.readBase64ProfileString(base64Profile);
		Genotype genotype = msp2.getGenotype();
		
		assertEquals("We check if the generated genotype with the base64code \"HflRkW-6GrNFmRhFhcd\" has defined the 28 genotype markers", 28, genotype.getListGenotypeElements().size());
		assertEquals("We check if the first element in the generated genotype with the base64code \"HflRkW-6GrNFmRhFhcd\" is \"rs113993960\"","rs113993960",genotype.getListGenotypeElements().get(0).getGeneticMarkerName());
		assertEquals("We check if the last element in the generated genotype with the base64code \"HflRkW-6GrNFmRhFhcd\" is \"UGT1A1\"","UGT1A1",genotype.getListGenotypeElements().get(genotype.getListGenotypeElements().size()-1).getGeneticMarkerName());
	}
	
	/**
	 * We test if the MedicineSafetyProfile is able to obtain the corresponding drug recommendations.
	 * */
	@Test
	public void testobtainDrugRecommendations() throws VariantDoesNotMatchAnyAllowedVariantException, BadFormedBase64NumberException, NotInitializedPatientsGenomicDataException{
		String base64Profile = "HflRkW-6GrNFmRhFhcd";
		msp2.readBase64ProfileString(base64Profile);
		HashMap<String, ArrayList<DrugRecommendation>> listRecommendations = msp2.obtainDrugRecommendations();
		
		assertEquals("We check if the resulting drug recommendations with default genotype is related to 47 drugs",47, listRecommendations.keySet().size());
		int nTriggeredRules = 0;
		for(String key: listRecommendations.keySet()){
			
			for(@SuppressWarnings("unused") DrugRecommendation dr: listRecommendations.get(key)){
				nTriggeredRules++;
				//System.out.println("["+nTriggeredRules+"]="+dr.getRuleId());
			}
		}
		assertEquals("We check if the resulting number of drug recommendations is 77",77,nTriggeredRules);
		assertEquals("We check if the default genotype triggers any Warfarin recommendation",false,listRecommendations.containsKey("Warfarin"));
		boolean rule_50_is_triggered = false;
		if(listRecommendations.containsKey("Simvastatin")){
			ArrayList<DrugRecommendation> list_dr = listRecommendations.get("Simvastatin");
			for(DrugRecommendation dr: list_dr){
				if(dr.getRuleId().contains("CDS_rule_50")){
					rule_50_is_triggered=true;
					break;
				}
			}
		}
		assertEquals("We check if the default genotype triggers the CDS rule 50",true,rule_50_is_triggered);
	}
	
	/**
	 * We test if the MedicineSafetyProfile is able to 
	 * */
	@Test(expected=NotInitializedPatientsGenomicDataException.class)
	public void testNotInitializedPatientsGenomicDataException() throws NotInitializedPatientsGenomicDataException{
		msp2 = new MedicineSafetyProfile_v2(ontologyURI);
		msp2.obtainDrugRecommendations();
	}
}
