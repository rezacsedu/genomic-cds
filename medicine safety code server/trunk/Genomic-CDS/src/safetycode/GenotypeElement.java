package safetycode;


/**
 * This interface represents the common methods of a genotype element. In particular, a genotype element can be a SNP or haplotype variant.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 * */
public interface GenotypeElement {

	/**
	 * Get method that provides the genotype marker name.
	 * 
	 * @return		The name of the genotype marker.
	 * */
	public String getGeneticMarkerName();
	
	/**
	 * Get method that provides the name of the first variant in the combination.
	 * 
	 * @return		Name of the first variant that is related to the combination.
	 * */
	public String getVariant1();
	
	/**
	 * Get method that provides the name of the second variant in the combination.
	 * 
	 * @return		Name of the second variant that is related to the combination.
	 * */
	public String getVariant2();
	
	/**
	 * Updates the information regarding the combination of genotype variant.
	 * 
	 * @param variant1	Name of the first genotype variation in the combination.
	 * @param variant2	Name of the second genotype variation in the combination.
	 * */
	public void setVariants(String variant1, String variant2);
	
	/**
	 * Get method that provides the string that represents the combination of variants in alphabetical order.
	 * 
	 * @return		String of the variatians in alphabetical order.
	 * */
	public String getCriteriaSyntax();
	
	/**
	 * It clones the information of the instance into another instance.
	 * 
	 * @return	It produces an exactly copy of the instance.
	 * */
	public GenotypeElement clone();
}
