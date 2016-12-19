## Using the Data Accessor Module for decrypting CSV files.

Please use DataAccessor.jar, as described below:
``` 
> java -jar path/to/DataAccessor.jar <name of encrypted CSV file>
```
E.g.:
``` 
> java -jar path/to/DataAccessor.jar backup.csv
```

The resulting JSON file will be located in the directory from where the
JAR file was executed, with the name __decrypted.json__.

It is possible to customise the name of the JSON file by supplying a 
second argument to the JAR file, like so:
``` 
> java -jar path/to/DataAccessor.jar <name of encrypted CSV file> <name of JSON file>
```
E.g.:
``` 
> java -jar path/to/DataAccessor.jar backup.csv result.json
```

## Notes

- Please note that in order to successfully run the JAR file, the RSA
private key (ePeg_pkcs8) has to be in the same folder as the JAR file.

- If the target JSON file already exists, the new data will be appended
to it.

- Each record will be decrypted into its own JSON object, however these
JSON object will not have a JSON array containing them created for them,
nor will they have separators between them.