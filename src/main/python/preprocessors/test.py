import sys

def main:
  print("running script")
  print("input: " + sys.argv[0])
  print("output: " + sys.argv[1])
  with open(sys.argv[1], "a") as outfile
    outfile.write("woooooo")

if __name__ == "__main__":
    main()
