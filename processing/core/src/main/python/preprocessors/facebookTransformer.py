import glob
import os
import json
import sys
from bs4 import BeautifulSoup
import pdb
from datetime import datetime

# this parser definitely leaves something to be desired

def main():
  infilePath  = sys.argv[1]
  outfilePath = sys.argv[2]

  with open(outfilePath, "a") as outfile:
    with open (infilePath, "r") as infile:
      print "reading file"
      html = BeautifulSoup(infile.read())
      
      print "finding threads"
      threads = html.find_all("div", class_="thread")

      print "processing threads"
      for thread in threads:
        try:
          users = thread.contents[0].split(",")
        except Exception as e:
          print( "bad thread\n" + str(e))

        # anything with more than 2 users is a group chat which we are
        # ignoring for now
        if (len(users) == 2):
          (participant1, participant2) = [name.strip() for name in users]
          messages = thread.find_all("div", class_="message")
          for message in messages:
            try:
              # Monday, July 8, 2013 at 6:31pm EDT'
              # strip timezone because datetime has a bug http://bugs.python.org/issue22377
              # I'll just let datetime go with UTC or local or whatever I don't really care
              dateString = message.find("span", class_="meta").contents[0][:-4]
              dateObj    = datetime.strptime(dateString, '%A, %B %d, %Y at %I:%M%p') #yikes
              
              sender     = message.find("span", class_="user").contents[0]
              recipient  = getRecipientName(sender,participant1,participant2)
              text       = message.findNext('p').contents[0]
              chatRecord = {
                "date": str(dateObj),
                "from": sender,
                "to":   recipient,
                "text": text,
                "source": 6
              }
              outfile.write(json.dumps(chatRecord) + "\n")

            except Exception as e:
              print( "bad message\n" + str(e))

# hmm "Kelcie Dalman didn't match either known participant: "Chandler Abraham","Kelcie Heppner"
# As you can see, facebook's impressively terrible download format has thread names that don't
# match the message names. fuck.
# So I guess I'll just check the first name, which should work because I only need to differentiate 
# between someone else and me, and I don't talk to any other Chandlers
def getRecipientName(senderName, participant1, participant2):

  senderFirstName       = senderName.split(" ")[0]
  participant1FirstName = participant1.split(" ")[0]
  participant2FirstName = participant2.split(" ")[0]

  if(senderName == participant1):
    return participant2
  
  elif (senderName == participant2):
    return participant1
  
  elif (participant1FirstName == participant2FirstName):
    raise Exception("full names don't match and the first names are the same, we're screwed: \"" + str(participant1) + "\",\"" + str(participant2) + "\"")
  
  elif (senderFirstName == participant1FirstName):  
    return participant2
  
  elif (senderFirstName == participant2FirstName):
    return participant1
  
  else:
    raise Exception(
      "senderName: " + str(senderName) + " didn't match either known participant: \"" + str(participant1) + "\",\"" + str(participant2) + "\""
      "senderName: " + str(senderFirstName) + " didn't match either known participant: \"" + str(participant1FirstName) + "\",\"" + str(participant2FirstName) + "\""
    )

if __name__ == "__main__":
    main()
