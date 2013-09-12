package safetycode;

import java.io.InputStream;

/**Interface that represents all file parser developed*/
public interface FileParser {
		
	/**
	 * Method that parses the input file stream with the given orientation of nucleotides strand.
	 * @param fileStream		InputStream file with the patient's genotype.
	 * @param strandOrientation	The orientation of the nucleotides strand.
	 * */
	public String parse(InputStream fileStream, String strandOrientation);
	/**
	 * Get method to obtain the resulting patient profile coded using a number in base 64.
	 * @return	The base64 number of the patient's profile.
	 * */
	public String getBase64ProfileString();
}
