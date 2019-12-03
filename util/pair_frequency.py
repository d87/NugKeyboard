import re

f = open("words.txt", 'r', encoding='utf-8')
words = f.readlines()
f.close()

pairs = {}

alphabet = set()

for word in words:
    # print(word)
    length = len(word)
    print("    ")
    for i in range(length-2):
        if length >= 2:
            lword = word.lower()
            pair = lword[i:i+2]
            alphabet.add(lword[i])
            if pair not in pairs:
                pairs[pair] = 0
            pairs[pair] += 1

alphabetSorted = sorted(list(alphabet))

all_pairs = list(pairs.keys())
all_pairs.sort()


f = open("pairs.txt", "w", encoding='utf-8')

# for letter in alphabetSorted:
#     letter_pairs = [ pair for pair in all_pairs if pair.startswith(letter) ]
#     letter_pairs.sort(key=lambda pair: pairs[pair], reverse=True )
#     for pair in letter_pairs:
#         count = pairs[pair]
#         f.write(pair)
#         f.write("  ")
#         f.write(str(count))
#         f.write("\n")
#     f.write("\n")

all_pairs.sort(key=lambda pair: pairs[pair], reverse=True )
for pair in all_pairs:
    count = pairs[pair]
    f.write(pair)
    f.write("  ")
    f.write(str(count))
    f.write("\n")

f.close()

# print(all_pairs)

# matches = re.findall("(\S+)\s.+", text)
# f = open("output_words.txt", "w", encoding='utf-8')
# for word in matches:
#     f.write(word)
#     f.write("\n")
# f.close()
