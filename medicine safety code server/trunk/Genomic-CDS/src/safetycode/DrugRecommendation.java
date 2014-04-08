package safetycode;

import java.util.ArrayList;

public class DrugRecommendation {
	private String cds_message	= null;
	private String source		= null;
	private String importance	= null;
	private String rule_id	= null;
	private String relevant_for = null;
	private ArrayList<String> seeAlsoList = null;
	private String drugRule = "";
	private NodeCondition node = null;
	private String lastUpdate = null;
	private String reason = null;
		
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
	
	public String getReason(){
		return reason;
	}
	
	public String getLastUpdate(){
		return lastUpdate;
	}
	
	public String getNodeDescription(){
		return node.toString();
	}
	
	public String getDrugName(){
		return relevant_for;
	}
	
	public String getCDSMessage(){
		return cds_message;
	}
	
	public String getSource(){
		return source;
	}
	
	public String getImportance(){
		return importance;
	}
	
	public String getRuleId(){
		return rule_id;
	}
	
	public String getRuleDescription(){
		return drugRule;
	}
	
	public boolean matchPatientProfile(Genotype genotype){
		ArrayList<GenotypeElement> listGenotypeElements = genotype.getListGenotypeElements();
		return node.test(listGenotypeElements);
	}
	
	public ArrayList<String> getSeeAlsoList(){
		return seeAlsoList;
	}
	
	public void setRule(String genomicRule){
		drugRule = genomicRule;
		
		genomicRule = genomicRule.replaceAll("\\n", " ");
		genomicRule = genomicRule.replaceAll("\\(", " ( ");
		genomicRule = genomicRule.replaceAll("\\)", " )");
		genomicRule = genomicRule.replaceAll("\\s+", " ");
		genomicRule = genomicRule.trim();
		
		if(!correctMatches(genomicRule)){
			System.out.println("ERROR-> bad parenthesis in rule="+genomicRule);
		}else{
			node = getTypeNode(genomicRule);
		}
	}
	
	public String toString(){
		if(node!=null){
			return node.toString();
		}else{
			return "null";
		}
	}
	
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
	
	private NodeCondition getTypeNode(String nodeExpression){
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
							System.out.println("ERROR: and/or inconsistency in has expression condition");
							return null;
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
						System.out.println("ERROR: and/or inconsistency");
						return null;
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
