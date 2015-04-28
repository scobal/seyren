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
package com.seyren.core.security.ldap;


import com.seyren.core.domain.User;
import com.seyren.core.security.UserManagement;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;


public class LdapUserManagement extends FilterBasedLdapUserSearch implements UserManagement {
    private static final String USERNAME = "sAMAccountName";
    private final BaseLdapPathContextSource contextSource;

    public LdapUserManagement(BaseLdapPathContextSource contextSource) {
        super("", "", contextSource);
        this.contextSource = contextSource;
    }

    @Override
    public String[] autoCompleteUsers(String name) {
        List<String> users = new ArrayList<String>();
        try {
            DirContext readOnlyContext = contextSource.getReadOnlyContext();
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {USERNAME};
            ctls.setReturningAttributes(attrIDs);
            NamingEnumeration<SearchResult> results = readOnlyContext.search("", "(sAMAccountName=" + name + "*)", ctls);
            while (results.hasMore()) {
                SearchResult rslt = results.next();
                Attributes attrs = rslt.getAttributes();
                if (attrs.get(USERNAME) != null) {
                    users.add((String) attrs.get(USERNAME).get());
                }
            }
        } catch (NamingException e) {

        }
        return users.toArray(new String[users.size()]);
    }

    @Override
    public boolean addUser(User user) {
        return false;
    }
}
