package usu.pajak.fariz.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

public enum MongoDb {
    getInstance();
    private static final String LOCAL = "localhost:27017";
    private static final String SERVER = "localhost:27000";
    private Datastore datastore = null;

    public Datastore getDatastore(String from, String dbName) {
        if(datastore==null) new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(
                new MongoClient(new MongoClientURI("mongodb://"+from)), dbName);
        return datastore;
    }
}
