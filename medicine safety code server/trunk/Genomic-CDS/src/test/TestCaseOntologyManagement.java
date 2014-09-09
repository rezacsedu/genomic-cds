package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import exception.VariantDoesNotMatchAnyAllowedVariantException;

import safetycode.DrugRecommendation;
import safetycode.GeneticMarkerGroup;
import safetycode.GenotypeElement;
import safetycode.SNPElement;
import safetycode.SNPsGroup;
import utils.Common;
import utils.OntologyManagement;


/**
 * Test the methods implemented in class OntologyManagement:
 * - getListDrugRecommendation()
 * - getListSNPsGroups()
 * - getVCFRefListRsids()
 * - getList23andMeRsids()
 * - getListGeneticMarkerGroups()
 * - getListGenotypeElements()
 * - getDefaultGenotypeElement()
 * */
public class TestCaseOntologyManagement {
	OntologyManagement om = null;
	
	@Before
	public void initOntologyManagement(){
		String ontologyURI = Common.ONT_NAME;
		om = OntologyManagement.getOntologyManagement(ontologyURI);
	}
	
	@Test
	public void testgetListDrugRecommendation(){
		ArrayList<DrugRecommendation> listRecommendations = om.getListDrugRecommendation();
		assertEquals("We check the size of the list of genomic cds rules is equal to 305",305,listRecommendations.size());
	}
	
	@Test
	public void testgetListSNPsGroups(){
		ArrayList<SNPsGroup> listSNPGroups = om.getListSNPsGroups();
		
		assertEquals("We check the size of the list of SNPs groups is equal to 336",336,listSNPGroups.size());
	}
	
	@Test
	public void testgetVCFRefListRsids(){
		ArrayList<SNPElement> listSNPs = om.getVCFRefListRsids();
		
		assertEquals("We check the size of the list of SNPs in VCF is equal to 336",336,listSNPs.size());
		assertEquals("We check the first SNP is rs4633","rs4633",listSNPs.get(0).getGeneticMarkerName());
		assertEquals("We check the default value of the first SNP in VCF is C;C","C;C",listSNPs.get(0).getCriteriaSyntax());
		assertEquals("We check the last SNP is rs373327528","rs373327528",listSNPs.get(listSNPs.size()-1).getGeneticMarkerName());
		assertEquals("We check the default value of the last SNP in VCF is G;G","G;G",listSNPs.get(listSNPs.size()-1).getCriteriaSyntax());
		
	}
	
	@Test
	public void testgetList23andMeRsids(){
		ArrayList<SNPElement> listSNPs = om.getList23andMeRsids();
		
		assertEquals("We check the size of the list of SNPs in 23andMe is equal to 336",336,listSNPs.size());
		assertEquals("We check the first SNP is rs4633","rs4633",listSNPs.get(0).getGeneticMarkerName());
		assertEquals("We check the default value of the first SNP in 23andme is null;null","null;null",listSNPs.get(0).getCriteriaSyntax());
		assertEquals("We check the last SNP is rs373327528","rs373327528",listSNPs.get(listSNPs.size()-1).getGeneticMarkerName());
		assertEquals("We check the default value of the last SNP in 23andme is null;null","null;null",listSNPs.get(listSNPs.size()-1).getCriteriaSyntax());
	}
	
	@Test
	public void testgetListGeneticMarkerGroups(){
		ArrayList<GeneticMarkerGroup> listgmg = om.getListGeneticMarkerGroups();
		assertEquals("We check the size of the list of Genetic Marker Group is equal to 28",28,listgmg.size());
		assertEquals("We check the first genetic marker is rs113993960","rs113993960",listgmg.get(0).getGeneticMarkerName());
		assertEquals("We check the last genetic marker is UGT1A1","UGT1A1",listgmg.get(listgmg.size()-1).getGeneticMarkerName());
	}
	
	@Test
	public void testgetListGenotypeElements() throws VariantDoesNotMatchAnyAllowedVariantException{
		ArrayList<GenotypeElement> listGenotypeElements = om.getListGenotypeElements();
		assertEquals("We check the size of the list of null genotype elements is equal to 28",28,listGenotypeElements.size());
		assertEquals("We check the first genotype element value is rs113993960","rs113993960",listGenotypeElements.get(0).getGeneticMarkerName());
		assertEquals("We check the first genotype element value is null;null","null;null",listGenotypeElements.get(0).getCriteriaSyntax());
	}
	
	@Test
	public void testgetDefaultGenotypeElement() throws VariantDoesNotMatchAnyAllowedVariantException{
		ArrayList<GenotypeElement> listGenotypeElements = om.getDefaultGenotypeElement();
		assertEquals("We check the size of the list of default genotype elements is equal to 28",28,listGenotypeElements.size());
		assertEquals("We check the first genotype element is rs113993960","rs113993960",listGenotypeElements.get(0).getGeneticMarkerName());
		assertEquals("We check the first genotype element value is CTT;CTT","CTT;CTT",listGenotypeElements.get(0).getCriteriaSyntax());
	}
}
