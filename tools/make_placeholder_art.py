"""Draws HeavyMek's placeholder main-menu art.

MegaMek's own splash screen, logo, anniversary medal and window icons are BattleTech art, so HeavyMek
replaces them with simple generated images. They keep MegaMek's file names, so no code has to change.
Swap in real art any time: draw your own images with the same names and sizes.

Run from the repository root:
    python3 tools/make_placeholder_art.py

Needs Python 3 with Pillow (pip install pillow). Uses the Exo 2 font that ships in megamek/data/fonts.
"""
import math
import os

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MISC = os.path.join(ROOT, "megamek", "data", "images", "misc")
FONT = os.path.join(ROOT, "megamek", "data", "fonts", "Exo 2", "Exo2-Bold.ttf")

SKY_TOP = (22, 28, 36)       # dusk over the Badlands
SKY_BOTTOM = (64, 50, 34)    # dusty horizon
GRID = (214, 168, 92)        # amber hex lines
INK = (236, 196, 120)        # wordmark colour
SHADOW = (12, 10, 8)


def hexagon(cx, cy, radius):
    """Corner points of a flat-topped hexagon."""
    return [(cx + radius * math.cos(math.radians(60 * i)), cy + radius * math.sin(math.radians(60 * i)))
            for i in range(6)]


def background(width=2560, height=1440):
    """Dark gradient with a faint hex grid that fades towards the top."""
    image = Image.new("RGB", (width, height))
    pixels = image.load()
    for y in range(height):
        t = y / (height - 1)
        row = tuple(int(SKY_TOP[i] + (SKY_BOTTOM[i] - SKY_TOP[i]) * t) for i in range(3))
        for x in range(width):
            pixels[x, y] = row

    grid = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(grid)
    radius = 48
    step_x = radius * 1.5
    step_y = radius * math.sqrt(3)
    columns = int(width / step_x) + 2
    rows = int(height / step_y) + 2
    for column in range(columns):
        for row in range(rows):
            cx = column * step_x
            cy = row * step_y + (step_y / 2 if column % 2 else 0)
            alpha = int(18 + 80 * (cy / height) ** 1.5)
            draw.polygon(hexagon(cx, cy, radius), outline=GRID + (alpha,))
    image = Image.alpha_composite(image.convert("RGBA"), grid)

    # Darken the edges so the menu buttons and logo stand out.
    vignette = Image.new("L", (width, height), 0)
    ImageDraw.Draw(vignette).ellipse((-width * 0.2, -height * 0.3, width * 1.2, height * 1.3), fill=255)
    vignette = vignette.filter(ImageFilter.GaussianBlur(200))
    dark = Image.new("RGBA", (width, height), (0, 0, 0, 255))
    image = Image.composite(image, dark, vignette)
    return image.convert("RGB")


def wordmark(width=1800, height=520):
    """'HEAVYMEK' with a small subtitle, on a transparent canvas."""
    image = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    title_font = ImageFont.truetype(FONT, 300)
    sub_font = ImageFont.truetype(FONT, 64)
    title = "HEAVYMEK"
    subtitle = "UNOFFICIAL HEAVY GEAR TACTICAL GAME"

    box = draw.textbbox((0, 0), title, font=title_font)
    x = (width - (box[2] - box[0])) / 2 - box[0]
    y = 40 - box[1]
    shadow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).text((x + 10, y + 12), title, font=title_font, fill=SHADOW + (200,))
    image = Image.alpha_composite(image, shadow.filter(ImageFilter.GaussianBlur(8)))
    draw = ImageDraw.Draw(image)
    draw.text((x, y), title, font=title_font, fill=INK, stroke_width=6, stroke_fill=SHADOW)

    box = draw.textbbox((0, 0), subtitle, font=sub_font)
    sx = (width - (box[2] - box[0])) / 2 - box[0]
    draw.text((sx, 400), subtitle, font=sub_font, fill=(220, 214, 200), stroke_width=3, stroke_fill=SHADOW)
    return image


def icon(size):
    """Amber hexagon with 'HM' for the window and taskbar icon."""
    scale = 4  # draw large, then shrink for smooth edges
    big = size * scale
    image = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    centre = big / 2
    draw.polygon(hexagon(centre, centre, big * 0.49), fill=SHADOW + (255,))
    draw.polygon(hexagon(centre, centre, big * 0.43), fill=INK + (255,))
    if size >= 32:
        font = ImageFont.truetype(FONT, int(big * 0.42))
        box = draw.textbbox((0, 0), "HM", font=font)
        draw.text((centre - (box[2] + box[0]) / 2, centre - (box[3] + box[1]) / 2), "HM", font=font,
                  fill=SHADOW + (255,))
    return image.resize((size, size), Image.LANCZOS)


def main():
    os.makedirs(MISC, exist_ok=True)
    background().save(os.path.join(MISC, "mm-background.jpg"), quality=88)
    wordmark().save(os.path.join(MISC, "mm-logo.png"))
    # MegaMek drew an anniversary medal here; HeavyMek leaves the spot empty.
    Image.new("RGBA", (8, 8), (0, 0, 0, 0)).save(os.path.join(MISC, "mm-medal.png"))
    for size in (16, 32, 48, 256):
        icon(size).save(os.path.join(MISC, f"megamek-icon-{size}x{size}.png"))
    print("Wrote placeholder art to", MISC)


if __name__ == "__main__":
    main()
