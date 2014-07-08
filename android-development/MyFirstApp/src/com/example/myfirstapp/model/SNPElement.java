package com.example.myfirstapp.model;

import java.util.ArrayList;

public class SNPElement implements GenotypeElement {
	/**Id of the SNP that represent the combination.*/
	private String rsid;
	/**Name of the first snp variant in the combination.*/
	private String snp1;
	/**Name of the second snp variant in the combination.*/
	private String snp2;
	
	
	/**
	 * Create the combination of SNP for a particular patient's genotype.
	 * 
	 * @param snpName1		Name of the first SNP variation in the combination.
	 * @param snpName2		Name of the second SNP variation in the combination.
	 * */
	public SNPElement(String rsid, String snpName1, String snpName2){
		this.rsid = rsid;
		this.snp1 = "null";
		this.snp2 = "null";
		if(snpName1!=null && snpName1.length()>0 && snpName2!=null && snpName2.length()>0){
		//if(snpName1!=null && !snpName1.isEmpty() && snpName2!=null && !snpName2.isEmpty()){
			if(snpName1.compareTo(snpName2)>0){
				String snp_aux = snpName2;
				snpName2 = snpName1;
				snpName1 = snp_aux;
			}
		
			if(snpName1!=null){
				if(snpName1.contains(rsid+"_")){
					this.snp1 = snpName1.substring(snpName1.indexOf(rsid+"_")+(rsid.length()+1));
				}else{
					this.snp1 = snpName1;
				}
			}
		
			if(snpName2!=null){
				if(snpName2.contains(rsid+"_")){
					this.snp2 = snpName2.substring(snpName2.indexOf(rsid+"_")+(rsid.length()+1));
				}else{
					this.snp2 = snpName2;
				}
			}
		}
	}

	
	/**
	 * Create the combination of SNPs for a particular rsid in a patient's genotype.
	 * 
	 * @param criteriaSyntax	String of the SNPs combination in alphabetical order.
	 * */
	public SNPElement(String rsid, String criteriaSyntax){
		this.rsid = rsid;
	
		if(criteriaSyntax.contains(";")){
			this.snp1 = criteriaSyntax.substring(0,criteriaSyntax.indexOf(";"));
			this.snp2 = criteriaSyntax.substring(criteriaSyntax.indexOf(";")+1);
		}
		if(snp1!=null && snp2!=null){
			if(snp1.compareTo(snp2)>0){
				String snp_aux = snp2;
				snp2 = snp1;
				snp1 = snp_aux;
			}
		}
	}
	
	
	/**
	 * Get method that provides the rsid of the corresponding SNP.
	 * 
	 * @return		The rsid that is related to the combination.
	 * */
	public String getGeneticMarkerName(){
		return rsid;
	}
	
	
	/**
	 * Get method that provides the name of the first variation in the combination.
	 * 
	 * @return		Name of the first SNP that is related to the combination.
	 * */
	public String getVariant1(){
		return snp1;
	}
	
	
	/**
	 * Get method that provides the name of the second variation in the combination.
	 * 
	 * @return		Name of the second SNP that is related to the combination.
	 * */
	public String getVariant2(){
		return snp2;
	}
	
	
	/**
	 * Get method that provides the string that represents the combination of SNPs in alphabetical order.
	 * 
	 * @return		String of the SNP variation in alphabetical order.
	 * */
	public String getCriteriaSyntax(){
		return snp1+";"+snp2;
	}
	
	/**
	 * Updates the information regarding the combination of SNPs.
	 * 
	 * @param snpName1	Name of the first SNP variation in the combination.
	 * @param snpName2	Name of the second SNP variation in the combination.
	 * */
	public void setVariants(String snpName1, String snpName2){
		this.snp1 = "null";
		this.snp2 = "null";
		
		if(snpName1!=null && snpName2!=null){
			if(snpName1.contains(rsid+"_")){
				this.snp1 = snpName1.substring(snpName1.indexOf(rsid+"_")+(rsid.length()+1));
			}else{
				this.snp1 = snpName1;
			}
			
			if(snpName2.contains(rsid+"_")){
				this.snp2 = snpName2.substring(snpName2.indexOf(rsid+"_")+(rsid.length()+1));
			}else{
				this.snp2 = snpName2;
			}
			if(compareVariants(snp1,snp2)>0){
				String snp_aux = snp2;
				snp2 = snp1;
				snp1 = snp_aux;
			}
		}
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
		
		if(v1.equalsIgnoreCase(v2)) return 0;
		if(v2.equals("D")) return -1;
		if(v1.length()>v2.length()) return 1;
		if(v1.compareTo(v2)>0) return 1;
		
		return -1;
	}
	
	
	
	/**
	 * It clones the information of the instance into another instance.
	 * 
	 * @return	It produces an exactly copy of the instance.
	 * */
	public SNPElement clone(){
		return (new SNPElement(rsid,snp1,snp2));  
	}

	
	/**
	 * Get the URIs of the related class in the ontology that corresponds to the SNP.
	 * 
	 * @return List with the URIs related to the SNP combination.
	 * */
	public ArrayList<String> getOntologyClassURIs(){
		ArrayList<String> resultURIs = new ArrayList<String>();
		resultURIs.add("http://www.genomic-cds.org/ont/MSC_classes.owl#human_with_genotype_"+rsid+"_variant_"+snp1+"_"+snp2); //E.g.: <human_with_genotype_rs1051266_C_C>
		return resultURIs;
	}
}
