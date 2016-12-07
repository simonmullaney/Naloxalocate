#!flask/bin/python
from __future__ import print_function # In python 2.7
import sqlite3

# DB Structure
# id (device id)
# location
# update time

dbPath = "database.sqlite"

db = sqlite3.connect(dbPath)
db.row_factory = sqlite3.Row


@app.route('/naloxalocate/api/v1.0/users', methods=['GET'])
def get_users():
    pass

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
