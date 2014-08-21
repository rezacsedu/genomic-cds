package safetycode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import utils.OntologyManagement;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasoner;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;
import exception.VariantDoesNotMatchAnyAllowedVariantException;


/**It represents the genotype of a patient. It can be represented as a set of SNPs or a set of Alleles and SNPs.
 * 
 * @author Jose Antonio Miñarro Giménez
 * */
public class Genotype {
	
	/**List of SNP gathered from genotype files in the formats 23andMe or VCF.*/
	private ArrayList<SNPElement> listSNPs = null;
	/**List of Genotype elements used to trigger the drug dosage recommendation rules.*/
	private ArrayList<GenotypeElement> listGenotypeElements = null;
	
	/**Construct the patient's genotype with the information of SNPs and infer the corresponding alleles.
	 * @throws VariantDoesNotMatchAnyAllowedVariantException */	
	public Genotype(ArrayList<SNPElement> listSNPs,OntologyManagement om) throws VariantDoesNotMatchAnyAllowedVariantException{
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
	private void inferGenotypeElements(OntologyManagement om) throws VariantDoesNotMatchAnyAllowedVariantException{
		
		OWLOntologyManager reasoner_manager = om.getNewOntologyManager();
		OWLOntology reasoner_ontology		= reasoner_manager.getOntologies().iterator().next();
		OWLDataFactory factory 				= reasoner_manager.getOWLDataFactory();
		OWLNamedIndividual patient 			= createPatient(reasoner_ontology,reasoner_manager, factory);
		
		initializeGenotype(reasoner_manager, patient);//Add the SNPs to the patient
		
		RELReasoner local_reasoner = new RELReasonerFactory().createReasoner(reasoner_ontology);
		local_reasoner.precomputeInferences();
		
		
		ArrayList<String> listAlleleLabels = new ArrayList<String>();
		ArrayList<String> listSNPLabels = new ArrayList<String>();
		NodeSet<OWLClass> list_types = local_reasoner.getTypes(patient, false);
		for (OWLClass type : list_types.getFlattened()) {
			
			Iterator<OWLClassExpression> list_superclasses = type.getSuperClasses(reasoner_ontology).iterator();
			while(list_superclasses.hasNext()){
				OWLClassExpression oce = list_superclasses.next();
				if(oce.isAnonymous()) continue;
				OWLClass superclass = oce.asOWLClass();
				
				if (superclass.getIRI().toString().contains("human_with_genetic_polymorphism")) {
					String label = "";
					Set<OWLAnnotation> listLabels = type.getAnnotations(reasoner_ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
					for (OWLAnnotation labelAnn : listLabels) {
						if (labelAnn.getValue() instanceof OWLLiteral) {
							OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
							label = literal.getLiteral().trim();
							listAlleleLabels.add(make_valid(label));
							break;
						}
					}
					break;
				}
				if(superclass.getIRI().toString().contains("human_with_genotype_at")){
					String label = "";
					Set<OWLAnnotation> listLabels = type.getAnnotations(reasoner_ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
					for (OWLAnnotation labelAnn : listLabels) {
						if (labelAnn.getValue() instanceof OWLLiteral) {
							OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
							label = literal.getLiteral().trim();
							listSNPLabels.add(label);
							break;
						}
					}
					break;
				}
			}
		}
		
		listGenotypeElements = om.getListGenotypeElements();		
		setPatientGenotype(listAlleleLabels,listSNPLabels);
	}
	
	
	/**
	 * Initialize the patient instance with the information from the list of SNPs gathered from genotype files.
	 * 
	 * @param reasoner_manager	New ontology manager instance with no patient data included.
	 * @param patient			The new instance of the patient to be included in the ontology.
	 * */
	private void initializeGenotype(OWLOntologyManager reasoner_manager, OWLNamedIndividual patient){
		OWLOntology reasoner_ontology = reasoner_manager.getOntologies().iterator().next();
		OWLDataFactory factory = reasoner_manager.getOWLDataFactory();
		if(listSNPs!=null && (!listSNPs.isEmpty())){
			for(SNPElement snpe: listSNPs){
				String criteriaSyntax = snpe.getCriteriaSyntax().replace(";", "_");
				String nameClassVariant = "http://www.genomic-cds.org/ont/MSC_classes.owl#human_with_genotype_"+snpe.getGeneticMarkerName()+"_variant_"+criteriaSyntax; //E.g.: <human_with_genotype_rs1051266_C_C>
				OWLClass matchedVariantClass = factory.getOWLClass(IRI.create(nameClassVariant));
				if(matchedVariantClass!=null){
					OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(matchedVariantClass,patient);
	        		reasoner_manager.addAxiom(reasoner_ontology,classAssertion);
				}else{
					System.out.println("ERROR: We could not add the SNP variant "+criteriaSyntax+" to the patient");
				}
			}
		}
	}
	
	/**
	 * Create the patient instance in the model.
	 * @return The individual that represents the patient's profile.
	 * */
	private OWLNamedIndividual createPatient(OWLOntology reasoner_ontology, OWLOntologyManager reasoner_manager, OWLDataFactory factory){
		
		OWLClass humanClass = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human"));
		OWLNamedIndividual patientIndividual = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));
		OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(humanClass, patientIndividual);
		reasoner_manager.addAxiom(reasoner_ontology, classAssertion);

		return patientIndividual;
	}
	
	
	private void setPatientGenotype(ArrayList<String> listAlleleLabels, ArrayList<String> listSNPLabels){
		for(GenotypeElement ge : listGenotypeElements){
			String variant1 = ge.getVariant1();
			String variant2 = ge.getVariant2();
			
			boolean setVariant1=false;
			boolean setVariant2=false;
			
			for(String alleleLabel : listAlleleLabels){
				if(alleleLabel.contains(ge.getGeneticMarkerName())){
					if(alleleLabel.contains("homozygous")){
						variant1 = alleleLabel.substring(alleleLabel.indexOf(ge.getGeneticMarkerName()+"_")+(ge.getGeneticMarkerName().length()+1));
						variant2 = variant1;
						ge.setVariants(variant1, variant2);
						break;
					}
					if(!setVariant1){
						setVariant1=true;
						variant1 = alleleLabel.substring(alleleLabel.indexOf(ge.getGeneticMarkerName()+"_")+(ge.getGeneticMarkerName().length()+1));
					}else{
						if(!setVariant2){
							setVariant2=true;
							variant2 = alleleLabel.substring(alleleLabel.indexOf(ge.getGeneticMarkerName()+"_")+(ge.getGeneticMarkerName().length()+1));
						}
					}
				}
				if(setVariant1 && setVariant2){
					ge.setVariants(variant1, variant2);
					break;
				}
			}
			if(!setVariant1 || !setVariant2){
				for(String snpLabel : listSNPLabels){
					if(snpLabel.contains(ge.getGeneticMarkerName())){

						if(snpLabel.contains("(")&&snpLabel.contains(";")&&snpLabel.contains(")")){
							variant1 = snpLabel.substring(snpLabel.indexOf("(")+1,snpLabel.indexOf(";"));
							variant2 = snpLabel.substring(snpLabel.indexOf(";")+1,snpLabel.indexOf(")"));
							ge.setVariants(variant1, variant2);
						}
					}
				}
			}
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
	
	public ArrayList<String> getPatientInferredStatistics(OntologyManagement om){
		OWLOntologyManager reasoner_manager = om.getNewOntologyManager();
		OWLOntology reasoner_ontology		= reasoner_manager.getOntologies().iterator().next();
		OWLDataFactory factory 				= reasoner_manager.getOWLDataFactory();
		OWLNamedIndividual patient 			= createPatient(reasoner_ontology,reasoner_manager, factory);
		
		
		
		initializeGenotype(reasoner_manager, patient);//Add the SNPs to the patient
		
		RELReasoner local_reasoner = new RELReasonerFactory().createReasoner(reasoner_ontology);
		local_reasoner.precomputeInferences();
		
		
		ArrayList<String> listAlleleLabels		= new ArrayList<String>();
		ArrayList<String> listSNPLabels			= new ArrayList<String>();
		ArrayList<String> listRuleLabels		= new ArrayList<String>();
		ArrayList<String> listPhenotypeLabels	= new ArrayList<String>();
		
		NodeSet<OWLClass> list_types = local_reasoner.getTypes(patient, false);
		for (OWLClass type : list_types.getFlattened()) {
			Iterator<OWLClassExpression> list_superclasses = type.getSuperClasses(reasoner_ontology).iterator();
			while(list_superclasses.hasNext()){
				OWLClassExpression oce = list_superclasses.next();
				if(oce.isAnonymous()) continue;
				OWLClass superclass = oce.asOWLClass();
				
				if (superclass.getIRI().toString().contains("human_with_genetic_polymorphism")) {
					String label = "";
					Set<OWLAnnotation> listLabels = type.getAnnotations(reasoner_ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
					for (OWLAnnotation labelAnn : listLabels) {
						if (labelAnn.getValue() instanceof OWLLiteral) {
							OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
							label = literal.getLiteral().trim();
							listAlleleLabels.add(make_valid(label));
							break;
						}
					}
					//break;
				}
				if(superclass.getIRI().toString().contains("human_with_genotype_at")){
					String label = "";
					Set<OWLAnnotation> listLabels = type.getAnnotations(reasoner_ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
					for (OWLAnnotation labelAnn : listLabels) {
						if (labelAnn.getValue() instanceof OWLLiteral) {
							OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
							label = literal.getLiteral().trim();
							if(label.contains("(null;null)")) break;
							if(label.contains("human with")){
								label = label.substring(label.indexOf("with")+4).trim();
								listSNPLabels.add(label);
								break;
							}
						}
					}
					//break;
				}
				
				if(superclass.getIRI().toString().contains("human_triggering_phenotype_inference_rule")){
					String label = "";
					Set<OWLAnnotation> listLabels = type.getAnnotations(reasoner_ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
					for (OWLAnnotation labelAnn : listLabels) {
						if (labelAnn.getValue() instanceof OWLLiteral) {
							OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
							label = literal.getLiteral().trim();
							if(label.contains("human with")){
								label = label.substring(label.indexOf("with")+4).trim();
								listPhenotypeLabels.add(label);
								break;
							}
						}
					}
					//break;
				}
				
				if(superclass.getIRI().toString().contains("human_triggering_CDS_rule")){
					String label = "";
					Set<OWLAnnotation> listLabels = type.getAnnotations(reasoner_ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
					for (OWLAnnotation labelAnn : listLabels) {
						if (labelAnn.getValue() instanceof OWLLiteral) {
							OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
							label = literal.getLiteral().trim();
							if(label.contains("rule")){
								label = label.substring(label.indexOf("rule")).trim();
								listRuleLabels.add(label);
								break;
							}
						}
					}
					//break;
				}
			}
		}
		
		ArrayList<String> results = new ArrayList<String>();
		
		String snpData ="Number of SNP = "+listSNPLabels.size()+";";
		/*for(String snp: listSNPLabels){
			snpData+=snp+";";
		}*/
		results.add(snpData);
		
		String haplotypeData ="Number of Haplotypes = "+listAlleleLabels.size()+";";
		for(String haplotype: listAlleleLabels){
			haplotypeData+=haplotype+";";
		}
		results.add(haplotypeData);
		
		String phenotypeData ="Number of phenotype rules = "+listPhenotypeLabels.size()+";";
		for(String phenotype: listPhenotypeLabels){
			phenotypeData+=phenotype+";";
		}
		results.add(phenotypeData);
		
		String ruleData ="Number of CDS rules = "+listRuleLabels.size()+";";
		for(String rule: listRuleLabels){
			ruleData+=rule+";";
		}
		results.add(ruleData);
		
		return results;
	}
	
}
