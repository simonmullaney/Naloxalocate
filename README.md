# Naloxalocate

Software Engineering Project - App to locate opioid reversal drug Naloxone

## Run Server Locally
Please note that the server component is live at [apaulling.com/naloxalocate/](http://apaulling.com/naloxalocate/) which the app communicates with. For testing, the flask server can be run locally. Please make sure to have both [Python 2.7](https://www.python.org/downloads/) and [Flask](http://flask.pocoo.org/) installed. To start the server, run

```sh
$ python Server/rest_server.py
```

The server should be started at [http://127.0.0.1:5000/api/v1.0/users](http://127.0.0.1:5000/api/v1.0/users)

## Is it tested?

You betcha. To see the tests (hopefully) pass, run

```sh
$ python Server/server_tests.py
```
