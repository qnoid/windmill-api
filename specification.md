WindMill.io Specification(s) 
============================

#### The user must be able to see his/her list of windmills deployed ####

##### The API must support persisting a new "windmill" given a "user" #####
  * The API at `POST /user/{user}/windmill` should create a new `Windmill` for the given user and persist it.
    A POST accepts a `manifest.pist` file that holds all the values (i.e. identifier, title, version) for the `Windmill`.
    The identifier, version is a joined key. It is perfectly acceptable to receive a `POST` for the same windmill but with different contents for the `{windmill}.ipa`.
  * If a `Windmill` for the given identifier, version is not found then create a new entry.
  * If a `Windmill` exists, skip creating a new entry in the database. (The contents of the `ipa` and `plist` should still be posted to the `AWS` bucket).

##### The API must support getting all the "windmill"s of a "user" #####

The API at `GET /user/{user}/windmill` should return a JSON of every windmill, like the following : 

```javascript
[{
    "id": 1,
    "title:": "{windmill_title}",
    "version:": "{windmill_version}",
    "URL": "itms-services://?action=download-manifest&url=https://ota.windmill.io/{user_identifier}/{windmill_identifier}/{windmill_version}/{windmill_title}.plist"
}]
```


