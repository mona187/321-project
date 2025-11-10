from time import sleep
import requests

def confirmConnection():

    # this program should be run when it is likely the server is already live. 
    # It is just to confirm as a precondition for our python test files
    waitTime = 5 #seconds
    totalTime = 30 #seconds
    timeTaken = 0
    atmNum = 0
    totalAttempts = waitTime/totalTime

    url = "http://3.135.231.73:3000/health/"

    while (timeTaken <= totalTime):

        try:
            req = requests.get(url=url, timeout=5)
            assert(req.status_code == 200) 
            print("Successfully connected to server")
            return True
            break
            
        except requests.exceptions.ConnectionError as e:
            atmNum+1
            print(f"Failed to connect to server, attempt {atmNum}/{totalAttempts}")
            sleep(waitTime)
            timeTaken += waitTime

    return False

if __name__ == "__main__":
    assert(confirmConnection())



