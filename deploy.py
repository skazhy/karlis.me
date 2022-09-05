#!/usr/bin/env python3

import hashlib
import json
import os
import urllib.request

AUTH_HEADER = {
    "Authorization": f"Bearer {os.environ.get('NETLIFY_TOKEN')}"
}


def file_hash(path):
    hasher = hashlib.sha1()
    with open(f"site/{path}", "rb") as f:
        buf = f.read()
        hasher.update(buf)
        return hasher.hexdigest()


def netlify_request(urn, method=None, json_body=None, binary_body=None):
    headers = AUTH_HEADER
    data = None
    if json_body is not None:
        headers["Content-Type"] = "application/json"
        data = bytes(json.dumps(json_body), "utf-8")

    if binary_body is not None:
        data = binary_body
        headers["Content-Type"] = "application/octet-stream"

    if (json_body is not None or binary_body is not None) and method is None:
        method = "POST"

    req = urllib.request.Request(
        f"https://api.netlify.com/api/v1/{urn}",
        method=method,
        data=data,
        headers=headers
    )
    data = urllib.request.urlopen(req).read()
    return json.loads(data.decode("utf-8"))


def upload_file(deploy_id, path):
    with open(f"site/{path}", "rb") as f:
        return netlify_request(
            f"deploys/{deploy_id}/files/{path}",
            method="PUT",
            binary_body=f.read()
        )


def upload_site():
    site_id = os.environ.get("NETLIFY_SITE_ID")
    payload = {
        "files": {"/index.html": file_hash("index.html")}
    }
    res = netlify_request(f"sites/{site_id}/deploys", json_body=payload)
    if res["required"]:
        print("Uploading index.html...")
        res = upload_file(res["id"], "index.html")
        print(res)
    else:
        print("Nothing to upload")


if __name__ == "__main__":
    upload_site()
