package safetycode;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import utils.Common;

import eu.trowl.owlapi3.rel.reasoner.dl.RELReasoner;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;

import exception.BadFormedBase64NumberException;
import exception.BadFormedBinaryNumberException;
import exception.VariantDoesNotMatchAnAllowedVariantException;

public class MedicineSafetyProfileOWLAPI {
	/**
	 * 64 base representation of the variant codes of the associated patient. 
	 * */
	private String base64ProfileString; 
	/**
	 * Ontology model that contains the patient conceptualization and pharmacogenomics semantic rules. 
	 * */
	private OWLOntology ontology		= null;
	private OWLOntologyManager manager	= null;
 	//private OWLDataFactory factory		= null;
 	private OWLReasoner reasoner		= null;
	
 	
	/**
	 * Constructor of the class. It initializes the model of the pharmacogenomics dataset.
	 * */
	public MedicineSafetyProfileOWLAPI() {
		super();
		initializeModel(null);
	}
	public MedicineSafetyProfileOWLAPI(String ontologyFile){
		super();
		initializeModel(ontologyFile);
		
	}
	
	
	
	/**
	 * initialize model with core pharmacogenomic dataset.
	 * */
	private void initializeModel (String ontologyFile) {
		
		if(ontologyFile==null){
			ontologyFile="file:/D:/workspace/Genomic-CDS/MSC_classes.ttl";
		}
				
		try{
			 manager=OWLManager.createOWLOntologyManager();
			 IRI physicalURI = IRI.create(ontologyFile);
			 ontology = manager.loadOntologyFromOntologyDocument(physicalURI);
			 //File inputOntologyFile = new File(ontologyFile);
			 //ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
			 //factory = manager.getOWLDataFactory();
		     OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		     reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		    
		     /*reasoner=new RELReasonerFactory().createReasoner(ontology);
		     reasoner.precomputeInferences();
		     boolean isConsistent = reasoner.isConsistent();
		     if(isConsistent) System.out.println("La ontología es consistente");*/
		     
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void calculateInferences() throws OWLOntologyCreationException{
		//reasoner.precomputeInferences();
		reasoner.interrupt();
		reasoner.dispose();
		IRI documentIRI = writeModel("reason_model_");
		/*manager.removeOntology(ontology);
		manager.clearIRIMappers();
		factory.purge();
		*/
		manager=null;
		//factory=null;
		ontology=null;
		reasoner=null;
		
		Runtime.getRuntime().gc();
		System.gc();
		
		manager=OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(documentIRI);
		//factory = manager.getOWLDataFactory();
	    
	    reasoner = new RELReasonerFactory().createReasoner(ontology);
		reasoner.precomputeInferences();
		boolean isConsistent = reasoner.isConsistent();
		if(isConsistent) System.out.println("La ontología es consistente");
	}
	
	/**
	 * It parsers the 23AndMe file with the default strand orientation (dbnsp_orientation).
	 * 
	 * @param my23AndMeFileStream	The 23AndMe file to parse.
	 * @return		The processing report of the 23AndMe file.
	 * */
	public String read23AndMeFileStream(InputStream my23AndMeFileStream){
		// default to dbSNP orientation when strand orientation of input data is not given
		return read23AndMeFileStream(my23AndMeFileStream, Common.DBSNP_ORIENTATION);
	}
	
	
	/**
	 * It parses the 23AndMe file with the corresponding strand orientation.
	 * 
	 * @param my23AndMeFileStream			The 23AndMe file to parse.
	 * @param strandOrientationOfInputData	The orientation of the input strand to parse.
	 * @return	The processing report of the 23AndMe file.
	 * */
	public String read23AndMeFileStream(InputStream my23AndMeFileStream, String strandOrientationOfInputData){
		
		int processedLines 						= 0;	//Number of processed lines of strands from the input 23AndMe file.
		int processedMatchingLines				= 0;	//Number of processedLines that corresponds to markers in the model.
		int linesThatDidNotMatchAllowedVariant	= 0;	//Number of processedMatchingLines that could not be matched in the model.
		String processingReport					= "<ul>\n";	//Report that contains the missing matched criteria syntax and general statistics of the parser.
		ArrayList<String[]> listRsids 			= getListRsids();	//Sorted list of strand markers.
		int numberOfRsids = listRsids.size();
		
		//Parsing the 23AndMe file
		try{
			String rsid 			= "";	//strand id
			String my23AndMeSNPCode = "";	//Two char code that is related to rsid.
			String line 			= "";	//Line of the parsed file.
			
			BufferedReader br = new BufferedReader(new InputStreamReader(my23AndMeFileStream));
			while((line=br.readLine())!=null){
				
				line=(line.replaceAll("#.*","")).trim();	//Avoid comments
				line=line.replaceAll(" ","");				//Avoid empty spaces (not \t or \n)
				if(line.isEmpty())	continue;
				
				processedLines++;
				
				String[] lineArray = line.split("\t");		//Obtain the columns of the strand
				if(lineArray.length>=4){
					rsid = lineArray[0].trim();				//Gather rsid of the strand
					my23AndMeSNPCode = lineArray[3].trim();	//Gather code of the strand
					if (my23AndMeSNPCode.length() != 2) continue; //Skip this line because of wrong code length
				}else continue; //Skip this line because of wrong number of columns associated to the strand
				
				//Add the code to the related marker
				for(int i=0;i<listRsids.size();i++){
					String[] genotype = listRsids.get(i);
					if(genotype[0].equalsIgnoreCase(rsid)){//Check which is the related marker defined in the model for this strand
						processedMatchingLines++;
						String[] variants = getVariants(my23AndMeSNPCode,strandOrientationOfInputData,genotype[2]); //Obtain the correct code regarding orientation and alphabetical order.
						genotype[3]=rsid+"("+variants[0]+";"+variants[1]+")";//Generate the new criteri syntax for this strand
					}
				}
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		//End parsing 23AndMe file
		
		String base2ProfileString=""; // binary code of patient variants
		
		//Create the individual patient
		OWLNamedIndividual patient = createPatient();
		
		//Process all patient variations
		for(int i=0;i<listRsids.size();i++){
			String[] genotype = listRsids.get(i);
			String criteriaSyntax=genotype[3];//Criteria syntax that will be related to the patient
			String bit_code=null;
			try{
				bit_code=addVariantToPatientByCriteriaSyntax(patient,genotype[0],criteriaSyntax);//Create rdf:type link between patient individual and matched variant class with the criteria syntax
			}catch(VariantDoesNotMatchAnAllowedVariantException e){
				processingReport+="<li>"+e.getMessage();
			}catch(Exception e){
				processingReport+="<li>"+e.getMessage();
			}
			if(bit_code==null){//The criteria syntax did not match any not "null;null" variant
				bit_code=genotype[4];
				linesThatDidNotMatchAllowedVariant++;
				processingReport+="<li>Warning: " + criteriaSyntax + " does not match any allowed genotype. Only genotypes listed in dbSNP are allowed. A possible reason for this could be that your data is not based on the same strand (+ or -) as dbSNP, and you did not choose the proper settings for strand orientation. This genotype will be reported as 'NULL;NULL' in the resulting Medicine Safety Code.";
			}else{
				genotype[4]=bit_code;
			}
			base2ProfileString+=bit_code;
		}
		processingReport+=("</ul><p><b>Processed " + processedLines + " lines describing variants. Of these, " + processedMatchingLines + " lines matched allowed Medicine Safety Code RS numbers. Of these, " + (numberOfRsids - linesThatDidNotMatchAllowedVariant - processedMatchingLines) + " lines contained genotypes that did not match allowed Medicine Safety Code genotypes. </b></p>");
		try{		
			base64ProfileString = Common.convertFrom2To64(base2ProfileString);
		}catch(BadFormedBinaryNumberException e){
			System.err.println("ERROR: "+e.getMessage());
		}
		return processingReport;
	}
	
	
	/**
	 * Create the patient instance in the model
	 * */
	private OWLNamedIndividual createPatient(){
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass humanClass = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human"));
		OWLNamedIndividual patientIndividual = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));
		OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(humanClass,patientIndividual);
		manager.addAxiom(ontology,classAssertion);
		
		return patientIndividual;
	}
	
	
	/**
	 * This method obtains the sorted list of markers defined in the model with its associated annotations bit_lenth, orientation and the associated annotations of its null;null variant for criteria syntax and bit code.  
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
		
		HashMap<Integer,String[]> results = new HashMap<Integer,String[]>();
				
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		NodeSet<OWLClass> list_marker = reasoner.getSubClasses(human_with_genotype_marker, true);
        for(OWLClass clase: list_marker.getFlattened() ){
        	String rank="";
        	OWLAnnotationProperty ann_rank			= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#rank"));
        	for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_rank)) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    rank=val.getLiteral();
                    break;
                }
            }
        	if(rank==null || rank.isEmpty()) continue;
        	int rank_int = -1;
        	try{
        		rank_int = Integer.parseInt(rank);
        	}catch(NumberFormatException e){
        		e.printStackTrace();
        		continue;
        	}
        	        	
        	String bit_length="";
        	OWLAnnotationProperty ann_bit_length	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_length"));
        	for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_bit_length)) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    bit_length=val.getLiteral();
                    break;
                }
            }
        	if(bit_length==null || bit_length.isEmpty()) continue;
        	
        	String rsid="";
        	OWLAnnotationProperty ann_rsid			= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rsid"));
        	for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_rsid)) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    rsid=val.getLiteral();
                    break;
                }
            }
        	if(rsid==null || rsid.isEmpty()) continue;
        	
        	String orientation="";
        	OWLAnnotationProperty ann_orientation	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#dbsnp_orientation_on_reference_genome"));
        	for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_orientation)) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    orientation=val.getLiteral();
                    break;
                }
            }
        	if(orientation==null || orientation.isEmpty()) continue;
        	
        	String criteria_syntax = rsid+"(null;null)";	//Generate the criteria syntax of "null;null" variant of this marker
        	
        	String bit_code = "";							//Generate the bit code of "null;null" variant of this marker
        	int length=2;
        	try{
        		length = Integer.parseInt(bit_length);
        	}catch(NumberFormatException e){
        		e.printStackTrace();
        	}
        	for(int i=0;i<length;i++){
				bit_code+="0";
			}
        	
        	String[] genotype = {rsid,bit_length,orientation,criteria_syntax,bit_code}; //create the corresponding string array of this markers
        	results.put(rank_int, genotype);
        }
        
        //Sort the list of markers by their rank number.
		for(int key=0;!results.isEmpty();key++){ 
			if(results.containsKey(key)){
				String[] genotype=results.get(key);
				listRsids.add(genotype);
				results.remove(key);
			}
		}
		
		return listRsids;
	}
	
	
	/**
	 * It obtains the correct variant nucleotides when considering its SNP orientation from 23AndMe file and the reference SNP orientation, and its alphabetical order
	 * 
	 * @param snpCode			It contains the two nucleotide character directly from 23AndMe file.
	 * @param orientation_file	It indicates the orientation of the 23AndMe file. Only "dbsnp-orientation" or "forward-orientation" are allowed.
	 * @param orientation_seq	It indicates the orientation of the reference SNP orientation. Only "reverse" or "forward" are allowed.
	 * @return		It returns the two dimensional array with the corresponding nucleotides with the correct orientation and alphabetical order.
	 * */
	private String[] getVariants(String snpCode,String orientation_file, String orientation_seq){
		String[] variant={snpCode.substring(0,1),snpCode.substring(1,2)};
		if ((orientation_file == Common.DBSNP_ORIENTATION) 
				&& (orientation_seq.equals("reverse"))) {
			for(int i=0;i<2;i++){
				if(variant[i].equalsIgnoreCase("A")){
					variant[i]= "T";
					continue;
				}
				if(variant[i].equalsIgnoreCase("T")){
					variant[i]= "A";
					continue;
				}
				if(variant[i].equalsIgnoreCase("C")){
					variant[i]= "G";
					continue;
				}
				if(variant[i].equalsIgnoreCase("G")){
					variant[i]= "C";
					continue;
				}
			}
		}
		if(((int)variant[0].toCharArray()[0]) > ((int)variant[1].toCharArray()[0])){
			String aux = variant[0];
			variant[0]=variant[1];
			variant[1]=aux;
		}
		return variant;
	}
	
	
	/**
	 * It associate the classes of variants that match the criteria syntax to the patient instance in the model.
	 * 
	 * @param patient			Individual that corresponds to the instance of the patient in the model.
	 * @param rsid				Indicates the id of the genotype marker.
	 * @param criteriaSyntax	It represents the criteria syntax of a variant in the model.
	 * @throws	A VariantDoesNotMatchAnAllowedVariantException when an error is detected in the model definition.
	 * @return	It returns the bit code associated to the user or null if the criteria syntax could not be matched in the model.
	 * */
	private String addVariantToPatientByCriteriaSyntax(OWLIndividual patient, String rsid, String criteriaSyntax) throws VariantDoesNotMatchAnAllowedVariantException{
		String bit_code=null;
		OWLDataFactory factory = manager.getOWLDataFactory();
		String criteriaSyntax_def 			= criteriaSyntax.substring(0,criteriaSyntax.indexOf("("))+"(null;null)";
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		OWLAnnotationProperty ann_rsid		= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rsid"));
		OWLAnnotationProperty ann_bit_code	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_code"));
    	OWLAnnotationProperty ann_criteria	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#criteria_syntax"));
    	OWLClass matchedVariantClass		= null;
    	OWLClass matchedVariantDefClass 	= null;
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
        		//Seek for the variants that contain the critieriaSyntax or criteriaSyntax_def
        		for(OWLClass variant: list_variants.getFlattened()){
        			//We obtain the criteriaSyntax annotation value and compare with the given string
                	for (OWLAnnotation annotation : variant.getAnnotations(ontology, ann_criteria)) {
                        if (annotation.getValue() instanceof OWLLiteral) {
                            OWLLiteral val = (OWLLiteral) annotation.getValue();
                            if(criteriaSyntax.equalsIgnoreCase(val.getLiteral())){//This variant matches the criteriaSyntax
                            	matchedVariantClass = variant;
                            	break;
                            }
                            if(criteriaSyntax_def.equalsIgnoreCase(val.getLiteral())){//This variant matches the criteriaSyntax_def
                            	matchedVariantDefClass = variant;
                            	break;
                            }
                        }
                	}
                	if(matchedVariantClass!=null) break;
                }
        		
        		//We obtain the bit_code digits if the desired variant is found
            	if(matchedVariantClass!=null){
            		for (OWLAnnotation annotation : matchedVariantClass.getAnnotations(ontology, ann_bit_code)) {
                        if (annotation.getValue() instanceof OWLLiteral) {
                            OWLLiteral val = (OWLLiteral) annotation.getValue();
                            bit_code=val.getLiteral();
                        }
                    }
            		//We add the variant class to the patient instance in the ontology
            		OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(matchedVariantClass,patient);
            		manager.addAxiom(ontology,classAssertion);
            	}else{
            		if(matchedVariantDefClass!=null){
            			//We add the variant null class to the patient instance in the ontology
                		OWLClassAssertionAxiom classAssertion = factory.getOWLClassAssertionAxiom(matchedVariantDefClass,patient);
                		manager.addAxiom(ontology,classAssertion);
                	}
            	}
            	break;
        	}
        }   
		return bit_code;
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
	 * Get method to obtain the 64 base representation of the patient variants of the markers.
	 * @return	The string associated to the variants of the patient or null if it is not yet defined. 
	 * */
	public String getBase64ProfileString() {
		if (base64ProfileString.isEmpty()) return null;
		return base64ProfileString;
	}
	
	/**
	 * Create the patient model that is related to the base64Profile.
	 * 
	 * @param base64Profile		Base 64 number that represent the binary codification of a patient genotype. 
	 * @throws VariantDoesNotMatchAnAllowedVariantException 
	 * */
	public void readBase64ProfileString(String base64Profile) throws VariantDoesNotMatchAnAllowedVariantException{
		base64ProfileString				= base64Profile;
		String binaryProfile			="";
		try{
			binaryProfile				= Common.convertFrom64To2(base64ProfileString);	
		}catch(BadFormedBase64NumberException e){
			System.err.println("ERROR: "+e.getMessage());
		}
		
		ArrayList<String[]> listRsids	= getListRsids();
		
		//OWLNamedIndividual patient = factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#patient"));
		OWLNamedIndividual patient = createPatient();
		
		for(int position = 0, i = 0;i<listRsids.size();i++){
			String[] genotype = listRsids.get(i);
			int bit_length = Integer.parseInt(genotype[1]);
			if(binaryProfile.length()<position+bit_length) {
				throw new VariantDoesNotMatchAnAllowedVariantException("<p>Warning: the length of the patient profile is shorter than the defined in the model</p>");
			}
			String bit_code = binaryProfile.substring(position,position+bit_length);
			genotype[4]=bit_code;
			position+=bit_length;
			if(!addVariantToPatientByBitCode(patient, genotype[0],genotype[4])){
				throw new VariantDoesNotMatchAnAllowedVariantException("<p>Warning: the genotype mark \""+genotype[0]+"\" or its corresponding code variant were not found in the model</p>");
			}
		}
	}
	
	
	/**
	 * Write the model into a file.
	 * @param fileOut	The file that will contain the model of the patient.
	 * */
	public IRI writeModel(String fileOut){
		// Save to RDF/XML
		try{
			File output = File.createTempFile(fileOut,".owl");
			IRI documentIRI = IRI.create(output);
			System.out.println("IRI="+documentIRI.toString());
			manager.saveOntology(ontology,documentIRI);
			return documentIRI;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Write the model into a OutputStream.
	 * @param output	The output stream where the ontology will be printed.
	 * */
	public void writeModel(OutputStream output){
		// Save to RDF/XML
		try{
			manager.saveOntology(ontology, output);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * It provides the information that is related to the CDS rules in the model for every drug.
	 * 
	 * */
	public HashMap<String,HashSet<String>> obtainDrugRecommendations(){
		HashMap<String,HashSet<String>> list_recommendations = new HashMap<String,HashSet<String>>();
			
		RELReasoner			local_reasoner	= new RELReasonerFactory().createReasoner(ontology);
		local_reasoner.precomputeInferences();
		
		OWLDataFactory 			factory				= manager.getOWLDataFactory();
		OWLNamedIndividual		patient				= factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));//We obtain the patient instance
		OWLAnnotationProperty	ann_relevant_for	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#relevant_for"));
		OWLAnnotationProperty	ann_cds_message		= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#CDS_message"));
        
		if(patient!=null){
        	NodeSet<OWLClass> list_types = local_reasoner.getTypes(patient, false);
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
        		
        		NodeSet<OWLClass> list_superclasses = local_reasoner.getSuperClasses(type, true);
        		for(OWLClass superclass : list_superclasses.getFlattened()){
        			if(superclass.getIRI().toString().contains("human_with_genetic_polymorphism")){
        				String label = "";
        				Set<OWLAnnotation> listLabels = type.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
        				for(OWLAnnotation labelAnn: listLabels){
        					if (labelAnn.getValue() instanceof OWLLiteral) {
        						OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
        						label = literal.getLiteral().trim().toLowerCase();
        					}
        				}
        				
        				if(list_recommendations.containsKey("raw_data")){
     	                	HashSet<String> list_messages = list_recommendations.get("raw_data");
     	                	list_messages.add(label);
     	                }else{
     	                	HashSet<String> list_messages = new HashSet<String>();
     	                	list_messages.add(label);
     	                	list_recommendations.put("raw_data", list_messages);
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
		
		return list_recommendations;
	}
	
	
	/**
	 * It provides the information that is related to the CDS rules in the model for every drug.
	 * 
	 * */
	/*public HashMap<String,HashSet<String>> obtainDrugRecommendationsThread(){
		HashMap<String,HashSet<String>> list_recommendations = new HashMap<String,HashSet<String>>();
		IRI documentIRI = writeModel("reason_model_");
		manager=null;
		ontology=null;
		reasoner=null;
				
		PatientProfileReasoning ppr = new PatientProfileReasoning(list_recommendations,documentIRI);
		try{
			ppr.start();
			ppr.join();
		}catch(Exception e){
			e.printStackTrace();
		}
				
		return list_recommendations;
	}*/
}
