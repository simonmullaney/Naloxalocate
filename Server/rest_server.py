#!flask/bin/python
###############################################################################
#
# Instructions
#
# Run this app with the command
# $ python rest-server.py
#
###############################################################################

from __future__ import print_function # In python 2.7
import sqlite3, os

from flask import Flask, jsonify, abort, request, make_response, g
from flask_cors import CORS, cross_origin


app = Flask(__name__, static_url_path="")
app.config['DEBUG'] = True
cors = CORS(app, resources={r"*": {"origins": "*"}})


###############################################################################
#
# API Calls
#
###############################################################################
# curl -X DELETE http://localhost:5000/naloxalocate/api/v1.0/users/3

@app.route('/')
def hello_world():
  return 'Hello from Flask!'

@app.route('/api/v1.0/users', methods=['GET'])
def get_users():
    # Select users from db and return as json
    return jsonify(users=query_db('SELECT * FROM users'))

@app.route('/api/v1.0/users/<int:user_id>', methods=['GET'])
def get_user(user_id):
    user = query_db('SELECT * FROM users WHERE id=?', [user_id])
    if not user:
        abort(404)
    else:
        return jsonify(user=user)

@app.route('/api/v1.0/users', methods=['POST'])
def create_user():
    lastid = query_db('INSERT INTO users DEFAULT VALUES', getLastId=True)
    return jsonify(user_id=lastid)

@app.route('/api/v1.0/users/<int:user_id>', methods=['PUT'])
def update_user(user_id):
    user = query_db('SELECT * FROM users WHERE id=?', [user_id])
    if not user:
        abort(404)
    else:
        # TODO
        pass

@app.route('/api/v1.0/users/<int:user_id>', methods=['DELETE'])
def delete_user(user_id):
    user = query_db('SELECT * FROM users WHERE id=?', [user_id])
    if not user:
        abort(404)
    else:
        query_db('DELETE FROM users WHERE id=?', [user_id])
        return jsonify(result=True)


###############################################################################
#
# Error Handlers
#
###############################################################################

@app.errorhandler(400)
def bad_request(error):
    return make_response(jsonify({'error': 'Bad request'}), 400)

@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)

###############################################################################
#
# Database
#
###############################################################################
# http://flask.pocoo.org/docs/0.11/patterns/sqlite3/

# DB Structure
# Users table
#   id (device id)
#   location
#   update time

DATABASE = os.path.join(app.root_path, 'database.sqlite')
print(DATABASE)

def get_db():
    """Opens a new database connection if there is none yet for the
    current application context.
    """
    db = getattr(g, '_database', None)
    if db is None:
        db = g._database = sqlite3.connect(DATABASE)
    return db

@app.teardown_appcontext
def close_connection(exception):
    """Closes the database again at the end of the request."""
    db = getattr(g, '_database', None)
    if db is not None:
        db.close()

def query_db(query, args=(), one=False, dict=False, getLastId=False):
    db = get_db()
    if dict: # Return a dictionary
        db.row_factory = sqlite3.Row
    cur = db.execute(query, args)
    if getLastId:
        rv = cur.lastrowid
    else:
        rv = cur.fetchall()
    db.commit()
    cur.close()
    return (rv[0] if rv else None) if one else rv

###############################################################################
#
# For Testing
#
###############################################################################
# https://github.com/pallets/flask/blob/master/examples/flaskr/flaskr/flaskr.py

def init_db():
    """Initializes the database."""
    db = get_db()
    with app.open_resource('schema.sql', mode='r') as f:
        db.cursor().executescript(f.read())
    db.commit()

@app.cli.command('initdb')
def initdb_command():
    """Creates the database tables."""
    init_db()
    print('Initialized the database.')


if __name__ == '__main__':
    app.run(debug=True)
