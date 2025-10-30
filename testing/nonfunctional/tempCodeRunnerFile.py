"""
Goal, test to see if 95% of the time user requests can be done in under 2s
"""

"""
POST /api/auth/signup
Purpose: Exchange Google ID token for JWT authentication token.

**Request:**
```json
{
  "idToken": "string" // Google OAuth ID token
}
```

**Response (200):**
```json
{
  "token": "string", // JWT token for future requests
  "user": {
    "userId": "string",
    "name": "string",
    "email": "string",
    "profilePicture": "string",
    "credibilityScore": 100
  }
}
```

"""

import requests

"""
Firstly we create a fake user by providing a mock Google ID token.

Then we use the received JWT token to authenticate subsequent requests.
"""

url = "http://3.135.231.73:3000/api/auth/signup"

from random import randint

mock_google_id_token = f"fake_google_id_token_for_testing_purposes_{randint(1000,9999)}"

response = requests.post(url, json={"idToken": mock_google_id_token})

if response.status_code == 200:
    jwt_token = response.json().get("token")
    print("JWT Token received:", jwt_token)
else:
    print("Failed to receive JWT Token:", response.status_code, response.text)


