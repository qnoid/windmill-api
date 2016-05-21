WindMill.io Backend
===================


### Pre-Requisites ###

1. You need Java 1.8
2. You need Maven 3.3.x 
3. You need to download and install locally a distribution of Wildfly.10.0.0.Final 
(download [here](http://wildfly.org/downloads/))
4. You need to have a postgres DB running locally, with `user:windmill`, `pwd:windmill`, and `db name:windmill` and `schema: public`.
 (download [here](http://www.postgresql.org/download/))


Specifications 
--------------
See [here](specification.md)


Build & Run 
-----------

### How to Setup the data source at your local application server ###

Assuming that your Wildfly Server is running!

Navigate to the root (`/windmill`) of the project and execute.

```
mvn clean package -Psetup-datasource -Dmaven.home=<Path_To_Wildfly>
```

where the `<Path_To_Wildfly>` should the directory where you have the app server e.g `~/dev/wildfly-10.0.0.Final`

### How to build the windmill war for Local deployment / use ###

Navigate to the root (`/windmill`) of the project and execute the following

```
mvn clean install
```

This will eventually build your war with `local` PostgresDB settings.

If you want to create a war that points to an in memory Wildfly H2 DB, then you need to do the following:

```
mvn clean install -Ph2
```

This will package whithin your war, a persistence.xml that will point to an already configured/standard datasource in Wildfly.

### How to Build for AWS ###
Navigate to the root (`/windmill`) of the project and execute.

```
mvn clean install -Dpostgres.username=<SOMETHING>
                  -Dpostgres.pwd=<SOMETHING> 
                  -Dpostgres.server.ip=<SOMETHING> 
                  -Dpostgres.server.port=<SOMETHING> 
                  -Dpostgres.server.databaseName=<SOMETHING>
```

The above properties, should point to the 'AWS' specific postgres.

### How to Deploy to AWS ###

Under the `/scripts` folder you will find the following :

1. `deploy-aws.sh` :  copies the war to AWS


API Endpoints 
-------------

### Main API ### 

The current setup initializes windmill's RESTFul Api in the following url :

`http://<IP>:8080/`

### Healthcheck ### 

Check if everything works. 
`http://<IP>:8080/healthcheck`
