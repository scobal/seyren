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
package com.seyren.mongo;

import com.mongodb.BasicDBObject;

public final class NiceDBObject extends BasicDBObject {
    
    private static final long serialVersionUID = 1L;
    
    private NiceDBObject(String field, Object value) {
        put(field, value);
    }
    
    public static NiceDBObject forId(Object id) {
        return object("_id", id);
    }
    
    public static NiceDBObject object(String field, Object value) {
        return new NiceDBObject(field, value);
    }
    
    public NiceDBObject with(String field, Object value) {
        put(field, value);
        return this;
    }
    
}
