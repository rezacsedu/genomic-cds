package meduniwien.msc.model;

import java.util.ArrayList;



public class Genotype {
	/**List of SNP gathered from genotype files in the formats 23andMe or VCF.*/
	private ArrayList<SNPElement> listSNPs = null;
	/**List of Genotype elements used to trigger the drug dosage recommendation rules.*/
	private ArrayList<GenotypeElement> listGenotypeElements = null;
	
	
	/**Construct the patient's genotype based on the list of SNPs and Allele markers.*/
	public Genotype(ArrayList<GenotypeElement> listGenotypeElements){
		this.listGenotypeElements = listGenotypeElements;
	}

	/**
	 * Set the list of patient's genotype elements.
	 * 
	 * @param listGenotypeElments	List of patient's genotype elements.
	 * */
	public void setGenotypeElements(ArrayList<GenotypeElement> listGenotypeElements){
		this.listGenotypeElements = listGenotypeElements;
	}
	
	/**Get the list of SNP variants associated to the patient's genotype.
	 * 
	 * @return List of SNPs gathered from the 23andMe or VCF genotype files.
	 * */
	public ArrayList<SNPElement> getListSNPelements(){
		return listSNPs;
	}
	
	/**Get the list of SNP variants and alleles associated to the patient's genotype.
	 * 
	 * @return List of SNPs and Alleles that represent the patient's genotype.
	 * */
	public ArrayList<GenotypeElement> getListGenotypeElements(){
		return listGenotypeElements;
	}
		
	/** Overrides the toString() method to show the list of genetic marker elements of the genotype instance.*/	
	public String toString(){
		String desc="";
		for(GenotypeElement ge: listGenotypeElements){
			//if(!desc.isEmpty()){
			if(desc.length()>0){	
				desc+="\n";
			}
			desc+="["+ge.getGeneticMarkerName()+"]->"+ge.getCriteriaSyntax();
		}
		return desc;
	}
}
