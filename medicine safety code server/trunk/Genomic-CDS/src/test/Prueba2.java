package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import eu.trowl.owlapi3.rel.reasoner.dl.RELReasoner;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;
import eu.trowl.owlapi3.rel.tms.model.Individual;
import exception.BadFormedBase64NumberException;
import exception.VariantDoesNotMatchAnAllowedVariantException;

import safetycode.FileParserFactory;
import safetycode.MedicineSafetyProfile;
import safetycode.MedicineSafetyProfileOptimized;
import utils.OntologyManagement;

public class Prueba2 {

	/**
	 * @param args
	 * @throws OWLOntologyCreationException 
	 * @throws Exception 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, Exception {
		String ontofile1 = "d:/MSC_classes_1.owl";
		String ontofile3 = "d:/workspace/Genomic-CDS/output_1/model_2.owl";
		String ontofile4 = "d:/workspace/Genomic-CDS/output_1/model_3.owl";
		String outputfile1 = "d:/workspace/Genomic-CDS/output_1/output_patient22.owl";
		String outputfile2 = "d:/workspace/Genomic-CDS/output_1/output_patient20.owl";
		System.out.println( "1 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		//generateNormalReasoner(ontofile3);
		//System.out.println( "2 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		//generateNonBufferingReasoner(ontofile3);
		/*MedicineSafetyProfileOptimized myProfile = new MedicineSafetyProfileOptimized(ontofile1);
		InputStream ips = new FileInputStream(new File("d:/workspace/Genomic-CDS/input/user22_file8_yearofbirth_1975_sex_XY.23andme.txt"));
		myProfile.parseFileStream(ips, FileParserFactory.FORMAT_23ANDME_FILE);
		System.out.println("base64 = "+myProfile.getBase64ProfileString());
		ips.close();
		System.out.println( "3 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		
		myProfile = new MedicineSafetyProfileOptimized(ontofile1);
		ips = new FileInputStream(new File("d:/workspace/Genomic-CDS/input/user22_file8_yearofbirth_1975_sex_XY.23andme.txt"));
		myProfile.parseFileStream(ips, FileParserFactory.FORMAT_23ANDME_FILE);
		System.out.println("base64 = "+myProfile.getBase64ProfileString());
		ips.close();
		System.out.println( "4 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		*/
		
		MedicineSafetyProfileOptimized myProfile = new MedicineSafetyProfileOptimized(ontofile1);
		InputStream ips = new FileInputStream(new File("d:/workspace/Genomic-CDS/input/user22_file8_yearofbirth_1975_sex_XY.23andme.txt"));
		myProfile.parseFileStream(ips, FileParserFactory.FORMAT_23ANDME_FILE);
		ips.close();
		String base64ProfileString = myProfile.getBase64ProfileString();
		System.out.println("base64profile="+base64ProfileString);
		//String base64ProfileString = "PmezNKF2r8geq2zSEGJw2jzLvVTrtD_q0vfK-5Arry0iIoAklxWI0bzPwTq51Y2UACw2nx2FF3SJhEn3uueQ_j4S55rDHGVU26mKAZ203z-RCqhavsFv0a5uY1y700Su7c1g80000";
		try {
			myProfile.readBase64ProfileString(base64ProfileString);
		} catch (VariantDoesNotMatchAnAllowedVariantException e) {
			throw (new ServletException( e.getMessage()));
		} catch (BadFormedBase64NumberException e) {
			throw (new ServletException( e.getMessage()));
		}
		System.out.println( "2 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		
		HashMap<String,ArrayList<String>> list_recommendations = myProfile.obtainDrugRecommendations();
		System.out.println( "3 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		System.out.println("size="+list_recommendations.size());
		/*Iterator<String> it_keys_recom = list_recommendations.keySet().iterator();
		while(it_keys_recom.hasNext()){
			String key = it_keys_recom.next();
			Iterator<String> it_recom = list_recommendations.get(key).iterator();
			while(it_recom.hasNext()){
				String value = it_recom.next();
				System.out.println("key = "+key+"->values = "+value);
			}
		}
		OutputStream outOnto = new FileOutputStream(new File(outputfile1));
		myProfile.writeModel(outOnto);
		outOnto.close();*/
		
		System.out.println( "4 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		myProfile = new MedicineSafetyProfileOptimized(ontofile1);
		ips = new FileInputStream(new File("d:/workspace/Genomic-CDS/input/user35_file210_yearofbirth_unknown_sex_XY.23andme-exome-vcf.txt"));
		myProfile.parseFileStream(ips, FileParserFactory.FORMAT_VCF_FILE);
		ips.close();
		base64ProfileString = myProfile.getBase64ProfileString();
		System.out.println("base64profile="+base64ProfileString);
		//base64ProfileString = "fnCqXIFpAajek2wyEGBk2tbLfGjuVDxa0Pmqlkktlo2sQqg6sRWI0ByPwTq51YYU2CD31xYFBZSRXkn2OWWPNj0C1551LGVU84mK2Z203zyRC0000000000000000000000000000";
		try {
			myProfile.readBase64ProfileString(base64ProfileString);
		} catch (VariantDoesNotMatchAnAllowedVariantException e) {
			throw (new ServletException( e.getMessage()));
		} catch (BadFormedBase64NumberException e) {
			throw (new ServletException( e.getMessage()));
		}
		System.out.println( "5 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		list_recommendations = myProfile.obtainDrugRecommendations();
		System.out.println( "6 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		System.out.println("size="+list_recommendations.size());
		
		
		/*it_keys_recom = list_recommendations.keySet().iterator();
		while(it_keys_recom.hasNext()){
			String key = it_keys_recom.next();
			Iterator<String> it_recom = list_recommendations.get(key).iterator();
			while(it_recom.hasNext()){
				String value = it_recom.next();
				System.out.println("key = "+key+"->values = "+value);
			}
		}
		outOnto = new FileOutputStream(new File(outputfile2));
		myProfile.writeModel(outOnto);
		outOnto.close();
		*/
		
		/*OWLOntologyManager manager_1 = OWLManager.createOWLOntologyManager();
		File file_1 = new File(ontofile1);
		OWLOntology ontology_1 = manager_1.loadOntologyFromOntologyDocument(file_1);
		
		System.out.println( "1 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		OntologyManagement om = OntologyManagement.getOntologyManagement(ontofile3);
		om.getManager().addAxioms(om.getOntology(), ontology_1.getAxioms());
		OutputStream outputStream = new FileOutputStream(new File(ontofile4));
		om.getManager().saveOntology(om.getOntology(),outputStream);
		System.out.println( "2 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		
		
		
		RELReasoner local_reasoner = om.getRELReasoner();
		getSimplifiedListRules(local_reasoner.manager,local_reasoner);
		//OutputStream outputStream = new FileOutputStream(new File(ontofile3));
		//local_reasoner.save(true, true, true, true, true, outputStream);
		
		System.out.println( "3 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		om=null;
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File(ontofile3);
		System.out.println( "4 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
		System.out.println( "5 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		manager.addAxioms(ontology, ontology_1.getAxioms());
		System.out.println( "6 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		local_reasoner = new RELReasonerFactory().createReasoner(ontology);
		System.out.println( "7 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		local_reasoner.precomputeInferences();
		getSimplifiedListRules(manager,local_reasoner);
		System.out.println( "8 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		*/
	}

	public static void generateNormalReasoner(String ontologyFile) throws OWLOntologyCreationException{
		ArrayList<String[]> listRsids = new ArrayList<String[]>();

		// Each String array of the list contains:
		// [0] -> rsid
		// [1] -> bit_length
		// [2] -> orientation
		// [3] -> criteria_syntax
		// [4] -> bit_code

		HashMap<Integer, String[]> results = new HashMap<Integer, String[]>();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File(ontologyFile);
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		
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

	}
	
	
	public static void generateNonBufferingReasoner(String ontologyFile) throws OWLOntologyCreationException{
		ArrayList<String[]> listRsids = new ArrayList<String[]>();

		// Each String array of the list contains:
		// [0] -> rsid
		// [1] -> bit_length
		// [2] -> orientation
		// [3] -> criteria_syntax
		// [4] -> bit_code

		HashMap<Integer, String[]> results = new HashMap<Integer, String[]>();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File(ontologyFile);
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		if(human_with_genotype_marker!=null){
			Iterator<OWLClassExpression> it_subclasses = human_with_genotype_marker.getSubClasses(ontology).iterator();
			while(it_subclasses.hasNext()){
				OWLClass owlclass = it_subclasses.next().asOWLClass();
				System.out.println("subclase = "+owlclass.getIRI());
			}
		}
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

	}
	
	public static ArrayList<String> getSimplifiedListRules(OWLOntologyManager manager, RELReasoner reasoner ) {
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
	
	
}
