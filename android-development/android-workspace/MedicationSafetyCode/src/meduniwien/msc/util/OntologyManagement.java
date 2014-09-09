package meduniwien.msc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.content.Context;
import android.content.res.AssetManager;

import meduniwien.msc.exception.BadRuleDefinitionException;
import meduniwien.msc.model.AlleleGroup;
import meduniwien.msc.model.DrugRecommendation;
import meduniwien.msc.model.GeneticMarkerGroup;
import meduniwien.msc.model.SNPsGroup;



/**
 * It represents the content of the Genomic CDS ontology that is needed to match cds rules and genetic profiles.
 * 
 * @author Jose Antonio Miñarro Giménez
 * */
public class OntologyManagement {
	/** List of SNP groups defined in the ontology. A group could be rs12516 with the alleles rs12516(null;null), rs12516(A;A), rs12516(A;G) and rs12516(G;G).*/
	private ArrayList<SNPsGroup>			listSNPsGroups			= null;
	/** List of Haplotype groups defined in the ontology. A group could be BRCA1 with the alleles BRCA1(null;null), BRCA1(1;1), BRCA1(1;2), ...*/
	private ArrayList<AlleleGroup>			listAlleleGroups		= null;
	/** List of drug recommendation rules defined in the ontology.*/
	private ArrayList<DrugRecommendation>	listDrugRecommendations	= null;
	/** List of phenotype rules that are used to replace their logical description into drug recommendations logical description.*/
	private HashMap<String,String>			listPhenotypeRules = null;
	/** List of Genetic marker groups used to define a patient's genotype. It contains all haplotype groups and some SNP groups.*/
	private ArrayList<GeneticMarkerGroup>	listGeneticMarkers		= null;
	
	private static OntologyManagement singleton = null;
	
	public static OntologyManagement getOntologyManagement(Context context){
		if(singleton == null){
			AssetManager am = context.getAssets();
			singleton = new OntologyManagement(am);
		}
		return singleton;
	}
	
	/**Constructor of the class that initialize the list of SNPs, Haplotypes, cds rules and the group of genetic markers used for defining a genetic profile.*/
	public OntologyManagement(AssetManager am){
		
		InputStream snpGroups=null;
		try {
			snpGroups = am.open(Common.tabSeparatedSNPGroups);
			initializeSNPsGroups(snpGroups);
			snpGroups.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		InputStream alleleGroups=null;
		try {
			alleleGroups = am.open(Common.tabSeparatedAlleleGroups);
			initializeAlleleGroups(alleleGroups);
			alleleGroups.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		initializeGeneticMarkerGroups();
		
		InputStream phenotypeRules=null;
		InputStream drugRecommendations=null;
		try {
			phenotypeRules = am.open(Common.tabSeparatedPhenotypeRules);
			drugRecommendations = am.open(Common.tabSeparatedCDSRules);
			initializeDrugRecommendations(phenotypeRules,drugRecommendations);
			phenotypeRules.close();
			drugRecommendations.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get method that retrieves the list of genetic marker groups related to a genetic profile.
	 * @return	The list of genetic marker groups related to a genetic profile.
	 * */
	public ArrayList<GeneticMarkerGroup> getListGeneticMarkerGroups(){
		return listGeneticMarkers;
	}
	
	/**
	 * Get method that retrives the list of drug recommendations defined in the ontology.
	 * @return	The list of drug recommendations defined in the ontology.
	 * */
	public ArrayList<DrugRecommendation> getListDrugRecommendations(){
		return listDrugRecommendations;
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
		
	/** Select the list of genetic markers used to represent a patient's genotype.*/
	private void initializeGeneticMarkerGroups(){
		listGeneticMarkers	= new ArrayList<GeneticMarkerGroup>();
		ArrayList<GeneticMarkerGroup> list_gmg = new ArrayList<GeneticMarkerGroup>();
		list_gmg.addAll(listAlleleGroups);
		list_gmg.addAll(listSNPsGroups);
		Collections.sort(list_gmg);
		for(GeneticMarkerGroup gmg: list_gmg){
			if(gmg.getRank()>=0) listGeneticMarkers.add(gmg);
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
}
