#!flask/bin/python
from __future__ import print_function # In python 2.7
import sqlite3

from flask import Flask, jsonify, abort, request, make_response, g
from flask_cors import CORS, cross_origin

app = Flask(__name__, static_url_path="")
cors = CORS(app, resources={r"*": {"origins": "*"}})


###############################################################################
#
# Database
#
###############################################################################

# DB Structure
# Users table
#   id (device id)
#   location
#   update time

DATABASE = 'database.sqlite'

def get_db():
    db = getattr(g, '_database', None)
    if db is None:
        db = g._database = sqlite3.connect(DATABASE)
    return db

@app.teardown_appcontext
def close_connection(exception):
    db = getattr(g, '_database', None)
    if db is not None:
        db.close()


###############################################################################
#
# API Calls
#
###############################################################################

@app.route('/naloxalocate/api/v1.0/users', methods=['GET'])
def get_users():
    # Get db connection and select users from db
    cur = get_db().cursor()
    cur.execute('SELECT * FROM users')
    # Fetch all users from db result
    users = cur.fetchall()
    return jsonify(users=users)


@app.route('/naloxalocate/api/v1.0/users/<string:user_id>', methods=['GET'])
def get_user(user_id):
    pass

@app.route('/naloxalocate/api/v1.0/users', methods=['POST'])
def create_user():
    pass

@app.route('/naloxalocate/api/v1.0/users/<string:user_id>', methods=['PUT'])
def update_day(user_id):
    pass

@app.route('/naloxalocate/api/v1.0/users/<string:user_id>', methods=['DELETE'])
def delete_user(user_id):
    pass


if __name__ == '__main__':
    app.run(debug=True)
