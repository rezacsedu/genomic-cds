package safetycode;

/**Class that implements the factory pattern to facilitate the creation of different FileParser objects.*/
public class FileParserFactory {

	public final static int FORMAT_23ANDME_FILE	= 0;
	public final static int FORMAT_VCF_FILE		= 1;
	
	/**
	 * Method to create the file parser that supports the suitable file format. 
	 * */
	public static FileParser getFileParser(int typeParser, MedicineSafetyProfile msp){
		switch(typeParser){
		case FORMAT_23ANDME_FILE:
			return new FileParser_23andme(msp.getListRsids(),msp.getMapCriteria2Bitcode());
		case FORMAT_VCF_FILE:
			return new FileParser_VCF(msp.getVCFReflistRsids(),msp.getMapCriteria2Bitcode());
		}
		return null;
	}
	
}
