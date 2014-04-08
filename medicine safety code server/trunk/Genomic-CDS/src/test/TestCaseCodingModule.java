package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import exception.BadFormedBase64NumberException;
import exception.BadFormedBinaryNumberException;
import exception.VariantDoesNotMatchAnAllowedVariantException;

import safetycode.Coding_module;
import safetycode.GenotypeElement;
import utils.OntologyManagement;

/**
 * Test the methods implemented in class CodingModule:
 * - codeListGeneticVariations(ArrayList<GenotypeElement> listGenotype)
 * - decodeListGenotypeVariations(String base64Genotype)
 * */
public class TestCaseCodingModule {
	private Coding_module cod_mod;
	private OntologyManagement om;
	
	@Before
	public void initCodingModule(){
		String ontologyURI = "MSC_classes_new_v2.owl";
		om = OntologyManagement.getOntologyManagement(ontologyURI);
		cod_mod = new Coding_module(om.getListGeneticMarkerGroups());
	}
	
	@Test
	public void testcodeListGeneticVariations() throws BadFormedBinaryNumberException, VariantDoesNotMatchAnAllowedVariantException{
		String code = cod_mod.codeListGeneticVariations(om.getDefaultGenotypeElement());
		assertEquals("We check if the generated code is equal to \"3O5gjSl1S-Az6GSmcClFaszmPTW7B-Yuv\"","3O5gjSl1S-Az6GSmcClFaszmPTW7B-Yuv",code);
	}
	
	@Test
	public void testdecodeListGeneticVariations() throws BadFormedBase64NumberException, VariantDoesNotMatchAnAllowedVariantException{
		String code = "3O5gjSl1S-Az6GSmcClFaszmPTW7B-Yuv";
		               
		ArrayList<GenotypeElement> listGE = cod_mod.decodeListGenotypeVariations(code);
		assertEquals("We check the generated list genotype elements size 45",45,listGE.size());
		assertEquals("We check if the generated variation of the rs2297595 SNP in the genotype is \"C;C\"","C;C",listGE.get(0).getCriteriaSyntax());
	}
	
	@Test(expected=BadFormedBase64NumberException.class)
	public void testBadFormedBase64NumberException() throws BadFormedBase64NumberException, VariantDoesNotMatchAnAllowedVariantException{
		String code = "Ñ";
		cod_mod.decodeListGenotypeVariations(code);
	}
}
