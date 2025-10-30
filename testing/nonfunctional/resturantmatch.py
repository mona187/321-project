"""
Goal of this file:
	Test if system can return a resturant match in 5 seconds or less for 90% of requests 

"""

import requests 

url = "http://3.135.231.73:3000/api/restaurant/search"
		

params = {
	"latitude" : 49.2606,
	"longitude": 1,
	"radius": 5000,
	"cuisine": "Italian",
	"price_level": 2
}


def test_restaurant_match_response_time():
	# Make GET request to the restaurant search API
	r = requests.get(url=url, params=params)
	assert r.elapsed.total_seconds() <= 5, f"Response time exceeded: {r.elapsed.total_seconds()} seconds"
	return r.status_code == 200

if __name__ == "__main__":
	for i in range(101):
		assert test_restaurant_match_response_time()
		if (i % 10) == 0:
			print(f"{i} requests completed successfully.")
	print("All requests completed within the time limit.")
