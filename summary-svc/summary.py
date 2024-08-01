import requests

def call_dms():
    """
    Calls an external service using the POST method.

    :param url: The URL of the external service.
    :param data: The data to be sent in the POST request (typically a dictionary).
    :param headers: Optional headers to include in the request.
    :return: Response object from the request.
    """

    url = "https://dms.isinovasi.co.id/api/documents/9/"
    headers = {"Authorization": "Basic ZnJhbnM6bGlidXJhbnNlcnU="}
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()  # Raise an HTTPError for bad responses (4xx and 5xx)
        return response.json()  # Return the JSON response (or use response.text if not JSON)
    except requests.exceptions.RequestException as e:
        print(f"An error occurred: {e}")
        return None

def call_summarize(data):
    url = "https://api.perplexity.ai/chat/completions"
    apikey = "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"
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

