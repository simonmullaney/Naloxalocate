#!flask/bin/python
from __future__ import print_function # In python 2.7
import sqlite3

from flask import Flask, jsonify, abort, request, make_response, g
from flask_cors import CORS, cross_origin

app = Flask(__name__, static_url_path="")
cors = CORS(app, resources={r"*": {"origins": "*"}})

###############################################################################
#
# API Calls
#
###############################################################################

@app.route('/naloxalocate/api/v1.0/users', methods=['GET'])
def get_users():
    # Select users from db and return as json
    return jsonify(users=query_db('select * from users'))


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


###############################################################################
#
# Error Handlers
#
###############################################################################

# @app.errorhandler(400)
# def bad_request(error):
#     return make_response(jsonify({'error': 'Bad request'}), 400)

# @app.errorhandler(404)
# def not_found(error):
#     return make_response(jsonify({'error': 'Not found'}), 404)

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
        # db.row_factory = sqlite3.Row
    return db

@app.teardown_appcontext
def close_connection(exception):
    db = getattr(g, '_database', None)
    if db is not None:
        db.close()

def query_db(query, args=(), one=False, dict=False):
    db = get_db()
    if dict: # Return as a dictionary
        db.row_factory = sqlite3.Row
    cur = db.execute(query, args)
    rv = cur.fetchall()
    cur.close()
    return (rv[0] if rv else None) if one else rv


if __name__ == '__main__':
    app.run(debug=True)
