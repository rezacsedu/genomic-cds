package com.example.myfirstapp.model;

import java.util.ArrayList;

import com.example.myfirstapp.exception.VariantDoesNotMatchAnyAllowedVariantException;
import com.example.myfirstapp.util.Common;

public class SNPsGroup implements GeneticMarkerGroup {

	/**rsid of the set of allele definitions*/
	private String				rsid;
	/**List of all possible SNPs combinations. For two SNP definitions ("A" and "C") the combinations would be: ("null;null", "A;A", "A;C", "C;C")*/
	private ArrayList<String>	listSNPs;
	/**Rank of the group in the genotype code. This will be use for coding/decoding reasons.*/
	private int					rank;
	/**The orientation of the VCF genotype.*/
	private String orientation;
	/**The reference combination for a VCF genotype.*/
	private String vcf_format_reference = "null;null";
	/**List of genotype formats that include the SNP*/
	private ArrayList<String> listFormats;
	
	/**
	 * Create the group of SNPs definitions with all possible combination for each rsid.
	 * 
	 * @param rsid			It is the rsid that corresponds to the SNP definition.
	 * @param listSNPs		The list of SNPs variations associated to the corresponding rsid. The list does not need to be sorted.
	 * @param rank			The rank of the group of SNP variants.
	 * @param orientation	The orientation of the VCF genotype.
	 * @param vcf_reference	The reference combination for a VCF genotype. Can be null or (x;x)
	 * @param listFormats	The list of genotype file formats that include the SNP.
	 * */
	public SNPsGroup(String rsid, ArrayList<String> listSNPs, int rank, String orientation, String vcf_format_reference, ArrayList<String> listFormats){
		this.rsid = rsid;
		this.orientation = orientation;
		this.vcf_format_reference = vcf_format_reference;
		this.listFormats = listFormats;
		
		for(int i=0;i<listSNPs.size();i++){
			String label = listSNPs.get(i);
			if(label.contains(rsid)){
				label = label.substring(label.indexOf("_")+1);
			}
			listSNPs.set(i, label);
		}
		
		//sortCollection(listSNPs);
		this.listSNPs	= getSNPCombinationList(listSNPs);
		this.rank		= rank;
	}	

	/**
	 * It provides the criteria syntax of a genetic marker combination in a particular position. The set of combinations is sorted by alphabetical order and includes the null;null at the position '0'.
	 * 
	 * @param position	The position of the combination in the set of 2-multicombination.
	 * @return	The textual description of the combination.
	 * */
	public String getGeneticMarkerVariantName(int position) {
		if(position == 0){
			return "null;null";
		}
		String variant1 = "";
		String variant2 = "";
		int n = listSNPs.size();

		for(int i=0;i<n;i++){
			int k = position - (i*n-((i*(i-1))/2)) + (i-1);
			if(k < n){
				variant1 = listSNPs.get(i);
				variant2 = listSNPs.get(k);
				return variant1+";"+variant2;
			}
		}
		return "null;null";
		
		/*if(position < listSNPs.size() && position >= 0){
			return listSNPs.get(position);
		}
		return null;*/
	}

	/**
	 * It provides the position of one particular combination by its criteria syntax. The set of combinations is sorted by alphabetical order and includes the null;null at the position '0'.
	 * 
	 *  @param criteriaSyntax	The textual description of the SNP combination.
	 *  @return		The position of the combination in the set of allele 2-multicombination.
	 * */
	public int getPositionGeneticMarker(String criteriaSyntax) {
		
		if(criteriaSyntax.contains("null")) return 0;
		int position = -1;
		int n = listSNPs.size();//list n variants in the groups.
		String[] tokens = criteriaSyntax.split(";");
		int pos1 = listSNPs.indexOf(tokens[0]);
		int pos2 = listSNPs.indexOf(tokens[1]);
		if(pos1>pos2){
			int pos_aux = pos1;
			pos1 = pos2;
			pos2 = pos_aux;
		}
		if(pos1>=0 && pos2>=0){
			position = pos1*n+pos2+1-((pos1*(pos1+1))/2);
		}
		return position;
			
		/*
		for(int position = 0; position < listSNPs.size(); position++){
			if(listSNPs.get(position).equalsIgnoreCase(criteriaSyntax)){
				return position;
			}
		}
		return -1;*/
	}
	
	/**
	 * Get method that indicates the rank of the SNP in a genotype.
	 * 
	 * @return	The rank of the combination set.
	 * */
	public int getRank() {
		return rank;
	}
	
	/**
	 * It indicates the number of combinations that can be formed with the group of SNPs.
	 * 
	 * @return		Number of 2-combinations with repetition from the group of SNP variations.
	 * */
	public int getNumberOfVariants() {
		return Common.get_kCombinations(listSNPs.size(), 2)+1;
		//return listSNPs.size();
	}

	/**
	 * Implements the compareTo method to sort the groups based on their rank number.
	 * 
	 * @param gmg	It represents an instance of Genetic_Marker_Group.
	 * @return		It returns a negative integer if its rank is lower than the rank of gmg, positive integer if its rank is greater than the rank of gmg, and 0 if the ranks are the same. 
	 * */
	public int compareTo(GeneticMarkerGroup gmg) {
		return (rank - gmg.getRank());
	}

	/**
	 * Get method that provides the rsid related to the combination set.
	 * 
	 * @return	The rsid related to the combination of the group of SNPs.
	 * */
	public String getGeneticMarkerName() {
		return rsid;
	}
	
	/**
	 * Get list SNP elements of the combination set.
	 * 
	 * @return	The list of SNPs that form this set.
	 * */
	public ArrayList<String> getListElements(){
		return listSNPs;
	}
	
	
	/**
	 * This method generates the 2-combinations with repetition from a group of SNP definitions.
	 * The set of combinations is sorted by the particular order based on:  The order of the variants in the SNP combination is: (1) The "null;null"; (2) The "D" of deletion; (3) single-nucleotides in alphabetical order "A;A","A;C","A;G","A;T", ...; and (4) multi-nucleotides, first the shorter ones in alphabetical order.
	 * The set of combinations includes the null element at the position '0' to represent the lack of evidence in a patient's genotype. 
	 * 
	 * @param listSNP	List of SNP definitions associated to the rsid group.
	 * @return	List of all combinations from the set of SNPs in alphabetical order.
	 * */
	private ArrayList<String> getSNPCombinationList(ArrayList<String> listSNPs){
		
		ArrayList<String> listCombinations = new ArrayList<String>();
		if(listSNPs!=null){
			listCombinations.addAll(listSNPs);
		}
		sortCollection(listCombinations);
		return listCombinations;
	}
	
	
	/**
	 * Get the default strand orientation of the SNP.
	 * 
	 * @return	The orientation of the reference SNP.
	 * */
	public String getStrandOrientation(){
		return orientation;
	}

	
	/**
	 * Get the default strand orientation of the SNP.
	 * 
	 * @return	The orientation of the reference SNP.
	 * */
	public String getVCFReference(){
		return vcf_format_reference;
	}
	
	/**
	 * Get the list of genotype formats that include the SNP.
	 * 
	 * @return	List of genotype formats, 
	 * */
	public ArrayList<String> getVCF_formats(){
		return listFormats;
	}
	
	/**
	 * Check if the nucleotides are a valid combination for the SNP group.
	 * 
	 *  @param variant1		The first nucleotide that is part of the SNP combination.
	 *  @param variant2		The second nucleotide that is part of the SNP combination.
	 *  @return		It returns 'true' when the nucleotides are valid for the SNP group.
	 * */
	public boolean allowedVariants(String variant1, String variant2){
		if(variant1.equals("I")) variant1 = getInsertionVariant();
		if(variant2.equals("I")) variant2 = getInsertionVariant();
		
		if(compareVariants(variant1,variant2)>0){
			String variant_aux = variant2;
			variant2 = variant1;
			variant1 = variant_aux;
		}
		
		return (getPositionGeneticMarker(variant1+";"+variant2)>=0);
		//return 	(listSNPs.contains(variant1+";"+variant2));
	}

	/**
	 * Get the SNPElement from the position in the group.
	 * 
	 * @param position	The position of the combination in the set of 2-multicombination.
	 * @return The SNP element associated to the position in the group.
	 * */
	public SNPElement getGenotypeElement(int position)  throws VariantDoesNotMatchAnyAllowedVariantException{
		/*if(listSNPs.size()>position && position >=0){
			return new SNPElement(rsid,listSNPs.get(position));
		}*/
		if(position<getNumberOfVariants() && position >=0){
			return new SNPElement(rsid,getGeneticMarkerVariantName(position));
		}
		throw new VariantDoesNotMatchAnyAllowedVariantException("The variant in position "+position+" does not exist. Please use other position in [0,"+(listSNPs.size()-1)+"]");
	}
	
	/**
	 * Similar to compareTo of String class but using a different order.  The order of the variants in the SNP combination is: (1) The "null;null"; (2) The "D" of deletion; (3) single-nucleotides in alphabetical order "A;A","A;C","A;G","A;T", ...; and (4) multi-nucleotides, first the shorter ones in alphabetical order. 
	 * 
	 * @param v1	The first SNP variant.
	 * @param v2	The second SNP variant.
	 * 
	 * @return		It returns 0 is v1 is equal to v2. It returns 1 if v1 is greater than v2. And it returns -1 if v1 is lower than v2.
	 * */
	private int compareVariants(String v1,String v2){
		if(v1!=null && v2!=null){
			if(v1.equalsIgnoreCase(v2)) return 0;
			if(v2.equals("D")) return -1;
			if(v1.length()>v2.length()) return 1;
			if(v1.compareTo(v2)>0) return 1;
			
			return -1;
		}
		System.out.println("ERROR in => "+rsid+"_"+v1+";"+v2);
		return 0;
	}
	
	
	/**
	 * Method to sort the element in an arraylist.
	 * */
	private void sortCollection(ArrayList<String> list){
		for(int i=0;i<list.size()-1;i++){
			for(int j=i+1;j<list.size();j++){
				if(compareVariants(list.get(i), list.get(j))>0){
					String aux = list.get(i);
					list.set(i,list.get(j));
					list.set(j,aux);
				}
			}
		}
	}
	
	
	/**
	 * Find the SNP variant related to an insertion in the SNP. In practice, we assume all variants with more than one nucleotide to be an insertion variant.
	 *
	 * @return It provides the corresponding string to the insertion if it exists, or "null" string otherwise.  
	 * */
	public String getInsertionVariant(){
		String result_variant="null";
		if(listSNPs!=null){
			
			for(String variant: listSNPs){
				if(variant.length()>1){
					return variant;
				}
				if(!variant.equals("D") && !variant.equals("null")){
					result_variant = variant;
				}
			}
		}
		return result_variant;
	}
}
