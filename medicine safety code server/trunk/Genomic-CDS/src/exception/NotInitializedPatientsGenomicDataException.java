package exception;

public class NotInitializedPatientsGenomicDataException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public NotInitializedPatientsGenomicDataException(String message){
		super(message);
	}
}
