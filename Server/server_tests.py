import os
import rest_server
import unittest
import tempfile

# Based off https://github.com/pallets/flask/blob/master/examples/flaskr/tests/test_flaskr.py

class FlaskrTestCase(unittest.TestCase):

    def setUp(self):
        # Create a new temp database every test with a random name
        self.db_fd, rest_server.app.DATABASE = tempfile.mkstemp()
        rest_server.app.config['TESTING'] = True
        rest_server.app.config['JSONIFY_PRETTYPRINT_REGULAR'] = False
        self.app = rest_server.app.test_client()
        with rest_server.app.app_context():
            rest_server.init_db()

    def tearDown(self):
        os.close(self.db_fd)
        os.unlink(rest_server.app.DATABASE)


    # Function starting with test is a test
    def test_post_get_delete_users(self):
        # Create user 1
        rv = self.app.post('/api/v1.0/users')
        assert '{"user_id":1}' in rv.data

        # Create user 2
        rv = self.app.post('/api/v1.0/users')
        assert '{"user_id":2}' in rv.data

        # Get all users
        rv = self.app.get('/api/v1.0/users')
        assert '{"users":[[1,null,null],[2,null,null]]}' in rv.data

        # Get user 2 only
        rv = self.app.get('/api/v1.0/users/2')
        assert '{"user":[[2,null,null]]}' in rv.data

        # Get non existing user
        rv = self.app.delete('/api/v1.0/users/3')
        assert response404 in rv.data

        # Delete user 2
        rv = self.app.delete('/api/v1.0/users/2')
        assert '{"result":true}' in rv.data

        # Get all users. Only user 1 should be left
        rv = self.app.get('/api/v1.0/users')
        assert '{"users":[[1,null,null]]}' in rv.data

        # Delete user 1
        rv = self.app.delete('/api/v1.0/users/1')
        assert '{"result":true}' in rv.data

        # Get all users. Result should be empty
        rv = self.app.get('/api/v1.0/users')
        assert '{"users":[]}' in rv.data

response404 = b'<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">\n<title>404 Not Found</title>\n<h1>Not Found</h1>\n<p>The requested URL was not found on the server.  If you entered the URL manually please check your spelling and try again.</p>'

if __name__ == '__main__':
    unittest.main()

