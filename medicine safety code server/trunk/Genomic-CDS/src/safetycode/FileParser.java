package safetycode;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Interface that represents all file parser supported.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 * */
public interface FileParser {
	
	/**
	 * Method that parses the input file stream with the given orientation of nucleotides strand.
	 * 
	 * @param fileStream		InputStream file with the patient's genotype.
	 * @param strandOrientation	The orientation of the nucleotides strand.
	 * @return The report of the file processing that can be used for detecting inconsistencies in the file format.
	 * */
	public String parse(InputStream fileStream, String strandOrientation);
	
	/**
	 * Get method to obtain the resulting patient profile coded using a number in base 64.
	 * @return	The base64 number of the patient's profile.
	 * */
	public ArrayList<SNPElement> getListSNPElements();
}
