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
import com.seyren.core.util.config.SeyrenConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.inject.Inject;

public class Initializer {
    private UserDao userDao;
    private PasswordEncoder passwordEncoder;
    private SeyrenConfig seyrenConfig;

    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.password}")
    private String adminPassword;
    @Value("${authentication.service}")
    private String serviceProvider;

    @Inject
    public Initializer(UserDao userDao, PasswordEncoder passwordEncoder, SeyrenConfig seyrenConfig) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.seyrenConfig = seyrenConfig;
    }

    public void initDataBase() {
        if (!seyrenConfig.isSecurityEnabled() || !serviceProvider.equals("database")) {
            return;
        }

        User adminUser = new User(adminUsername, this.passwordEncoder.encode(adminPassword));
        adminUser.addRole("USER");
        adminUser.addRole("ADMIN");
        this.userDao.save(adminUser);
    }
}
