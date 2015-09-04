import glob
import os
import json
import sys

# sys.path.append(os.path.abspath('../3rdparty/chats.py'))
import chats

def main():
  infilePath  = sys.argv[1]
  outfilePath = sys.argv[2]

  pathToConvos = infilePath + "/*.chat"

  with open(outfilePath, "a") as outfile:
    for filename in glob.iglob(pathToConvos):
      try:
        chat = chats.ChatLog.read(os.path.basename(filename), infilePath)

        for m in chat.messages:
          chatRecord = {
            "date": str(m.timestamp),
            "from": chat.sender,
            "to":   chat.to,
            "text": str(m.body),
            "source": 9
          }
          outfile.write(json.dumps(chatRecord) + "\n")
      except Exception as e:
        error = {
          "file": filename,
          "error": str(e)
        }
        print(str(error))

if __name__ == "__main__":
    main()
