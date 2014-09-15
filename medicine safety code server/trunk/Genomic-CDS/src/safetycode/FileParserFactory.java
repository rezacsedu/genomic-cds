package safetycode;

import utils.OntologyManagement;

/**
 * Class that implements the factory pattern to facilitate the creation of different FileParser objects.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 * */
public class FileParserFactory {
	/** Type of format for 23&me files*/
	public final static int FORMAT_23ANDME_FILE	= 0;
	/** Type of format for VCF files*/
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
