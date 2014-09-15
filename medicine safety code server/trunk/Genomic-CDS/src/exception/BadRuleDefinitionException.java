package exception;

/**
 * This Exception will capture any problem when parsing a rule description.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 */
public class BadRuleDefinitionException  extends Exception{
	private static final long serialVersionUID = 1L;

	public BadRuleDefinitionException(String message) {
		super(message);
	}
}
