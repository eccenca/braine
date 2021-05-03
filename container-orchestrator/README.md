# Relay
This tool reads metrics from k8s metrics server and write it to a selected outputstream.

## Dependencies
There are three not standard libraries that required to be preinstalled:
```
pip install yaml # required for reading configuration file
pip install cmem-cmempy # for communicating with CMEM
```

## Configuration

The configuration can be setup by editing the `config.yaml` file as follows:

`out`: use this parameter to add or remove an outputstream channel.
The relay supports two types of outputstreams `stdout` (default) and `cmem`.

## Updating CMEM credentials
If you are working with CMEM edit the script `runnable.py` adding your credentiials as follows:

Select your authentication type and replace the question mark (?) by your respective credentials.

For cmemc authentication using user and password:
```
# setup the environment for the connection to Corporate Memory
environ["CMEM_BASE_URI"] = "https://braine.eccenca.dev/" # target cmem instance when writing to 'cmem'
# use the following for 'password' OAUTH_GRANT_TYPE
environ["OAUTH_GRANT_TYPE"] = "password"
environ["OAUTH_USER"] = "?"
environ["OAUTH_PASSWORD"] = "?"
environ["OAUTH_CLIENT_ID"] = "cmemc"
```

For service account authentication type:
```
environ["CMEM_BASE_URI"] = "https://braine.eccenca.dev/" # target cmem instance when writing to 'cmem'
# use the following for 'client_credentials' OAUTH_GRANT_TYPE
environ["OAUTH_GRANT_TYPE"] = "client_credentials"
environ["OAUTH_CLIENT_ID"] = "cmem-service-account"
environ["OAUTH_CLIENT_SECRET"] = "?"
```

## Usage 
Now we are all set, execute `runnable.py` as following:
```
python runnable.py
No image to activate has been found...
...
```
