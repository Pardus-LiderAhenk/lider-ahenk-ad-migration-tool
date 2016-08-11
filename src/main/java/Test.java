import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.migrate.MigrateGroups;
import tr.org.liderahenk.migrate.MigrateUsers;
import tr.org.liderahenk.utils.LdapUtils;
import tr.org.liderahenk.utils.PropertyReader;

public class Test {

	private final static Logger logger = LoggerFactory.getLogger(Test.class);

	public static void main(String[] args) throws Exception {

		// TODO configura file-based logging
		BasicConfigurator.configure();

		//
		// Active directory
		//
		logger.debug("Configuring Active Directory connection parameters.");
		String aHost = PropertyReader.getInstance().get("active.directory.host");
		int aPort = PropertyReader.getInstance().getInt("active.directory.port");
		String aUsername = PropertyReader.getInstance().get("active.directory.username");
		String aPassword = PropertyReader.getInstance().get("active.directory.password");
		boolean aUseSsl = PropertyReader.getInstance().getBoolean("active.directory.use.ssl");
		logger.info("Creating Active Directory connection pool.");
		LdapUtils activeDirectory = new LdapUtils(aHost, aPort, aUsername, aPassword, aUseSsl);

		//
		// OpenLDAP
		//
		logger.debug("Configuring OpenLDAP connection parameters.");
		String oHost = PropertyReader.getInstance().get("open.ldap.host");
		int oPort = PropertyReader.getInstance().getInt("open.ldap.port");
		String oUsername = PropertyReader.getInstance().get("open.ldap.username");
		String oPassword = PropertyReader.getInstance().get("open.ldap.password");
		boolean oUseSsl = PropertyReader.getInstance().getBoolean("open.ldap.use.ssl");
		logger.info("Creating OpenLDAP connection pool.");
		LdapUtils openLdap = new LdapUtils(oHost, oPort, oUsername, oPassword, oUseSsl);
		
		MigrateUsers mUsers = new MigrateUsers(activeDirectory, openLdap);
		mUsers.migrate();

		MigrateGroups mGroups = new MigrateGroups(activeDirectory, openLdap);
		mGroups.migrate();

		activeDirectory.destroy();
		openLdap.destroy();
	}

}
