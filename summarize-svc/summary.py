import json
import os

import requests
import os
from dotenv import load_dotenv

load_dotenv()


def get_summary_field():
    url = "https://dms.isinovasi.co.id/api/custom_fields/"
    dms_cred = os.environ["DMS_CRED"]
    headers = {"Authorization": dms_cred}
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()  # Raise an HTTPError for bad responses (4xx and 5xx)
        fields = response.json()['results']
        return fields
    except requests.exceptions.RequestException as e:
        print("An error occurred: ", e)
        return None


def get_document_info():
    url = "https://dms.isinovasi.co.id/api/documents/9/"
    dms_cred = os.environ["DMS_CRED"]
    headers = {"Authorization": dms_cred}
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()  # Raise an HTTPError for bad responses (4xx and 5xx)
        return response.json()  # Return the JSON response (or use response.text if not JSON)
    except requests.exceptions.RequestException as e:
        print("An error occurred: ", e)
        return None


def summarize_content(data):
    url = "https://api.perplexity.ai/chat/completions"
    apikey = os.environ["API_KEY"]
    payload = {
        "model": "llama-3.1-8b-instruct",
        "messages": [
            {
                "role": "system",
                "content": "Anda adalah asisten Intelijen Buatan yang bertugas "
                           "membantu menyimpulkan tujuan dari teks dokumen yang diberikan."
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


def add_note(data):
    url = "https://dms.isinovasi.co.id/api/documents/9/notes/"
    dms_cred = os.environ["DMS_CRED"]
    headers = {
        'Authorization': dms_cred,
        'Content-Type': 'application/json'
    }
    data = {"note": data}
    try:
        response = requests.request("POST", url, data=json.dumps(data), headers=headers)
        response.raise_for_status()  # Raise an HTTPError for bad responses (4xx and 5xx)
        return response.json()  # Return the JSON response (or use response.text if not JSON)
    except requests.exceptions.RequestException as e:
        print("An error occurred:", e)
        return None


if __name__ == "__main__":
    print("start")
    doc_info = get_document_info()
    ai_results = summarize_content(doc_info["content"])
    summary = "[SUMMARY] " + ai_results["choices"][0]["message"]["content"]
    print(summary)
    add_note(summary)
