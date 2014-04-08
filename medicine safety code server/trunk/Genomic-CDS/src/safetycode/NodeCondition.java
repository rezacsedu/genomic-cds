package safetycode;

import java.util.ArrayList;

public class NodeCondition {
	private ArrayList<NodeCondition> listConditions;
	private String element;//GenotypeElement
	private String quality;//and, or
	private String type;//some, exactly 
	private int number;
	
	public NodeCondition(){
		listConditions=null;
		element = "";
		quality="";
		type = "";
		number=-1;
	}
	
	public boolean test(ArrayList<GenotypeElement> listElements){
		if(!type.isEmpty()){
			if(type.equals("some")){
				if(!element.isEmpty()){
					for(GenotypeElement ge : listElements){
						String variant1 = ge.getGeneticMarkerName()+"_"+ge.getVariant1();
						
						if(variant1.contains("duplicated_")){//When the variant contains a copy number of the genotype variation. We just need to check that there is one.
							variant1 = variant1.replace("duplicated_", "");
						}
						if(variant1.equals(element)){
							return true;
						}
						
						String variant2 = ge.getGeneticMarkerName()+"_"+ge.getVariant2();
						if(variant2.contains("duplicated_")){//When the variant contains a copy number of the genotype variation. We just need to check that there is one.
							variant2 = variant2.replace("duplicated_", "");
						}
						if(variant2.equals(element)){
							return true;
						}
						
						//This exception occurs when the conditions requires one subclass of a SNPs and it does not indicate which one. Therefore, we accept all subclasses of it as matching classes. We assue that all subclasses of a SNP will contain its name in the description.
						if(element.matches("rs[0-9]+")){
							if(ge.getGeneticMarkerName().equals(element)&& !ge.getCriteriaSyntax().contains("null")){
								//System.out.println("matches some element = "+element);
								return true;
							}
						}
					}
					return false;
				}
				if(listConditions!=null && !listConditions.isEmpty()){
					if(!quality.isEmpty()){
						if(quality.equals("or")){
							for(NodeCondition condition : listConditions){
								if(!condition.getElement().isEmpty()){
									String nodeElement = condition.getElement();
									for(GenotypeElement ge : listElements){
										String variant1 = ge.getGeneticMarkerName()+"_"+ge.getVariant1();
										if(variant1.contains("duplicated_")){//When the variant contains a copy number of the genotype variation. We just need to check that there is one.
											variant1 = variant1.replace("duplicated_", "");
										}
										if(variant1.equals(nodeElement)){
											return true;
										}
										String variant2 = ge.getGeneticMarkerName()+"_"+ge.getVariant2();
										if(variant2.contains("duplicated_")){//When the variant contains a copy number of the genotype variation. We just need to check that there is one.
											variant2 = variant2.replace("duplicated_", "");
										}
										if(variant2.equals(nodeElement)){
											return true;
										}
										//This exception occurs when the conditions requires one subclass of a SNPs and it does not indicate which one. Therefore, we accept all subclasses of it as matching classes. We assue that all subclasses of a SNP will contain its name in the description.
										if(nodeElement.matches("rs[0-9]+")){
											if(ge.getGeneticMarkerName().equals(nodeElement) && !ge.getCriteriaSyntax().contains("null")){
												//System.out.println("matches some or nodeElement = "+nodeElement);
												return true;
											}
										}
									}
								}
							}
							return false;
						}
						if(quality.equals("and")){
							for(NodeCondition condition : listConditions){
								boolean andResult = false;
								if(!condition.getElement().isEmpty()){
									String nodeElement = condition.getElement();
									for(GenotypeElement ge : listElements){
										String variant1 = ge.getGeneticMarkerName()+"_"+ge.getVariant1();
										if(variant1.contains("duplicated_")){
											variant1 = variant1.replace("duplicated_", "");
										}
										String variant2 = ge.getGeneticMarkerName()+"_"+ge.getVariant2();
										if(variant2.contains("duplicated_")){
											variant2 = variant2.replace("duplicated_", "");
										}
										if(variant1.equals(nodeElement) || variant2.equals(nodeElement)){
											andResult=true;
											break;
										}
										if(nodeElement.matches("rs[0-9]+")){
											if(ge.getGeneticMarkerName().equals(nodeElement) && !ge.getCriteriaSyntax().contains("null")){
												//System.out.println("matches some and nodeElement = "+nodeElement);
												return true;
											}
										}
									}
								}
								if(!andResult){
									return false;
								}
							}
							return true;
						}
					}else{
						System.out.println("It should not be like this ->"+this.toString());
					}
				}
			}
			if(type.equals("exactly")&&number>=0){
				if(listConditions!=null && !listConditions.isEmpty()){
					if(!quality.isEmpty()){
						if(quality.equals("or")){
							for(NodeCondition condition : listConditions){
								int nMatches = 0;
								if(!condition.getElement().isEmpty()){
									String nodeElement = condition.getElement();
									for(GenotypeElement ge : listElements){
										String variant1 = ge.getGeneticMarkerName()+"_"+ge.getVariant1();
										if(variant1.contains("duplicated_")){
											variant1 = variant1.replace("duplicated_", "");
											if(variant1.equals(nodeElement)) nMatches+=2;
										}else{
											if(variant1.equals(nodeElement)) nMatches++;
										}
										String variant2 = ge.getGeneticMarkerName()+"_"+ge.getVariant2();
										if(variant2.contains("duplicated_")){
											variant2 = variant2.replace("duplicated_", "");
											if(variant2.equals(nodeElement)) nMatches+=2;
										}else{
											if(variant2.equals(nodeElement)) nMatches++;
										}
										if(nodeElement.matches("rs[0-9]+")){
											if(ge.getGeneticMarkerName().equals(nodeElement) && !ge.getCriteriaSyntax().contains("null")){
												//System.out.println("matches exactly or nodeElement = "+nodeElement);
												nMatches+=2;
											}
										}
									}
								}
								if(nMatches == number){
									return true;
								}
							}
							return false;
						}
						if(quality.equals("and")){
							for(NodeCondition condition : listConditions){
								if(!condition.getElement().isEmpty()){
									String nodeElement = condition.getElement();
									int nMatches = 0;
									for(GenotypeElement ge : listElements){
										String variant1 = ge.getGeneticMarkerName()+"_"+ge.getVariant1();
										if(variant1.contains("duplicated_")){
											variant1 = variant1.replace("duplicated_", "");
											if(variant1.equals(nodeElement)) nMatches+=2;
										}else{
											if(variant1.equals(nodeElement)) nMatches++;
										}
										String variant2 = ge.getGeneticMarkerName()+"_"+ge.getVariant2();
										if(variant2.contains("duplicated_")){
											variant2 = variant2.replace("duplicated_", "");
											if(variant2.equals(nodeElement)) nMatches+=2;
										}else{
											if(variant2.equals(nodeElement)) nMatches++;
										}
										
										if(nodeElement.matches("rs[0-9]+")){
											if(ge.getGeneticMarkerName().equals(nodeElement) && !ge.getCriteriaSyntax().contains("null")){
												//System.out.println("matches exactly and nodeElement = "+nodeElement);
												nMatches+=2;
											}
										}
									}
									
									if(nMatches != number){
										return false;
									}
								}
							}
							return true;
						}
					}else{
						System.out.println("It should not be like this ->"+this.toString());
					}
				}else{
					if(!element.isEmpty()){
						int nMatches = 0;
						for(GenotypeElement ge : listElements){
							String variant1 = ge.getGeneticMarkerName()+"_"+ge.getVariant1();
							if(variant1.contains("duplicated_")){
								variant1 = variant1.replace("duplicated_", "");
								if(variant1.equals(element)) nMatches+=2;
							}else{
								if(variant1.equals(element)) nMatches++;
							}
							String variant2 = ge.getGeneticMarkerName()+"_"+ge.getVariant2();
							if(variant2.contains("duplicated_")){
								variant2 = variant2.replace("duplicated_", "");
								if(variant2.equals(element)) nMatches+=2;
							}else{
								if(variant2.equals(element)) nMatches++;
							}
							
							if(element.matches("rs[0-9]+")){
								if(ge.getGeneticMarkerName().equals(element) && !ge.getCriteriaSyntax().contains("null")){
									//System.out.println("matches exactly element = "+element);
									nMatches+=2;
								}
							}
						}
						
						if(nMatches != number){
							return false;
						}
						return true;
					}
				}
			}
			if(type.equals("min")&&number>=0){
				if(listConditions!=null && !listConditions.isEmpty()){
					if(!quality.isEmpty()){
						if(quality.equals("or")){
							int nMatches = 0;
							for(NodeCondition condition : listConditions){
								if(!condition.getElement().isEmpty()){
									String nodeElement = condition.getElement();
									for(GenotypeElement ge : listElements){
										String variant1 = ge.getGeneticMarkerName()+"_"+ge.getVariant1();
										if(variant1.contains("duplicated_")){
											variant1 = variant1.replace("duplicated_", "");
											if(variant1.equals(nodeElement)) nMatches+=2;
										}else{
											if(variant1.equals(nodeElement)) nMatches++;
										}
										String variant2 = ge.getGeneticMarkerName()+"_"+ge.getVariant2();
										if(variant2.contains("duplicated_")){
											variant2 = variant2.replace("duplicated_", "");
											if(variant2.equals(nodeElement)) nMatches+=2;
										}else{
											if(variant2.equals(nodeElement)) nMatches++;
										}
										if(nodeElement.matches("rs[0-9]+")){
											if(ge.getGeneticMarkerName().equals(nodeElement) && !ge.getCriteriaSyntax().contains("null")){
												//System.out.println("matches min or nodeElement = "+nodeElement);
												nMatches+=2;
											}
										}
									}
								}
							}
							if(nMatches >= number){
								return true;
							}
							return false;
						}
						if(quality.equals("and")){
							for(NodeCondition condition : listConditions){
								if(!condition.getElement().isEmpty()){
									String nodeElement = condition.getElement();
									int nMatches = 0;
									for(GenotypeElement ge : listElements){
										String variant1 = ge.getGeneticMarkerName()+"_"+ge.getVariant1();
										if(variant1.contains("duplicated_")){
											variant1 = variant1.replace("duplicated_", "");
											if(variant1.equals(nodeElement)) nMatches+=2;
										}else{
											if(variant1.equals(nodeElement)) nMatches++;
										}
										String variant2 = ge.getGeneticMarkerName()+"_"+ge.getVariant2();
										if(variant2.contains("duplicated_")){
											variant2 = variant2.replace("duplicated_", "");
											if(variant2.equals(nodeElement)) nMatches+=2;
										}else{
											if(variant2.equals(nodeElement)) nMatches++;
										}
										if(nodeElement.matches("rs[0-9]+")){
											if(ge.getGeneticMarkerName().equals(nodeElement) && !ge.getCriteriaSyntax().contains("null")){
												//System.out.println("matches min and nodeElement = "+nodeElement);
												nMatches+=2;
											}
										}
									}
									if(nMatches < number){
										return false;
									}
								}
							}
							return true;
						}
					}else{
						System.out.println("It should not be like this ->"+this.toString());
					}
				}else{
					if(!element.isEmpty()){
						int nMatches = 0;
						for(GenotypeElement ge : listElements){
							String variant1 = ge.getGeneticMarkerName()+"_"+ge.getVariant1();
							if(variant1.contains("duplicated_")){
								variant1 = variant1.replace("duplicated_", "");
								if(variant1.equals(element)) nMatches+=2;
							}else{
								if(variant1.equals(element)) nMatches++;
							}
							String variant2 = ge.getGeneticMarkerName()+"_"+ge.getVariant2();
							if(variant2.contains("duplicated_")){
								variant2 = variant2.replace("duplicated_", "");
								if(variant2.equals(element)) nMatches+=2;
							}else{
								if(variant2.equals(element)) nMatches++;
							}
							if(element.matches("rs[0-9]+")){
								if(ge.getGeneticMarkerName().equals(element) && !ge.getCriteriaSyntax().contains("null")){
									//System.out.println("matches min element = "+element);
									nMatches+=2;
								}
							}
						}
						if(nMatches < number){
							return false;
						}
						return true;
					}
				}
			}
		}else{
			if(!quality.isEmpty()){
				if(quality.equals("or") && (listConditions!=null && !listConditions.isEmpty())){
					for(NodeCondition condition : listConditions){
						if(condition.test(listElements)){
							return true;
						}
					}
					
					return false;
				}
				if(quality.equals("and") && (listConditions!=null && !listConditions.isEmpty())){
					for(NodeCondition condition : listConditions){
						if(!condition.test(listElements)){
							return false;
						}
					}
					return true;
				}
			}
		}
		if(type.isEmpty()&&quality.isEmpty()&&listConditions!=null&&listConditions.size()==1){
			return listConditions.get(0).test(listElements);
		}
		
		System.out.println("ERROR: Nothing was matched->"+toString());
		/*if(listConditions == null){
			System.out.println("listConditions is null");
		}else{
			if(listConditions.isEmpty()){
				System.out.println("listConditions is Empty");
			}else{
				System.out.println("listConditions has size = "+listConditions.size());
			}
		}
		System.out.println("element="+element);
		System.out.println("quality="+quality);
		System.out.println("type="+type);
		System.out.println("number="+number);*/
		
		return false;
	}
	
		
	public void optimized(){
		if(type.isEmpty()&&quality.isEmpty()&&listConditions!=null&&listConditions.size()==1){
			NodeCondition aux = listConditions.get(0);
			this.element = aux.getElement();
			this.number = aux.getNumber();
			this.quality = aux.getQuality();
			this.type = aux.getType();
			this.listConditions = aux.getListNodeConditions();
		}
	}
	
	
	public void addNode(NodeCondition node){
		if(listConditions==null){
			listConditions = new ArrayList<NodeCondition>();
		}
		if(node!=null){
			listConditions.add(node);
		}
	}
	
	public ArrayList<NodeCondition> getListNodeConditions(){
		return listConditions;
	}
		
	public String getQuality(){
		return quality;
	}
	
	public void setQuality(String quality){
		this.quality = quality;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return type;
	}
	
	public void setNumber(String number){
		if(number!=null && !number.isEmpty()){
			try{
				this.number = Integer.parseInt(number);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public int getNumber(){
		return number;
	}
	
	public void setElement(String element){
		this.element = element;
	}
	
	public String getElement(){
		return element;
	}
	
	public String toString(){
		String desc = "";
		if(!quality.isEmpty() && !type.isEmpty() && listConditions!=null && !listConditions.isEmpty()){
			desc+="has "+type+" ";
			if(number>=0){
				desc+=number+" ";
			}
			String aux = "";
			for(NodeCondition node: listConditions){
				if(!aux.isEmpty()) aux+=" "+quality+" ";
				aux+=node.getElement();
			}
			aux = "( "+aux+" )";
			desc+=aux;
			return desc;
		}
		if(!type.isEmpty() && !element.isEmpty()){
			desc+="has "+type+" ";
			if(number>=0){
				desc+=number+" ";
			}
			desc+=element;
			return desc;
		}
		if(!quality.isEmpty() && listConditions!=null && !listConditions.isEmpty()){
			for(NodeCondition node: listConditions){
				if(!desc.isEmpty()) desc+=" "+quality+" ";
				desc+="( "+node.toString()+" )";
			}
			return desc;
		}
		
		if(listConditions!=null && listConditions.size()==1){
			return listConditions.get(0).toString();
		}
		
		if(!element.isEmpty()) return "\n"+element;
		
		System.out.println("ERROR node tostring");
		return desc;
	}
}
