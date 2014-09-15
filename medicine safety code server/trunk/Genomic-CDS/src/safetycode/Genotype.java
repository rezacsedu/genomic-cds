package safetycode;

import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.Set;

//import org.semanticweb.owlapi.model.IRI;
//import org.semanticweb.owlapi.model.OWLAnnotation;
//import org.semanticweb.owlapi.model.OWLClass;
//import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
//import org.semanticweb.owlapi.model.OWLClassExpression;
//import org.semanticweb.owlapi.model.OWLDataFactory;
//import org.semanticweb.owlapi.model.OWLLiteral;
//import org.semanticweb.owlapi.model.OWLNamedIndividual;
//import org.semanticweb.owlapi.model.OWLOntology;
//import org.semanticweb.owlapi.model.OWLOntologyManager;
//import org.semanticweb.owlapi.reasoner.NodeSet;
//import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import utils.OntologyManagement;
//import eu.trowl.owlapi3.rel.reasoner.dl.RELReasoner;
//import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;
import exception.VariantDoesNotMatchAnyAllowedVariantException;


/**
 * It represents the genotype of a patient. It can be represented as a set of SNPs or a set of Alleles and SNPs.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 * */
public class Genotype {
	
	/**List of SNP gathered from genotype files in the formats 23andMe or VCF.*/
	private ArrayList<SNPElement> listSNPs = null;
	/**List of Genotype elements used to trigger the drug dosage recommendation rules.*/
	private ArrayList<GenotypeElement> listGenotypeElements = null;
	
	/**Construct the patient's genotype with the information of SNPs and infer the corresponding alleles.
	 * @throws VariantDoesNotMatchAnyAllowedVariantException */	
	public Genotype(ArrayList<SNPElement> listSNPs, OntologyManagement om) throws VariantDoesNotMatchAnyAllowedVariantException{
		this.listSNPs = listSNPs;
		inferGenotypeElements(om);
	}
	
	/**Construct the patient's genotype based on the list of SNPs and Allele markers.*/
	public Genotype(ArrayList<GenotypeElement> listGenotypeElements){
		this.listGenotypeElements = listGenotypeElements;
	}

	/**
	 * Set the list of patient's genotype elements.
	 * 
	 * @param listGenotypeElments	List of patient's genotype elements.
	 * */
	public void setGenotypeElements(ArrayList<GenotypeElement> listGenotypeElements){
		this.listGenotypeElements = listGenotypeElements;
	}
	
	/**Get the list of SNP variants associated to the patient's genotype.
	 * 
	 * @return List of SNPs gathered from the 23andMe or VCF genotype files.
	 * */
	public ArrayList<SNPElement> getListSNPelements(){
		return listSNPs;
	}
	
	/**Get the list of SNP variants and alleles associated to the patient's genotype.
	 * 
	 * @return List of SNPs and Alleles that represent the patient's genotype.
	 * */
	public ArrayList<GenotypeElement> getListGenotypeElements(){
		return listGenotypeElements;
	}
		
	
	/**Method to infer the alleles associated to a patient's genotype based on its SNP variants.
	 * 
	 * @param om	OntologyManagement singleton instance that provides the ontology information to infer the genotype elements related to patient's genotype.
	 * @throws VariantDoesNotMatchAnyAllowedVariantException 
	 * */
	private void inferGenotypeElements(OntologyManagement om){
		listGenotypeElements = new ArrayList<GenotypeElement>();
		ArrayList<GenotypeElement> listSNPsElements = new ArrayList<GenotypeElement>();
		for(SNPElement snpe: listSNPs){
			listSNPsElements.add(snpe);
		}

		ArrayList<AlleleRule> listAlleleRules = om.getListAlleleRules();
		ArrayList<GeneticMarkerGroup> listGMG = om.getListGeneticMarkerGroups();
		for(GeneticMarkerGroup gmg: listGMG){
			String geneName = gmg.getGeneticMarkerName();
			GenotypeElement ge = null;
			for(AlleleRule ar: listAlleleRules){
				if(geneName.equals(ar.getGeneName())){
					try {
						ge=ar.matchPatientProfile(listSNPsElements);
						break;
					}catch (VariantDoesNotMatchAnyAllowedVariantException e) {
						e.printStackTrace();
					}
				}
			}
			if(ge==null){
				for(SNPElement snpe: listSNPs){
					if(geneName.equals(snpe.getGeneticMarkerName())){
						ge = snpe;
						break;
					}
				}
				if(ge==null){
					try {
						ge = gmg.getGenotypeElement(0);
					} catch (VariantDoesNotMatchAnyAllowedVariantException e1) {
						e1.printStackTrace();
					}
				}
			}
			listGenotypeElements.add(ge);
		}
	}
		
	/** 
	 * It transforms ids in order to be used in an ontology URI.
	 * 
	 * @param label		The string with replaced special chars such as '*', '#', and without spaces.
	 * @return 			The string with the original format.
	 */
	public String make_valid(String label){
		String valid_label = label.replace("*","star_");
		valid_label = valid_label.replace("#","_hash");
		valid_label = valid_label.replaceAll("[\\[\\]()\\s/:;]","_");
		valid_label = valid_label.replaceAll("__", "_");
		if(valid_label.startsWith("_")){
			valid_label = valid_label.substring(1);
		}
		if(valid_label.endsWith("_")){
			valid_label = valid_label.substring(0,valid_label.length()-1);
		}
		return valid_label;
	}

	/**
	 * Overrides the toString method to show how the rule was parsed.
	 * */
	public String toString(){
		String desc="";
		for(GenotypeElement ge: listGenotypeElements){
			if(!desc.isEmpty()){
				desc+="\n";
			}
			desc+="["+ge.getGeneticMarkerName()+"]->"+ge.getCriteriaSyntax();
		}
		return desc;
	}
	
	/**
	 * Modify the variant related to a particular genotype marker.
	 * 
	 * @param genotypeName		The name or id of the corresponding genotype marker group to be modified.
	 * @param genotypeVariant	The criteria syntax of the new variant element. 
	 * */
	public void modifyGenotypeElement(String genotypeName,String genotypeVariant){
		if(listGenotypeElements!=null){
			for(GenotypeElement ge: listGenotypeElements){
				if(ge.getGeneticMarkerName().equalsIgnoreCase(genotypeName)){
					String variant1 = null;
					String variant2 = null;
					if(genotypeVariant.contains(";")){
						variant1 = genotypeVariant.substring(0,genotypeVariant.indexOf(";"));
						variant2 = genotypeVariant.substring(genotypeVariant.indexOf(";")+1);
					}
					ge.setVariants(variant1, variant2);
				}
			}
		}
	}
}
