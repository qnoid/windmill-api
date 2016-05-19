WindMill.io Backend
===================


### Pre-Requisites ###

1. You need Java 1.8
2. You need Maven 3.3.x 
3. You need to download and install locally a distribution of Wildfly.10.0.0.Final
4. You need to have a postgres DB running locally, with user:windmill, pwd:windmill, and db name: windmill.


Build & Run 
-----------

### How to Setup the data source at a local server ###

Assuming that your Wildfly Server is running!

Navigate to the root (`/windmill`) of the project and execute.

```
mvn clean package -Psetup-datasource -Dmaven.home=<Path_To_Wildfly>
```

### How to Build for Local ###

Navigate to the root (`/windmill`) of the project and execute. This will eventually build your war with PostgresDB settings

```
mvn clean install
```

If you want to create a war that points to an in memory Wildfly H2 DB, then you need to do the following

```
mvn clean install -Ph2
```

### How to Build for AWS ###

Navigate to the root (`/windmill`) of the project and execute.


```
mvn clean install -Dpostgres.username=<SOMETHING>
                  -Dpostgres.pwd=<SOMETHING> 
                  -Dpostgres.server.ip=<SOMETHING> 
                  -Dpostgres.server.port=<SOMETHING> 
                  -Dpostgres.server.databaseName=<SOMETHING>
```

### How to Deploy to AWS ###

Under the `/scripts` folder you will find the following :

1. `deploy-aws.sh` :  copies the war to AWS


API Endpoints 
-------------

### Main API ### 

The current setup initializes windmill's RESTFul Api in the following url :
`http://<IP>/windmill/`

### Healthcheck ### 

Check if everything works. 
`http://<IP>/windmill/healthcheck`
