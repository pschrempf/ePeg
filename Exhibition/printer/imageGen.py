import argparse
from PIL import Image, ImageFont, ImageDraw

def run(pegQ, avg_time):

    # W x H
    image_size = (336, 192)

    # This is the level at which the indicator arrow will be placed
    score_indicator_y = 50

    # The x-coordinate of the arrow will be interpolated between these two points
    score_indicator_min_x = 1
    score_indicator_max_x = 328

    # These values will be based on which we shall interpolate
    score_min = -1.5
    score_max = 1.5

    # Score text start coordiantes
    pegQ_score_coords = (201, 77)
    avg_time_score_coords = (225, 109)

    # Coordinate for the complementary text
    extra_text_coords = (6, 150)

    # Complementary texts
    extra_texts = ["You're pretty quick with your\ndominant hand!"]

    # Load the font that we want to use to write onto the label
    score_font = ImageFont.truetype("printer/font/josefinsans.ttf", 24, encoding='unic')
    arrow_font = ImageFont.truetype("printer/font/arial.ttf", 18, encoding='unic')
    extra_font = ImageFont.truetype("printer/font/josefinsans.ttf", 19, encoding='unic')

    # Load the label template
    im = Image.open("printer/hist_template.png")

    draw = ImageDraw.Draw(im)

    # Draw the arrow
    # First we have to calculate the x-coordinate
    domain_size = score_max - score_min
    range_size = score_indicator_max_x - score_indicator_min_x

    # Force the pegQ to be in the allowed range that we can draw
    capped_pegQ = min(max(pegQ, score_min), score_max)

    # Simple scaling formula
    score_indicator_x = (capped_pegQ - score_min) / domain_size * range_size + score_indicator_min_x

    draw.text((score_indicator_x, score_indicator_y), "↑", font=arrow_font, fill="black")

    # Draw the scores
    hand = "right" if pegQ >=0 else "left"
    pegQ_str = hand + ",  {0:.2f}"
    draw.text(pegQ_score_coords, pegQ_str.format(pegQ), font=score_font, fill="black")

    draw.text(avg_time_score_coords, "{0:.0f} ms".format(avg_time), font=score_font, fill="black")

    # Draw the complementary text
    extra_text = "For more information, visit:"
    w, h = draw.textsize(extra_text, font=extra_font)
    draw.text(((image_size[0]-w)/2, extra_text_coords[1]), extra_text, font=extra_font, fill="black")

    extra_text = "https://neurogenetics.st-andrews.ac.uk"
    w, h = draw.textsize(extra_text, font=extra_font)
    draw.text(((image_size[0]-w)/2, extra_text_coords[1] + 21), extra_text, font=extra_font, fill="black")

    # Save the image
    im.save("printer/customLabel.png", "PNG")

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="Create a custom image to be printed on the ePeg labels.")
    parser.add_argument('pegQ', type=float, help="The pegQ score to be printed.")
    parser.add_argument('avg_time', type=float, help="The average time to be printed.")

    args = parser.parse_args()

    run(args.pegQ, args.avg_time)
