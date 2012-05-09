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
