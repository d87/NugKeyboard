import re

f = open("words.txt", 'r', encoding='utf-8')
words = f.readlines()
f.close()

pairs = {}

vowels = set(['а','о','и','е','ё','у','ы','э','ю','я', 'ъ','ь'])
consonants = set(['б','в','г','д','ж','з','й','к','л','м','н','п','р','с','т','ф','х','ц','ч','ш','щ'])

def get_sound_type(l):
    if l in vowels:
        return "vowel"
    elif l in consonants:
        return "consonant"
    else:
        return "other"


for word in words:
    # print(word)
    length = len(word)
    print("    ")
    for i in range(length-2):
        if length >= 2:
            lword = word.lower()
            letter1 = lword[i]
            letter2 = lword[i+1]
            pair = get_sound_type(letter1)+"-"+get_sound_type(letter2)
            if pair not in pairs:
                pairs[pair] = 0
            pairs[pair] += 1

all_pairs = list(pairs.keys())
all_pairs.sort()


f = open("pairs_by_sound.txt", "w", encoding='utf-8')

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
