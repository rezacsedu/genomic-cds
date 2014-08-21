package safetycode;

import java.util.ArrayList;

/**
 * This class represents an allele combination related to a patient's genotype.
 * 
 * @author Jose Antonio Miñarro Giménez
 * */
public class AlleleElement implements GenotypeElement{
	/**Name of the gene related to the allele definitions.*/
	private String geneName;
	/**Name of the first allele definition in the combination. If both allele have the same name, it corresponds to a homozygous allele combination.*/
	private String alleleName1;
	/**Name of the second allele definition in the combination.*/
	private String alleleName2;
	
	/**
	 * Create the combination of allele for a particular gene in a patient's genotype.
	 * 
	 * @param alleleName1		Name of the first allele definition in the combination.
	 * @param alleleName2		Name of the second allele definition in the combination.
	 * */
	public AlleleElement(String geneName, String alleleName1, String alleleName2){
		this.geneName = geneName;
		
		alleleName1 = make_valid(alleleName1);
		alleleName2 = make_valid(alleleName2);
		
		if(alleleName1.compareTo(alleleName2)>0){
			String allele_aux = alleleName2;
			alleleName2 = alleleName1;
			alleleName1 = allele_aux;
		}
		
		if(alleleName1!=null){
			alleleName1 = make_valid(alleleName1);
			if(alleleName1.contains(geneName+"_")){
				this.alleleName1 = alleleName1.substring(alleleName1.indexOf(geneName+"_")+(geneName.length()+1));
			}else{
				this.alleleName1 = alleleName1;
			}
		}
		
		if(alleleName2!=null){
			alleleName2 = make_valid(alleleName2);
			if(alleleName2.contains(geneName+"_")){
				this.alleleName2 = alleleName2.substring(alleleName2.indexOf(geneName+"_")+(geneName.length()+1));
			}else{
				this.alleleName2 = alleleName2;
			}
		}
	}
	
	
	/**
	 * Create the combination of allele for a particular gene in a patient's genotype.
	 * 
	 * @param criteriaSyntax	String of the allele combination in alphabetical order.
	 * */
	public AlleleElement(String geneName, String criteriaSyntax){
		this.geneName = geneName;
		this.alleleName1 = "null";
		this.alleleName2 = "null";
		
		if(criteriaSyntax.contains(";")){
			this.alleleName1 = criteriaSyntax.substring(0,criteriaSyntax.indexOf(";"));
			this.alleleName2 = criteriaSyntax.substring(criteriaSyntax.indexOf(";")+1);
		}
		
		if(alleleName1.compareTo(alleleName2)>0){
			String allele_aux = alleleName2;
			alleleName2 = alleleName1;
			alleleName1 = allele_aux;
		}
	}
	
	
	/**
	 * Get method that provides the name of the corresponding gene of the combination of alleles.
	 * 
	 * @return		Gene name that is related to the combination.
	 * */
	public String getGeneticMarkerName(){
		return geneName;
	}
	
	
	/**
	 * Get method that provides the name of the first allele in the combination.
	 * 
	 * @return		Name of the first allele that is related to the combination.
	 * */
	public String getVariant1(){
		return alleleName1;
	}
	
	
	/**
	 * Get method that provides the name of the second allele in the combination.
	 * 
	 * @return		Name of the second allele that is related to the combination.
	 * */
	public String getVariant2(){
		return alleleName2;
	}
	
	
	/**
	 * Get method that provides the string that represents the combination of alleles in alphabetical order.
	 * 
	 * @return		String of the allele combination in alphabetical order.
	 * */
	public String getCriteriaSyntax(){
		return alleleName1+";"+alleleName2;
	}
	
	/**
	 * Updates the information regarding the combination of alleles.
	 * 
	 * @param alleleName1	Name of the first allele definition in the combination.
	 * @param alleleName2	Name of the second allele definition in the combination.
	 * */
	public void setVariants(String alleleName1, String alleleName2){
		alleleName1 = make_valid(alleleName1);
		alleleName2 = make_valid(alleleName2);
		
		if(alleleName1.compareTo(alleleName2)>0){
			String allele_aux = alleleName2;
			alleleName2 = alleleName1;
			alleleName1 = allele_aux;
		}
		
		alleleName1 = make_valid(alleleName1);
		if(alleleName1.contains(geneName+"_")){
			this.alleleName1 = alleleName1.substring(alleleName1.indexOf(geneName+"_")+(geneName.length()+1));
		}else{
			this.alleleName1 = alleleName1;
		}
		
		alleleName2 = make_valid(alleleName2);
		if(alleleName2.contains(geneName+"_")){
			this.alleleName2 = alleleName2.substring(alleleName2.indexOf(geneName+"_")+(geneName.length()+1));
		}else{
			this.alleleName2 = alleleName2;
		}
	}
	
	/**
	 * It clones the information of the instance into another instance.
	 * 
	 * @return	It produces an exactly copy of the instance.
	 * */
	public AlleleElement clone(){
		return (new AlleleElement(geneName,alleleName1,alleleName2));  
	}
	
	
	/**
	 * Get the list of URIs of the related classes in the ontology that corresponds to the Allele variant.
	 * 
	 * @return It returns a list with a homozygous class URI if the allele variants are equal or two class URIs if the alleles are different. If one of the alleles are null the list will be empty.
	 * */
	public ArrayList<String> getOntologyClassURIs(){
		ArrayList<String> resultURIs = new ArrayList<String>();
		if(alleleName1 == null || alleleName1.equals("null")|| alleleName2 == null ) return resultURIs;
		String uriString = "http://www.genomic-cds.org/ont/genomic-cds.owl#";
		if(alleleName1.equals(alleleName2)){//homozygous
			resultURIs.add(uriString+"human_with_homozygous_"+geneName+"_"+alleleName1);
		}else{//not homozygous
			//Variant 1
			resultURIs.add(uriString+"human_with_"+geneName+"_"+alleleName1);
			//Variant 2
			resultURIs.add(uriString+"human_with_"+geneName+"_"+alleleName2);
		}
		return resultURIs;
	}

	// It transforms ids in order to be used in an ontology URI.
	private String make_valid(String label){
		if(label==null || label.equals("null")) return "null";
		String valid_label = label.replace("*","star_");
		valid_label = valid_label.replace("#","_hash");
		valid_label = valid_label.replaceAll("[\\[\\]()\\s/:;]","_");
		return valid_label;
	}
}
