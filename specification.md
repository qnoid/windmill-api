#### The user must be able to see his/her list of exports deployed ####

##### The API must support persisting a new "export" given a "user" #####
  * The API at `POST /user/{user}/export` should create a new `export` for the given user and persist it.
    A POST accepts a `manifest.pist` file that holds all the values (i.e. identifier, title, version) for the `export`.
    The identifier, version is a joined key. It is perfectly acceptable to receive a `POST` for the same windmill but with different contents for the `{windmill}.ipa`.
  * If an `export` for the given identifier, version is not found then create a new entry.
  * If an `export` exists,	
	* Under the same account, skip creating a new entry in the database. (The contents of the `ipa` and `plist` should still be posted to the `AWS` bucket).
	* Under a different account, treat it as an error.

##### The API must support getting all the "windmill"s of a "user" #####

The API at `GET /user/{user}/exports` should return a JSON of every export, like the following : 

```javascript
[{
    "id": 1,
    "title": "{export_title}",
    "version": "{export_version}",
    "createdAt" "{timestamp}",
    "modifiedAt" "{timestamp}",
    "URL": "itms-services://?action=download-manifest&url=https://ota.windmill.io/{user_identifier}/{export_identifier}/{export_version}/{export_title}.plist"
}]
```
