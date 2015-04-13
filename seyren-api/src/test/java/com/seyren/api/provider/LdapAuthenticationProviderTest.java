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
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LdapAuthenticationProviderTest {

    private LdapAuthenticationProvider authenticationProvider;
    private final SeyrenConfig seyrenConfig = mock(SeyrenConfig.class);
    @Before
    public void setUp() throws Exception {
        when(seyrenConfig.getLdapUrl()).thenReturn("ldap://ldap.com");
        when(seyrenConfig.isLdapSsl()).thenReturn(false);
        when(seyrenConfig.getAdminUser()).thenReturn("admin");
        when(seyrenConfig.getAdminPass()).thenReturn("pass");
        when(seyrenConfig.getLdapDomain()).thenReturn("SEYREN");
        when(seyrenConfig.getLdapUserPattern()).thenReturn("dc=COM");
        authenticationProvider = new LdapAuthenticationProvider(seyrenConfig);
        authenticationProvider.setInitialContextFactoryImpl(MockInitialDirContextFactory.class.getName());
    }

    @Test
    public void testIsValidUser() throws Exception {
        User user = new User();
        user.setUsername("John");
        user.setPassword("Doe");
        final DirContext mockContext = MockInitialDirContextFactory.getLatestMockContext();
        NamingEnumeration<SearchResult> mockNamingEnumeration = new MockNamingEnumeration();
        when(mockContext.search(eq("dc=COM"), anyString(), any(SearchControls.class))).thenReturn(mockNamingEnumeration);
        Boolean validCredentials = authenticationProvider.isValidCredentials(user);
        assertEquals(validCredentials, true);
    }

    @Test
    public void testUserExists() throws Exception {
        User user = new User();
        user.setUsername("John");
        user.setPassword("Doe");
        final DirContext mockContext = MockInitialDirContextFactory.getLatestMockContext();
        NamingEnumeration<SearchResult> mockNamingEnumeration = new MockNamingEnumeration();
        when(mockContext.search(eq("dc=COM"), anyString(), any(SearchControls.class))).thenReturn(mockNamingEnumeration);
        Boolean validUser =  authenticationProvider.isValidUser(user.getUsername());
        assertEquals(validUser, true);
    }

    @Test
    public void testUserDoesNotExist() throws Exception {
        User user = new User();
        user.setUsername("Seyren");
        user.setPassword("User");
        final DirContext mockContext = MockInitialDirContextFactory.getLatestMockContext();
        NamingEnumeration<SearchResult> mockNamingEnumeration = new MockEmptyEnumeration();
        when(mockContext.search(eq("dc=COM"), anyString(), any(SearchControls.class))).thenReturn(mockNamingEnumeration);
        Boolean validUser =  authenticationProvider.isValidUser(user.getUsername());
        assertEquals(validUser, false);
    }

    public static class MockInitialDirContextFactory implements InitialContextFactory {
        private static DirContext mockContext = mock(DirContext.class);

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            return mockContext;
        }

        public static DirContext getLatestMockContext() {
            return mockContext;
        }
    }
    public static class MockNamingEnumeration implements NamingEnumeration<SearchResult> {

        @Override
        public SearchResult next() throws NamingException {
            BasicAttributes basicAttributes = new BasicAttributes();
            basicAttributes.put("memberOf", "groups=SCT Admin");
            basicAttributes.put("givenName", "John Doe");
            basicAttributes.put("sn", "JD");
            basicAttributes.put("sAMAccountName", "johndoe");
            return new SearchResult("test", new Object(), basicAttributes);
        }

        @Override
        public boolean hasMore() throws NamingException {
            return true;
        }

        @Override
        public void close() throws NamingException {

        }

        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public SearchResult nextElement() {
            return null;
        }
    }

    public static class MockEmptyEnumeration implements NamingEnumeration<SearchResult> {

        @Override
        public SearchResult next() throws NamingException {
            return null;
        }

        @Override
        public boolean hasMore() throws NamingException {
            return false;
        }

        @Override
        public void close() throws NamingException {

        }

        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public SearchResult nextElement() {
            return null;
        }
    }
}