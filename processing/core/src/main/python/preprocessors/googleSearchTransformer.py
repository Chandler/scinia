import glob
import os
import json
import sys
import datetime

def main():
  infilePath  = sys.argv[1]
  outfilePath = sys.argv[2]

  pathToConvos = infilePath + "/Searches/*.json"

  with open(outfilePath, "a") as outfile:
    for filename in glob.iglob(pathToConvos):
      try:
        with open (filename, "r") as infile:
          data = json.load(infile)

          for event in data["event"]:
            query     = event["query"]
            text      = query["query_text"]
            timestamp = query["id"][0]["timestamp_usec"]
            date      = datetime.datetime.fromtimestamp(int(timestamp)/1000000.0).isoformat()

            searchQuery = {
              "query": text,
              "date": date,
              "sourceId": 10
            }

            outfile.write(json.dumps(searchQuery) + "\n")
      except Exception as e:
        error = {
          "file": filename,
          "error": str(e)
        }
        print(str(error))

if __name__ == "__main__":
    main()
