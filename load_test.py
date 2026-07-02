import sys
import threading
import time
import requests

URL = "http://localhost:8080/hello"
HEADERS = {"X-API-Key": "load-test-client"}

def send_request(thread_id):
    try:
        response = requests.get(URL, headers=HEADERS)
        print(f"[Thread {thread_id}] Status {response.status_code} | Body: {response.text.strip()}")
    except Exception as e:
        print(f"[Thread {thread_id}] Error: {e}")

if __name__ == "__main__":
    print("Starting load test. Sending 10 concurrent requests to /hello (configured limit is 5/min)...")
    threads = []
    for i in range(10):
        t = threading.Thread(target=send_request, args=(i,))
        threads.append(t)
        t.start()
        
    for t in threads:
        t.join()
        
    print("Load test complete.")
