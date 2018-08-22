from PIL import Image, ImageDraw
import sys

im = Image.open("large.png")

draw = ImageDraw.Draw(im)

im.save("haha.png", "PNG")
