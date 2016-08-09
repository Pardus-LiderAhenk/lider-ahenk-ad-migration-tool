package tr.org.liderahenk.exceptions;

/**
 * This exception is thrown when an error occurs during an LDAP operation
 * 
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class LdapException extends Exception {

	private static final long serialVersionUID = -9029746454064288353L;

	/**
	 * default constructor
	 */
	public LdapException() {
		super();
	}

	/**
	 * 
	 * @param message
	 */
	public LdapException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param throwable
	 */
	public LdapException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * 
	 * @param message
	 * @param throwable
	 */
	public LdapException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
