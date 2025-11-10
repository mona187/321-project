"""
Goal of this file:
	Test if system can return a resturant match in 5 seconds or less for 90% of requests 

"""

import requests 
from time import sleep
import urllib3
from serverLive import confirmConnection

url = "http://3.135.231.73:3000/api/restaurant/search"
		

params = {
	"latitude" : 49.2606,
	"longitude": 1,
	"radius": 5000,
	"cuisine": "Italian",
	"price_level": 2
}

failureCount = 0
totalCount = 101


def test_restaurant_match_response_time():
	# Make GET request to the restaurant search API
	r = requests.get(url=url, params=params,timeout=6)

	assert r.elapsed.total_seconds() <= 5, f"Response time exceeded: {r.elapsed.total_seconds()} seconds"
	return r.status_code == 200

if __name__ == "__main__":
	assert(confirmConnection)

 
	for i in range(totalCount):
		try:
			assert test_restaurant_match_response_time()
			if (i % 10) == 0:
				print(f"{i} requests completed successfully.")
		except requests.exceptions.ConnectionError as e:
				print(f"Connection error: {e}. Retrying in 5 seconds...")
				sleep(5)
				failureCount += 1
		except requests.exceptions.ConnectTimeout as ct:
				print(f"Timeout error: {e} Retrying in 5 seconds")
				sleep(5)
				failureCount += 1

	# assert 90% of req in correct time
	assert((1 - failureCount/totalCount) >= 0.90)

	print("All requests completed within the time limit.")
	
	print("##### resturantmatch.py tests complete #####")
