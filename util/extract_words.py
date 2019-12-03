import re

f = open("freqrnc2011.csv", 'r', encoding='utf-8')

text = f.read()
f.close()

matches = re.findall("(\S+)\s.+", text)
f = open("words.txt", "w", encoding='utf-8')
for word in matches:
    f.write(word)
    f.write("\n")
f.close()
