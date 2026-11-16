"""
Rewrites the "Picture artwork" part of THIRD_PARTY_NOTICES.md from ART_MANIFEST.csv,
so the credits always match what is bundled. Run after build_art.py.
"""
import collections
import csv
import os

HERE = os.path.dirname(__file__)
ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
NOTICES = os.path.join(ROOT, "THIRD_PARTY_NOTICES.md")
START, END = "<!-- art:start -->", "<!-- art:end -->"


def main():
    rows = list(csv.DictReader(open(os.path.join(HERE, "ART_MANIFEST.csv"), encoding="utf-8")))
    kenney = collections.defaultdict(list)
    clip, icons, drawn = [], [], []
    for r in rows:
        if r["license"] == "CC0-1.0" and r["creator"].startswith("Kenney"):
            kit = r["source"].split(":")[0].replace("Kenney ", "")
            kenney[(kit, r["source_url"])].append(r["source"].split(": ", 1)[1])
        elif "FreeSVG" in r["creator"]:
            clip.append(r)
        elif r["license"] == "Apache-2.0":
            icons.append(r)
        elif r["source"].startswith("Drawn"):
            drawn.append(r)

    out = [START, "## Picture artwork",
           "Every picture a child sees is bundled with the app (offline) and comes from one of the",
           "sources below. All are human-made, free for commercial use and carry no watermark.",
           "`tools/art/ART_MANIFEST.csv` lists the source of every individual picture, and",
           "`tools/art/build_art.py` rebuilds them (`app/src/main/res/drawable-nodpi/art_*.webp`).", "",
           "### Kenney (kenney.nl) — Creative Commons Zero (CC0 1.0)",
           "3D kits are rendered to pictures by `tools/art/render_obj.py` from the original models;",
           "the Animal Pack and Board Game Icons are rendered from Kenney's own vector files.", "",
           "| Kit | Models used | Source |", "|---|---|---|"]
    for (kit, url), names in sorted(kenney.items()):
        out.append(f"| {kit} | {', '.join(sorted(set(names)))} | {url} |")
    out += ["", "### Public-domain clipart — Creative Commons Zero (CC0 1.0)",
            "Individual drawings from [FreeSVG.org](https://freesvg.org), most originally shared on",
            "OpenClipart. Each page states \"Public Domain (CC0)\". Only drawings published before",
            "2022 were chosen, to stay clear of AI-generated uploads. Some are lightly cropped or",
            "have a white background removed.", "",
            "| Picture | Drawing | Published by | Date | Source |", "|---|---|---|---|---|"]
    seen = set()
    for r in sorted(clip, key=lambda r: r["source_url"]):
        if r["source_url"] in seen:
            continue
        seen.add(r["source_url"])
        name, date = r["source"].replace("FreeSVG: ", "").rsplit(" (", 1)
        users = ", ".join(x["unicode_name"] for x in clip if x["source_url"] == r["source_url"])
        out.append(f"| {users} | {name} | {r['creator'].replace(' via FreeSVG.org', '')} | {date.rstrip(')')} | {r['source_url']} |")
    out += ["", "### Material Symbols — Apache License 2.0",
            "Interface symbols (stars, hearts, ticks, arrows, locks and similar) use Google's",
            "Material icons from `androidx.compose.material:material-icons-extended`.", "",
            "### Drawn by the app",
            "Colour swatches, geometric shapes and clock faces are drawn in code (`DrawnArt`).", "",
            "## Nunito typeface — SIL Open Font License 1.1",
            "Copyright 2014 The Nunito Project Authors (https://github.com/googlefonts/nunito).",
            "Static weights (`app/src/main/res/font/nunito_*.ttf`) were cut from the variable font",
            "published at https://github.com/google/fonts/tree/main/ofl/nunito. Licence text:",
            "https://openfontlicense.org.", END]

    text = open(NOTICES, encoding="utf-8").read()
    if START in text:
        text = text[:text.index(START)] + "\n".join(out) + text[text.index(END) + len(END):]
    else:
        cut = text.index("## Picture artwork")
        text = text[:cut] + "\n".join(out) + "\n"
    open(NOTICES, "w", encoding="utf-8").write(text)
    print(f"{sum(len(v) for v in kenney.values())} Kenney, {len(seen)} clipart drawings, {len(icons)} icons, {len(drawn)} drawn")


if __name__ == "__main__":
    main()
