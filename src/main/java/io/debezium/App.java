package io.debezium;

import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;
import java.lang.annotation.Target;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
        service = App.class,
        immediate = true
)
public class App {

//    public static void main(String[] args) {
//        org.apache.log4j.BasicConfigurator.configure(); //uncomment this line to show more execution details
//        (new App()).runOracleCDC();
//    }

    @Activate
    public void startUpMethod() {
//        org.apache.log4j.BasicConfigurator.configure(); //uncomment this line to show more execution details
        (new App()).runOracleCDC();
    }

    public void runOracleCDC() {
        System.setProperty("LD_LIBRARY_PATH","/root/instantclient_12_2/");
        System.setProperty("java.library.path","/root/instantclient_12_2/");
        System.out.println("library path: " + System.getProperty("LD_LIBRARY_PATH"));

        // Define the configuration for the embedded and Oracle connector ...
        //Note:
        //configurations on the database should be done as described in debezium documentation.
        //unsupported data types will throw parsing error
        //primary key must be added to the table which is monitored.
        Configuration config = Configuration.create()
                .with("name", "test1")
                .with("tasks.max", "1")
                .with("database.server.name", "server1")
                .with("database.hostname", "10.100.5.219")
                .with("database.port", 1521)
                .with("database.user", "c##xstrm")
                .with("database.password", "xs")
                .with("database.dbname","ORCLCDB")
                .with("database.pdb.name","ORCLPDB1")
                .with("database.out.server.name", "dbzxout")
                .with("connector.class", "io.debezium.connector.oracle.OracleConnector")
                .with("offset.storage",
                        "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename",
                        "/Users/charukak/TestProjects/Embedded-Debezium-CDC/offset.dat") //provide per your environment.
                .with("database.history",
                        "io.debezium.relational.history.FileDatabaseHistory")
                .with("database.history.file.filename",
                        "/Users/charukak/TestProjects/Embedded-Debezium-CDC/history.dat") //provide per your environment.
                .with("table.whitelist", "ORCLPDB1.DEBEZIUM.sweetproductiontable")
                .build();
//                .with("database.tablename.case.insensitive", true)


        /*
        * .with("connector.class", "io.debezium.connector.oracle.OracleConnector")
                .with("tasks.max", "1")
                .with("offset.storage",
                        "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename",
                        "/Users/charukak/TestProjects/Embedded-Debezium-CDC/offsetNewnew1.dat") //provide per your environment.
                .with("offset.flush.interval.ms", 2000)
                .with("name", "oracle_debezium_connector")
                .with("database.hostname", "localhost") //provide docker ip address if docker container db is used.
                .with("database.port", "1521")
                .with("database.user", "c##xstrm")
                .with("database.password", "xs")
                .with("database.sid", "ORCLCDB").with("table.whitelist", "customers" )
                .with("database.server.name", "mServer")
                .with("database.out.server.name", "dbzxout")
                .with("database.history",
                        "io.debezium.relational.history.FileDatabaseHistory")
                .with("database.history.file.filename",
                        "/Users/charukak/TestProjects/Embedded-Debezium-CDC/dbhistory3.dat") //provide per your environment.
                .with("database.dbname", "ORCLCDB")
                .with("database.pdb.name", "ORCLPDB1")

        *
        * */

        EmbeddedEngine.CompletionCallback completionCallback = (b, s, throwable) -> {
            System.out.println("---------------------------------------------------");
            System.out.println("success status: " + b + "\n, message : " + s + "\n" +
                    ", Error: " + throwable);
        };

        //Just for development purposes.
        EmbeddedEngine.ConnectorCallback connectorCallback = new EmbeddedEngine.ConnectorCallback() {
            /**
             * Called after a connector has been successfully started by the engine;
             */
            @Override
            public void connectorStarted() {
                System.out.println("connector started successfully");
            }

            /**
             * Called after a connector has been successfully stopped by the engine;
             */
            @Override
            public void connectorStopped() {
                System.out.println("connector stopped successfully");
            }

            /**
             * Called after a connector task has been successfully started by the engine;
             */
            @Override
            public void taskStarted() {
                System.out.println("connector task has been successfully started");
            }

            /**
             * Called after a connector task has been successfully stopped by the engine;
             */
            @Override
            public void taskStopped() {
                System.out.println("connector task has been successfully stopped");
            }
        };

        // Create the engine with this configuration ...
        EmbeddedEngine engine = EmbeddedEngine.create()
                .using(config)
                .using(connectorCallback)
                .using(completionCallback)
                .notifying(this::handleEvent)
                .build();

        // Run the engine asynchronously ...
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);

        try {
            Thread.sleep(10000);
            AllLoadedNativeLibrariesInJVM.listAllLoadedNativeLibrariesFromJVM();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //After some time or operations, stop the engine.
        //engine.stop();
    }

    public void runMysqlCDC() {
        // Define the configuration for the embedded and MySQL connector ...
        Configuration config = Configuration.empty();
        config = config.edit()
                /* begin engine properties */
                .with("connector.class",
                        "io.debezium.connector.mysql.MySqlConnector").build();

        config = config.edit()
                .with("offset.storage",
                        "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename",
                        "/Users/charukak/TestProjects/Embedded-Debezium-CDC/offsetNewnew1.dat")
                .with("offset.flush.interval.ms", 1000)
                /* begin connector properties */
                .with("name", "mySqlConnector")
                .with("database.hostname", "localhost")
                .with("database.port", 3306)
                .with("database.user", "root")
                .with("database.password", "1234")
                .with("server.id", 5756)
                .with("database.server.name", "mySqlConnectorServer")
                .with("database.history",
                        "io.debezium.relational.history.FileDatabaseHistory")
                .with("database.history.file.filename",
                        "/Users/charukak/TestProjects/Embedded-Debezium-CDC/dbhistoryNewnew1.dat").build();


        EmbeddedEngine.CompletionCallback callback = (b, s, throwable) -> {
            System.out.println("---------------------------------------------------");
            System.out.println("success status: " + b + ", message : " + s + ", Error: " + throwable);
        };

        // Create the engine with this configuration ...
        EmbeddedEngine engine = EmbeddedEngine.create()
                .using(config)
                .using(callback)
                .notifying(this::handleEvent)
                .build();


        // Run the engine asynchronously ...
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);




    }

    public void handleEvent(ConnectRecord connectRecord) {
        System.out.println("haha :::::: " + connectRecord);
    }
}
