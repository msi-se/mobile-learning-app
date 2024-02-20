package de.htwg_konstanz.mobilelearning.services.auth;

import java.util.Hashtable;

import de.htwg_konstanz.mobilelearning.models.auth.User;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Service used to authenticate users against the HTWG LDAP server.
 */
public class LdapHtwg {

    private static final String PRINCIPAL = "ou=users,dc=fh-konstanz,dc=de";
    private static final String ldapURL = "ldap://ldap.htwg-konstanz.de:389";

    public User doLogin(String username, String password) throws Exception {

        final String dn = "uid=" + username.trim() + "," + PRINCIPAL;

        Hashtable<String, String> environment = new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL, ldapURL);
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, dn);
        environment.put(Context.SECURITY_CREDENTIALS, password);

        DirContext authContext = null;
        try {
            authContext = new InitialDirContext(environment);

            // create user object
            User user = new User(
                authContext.getAttributes(dn).get("mail") + "",
                authContext.getAttributes(dn).get("cn") + "",
                authContext.getAttributes(dn).get("uid") + "",
                ""
            );
        
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[] {
                    // "nsRole", //Role of user
                    "uid",
                    // "objectClass",
                    "givenName", "sn", "cn", "gidNumber", "mail" });
            NamingEnumeration<?> results = authContext.search(PRINCIPAL, "uid=" + username.trim(),
                    controls);

            if (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                String gidNumber = attributes.get("gidNumber") + "";
                user.assignProfAndStudentRoleByLdapId(gidNumber);
            }
 
            return user;
        } catch (AuthenticationException ex) {
            System.out.println(ex.getMessage());
            throw new Exception(ex.getMessage());
        } catch (NamingException ex) {
            System.out.println(ex.getMessage());
            throw new Exception(ex.getMessage());
        } finally {
            if (authContext!=null)
                authContext.close();
        }

    }
    
}
