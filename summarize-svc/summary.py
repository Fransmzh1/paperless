import os

import requests
import os
from dotenv import load_dotenv

load_dotenv()
def call_dms():
    url = "https://dms.isinovasi.co.id/api/documents/9/"
    dms_cred = os.environ["DMS_CRED"]
    headers = {"Authorization": dms_cred}
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()  # Raise an HTTPError for bad responses (4xx and 5xx)
        return response.json()  # Return the JSON response (or use response.text if not JSON)
    except requests.exceptions.RequestException as e:
        print("An error occurred: {e}")
        return None

def call_summarize(data):
    url = "https://api.perplexity.ai/chat/completions"
    apikey = os.environ["API_KEY"]
    payload = {
        "model": "llama-3.1-8b-instruct",
        "messages": [
            {
                "role": "system",
                "content": "Anda adalah asisten Intelijen Buatan yang bertugas membantu menyimpulkan tujuan dari teks dokumen yang diberikan."
            },
            {
                "role": "user",
                "content": data
            }
        ]
    }
    headers = {
        "accept": "application/json",
        "content-type": "application/json",
        "authorization": "Bearer " + apikey
    }

    response = requests.post(url, json=payload, headers=headers)
    # print(response.text)
    return response.json()


# Example usage
if __name__ == "__main__":
    print("start")
    result = call_dms()
    print(result["content"])
    summary = call_summarize(result["content"])
    rangkum = summary["choices"][0]["message"]["content"]
    print(rangkum)

