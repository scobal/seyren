/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.api.provider;

import com.seyren.core.domain.User;
import com.seyren.core.util.config.SeyrenConfig;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@Named
public class LdapAuthenticationProvider implements AuthenticationProvider {
    private static final String FIRSTNAME = "givenName";
    private static final String LASTNAME = "sn";
    private static final String MEMBERSHIP = "memberOf";
    private static final String USERNAME = "sAMAccountName";
    private String initialContextFactoryImpl = "com.sun.jndi.ldap.LdapCtxFactory";
    private final SeyrenConfig seyrenConfig;

    @Inject
    public LdapAuthenticationProvider(SeyrenConfig seyrenConfig) {
        this.seyrenConfig = seyrenConfig;
    }

    /**
     * Validates Basic Credentials
     * @param authentication
     * @return
     */
    @Override
    public Boolean isValidCredentials(User authentication) {
        StringBuilder username = new StringBuilder();
        if(authentication.getUsername().startsWith(seyrenConfig.getLdapDomain()) || seyrenConfig.getLdapDomain().isEmpty()) {
            username.append(authentication.getUsername());
        } else {
            username.append(seyrenConfig.getLdapDomain() + "\\" + authentication.getUsername());
        }
        try {
            new InitialDirContext(contextConfiguration(username.toString(), authentication.getPassword()));
        } catch (NamingException e) {
            authentication.setMessage(e.getMessage());
            return false;
        }
        authentication.setAuthenticated(true);
        return true;
    }

    /**
     * Validates weather this person is a valid ldap user
     * @param name
     * @return
     */
    @Override
    public Boolean isValidUser(String name) {
        try {
            return isValidAccountName(createContext(), name);
        } catch (NamingException e) {
            return false;
        }
    }

    /**
     * Gets LDAP Directory users based on a pattern
     * @param namePattern
     * @return
     */
    @Override
    public String[] getUsers(String namePattern) {
        List<String> groups = new ArrayList<String>();
        try {
            InitialDirContext initialDirContext = createContext();
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = { USERNAME};
            ctls.setReturningAttributes(attrIDs);
            NamingEnumeration results = initialDirContext.search(seyrenConfig.getLdapUserPattern(),
                    "(sAMAccountName=" + namePattern +"*)", ctls);
            while (results.hasMore()) {
                SearchResult rslt = (SearchResult) results.next();
                Attributes attrs = rslt.getAttributes();
                if(attrs.get(USERNAME) != null) {
                    groups.add((String) attrs.get(USERNAME).get());
                }
            }
            initialDirContext.close();
        } catch (NamingException e) {

        }
        return groups.toArray(new String[groups.size()]);
    }

    /**
     * Validates the username against the active directory context
     * @param dirContext
     * @param username
     * @return
     */
    private boolean isValidAccountName(InitialDirContext dirContext, String username) {
        try {
            SearchControls sc = new SearchControls();
            String[] attributeFilter = {FIRSTNAME, LASTNAME, MEMBERSHIP, USERNAME};
            sc.setReturningAttributes(attributeFilter);
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = dirContext.search(seyrenConfig.getLdapUserPattern(), "(sAMAccountName=" + username + ")", sc);
            if (results.hasMore()) {
                return true;
            }
        } catch (NamingException e) {
            return false;
        }
        return false;
    }

    /**
     * Creates a context for LDAP Protocol
     * @return
     * @throws NamingException
     */
    private InitialDirContext createContext() throws NamingException {
        return new InitialDirContext(contextConfiguration(seyrenConfig.getAdminUser(), seyrenConfig.getAdminPass()));
    }

    /**
     * Creates Directory Context
     * @param fullName
     * @param password
     * @return
     */
    private Hashtable<String, String> contextConfiguration(String fullName, String password) {
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactoryImpl);
        env.put(Context.PROVIDER_URL, seyrenConfig.getLdapUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, fullName);
        env.put(Context.SECURITY_CREDENTIALS, password);
        if (seyrenConfig.isLdapSsl()) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        return env;
    }

    public void setInitialContextFactoryImpl(String initialContextFactoryImpl) {
        this.initialContextFactoryImpl = initialContextFactoryImpl;
    }
}
