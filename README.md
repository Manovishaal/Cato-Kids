# Cato Kids

An Android learning app for Pre-KG, LKG and UKG children, built on the **Cato Kids /
Mother Goose Learning** workbooks. Kotlin + Jetpack Compose + Supabase.

```
Letter Land    →  Reading, Writing & Activities   (English)
Number Land    →  Concepts and Number Fun         (Maths)
Know My World  →  Fun Activities                  (EVS)
```

Each book runs across three levels: **Level 1 = Pre-KG**, **Level 2 = LKG**, **Level 3 = UKG** —
the same structure as the printed books.

---

## What's in the box

| | |
|---|---|
| **107 lessons** | 9 book/level combinations, all bundled in the app |
| **9 game engines** | Trace, Find-them-all, Count-and-tap, Match pairs, Sort it out, Shape hunt, Listen-and-pick, Build the word, Quiz |
| **5 roles** | Student, Teacher, Parent, Administrator, School — each with its own home |
| **Real auth** | Supabase email + password, role stamped at signup, Row Level Security per role |
| **Offline first** | Every lesson plays with no network; progress is written to the device and synced when possible |
| **Bundled artwork** | 205 illustrations, so every child sees the same apple. Charts, confetti, progress rings and backdrops stay Canvas-drawn |

### The five roles

- **Student** — home with "continue where you left off", the three books, progress, badges, games.
- **Teacher** — class list, completion chart by level, "needs a hand" list, per-child report cards.
- **Parent** — each child's progress, plus practical tips for grown-ups.
- **School** — whole-school metrics, classes, completion by level, top performers.
- **Administrator** — platform-wide user counts and the full content library.

Signing in as an **Administrator** also pushes the bundled curriculum up to the
`lessons` table, so the server catalogue always matches the shipped app.

### Explore mode

Every login screen has **"Explore without an account"**. It creates a local profile,
skips the network entirely and saves progress on the device. Useful for demos, for
classrooms with no connectivity, and for trying the app before signing anything up.

---

## Building

Requires **JDK 17** and the **Android SDK** (compileSdk 35). Open the folder in
Android Studio and press Run — or from the command line:

```bash
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease        # signed with the debug key so it installs as-is
./gradlew testDebugUnitTest      # curriculum + scoring tests
./gradlew installDebug           # straight onto a connected device
```

`.github/workflows/android.yml` builds both APKs on every push and uploads them as
artifacts, so you never need a local Android toolchain to get an installable build.

### Backend configuration

Credentials live in `gradle.properties` and are surfaced through `BuildConfig`:

```properties
CATO_SUPABASE_URL=https://<project>.supabase.co
CATO_SUPABASE_ANON_KEY=<publishable key>
```

Override them per-machine in `local.properties` (git-ignored) or per-build with
`-PCATO_SUPABASE_URL=…`. In CI, set the repository secrets `CATO_SUPABASE_URL` and
`CATO_SUPABASE_ANON_KEY`. Leave both blank and the app still builds and runs —
it just starts in explore mode with no sign-in.

---

## Backend

The schema lives in `supabase/migrations/` and is already applied to the connected
project. Apply it elsewhere with `supabase db push`, or paste the files in order into
the SQL editor.

```
schools · profiles · classes · class_students · parent_children
subjects · lessons
lesson_progress · quiz_attempts · assignments · announcements
```

- A trigger on `auth.users` creates the matching `profiles` row and reads `role`,
  `full_name`, `grade` and `phone` out of the signup metadata.
- Row Level Security is on for every table. Access is expressed through
  `SECURITY DEFINER` helpers (`cato_is_admin()`, `cato_teaches()`, `cato_is_child()`,
  `cato_same_school()`, …) so policies can consult a user's role without recursing
  back into `profiles`.
- A student sees only their own rows; a teacher sees the children in classes they
  teach; a parent sees their linked children; a school sees its own school; an
  administrator sees everything.
- `lesson_id` is deliberately **not** a foreign key. The curriculum ships inside the
  app, so a device running a newer build must never fail to save a child's stars just
  because the server catalogue hasn't caught up.
- Supabase's linter flags the nine `cato_*` helpers as executable by signed-in users.
  That is required — RLS policy expressions run as the querying role — and each helper
  only reads rows scoped to `auth.uid()`, so it exposes nothing a user can't already
  read. Anonymous access to them has been revoked.

---

## Project layout

```
app/src/main/java/com/catokids/app/
├── core/            AppContainer (manual DI), Result type, TTS, sound effects
├── data/
│   ├── model/       Role, Grade, Lesson, GameType, GameRound, domain types
│   ├── curriculum/  The whole syllabus as Kotlin: PreKG / LKG / UKG + a small DSL
│   ├── remote/      Supabase client and DTOs
│   ├── local/       DataStore preferences + offline progress mirror
│   └── repository/  Auth, Progress, Roster, CurriculumSync, SampleData
└── ui/
    ├── theme/       Palette sampled from the CatoKidz Figma exports
    ├── components/  Buttons, cards, the Cato mascot, backdrop, progress bars
    ├── navigation/  Routes + the single NavHost
    ├── auth/        Splash, role picker, login, register, forgot password
    ├── student/     Home, subject/topics, rewards
    ├── games/       The nine engines, the game host, the result screen
    └── dashboard/   Teacher, Parent, School, Administrator, reports, profile
```

### Adding a lesson

Everything is data. Open the right file in `data/curriculum/` and add to the block:

```kotlin
lesson(
    id = "colours_2", title = "More colours", subtitle = "Purple and orange",
    game = GameType.SHAPE_HUNT, intro = "Tap the colour you hear.",
    rounds = listOf(
        pickRound("c1", "Tap the purple one", "Purple",
            correct = "Purple" to "🟣",
            distractors = listOf("Red" to "🔴", "Blue" to "🔵", "Green" to "🟢")),
    ),
)
```

`CurriculumTest` will then check it: one correct answer per choice round, no repeated
options, every sorting item lands in a declared basket, counting rounds agree with the
number of pictures shown, and so on. Run `./gradlew testDebugUnitTest` before shipping.

---

## Brand

Two marks, used for two different jobs and never interchangeably:

- **The Cato Kids tree** is the product. It is the launcher icon, the splash, and the
  header on the role picker and sign-in screens — brand moments only, never decoration.
- **Mother Goose Learning** is the curriculum. The full lockup appears on the splash
  beneath the app name and on each subject screen, crediting the books the lessons come
  from. The goose herself, cropped out of the wordmark, is the character who reacts to
  the child: she cheers a right answer, waits patiently on a wrong one, and turns up on
  the results and rewards screens.

`ui/components/Brand.kt` holds all three composables — `CatoKidsLogo`,
`MotherGooseLogo` and `GooseCharacter`. Nothing else should reach for the drawables
directly. Brand colours sampled from the tree live in `CatoPalette` as `BrandBlue`
(#2585BC, the trunk), `BrandTeal`, `BrandPink` and `BrandOrange`.

## Artwork

No emoji font ever reaches a child's screen. Content still names its pictures with emoji
code points (it keeps the curriculum readable), but every key resolves to real artwork,
bundled for offline use. Emoji render differently on every device and are blank boxes on
some older ones; when the picture *is* the question, that is a broken lesson.

- **Illustrations (203):** human-made, free for commercial use, no watermarks.
  - [Kenney](https://kenney.nl) kits, **CC0**. 3D models are rendered to pictures by
    `tools/art/render_obj.py` (Kenney only ships 64px previews); the Animal Pack and
    Board Game Icons come from Kenney's vector files.
  - Individually picked public-domain clipart from [FreeSVG](https://freesvg.org) /
    OpenClipart, **CC0**, only drawings published before 2022 (to avoid AI uploads).
- **UI symbols (86):** Material icons (Apache 2.0), tinted per meaning.
- **Shapes and clocks (23):** drawn in code by `DrawnArt`, so a "rectangle" is real geometry.

`tools/art/build_art.py` rebuilds every picture (`res/drawable-nodpi/art_*.webp`, 320px,
about 12 KB each), regenerates `ArtCatalog.kt` and fills in `tools/art/ART_MANIFEST.csv`
(source, creator and licence per picture); `tools/art/notices.py` then rewrites the credits
in `THIRD_PARTY_NOTICES.md`. `ArtCoverageTest` fails the build if any picture used in
code or the curriculum has no artwork.

| | |
|---|---|
| `EmojiArt(key, size)` | a standalone picture. Splits runs, so `"🍎🍎🍎"` lays out as three apples |
| `EmojiText(text, style)` | a drop-in for `Text` that puts pictures **inline**, sized in `em` |
| `ArtPicture(key)` | one picture filling its bounds |

## Design system

`ui/theme/Tokens.kt` holds the shared measurements: a 4dp spacing grid (`Space`), four
rounded corner sizes (`Radius`), touch targets of 56dp for controls and 72dp for game
pieces (`Touch`). Type is **Nunito** (SIL OFL, bundled in `res/font`). Buttons and
selected states use each role's *deep* colour so white text stays readable.

## Motion

`ui/components/Motion.kt` is the whole vocabulary — nothing animates by hand-rolling
its own spring. Two rules govern it: **nothing moves without meaning** (every effect is
either feedback for something the child just did, or a cue about where to look next),
and **nothing blocks** (effects sit on top of a layout that is already correct and
tappable, so a slow device degrades to a static screen rather than a broken one).

| Piece | What it does | Where it earns its place |
|---|---|---|
| `PopIn` + `stagger` | scale-and-rise entrance, siblings offset 45–80ms | lists deal themselves out instead of appearing at once |
| `bounceOnPress` | squashes to 94% under a finger, springs back | every button, card and answer tile |
| `pulse` | slow breathing scale | the single next thing to tap — CTA, role arrow, untraced letter |
| `floaty` | idle bob, phase-offset per item | game tiles and unlocked badges, so a row never moves in lockstep |
| `hop` | one celebratory jump on a trigger | coins going up, a letter landing in a slot, a found tile |
| `wiggle` | short head-shake | reserved for wrong answers |
| `AnimatedCounter` | rolls a number up instead of snapping | the score, time and coins on the results screen |
| `SparkleBurst` | one-shot ring of sparks, draws nothing at rest | behind the stars on a 2★+ result, behind the goose on a correct answer |
| `BouncingDots` | three dots taking turns | friendlier than a spinner for this audience |

On top of that: screens slide in from the right and back out to the left through
`NavHost` transitions, so a child feels which way they moved; game rounds cross-slide
via `AnimatedContent`, making a finished question feel like turning a page; stars land
one at a time with a spin; and progress bars spring to their value under a travelling
shine.

The backdrop runs eleven translucent shapes — circles, blobs and four-point sparkles —
plus fourteen twinkles, all driven by **one** shared infinite transition and phase
offsets rather than thirty separate animation clocks. Their positions come from a fixed
seed, so the composition is identical every launch and never lands something
distracting behind a heading.

Two deliberate restraints. `StarRow(animate = false)` in scrolling lists, because three
stars springing in on every row is noise rather than delight. And `SparkleBurst` reads
its progress inside the draw lambda, not in composition, so a 700ms burst repaints
without recomposing the subtree around it.

## Design

Colours were sampled directly from the CatoKidz Figma exports — the coral card
gradients, the amber "recommended" banner, the periwinkle teacher surfaces, the mint
and sky accents. Each role gets its own tint through `LocalRoleColors`, so a teacher's
app feels different from a child's without maintaining five themes.

Type is deliberately larger and heavier than Material defaults, and every tap target
clears 48dp. Prompts are read aloud through Android's text-to-speech, because the
users can't read yet — that's the whole point.

---

Cato Kids · Mother Goose Learning
