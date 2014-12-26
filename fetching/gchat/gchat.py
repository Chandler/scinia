# Read chats from gmail's imap interface then put together some stats
from collections import Counter, OrderedDict

import os
import sys
import imaplib
from xml.dom import minidom
from xml.parsers.expat import ExpatError
from datetime import datetime
from dateutil import parser as date_parser
from matplotlib import pyplot
import matplotlib.ticker as ticker
import numpy as np

class GChatLogs(object):
    """
    Auths with gmail and loads gchat logs.
    """

    def __init__(self, user, passwd):
        self.gmail = imaplib.IMAP4_SSL('imap.gmail.com')
        self.gmail.login(user, passwd)
        self.gmail.select('[Gmail]/Chats', True)
        self.ids = None

    @property
    def chat_ids(self):
        """
        Returns a list of all the chat ids.
        """
        if self.ids == None:
            result, data = self.gmail.search(None, "ALL")
            self.ids = data[0].split() # the ids are space separated
        return self.ids

    def get_chat(self, id):
        """
        Returns the chat with the log.
        """
        result, data = self.gmail.fetch(id, "(RFC822)")
        return ChatLog(id, data[0][1])

    def import_chats(self, directory):
        """
        Imports all of the chats into the directory given.
        """
        count = 0
        for id in self.chat_ids:
            if not os.path.exists('%s/%s.chat' % (directory, id)):
                try:
                    chat = self.get_chat(id)
                    chat.write(directory)
                    count += 1
                except:
                    print 'failed to get chat id: %s - %s' % (id, sys.exc_info()[0])
        print 'finished saving %d chat logs' % count


class ChatLog(object):
    """
    Returns data about the chat log.
    """

    def __init__(self, id, data):
        """
        Params:
          id: string
          data: imap data returned by gmail
        """
        self.id = id
        self.raw = data.replace("=\r\n", "").replace("=3D\"", "=\"")
        self.sender = self.header('From')
        self.to = self.header('To')
        self.date = date_parser.parse(self.header('Date'))
        self.subject = self.header('Subject')

        self._messages = None
        self._conversation = None


    def print_conversation(self):
        print self.to
        print self.sender
        print self.date
        for message in self.messages:
            print message

    @property
    def conversation(self):
        if not self._conversation:
            self._conversation = minidom.parseString(self.body())
        return self._conversation

    @property
    def messages(self):
        if self._messages != None:
            return self._messages

        try:
            self._messages = []
            for message in self.conversation.getElementsByTagName('cli:message'):
                self._messages.append(ChatMessage(message))
        except (ExpatError, KeyError) as e:
            print 'error parsing data for chat id %s: %s' % (id, e)

        return self._messages

    def header(self, name):
        """
        Returns the value of the specified header.
        """
        header = self.raw[:self.raw.index(';')]
        start = header.index(name + ":")
        end = header.index('\r\n', start)
        assert end > start
        value = header[start + len(name) + 1:end]
        return value.rstrip().lstrip()

    def body(self):
        start = self.raw.index("<con:")
        end = self.raw.rindex("</con:conversation>") + len("</con:conversation>")
        return self.raw[start:end]

    def write(self, directory='.'):
        """
        Writes the chat to a file in the directory.
        """
        f = open('%s/%s.chat' % (directory, self.id), 'w')
        f.write(self.raw)
        f.close()

    @classmethod
    def read(cls, filename, directory='.'):
        """
        Read the file from the current directory and returns a ChatLog object.

        Params:
          filename: name of a file in the current directory
        """
        f = open(directory + '/' + filename, 'r')
        try:
            id = filename[:-5]
            raw = f.read()
            return ChatLog(id, raw)
        finally:
            f.close()


class ChatMessage(object):
    """
    An individual message in a chat
    """

    def __init__(self, message):
        """
        Params:
          message: dom representation of the xml message
        """
        self.sender = message.attributes['to'].value
        body = message.getElementsByTagName('cli:body')
        self.timestamp = datetime.fromtimestamp(float(message.attributes['int:time-stamp'].value) / 1000)
        assert(len(body) == 1)
        self.body = body[0].childNodes[0].nodeValue

    def __str__(self):
        return "(%s) %s: %s" % (self.timestamp, self.sender, self.body)


def read_chats(directory):
    """
    Reads all the chats saved into a directory and returns them.

    :param directory: directory to read chats from
    :returns: a sequence of ChatLogs
    """
    files = os.listdir(directory)
    return [ChatLog.read(filename, directory) for filename in files]


def simple_analysis(directory):
    """
    Runs a report on the chat logs available.
    """
    chats = read_chats(directory)
    people = Counter()
    dates = Counter()
    weekdays = Counter()
    hours = Counter()

    for chat in chats:
        people[chat.sender] += 1
        dates[chat.date.date()] += 1
        weekdays[chat.date.isoweekday()] += 1
        hours[chat.date.hour] += 1

    plot_chat_volume(chats)


def convert(directory):
    """
    Runs a report on the chat logs available.
    """
    chats = read_chats(directory)

    for chat in chats:
      chat.print_conversation()
        
def plot_chat_volume(chats):
    """
    Gives a list of ChatLogs this creates a plot of number of conversations by date.

    :param chats: sequence of ChatLog
    """

    dates = Counter()
    for chat in chats:
        dates[chat.date.date()] += 1
    ordered = sorted(dates.keys())
    ordered = ordered[1:]
    fig = pyplot.figure()
    plot = fig.add_subplot(111)
    plot.plot([d.toordinal() for d in ordered], [dates[d] for d in ordered])

    def format_date(x, pos=None):
        d = datetime.fromordinal(int(x))
        return d.strftime('%Y-%m-%d')

    plot.xaxis.set_major_formatter(ticker.FuncFormatter(format_date))
    fig.autofmt_xdate()

    pyplot.show()

logger = GChatLogs(user='cbabraham@gmail.com', passwd='plzymlzjvsdxurws')

convert("all_old_school_gchats")
