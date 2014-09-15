package exception;

/**
 * This Exception will be launched when a genomic profile file has not been parsed before getting the corresponding drug recommendations.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 * */
public class NotPatientGenomicFileParsedException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public NotPatientGenomicFileParsedException(String message){
		super(message);
	}
}
