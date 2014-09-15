package exception;


/**
 * This Exception will capture any problem when transforming any binary number into base 64 number.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 */
public class BadFormedBinaryNumberException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public BadFormedBinaryNumberException(String message) {
		super(message);
	}
}
