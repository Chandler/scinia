# full disclosure, this is the first python I've written in 4 years. 
# This is a parser that goes from a directory of Google Voice history
# files, to a single json file.

import glob
from bs4 import BeautifulSoup
import os
import json
import sys

def main():
  infilePath  = sys.argv[1]
  outfilePath = sys.argv[2]

  pathToConvos = infilePath + "/Calls/*Text*.html"

  with open(outfilePath, "a") as outfile:
    for filename in glob.iglob(pathToConvos):
      try:
        with open (filename, "r") as infile:
          html = BeautifulSoup(infile.read())

          messages = html.find_all("div", class_="message")

          # This is not efficient, but it's easy to reason about
          participant1, participant2 = getParticipants(filename)
  
          for m in messages:
            sender    = str(m.find_all(["span","abbr"], class_="fn").pop().text).strip()
            recipient = getRecipientName(sender, participant1, participant2)
            date      = str(m.find('abbr')['title'])
            text      = str(m.find('q').text)
            
            # This matches the ChatRecord schema used in the scala application 
            chatRecord = {
              "date": date,
              "from": sender,
              "to":   recipient,
              "text": text,
              "source": 2 # com.scinia.LoaderId.GVOICE
            }

            outfile.write(json.dumps(chatRecord) + "\n")
      except Exception as e:
        error = {
          "file": filename,
          "error": str(e)
        }
        print(str(error))

# Files do not reliablly contain any participant info
# The only gauranteed way to find both participants
# is to check the filename.
# participant1 is in the filename
# participant2 is always "Me"
def getParticipants(filename):
  try:
    participant1 = os.path.basename(filename).split("-")[0].strip()
    participant2 = "Me"
    return(participant1, participant2)
  except Exception as e:
    raise Exception("Something blew up with the filename: " + str(e))

# at the top of the file we know both participants
# in each message we know the sender. The recipient
# is the opposite of the sender.
def getRecipientName(senderName, participant1, participant2):
  if (senderName == participant1):
    return participant2
  elif(senderName == participant2):
    return participant1
  else:
    raise Exception(
      "senderName: " + str(senderName) + " didn't match either known participant: " + str(participant1) + "," + str(participant2)
    )

if __name__ == "__main__":
    main()
