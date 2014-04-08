package safetycode;

public class SNPelement_old {
	private String rsid = "";
	private String bit_length="";
	private String orientation = "";
	private String criteriaSyntax = "";
	private String bit_code = "";
	
	public SNPelement_old(String rsid, String bit_length, String orientation, String criteriaSyntax, String bit_code){
		this.rsid = rsid;
		this.bit_length = bit_length;
		this.orientation = orientation;
		this.criteriaSyntax = criteriaSyntax;
		this.bit_code = bit_code;
	}
	
	public String getRsid(){
		return rsid;
	}
	
	public void setRsid(String rsid){
		this.rsid = rsid;
	}
	
	public String getBit_lenght(){
		return bit_length;
	}
	
	public void setBit_length(String bit_length){
		this.bit_length = bit_length;
	}
	
	public String getOrientation(){
		return orientation;
	}
	
	public void setOrientation(String orientation){
		this.orientation = orientation;
	}

	public String getCriteriaSyntax(){
		return criteriaSyntax;
	}
	
	public void setCriteriaSyntax(String criteriaSyntax){
		this.criteriaSyntax = criteriaSyntax;
	}

	public String getBit_code(){
		return bit_code;
	}
	
	public void setBit_code(String bit_code){
		this.bit_code = bit_code;
	}
	
	public SNPelement_old clone(){
		return (new SNPelement_old(this.rsid, this.bit_length, this.orientation, this.criteriaSyntax, this.bit_code));
	}
}
