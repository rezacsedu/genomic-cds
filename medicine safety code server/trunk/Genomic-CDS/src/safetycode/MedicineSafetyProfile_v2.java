package safetycode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import exception.BadFormedBase64NumberException;
import exception.BadFormedBinaryNumberException;
import exception.NotInitializedPatientsGenomicDataException;
import exception.NotPatientGenomicFileParsedException;
import exception.VariantDoesNotMatchAnAllowedVariantException;

import utils.Common;
import utils.OntologyManagement;

public class MedicineSafetyProfile_v2 {
	
	/** Singleton instance of the OntologyManagement class which contains the base ontological model. */
	private OntologyManagement om 		= null;
	private Genotype patientGenotype	= null;
	private Coding_module cod_mod		= null;
	/**
	 * Constructor of the class. It initializes the model of the pharmacogenomics dataset.
	 * 
	 * @param ontologyFile	Path of the ontology file in the local disk.
	 * @return		New instance of MedicineSafetyProfileOptimized class.
	 * */
	public MedicineSafetyProfile_v2(String ontologyFile) {
		try {
			om = OntologyManagement.getOntologyManagement(ontologyFile);
			//patientGenotype = new Genotype(om.getListGenotypeElements());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ERROR in Path = " + ontologyFile);
		}
	}
	
	public OntologyManagement getOntologyManagement(){
		return om;
	}
	
	public void initializeCodingModule(){
		ArrayList<GenotypeElement> listGenotypeElements;
		try {
			listGenotypeElements = om.getDefaultGenotypeElement();
			patientGenotype = new Genotype (listGenotypeElements);
		} catch (VariantDoesNotMatchAnAllowedVariantException e) {
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<Genetic_Marker_Group> getListGenotypeGroups(){
		return om.getListGeneticMarkerGroups();
	}
	
	public Genotype getGenotype(){
		return patientGenotype;
	}
	
	public void setGenotype(Genotype genotype){
		if(genotype==null){
			try {
				this.patientGenotype = new Genotype(om.getListGenotypeElements());
			} catch (VariantDoesNotMatchAnAllowedVariantException e) {
				e.printStackTrace();
			}
		}else{
			this.patientGenotype=genotype;
		}
	}
	
	/**
	 * It initializes the model with core pharmacogenomic dataset.
	 * 
	 * @param ontologyFile	Path of the ontology file in the local disk.
	 * */
	/*private void initializeModel(String ontologyFile) {
		try {
			om = OntologyManagement.getOntologyManagement(ontologyFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ERROR in Path = " + ontologyFile);
		}
	}*/
	
	
	/**
	 * It parses the input file with the corresponding file parser.
	 * 
	 * @param fileStream					The input file to parse.
	 * @param strandOrientationOfInputData	The orientation of the input strand to parse.
	 * @param typeFileFormat				The type of file format that is used by the input file.
	 * @return	The processing report of the input file.
	 * @throws VariantDoesNotMatchAnAllowedVariantException 
	 * */
	public String parseFileStream(InputStream fileStream, String strandOrientationOfInputData, int typeFileFormat) throws VariantDoesNotMatchAnAllowedVariantException{
		
		FileParser fp= FileParserFactory.getFileParser(typeFileFormat, om);
		String report = fp.parse(fileStream, strandOrientationOfInputData);
		
		patientGenotype = new Genotype ( fp.getListSNPElements(),om);
		
		return report;
	}
	
	
	/**
	 * It parses the input file with the corresponding file parser and default strand orientation.
	 * 
	 * @param fileStream					The input file to parse.
	 * @param typeFileFormat				The type of file format that is used by the input file.
	 * @return	The processing report of the input file.
	 * @throws VariantDoesNotMatchAnAllowedVariantException 
	 * */
	public String parseFileStream(InputStream fileStream, int typeFileFormat) throws VariantDoesNotMatchAnAllowedVariantException{
		
		FileParser fp= FileParserFactory.getFileParser(typeFileFormat, om);
		String report = fp.parse(fileStream, Common.FORWARD_ORIENTATION);
		patientGenotype = new Genotype ( fp.getListSNPElements(),om);
		
		return report;
	}
	
	
	/**
	 * Get method to obtain the 64 base representation of the patient variants
	 * of the markers.
	 * 
	 * @return The string associated to the variants of the patient or null if it is not yet defined.
	 * @throws NotPatientGenomicFileParsedException 
	 * @throws BadFormedBinaryNumberException 
	 * */
	public String getBase64ProfileString() throws NotPatientGenomicFileParsedException, BadFormedBinaryNumberException {
		if (patientGenotype==null){
			throw new NotPatientGenomicFileParsedException("ERROR: No patient's genomic data has been processed yet!");
		}
		if(cod_mod==null){
			cod_mod = new Coding_module(om.getListGeneticMarkerGroups());
		}
		
		return cod_mod.codeListGeneticVariations(patientGenotype.getListGenotypeElements());
	}
	
	
	/**
	 * Create the patient model that is related to the base64Profile.
	 * 
	 * @param base64Profile Base 64 number that represent the binary codification of a patient genotype.
	 * @throws BadFormedBase64NumberException
	 * @throws VariantDoesNotMatchAnAllowedVariantException 
	 * */
	public void readBase64ProfileString(String base64Profile) throws BadFormedBase64NumberException, VariantDoesNotMatchAnAllowedVariantException {
		if(cod_mod==null){
			cod_mod = new Coding_module(om.getListGeneticMarkerGroups());
		}
		ArrayList<GenotypeElement> listGenotypeElements = cod_mod.decodeListGenotypeVariations(base64Profile);
		if(patientGenotype==null){
			patientGenotype = new Genotype(listGenotypeElements);
		}else{
			patientGenotype.setGenotypeElements(listGenotypeElements);
		}
	}
	
	/**
	 * Get the drug recommendations based on patient's genotype and grouped by the drug which are relevant for the recommendation.
	 * 
	 * @return List of triggered rules based on patient's genotype.
	 * */
	public HashMap<String, ArrayList<DrugRecommendation>> obtainDrugRecommendations() throws NotInitializedPatientsGenomicDataException {
		HashMap<String,ArrayList<DrugRecommendation>> mapDrugRecommendations = null;
		if(patientGenotype==null){
			throw new NotInitializedPatientsGenomicDataException("The patient's genotype was not initialized");
		}else{
			mapDrugRecommendations = new HashMap<String,ArrayList<DrugRecommendation>>();
			ArrayList<DrugRecommendation> listRecommendations = om.getListDrugRecommendation();
			for(DrugRecommendation dr: listRecommendations){
				if(dr.matchPatientProfile(patientGenotype)){
					String drug_name = dr.getDrugName();
					if(mapDrugRecommendations.containsKey(drug_name)){
						 mapDrugRecommendations.get(drug_name).add(dr);
					}else{
						ArrayList<DrugRecommendation> listMatchedRecommendations = new ArrayList<DrugRecommendation>();
						listMatchedRecommendations.add(dr);
						mapDrugRecommendations.put(drug_name, listMatchedRecommendations);
					}
				}
			}
		}
		return mapDrugRecommendations;
	}
	
	/**
	 * Get the list of genetic markers involved in the treatment recommendations rules related to the list of drugs provided.
	 * 
	 * @param listDrugs		List of drug terms which will be used to find their related genetic markers.
	 * @return				List of genetic markers related to the provided list of drugs.
	 * */
	public ArrayList<Genetic_Marker_Group> getGenotypeGroupsRelatedToDrugs(String[] listDrugs){
		ArrayList<Genetic_Marker_Group> display_groups = new ArrayList<Genetic_Marker_Group>();
		ArrayList<Genetic_Marker_Group> list_groups = om.getListGeneticMarkerGroups();
		for(Genetic_Marker_Group gmg: list_groups){
			ArrayList<DrugRecommendation> list_recommendations = om.getListDrugRecommendation();
			for(DrugRecommendation dr: list_recommendations){
				boolean next_gmg = false;
				for(int i=0;i<listDrugs.length;i++){
					if(dr.getDrugName().equals(listDrugs[i])){
						String rule = dr.getRuleDescription();
						if(rule.contains(gmg.getGeneticMarkerName())){
							display_groups.add(gmg);
							next_gmg = true;
							break;
						}
					}
				}
				if(next_gmg) break;
			}
		}
		return display_groups;
	}
	
	
	public ArrayList<String> getGenotypeStatistics(){
		return patientGenotype.getPatientInferredStatistics(om);
	}
}
