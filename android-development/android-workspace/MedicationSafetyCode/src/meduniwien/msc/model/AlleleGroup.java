package meduniwien.msc.model;

import java.util.ArrayList;
import java.util.Collections;

import meduniwien.msc.exception.VariantDoesNotMatchAnyAllowedVariantException;
import meduniwien.msc.util.Common;

/**
 * 	This class represents the set of haplotype variants related to one gene. 
 * 
 * @author Jose Antonio Miñarro Giménez
 * */
public class AlleleGroup implements GeneticMarkerGroup{
	
	/**Gene name of the set of allele definitions*/
	private String				geneName;
	/**List of all possible allele combinations. For two allele definitions ("star_1" and "star_2") the combinations would be: ("null;null", "star_1;star_1", "star_1;star_2", "star_2;star_2")*/
	private ArrayList<String>	listAlleles;
	/**Rank of the group in the genotype code. This will be use for coding/decoding reasons.*/
	private int					rank;
	
	/**
	 * Create the group of allele definitions with all possible combination for the gene name.
	 * 
	 * @param geneName		It is the name of the gene that corresponds to every allele definition.
	 * @param listAlleles	The list of allele definitions associated to the corresponding gene. The list does not need to be sorted.
	 * @param rank			The rank of the group of alleles.
	 * */
	public AlleleGroup(String geneName, ArrayList<String> listAlleles, int rank){
		this.geneName		= geneName;
		
		for(int i=0;i<listAlleles.size();i++){
			String label = listAlleles.get(i);
			label = make_valid(label);
			if(label.contains(geneName)){
				label = label.substring(label.indexOf("_")+1);
			}
			listAlleles.set(i, label);
		}
		
		Collections.sort(listAlleles);
		this.listAlleles	= getAlleleCombinationList(listAlleles);
		this.rank			= rank;
	}
	
	/**
	 * It indicates the number of combinations that can be formed with the group of alleles.
	 * 
	 * @return		Number of 2-combinations with repetition from the group of allele definitions.
	 * */
	public int getNumberOfVariants(){
		return Common.get_kCombinations(listAlleles.size(), 2)+1;
		//return listAlleles.size();
	}
	
	/**
	 * It provides the position of one particular combination by its criteria syntax. The set of combinations is sorted by alphabetical order and includes the null;null at the position '0'.
	 * 
	 *  @param criteriaSyntax	The textual description of the combination.
	 *  @return		The position of the combination in the set of allele 2-multicombination.
	 * */
	public int getPositionGeneticMarker(String criteriaSyntax){
		
		if(criteriaSyntax.contains("null")) return 0;
		int position = -1;
		int n = listAlleles.size();//list n variants in the groups.
		String[] tokens = criteriaSyntax.split(";");
		int pos1 = listAlleles.indexOf(tokens[0]);
		int pos2 = listAlleles.indexOf(tokens[1]);
		if(pos1>pos2){
			int pos_aux = pos1;
			pos1 = pos2;
			pos2 = pos_aux;
		}
		if(pos1>=0 && pos2>=0){
			position = pos1*n+pos2+1-((pos1*(pos1+1))/2);
		}
		return position;
	}
	
	
	/**
	 * It provides the criteria syntax of a combination in a particular position. The set of combinations is sorted by alphabetical order and includes the null;null at the position '0'.
	 * 
	 * @param position	The position of the combination in the set of 2-multicombination.
	 * @return		The textual description of the combination.
	 * */
	public String getGeneticMarkerVariantName(int position){
		if(position == 0){
			return "null;null";
		}
		String variant1 = "";
		String variant2 = "";
		int n = listAlleles.size();
		
		for(int i=0;i<n;i++){
			int k = position - (i*n-((i*(i-1))/2)) + (i-1);
			if(k < n){
				variant1 = listAlleles.get(i);
				variant2 = listAlleles.get(k);
				return variant1+";"+variant2;
			}
		}
		return "null;null";
	}
	
	/**
	 * Get method that indicates the rank of the gene alleles in a genotype.
	 * 
	 * @return	The rank of the combination set.
	 * */
	public int getRank(){
		return rank;
	}
	
	/**
	 * Get method that provides the gene name related to the combination set.
	 * 
	 * @return	The gene name related to the combination of the group of allele.
	 * */
	public String getGeneticMarkerName(){
		return geneName;
	}
	
	/**
	 * Get list Allele elements of the combination set.
	 * 
	 * @return	The list of alleles that form this set.
	 * */
	public ArrayList<String> getListElements(){
		return listAlleles;
	}
	
	/**
	 * This method generates the 2-combinations with repetition from a group of allele definitions.
	 * The set of combinations is sorted by alphabetical order.
	 * The set of combinations includes the null element at the position '0' to represent the lack of evidence in a patient's genotype. 
	 * 
	 * @param listAlleles	List of allele definitions associated to the gene.
	 * @return		List of all combinations from the set of alleles in alphabetical order.
	 * */
	private ArrayList<String> getAlleleCombinationList(ArrayList<String> listAlleles){
		/*ArrayList<String> listCombinations = new ArrayList<String>();
		Collections.sort(listAlleles);
		for(int i=0;i<listAlleles.size();i++){
			for(int j=i;j<listAlleles.size();j++){
				listCombinations.add(listAlleles.get(i)+";"+listAlleles.get(j));
			}
		}
		listCombinations.add(0,"null;null");
		return listCombinations;*/
		ArrayList<String> listCombinations = new ArrayList<String>();
		if(listAlleles!=null){
			listCombinations.addAll(listAlleles);
		}
		Collections.sort(listCombinations);
		return listCombinations;
	}
	
	/**
	 * Implements the compareTo method to sort the groups based on their rank number.
	 * 
	 * @param gmg	It represents an instance of AlleleGroup class.
	 * @return		It returns a negative integer if its rank is lower than the rank of gmg, positive integer if its rank is greater than the rank of gmg, and 0 if the ranks are the same. 
	 * */
	public int compareTo(GeneticMarkerGroup gmg) {
		return (rank - gmg.getRank());
	}
	
	// It transforms ids in order to be used in an ontology URI.
	private String make_valid(String label){
		String valid_label = label.replace("*","star_");
		valid_label = valid_label.replace("#","_hash");
		valid_label = valid_label.replaceAll("[\\[\\]()\\s/:;]","_");
		valid_label = valid_label.replaceAll("__", "_");
		if(valid_label.startsWith("_")){
			valid_label = valid_label.substring(1);
		}
		if(valid_label.endsWith("_")){
			valid_label = valid_label.substring(0,valid_label.length()-1);
		}
		return valid_label;
	}

	
	/**
	 * Get the AlleleElement from the position in the group.
	 * 
	 * @param position	The position of the combination in the set of 2-multicombination.
	 * @return The allele element associated to the position in the group.
	 * */
	public AlleleElement getGenotypeElement(int position) throws VariantDoesNotMatchAnyAllowedVariantException{
	
		if(position<getNumberOfVariants() && position >= 0){
			return new AlleleElement(geneName,getGeneticMarkerVariantName(position));			
		}
		
		throw new VariantDoesNotMatchAnyAllowedVariantException("The variant in position "+position+" does not exist. Please use other position in [0,"+(listAlleles.size()-1)+"]");
	}
}
