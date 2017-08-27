import urllib.request

WIDTH = 720
HEIGHT = 576
FOLDER = "demo"
NUM_IMGS = 1

URL = "http://lorempixel.com/%d/%d" % (WIDTH, HEIGHT)

for i in range(NUM_IMGS):
	# filename = FOLDER + "/" + format(i, '02d') + ".jpg"
	filename = FOLDER + "/91.jpg"
	urllib.request.urlretrieve(URL, filename)
