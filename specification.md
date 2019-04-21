# The API must support "accepting" a new `export` given an `account`
  * A `POST /account/{account}/export` creates a new `export` for the given `manifest` and persists it.
    A POST accepts a `manifest.pist` file that holds all the values (i.e. identifier, title, version) for the `export`.
    It is perfectly acceptable to receive a `POST` for an export with same bundle identifier.
  * If an `export` for the given bundle is not found then create a new entry.
  * The `export` is NOT assigned to the `account` immediately.
    * The API returns a URI where the client can `PUT` the IPA.
    * The API returns the identifier for the `export` to be used for assigning the `export` to an `account`

# The API must support assigning an `export` to a given `account`
  * A `PATCH /account/{account}/export/{export}` assigns an existing `export` to an authorised `account`.
  * The `export` must have previously been created via the `POST /account/{account}/export`.
  * If the given `export` exists under a different account, treat it a client error.

# The API must support returning the list of `export` for a given `account`

The API at `GET /account/{account}/exports` should return a JSON of every `export`, like the following : 

```javascript
[{
    "id": {an ordinal id},
    "identifier": "{export_identifier, unique per account_identifier}",
	"bundle": "metadata.bundle-identifier, unique globally",
    "title": "{metadata.title}",
    "version": "{metadata.version}",
    "createdAt" {timestamp in epoch seconds},
    "modifiedAt" {timestamp in epoch seconds},
    "url": "itms-services://?action=download-manifest&url=https://api.windmill.io/export/manifest/{authentication}"
}]
```
