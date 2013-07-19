package exception;


/**
 * This Exception will capture any problem when matching criteria syntax strings to retrieve genetic variants.
 */
public class VariantDoesNotMatchAnAllowedVariantException extends Exception {
	private static final long serialVersionUID = 1L;

	public VariantDoesNotMatchAnAllowedVariantException(String message) {
		super(message);
	}
}
