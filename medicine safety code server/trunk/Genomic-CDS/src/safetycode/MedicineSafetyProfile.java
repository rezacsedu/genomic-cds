package safetycode;


import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import utils.Common;
import utils.OntologyManagement;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasoner;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;
import exception.BadFormedBase64NumberException;
import exception.VariantDoesNotMatchAnAllowedVariantException;


public class MedicineSafetyProfile {
	/** 64 base representation of the variant codes of the associated patient. */
	private String base64ProfileString;
	/** Ontology model that contains the patient conceptualization and pharmacogenomics semantic rules. */
	private OWLOntology ontology = null;
	private OWLOntologyManager manager = null;
	private OWLReasoner reasoner = null;
	private String desc="";
	private OntologyManagement om = null;
	
	public void setDesc(String desc){
		this.desc=desc;
	}
	
	
	/**
	 * Constructor of the class. It initializes the model of the
	 * pharmacogenomics dataset.
	 * */
	public MedicineSafetyProfile(String ontologyFile) {
		initializeModel(ontologyFile);
	}
	
	
	/**
	 * It initializes the model with core pharmacogenomic dataset.
	 * */
	private void initializeModel(String ontologyFile) {
		try {
			
			om = OntologyManagement.getOntologyManagement(ontologyFile);
			ontology = om.getOntology();
			manager = om.getManager();
			//manager = OWLManager.createOWLOntologyManager();
			//File file = new File(ontologyFile);
			//ontology = manager.loadOntologyFromOntologyDocument(file);
			OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
			reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ERROR in Path = " + ontologyFile);
		}
	}

	
	/**
	 * Create the patient instance in the model.
	 * @return The individual that represents the patient's profile.
	 * */
	private OWLNamedIndividual createPatient() {
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass humanClass = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human"));
		OWLNamedIndividual patientIndividual = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));
		OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(humanClass, patientIndividual);
		manager.addAxiom(ontology, classAssertion);

		return patientIndividual;
	}
	

	/**
	 * This method obtains the sorted list of markers defined in the model with
	 * its associated annotations bit_lenth, orientation and the associated
	 * annotations of its VCF reference variant for criteria syntax and bit code.
	 * 
	 * @return Sorted list of the genetics markers that consists of an string array with element {rsid,bit_length,orientation,criteria_syntax,bit_code}
	 * */
	public ArrayList<String[]> getVCFReflistRsids(){
		ArrayList<String[]> listRsids = new ArrayList<String[]>();
		// Each String array of the list contains:
		// [0] -> rsid
		// [1] -> bit_length
		// [2] -> orientation
		// [3] -> criteria_syntax
		// [4] -> bit_code
		
		HashMap<Integer, String[]> results = new HashMap<Integer, String[]>();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		OWLAnnotationProperty ann_rank = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#rank"));
		NodeSet<OWLClass> list_marker = reasoner.getSubClasses(human_with_genotype_marker, true);
		for (OWLClass clase : list_marker.getFlattened()) {
			String rank = "";
			for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_rank)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					rank = val.getLiteral();
					break;
				}
			}
			if (rank == null || rank.isEmpty()){
				continue;
			}
			int rank_int = -1;
			try {
				rank_int = Integer.parseInt(rank);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				continue;
			}
					
			String bit_length = "";
			OWLAnnotationProperty ann_bit_length = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_length"));
			for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_bit_length)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					bit_length = val.getLiteral();
					break;
				}
			}
			if (bit_length == null || bit_length.isEmpty()){
				continue;
			}
			String rsid = "";
			OWLAnnotationProperty ann_rsid = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rsid"));
			for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_rsid)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					rsid = val.getLiteral();
					break;
				}
			}
			if (rsid == null || rsid.isEmpty()){
				continue;
			}
			String orientation = "";
			OWLAnnotationProperty ann_orientation = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#dbsnp_orientation_on_reference_genome"));
			for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_orientation)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					orientation = val.getLiteral();
					break;
				}
			}
			if (orientation == null || orientation.isEmpty()){
				continue;
			}
			
			String criteria_syntax	= "";
			String bit_code			= "";
						
			//Set VCF reference variant criteria_syntax and bit_code
			NodeSet<OWLClass> list_variants = reasoner.getSubClasses(clase, true);
			for (OWLClass variant_class : list_variants.getFlattened()) {
				String isVCFReference="";
				OWLAnnotationProperty ann_vcf_reference = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#vcf_format_reference"));
				for (OWLAnnotation annotation : variant_class.getAnnotations(ontology, ann_vcf_reference)) {
					if (annotation.getValue() instanceof OWLLiteral) {
						OWLLiteral val = (OWLLiteral) annotation.getValue();
						isVCFReference = val.getLiteral();
						break;
					}
				}
				if(isVCFReference.equals("true")){
					OWLAnnotationProperty ann_criteria_syntax = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#criteria_syntax"));
					for (OWLAnnotation annotation : variant_class.getAnnotations(ontology, ann_criteria_syntax)) {
						if (annotation.getValue() instanceof OWLLiteral) {
							OWLLiteral val = (OWLLiteral) annotation.getValue();
							criteria_syntax = val.getLiteral();
							break;
						}
					}
					
					OWLAnnotationProperty ann_bit_code = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_code"));
					for (OWLAnnotation annotation : variant_class.getAnnotations(ontology, ann_bit_code)) {
						if (annotation.getValue() instanceof OWLLiteral) {
							OWLLiteral val = (OWLLiteral) annotation.getValue();
							bit_code = val.getLiteral();
							break;
						}
					}
					break;
				}
			}
			
			//Set null;null criteria_syntax and bit_code
			if(criteria_syntax.isEmpty() || bit_code.isEmpty()){
				criteria_syntax = rsid + "(null;null)"; // Generate the criteria syntax of "null;null" variant of this marker
				bit_code = ""; // Generate the bit code of "null;null" variant of this marker
				int length = 2;
				try {
					length = Integer.parseInt(bit_length);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				for (int i = 0; i < length; i++) {
					bit_code += "0";
				}
			}
			String[] genotype = { rsid, bit_length, orientation, criteria_syntax, bit_code }; // create the corresponding string array of these markers
			results.put(rank_int, genotype);
		}
		
		// Sort the list of markers by their rank number.
		for (int key = 0; !results.isEmpty(); key++) {
			if (results.containsKey(key)) {
				String[] genotype = results.get(key);
				listRsids.add(genotype);
				results.remove(key);
			}
		}
		return listRsids;
	}
	
	
	
	/**
	 * This method obtains the sorted list of markers defined in the model with
	 * its associated annotations bit_lenth, orientation and the associated
	 * annotations of its null;null variant for criteria syntax and bit code.
	 * 
	 * @return Sorted list of the genetics markers that consists of an string array with element {rsid,bit_length,orientation,criteria_syntax,bit_code}
	 * */
	public ArrayList<String[]> getListRsids() {
		ArrayList<String[]> listRsids = new ArrayList<String[]>();

		// Each String array of the list contains:
		// [0] -> rsid
		// [1] -> bit_length
		// [2] -> orientation
		// [3] -> criteria_syntax
		// [4] -> bit_code

		HashMap<Integer, String[]> results = new HashMap<Integer, String[]>();

		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		OWLAnnotationProperty ann_rank = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#rank"));
		NodeSet<OWLClass> list_marker = reasoner.getSubClasses(human_with_genotype_marker, true);
		for (OWLClass clase : list_marker.getFlattened()) {
			String rank = "";
			for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_rank)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					rank = val.getLiteral();
					break;
				}
			}
			if (rank == null || rank.isEmpty())	continue;
			int rank_int = -1;
			try {
				rank_int = Integer.parseInt(rank);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				continue;
			}

			String bit_length = "";
			OWLAnnotationProperty ann_bit_length = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_length"));
			for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_bit_length)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					bit_length = val.getLiteral();
					break;
				}
			}
			if (bit_length == null || bit_length.isEmpty()) continue;

			String rsid = "";
			OWLAnnotationProperty ann_rsid = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rsid"));
			for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_rsid)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					rsid = val.getLiteral();
					break;
				}
			}
			if (rsid == null || rsid.isEmpty()) continue;

			String orientation = "";
			OWLAnnotationProperty ann_orientation = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#dbsnp_orientation_on_reference_genome"));
			for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_orientation)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					orientation = val.getLiteral();
					break;
				}
			}
			if (orientation == null || orientation.isEmpty()) continue;

			String criteria_syntax = rsid + "(null;null)"; // Generate the criteria syntax of "null;null" variant of this marker

			String bit_code = ""; // Generate the bit code of "null;null" variant of this marker
			int length = 2;
			try {
				length = Integer.parseInt(bit_length);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < length; i++) {
				bit_code += "0";
			}

			String[] genotype = { rsid, bit_length, orientation, criteria_syntax, bit_code }; // create the corresponding string array of these markers
			results.put(rank_int, genotype);
		}

		// Sort the list of markers by their rank number.
		for (int key = 0; !results.isEmpty(); key++) {
			if (results.containsKey(key)) {
				String[] genotype = results.get(key);
				listRsids.add(genotype);
				results.remove(key);
			}
		}

		return listRsids;
	}

	
	/**
	 * Get method to obtain the 64 base representation of the patient variants
	 * of the markers.
	 * 
	 * @return The string associated to the variants of the patient or null if it is not yet defined.
	 * */
	public String getBase64ProfileString() {
		if (base64ProfileString.isEmpty())
			return null;
		return base64ProfileString;
	}

	
	/**
	 * Get method to obtain the relations between criteria syntax of variants and their corresponding bit codes.
	 * 
	 * @return	A Map that relates each criteria syntax with their bit code representation in the patient profile.
	 * */
	public HashMap<String,String> getMapCriteria2Bitcode(){
		HashMap<String,String> map = new HashMap<String,String>();
		
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		OWLAnnotationProperty ann_bit_code	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_code"));
		OWLAnnotationProperty ann_criteria	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#criteria_syntax"));
		
		NodeSet<OWLClass> list_marker = reasoner.getSubClasses(human_with_genotype_marker, true);
		for (OWLClass clase : list_marker.getFlattened()) {
			NodeSet<OWLClass> list_variants = reasoner.getSubClasses(clase, true);
			for (OWLClass subClass : list_variants.getFlattened()) {
				String criteria = "";
				for (OWLAnnotation annotation : subClass.getAnnotations(ontology, ann_criteria)) {
					if (annotation.getValue() instanceof OWLLiteral) {
						OWLLiteral val = (OWLLiteral) annotation.getValue();
						criteria = val.getLiteral();
						break;
					}
				}
				if (criteria == null || criteria.isEmpty()) continue;
				
				String bit_code = "";
				for (OWLAnnotation annotation : subClass.getAnnotations(ontology, ann_bit_code)) {
					if (annotation.getValue() instanceof OWLLiteral) {
						OWLLiteral val = (OWLLiteral) annotation.getValue();
						bit_code = val.getLiteral();
						break;
					}
				}
				if (bit_code == null || bit_code.isEmpty()) continue;
				
				map.put(criteria, bit_code);
			}
		}
		
		return map;
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
		
		base64ProfileString = fp.getBase64ProfileString();
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
		
		base64ProfileString = fp.getBase64ProfileString();
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
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		OWLAnnotationProperty ann_rsid		= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rsid"));
		OWLAnnotationProperty ann_bit_code	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_code"));
    	OWLClass matchedVariantClass		= null;
    	NodeSet<OWLClass> list_marker 		= reasoner.getSubClasses(human_with_genotype_marker, true);
		
		//Seek for the corresponding marker in the ontology
		for(OWLClass clase: list_marker.getFlattened() ){
        	String literal_rsid="";
        	//Obtain the rsid of each marker
        	for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_rsid)) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    literal_rsid=val.getLiteral();
                }
            }
        	
        	if(literal_rsid!=null && literal_rsid.equalsIgnoreCase(rsid)){//Check if the literal_rsid is equal to the marker's rsid we are looking for
        		NodeSet<OWLClass> list_variants = reasoner.getSubClasses(clase, true);
        		//Seek for the variants that contain the desired bit_code
        		for(OWLClass variant: list_variants.getFlattened() ){
        			//We obtain the criteriaSyntax annotation value and compare with the given string
                	for (OWLAnnotation annotation : variant.getAnnotations(ontology, ann_bit_code)) {
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
            		manager.addAxiom(ontology,classAssertion);
            		added=true;
            	}
        	}
        }
		
		return added;
	}
		
	
	/**
	 * Create the patient model that is related to the base64Profile.
	 * 
	 * @param base64Profile Base 64 number that represent the binary codification of a patient genotype.
	 * @throws VariantDoesNotMatchAnAllowedVariantException
	 * @throws BadFormedBase64NumberException
	 * */
	public void readBase64ProfileString(String base64Profile) throws VariantDoesNotMatchAnAllowedVariantException, BadFormedBase64NumberException {
		base64ProfileString = base64Profile;
		String binaryProfile = "";

		binaryProfile = Common.convertFrom64To2(base64ProfileString);

		ArrayList<String[]> listRsids = getListRsids();

		OWLNamedIndividual patient = createPatient();

		for (int position = 0, i = 0; i < listRsids.size(); i++) {
			String[] genotype = listRsids.get(i);
			int bit_length = Integer.parseInt(genotype[1]);
			if (binaryProfile.length() < position + bit_length) {
				throw new VariantDoesNotMatchAnAllowedVariantException(
						"<p>Warning: the length of the patient profile is shorter than the defined in the model</p>");
			}
			String bit_code = binaryProfile.substring(position, position + bit_length);
			genotype[4] = bit_code;
			position += bit_length;
			if (!addVariantToPatientByBitCode(patient, genotype[0], genotype[4])) {
				throw new VariantDoesNotMatchAnAllowedVariantException(
						"<p>Warning: the genotype mark \"" + genotype[0] + "\" or its corresponding code variant were not found in the model</p>");
			}
		}
	}

	
	/**
	 * Write the model into a file.
	 * 
	 * @param fileOut The file that will contain the model of the patient.
	 * */
	public IRI writeModel(String fileOut) {
		// Save to RDF/XML
		try {
			File output = File.createTempFile(fileOut, ".owl");
			IRI documentIRI = IRI.create(output);
			System.out.println("IRI=" + documentIRI.toString());
			manager.saveOntology(ontology, documentIRI);
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
			manager.saveOntology(ontology, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * It provides the information that is associated with every CDS rules in the model for every drug.
	 * 
	 * @return The list of drugs and their related recommendations for the patient.
	 * */
	public HashMap<String, ArrayList<String>> obtainDrugRecommendations() {
		HashMap<String, ArrayList<String>> list_recommendations = new HashMap<String, ArrayList<String>>();

		RELReasoner local_reasoner = new RELReasonerFactory().createReasoner(ontology);
		local_reasoner.precomputeInferences();

		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLNamedIndividual patient = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));// We obtain the patient instance
		OWLAnnotationProperty ann_relevant_for = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#relevant_for"));
		OWLAnnotationProperty ann_cds_message = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#CDS_message"));
		OWLAnnotationProperty ann_criteria = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#criteria_syntax"));
		OWLAnnotationProperty ann_gene_symbol = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#symbol_of_associated_gene"));

		if (patient != null) {
			NodeSet<OWLClass> list_types = local_reasoner.getTypes(patient, false);
			for (OWLClass type : list_types.getFlattened()) {
				String drug_name = "";
				for (OWLAnnotation annotation : type.getAnnotations(ontology,
						ann_relevant_for)) {
					IRI drug_IRI = IRI.create(annotation.getValue().toString());
					OWLClass drug_class = factory.getOWLClass(drug_IRI);
					if (drug_class != null) {
						Set<OWLAnnotation> listLabels = drug_class.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
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
					for (OWLAnnotation annotation : type.getAnnotations(ontology, ann_cds_message)) {
						if (annotation.getValue() instanceof OWLLiteral) {
							OWLLiteral rule_message = (OWLLiteral) annotation.getValue();
							cds_message = rule_message.getLiteral();
							if (list_recommendations.containsKey(drug_name)) {
								ArrayList<String> list_messages = list_recommendations.get(drug_name);
								if (!list_messages.contains(cds_message)) {
									list_messages.add(cds_message);
								}
							} else {
								ArrayList<String> list_messages = new ArrayList<String>();
								list_messages.add(cds_message);
								list_recommendations.put(drug_name, list_messages);
							}
							break;
						}
					}
				} else {
					String criteriaSyntax = "";
					for (OWLAnnotation annotation : type.getAnnotations(ontology, ann_criteria)) {
						if (annotation.getValue() instanceof OWLLiteral) {
							OWLLiteral val = (OWLLiteral) annotation.getValue();
							criteriaSyntax = val.getLiteral();
							break;
						}
					}
					if (!criteriaSyntax.isEmpty()) {
						NodeSet<OWLClass> list_superclasses = local_reasoner.getSuperClasses(type, true);
						for (OWLClass superclass : list_superclasses.getFlattened()) {
							boolean insert = false;
							for (OWLAnnotation annotation : superclass.getAnnotations(ontology, ann_gene_symbol)) {
								if (annotation.getValue() instanceof OWLLiteral) {
									OWLLiteral val = (OWLLiteral) annotation.getValue();
									String gene_symbol = val.getLiteral();
									insert = true;
									if (list_recommendations.containsKey("raw_data")) {
										ArrayList<String> list_data = list_recommendations.get("raw_data");
										if (!list_data.contains(gene_symbol+ ": " + criteriaSyntax)) {
											list_data.add(gene_symbol + ": "+ criteriaSyntax);
										}
									} else {
										ArrayList<String> list_messages = new ArrayList<String>();
										list_messages.add(gene_symbol + ": "+ criteriaSyntax);
										list_recommendations.put("raw_data", list_messages);
									}
								}
							}
							if (!insert) {
								if (list_recommendations.containsKey("raw_data")) {
									ArrayList<String> list_data = list_recommendations.get("raw_data");
									if (!list_data.contains(": "+ criteriaSyntax)) {
										list_data.add(": " + criteriaSyntax);
									}
								} else {
									ArrayList<String> list_messages = new ArrayList<String>();
									list_messages.add(": " + criteriaSyntax);
									list_recommendations.put("raw_data", list_messages);
								}
							}
						}
					} else {
						NodeSet<OWLClass> list_superclasses = local_reasoner.getSuperClasses(type, true);
						for (OWLClass superclass : list_superclasses.getFlattened()) {
							if (superclass.getIRI().toString().contains("human_with_genetic_polymorphism")) {
								String label = "";
								Set<OWLAnnotation> listLabels = type.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
								for (OWLAnnotation labelAnn : listLabels) {
									if (labelAnn.getValue() instanceof OWLLiteral) {
										OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
										label = literal.getLiteral().trim().toLowerCase();
									}
								}

								if (list_recommendations.containsKey("inferred_alleles")) {
									ArrayList<String> list_messages = list_recommendations.get("inferred_alleles");
									if (!list_messages.contains(label)) {
										list_messages.add(label);
									}
								} else {
									ArrayList<String> list_messages = new ArrayList<String>();
									list_messages.add(label);
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
		NodeSet<OWLClass> list_marker = reasoner.getSubClasses(human_with_genotype_marker, true);

		for (OWLClass clase : list_marker.getFlattened()) {
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
		NodeSet<OWLClass> list_marker = reasoner.getSubClasses(human_with_genetic_polymorphism, true);

		for (OWLClass clase : list_marker.getFlattened()) {
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
		NodeSet<OWLClass> list_marker = reasoner.getSubClasses(human_triggering_CDS_rule, true);

		for (OWLClass clase : list_marker.getFlattened()) {
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
	 * */
	public ArrayList<String> getStatistics(ArrayList<String> list_snp, ArrayList<String> list_poly, ArrayList<String> list_rule) {

		ArrayList<String> results = new ArrayList<String>(list_snp.size() + list_poly.size() + list_rule.size());
		for (int i = 0; i < (list_snp.size() + list_poly.size() + list_rule.size()); i++) {
			results.add("");
		}

		RELReasoner local_reasoner = new RELReasonerFactory().createReasoner(ontology);
		local_reasoner.precomputeInferences();

		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLNamedIndividual patient = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));// We obtain the patient instance
		OWLAnnotationProperty ann_relevant_for = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#relevant_for"));
		OWLAnnotationProperty ann_criteria = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#criteria_syntax"));

		if (patient != null) {
			NodeSet<OWLClass> list_types = local_reasoner.getTypes(patient, false);
			for (OWLClass type : list_types.getFlattened()) {
				// FIND RULES TRIGGERED
				Set<OWLAnnotation> match = type.getAnnotations(ontology, ann_relevant_for);
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

				match = type.getAnnotations(ontology, ann_criteria);
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
	
	public void testInconsistencies(){
		RELReasoner			local_reasoner	= new RELReasonerFactory().createReasoner(ontology);
		local_reasoner.precomputeInferences();
		
		OWLDataFactory 			factory				= manager.getOWLDataFactory();
		OWLNamedIndividual		patient				= factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));//We obtain the patient instance
		NodeSet<OWLClass> list_types = local_reasoner.getTypes(patient, false);
		System.out.println("Patient types:");
		for(OWLClass type: list_types.getFlattened()){
    		System.out.println("\t"+type.getIRI());
    	}
	}
	
	public String getDesc(){
		return desc;
	}
	
}
