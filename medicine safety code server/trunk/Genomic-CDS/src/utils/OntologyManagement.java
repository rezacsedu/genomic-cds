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
 * This class keeps a single instance of the information collected from resource files in order to use it without having to load it in memory for every request.
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 * */
public class OntologyManagement {
	/**	Singleton instance of this class */
	private static OntologyManagement singleton=null;
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
	public static OntologyManagement getOntologyManagement(String path){
		if(singleton == null){
			singleton = new OntologyManagement(path);
		}
		return singleton;
	}
	
	
	/**
	 * Constructor of the class. Initialises the variables with the content of resource files.
	 * 
	 * @return		Instance of the OntologyManagement class.
	 * */
	public OntologyManagement(String localPath){
		try {
			
			//Initialise allele groups information from tab separated file: alleleGroups.
			InputStream allelesStream = new FileInputStream(new File(localPath+"/"+Common.tabSeparatedAlleleGroups));			
			initializeAlleleGroups(allelesStream);
			allelesStream.close();
			//for(AlleleGroup ag: listAlleleGroups){System.out.println("AlleleGroup["+ag.getRank()+"]="+ag.getGeneticMarkerName());}
			
			//Initialise SNP groups information from tab separated file: snpGroups.
			InputStream snpsStream = new FileInputStream(new File(localPath+"/"+Common.tabSeparatedSNPGroups));
			initializeSNPsGroups(snpsStream);
			snpsStream.close();
			//for(SNPsGroup sg: listSNPsGroups){System.out.println("SNPsGroup["+sg.getRank()+"]="+sg.getGeneticMarkerName());}
			
			//Initialise genotype elements from the lists of SNPs and alleles.
			initializeGenotypeElements();
			//for(GeneticMarkerGroup gmg: listGeneticMarkers){System.out.println("GeneticMarkerGroup["+gmg.getRank()+"]="+gmg.getGeneticMarkerName());}
			
			//Initialise rule information from tab separated files: phenotype and drugRecommendations.
			InputStream phenotypeStream = new FileInputStream(new File(localPath+"/"+Common.tabSeparatedPhenotypeRules));
			InputStream drugRecommendationsStream = new FileInputStream(new File(localPath+"/"+Common.tabSeparatedCDSRules));
			initializeDrugRecommendations(phenotypeStream, drugRecommendationsStream);
			//for(DrugRecommendation dr: listDrugRecommendations){System.out.println("Drug recommendation "+dr.getDrugName()+" = "+dr.toString());}
			
			//Initialise Allele rules information from tab separated file: alleleRules.
			InputStream alleleRulesStream = new FileInputStream(new File(localPath+"/"+Common.tabSeparatedAlleleRules));
			initializeAlleleRules(alleleRulesStream);
			alleleRulesStream.close();
			//for(AlleleRule ar: listAlleleRules){System.out.println("allele rule "+ar.getGeneName()+" = ");HashMap<String, NodeCondition> listNodes = ar.getListNodes();	for(String key: listNodes.keySet()){System.out.println("\tAllele->"+key+" = "+listNodes.get(key).toString());}}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ERROR in Path = " + localPath);
		}
	}
	
	/**
	 * Get method to obtain the list of drug recommendations rules.
	 * 
	 * @return	The list of cds rules parsed from the resource files. 
	 * */
	public ArrayList<DrugRecommendation> getListDrugRecommendation(){
		return listDrugRecommendations;
	}
	
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
	
	/**
	 * Get the genotype profile which consists of the first allele variation for both strand orientation of each gene.
	 * 
	 * @return	The list of genotype elements that form a genetotype profile.
	 * */	
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
	 * Get the list of SNPs groups information from the resource files.
	 * 
	 * @return	List of SNPs groups.
	 * */
	public ArrayList<SNPsGroup> getListSNPsGroups(){
		return listSNPsGroups;
	}
	
	/**
	 * Get the list of allele rules information from the resource files.
	 * 
	 * @return	List of Allele rules.
	 * */
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
	
	/**
	 * Generate the instances of AlleleGroup based on the latest version of the resource files.
	 * */
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
	
	/**
	 *  Generate the instances of SNPsGroup based on the latest version of the resource files.
	 * */
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
	
	/**
	 *  Generates the instances of DrugRecommendation class based on the information of the latest version of the resource files.
	 *  
	 *  */	
	private void initializeDrugRecommendations(InputStream phenotypeFile, InputStream recommendationsFile){
		//RULEID	LOGICALDESCRIPTION
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
		
		//RULEID	CDSMESSAGE	IMPORTANCE	SOURCE	RELEVANTFOR	LASTUPDATE	PHENOTYPE	RECOMMENDATION_DL	SEEALSOLIST*
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
	
	/**
	 *  Generates the list of AlleleRule class based on the information of the resource files.
	 *  
	 *  */
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
}
