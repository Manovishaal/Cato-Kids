"""
Builds every picture in Cato Kids from its licensed source and writes the Kotlin lookup.

    python build_art.py <kenney_kits_dir> <freesvg_thumbs_dir>

<kenney_kits_dir> holds the unzipped Kenney kits (one folder per kit, as downloaded from
kenney.nl); <freesvg_thumbs_dir> holds FreeSVG's 600px renders named <download id>.png.

Outputs
  app/src/main/res/drawable-nodpi/art_<codepoints>.webp   one picture per glyph
  app/src/main/java/com/catokids/app/ui/components/ArtCatalog.kt
  tools/art/ART_MANIFEST.csv                               source, creator, licence per glyph
"""
import csv
import io
import os
import sys

from PIL import Image, ImageFilter

sys.path.insert(0, os.path.dirname(__file__))
from mapping import MAP, CLIP  # noqa: E402
from render_obj import render  # noqa: E402

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
RES = os.path.join(ROOT, "app/src/main/res/drawable-nodpi")
KT = os.path.join(ROOT, "app/src/main/java/com/catokids/app/ui/components/ArtCatalog.kt")
SIZE = 320

ANIMAL_GRID = [
    ["snake", "giraffe", "penguin", "panda", "parrot"],
    ["monkey", "hippo", "pig", "elephant", "rabbit"],
    ["whale", "chicken", "chick", "horse", "moose"],
    ["walrus", "bear", "narwhal", "frog", "buffalo"],
    ["rhino", "zebra", "gorilla", "duck", "cow"],
    ["goat", "owl", "crocodile", "sloth", "dog"],
]
ROW_EDGES = [0, 225, 415, 605, 797, 990, 1180]

KIT_NAMES = {   # mapping.py folder name -> Kenney kit name, page
    "food-kit": "Food Kit", "car-kit": "Car Kit", "watercraft-kit": "Watercraft Kit",
    "space-kit": "Space Kit", "nature-kit": "Nature Kit", "hexagon-kit": "Hexagon Kit",
    "city-kit-suburban": "City Kit (Suburban)", "city-kit-commercial": "City Kit (Commercial)",
    "furniture-kit": "Furniture Kit", "mini-shop": "Mini Market", "retro-urban-kit": "Retro Urban Kit",
    "mini-characters": "Mini Characters", "cube-pets": "Cube Pets", "animal-pack": "Animal Pack Remastered",
    "board-icons": "Board Game Icons",
}
KIT_PAGE = {"animal-pack": "animal-pack-remastered", "board-icons": "board-game-icons", "mini-shop": "mini-market"}


def cps(glyph):
    return "_".join(f"{ord(c):04x}" for c in glyph.replace("️", ""))


def tidy(im):
    """Trim, centre on a square canvas and add a soft white sticker edge."""
    im = im.convert("RGBA")
    a = im.getchannel("A")
    if a.getextrema()[1] == 0:
        raise ValueError("empty image")
    im = im.crop(a.point(lambda v: 255 if v > 8 else 0).getbbox())
    inner = int(SIZE * 0.84)
    w, h = im.size
    k = inner / max(w, h)
    im = im.resize((max(1, round(w * k)), max(1, round(h * k))), Image.LANCZOS)
    canvas = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    canvas.alpha_composite(im, ((SIZE - im.width) // 2, (SIZE - im.height) // 2))
    edge = canvas.getchannel("A").filter(ImageFilter.MaxFilter(7)).filter(ImageFilter.GaussianBlur(1))
    sticker = Image.new("RGBA", (SIZE, SIZE), (255, 255, 255, 0))
    sticker.putalpha(edge)
    sticker.alpha_composite(canvas)
    return sticker


def white_to_alpha(im):
    """Some FreeSVG drawings sit on a white card; knock out white that touches the border."""
    from PIL import ImageDraw
    im = im.convert("RGBA")
    flat = Image.new("RGBA", im.size, (255, 255, 255, 255))
    flat.alpha_composite(im)
    rgb = flat.convert("RGB")
    W, H = im.size
    for seed in [(0, 0), (W - 1, 0), (0, H - 1), (W - 1, H - 1), (W // 2, 0), (W // 2, H - 1), (0, H // 2), (W - 1, H // 2)]:
        if rgb.getpixel(seed) != (1, 2, 3) and min(rgb.getpixel(seed)) > 235:
            ImageDraw.floodfill(rgb, seed, (1, 2, 3), thresh=22)
    keep = Image.eval(rgb.convert("L"), lambda v: 255)
    px, k = rgb.load(), keep.load()
    for y in range(H):
        for x in range(W):
            if px[x, y] == (1, 2, 3):
                k[x, y] = 0
    from PIL import ImageChops
    im.putalpha(ImageChops.multiply(im.getchannel("A"), keep))
    return im


_animals = {}


ROW_CENTRES = [130, 320, 510, 700, 893, 1085]


def animal(kdir, name):
    """Cut one animal out of the vector sheet by flood-filling its silhouette."""
    from PIL import ImageDraw, ImageChops
    if not _animals:
        import resvg_py
        svg = os.path.join(kdir, "animal-pack-remastered/Vector/round.svg")
        _animals["_sheet"] = Image.open(io.BytesIO(bytes(resvg_py.svg_to_bytes(svg_path=svg, zoom=2)))).convert("RGBA")
    sheet = _animals["_sheet"]
    for r, row in enumerate(ANIMAL_GRID):
        if name in row:
            c = row.index(name)
            cx, cy = (c * 256 + 128) * 2, ROW_CENTRES[r] * 2
    mask = sheet.getchannel("A").point(lambda v: 255 if v > 0 else 0)
    ImageDraw.floodfill(mask, (cx, cy), 128)
    only = mask.point(lambda v: 255 if v == 128 else 0)
    out = sheet.copy()
    out.putalpha(ImageChops.multiply(sheet.getchannel("A"), only))
    return out.crop(only.getbbox())


BOARD_TINT = {"crown_a": (247, 185, 40), "lock_open": (247, 185, 40)}


def kenney(kdir, ref, tmp):
    kit, name = ref.split("/", 1)
    if kit == "animal-pack":
        return animal(kdir, name)
    if kit == "board-icons":
        import resvg_py
        svg = os.path.join(kdir, "board-game-icons/Vector/Icons", name + ".svg")
        im = Image.open(io.BytesIO(bytes(resvg_py.svg_to_bytes(svg_path=svg, width=512, height=512)))).convert("RGBA")
        tint = Image.new("RGBA", im.size, BOARD_TINT.get(name, (243, 190, 140)) + (255,))
        tint.putalpha(im.getchannel("A"))
        return tint
    folder = {"mini-shop": "mini-market"}.get(kit, kit)
    obj = os.path.join(kdir, folder, "Models/OBJ format", name + ".obj")
    render(obj, tmp, size=SIZE + 64)
    return Image.open(tmp)


def main(kdir, thumbs):
    os.makedirs(RES, exist_ok=True)
    for f in os.listdir(RES):
        if f.startswith("art_"):
            os.remove(os.path.join(RES, f))
    images, icons, draws, credits = {}, {}, {}, {}
    tmp = os.path.join(os.path.dirname(thumbs), "_render.png")
    failed = []
    for glyph, (kind, ref) in sorted(MAP.items()):
        key = glyph.replace("️", "")
        try:
            if kind == "icon":
                icons[key] = ref
                credits[key] = ("Material Symbols: " + ref, "Google", "Apache-2.0", "https://fonts.google.com/icons")
                continue
            if kind == "draw":
                draws[key] = ref
                credits[key] = ("Drawn by the app (" + ref + ")", "Cato Kids", "Project's own", "")
                continue
            if kind == "text":
                continue
            if kind == "kenney":
                im = kenney(kdir, ref, tmp)
                kit = ref.split("/")[0]
                credits[key] = (f"Kenney {KIT_NAMES.get(kit, kit)}: {ref.split('/', 1)[1]}", "Kenney (kenney.nl)", "CC0-1.0",
                                f"https://kenney.nl/assets/{KIT_PAGE.get(kit, kit)}")
            else:   # clip
                row = CLIP[glyph]
                im = white_to_alpha(Image.open(os.path.join(thumbs, ref + ".png")))
                if glyph == "👃":      # the drawing is a nose above a mouth; keep the nose
                    im = im.crop((0, 0, im.width, int(im.height * 0.42)))
                credits[key] = (f"FreeSVG: {row['slug']} ({row['date']})", f"{row['by']} via FreeSVG.org", "CC0-1.0",
                                f"https://freesvg.org/{row['slug']}")
            name = "art_" + cps(glyph)
            tidy(im).save(os.path.join(RES, name + ".webp"), "WEBP", quality=88, method=6)
            images[key] = name
        except Exception as e:    # report, never silently ship a gap
            failed.append((glyph, ref, repr(e)))

    write_kotlin(images, icons, draws)
    write_manifest(credits, images)
    print(f"{len(images)} pictures, {len(icons)} icons, {len(draws)} drawn")
    if failed:
        print("FAILED:", failed)
        sys.exit(1)


def kstr(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"') + '"'


def icon_ident(name):
    return "".join(p.capitalize() if not p[0].isdigit() else p for p in name.split("_"))


def write_kotlin(images, icons, draws):
    out = ["package com.catokids.app.ui.components", "",
           "import androidx.compose.material.icons.Icons",
           "import androidx.compose.material.icons.rounded.*",
           "import androidx.compose.ui.graphics.vector.ImageVector",
           "import com.catokids.app.R", "",
           "/*",
           " * Generated by tools/art/build_art.py — do not edit by hand.",
           " * Every glyph the app once showed as an emoji now resolves to real artwork:",
           " * a bundled illustration (Kenney CC0 or public-domain clipart), a Material icon",
           " * for UI symbols, or a colour/shape drawn by the app. Sources: THIRD_PARTY_NOTICES.md.",
           " */", "",
           "internal val ART_IMAGES: Map<String, Int> = mapOf("]
    out += [f"    {kstr(g)} to R.drawable.{n}," for g, n in sorted(images.items())]
    out += [")", "", "internal val ART_ICONS: Map<String, ImageVector> = mapOf("]
    out += [f"    {kstr(g)} to Icons.Rounded.{icon_ident(n)}," for g, n in sorted(icons.items())]
    out += [")", "", "/** \"shape:colour\" or \"clock:hour\" — see [DrawnArt]. */",
            "internal val ART_DRAWN: Map<String, String> = mapOf("]
    out += [f"    {kstr(g)} to {kstr(s)}," for g, s in sorted(draws.items())]
    out += [")", ""]
    open(KT, "w", encoding="utf-8").write("\n".join(out))


def write_manifest(credits, images):
    path = os.path.join(os.path.dirname(__file__), "ART_MANIFEST.csv")
    rows = list(csv.DictReader(open(path, encoding="utf-8")))
    for r in rows:
        key = r["glyph"].replace("️", "")
        c = credits.get(key)
        r["asset"] = images.get(key, "")
        r["source"], r["creator"], r["license"], r["source_url"] = c if c else ("text", "", "", "")
    w = csv.DictWriter(open(path, "w", encoding="utf-8", newline=""), fieldnames=list(rows[0]))
    w.writeheader()
    w.writerows(rows)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
