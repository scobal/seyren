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
package com.seyren.integrationtests.mongo;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

/**
 * Load mongodb before jetty starts
 */
public class MongoDbIT {
    @Test
    public void testPopulateMongoDb() throws Exception {
        Mongo mongo = new MongoClient("localhost", 27017);
        DB db = mongo.getDB("seyren");
        
        File[] collections = readCollectionDirectory();
        for (File collection : collections) {
            Collection<File> jsonFiles = readJsonFiles(collection);
            loadJsonFiles(collection, jsonFiles, db);
        }
        
    }
    
    private File[] readCollectionDirectory() {
        return new File(this.getClass().getResource(".").getPath()).listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
    }
    
    private Collection<File> readJsonFiles(File collection) {
        Collection<File> files = FileUtils.listFiles(
                collection,
                new SuffixFileFilter(".json"),
                DirectoryFileFilter.DIRECTORY
                );
        System.out.println(files);
        return files;
    }
    
    private void loadJsonFiles(File collection, Collection<File> jsonFiles, DB db) throws Exception {
        String collectionName = collection.getName();
        DBCollection collectionMongoDb = db.getCollection(collectionName);
        for (File jsonFile : jsonFiles) {
            String json = FileUtils.readFileToString(jsonFile);
            DBObject dbObject = (DBObject) JSON.parse(json);
            collectionMongoDb.insert(dbObject);
        }
        // DBCursor cursorDoc = collectionMongoDb.find();
        // while (cursorDoc.hasNext()) {
        // System.out.println(cursorDoc.next());
        // }
    }
    
}
