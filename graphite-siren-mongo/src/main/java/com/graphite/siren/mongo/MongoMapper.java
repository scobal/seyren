package com.graphite.siren.mongo;

import java.util.HashMap;
import java.util.Map;

import com.graphite.siren.core.domain.Check;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoMapper {

	public Check checkFrom(DBObject dbo) {
		return new Check()
			.withId(dbo.get("_id").toString());
	}

	public DBObject checkToDBObject(Check check) {
		return new BasicDBObject(propertiesToMap(check));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map propertiesToMap(Check check) {
		Map map = new HashMap();
		map.put("_id", check.getId());
		return map;
	}

}
