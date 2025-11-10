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

POST /api/user/profile
Create or update user profile.

**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "name": "string",
  "bio": "string",
  "profilePicture": "string",
  "contactNumber": "string"
}
```

"""

import requests
from random import randint
import urllib3

fake_googleidtoken = f"fake_token{randint(1000,9999)}"

def verify_unauth_quick(ext, fake_googleidtoken):
  url = "http://3.135.231.73:3000/api/"
  extendedURL = url + ext
  """ assert this returns 401 unauthorized """
  r = requests.post(extendedURL, json={"idToken": fake_googleidtoken}, timeout=5)
  assert r.status_code == 401
  """ assert this returns in under 2s """

  assert r.elapsed.total_seconds() <= 2, f"Response time exceeded: {r.elapsed.total_seconds()} seconds"


  # print(f"'{ext}' request completed in {r.elapsed.total_seconds()} seconds with status code {r.status_code}")

signup_url = "auth/signup"
profile_url = "user/profile"
settings_url = "user/settings"
join_url = "matching/join"


# goal: verify 95% of unauth requests return 401 in under 2s
for callCount in range(101):
  verify_unauth_quick(signup_url, fake_googleidtoken)
  verify_unauth_quick(profile_url, fake_googleidtoken)
  verify_unauth_quick(settings_url, fake_googleidtoken)
  verify_unauth_quick(join_url, fake_googleidtoken)
  if (callCount % 10) == 0:
    print(f"{callCount} unauth requests completed successfully.")

print("All unauth requests completed within the time limit.")

print("##### userreq.py tests complete #####")
      
