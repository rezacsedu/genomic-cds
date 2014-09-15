package safetycode;

import java.util.ArrayList;

import exception.BadRuleDefinitionException;

/**
 * It represents a cds rule and is able to check if a patient's genotype match the logical description of the rule.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 * */
public class DrugRecommendation {
	private String cds_message	= null;	//AnnotationProperty: CDS_message
	private String source		= null;	//AnnotationProperty: source
	private String importance	= null;	//AnnotationProperty: recommendation_importance
	private String rule_id	= null;		//AnnotationProperty: rdfs:label
	private String relevant_for = null;	//AnnotationProperty: relevant_for
	private ArrayList<String> seeAlsoList = null;	//AnnotationProperty: rdfs:seeAlso
	private String drugRule = "";		//AnnotationProperty: textual_genetic_description
	private NodeCondition node = null;	//Node that represents the logical description of the rule.
	private String lastUpdate = null;	//AnnotationProperty: date_last_validation
	private String reason = null;		//AnnotationProperty: phenotype_description
	
	/**
	 * Create the representation of a cds rule.
	 * @param rule_id		The id of the rule class of the ontology.
	 * @param cds_message	The drug recommendations related to the rule.
	 * @param importance	The importance of the rule recommendation. Important modification vs Standard recommendation.  
	 * @param source		The source repository where the drug recommendation information is published.
	 * @param relevant_for	The name of the drug related to the recommendation.
	 * @param seeAlsoList	The list of urls where the information is published.
	 * @param lastUpdate	The date of the last update of the rule. 
	 * @param reason		The phenotype description related to the rule.
	 * */
	public DrugRecommendation(String rule_id, String cds_message, String importance, String source, String relevant_for,ArrayList<String> seeAlsoList, String lastUpdate, String reason){
		this.cds_message = cds_message;
		this.importance = importance;
		this.source = source;
		this.rule_id = rule_id;
		this.seeAlsoList = seeAlsoList;
		this.relevant_for = relevant_for;
		this.lastUpdate = lastUpdate;
		this.reason = reason;
	}
	
	/**
	 * Get method of the phenotype description of the rule.
	 * @return The phenotype description of the rule.
	 * */
	public String getReason(){
		return reason;
	}
	
	/**
	 * Get method of the date of the last update of the rule.
	 * @return The date of the last update of the rule.
	 * */
	public String getLastUpdate(){
		return lastUpdate;
	}
	
	/**
	 * Get method of the drug name related to the rule.
	 * @return The drug name related to the rule.
	 * */
	public String getNodeDescription(){
		return node.toString();
	}
	
	/**
	 * Get method of the drug dosage recommendation related to the rule.
	 * @return The drug dosage recommendation related to the rule.
	 * */
	public String getDrugName(){
		return relevant_for;
	}
	
	/**
	 * Get method of the source repository name of the rule information.
	 * @return The source repository name of the rule information.
	 * */
	public String getCDSMessage(){
		return cds_message;
	}
	
	/**
	 * Get method of the rule importance.
	 * @return The rule importance.
	 * */
	public String getSource(){
		return source;
	}
	
	/**
	 * Get method of the rule id.
	 * @return The rule id.
	 * */
	public String getImportance(){
		return importance;
	}
	
	/**
	 * Get method of the rule logical description.
	 * @return The rule logical description.
	 * */
	public String getRuleId(){
		return rule_id;
	}
	
	/**
	 * Check if the rule matches the provided patient's genotype.
	 * @param A patient's genotype.
	 * @return Whether the genotype matches the rule logical description or not.
	 * */
	public String getRuleDescription(){
		return drugRule;
	}
	
	/**
	 * Check if the rule matches the provided patient's genotype.
	 * @param A patient's genotype.
	 * @return Whether the genotype matches the rule logical description or not.
	 * */
	public boolean matchPatientProfile(Genotype genotype){
		ArrayList<GenotypeElement> listGenotypeElements = genotype.getListGenotypeElements();
		return node.test(listGenotypeElements);
	}
	
	/**
	 * Get method of the list of URLs where information related to the rule is published.
	 * @return The list of URLs related to the drug recommendation rule.
	 * */
	public ArrayList<String> getSeeAlsoList(){
		return seeAlsoList;
	}
	
	/**
	 * Generates the node structure related to the logical description of the rule.
	 * @param genomicRule	The logical description of the rule.
	 * @throws BadRuleDefinitionException	This exception is triggered when there is a bad logical statements in a rule description.
	 * */
	public void setRule(String genomicRule) throws BadRuleDefinitionException{
		drugRule = genomicRule;
		
		genomicRule = genomicRule.replaceAll("\\n", " ");
		genomicRule = genomicRule.replaceAll("\\(", " ( ");
		genomicRule = genomicRule.replaceAll("\\)", " )");
		genomicRule = genomicRule.replaceAll("\\s+", " ");
		genomicRule = genomicRule.trim();
		if(genomicRule.isEmpty()){
			throw new BadRuleDefinitionException("Empty rule description");
		}
			
		if(!correctMatches(genomicRule)){
			//System.out.println("ERROR-> bad parenthesis in rule="+genomicRule);
			throw new BadRuleDefinitionException("Bad parenthesis in rule:"+ genomicRule);
		}else{
			node = getTypeNode(genomicRule);
		}
	}
	
	/**
	 * Overrides the toString method to show how the rule was parsed.
	 * */
	public String toString(){
		if(node!=null){
			return node.toString();
		}else{
			return "null";
		}
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
