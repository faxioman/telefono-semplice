"""Copia gli screenshot generati da StoreAssetsTest nella scheda Play (app/src/play/play)."""
import glob, os, re, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SNAP = os.path.join(ROOT, "app/src/test/snapshots/images")
PLAY = os.path.join(ROOT, "app/src/play/play/listings")


# Paparazzi mette i nomi in minuscolo: "de-de" -> "de-DE" usando le cartelle della scheda.
LOCALES = {d.lower(): d for d in os.listdir(PLAY)}


def dest(kind, locale, name):
    d = os.path.join(PLAY, LOCALES[locale.lower()], "graphics", kind)
    os.makedirs(d, exist_ok=True)
    return os.path.join(d, name)


n = 0
for f in sorted(glob.glob(os.path.join(SNAP, "*StoreAssetsTest*.png"))):
    base = os.path.basename(f)
    if m := re.search(r"phone-(.+)-(\d)\.png$", base):
        shutil.copy(f, dest("phone-screenshots", m[1], f"{m[2]}.png"))
    elif m := re.search(r"feature-(.+)\.png$", base):
        shutil.copy(f, dest("feature-graphic", m[1], "1.png"))
    elif base.endswith("_icon.png"):
        shutil.copy(f, dest("icon", "it-it", "1.png"))  # l'icona è unica: lingua predefinita
    else:
        continue
    n += 1
print(f"copiati {n} file")
