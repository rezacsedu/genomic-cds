package exception;


/**
 * This Exception will capture any problem when transforming any binary number into base 64 number.
 */
public class BadFormedBinaryNumberException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public BadFormedBinaryNumberException(String message) {
		super(message);
	}
}
