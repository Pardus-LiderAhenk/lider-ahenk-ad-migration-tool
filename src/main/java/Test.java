import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.enums.SearchFilterEnum;
import tr.org.liderahenk.exceptions.LdapException;
import tr.org.liderahenk.utils.LdapSearchFilterAttribute;
import tr.org.liderahenk.utils.LdapUtils;
import tr.org.liderahenk.utils.PropertyReader;

public class Test {

	private final static Logger logger = LoggerFactory.getLogger(Test.class);

	public static void main(String[] args) throws LdapException, Exception {

		BasicConfigurator.configure();

		//
		// Active directory
		//

		// Connection parameters
		logger.debug("Configuring Active Directory connection parameters.");
		String aHost = PropertyReader.getInstance().get("active.directory.host");
		int aPort = PropertyReader.getInstance().getInt("active.directory.port");
		String aUsername = PropertyReader.getInstance().get("active.directory.username");
		String aPassword = PropertyReader.getInstance().get("active.directory.password");
		boolean aUseSsl = PropertyReader.getInstance().getBoolean("active.directory.use.ssl");

		logger.debug("Creating Active Directory connection pool.");
		LdapUtils activeDirectory = new LdapUtils(aHost, aPort, aUsername, aPassword, aUseSsl);

		// Search parameters
		logger.debug("Configuring search parameters for Active Directory");
		String aBaseDn = PropertyReader.getInstance().get("active.directory.search.base.dn");
		String[] aObjectClasses = PropertyReader.getInstance().getStringArr("active.directory.search.object.classes");
		List<LdapSearchFilterAttribute> aFilterAttributes = new ArrayList<LdapSearchFilterAttribute>();
		if (aObjectClasses != null && aObjectClasses.length > 0) {
			for (String objectClass : aObjectClasses) {
				aFilterAttributes.add(new LdapSearchFilterAttribute("objectClass", objectClass, SearchFilterEnum.EQ));
			}
		}

		//
		// OpenLDAP
		//

		// Connection parameters
		logger.debug("Configuring OpenLDAP connection parameters.");
		String oHost = PropertyReader.getInstance().get("open.ldap.host");
		int oPort = PropertyReader.getInstance().getInt("open.ldap.port");
		String oUsername = PropertyReader.getInstance().get("open.ldap.username");
		String oPassword = PropertyReader.getInstance().get("open.ldap.password");
		boolean oUseSsl = PropertyReader.getInstance().getBoolean("open.ldap.use.ssl");

		logger.debug("Creating OpenLDAP connection pool.");
		LdapUtils openLdap = new LdapUtils(oHost, oPort, oUsername, oPassword, oUseSsl);

		// Search parameters
		logger.debug("Configuring search parameters for OpenLDAP");
		String oBaseDn = PropertyReader.getInstance().get("open.ldap.search.base.dn");
		String[] oObjectClasses = PropertyReader.getInstance().getStringArr("open.ldap.search.object.classes");
		List<LdapSearchFilterAttribute> oFilterAttributes = new ArrayList<LdapSearchFilterAttribute>();
		if (oObjectClasses != null && oObjectClasses.length > 0) {
			for (String objectClass : oObjectClasses) {
				oFilterAttributes.add(new LdapSearchFilterAttribute("objectClass", objectClass, SearchFilterEnum.EQ));
			}
		}

		// Collect OpenLDAP attributes, so that we can map AD attributes to
		// them.
		logger.debug("Collecting attributes in OpenLDAP...");
		Set<String> validAttrNames = null;
		Set<String> validObjClsValues = null;
		List<Entry> oEntries = openLdap.search(oBaseDn, oFilterAttributes, null);
		if (oEntries != null && !oEntries.isEmpty()) {
			// Select first entry
			Entry entry = oEntries.get(0);

			validAttrNames = new HashSet<String>();
			validObjClsValues = new HashSet<String>();

			// Iterate over its each attribute
			Collection<Attribute> attributes = entry.getAttributes();
			if (attributes != null) {
				for (Attribute attribute : attributes) {
					// Flag current attribute as valid
					validAttrNames.add(attribute.getId().toLowerCase(Locale.ENGLISH));
					// If it is an object class, store its valid object class
					// values as well...
					if (attribute.getId().equalsIgnoreCase("objectClass")) {
						for (Value<?> value : attribute) {
							if (value == null || value.getValue() == null) {
								continue;
							}
							validObjClsValues.add(value.getValue().toString());
						}
					}
				}
			}
		}

		// Search user entries in Active Directory
		// For each entry in AD, we try to create a new one in OpenLDAP
		List<Entry> aEntries = activeDirectory.search(aBaseDn, aFilterAttributes, null);
		for (Entry entry : aEntries) {
			try {
				logger.info("Copying entry {} to OpenLDAP...", entry.getDn().getName());

				String newDn = null;
				Map<String, String[]> newAttributes = null;

				logger.debug("Reading attributes of the entry.");
				Collection<Attribute> attributes = entry.getAttributes();
				if (attributes != null) {
					newAttributes = new HashMap<String, String[]>();
					for (Attribute attribute : attributes) {
						// Determine new DN!
						if (attribute.getId().equalsIgnoreCase(
								PropertyReader.getInstance().get("open.ldap.new.entry.prefix.attribute"))
								&& attribute.get() != null) {
							newDn = PropertyReader.getInstance().get("open.ldap.new.entry.prefix.attribute");
							newDn += "=" + attribute.get() + ",";
							newDn += PropertyReader.getInstance().get("open.ldap.new.entry.suffix");
							logger.debug("Creating new DN {} for the entry...", newDn);
						}
						String log = "";
						// Copy this AD attribute only if it has some value AND
						// it is a valid attribute.
						if (attribute.size() > 0 && (validAttrNames == null
								|| validAttrNames.contains(attribute.getId().toLowerCase(Locale.ENGLISH)))) {
							String[] attrValues = new String[attribute.size()];
							int i = 0;
							for (Value<?> value : attribute) {
								if (value == null || value.getValue() == null) {
									continue;
								}
								attrValues[i] = value.getValue().toString();
								log += value.getValue().toString() + " ";
							}
							newAttributes.put(attribute.getId(), attrValues);
							logger.debug("Copying new attribute {} = {} for the entry...",
									new String[] { attribute.getUpId(), log });
						}
					}
				}

				// Create entry
				openLdap.addEntry(newDn, newAttributes);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		activeDirectory.destroy();
		openLdap.destroy();
	}

}
