package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import exception.BadRuleDefinitionException;
import exception.VariantDoesNotMatchAnyAllowedVariantException;

import safetycode.DrugRecommendation;
import safetycode.Genotype;
import utils.Common;
import utils.OntologyManagement;

/**
 * Test the methods implemented in class DrugRecommendation:
 * - setRule(String genomicRule)
 * - matchPatientProfile(Genotype genotype)
 * */
public class TestCaseDrugRecommendation {
	DrugRecommendation dr;
	Genotype genotype;
	OntologyManagement om;
	
	@Before
	public void initDrugRecommendation() throws VariantDoesNotMatchAnyAllowedVariantException{
		String ontologyURI = Common.ONT_NAME;
		om = OntologyManagement.getOntologyManagement(ontologyURI);
		String rule_id = "rule 1";
		String cds_message = "rule message";
		String importance = "Important modification";
		String source = "rule source repository";
		String relevant_for = "Warfarin";
		ArrayList<String> seeAlsoList = new ArrayList<String>();
		seeAlsoList.add("http://safety-code.org");
		String phenotype = "No phenotype description available.";
		dr = new DrugRecommendation(rule_id, cds_message, importance, source, relevant_for, seeAlsoList,"23-08-1983", phenotype);
		genotype = new Genotype(om.getDefaultGenotypeElement());
	}
	
	/**
	 * We check if the rule is correctly parsed.
	 * */
	@Test
	public void testRuleDefinition(){
		ArrayList<DrugRecommendation> listDrugRecommendations = om.getListDrugRecommendation();
		for(DrugRecommendation drRe: listDrugRecommendations){
			assertEquals("We check if the parsing rule is correctly produced in rule "+drRe.getRuleId(),false,drRe.toString().equalsIgnoreCase("null"));
		}
	}
	
	/**
	 * We check if the rule is correctly parsed.
	 * */
	@Test
	public void testsetRule() throws BadRuleDefinitionException{
		String rule = "has exactly 2 ( TPMT_star_2 or TPMT_star_3A or TPMT_star_3B or TPMT_star_3C or TPMT_star_3D or TPMT_star_4 or TPMT_star_5 or TPMT_star_6 or TPMT_star_7 or TPMT_star_8 or TPMT_star_9 or TPMT_star_10 or TPMT_star_11 or TPMT_star_12 or TPMT_star_13 or TPMT_star_14 or TPMT_star_15 or TPMT_star_16 or TPMT_star_17 or TPMT_star_18 )";
		dr.setRule(rule);
		String ruleString = dr.toString();
		assertEquals("We check parsing rule is correctly produced",true,rule.equalsIgnoreCase(ruleString));
	}
	
	/**
	 * We check if the logical description triggers an error when having a combination of 'and' and 'or' in a list of genotype elements.
	 * */
	@Test(expected=BadRuleDefinitionException.class)
	public void testsetRuleErrorAndOr() throws BadRuleDefinitionException{
		String rule = "has some ( TPMT_star_2 or TPMT_star_3A and TPMT_star_3B )";
		dr.setRule(rule);
	}
	
	/**
	 * We check if the logical description triggers an error when having a combination of 'and' and 'or' in a list of conditions.
	 * */
	@Test(expected=BadRuleDefinitionException.class)
	public void testsetRuleErrorAndOr2() throws BadRuleDefinitionException{
		String rule = "has some TPMT_star_2 or has some TPMT_star_3A and has exactly 2 CYP2C19_star_1";
		dr.setRule(rule);
	}
	
	/**
	 * We check if the logical description triggers an error when having a wrong definition of parenthesis.
	 * */
	@Test(expected=BadRuleDefinitionException.class)
	public void testsetRuleErrorParenthesis() throws BadRuleDefinitionException{
		String rule = "has some TPMT_star_2 or has some TPMT_star_3A )";
		dr.setRule(rule);
	}
	
	
	/**
	 * We check if the logical description triggers an error when having an empty string as input.
	 * */
	@Test(expected=BadRuleDefinitionException.class)
	public void testsetRuleErrorEmpty() throws BadRuleDefinitionException{
		String rule = "";
		dr.setRule(rule);
	}
	
	/**
	 * We test that the default genotype has 2 TMPT *1 alleles. 
	 * */
	@Test
	public void testmatchPatientProfile() throws BadRuleDefinitionException{
		String rule = "has exactly 2 TPMT_star_1";
		dr.setRule(rule);
		assertEquals("We check the genomic rule matches the default patient's genotype",true,dr.matchPatientProfile(genotype));
	}
}
