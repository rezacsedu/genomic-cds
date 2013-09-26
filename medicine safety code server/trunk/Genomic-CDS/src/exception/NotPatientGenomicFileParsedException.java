package exception;

public class NotPatientGenomicFileParsedException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public NotPatientGenomicFileParsedException(String message){
		super(message);
	}
}
