"""
Where every picture in Cato Kids comes from.

Each emoji the app used as artwork maps to one of:
  ("icon",  name)      a Material Symbols icon (Apache 2.0) — for UI symbols
  ("draw",  spec)      drawn by the app itself — colour swatches and geometric shapes
  ("kenney", "kit/file") a Kenney preview render (CC0, https://kenney.nl)
  ("clip",  query)     still needs an individually vetted public-domain illustration
  ("text",  chars)     genuinely text (letters/numbers) — rendered as type, not art

Run `python3 tools/art/mapping.py <kenney_dir>` to check every Kenney file exists.
"""

K = "kenney"
MAP = {
    # ---------------- UI symbols -> Material icons
    "⭐": ("icon", "star"), "🌟": ("icon", "star"), "✨": ("icon", "auto_awesome"),
    "✅": ("icon", "check_circle"), "✕": ("icon", "close"), "❓": ("icon", "help"), "➕": ("icon", "add"),
    "−": ("icon", "remove"), "▶": ("icon", "play_arrow"), "◀": ("icon", "arrow_back"), "▸": ("icon", "chevron_right"),
    "➡": ("icon", "arrow_forward"), "↩": ("icon", "undo"), "↔": ("icon", "swap_horiz"), "🔁": ("icon", "replay"),
    "🔊": ("icon", "volume_up"), "🔒": ("icon", "lock"), "🪙": ("icon", "paid"), "🏆": ("icon", "emoji_events"),
    "🏅": ("icon", "military_tech"), "🎯": ("icon", "track_changes"), "⏱": ("icon", "timer"), "⏰": ("icon", "alarm"),
    "⏳": ("icon", "hourglass_bottom"), "⚠": ("icon", "warning"), "📬": ("icon", "mark_email_unread"),
    "📭": ("icon", "drafts"), "📝": ("icon", "edit_note"), "🔢": ("icon", "pin"), "🔤": ("icon", "abc"),
    "🔡": ("icon", "abc"), "🔟": ("text", "10"), "⃣": ("text", ""), "🔍": ("icon", "search"), "🗂": ("icon", "folder"),
    "🗑": ("icon", "delete"), "📅": ("icon", "calendar_month"), "🔗": ("icon", "link"), "🔔": ("icon", "notifications"),
    "📴": ("icon", "phone_disabled"), "📞": ("icon", "call"), "📸": ("icon", "photo_camera"), "🎮": ("icon", "sports_esports"),
    "🧩": ("icon", "extension"), "🃏": ("icon", "style"), "🛍": ("icon", "shopping_bag"), "♻": ("icon", "recycling"),
    "🚫": ("icon", "block"), "🛑": ("icon", "do_not_disturb_on"), "💭": ("icon", "chat_bubble"), "🎵": ("icon", "music_note"),
    "🎉": ("icon", "celebration"), "💡": ("icon", "lightbulb"), "🗺": ("icon", "map"), "🔥": ("icon", "local_fire_department"),
    "🌀": ("icon", "cyclone"), "🏁": ("icon", "sports_score"), "🗣": ("icon", "record_voice_over"), "🎨": ("icon", "palette"),
    "❤": ("icon", "favorite"), "💕": ("icon", "favorite"), "💖": ("icon", "favorite"), "💗": ("icon", "favorite"),
    "💙": ("icon", "favorite"), "💚": ("icon", "favorite"), "💛": ("icon", "favorite"), "💜": ("icon", "favorite"),
    "🧡": ("icon", "favorite"), "👉": ("icon", "touch_app"), "👆": ("icon", "touch_app"), "☝": ("icon", "touch_app"),
    "🚰": ("icon", "water_drop"), "🛠": ("icon", "build"), "📺": ("kenney", "furniture-kit/televisionVintage"),

    # ---------------- colours and shapes -> drawn by the app
    "🔵": ("draw", "circle:blue"), "🔴": ("draw", "circle:red"), "🟡": ("draw", "circle:yellow"),
    "🟢": ("draw", "circle:green"), "🟠": ("draw", "circle:orange"), "🟣": ("draw", "circle:purple"),
    "🟤": ("draw", "circle:brown"), "⚪": ("draw", "circle:white"), "⚫": ("draw", "circle:black"),
    "○": ("draw", "circle:white"), "●": ("draw", "circle:black"),
    "🟥": ("draw", "square:red"), "🟧": ("draw", "square:orange"), "🟨": ("draw", "square:yellow"),
    "🟩": ("draw", "square:green"), "🟪": ("draw", "square:purple"),
    "🔺": ("draw", "triangle:red"), "🔷": ("draw", "diamond:blue"), "▭": ("draw", "rectangle:blue"),
    "⬭": ("draw", "oval:green"), "⬟": ("draw", "pentagon:purple"),

    # ---------------- animals (Kenney Cube Pets + kits)
    "🐱": (K, "cube-pets/animal-cat"), "🐶": (K, "cube-pets/animal-dog"), "🐘": (K, "cube-pets/animal-elephant"),
    "🦁": (K, "cube-pets/animal-lion"), "🐝": (K, "cube-pets/animal-bee"), "🐷": (K, "cube-pets/animal-pig"),
    "🦒": (K, "cube-pets/animal-giraffe"), "🦜": (K, "cube-pets/animal-parrot"), "🐰": (K, "cube-pets/animal-bunny"),
    "🐄": (K, "cube-pets/animal-cow"), "🐵": (K, "cube-pets/animal-monkey"), "🐤": (K, "cube-pets/animal-chick"),
    "🐣": (K, "cube-pets/animal-chick"), "🐅": (K, "cube-pets/animal-tiger"), "🐟": (K, "cube-pets/animal-fish"),
    "🐻": (K, "cube-pets/animal-polar"), "🐼": (K, "cube-pets/animal-panda"),
    "🧸": (K, "furniture-kit/bear"),

    # ---------------- food (Kenney Food Kit)
    "🍎": (K, "food-kit/apple"), "🍓": (K, "food-kit/strawberry"), "🥕": (K, "food-kit/carrot"),
    "🍇": (K, "food-kit/grapes"), "🍊": (K, "food-kit/orange"), "🥔": (K, "food-kit/potato"),
    "🍌": (K, "food-kit/banana"), "🥭": (K, "food-kit/mango"), "🧅": (K, "food-kit/onion"),
    "🍅": (K, "food-kit/tomato"), "🍉": (K, "food-kit/watermelon"), "🍐": (K, "food-kit/pear"),
    "🧄": (K, "food-kit/garlic"), "🥚": (K, "food-kit/egg"), "🥬": (K, "food-kit/cabbage"),
    "🍄": (K, "food-kit/mushroom"), "🍍": (K, "food-kit/pineapple"), "🍦": (K, "food-kit/ice-cream"),
    "🍋": (K, "food-kit/lemon"), "🍯": (K, "food-kit/honey"), "🍬": (K, "food-kit/lollypop"),
    "🍪": (K, "food-kit/cookie"), "🍽": (K, "food-kit/plate-dinner"), "☕": (K, "food-kit/cup-tea"),
    "🥤": (K, "food-kit/soda-glass"), "🥄": (K, "food-kit/utensil-spoon"), "🥗": (K, "food-kit/salad"),
    "🥜": (K, "food-kit/peanut-butter"), "🥥": (K, "food-kit/coconut"),

    # ---------------- vehicles
    "🚌": (K, "car-kit/van"), "🚗": (K, "car-kit/sedan"), "🚐": (K, "car-kit/van"), "🚙": (K, "car-kit/suv"),
    "🚚": (K, "car-kit/delivery"), "🚜": (K, "car-kit/tractor"), "🚢": (K, "watercraft-kit/ship-ocean-liner"),
    "⛵": (K, "watercraft-kit/boat-sail-a"), "🚀": (K, "space-kit/rocket_baseA"),

    # ---------------- nature, places and things
    "🌳": (K, "nature-kit/tree_default"), "🌸": (K, "nature-kit/flower_redA"), "🌻": (K, "nature-kit/flower_yellowA"),
    "🌼": (K, "nature-kit/flower_yellowB"), "🌱": (K, "nature-kit/plant_bush"), "🌿": (K, "nature-kit/grass_large"),
    "🍃": (K, "nature-kit/plant_bushSmall"), "🍂": (K, "nature-kit/plant_bushDetailed"), "🪨": (K, "nature-kit/rock_largeA"),
    "🪵": (K, "nature-kit/log"), "⛺": (K, "nature-kit/tent_detailedOpen"), "⛰": (K, "hexagon-kit/stone-mountain"),
    "🏜": (K, "hexagon-kit/sand-desert"), "🏝": (K, "hexagon-kit/water-island"), "🏠": (K, "city-kit-suburban/building-type-a"),
    "🏫": (K, "city-kit-commercial/building-a"), "🏢": (K, "city-kit-commercial/building-skyscraper-a"),
    "🏥": (K, "city-kit-commercial/building-b"), "🌇": (K, "city-kit-commercial/building-c"),
    "🛏": (K, "furniture-kit/bedSingle"), "🪑": (K, "furniture-kit/chair"), "📚": (K, "furniture-kit/books"),
    "📖": (K, "furniture-kit/books"), "📕": (K, "furniture-kit/books"), "📘": (K, "furniture-kit/books"),
    "📗": (K, "furniture-kit/books"), "📦": (K, "furniture-kit/cardboardBoxClosed"), "🧺": (K, "mini-shop/shopping-basket"),
    "🚦": (K, "retro-urban-kit/detail-light-traffic"), "🛡": (K, "mini-characters/shield-round"),
    "🪐": (K, "space-kit/planet"), "🌍": (K, "space-kit/planet"), "🧪": (K, "space-kit/barrel"),

    # ---------------- people (Kenney Mini Characters)
    "👧": (K, "mini-characters/character-female-a"), "👩": (K, "mini-characters/character-female-b"),
    "👨": (K, "mini-characters/character-male-a"), "👦": (K, "mini-characters/character-male-b"),
    "👴": (K, "mini-characters/character-male-e"), "👵": (K, "mini-characters/character-female-e"),
    "🧑": (K, "mini-characters/character-male-c"), "🧒": (K, "mini-characters/character-female-c"),
    "👨‍🌾": (K, "mini-characters/character-worker"), "👩‍⚕": (K, "mini-characters/character-female-d"),
    "👩‍🍳": (K, "mini-characters/character-female-f"), "👩‍🏫": (K, "mini-characters/character-female-c"),
    "👮": (K, "mini-characters/character-soldier"), "👨‍🚒": (K, "mini-characters/character-worker"),
    "👨‍✈": (K, "mini-characters/character-male-d"), "🧑‍🎨": (K, "mini-characters/character-female-a"),
    "👸": (K, "mini-characters/character-knight"), "🦸": (K, "mini-characters/character-knight"),
    "🧙": (K, "mini-characters/character-robot"), "🏃": (K, "mini-characters/character-male-f"),
    "🕶": (K, "mini-characters/aid-sunglasses"), "👓": (K, "mini-characters/aid-glasses"),
}

# ---------------- animals: one flat, round style for every animal picture (Kenney Animal Pack, CC0)
A = "animal-pack/"
MAP.update({
    "🐱": (K, "cube-pets/animal-cat"),   # the animal pack has no cat; Cube Pets cat renders in a close round style
    "🐶": (K, A + "dog"), "🐘": (K, A + "elephant"), "🐷": (K, A + "pig"), "🦒": (K, A + "giraffe"),
    "🦜": (K, A + "parrot"), "🐰": (K, A + "rabbit"), "🐄": (K, A + "cow"), "🐵": (K, A + "monkey"),
    "🐤": (K, A + "chick"), "🐣": (K, A + "chick"), "🐔": (K, A + "chicken"), "🦆": (K, A + "duck"),
    "🦓": (K, A + "zebra"), "🦉": (K, A + "owl"), "🐸": (K, A + "frog"), "🐐": (K, A + "goat"),
    "🐴": (K, A + "horse"), "🦏": (K, A + "rhino"), "🐻": (K, A + "bear"), "🐳": (K, A + "whale"),
    "🐂": (K, A + "buffalo"), "🐼": (K, A + "panda"),
    # faces and feelings (Kenney Emotes, CC0)
    "😊": (K, "emotes/emote_faceHappy"), "🙂": (K, "emotes/emote_faceHappy"), "😋": (K, "emotes/emote_faceHappy"),
    "😢": (K, "emotes/emote_faceSad"), "😴": (K, "emotes/emote_sleep"), "🤔": (K, "emotes/emote_question"),
    "😇": (K, "emotes/emote_star"), "🙅": (K, "emotes/emote_cross"), "🤥": (K, "emotes/emote_faceAngry"),
    # small objects (Kenney Board Game Icons, CC0)
    "👑": (K, "board-icons/crown_a"), "✋": (K, "board-icons/hand"), "🖐": (K, "board-icons/hand"),
    "👋": (K, "board-icons/hand"), "🔑": (K, "board-icons/lock_open"),
})

# ---------------- the rest: individually picked public-domain clipart (FreeSVG/OpenClipart, CC0).
# These also replace the few Kenney files that don't exist (potato, mango, garlic, planets).
import csv as _csv, os as _os
CLIP = {r["glyph"]: r for r in _csv.DictReader(open(_os.path.join(_os.path.dirname(__file__), "clipart.csv"), encoding="utf-8"))}
MAP.update({g: ("clip", r["dl"]) for g, r in CLIP.items()})
MAP.update({
    "🕒": ("draw", "clock:3"), "🕕": ("draw", "clock:6"),
    "💇": ("icon", "face"), "💁": ("icon", "face_3"), "🧠": ("icon", "psychology"), "🤷": ("icon", "help"),
    "😴": ("icon", "bedtime"), "🤔": ("icon", "help"), "😇": ("icon", "face_retouching_natural"), "🙅": ("icon", "block"),
    "✋": ("icon", "back_hand"), "🖐": ("icon", "back_hand"), "👋": ("icon", "waving_hand"), "🔑": ("icon", "key"),
    "🤏": ("icon", "pinch"), "🖖": ("icon", "back_hand"), "🤟": ("icon", "back_hand"),
})

if __name__ == "__main__":
    import sys, os, csv
    kdir = sys.argv[1]
    missing = [g for g, (t, v) in MAP.items() if t == "kenney" and not os.path.exists(f"{kdir}/{v}.png")]
    print("Kenney files missing:", [(g, MAP[g][1]) for g in missing])
    rows = list(csv.DictReader(open(os.path.join(os.path.dirname(__file__), "ART_MANIFEST.csv"), encoding="utf-8")))
    unmapped = [r["glyph"] + " " + r["unicode_name"] for r in rows if r["glyph"] not in MAP]
    print(len(unmapped), "unmapped:", " | ".join(unmapped))
