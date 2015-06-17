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
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.core.security.db;

import com.seyren.core.domain.User;
import com.seyren.core.security.UserManagement;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

public class DatabaseUserManagement implements UserManagement {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public DatabaseUserManagement(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String[] autoCompleteUsers(String pattern) {
        List<User> users = userDao.findLike("username", pattern);
        List<String> names = new ArrayList<String>();
        if (users == null) {
            return new String[]{};
        }
        for (User user : users) {
            names.add(user.getUsername());
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    public boolean addUser(User user) {
        try {
            User newUser = new User(user.getUsername(), passwordEncoder.encode(user.getPassword()));
            newUser.addRole("USER");
            userDao.save(newUser);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
