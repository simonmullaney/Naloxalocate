# Naloxalocate

Software Engineering Project - App to locate opioid reversal drug Naloxone

## Run Android App
The latest beta build of the app can be downloaded from the Google Play Store here [https://play.google.com/store/apps/details?id=com.apaulling.naloxalocate](https://play.google.com/store/apps/details?id=com.apaulling.naloxalocate)

## Run Server Locally
Please note that the server component is live at [apaulling.com/naloxalocate/](http://apaulling.com/naloxalocate/) which the app communicates with. For testing, the flask server can be run locally. Please make sure to have both [Python 2.7](https://www.python.org/downloads/) and [Flask](http://flask.pocoo.org/) installed. To start the server, run

```sh
$ python Server/rest_server.py
```

The server should be started at [http://127.0.0.1:5000/api/v1.0/users](http://127.0.0.1:5000/api/v1.0/users)

## Is it tested?

You bet your bottom dollar it's tested! To see the tests pass, with the server running locally as above, run

```sh
$ python Server/server_tests.py
```
