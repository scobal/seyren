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
package com.seyren.core.security.mongo;


import com.seyren.core.domain.User;
import com.seyren.core.store.UserStore;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.inject.Inject;


public class MongoUserDetails implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserStore userStore;

    @Inject
    public MongoUserDetails(PasswordEncoder passwordEncoder, UserStore userStore) {
        this.passwordEncoder = passwordEncoder;
        this.userStore = userStore;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("The user with name " + username + " could not be found");
        }

        return user;
    }

    private User getUser(String name) {
        return userStore.getUser(name);
    }
}
