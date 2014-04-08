package safetycode;

import utils.OntologyManagement;

/**Class that implements the factory pattern to facilitate the creation of different FileParser objects.*/
public class FileParserFactory {

	public final static int FORMAT_23ANDME_FILE	= 0;
	public final static int FORMAT_VCF_FILE		= 1;
	
	/**
	 * Method to create the file parser that supports the suitable file format. 
	 * */
	public static FileParser getFileParser(int typeParser, OntologyManagement om){
		switch(typeParser){
		case FORMAT_23ANDME_FILE:
			return new FileParser_23andme_Format(om.getList23andMeRsids(),om.getListSNPsGroups());
		case FORMAT_VCF_FILE:
			return new FileParser_VCF_Format(om.getVCFRefListRsids(),om.getListSNPsGroups());
		}
		return null;
	}
	
}
