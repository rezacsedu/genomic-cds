package exception;

/**
 * This Exception will be launched when a patient's profile has not been initialised before getting the corresponding drug recommendations.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 * */
public class NotInitializedPatientsGenomicDataException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public NotInitializedPatientsGenomicDataException(String message){
		super(message);
	}
}
