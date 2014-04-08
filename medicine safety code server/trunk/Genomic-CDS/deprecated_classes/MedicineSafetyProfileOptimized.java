package safetycode;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import utils.Common;
import utils.OntologyManagement;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasoner;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;
import exception.BadFormedBase64NumberException;
import exception.NotInitializedPatientsGenomicDataException;
import exception.NotPatientGenomicFileParsedException;
import exception.VariantDoesNotMatchAnAllowedVariantException;
/**
 * @deprecated
 * */
public class MedicineSafetyProfileOptimized {
	/** 64 base representation of the variant codes of the associated patient. */
	private String base64ProfileString;
	/** Ontology model that contains the patient conceptualization and pharmacogenomics semantic rules. */
	private OWLOntology ontology = null;
	/** Ontology manager that contains the base ontological model. */
	private OWLOntologyManager manager = null;
	/** Ontology manager that contains the populated ontological model based on patient genomic data.*/
	private OWLOntologyManager reasoner_manager = null;
	/** Populated ontology that contains the patient genomic data.*/
	private OWLOntology reasoner_ontology = null;
	/** Singleton instance of the OntologyManagement class which contains the base ontological model. */
	private OntologyManagement om = null;
	
	private String desc="";
	
	public void setDesc(String desc){
		this.desc=desc;
	}
	
	public String getDesc(){
		return desc;
	}
	
	/**
	 * Constructor of the class. It initializes the model of the pharmacogenomics dataset.
	 * 
	 * @param ontologyFile	Path of the ontology file in the local disk.
	 * @return		New instance of MedicineSafetyProfileOptimized class.
	 * */
	public MedicineSafetyProfileOptimized(String ontologyFile) {
		initializeModel(ontologyFile);
	}
	
	
	/**
	 * It initializes the model with core pharmacogenomic dataset.
	 * 
	 * @param ontologyFile	Path of the ontology file in the local disk.
	 * */
	private void initializeModel(String ontologyFile) {
		/*try {
			om = OntologyManagement.getOntologyManagement(ontologyFile);
			ontology = om.getOntology();
			manager = om.getManager();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ERROR in Path = " + ontologyFile);
		}*/
	}

	
	/**
	 * Create the patient instance in the model.
	 * @return The individual that represents the patient's profile.
	 * @throws NotInitializedPatientsGenomicDataException 
	 * */
	private OWLNamedIndividual createPatient() throws NotInitializedPatientsGenomicDataException {
		
		if(reasoner_manager == null || reasoner_ontology==null){
			throw new NotInitializedPatientsGenomicDataException("ERROR: The patient's genomic data has not been proceseed yet!");
		}	
		
		OWLDataFactory factory = reasoner_manager.getOWLDataFactory();
		OWLClass humanClass = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human"));
		OWLNamedIndividual patientIndividual = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));
		OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(humanClass, patientIndividual);
		reasoner_manager.addAxiom(reasoner_ontology, classAssertion);

		return patientIndividual;
	}
	
	
	/**
	 * Get method to obtain the 64 base representation of the patient variants
	 * of the markers.
	 * 
	 * @return The string associated to the variants of the patient or null if it is not yet defined.
	 * @throws NotPatientGenomicFileParsedException 
	 * */
	public String getBase64ProfileString() throws NotPatientGenomicFileParsedException {
		if (base64ProfileString.isEmpty()){
			throw new NotPatientGenomicFileParsedException("ERROR: No patient's genomic data has been processed yet!");
		}
			
		return base64ProfileString;
	}

	
	/**
	 * It parses the input file with the corresponding file parser.
	 * 
	 * @param fileStream					The input file to parse.
	 * @param strandOrientationOfInputData	The orientation of the input strand to parse.
	 * @param typeFileFormat				The type of file format that is used by the input file.
	 * @return	The processing report of the input file.
	 * */
	public String parseFileStream(InputStream fileStream, String strandOrientationOfInputData, int typeFileFormat){
		
		FileParser fp= FileParserFactory.getFileParser(typeFileFormat, om);
		String report = fp.parse(fileStream, strandOrientationOfInputData);
		
		//base64ProfileString = fp.getBase64ProfileString();
		return report;
	}
	
	
	/**
	 * It parses the input file with the corresponding file parser and default strand orientation.
	 * 
	 * @param fileStream					The input file to parse.
	 * @param typeFileFormat				The type of file format that is used by the input file.
	 * @return	The processing report of the input file.
	 * */
	public String parseFileStream(InputStream fileStream, int typeFileFormat){
		
		FileParser fp= FileParserFactory.getFileParser(typeFileFormat, om);
		String report = fp.parse(fileStream, Common.FORWARD_ORIENTATION);
		
		//base64ProfileString = fp.getBase64ProfileString();
		return report;
	}
	
	
	/**
	 * It associate the classes of variants that match the bit_code of the marker to the patient instance in the model.
	 * 
	 * @param patient			Individual that corresponds to the instance of the patient in the model.
	 * @param criteriaSyntax	It represents the criteria syntax of a variant in the model.
	 * @throws	A VariantDoesNotMatchAnAllowedVariantException when an error is detected in the model definition.
	 * @return	It returns true if the variant is added to the patient and false otherwise.
	 * */
	private boolean addVariantToPatientByBitCode(OWLIndividual patient, String rsid, String bit_code) throws VariantDoesNotMatchAnAllowedVariantException{
		boolean added=false;
		
		OWLDataFactory factory 				= reasoner_manager.getOWLDataFactory();
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		OWLAnnotationProperty ann_rsid		= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rsid"));
		OWLAnnotationProperty ann_bit_code	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_code"));
    	OWLClass matchedVariantClass		= null;
    			
    	//Seek for the corresponding marker in the ontology
    	Iterator<OWLClassExpression> list_marker = human_with_genotype_marker.getSubClasses(reasoner_ontology).iterator();
		while(list_marker.hasNext()){
			OWLClass clase = list_marker.next().asOWLClass();
        	String literal_rsid="";
        	//Obtain the rsid of each marker
        	for (OWLAnnotation annotation : clase.getAnnotations(reasoner_ontology, ann_rsid)) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    literal_rsid=val.getLiteral();
                }
            }
        	
        	if(literal_rsid!=null && literal_rsid.equalsIgnoreCase(rsid)){//Check if the literal_rsid is equal to the marker's rsid we are looking for
        		//Seek for the variants that contain the desired bit_code
        		Iterator<OWLClassExpression> list_variants = clase.getSubClasses(reasoner_ontology).iterator();
        		while(list_variants.hasNext()){
        			OWLClass variant = list_variants.next().asOWLClass();	
        			
        			//We obtain the criteriaSyntax annotation value and compare with the given string
                	for (OWLAnnotation annotation : variant.getAnnotations(reasoner_ontology, ann_bit_code)) {
                        if (annotation.getValue() instanceof OWLLiteral) {
                            OWLLiteral val = (OWLLiteral) annotation.getValue();
                            if(bit_code.equals(val.getLiteral())){
                            	matchedVariantClass = variant;
                            }
                        }
                	}
                	if(matchedVariantClass!=null) break;
                }
        		
        		//We obtain the bit_code digits if the desired variant is found
            	if(matchedVariantClass!=null){
            		//We add the variant class to the patient instance in the ontology
            		OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(matchedVariantClass,patient);
            		reasoner_manager.addAxiom(reasoner_ontology,classAssertion);
            		added=true;
            	}
        	}
        }
		
		return added;
	}
		
	
	public void readBase64ProfileString() throws NotInitializedPatientsGenomicDataException, VariantDoesNotMatchAnAllowedVariantException{
		String genotype_code="";
		//Genotype genotype = new Genotype(om);
		//genotype.setGenotypeByCode(genotype_code);
		OWLNamedIndividual patient = createPatient();
		//ArrayList<SNPelement_old> listSNP = genotype.getListSNPelements();
		
		
	}
	
	
	/**
	 * Create the patient model that is related to the base64Profile.
	 * 
	 * @param base64Profile Base 64 number that represent the binary codification of a patient genotype.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * @throws BadFormedBase64NumberException
	 * @throws NotInitializedPatientsGenomicDataException 
	 * */
	public void readBase64ProfileString(String base64Profile) throws VariantDoesNotMatchAnAllowedVariantException, BadFormedBase64NumberException, NotInitializedPatientsGenomicDataException {
		/*base64ProfileString = base64Profile;
		String binaryProfile = "";

		binaryProfile = Common.convertFrom64To2(base64ProfileString);
		reasoner_manager = om.getNewOntologyManager();
		reasoner_ontology = reasoner_manager.getOntologies().iterator().next();
		
		//ArrayList<SNPelement_old> listRsids = om.getListRsids();

		OWLNamedIndividual patient = createPatient();
		
		for (int position = 0, i = 0; i < listRsids.size(); i++) {
			SNPelement_old genotype = listRsids.get(i);
			int bit_length = Integer.parseInt(genotype.getBit_lenght());
			if (binaryProfile.length() < position + bit_length) {
				throw new VariantDoesNotMatchAnAllowedVariantException(
						"<p>Warning: the length of the patient profile is shorter than the defined in the model</p>");
			}
			String bit_code = binaryProfile.substring(position, position + bit_length);
			genotype.setBit_code(bit_code);
			position += bit_length;
			if (!addVariantToPatientByBitCode(patient, genotype.getRsid(), genotype.getBit_code())) {
				throw new VariantDoesNotMatchAnAllowedVariantException(
						"<p>Warning: the genotype mark \"" + genotype.getRsid() + "\" or its corresponding code variant were not found in the model</p>");
			}
		}*/
	}

	
	/**
	 * Create the patient profile based on allele data.
	 * 
	 * @param classURI	Indicates the IRI of a class in the ontology related to an Allele.
	 * @throws NotInitializedPatientsGenomicDataException 
	 * */
	public void addPatientAllele(String classURI) throws NotInitializedPatientsGenomicDataException {
		OWLNamedIndividual patient = null;
		OWLDataFactory factory = null;
		if(reasoner_manager==null){
			reasoner_manager = om.getNewOntologyManager();
			reasoner_ontology = reasoner_manager.getOntologies().iterator().next();
			patient = createPatient();
			factory = reasoner_manager.getOWLDataFactory(); 
		}else{
			factory = reasoner_manager.getOWLDataFactory();
			patient = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));
		}
		OWLClass newType = factory.getOWLClass(IRI.create(classURI));
		OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(newType,patient);
		reasoner_manager.addAxiom(reasoner_ontology, classAssertion);
	}
	
	
	/**
	 * Obtain the label of a particular class in the ontology.
	 * 
	 * @param classURI	The IRI of a class in the ontology.
	 * @return		The label of the class in the ontology.
	 * */
	public String getResourceLabel(String classURI){
		String label_class="";
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass ontoClass = factory.getOWLClass(IRI.create(classURI));
		
		Set<OWLAnnotation> listLabels = ontoClass.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
		for (OWLAnnotation labelAnn : listLabels) {
			if (labelAnn.getValue() instanceof OWLLiteral) {
				OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
				label_class = literal.getLiteral().trim();
			}
		}
		return label_class;
	}
	
	/**
	 * Write the model into a file.
	 * 
	 * @param fileOut The file that will contain the model of the patient.
	 * @return		It returns the document IRI or null if the model could not be saved.
	 * */
	public IRI writeModel(String fileOut) {
		// Save to RDF/XML
		try {
			File output = File.createTempFile(fileOut, ".owl");
			IRI documentIRI = IRI.create(output);
			reasoner_manager.saveOntology(reasoner_ontology, documentIRI);
			return documentIRI;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	/**
	 * Write the model into a OutputStream.
	 * 
	 * @param output	The output stream where the ontology will be printed.
	 * */
	public void writeModel(OutputStream output) {
		// Save to RDF/XML
		try {
			reasoner_manager.saveOntology(reasoner_ontology, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * It provides the information that is associated with every CDS rules in the model for every drug.
	 * 
	 * @return The list of drugs and their related recommendations for the patient.	[0] cds_message; [1] recommendation_importance; [2] source; [3] rule_URL;
	 * */
	public HashMap<String, ArrayList<String[]>> obtainDrugRecommendations() {
		HashMap<String, ArrayList<String[]>> list_recommendations = new HashMap<String, ArrayList<String[]>>();
				
		RELReasoner local_reasoner = new RELReasonerFactory().createReasoner(reasoner_ontology);
		local_reasoner.precomputeInferences();
				
		OWLDataFactory factory = reasoner_manager.getOWLDataFactory();
		OWLNamedIndividual patient = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));// We obtain the patient instance
		OWLAnnotationProperty ann_relevant_for = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#relevant_for"));
		OWLAnnotationProperty ann_cds_message = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#CDS_message"));
		OWLAnnotationProperty ann_criteria = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#criteria_syntax"));
		OWLAnnotationProperty ann_gene_symbol = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#symbol_of_associated_gene"));
		OWLAnnotationProperty ann_recommendation_importance = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#recommendation_importance"));
		OWLAnnotationProperty ann_source = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#source"));
				
		if (patient != null) {
			NodeSet<OWLClass> list_types = local_reasoner.getTypes(patient, false);
			for (OWLClass type : list_types.getFlattened()) {
				String drug_name = "";
				for (OWLAnnotation annotation : type.getAnnotations(reasoner_ontology, ann_relevant_for)) {
					IRI drug_IRI = IRI.create(annotation.getValue().toString());
					OWLClass drug_class = factory.getOWLClass(drug_IRI);
					if (drug_class != null) {
						Set<OWLAnnotation> listLabels = drug_class.getAnnotations(reasoner_ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
						for (OWLAnnotation labelAnn : listLabels) {
							if (labelAnn.getValue() instanceof OWLLiteral) {
								OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
								drug_name = literal.getLiteral().trim().toLowerCase();
							}
						}
						break;
					}
				}
				
				if (!drug_name.isEmpty()) {
					
					String cds_message = "";
					for (OWLAnnotation annotation : type.getAnnotations(reasoner_ontology, ann_cds_message)) {
						if (annotation.getValue() instanceof OWLLiteral) {
							OWLLiteral rule_message = (OWLLiteral) annotation.getValue();
							cds_message = rule_message.getLiteral();
							break;
						}
					}
					
					String recommendation_importance = "";
					for (OWLAnnotation annotation : type.getAnnotations(reasoner_ontology, ann_recommendation_importance)) {
						if (annotation.getValue() instanceof OWLLiteral) {
							OWLLiteral rule_importance = (OWLLiteral) annotation.getValue();
							recommendation_importance = rule_importance.getLiteral();
							break;
						}
					}
					
					String source = "";
					for (OWLAnnotation annotation : type.getAnnotations(reasoner_ontology, ann_source)) {
						if (annotation.getValue() instanceof OWLLiteral) {
							OWLLiteral rule_source = (OWLLiteral) annotation.getValue();
							source = rule_source.getLiteral();
							break;
						}
					}
					
					String rule_URL = "";
					for (OWLAnnotation annotation : type.getAnnotations(reasoner_ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_SEE_ALSO.getIRI()))) {
						if (annotation.getValue() instanceof OWLLiteral) {
							OWLLiteral rule_source_URL = (OWLLiteral) annotation.getValue();
							rule_URL = rule_source_URL.getLiteral();
							break;
						}
					}
					
					String[] rule_attr = {cds_message,recommendation_importance,source,rule_URL};
					if (list_recommendations.containsKey(drug_name)) {
						ArrayList<String[]> list_messages = list_recommendations.get(drug_name);
						list_messages.add(rule_attr);
					} else {
						ArrayList<String[]> list_messages = new ArrayList<String[]>();
						list_messages.add(rule_attr);
						list_recommendations.put(drug_name, list_messages);
					}
					
					
				} else {
					String criteriaSyntax = "";
					for (OWLAnnotation annotation : type.getAnnotations(reasoner_ontology, ann_criteria)) {
						if (annotation.getValue() instanceof OWLLiteral) {
							OWLLiteral val = (OWLLiteral) annotation.getValue();
							criteriaSyntax = val.getLiteral();
							break;
						}
					}
					if (!criteriaSyntax.isEmpty()) {
						Iterator<OWLClassExpression> list_superclasses = type.getSuperClasses(reasoner_ontology).iterator();
						while(list_superclasses.hasNext()){
							OWLClassExpression oce = list_superclasses.next();
							if(oce.isAnonymous()) continue;
							OWLClass superclass = oce.asOWLClass();
							boolean insert = false;
							for (OWLAnnotation annotation : superclass.getAnnotations(reasoner_ontology, ann_gene_symbol)) {
								if (annotation.getValue() instanceof OWLLiteral) {
									OWLLiteral val = (OWLLiteral) annotation.getValue();
									String gene_symbol = val.getLiteral();
									insert = true;
									if (list_recommendations.containsKey("raw_data")) {
										ArrayList<String[]> list_data = list_recommendations.get("raw_data");
										//if (!list_data.contains(gene_symbol+ ": " + criteriaSyntax)) {
											String[] description_raw_data = {gene_symbol + ": "+ criteriaSyntax};
											list_data.add(description_raw_data);
										//}
									} else {
										ArrayList<String[]> list_messages = new ArrayList<String[]>();
										String[] description_raw_data = {gene_symbol + ": "+ criteriaSyntax};
										list_messages.add(description_raw_data);
										list_recommendations.put("raw_data", list_messages);
									}
								}
							}
							if (!insert) {
								if (list_recommendations.containsKey("raw_data")) {
									ArrayList<String[]> list_data = list_recommendations.get("raw_data");
									//if (!list_data.contains(": "+ criteriaSyntax)) {
										String[] description_raw_data = {": " + criteriaSyntax};
										list_data.add(description_raw_data);
									//}
								} else {
									ArrayList<String[]> list_messages = new ArrayList<String[]>();
									String[] description_raw_data = {": " + criteriaSyntax};
									list_messages.add(description_raw_data);
									list_recommendations.put("raw_data", list_messages);
								}
							}
						}
					} else {
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
										label = literal.getLiteral().trim().toLowerCase();
									}
								}

								if (list_recommendations.containsKey("inferred_alleles")) {
									ArrayList<String[]> list_messages = list_recommendations.get("inferred_alleles");
									
									//if (!list_messages.contains(label)) {
										String[] description_inferred_alleles = {label};
										list_messages.add(description_inferred_alleles);
									//}
								} else {
									ArrayList<String[]> list_messages = new ArrayList<String[]>();
									String[] description_inferred_alleles = {label};
									list_messages.add(description_inferred_alleles);
									list_recommendations.put("inferred_alleles", list_messages);
								}
								break;
							}
						}
					}
				}
			}
		}
		
		return list_recommendations;
	}

	
	/**
	 * Obtain the sorted list of rsids.
	 * 
	 * @return	The sorted list of rsids from the ontology model.
	 * */
	public ArrayList<String> getSimplifiedListRsids() {
		ArrayList<String> list_rsids = new ArrayList<String>();

		HashMap<Integer, String> results = new HashMap<Integer, String>();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLAnnotationProperty ann_rank = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#rank"));
		OWLAnnotationProperty ann_rsid = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rsid"));
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		
		Iterator<OWLClassExpression> list_marker = human_with_genotype_marker.getSubClasses(ontology).iterator();
		while(list_marker.hasNext()){
			OWLClass clase = list_marker.next().asOWLClass();
			String rank = "";
			for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_rank)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					rank = val.getLiteral();
					break;
				}
			}
			if (rank == null || rank.isEmpty())
				continue;
			int rank_int = -1;
			try {
				rank_int = Integer.parseInt(rank);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				continue;
			}

			String rsid = "";
			for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_rsid)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					rsid = val.getLiteral();
					break;
				}
			}
			if (rsid == null || rsid.isEmpty())
				continue;

			results.put(rank_int, rsid);
		}

		// Sort the list of markers by their rank number.
		for (int key = 0; !results.isEmpty(); key++) {
			if (results.containsKey(key)) {
				String genotype = results.get(key);
				list_rsids.add(genotype);
				results.remove(key);
			}
		}

		return list_rsids;
	}

	
	/**
	 * Obtain the sorted list of polymorphisms.
	 * 
	 * @return	The list of sorted polymorphisms. 
	 * */
	public ArrayList<String> getSimplifiedListPolymorphisms() {
		ArrayList<String> list_polymorphism = new ArrayList<String>();

		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass human_with_genetic_polymorphism = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genetic_polymorphism"));

		Iterator<OWLClassExpression> list_marker = human_with_genetic_polymorphism.getSubClasses(ontology).iterator();
		while(list_marker.hasNext()){
			OWLClass clase = list_marker.next().asOWLClass();
			String name = clase.getIRI().toString();
			name = name.substring(name.indexOf("_with_") + 6);
			boolean inserted = false;
			for (int i = 0; i < list_polymorphism.size(); i++) {
				if (list_polymorphism.get(i).compareTo(name) >= 0) {
					list_polymorphism.add(i, name);
					inserted = true;
					break;
				}
			}
			if (!inserted)
				list_polymorphism.add(name);
		}

		return list_polymorphism;
	}

	
	/**
	 * Obtain the sorted list of rules.
	 * 
	 * @return	Sorted list of rules in the ontology.
	 * */
	public ArrayList<String> getSimplifiedListRules() {
		ArrayList<String> list_rules = new ArrayList<String>();

		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass human_triggering_CDS_rule = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_triggering_CDS_rule"));
		
		Iterator<OWLClassExpression> list_marker = human_triggering_CDS_rule.getSubClasses(ontology).iterator();
		while(list_marker.hasNext()){
			OWLClass clase = list_marker.next().asOWLClass();
			String name = clase.getIRI().toString();
			name = name.substring(name.indexOf("rule_"));
			int position = Integer.parseInt(name.substring(5));
			boolean inserted = false;
			for (int i = 0; i < list_rules.size(); i++) {
				String name2 = list_rules.get(i);
				int position2 = Integer.parseInt(name2.substring(5));
				if (position2 >= position) {
					list_rules.add(i, name);
					inserted = true;
					break;
				}
			}
			if (!inserted) list_rules.add(name);
		}

		return list_rules;
	}
	
	
	/** 
	 * Calculate the statistics related to SNPs, polymorphisms, and rules that are associated and inferred to the patient in the ontology.
	 * 
	 * @param list_snp	The sorted list of SNPs defined in the ontology.
	 * @param list_poly	The sorted list of polymorphisms defined in the ontology.
	 * @param list_rule	The sorted list of rules defined in the ontology.
	 * 
	 * @return The list of the resulting statistics for SNPs, polymorphisms and rules.
	 * @throws NotInitializedPatientsGenomicDataException 
	 * */
	public ArrayList<String> getStatistics(ArrayList<String> list_snp, ArrayList<String> list_poly, ArrayList<String> list_rule) throws NotInitializedPatientsGenomicDataException {
		if(reasoner_manager == null || reasoner_ontology==null){
			throw new NotInitializedPatientsGenomicDataException("ERROR: The patient's genomic data has not been proceseed yet!");
		}
		
		ArrayList<String> results = new ArrayList<String>(list_snp.size() + list_poly.size() + list_rule.size());
		for (int i = 0; i < (list_snp.size() + list_poly.size() + list_rule.size()); i++) {
			results.add("");
		}
		

		RELReasoner local_reasoner = new RELReasonerFactory().createReasoner(reasoner_ontology);
		local_reasoner.precomputeInferences();

		OWLDataFactory factory = reasoner_manager.getOWLDataFactory();
		OWLNamedIndividual patient = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));// We obtain the patient instance
		OWLAnnotationProperty ann_relevant_for = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#relevant_for"));
		OWLAnnotationProperty ann_criteria = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#criteria_syntax"));

		if (patient != null) {
			NodeSet<OWLClass> list_types = local_reasoner.getTypes(patient, false);
			for (OWLClass type : list_types.getFlattened()) {
				// FIND RULES TRIGGERED
				Set<OWLAnnotation> match = type.getAnnotations(local_reasoner.getRootOntology(), ann_relevant_for);
				if (match != null && !match.isEmpty()) {
					String rule_name = type.getIRI().toString();
					rule_name = rule_name.substring(rule_name.indexOf("_rule") + 1);
					for (int i = 0; i < list_rule.size(); i++) {
						if (rule_name.equals(list_rule.get(i))) {
							results.set(i + list_snp.size() + list_poly.size(), "1");
							break;
						}
					}
					continue;
				}

				match = type.getAnnotations(local_reasoner.getRootOntology(), ann_criteria);
				if (match != null && !match.isEmpty()) {
					String snp_name = type.getIRI().toString();
					snp_name = snp_name.substring(snp_name.indexOf("_rs") + 1);
					String rsid = snp_name.substring(0, snp_name.indexOf("_variant"));
					for (int i = 0; i < list_snp.size(); i++) {
						if (rsid.equals(list_snp.get(i))) {
							results.set(i, snp_name);
							break;
						}
					}
					continue;
				}

				NodeSet<OWLClass> list_superclasses = local_reasoner.getSuperClasses(type, true);
				for (OWLClass superclass : list_superclasses.getFlattened()) {
					if (superclass.getIRI().toString().contains("human_with_genetic_polymorphism")) {
						String poly_name = type.getIRI().toString();
						poly_name = poly_name.substring(poly_name.indexOf("_with_") + 6);
						for (int i = 0; i < list_poly.size(); i++) {
							if (poly_name.equals(list_poly.get(i))) {
								results.set(i + list_snp.size(), "1");
								break;
							}
						}

						break;
					}
				}
			}
		}

		return results;
	}
	
	
	/**
	 * It tests if the populated ontology of the patient is consistent and print on standard output the types of patient instance of the model.
	 * */
	public void testInconsistencies() throws NotInitializedPatientsGenomicDataException{
		if(reasoner_manager == null || reasoner_ontology==null){
			throw new NotInitializedPatientsGenomicDataException("ERROR: The patient's genomic data has not been proceseed yet!");
		}
		
		RELReasoner			local_reasoner	= new RELReasonerFactory().createReasoner(reasoner_ontology);
		local_reasoner.precomputeInferences();
		
		OWLDataFactory 			factory				= reasoner_manager.getOWLDataFactory();
		OWLNamedIndividual		patient				= factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));//We obtain the patient instance
		NodeSet<OWLClass> list_types = local_reasoner.getTypes(patient, false);
		System.out.println("Patient types:");
		for(OWLClass type: list_types.getFlattened()){
    		System.out.println("\t"+type.getIRI());
    	}
	}
}
