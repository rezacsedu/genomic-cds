package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import eu.trowl.owlapi3.rel.reasoner.dl.RELReasoner;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;


/**
 * This class keeps a single instance of the base ontology in order to use it without having to load in memory one for every request.
 * @author Jose Antonio Miñarro Giménez
 * @version 1.0
 * */
public class OntologyManagement {
	/**	Singleton instance of this class */
	private static OntologyManagement singleton=null;
	/** Ontology modelo that will be always load into memory */
	private OWLOntology ontology = null;
	/** Ontology manager of the base ontology */
	private OWLOntologyManager manager = null;
	/** Source file where base ontology is stored */
	private static String ontFile=null;
	/** List of base variants when parsing a VCF file */
	private ArrayList<String[]> vcfListRsids = null;
	/** List of base variants (null;null) when parsing a 23andme file */
	private ArrayList<String[]> meListRsids = null;
	/** Map of the correspondences between criteria syntax and bit codes */
	private HashMap<String,String> criteria2Bitcode = null;
	
	
	/**
	 * Static method to obtain the singleton instance of this class.
	 * 
	 * @return		A static reference to the singleton instance of OntologyManagement class.
	 * */
	public static OntologyManagement getOntologyManagement(String ontologyFile){
		if(ontologyFile!=null && ontologyFile.equalsIgnoreCase(ontFile)||singleton==null){
			ontFile=ontologyFile;
			singleton = new OntologyManagement(ontFile);
		}
		return singleton;
	}
	
	
	/**
	 * Constructor of the class. It loads the base ontology model.
	 * 
	 * @return		Instance of the OntologyManagement class.
	 * */
	public OntologyManagement(String ontologyFile){
		try {
			manager = OWLManager.createOWLOntologyManager();
			File file = new File(ontologyFile);
			ontology = manager.loadOntologyFromOntologyDocument(file);	
						
			criteria2Bitcode	= initializedMapCriteria2Bitcode();
			vcfListRsids		= initializedVCFRefListRsids();
			meListRsids			= initializedListRsids();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ERROR in Path = " + ontologyFile);
		}
	}
	
	
	/**
	 * Get method to obtain the instance of the ontology model.
	 * 
	 * @return		The corresponding OWL ontology of the base ontological model.
	 * */
	public OWLOntology getOntology(){		
		return singleton.ontology;
	}
	
	
	/**Get method to obtain the ontology manager of the corresponding base ontological model.
	 * 
	 * @return		The ontology manager of the base ontological model.
	 * */
	public OWLOntologyManager getManager(){
		return singleton.manager;
	}
	
	
	/**
	 * It is a clone method to create a new instance of the base ontological model with its corresponding ontology manager.
	 * 	
	 * @return		The new ontology manager that is associated to the new instance model.
	 * */
	public OWLOntologyManager getNewOntologyManager(){
		OWLOntologyManager mang = null;
		try{
			mang = OWLManager.createOWLOntologyManager();
			mang.createOntology(singleton.ontology.getAxioms());
		}catch(Exception e){
			e.printStackTrace();
		}
		return mang;
	}
	
	
	/**
	 * Obtain a TrOWL reasoner from the base ontology model.
	 * 
	 * @return		A TrOWL reasoner without precomputed inferences.
	 * */
	public RELReasoner getRELReasoner(){
		RELReasoner local_reasoner = null;
		try{
			OWLOntologyManager mang = OWLManager.createOWLOntologyManager();
			OWLOntology onto = mang.createOntology(singleton.ontology.getAxioms());
			local_reasoner = new RELReasonerFactory().createReasoner(onto);
		}catch(Exception e){
			e.printStackTrace();
		}
		return local_reasoner;
	}
	
	
	/**
	 * Get Method to obtain the map between criteria syntax and bit codes.
	 * 
	 * @return		A map that indicates the links between each criteria syntax and its bit code.
	 * */
	public HashMap<String,String> getMapCriteria2Bitcode(){
		return criteria2Bitcode;
	}
	
	
	/**
	 * Get method to obtain the list of base variants when parsing a VCF file.
	 * 
	 * @return		List of base SNPs, its annotations and reference criteria syntax and bitcode. 
	 * */
	public ArrayList<String[]> getVCFRefListRsids(){
		ArrayList<String[]> listRsids = new ArrayList<String[]>();

		// Each String array of the list contains:
		// [0] -> rsid
		// [1] -> bit_length
		// [2] -> orientation
		// [3] -> criteria_syntax
		// [4] -> bit_code
		
		for(int i=0;i<vcfListRsids.size();i++){
			String[] node = vcfListRsids.get(i);
			String[] new_node = {node[0],node[1],node[2],node[3],node[4]};
			listRsids.add(new_node);
		}
		
		return listRsids;
	}
	
	
	/**
	 * Get method to obtain the list of base variants when parsing a 23andme file.
	 * 
	 * @return		List of base SNPs, its annotations and the criteria syntax and bitcode of (null;null) variants.
	 * */
	public ArrayList<String[]> getListRsids(){
		ArrayList<String[]> listRsids = new ArrayList<String[]>();

		// Each String array of the list contains:
		// [0] -> rsid
		// [1] -> bit_length
		// [2] -> orientation
		// [3] -> criteria_syntax
		// [4] -> bit_code
		
		for(int i=0;i<meListRsids.size();i++){
			String[] node =meListRsids.get(i);
			String[] new_node = {node[0],node[1],node[2],node[3],node[4]};
			listRsids.add(new_node);
		}
		
		return listRsids;
	}
	
	
	/**
	 * Method to initialize the map of criteria syntax and corresponding bit codes of each variant in the ontology.
	 * 
	 * @return	A Map that relates each criteria syntax with their bit code representation in the patient profile.
	 * */
	private HashMap<String,String> initializedMapCriteria2Bitcode(){
		HashMap<String,String> map = new HashMap<String,String>();
		
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		OWLAnnotationProperty ann_bit_code	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_code"));
		OWLAnnotationProperty ann_criteria	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#criteria_syntax"));
		
		Iterator<OWLClassExpression> list_marker = human_with_genotype_marker.getSubClasses(ontology).iterator();
		while(list_marker.hasNext()){
			OWLClass clase = list_marker.next().asOWLClass();
			Iterator<OWLClassExpression> list_variants = clase.getSubClasses(ontology).iterator();
			while(list_variants.hasNext()){
				OWLClass subClass = list_variants.next().asOWLClass();
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
	 * Method to initialize the sorted list of markers defined in the model with its associated annotations bit_lenth, orientation and the associated annotations of its VCF reference variant for criteria syntax and bit code.
	 * 
	 * @return Sorted list of the genetics markers that consists of an string array with element {rsid,bit_length,orientation,criteria_syntax,bit_code}
	 * */
	private ArrayList<String[]> initializedVCFRefListRsids(){
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
			Iterator<OWLClassExpression> list_variants = clase.getSubClasses(ontology).iterator();
			while(list_variants.hasNext()){
				OWLClass variant_class = list_variants.next().asOWLClass();
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
	 * Method to initialize the sorted list of markers defined in the model with its associated annotations bit_lenth, orientation and the associated annotations of its null;null variant for criteria syntax and bit code.
	 * 
	 * @return Sorted list of the genetics markers that consists of an string array with element {rsid,bit_length,orientation,criteria_syntax,bit_code}
	 * */
	private ArrayList<String[]> initializedListRsids() {
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
}
