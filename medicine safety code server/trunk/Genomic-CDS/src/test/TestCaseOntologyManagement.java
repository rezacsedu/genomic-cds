package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import exception.VariantDoesNotMatchAnAllowedVariantException;

import safetycode.DrugRecommendation;
import safetycode.Genetic_Marker_Group;
import safetycode.GenotypeElement;
import safetycode.SNPElement;
import safetycode.SNPsGroup;
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
		String ontologyURI = "MSC_classes_new_v2.owl";
		om = OntologyManagement.getOntologyManagement(ontologyURI);
	}
	
	@Test
	public void testgetListDrugRecommendation(){
		ArrayList<DrugRecommendation> listRecommendations = om.getListDrugRecommendation();
		assertEquals("We check the size of the list of genomic cds rules is equal to 298",298,listRecommendations.size());
	}
	
	@Test
	public void testgetListSNPsGroups(){
		ArrayList<SNPsGroup> listSNPGroups = om.getListSNPsGroups();
		
		assertEquals("We check the size of the list of SNPs groups is equal to 320",320,listSNPGroups.size());
	}
	
	@Test
	public void testgetVCFRefListRsids(){
		ArrayList<SNPElement> listSNPs = om.getVCFRefListRsids();
		
		assertEquals("We check the size of the list of SNPs in VCF is equal to 320",320,listSNPs.size());
		assertEquals("We check the first SNP is rs10038095","rs10038095",listSNPs.get(0).getGeneticMarkerName());
		assertEquals("We check the default value of the first SNP in VCF is A;A","A;A",listSNPs.get(0).getCriteriaSyntax());
		assertEquals("We check the last SNP is rs67376798","rs67376798",listSNPs.get(listSNPs.size()-1).getGeneticMarkerName());
		assertEquals("We check the default value of the last SNP in VCF is T;T","T;T",listSNPs.get(listSNPs.size()-1).getCriteriaSyntax());
		
	}
	
	@Test
	public void testgetList23andMeRsids(){
		ArrayList<SNPElement> listSNPs = om.getList23andMeRsids();
		
		assertEquals("We check the size of the list of SNPs in 23andMe is equal to 320",320,listSNPs.size());
		assertEquals("We check the first SNP is rs10038095","rs10038095",listSNPs.get(0).getGeneticMarkerName());
		assertEquals("We check the default value of the first SNP in 23andme is null;null","null;null",listSNPs.get(0).getCriteriaSyntax());
		assertEquals("We check the last SNP is rs67376798","rs67376798",listSNPs.get(listSNPs.size()-1).getGeneticMarkerName());
		assertEquals("We check the default value of the last SNP in 23andme is null;null","null;null",listSNPs.get(listSNPs.size()-1).getCriteriaSyntax());
	}
	
	@Test
	public void testgetListGeneticMarkerGroups(){
		ArrayList<Genetic_Marker_Group> listgmg = om.getListGeneticMarkerGroups();
		assertEquals("We check the size of the list of Genetic Marker Group is equal to 45",45,listgmg.size());
		assertEquals("We check the first genetic marker is rs6025","rs6025",listgmg.get(0).getGeneticMarkerName());
		assertEquals("We check the last genetic marker is HLA-B","HLA-B",listgmg.get(listgmg.size()-1).getGeneticMarkerName());
	}
	
	@Test
	public void testgetListGenotypeElements() throws VariantDoesNotMatchAnAllowedVariantException{
		ArrayList<GenotypeElement> listGenotypeElements = om.getListGenotypeElements();
		assertEquals("We check the size of the list of null genotype elements is equal to 45",45,listGenotypeElements.size());
		assertEquals("We check the first genotype element value is rs6025","rs6025",listGenotypeElements.get(0).getGeneticMarkerName());
		assertEquals("We check the first genotype element value is null;null","null;null",listGenotypeElements.get(0).getCriteriaSyntax());
	}
	
	@Test
	public void testgetDefaultGenotypeElement() throws VariantDoesNotMatchAnAllowedVariantException{
		ArrayList<GenotypeElement> listGenotypeElements = om.getDefaultGenotypeElement();
		assertEquals("We check the size of the list of default genotype elements is equal to 45",45,listGenotypeElements.size());
		assertEquals("We check the first genotype element is rs6025","rs6025",listGenotypeElements.get(0).getGeneticMarkerName());
		assertEquals("We check the first genotype element value is C;C","C;C",listGenotypeElements.get(0).getCriteriaSyntax());
	}
}
