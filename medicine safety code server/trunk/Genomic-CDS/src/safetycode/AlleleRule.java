package safetycode;

import java.util.ArrayList;
import java.util.HashMap;

import exception.BadRuleDefinitionException;
import exception.VariantDoesNotMatchAnyAllowedVariantException;

/**
 * It represents a group of allele rule descriptions that makes possible to infer the corresponding haplotype variation based on raw SNP data.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 * */
public class AlleleRule {
	/**Name of the gene associated to the group of allele rules.*/
	private String geneName;
	/***/
	private HashMap<String, NodeCondition> listNodes;
	private GeneticMarkerGroup gmg;
	
	
	public AlleleRule(String geneName, HashMap<String, String> listRules, GeneticMarkerGroup gmg) throws BadRuleDefinitionException{
		listNodes = new HashMap<String, NodeCondition>();
		this.gmg = gmg;
		this.geneName = geneName;
		for(String key: listRules.keySet()){
			String ld = listRules.get(key);

			ld = ld.replaceAll("\\n", " ");
			ld = ld.replaceAll("\\(", " ( ");
			ld = ld.replaceAll("\\)", " )");
			ld = ld.replaceAll("\\s+", " ");
			ld = ld.trim();
			
			if(ld.isEmpty()){
				throw new BadRuleDefinitionException("Empty rule description");
			}
				
			if(!correctMatches(ld)){
				throw new BadRuleDefinitionException("Bad parenthesis in rule:"+ ld);
			}else{
				listNodes.put(key, getTypeNode(ld));
			}
		}
	}
	
	public String getGeneName(){
		return geneName;
	}
	
	public HashMap<String, NodeCondition> getListNodes(){
		return listNodes;
	}
	/**
	 * Check if the rule matches the provided patient's genotype.
	 * @param A patient's genotype.
	 * @return Whether the genotype matches the rule logical description or not.
	 * @throws VariantDoesNotMatchAnyAllowedVariantException 
	 * */
	public GenotypeElement matchPatientProfile(ArrayList<GenotypeElement> listSNPs) throws VariantDoesNotMatchAnyAllowedVariantException{
		ArrayList<String> listMatched = new ArrayList<String>();
		for(String key: listNodes.keySet()){
			NodeCondition root_node = listNodes.get(key);
			if(root_node.test(listSNPs)){
				listMatched.add(key);
			}
		}
		
		String var1 = null;
		String var2 = null;
		
		for(String key: listMatched){
			if(key.endsWith("_homozygous")){
				String aux = key.substring(0,key.lastIndexOf("_"));
				var1 = var2 = aux.substring(aux.indexOf(geneName+"_")+(geneName.length()+1)); 
				break;
			}

			if(var1 == null){
				var1 = key.substring(key.indexOf(geneName+"_")+(geneName.length()+1));
			}else{
				if(var2 == null){
					var2 = key.substring(key.indexOf(geneName+"_")+(geneName.length()+1));
					break;
				}
			}
		}
		return gmg.getGenotypeElement(gmg.getPositionGeneticMarker(var1+";"+var2));
	}
	
	/**
	 * Check if the rule contains the correct number of '(' and ')' to avoid errors during the parsing of the rule description.
	 * @param rule		The logical description of the rule.
	 * @return 			Whether the rule has a missing parenthesis or not.
	 * */
	private boolean correctMatches(String rule){
		int nOpen = 0;
		int nClose = 0;
		
		String subRule = rule;
		while(subRule.indexOf("(")>=0){
			subRule = subRule.substring(subRule.indexOf("(")+1);
			nOpen++;
		}
		
		subRule = rule;
		while(subRule.indexOf(")")>=0){
			subRule = subRule.substring(subRule.indexOf(")")+1);
			nClose++;
		}
		
		return (nOpen == nClose);
	}
	
	/**
	 * It parses the rule logical description to create the corresponding node conditions. i.e. ((A and B ) or C) -> is parsed as: node_1 represents condition "A", node_2 represents condition "B", node_3 represents expression "(node_1 and node_2)", node_4 represents condition "C" and node_5 represents expression "(node_3 or node_4)". For example,a condition could be "has some BRCA1_1"    
	 * @param nodeExpression	The logical description of the rule o a subsection of the rule
	 * */
	private NodeCondition getTypeNode(String nodeExpression) throws BadRuleDefinitionException{
		nodeExpression = nodeExpression.trim();
		String mainType ="";
		String mainNumber = "";
		String mainElement = "";
		String mainQuality = "";
		ArrayList<NodeCondition> mainListNodes = null;
		
		while(!nodeExpression.isEmpty()){
			if(nodeExpression.startsWith("has")){
				String hasType ="";
				String hasNumber = "";
				String hasElement = "";
				String hasQuality = "";
				ArrayList<NodeCondition> hasListNodes = null;
				
				nodeExpression = nodeExpression.substring(nodeExpression.indexOf("has")+3).trim();
				if(nodeExpression.startsWith("some")){
					hasType = "some";
					nodeExpression = nodeExpression.substring(nodeExpression.indexOf("some")+4).trim();
				}
				if(nodeExpression.startsWith("min")){
					hasType = "min";
					nodeExpression = nodeExpression.substring(nodeExpression.indexOf("min")+3).trim();
					hasNumber = nodeExpression.substring(0,nodeExpression.indexOf(" "));
					nodeExpression = nodeExpression.substring(nodeExpression.indexOf(" ")+1).trim();
				}
				if(nodeExpression.startsWith("exactly")){
					hasType = "exactly";
					nodeExpression = nodeExpression.substring(nodeExpression.indexOf("exactly")+7).trim();
					hasNumber = nodeExpression.substring(0,nodeExpression.indexOf(" "));
					nodeExpression = nodeExpression.substring(nodeExpression.indexOf(" ")+1).trim();
				}
				if(nodeExpression.startsWith("(")){
					hasListNodes = new ArrayList<NodeCondition>();
					String listElements = nodeExpression.substring(1,nodeExpression.indexOf(")")).trim();
					nodeExpression = nodeExpression.substring(nodeExpression.indexOf(")")+1).trim();

					while(!listElements.isEmpty()){
						if((listElements.startsWith("and")&&hasQuality.equals("or"))||(listElements.startsWith("and")&&hasQuality.equals("or"))){
							throw new BadRuleDefinitionException("and/or inconsistency in has expression condition: " + nodeExpression);
						}	
						if(listElements.startsWith("and")){
							hasQuality = "and";
							listElements = listElements.substring(listElements.indexOf("and")+3).trim();
							continue;
						}
						if(listElements.startsWith("or")){
							hasQuality = "or";
							listElements = listElements.substring(listElements.indexOf("or")+2).trim();
							continue;
						}
						String nodeElement = "";
						if(listElements.indexOf(" ")>=0){
							nodeElement = listElements.substring(0,listElements.indexOf(" ")).trim();
							listElements = listElements.substring(listElements.indexOf(" ")+1).trim();							
						}else{
							nodeElement = listElements;
							listElements = "";
						}

						NodeCondition subNode = new NodeCondition();
						subNode.setElement(nodeElement);
						hasListNodes.add(subNode);
					}
				}else{					
					if(nodeExpression.indexOf(" ")>=0){
						hasElement = nodeExpression.substring(0,nodeExpression.indexOf(" ")).trim();
						nodeExpression = nodeExpression.substring(nodeExpression.indexOf(" ")+1).trim();
					}else{
						hasElement = nodeExpression;
						nodeExpression = "";
					}
				}
				
				NodeCondition node = new NodeCondition();
				node.setElement(hasElement);
				node.setQuality(hasQuality);
				node.setType(hasType);
				node.setNumber(hasNumber);
				if(hasListNodes!=null){
					for(NodeCondition subNode: hasListNodes){
						node.addNode(subNode);
					}
				}
				if(mainListNodes==null) mainListNodes = new ArrayList<NodeCondition>();
				mainListNodes.add(node);
			}else{
				if(nodeExpression.startsWith("and")||nodeExpression.startsWith("or")){
					if((nodeExpression.startsWith("or")&&mainQuality.equals("and"))||(nodeExpression.startsWith("and")&&mainQuality.equals("or"))){
						throw new BadRuleDefinitionException("and/or inconsistency in has expression condition: " + nodeExpression);
					}else{
						if(nodeExpression.startsWith("or")){
							mainQuality = "or";
							nodeExpression = nodeExpression.substring(nodeExpression.indexOf("or")+2).trim();
						}
						if(nodeExpression.startsWith("and")){
							mainQuality = "and";
							nodeExpression = nodeExpression.substring(nodeExpression.indexOf("and")+3).trim();
						}
					}
				}else{
					if(nodeExpression.startsWith("(")){
						int nCondition = 1;
						String subNodeString = nodeExpression.substring(1).trim();
						String completeNode = "";
						while(nCondition>0){
							if(subNodeString.indexOf("(")<subNodeString.indexOf(")")&&subNodeString.indexOf("(")>=0){
								nCondition++;
								if(subNodeString.startsWith("(")){
									completeNode +="(";
									subNodeString = subNodeString.substring(1).trim();
								}else{
									completeNode += " "+subNodeString.substring(0,subNodeString.indexOf("(")+1).trim();
									subNodeString = subNodeString.substring(subNodeString.indexOf("(")+1).trim();
								}
							}else{
								nCondition--;
								if(nCondition>0){
									completeNode += " "+subNodeString.substring(0,subNodeString.indexOf(")")+1).trim();
								}else{
									completeNode += " "+subNodeString.substring(0,subNodeString.indexOf(")")).trim();
								}
								subNodeString = subNodeString.substring(subNodeString.indexOf(")")+1).trim();
							}
						}
						NodeCondition nodeCondition = getTypeNode(completeNode);
						if(mainListNodes==null) mainListNodes = new ArrayList<NodeCondition>();
						mainListNodes.add(nodeCondition);
						nodeExpression=subNodeString;
					}else{
						if(nodeExpression.indexOf(" ")>=0){
							String element = nodeExpression.substring(0,nodeExpression.indexOf(" ")).trim();
							nodeExpression = nodeExpression.substring(nodeExpression.indexOf(" ")+1).trim();
							NodeCondition node = new NodeCondition();
							node.setElement(element);
							
							if(mainListNodes==null) mainListNodes = new ArrayList<NodeCondition>();
							mainListNodes.add(node);
						}else{
							mainElement = nodeExpression;
							nodeExpression = "";
						}
					}
				}
			}
		}
		NodeCondition mainNode = new NodeCondition();
		
		mainNode.setElement(mainElement);
		mainNode.setNumber(mainNumber);
		mainNode.setQuality(mainQuality);
		mainNode.setType(mainType);
		for(NodeCondition subnodes: mainListNodes){
			mainNode.addNode(subnodes);
		}
		return mainNode;
	}
}
