import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;
import exception.BadFormedBase64NumberException;
import exception.BadFormedBinaryNumberException;
import exception.VariantDoesNotMatchAnAllowedVariantException;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;


import safetycode.PatientProfileReasoning;
import safetycode.MedicineSafetyProfileOWLAPI;
import safetycode.PatientProfileReasoningPellet;

public class test {

	
	/*private void parserOWLAPI() throws OWLOntologyCreationException, OWLOntologyStorageException{
	
        OWLOntology ontology = null;
    	OWLOntologyManager manager = null;
    	OWLDataFactory factory = null;
    	OWLReasoner trOwl	= null;
    	
    	manager=OWLManager.createOWLOntologyManager();
        
        String ontologyFile="D:/workspace/Genomic-CDS/MSC_classes.ttl";
        File inputOntologyFile = new File(ontologyFile);

        ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
        factory = manager.getOWLDataFactory();
             
        trOwl=new RELReasonerFactory().createReasoner(ontology);
		trOwl.precomputeInferences();
        boolean isConsistent = trOwl.isConsistent();
        if(isConsistent) System.out.println("La ontología es consistente");
        
        OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
        OWLDeclarationAxiom declarationAxiom = factory.getOWLDeclarationAxiom(human_with_genotype_marker);
		manager.addAxiom(ontology, declarationAxiom);
		
		OWLClass human = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human"));
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(human_with_genotype_marker,human);
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		manager.applyChange(addAxiom);
		
		NodeSet<OWLClass> list_human = trOwl.getSubClasses(human, false);
        for(OWLClass clase: list_human.getFlattened() ){
        	Set<OWLAnnotation> list_ann = clase.getAnnotations(ontology);
        	for(OWLAnnotation ann : list_ann){
				if(ann.getProperty().getIRI().toString().contains("#rank")){
					//Asociamos la clase padre a la clase hijo
					axiom = factory.getOWLSubClassOfAxiom(clase, human_with_genotype_marker);
					addAxiom = new AddAxiom(ontology, axiom);
					manager.applyChange(addAxiom);
					break;
				}
			}
    	}       
        
		list_human = trOwl.getSubClasses(human_with_genotype_marker, true);
        for(OWLClass clase: list_human.getFlattened() ){
        	Iterator<OWLClassAxiom> it = ontology.getAxioms(clase).iterator();
        	while(it.hasNext()){
				OWLClassAxiom oca = it.next();
				if(oca.toString().contains("<http://www.genomic-cds.org/ont/genomic-cds.owl#human>")){
					RemoveAxiom removeAxiom = new RemoveAxiom(ontology,oca);
					manager.applyChange(removeAxiom);
				}
			}       	
    	}
        manager.saveOntology(ontology);
	}
	
	private static void test7(){
		MedicineSafetyProfileOWLAPI msp = new MedicineSafetyProfileOWLAPI();
		String base64Profile="cCB3RLNS2vCXUgK5Gl3c2z12jrLzVjqND-AG3bH7jWhDSVG392k2SR_NWK0nzPwTq51Y27ZaVmMFKHvuUpTUrYFZYXICleZWeBgQYW--8V1GAC80Ftvio9N9piVo0I2yH0-3Y0vmFC3KG0000";
		try {
			msp.readBase64ProfileString(base64Profile);
		} catch (VariantDoesNotMatchAnAllowedVariantException e1) {
			e1.printStackTrace();
		}
		try {
			msp.calculateInferences();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		System.out.println("Execute all");
		HashMap<String,HashSet<String>> drug_recommendations = msp.obtainDrugRecommendations();
		Iterator<String> it_keys = drug_recommendations.keySet().iterator();
		while(it_keys.hasNext()){
			String key = it_keys.next();
			System.out.println(key+":");
			Iterator<String> it_messages = drug_recommendations.get(key).iterator();
			while(it_messages.hasNext()){
				System.out.println("\t"+it_messages.next());
			}
		}
	}
	
	private static void parserReasonOWLAPI() throws OWLOntologyCreationException, OWLOntologyStorageException{
		
        OWLOntology ontology = null;
    	OWLOntologyManager manager = null;
    	OWLDataFactory factory = null;
    	OWLReasoner trOwl	= null;
    	
    	manager=OWLManager.createOWLOntologyManager();
        
        String ontologyFile="C:/Users/Jose/AppData/Local/Temp/reason_model_3994847516189396849.owl";
        File inputOntologyFile = new File(ontologyFile);

        ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
        factory = manager.getOWLDataFactory();
             
        trOwl=new RELReasonerFactory().createReasoner(ontology);
		trOwl.precomputeInferences();
        boolean isConsistent = trOwl.isConsistent();
        if(isConsistent) System.out.println("La ontología es consistente");
        
        HashMap<String,HashSet<String>> list_recommendations = new HashMap<String,HashSet<String>>();
		
		OWLNamedIndividual patient				= factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#patient"));//We obtain the patient instance
		OWLAnnotationProperty ann_relevant_for	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#relevant_for"));
		OWLAnnotationProperty ann_cds_message	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#CDS_message"));
        if(patient!=null){
        	NodeSet<OWLClass> list_types = trOwl.getTypes(patient, false);
        	for(OWLClass type: list_types.getFlattened() ){
        		String drug_name="";
        		for (OWLAnnotation annotation : type.getAnnotations(ontology, ann_relevant_for)) {
        			IRI drug_IRI = IRI.create(annotation.getValue().toString());
        			OWLClass drug_class = factory.getOWLClass(drug_IRI);
        			if(drug_class!=null){
        				Set<OWLAnnotation> listLabels = drug_class.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
        				for(OWLAnnotation labelAnn: listLabels){
        					if (labelAnn.getValue() instanceof OWLLiteral) {
        						OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
        						drug_name = literal.getLiteral().trim().toLowerCase();
        					}
        				}
     	                System.out.println("\t\t\tdrug="+drug_name);
     	                break;
        			}
    	        }
        		
        		if(!drug_name.isEmpty()){
        			String cds_message = "";
        			for (OWLAnnotation annotation : type.getAnnotations(ontology, ann_cds_message)) {
        	            if (annotation.getValue() instanceof OWLLiteral) {
        	                OWLLiteral rule_message = (OWLLiteral) annotation.getValue();
        	                cds_message = rule_message.getLiteral();
        	                if(list_recommendations.containsKey(drug_name)){
        	                	HashSet<String> list_messages = list_recommendations.get(drug_name);
        	                	list_messages.add(cds_message);
        	                }else{
        	                	HashSet<String> list_messages = new HashSet<String>();
        	                	list_messages.add(cds_message);
        	                	list_recommendations.put(drug_name, list_messages);
        	                }
        	                break;
        	            }
        	        }
        		}	
        	}
        }
        
		System.out.println("Resultados test 6");
		Iterator<String> it_keys = list_recommendations.keySet().iterator();
		while(it_keys.hasNext()){
			String key = it_keys.next();
			System.out.println(key);
			Iterator<String> it_sms = list_recommendations.get(key).iterator();
			while(it_sms.hasNext()){
				System.out.println("\t"+it_sms.next());
			}
		}
	}
	
	
	
	private static void test8(){
		
		HashMap<String,HashSet<String>> list_recommendations = new HashMap<String,HashSet<String>>();
		PatientProfileReasoning ppr = new PatientProfileReasoning(list_recommendations,IRI.create("file:/C:/Users/Jose/AppData/Local/Temp/reason_model_3774406547210853476.owl"));
		try{
			ppr.start();
			ppr.join();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Resultados test 8");
		Iterator<String> it_keys = list_recommendations.keySet().iterator();
		while(it_keys.hasNext()){
			String key = it_keys.next();
			System.out.println(key);
			Iterator<String> it_sms = list_recommendations.get(key).iterator();
			while(it_sms.hasNext()){
				System.out.println("\t"+it_sms.next());
			}
		}
	}
	
	
	private static void test10(){
		
		HashMap<String,HashSet<String>> list_recommendations = new HashMap<String,HashSet<String>>();
		PatientProfileReasoningPellet ppr = new PatientProfileReasoningPellet(list_recommendations,IRI.create("file:/C:/Users/Jose/AppData/Local/Temp/reason_model_3774406547210853476.owl"));
		try{
			ppr.start();
			ppr.join();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Resultados test 8");
		Iterator<String> it_keys = list_recommendations.keySet().iterator();
		while(it_keys.hasNext()){
			String key = it_keys.next();
			System.out.println(key);
			Iterator<String> it_sms = list_recommendations.get(key).iterator();
			while(it_sms.hasNext()){
				System.out.println("\t"+it_sms.next());
			}
		}
	}
	
	private static void test9(){
		HashMap<String,HashSet<String>> list_recommendations = new HashMap<String,HashSet<String>>();
		
		OWLOntology ontology = null;
    	OWLOntologyManager manager = null;
    	OWLReasoner reasoner	= null;
    	
    	 
        String ontologyFile="D:/workspace/Genomic-CDS/genomic-cds-demo.owl";
        File inputOntologyFile = new File(ontologyFile);
        manager=OWLManager.createOWLOntologyManager();
        try {
			ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
        
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	    reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
                
        reasoner=new RELReasonerFactory().createReasoner(ontology);
		reasoner.precomputeInferences();
        boolean isConsistent = reasoner.isConsistent();
        if(isConsistent) System.out.println("La ontología es consistente");
        
        OWLDataFactory factory = manager.getOWLDataFactory();
		OWLNamedIndividual patient				= factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));//We obtain the patient instance
		OWLAnnotationProperty ann_relevant_for	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#relevant_for"));
		OWLAnnotationProperty ann_cds_message	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#CDS_message"));
        if(patient!=null){
        	NodeSet<OWLClass> list_types = reasoner.getTypes(patient, false);
        	for(OWLClass type: list_types.getFlattened() ){
        		String drug_name="";
        		for (OWLAnnotation annotation : type.getAnnotations(ontology, ann_relevant_for)) {
        			IRI drug_IRI = IRI.create(annotation.getValue().toString());
        			OWLClass drug_class = factory.getOWLClass(drug_IRI);
        			if(drug_class!=null){
        				Set<OWLAnnotation> listLabels = drug_class.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
        				for(OWLAnnotation labelAnn: listLabels){
        					if (labelAnn.getValue() instanceof OWLLiteral) {
        						OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
        						drug_name = literal.getLiteral().trim().toLowerCase();
        					}
        				}
     	                break;
        			}
    	        }
        		
        		if(!drug_name.isEmpty()){
        			String cds_message = "";
        			for (OWLAnnotation annotation : type.getAnnotations(ontology, ann_cds_message)) {
        	            if (annotation.getValue() instanceof OWLLiteral) {
        	                OWLLiteral rule_message = (OWLLiteral) annotation.getValue();
        	                cds_message = rule_message.getLiteral();
        	                if(list_recommendations.containsKey(drug_name)){
        	                	HashSet<String> list_messages = list_recommendations.get(drug_name);
        	                	list_messages.add(cds_message);
        	                }else{
        	                	HashSet<String> list_messages = new HashSet<String>();
        	                	list_messages.add(cds_message);
        	                	list_recommendations.put(drug_name, list_messages);
        	                }
        	                break;
        	            }
        	        }
        		}	
        	}
        }
        
        System.out.println("Resultados test 9");
		Iterator<String> it_keys = list_recommendations.keySet().iterator();
		while(it_keys.hasNext()){
			String key = it_keys.next();
			System.out.println(key);
			Iterator<String> it_sms = list_recommendations.get(key).iterator();
			while(it_sms.hasNext()){
				System.out.println("\t"+it_sms.next());
			}
		}
        
	}
	
	private static void test11(){
		String ontologyFile="file:/D:/workspace/Genomic-CDS/genomic-cds-demo.owl";
        HashMap<String,HashSet<String>> list_recommendations = new HashMap<String,HashSet<String>>();
        IRI documentIRI = IRI.create(ontologyFile);
		
		PatientProfileReasoning ppr = new PatientProfileReasoning(list_recommendations,documentIRI);
        try{
        	ppr.start();
        	System.out.println("Empezamos a ejecutar el código");
        	ppr.join();
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        System.out.println("Resultados test 11");
		Iterator<String> it_keys = list_recommendations.keySet().iterator();
		while(it_keys.hasNext()){
			String key = it_keys.next();
			System.out.println(key);
			Iterator<String> it_sms = list_recommendations.get(key).iterator();
			while(it_sms.hasNext()){
				System.out.println("\t"+it_sms.next());
			}
		}
	}*/
	
	
	private static void test12(){
		String ontologyFile="file:/D:/workspace/Genomic-CDS/genomic-cds-demo.owl";
        MedicineSafetyProfileOWLAPI msp = new MedicineSafetyProfileOWLAPI(ontologyFile);
        HashMap<String,HashSet<String>> list_recommendations = msp.obtainDrugRecommendations();
		
		System.out.println("Resultados test 11");
		Iterator<String> it_keys = list_recommendations.keySet().iterator();
		while(it_keys.hasNext()){
			String key = it_keys.next();
			System.out.println(key);
			Iterator<String> it_sms = list_recommendations.get(key).iterator();
			while(it_sms.hasNext()){
				System.out.println("\t"+it_sms.next());
			}
		}
	}
	
	
	/**
	 * @param args
	 * @throws BadFormedBase64NumberException 
	 * @throws BadFormedBinaryNumberException 
	 * @throws OWLOntologyCreationException 
	 * @throws OWLOntologyStorageException 
	 */
	public static void main(String[] args) throws BadFormedBase64NumberException, BadFormedBinaryNumberException, OWLOntologyCreationException, OWLOntologyStorageException {
		Runtime rt = Runtime.getRuntime();
        long totalMem = rt.totalMemory();
        long maxMem = rt.maxMemory();
        long freeMem = rt.freeMemory();
        double megs = 1048576.0;

        System.out.println ("Total Memory: " + totalMem + " (" + (totalMem/megs) + " MiB)");
        System.out.println ("Max Memory:   " + maxMem + " (" + (maxMem/megs) + " MiB)");
        System.out.println ("Free Memory:  " + freeMem + " (" + (freeMem/megs) + " MiB)");
		
        /*TEST 12*/
        test12();
        
        /*TEST 11*/
        //test11();
        
        /*TEST 10*/
        //test10();
        
        /*TEST 9*/
        //test9();
        
        /*TEST 8*/
        //test8();
        
        /*TEST 6*/
        //test7();
        
        
        /*TEST 6*/
        //parserReasonOWLAPI();
        
        
        
		/*TEST 5
        OWLOntology ontology = null;
    	OWLOntologyManager manager = null;
    	OWLDataFactory factory = null;
    	OWLReasoner trOwl	= null;
    	
    	// Primero, creamos un objeto OWLOntologyManager. Este gestor nos permitirá 
    	// cargar y guardar la ontogía. 
        manager=OWLManager.createOWLOntologyManager();
        
        // Ahora, creamos el objeto File con la ruta que contiene la ontologia para el 
        // Matching de Expertos.
        String ontologyFile="D:/workspace/Genomic-CDS/MSC_classes.ttl";
        File inputOntologyFile = new File(ontologyFile);

        // Utilizamos la librería OWL API para cargar la ontología. 
        //ontology = manager.loadOntologyFromOntologyDocument(IRI.create(MEOntologyURI));
        ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
        factory = manager.getOWLDataFactory();
             
        //Calculamos el modelo inferido con el razonador Hermit
        trOwl=new RELReasonerFactory().createReasoner(ontology);
		trOwl.precomputeInferences();
        boolean isConsistent = trOwl.isConsistent();
        if(isConsistent) System.out.println("La ontología es consistente");
        
        OWLNamedIndividual patient = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#patient"));
        HashSet<String> list_rules = new HashSet<String>();
        OWLClass root_rule = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_triggering_CDS_rule"));
        if(root_rule!=null){
        	NodeSet<OWLClass> rule_classes = trOwl.getSubClasses(root_rule, true);
        	for(OWLClass rule: rule_classes.getFlattened() ){
        		list_rules.add(rule.getIRI().toString());
        	}
        }
        HashSet<String> list_polymorphism = new HashSet<String>();
        OWLClass root_polymorphism = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genetic_polymorphism"));
        if(root_polymorphism!=null){
        	NodeSet<OWLClass> polymorphism_classes = trOwl.getSubClasses(root_polymorphism, true);
        	for(OWLClass polymorphism: polymorphism_classes.getFlattened() ){
        		list_polymorphism.add(polymorphism.getIRI().toString());
        	}
        }
        if(patient!=null){
        	System.out.println("Patient="+patient.getIRI());
        	NodeSet<OWLClass> types = trOwl.getTypes(patient, false);
        	for(OWLClass type: types.getFlattened() ){
        		if(list_rules.contains(type.getIRI().toString())){
        			System.out.println("RULE="+type.getIRI());
        			Set<OWLAnnotation> list_ann = type.getAnnotations(ontology);
        			String drug="";
        			String rule_comment="";
        			for(OWLAnnotation ann : list_ann){
        				if(ann.getProperty().getIRI().toString().contains("#relevant_for")){
        					String aux = ann.getValue().toString();
        					drug=aux.substring(aux.indexOf("#")+1);
        				}
        				if(ann.getProperty().getIRI().toString().contains("#CDS_message")){
        					String aux = ann.getValue().toString();
        					rule_comment=aux.replaceAll("\"", "");
        				}
        			}
        			System.out.println("RULE->"+type.getIRI());
        			System.out.println("\tdrug="+drug);
        			System.out.println("\tcomment="+rule_comment);
        		}
        		if(list_polymorphism.contains(type.getIRI().toString())){
        			System.out.println("POLY="+type.getIRI());
        		}
        	}
        }*/
        
        
        /*TEST 4
		InputStream my23AndMeFileStream=null;
		String fileName="D:/workspace/Genomic-CDS/1097.23andme.564";
		String fileOut ="D:/workspace/Genomic-CDS/model_1097.23andme.564.owl";
		String report="";
		MedicineSafetyProfile msp = null;
		msp = new MedicineSafetyProfile();
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		report = msp.read23AndMeFileStream(my23AndMeFileStream, Common.DBSNP_ORIENTATION);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:/workspace/Genomic-CDS/report.txt"));
			bw.write(report+"\n");
			bw.write(msp.getBase64ProfileString()+"\n");
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(msp.getBase64ProfileString());
		
		OntModel reasonerModel = ModelFactory.createOntologyModel(TrOWLJenaFactory.THE_SPEC,msp.getRDFModel());
		//OntModel reasonerModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF,msp.getRDFModel());
		Individual indi = reasonerModel.getIndividual("http://www.genomic-cds.org/ont/genomic-cds.owl#patient");
		ExtendedIterator<OntClass> itClasses = indi.listOntClasses(false);
		while(itClasses.hasNext()){
			OntClass superClass = itClasses.next();
			if(superClass.isAnon()){
				System.out.println("Annon = "+superClass.toString());
				ExtendedIterator<OntClass> subClasses = superClass.listSubClasses(false);
				while(subClasses.hasNext()){
					OntClass subClass = subClasses.next();
					System.out.println("subClass="+subClass);
				}
				ExtendedIterator<? extends OntResource> itIndi = superClass.listInstances(false);
				while(itIndi.hasNext()){
					OntResource or = itIndi.next();
					System.out.println("instance="+or.toString());
				}
				ExtendedIterator<OntProperty> itProp = superClass.listDeclaredProperties(false);
				while(itProp.hasNext()){
					OntProperty op = itProp.next();
					System.out.println("property="+op.toString());
				}
			}
			System.out.println(superClass.toString());
		}
		msp.closeModel(fileOut);
		*/
		
		
		/* TEST 3
		System.out.println(Common.convertFrom64To2("cCB3RLNS2vCXUgK5Gl3c2z12jrLzVjqND-AG3bH7jWhDSVG392k2SR_NWK0nzPwTq51Y27ZaVmMFKHvuUpTUrYFZYXICleZWeBgQYW--8V1GAC80Ftvio9N9piVo0I2yH0-3Y0vmFC3KG0000"));
		System.out.println(Common.convertFrom2To64("100110001100001011000011011011010101010111011100000010111001001100100001011110101011110100000101010000101111000011100110000010111101000001000010101101110101010101111101011111101101110100010111001101111110001010010000000011100101010001000111101101100000101011001101011100011111010000000011001001000010101110000010011100011011111111010111100000010100000000110001111101011001111010011101110100000101000001100010000010000111100011100100011111110000010110001111010100010001111001111000011110110011011101011110110101100010001111100011100010100001010010001100101111101000100011100000101000001011101010011010100010100000111110111110001000011111000001010000001010001100001000000000001111110111111001101100110010001001010111001001110011101100011111110010000000010010000010111100010001000000111110000011100010000000111001110000001111001100000011010100010000000000000000000000000000"));
		*/
		
		/*TEST 2
		InputStream my23AndMeFileStream=null;
		String fileName="D:/workspace/Genomic-CDS/1097.23andme.564";
		String report="";
		MedicineSafetyProfile msp = null;
		
		msp = new MedicineSafetyProfile();
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		report = msp.read23AndMeFileStream(my23AndMeFileStream, Common.DBSNP_ORIENTATION);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:/workspace/Genomic-CDS/report.txt"));
			bw.write(report+"\n");
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(msp.getBase64ProfileString());
		msp.closeModel(null);
		
		msp = new MedicineSafetyProfile();
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		report = msp.read23AndMeFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:/workspace/Genomic-CDS/report2.txt"));
			bw.write(report+"\n");
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(msp.getBase64ProfileString());
		msp.closeModel(null);*/
		
		
		/*TEST 1
		OntModel model = ModelFactory.createOntologyModel();
		InputStream in = FileManager.get().open("D:/workspace/Genomic-CDS/MSC_classes.ttl");
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + "D:/workspace/Genomic-CDS/MSC_classes.ttl" + " not found");
		}
		RDFReader reader = model.getReader("N3");
		reader.read(model, in,"http://www.genomic-cds.org/ont/genomic-cds.owl");
		
				
		String queryString = Common.SPARQL_NAME_SPACES
				+ " SELECT DISTINCT ?item ?rsid ?rank ?position ?bit_length " 
				+ " WHERE { " 
				+ "     ?item pgx:rsid ?rsid . "
				+ "		?item sc:rank ?rank . " 
				+ "		?item sc:position_in_base_2_string ?position . " 
				+ "		?item sc:bit_length ?bit_length . " 
				+ " } ORDER BY ?rank ";

		QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), model);

		try {
			int tam=0;
			AnnotationProperty ap1 = model.getAnnotationProperty("http://www.genomic-cds.org/ont/MSC_classes.owl#criteria_syntax");
			AnnotationProperty ap2 = model.getAnnotationProperty("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_code");
			
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln	= results.nextSolution();
				Resource res		= soln.getResource("item");
				Literal rsid		= soln.getLiteral("rsid");
				Literal rank 		= soln.getLiteral("rank");
				Literal position	= soln.getLiteral("position");
				Literal bit_length	= soln.getLiteral("bit_length");
				
				System.out.println(rsid.getString()+" -> ["+rank.getInt()+"] = "+bit_length.getInt()+" -> position = "+position.getInt());
				tam+=bit_length.getInt();
				if(res.canAs(OntClass.class)){
					OntClass ontClass = res.as(OntClass.class);
					ExtendedIterator<OntClass> eioc = ontClass.listSubClasses();
					while(eioc.hasNext()){
						OntClass oc = eioc.next();
						RDFNode criteria_syntax	= oc.getPropertyValue(ap1);
						if(criteria_syntax==null) continue;
						RDFNode bit_code 		= oc.getPropertyValue(ap2);
						if(bit_code==null) continue;
						
						//System.out.print("\t "+criteria_syntax.toString()+" -> ");
						//System.out.println(bit_code.toString());
					}
				}
			}
			System.out.println("Total length = "+tam);
		} finally {
			qexec.close();
		}*/
        
        
        rt = Runtime.getRuntime();
        totalMem = rt.totalMemory();
        maxMem = rt.maxMemory();
        freeMem = rt.freeMemory();
        megs = 1048576.0;

        System.out.println ("Total Memory: " + totalMem + " (" + (totalMem/megs) + " MiB)");
        System.out.println ("Max Memory:   " + maxMem + " (" + (maxMem/megs) + " MiB)");
        System.out.println ("Free Memory:  " + freeMem + " (" + (freeMem/megs) + " MiB)");
	}
}
