package utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

//import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.model.OWLOntology;
//import org.semanticweb.owlapi.model.OWLOntologyManager;

import exception.BadRuleDefinitionException;
import exception.VariantDoesNotMatchAnyAllowedVariantException;

import safetycode.AlleleGroup;
import safetycode.AlleleRule;
import safetycode.DrugRecommendation;
import safetycode.GeneticMarkerGroup;
import safetycode.GenotypeElement;
//import safetycode.NodeCondition;
import safetycode.SNPElement;
import safetycode.SNPsGroup;

/**
 * This class keeps a single instance of the base ontology in order to use it without having to load in memory one for every request.
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * */
public class OntologyManagement {
	/**	Singleton instance of this class */
	private static OntologyManagement singleton=null;
	/** Ontology modelo that will be always load into memory */
	//private OWLOntology ontology = null;
	/** Ontology manager of the base ontology */
	//private OWLOntologyManager manager = null;
	/** Source file where base ontology is stored */
	private static String ontFile=null;
	/** List of base Genetic markers groups */
	private ArrayList<GeneticMarkerGroup> listGeneticMarkers = null;
	/** List of allele groups */
	private ArrayList<AlleleGroup> listAlleleGroups = null;
	/** List of SNP groups */
	private ArrayList<SNPsGroup> listSNPsGroups = null;
	/** List of defined Drug recommendations rules */
	private ArrayList<DrugRecommendation> listDrugRecommendations = null;
	/** List of defined phenotype rules */
	private HashMap<String,String> listPhenotypeRules = null; 
	/** List of defined allele rules */
	private ArrayList<AlleleRule> listAlleleRules = null;
	
	/**
	 * Static method to obtain the singleton instance of this class.
	 * 
	 * @return		A static reference to the singleton instance of OntologyManagement class.
	 * */
	public static OntologyManagement getOntologyManagement(String ontologyFile){
		if(ontologyFile!=null && (!ontologyFile.equalsIgnoreCase(ontFile)||singleton==null)){
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
			//Initialise ontology manager to speed up the process of inferring the corresponding haplotypes from the raw SNPs.
			/*manager = OWLManager.createOWLOntologyManager();
			File file = new File(ontologyFile);
			ontology = manager.loadOntologyFromOntologyDocument(file);*/
			
			//Initialise allele groups information from tab separated file: alleleGroups.
			InputStream allelesStream = new FileInputStream(new File(Common.tabSeparatedAlleleGroups));			
			initializeAlleleGroups(allelesStream);
			allelesStream.close();
			//for(AlleleGroup ag: listAlleleGroups){System.out.println("AlleleGroup["+ag.getRank()+"]="+ag.getGeneticMarkerName());}
			
			//Initialise SNP groups information from tab separated file: snpGroups.
			InputStream snpsStream = new FileInputStream(new File(Common.tabSeparatedSNPGroups));
			initializeSNPsGroups(snpsStream);
			snpsStream.close();
			//for(SNPsGroup sg: listSNPsGroups){System.out.println("SNPsGroup["+sg.getRank()+"]="+sg.getGeneticMarkerName());}
			
			//Initialise genotype elements from the lists of SNPs and alleles.
			initializeGenotypeElements();
			//for(GeneticMarkerGroup gmg: listGeneticMarkers){System.out.println("GeneticMarkerGroup["+gmg.getRank()+"]="+gmg.getGeneticMarkerName());}
			
			//Initialise rule information from tab separated files: phenotype and drugRecommendations.
			InputStream phenotypeStream = new FileInputStream(new File(Common.tabSeparatedPhenotypeRules));
			InputStream drugRecommendationsStream = new FileInputStream(new File(Common.tabSeparatedCDSRules));
			initializeDrugRecommendations(phenotypeStream, drugRecommendationsStream);
			//for(DrugRecommendation dr: listDrugRecommendations){System.out.println("Drug recommendation "+dr.getDrugName()+" = "+dr.toString());}
			
			//Initialise Allele rules information from tab separated file: alleleRules.
			InputStream alleleRulesStream = new FileInputStream(new File(Common.tabSeparatedAlleleRules));
			initializeAlleleRules(alleleRulesStream);
			alleleRulesStream.close();
			//for(AlleleRule ar: listAlleleRules){System.out.println("allele rule "+ar.getGeneName()+" = ");HashMap<String, NodeCondition> listNodes = ar.getListNodes();	for(String key: listNodes.keySet()){System.out.println("\tAllele->"+key+" = "+listNodes.get(key).toString());}}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ERROR in Path = " + ontologyFile);
		}
	}
	
	
	public ArrayList<DrugRecommendation> getListDrugRecommendation(){
		return listDrugRecommendations;
	}
	
	/**
	 * Get method to obtain the instance of the ontology model.
	 * 
	 * @return		The corresponding OWL ontology of the base ontological model.
	 * */
	/*public OWLOntology getOntology(){		
		return singleton.ontology;
	}*/
	
	
	/**Get method to obtain the ontology manager of the corresponding base ontological model.
	 * 
	 * @return		The ontology manager of the base ontological model.
	 * */
	/*public OWLOntologyManager getManager(){
		return singleton.manager;
	}*/
	
	
	/**
	 * It is a clone method to create a new instance of the base ontological model with its corresponding ontology manager.
	 * 	
	 * @return		The new ontology manager that is associated to the new instance model.
	 * */
	/*public OWLOntologyManager getNewOntologyManager(){
		OWLOntologyManager mang = null;
		try{
			mang = OWLManager.createOWLOntologyManager();
			mang.createOntology(singleton.ontology.getAxioms());
		}catch(Exception e){
			e.printStackTrace();
		}
		return mang;
	}*/
	
	
	/**
	 * Obtain a TrOWL reasoner from the base ontology model.
	 * 
	 * @return		A TrOWL reasoner without precomputed inferences.
	 * */
	/*public RELReasoner getRELReasoner(){
		RELReasoner local_reasoner = null;
		try{
			OWLOntologyManager mang = OWLManager.createOWLOntologyManager();
			OWLOntology onto = mang.createOntology(singleton.ontology.getAxioms());
			local_reasoner = new RELReasonerFactory().createReasoner(onto);
		}catch(Exception e){
			e.printStackTrace();
		}
		return local_reasoner;
	}*/
	
	
	/**
	 * Get method to obtain the list of base variants when parsing a VCF file.
	 * 
	 * @return		List of base SNPs for reference VCF format. 
	 * */
	public ArrayList<SNPElement> getVCFRefListRsids(){
		ArrayList<SNPElement> listRsids = new ArrayList<SNPElement>();

		for(SNPsGroup snpg: listSNPsGroups){
			listRsids.add(new SNPElement(snpg.getGeneticMarkerName(),snpg.getVCFReference(),snpg.getVCFReference()));
		}
		
		return listRsids;
	}
	
	
	/**
	 * Get method to obtain the list of base variants when parsing a 23andme file.
	 * 
	 * @return		List of base SNPs of (null;null) variants.
	 * */
	public ArrayList<SNPElement> getList23andMeRsids(){
		ArrayList<SNPElement> listRsids = new ArrayList<SNPElement>();

		for(SNPsGroup snpg: listSNPsGroups){
			listRsids.add(new SNPElement(snpg.getGeneticMarkerName(),null,null));
		}
				
		return listRsids;
	}
	
	
	/**
	 * Get the list of genetic marker groups.
	 * 
	 * @return List of genetic marker groups.
	 * */
	public ArrayList<GeneticMarkerGroup> getListGeneticMarkerGroups(){
		return listGeneticMarkers;
	}
	
	
	/**
	 * Get method to obtain the list of base allele variants.
	 * 
	 * @return		List of base Alleles, its annotations and the criteria syntax.
	 * @throws VariantDoesNotMatchAnyAllowedVariantException 
	 * */
	public ArrayList<GenotypeElement> getListGenotypeElements() throws VariantDoesNotMatchAnyAllowedVariantException{
		ArrayList<GenotypeElement> listGenotypeElements = new ArrayList<GenotypeElement>();
	
		for(int i=0;i<listGeneticMarkers.size();i++){
			GeneticMarkerGroup gmg = listGeneticMarkers.get(i);
			GenotypeElement new_node = gmg.getGenotypeElement(0);
			listGenotypeElements.add(new_node);
		}
		
		return listGenotypeElements;
	}
	
	public ArrayList<GenotypeElement> getDefaultGenotypeElement() throws VariantDoesNotMatchAnyAllowedVariantException{
		
		ArrayList<GenotypeElement> listGenotypeElements = new ArrayList<GenotypeElement>();
		for(int i=0;i<listGeneticMarkers.size();i++){
			GeneticMarkerGroup gmg = listGeneticMarkers.get(i);
					
			GenotypeElement ge = null;
			if(gmg.getNumberOfVariants()>0){
				ge = gmg.getGenotypeElement(1);
			}else{
				ge = gmg.getGenotypeElement(0);
			}
			listGenotypeElements.add(ge);
		}
		return listGenotypeElements;
	}	
	
	/**
	 * Get the list of SNPs groups information from the ontology.
	 * 
	 * @return	List of SNPs groups.
	 * */	
	public ArrayList<SNPsGroup> getListSNPsGroups(){
		return listSNPsGroups;
	}
	
	public ArrayList<AlleleRule> getListAlleleRules(){
		return listAlleleRules;
	}
	
	/**
	 * Initialize the sorted list of alleles and SNPs groups defined in the model. These groups formed the inferred patient's genotype.  
	 * */
	private void initializeGenotypeElements(){
		listGeneticMarkers	= new ArrayList<GeneticMarkerGroup>();
		ArrayList<GeneticMarkerGroup> list_gmg = new ArrayList<GeneticMarkerGroup>();
		list_gmg.addAll(listAlleleGroups);
		list_gmg.addAll(listSNPsGroups);
		Collections.sort(list_gmg);
		for(GeneticMarkerGroup gmg: list_gmg){
			if(gmg.getRank()>=0) listGeneticMarkers.add(gmg);
		}
	}
	
	/** Generate the instances of AlleleGroup based on the latest version of the Genomic CDS ontology. June 2014*/
	private void initializeAlleleGroups(InputStream fileIn){
		//GENENAME	RANK	LISTALLELES
		listAlleleGroups = new ArrayList<AlleleGroup>();
		String gene_name;
		ArrayList<String> list_allele_names;
		int rank;
		AlleleGroup ag;
		
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(fileIn));
			String linea = "";
			br.readLine();//The first line is the header
			while((linea=br.readLine())!=null){
				String[] tokens = linea.split("\t");
				if(tokens.length > 1){
					gene_name = tokens[0];
					rank = Integer.parseInt(tokens[1]);
					list_allele_names = new ArrayList<String>();
					for(int i=2; i<tokens.length;i++){
						list_allele_names.add(tokens[i]);
					}
					ag = new AlleleGroup(gene_name,list_allele_names,rank);
					listAlleleGroups.add(ag);
				}
			}
			br.close();
			fileIn.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}	
	
	/** Generate the instances of SNPsGroup based on the latest version of the Genomic CDS ontology. June 2014*/
	private void initializeSNPsGroups(InputStream fileIn){
		//RSID	RANK	ORIENTATION	VCFREFERENCE	LISTGENOMICTEST(SEPARATED_BY_';')	LISTSNPS
		listSNPsGroups = new ArrayList<SNPsGroup>();
		String rsid;
		ArrayList<String> list_SNP_names;
		int rank;
		String strandOrientation;
		String vcf_format_reference;
		ArrayList<String> listTestedWith;
		SNPsGroup sg;
		
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(fileIn));
			String linea = "";
			br.readLine();//The first line is the header
			while((linea=br.readLine())!=null){
				String[] tokens = linea.split("\t");
				if(tokens.length > 4){
					rsid = tokens[0];
					rank = Integer.parseInt(tokens[1]);
					strandOrientation = tokens[2];
					vcf_format_reference = tokens[3];
					String testFormats = tokens[4];
					listTestedWith = new ArrayList<String>();
					if(!testFormats.isEmpty()){
						String[] items = testFormats.split(";");
						for(String test : items){
							listTestedWith.add(test);
						}
					}
					
					list_SNP_names = new ArrayList<String>();
					for(int i=5; i<tokens.length;i++){
						list_SNP_names.add(tokens[i]);
					}
					sg = new SNPsGroup(rsid, list_SNP_names, rank, strandOrientation, vcf_format_reference, listTestedWith);
					listSNPsGroups.add(sg);
				}
			}
			br.close();
			fileIn.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/** Generates the instances of DrugRecommendation class based on the information of the latest version of the Genomic CDS ontology. June 2014*/	
	private void initializeDrugRecommendations(InputStream phenotypeFile, InputStream recommendationsFile){
		listPhenotypeRules = new HashMap<String,String>();
		try{
			String ruleId = "";
			String logicalDescription = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(phenotypeFile));
			String linea = "";
			br.readLine();//The first line is the header
			while((linea=br.readLine())!=null){
				String[] tokens = linea.split("\t");
				if(tokens.length == 2){
					ruleId = tokens[0];
					logicalDescription = tokens[1];
					listPhenotypeRules.put("human_with_"+ruleId, logicalDescription);
				}
			}
			br.close();
			phenotypeFile.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		listDrugRecommendations			= new ArrayList<DrugRecommendation>();
		try{
			String recommendation_label		= "";
			String cds_message				= "";
			String importance				= "";
			String source					= "";
			String relevant_for				= "";
			ArrayList<String> seeAlsoList	= null;
			String lastUpdate				= "";
			String phenotype				= "";
			String recommendation_comment	= "";
			DrugRecommendation dr			= null;
			BufferedReader br = new BufferedReader(new InputStreamReader(recommendationsFile));
			String linea = "";
			br.readLine();//The first line is the header
			while((linea=br.readLine())!=null){
				String[] tokens = linea.split("\t");
				if(tokens.length > 8){
					recommendation_label = tokens[0];
					cds_message = tokens[1];
					importance = tokens[2];
					source = tokens[3];
					relevant_for = tokens[4];
					lastUpdate = tokens[5];
					phenotype = tokens[6];
					recommendation_comment = (tokens[7]).trim();
					if(recommendation_comment.isEmpty()) continue;
					
					for(String key: listPhenotypeRules.keySet()){
						if(recommendation_comment.contains(key)){
							recommendation_comment = recommendation_comment.replace(key, " ("+listPhenotypeRules.get(key)+") ");
						}
					}
					seeAlsoList = new ArrayList<String>();
					for(int i=8;i<tokens.length;i++){
						seeAlsoList.add(tokens[i]);
					}
					
					try {
						dr = new DrugRecommendation(recommendation_label, cds_message, importance, source, relevant_for, seeAlsoList, lastUpdate, phenotype);
						dr.setRule(recommendation_comment);
						listDrugRecommendations.add(dr);
					} catch (BadRuleDefinitionException e) {
						System.err.println("ERROR:"+e.getMessage());
					}
				}
			}
			br.close();
			recommendationsFile.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void initializeAlleleRules(InputStream alleleRuleFile){
		//AlleleID	logical_description
		listAlleleRules = new ArrayList<AlleleRule>();
		String allele_id;
		String logical_description;
		
		HashMap<String, HashMap<String,String>> listRules = new HashMap<String,HashMap<String,String>>();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(alleleRuleFile));
			String linea = "";
			br.readLine();//The first line is the header
			while((linea=br.readLine())!=null){
				String[] tokens = linea.split("\t");
				if(tokens.length == 2){
					allele_id = tokens[0];
					logical_description = tokens[1];
					String gene = allele_id.substring(0,allele_id.indexOf("_"));
					if(listRules.containsKey(gene)){
						HashMap<String,String> listHaplotypesRules = listRules.get(gene);
						listHaplotypesRules.put(allele_id, logical_description);
					}else{
						HashMap<String,String> listHaplotypesRules = new HashMap<String,String>	();
						listHaplotypesRules.put(allele_id, logical_description);
						listRules.put(gene, listHaplotypesRules);
					}
				}
			}
			br.close();
			alleleRuleFile.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		AlleleRule ar;
		try{
			for(String geneName: listRules.keySet()){
				HashMap<String,String> listAllelesLD = listRules.get(geneName);
				for(GeneticMarkerGroup gmg: listGeneticMarkers){
					if(gmg.getGeneticMarkerName().equalsIgnoreCase(geneName)){
						ar = new AlleleRule(geneName,listAllelesLD,gmg);
						listAlleleRules.add(ar);
						break;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*private void initializeSNPsGroups(){
		listSNPsGroups = new ArrayList<SNPsGroup>();
		
		OWLDataFactory factory					= manager.getOWLDataFactory();
		OWLClass polymorphismClass				= factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#polymorphism"));
		OWLAnnotationProperty ann_rsid			= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rsid"));
		OWLAnnotationProperty ann_testedWith	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#can_be_tested_with"));
		OWLAnnotationProperty ann_orientation	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#dbsnp_orientation_on_reference_genome"));
		OWLAnnotationProperty ann_rank			= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rank"));
		OWLAnnotationProperty ann_vcf_reference	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#vcf_format_reference"));
		
		Iterator<OWLClassExpression> list_polymorphisms = polymorphismClass.getSubClasses(ontology).iterator();
		while(list_polymorphisms.hasNext()){
			OWLClass snpClass = list_polymorphisms.next().asOWLClass();
			
			//rsid
			String rsid = "";
			for (OWLAnnotation annotation : snpClass.getAnnotations(ontology, ann_rsid)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					rsid = val.getLiteral();
					break;
				}
			}
			if (rsid == null || rsid.isEmpty())	continue;
			
			//can_be_tested_with
			ArrayList<String> listTestedWith = new ArrayList<String>();
			for (OWLAnnotation annotation : snpClass.getAnnotations(ontology, ann_testedWith)) {
				IRI assay_IRI = IRI.create(annotation.getValue().toString());
				OWLNamedIndividual assay_instance = factory.getOWLNamedIndividual(assay_IRI);
				if (assay_instance != null) {
					Set<OWLAnnotation> listLabels = assay_instance.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
					for (OWLAnnotation labelAnn : listLabels) {
						if (labelAnn.getValue() instanceof OWLLiteral) {
							OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
							listTestedWith.add(literal.getLiteral().trim().toLowerCase());
							break;
						}
					}
				}
			}
			
			//orientation
			String strandOrientation = "";
			for (OWLAnnotation annotation : snpClass.getAnnotations(ontology, ann_orientation)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					strandOrientation = val.getLiteral();
					break;
				}
			}
			if (strandOrientation == null || strandOrientation.isEmpty())	continue;
			
			//List allowed nucleotides in the SNP
			String vcf_format_reference = "";
			ArrayList<String> list_SNP_names = new ArrayList<String>();
			Iterator<OWLClassExpression> snpsIterator = snpClass.getSubClasses(ontology).iterator();
			while(snpsIterator.hasNext()){
				OWLClass snpVariantClass = snpsIterator.next().asOWLClass();
				String snpVariant_name = "";
				
				Set<OWLAnnotation> listAlleleLabels = snpVariantClass.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
				for (OWLAnnotation labelAnn : listAlleleLabels) {
					if (labelAnn.getValue() instanceof OWLLiteral) {
						OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
						snpVariant_name = literal.getLiteral().trim();
					}
				}
				if (snpVariant_name == null || snpVariant_name.isEmpty())	continue;
				list_SNP_names.add(snpVariant_name);
				
				//vcf_format_reference
				for (OWLAnnotation annotation_ref : snpVariantClass.getAnnotations(ontology, ann_vcf_reference)) {
					if (annotation_ref.getValue() instanceof OWLLiteral) {
						OWLLiteral val = (OWLLiteral) annotation_ref.getValue();
						if(val.getLiteral().equalsIgnoreCase("true")){
							if(snpVariant_name.contains("_")){
								vcf_format_reference = snpVariant_name.substring(snpVariant_name.indexOf("_")+1);
							}else{
								vcf_format_reference = snpVariant_name;
							}
						}
						break;
					}
				}
			}
			
			//rank
			int rank=-1;
			String rank_label = null;
			for (OWLAnnotation annotation : snpClass.getAnnotations(ontology, ann_rank)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					rank_label = val.getLiteral();
					break;
				}
			}
			if (rank_label != null && !rank_label.isEmpty()){
				try {
					rank = Integer.parseInt(rank_label);
				} catch (NumberFormatException e) {
					continue;
				}
			}
						
			SNPsGroup sg = new SNPsGroup(rsid, list_SNP_names, rank, strandOrientation, vcf_format_reference, listTestedWith); // create the corresponding string array of these markers
			listSNPsGroups.add(sg);
		}
		Collections.sort(listSNPsGroups);
	}*/
	
	/**
	 * Initialize the list of allele groups defined in the ontology.
	 * */
	/*private void initializeAlleleGroups(){
		listAlleleGroups	= new ArrayList<AlleleGroup>();
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass alleleClass = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#allele"));
		OWLAnnotationProperty ann_rank = factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rank"));
		
		Iterator<OWLClassExpression> list_allele_genes = alleleClass.getSubClasses(ontology).iterator();
		
		while(list_allele_genes.hasNext()){
			OWLClass gene_class = list_allele_genes.next().asOWLClass();
			
			//rank
			String rank_label = "";
			for (OWLAnnotation annotation : gene_class.getAnnotations(ontology, ann_rank)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					rank_label = val.getLiteral();
					break;
				}
			}
			
			if (rank_label == null || rank_label.isEmpty())	continue;
			int rank = -1;
			try {
				rank = Integer.parseInt(rank_label);
			} catch (NumberFormatException e) {
				continue;
			}
			
			//gene_name
			String gene_name = "";
			Set<OWLAnnotation> listLabels = gene_class.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
			for (OWLAnnotation labelAnn : listLabels) {
				if (labelAnn.getValue() instanceof OWLLiteral) {
					OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
					gene_name = literal.getLiteral().trim();
				}
			}
			if (gene_name == null || gene_name.isEmpty())	continue;
			
			//Allele variants
			OWLAnnotationProperty ann_label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
			ArrayList<String> list_allele_names = new ArrayList<String>();
			list_allele_names.addAll(getSubAlleles(gene_class,ann_label));
						
			AlleleGroup ag = new AlleleGroup(gene_name,list_allele_names,rank); // create the corresponding string array of these markers
			listAlleleGroups.add(ag);
		}
		Collections.sort(listAlleleGroups);
	}*/
	
	/*private ArrayList<String> getSubAlleles(OWLClass root_allele,OWLAnnotationProperty ann_label){
		ArrayList<String> listSubAlleles = new ArrayList<String>();
		Iterator<OWLClassExpression> allelesIterator = root_allele.getSubClasses(ontology).iterator();
		while(allelesIterator.hasNext()){
			OWLClass allele_class = allelesIterator.next().asOWLClass();
			String allele_name = "";
			Set<OWLAnnotation> listAlleleLabels = allele_class.getAnnotations(ontology, ann_label);
			for (OWLAnnotation labelAnn : listAlleleLabels) {
				if (labelAnn.getValue() instanceof OWLLiteral) {
					OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
					allele_name = literal.getLiteral().trim();
				}
			}
			if (allele_name == null || allele_name.isEmpty())	continue;
			listSubAlleles.addAll(getSubAlleles(allele_class,ann_label));
			listSubAlleles.add(allele_name);
		}
		return listSubAlleles;
	}*/
	
	
	/*private void initializeDrugRules(){
		HashMap<String, String> listPhenotypeRules	= new HashMap<String,String>();
		OWLDataFactory factory						= manager.getOWLDataFactory();
		
		OWLClass rootPhenotypeRuleClass				= factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#phenotype_rule"));
		Iterator<OWLClassExpression> list_rules		= rootPhenotypeRuleClass.getSubClasses(ontology).iterator();
		while(list_rules.hasNext()){
			OWLClass phenotype = list_rules.next().asOWLClass();
			
			String phenotype_comment = "";
			Set<OWLAnnotation> listComments = phenotype.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()));
			for (OWLAnnotation commentAnn : listComments) {
				if (commentAnn.getValue() instanceof OWLLiteral) {
					OWLLiteral literal = (OWLLiteral) commentAnn.getValue();
					phenotype_comment = literal.getLiteral().trim();
					break;
				}
			}
			if (phenotype_comment == null || phenotype_comment.isEmpty()){
				continue;
			}
			
			String uri = phenotype.getIRI().toString();
			if(uri.indexOf("#")>=0){
				uri = uri.substring(uri.indexOf("#")+1);
			}
			uri = "human_with_"+uri;
			listPhenotypeRules.put(uri, phenotype_comment);
		}
		
		
		
		listDrugRecommendations	= new ArrayList<DrugRecommendation>();
		OWLClass rootRuleClass		= factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rule"));
		OWLAnnotationProperty ann_cds_message	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#CDS_message"));
		OWLAnnotationProperty ann_source		= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#source"));
		OWLAnnotationProperty ann_relevant_for	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#relevant_for"));
		OWLAnnotationProperty ann_importance	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#recommendation_importance"));
		OWLAnnotationProperty ann_lastUpdate	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#date_last_validation"));
		OWLAnnotationProperty ann_phenotype		= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#phenotype_description"));
		
		//java.util.HashSet<String> list_drugs= new java.util.HashSet<String>();
		
		Iterator<OWLClassExpression> list_recommendations = rootRuleClass.getSubClasses(ontology).iterator();
		while(list_recommendations.hasNext()){
			OWLClass recommendation = list_recommendations.next().asOWLClass();
						
			//cds_message
			String cds_message = "";
			for (OWLAnnotation annotation : recommendation.getAnnotations(ontology, ann_cds_message)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					cds_message = val.getLiteral();
					break;
				}
			}
			if (cds_message == null || cds_message.isEmpty()){
				cds_message = "None.";
				//continue;
			}
			
			//source
			String source = "";
			for (OWLAnnotation annotation : recommendation.getAnnotations(ontology, ann_source)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					source = val.getLiteral();
					break;
				}
			}
			if (source == null || source.isEmpty()){
				continue;
			}

			//phenotype description
			String phenotype = "";
			for (OWLAnnotation annotation : recommendation.getAnnotations(ontology, ann_phenotype)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					phenotype = val.getLiteral();
					break;
				}
			}
			
			//importance
			String importance = "";
			for (OWLAnnotation annotation : recommendation.getAnnotations(ontology, ann_importance)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					importance = val.getLiteral();
					break;
				}
			}
			if (importance == null || importance.isEmpty()){
				importance = "Standard treatment";
			}

			//last update
			String lastUpdate = "";
			for (OWLAnnotation annotation : recommendation.getAnnotations(ontology, ann_lastUpdate)) {
				if (annotation.getValue() instanceof OWLLiteral) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					lastUpdate = val.getLiteral();
					break;
				}
			}
			if (lastUpdate == null || lastUpdate.isEmpty()){
				continue;
			}
			
			//recommendation_comment
			String recommendation_comment = "";
			Set<OWLAnnotation> listComments = recommendation.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()));
			for (OWLAnnotation commentAnn : listComments) {
				if (commentAnn.getValue() instanceof OWLLiteral) {
					OWLLiteral literal = (OWLLiteral) commentAnn.getValue();
					recommendation_comment = literal.getLiteral().trim();
				}
			}
			if (recommendation_comment == null || recommendation_comment.isEmpty()){
				continue;
			}
			
			for(String key: listPhenotypeRules.keySet()){
				if(recommendation_comment.contains(key)){
					recommendation_comment = recommendation_comment.replaceAll(key, "("+listPhenotypeRules.get(key)+")");
				}
			}
			
			//recommendation_label
			String recommendation_label = "";
			Set<OWLAnnotation> listLabels = recommendation.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
			for (OWLAnnotation labelAnn : listLabels) {
				if (labelAnn.getValue() instanceof OWLLiteral) {
					OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
					recommendation_label = literal.getLiteral().trim();
				}
			}
			if (recommendation_label == null || recommendation_label.isEmpty()){
				continue;
			}
			
			//seeAlso
			ArrayList<String> seeAlsoList = new ArrayList<String>();
			
			Set<OWLAnnotation> listSeeAlso = recommendation.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_SEE_ALSO.getIRI()));
			for (OWLAnnotation seeAlsoAnn : listSeeAlso) {
				seeAlsoList.add(seeAlsoAnn.getValue().toString());
			}
			
			//relevant_for
			String relevant_for = "";
			for (OWLAnnotation annotation : recommendation.getAnnotations(ontology, ann_relevant_for)) {
				IRI assay_IRI = IRI.create(annotation.getValue().toString());
				OWLClass drug_class = factory.getOWLClass(assay_IRI);
				if (drug_class != null) {
					Set<OWLAnnotation> listDrugLabels = drug_class.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
					for (OWLAnnotation labelAnn : listDrugLabels) {
						if (labelAnn.getValue() instanceof OWLLiteral) {
							OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
							relevant_for= literal.getLiteral().trim();
							break;
						}
					}
				}
			}
			if (relevant_for == null || relevant_for.isEmpty()){
				continue;
			}
		
			
			DrugRecommendation dr = new DrugRecommendation(recommendation_label, cds_message, importance, source, relevant_for, seeAlsoList, lastUpdate,phenotype);
			dr.setRule(recommendation_comment);
			listDrugRecommendations.add(dr);
		}
	}*/
}
