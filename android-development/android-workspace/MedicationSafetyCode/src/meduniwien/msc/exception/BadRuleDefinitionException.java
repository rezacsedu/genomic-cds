package meduniwien.msc.exception;

/**
 * This Exception will capture any problem when parsing a rule description.
 */
public class BadRuleDefinitionException  extends Exception{
	private static final long serialVersionUID = 1L;

	public BadRuleDefinitionException(String message) {
		super(message);
	}
}