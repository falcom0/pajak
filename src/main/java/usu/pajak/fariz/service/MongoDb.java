package usu.pajak.fariz.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

public enum MongoDb {
    getInstance();
    public static final String LOCAL = "localhost:27017";
    public static final String SERVER = "localhost:27000";
    private Datastore datastore = null;

    public Datastore getDatastore(String from, String dbName) {
        datastore = new Morphia().createDatastore(
                new MongoClient(new MongoClientURI("mongodb://"+from)), dbName);
        return datastore;
    }
}
