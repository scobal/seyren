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
import com.seyren.core.security.UserManagement;
import com.seyren.core.store.UserStore;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;

public class MongoUserManagement implements UserManagement {

    private final PasswordEncoder passwordEncoder;
    private final UserStore userStore;

    @Inject
    public MongoUserManagement(PasswordEncoder passwordEncoder, UserStore userStore) {
        this.passwordEncoder = passwordEncoder;
        this.userStore = userStore;
    }

    @Override
    public String[] autoCompleteUsers(String name) {
        return userStore.autoCompleteUsers(name);
    }

    @Override
    public boolean addUser(User user) {
        User userInMongo = userStore.getUser(user.getUsername());
        if(userInMongo == null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(new HashSet<String>(Arrays.asList("USER")));
            userStore.addUser(user);
            return true;
        }
        return false;
    }
}
