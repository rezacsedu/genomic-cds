package exception;


/**
 * This Exception will capture any problem when transforming a base 64 number into a binary number.
 */
public class BadFormedBase64NumberException extends Exception {

	private static final long serialVersionUID = 1L;

	public BadFormedBase64NumberException(String message) {
		super(message);
	}
}
