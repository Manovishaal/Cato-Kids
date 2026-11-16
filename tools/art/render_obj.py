"""
Renders a Kenney OBJ model (CC0) to a transparent PNG.

Kenney ships 64px previews only, which look soft on a phone, so the app's pictures are
rendered here from the original models instead: a small z-buffer rasteriser with soft
two-light shading, supersampled for clean edges. Kenney models colour their faces from a
flat palette texture, so one texture lookup per face reproduces their look exactly.

    python render_obj.py model.obj out.png [size]
"""
import os
import sys

import numpy as np
from PIL import Image


def load_obj(path):
    verts, uvs, faces = [], [], []          # faces: (vertex idx x3, uv idx x3, material)
    mats, mtl, cur = {}, None, None
    base = os.path.dirname(path)
    for line in open(path, encoding="utf-8", errors="ignore"):
        p = line.split()
        if not p:
            continue
        if p[0] == "mtllib":
            mtl = os.path.join(base, " ".join(p[1:]))
        elif p[0] == "usemtl":
            cur = " ".join(p[1:])
        elif p[0] == "v":
            verts.append([float(x) for x in p[1:4]])
        elif p[0] == "vt":
            uvs.append([float(x) for x in p[1:3]])
        elif p[0] == "f":
            idx = [q.split("/") for q in p[1:]]
            vi = [int(q[0]) - 1 for q in idx]
            ti = [int(q[1]) - 1 if len(q) > 1 and q[1] else -1 for q in idx]
            for k in range(1, len(vi) - 1):    # fan-triangulate
                faces.append((vi[0], vi[k], vi[k + 1], ti[0], ti[k], ti[k + 1], cur))
    if mtl and os.path.exists(mtl):
        name = None
        for line in open(mtl, encoding="utf-8", errors="ignore"):
            p = line.split()
            if not p:
                continue
            if p[0] == "newmtl":
                name = " ".join(p[1:])
                mats[name] = {"kd": (0.8, 0.8, 0.8), "map": None}
            elif p[0] == "Kd" and name:
                mats[name]["kd"] = tuple(float(x) for x in p[1:4])
            elif p[0] == "map_Kd" and name:
                tex = os.path.join(base, " ".join(p[1:]))
                if not os.path.exists(tex):
                    alt = os.path.join(base, "Textures", os.path.basename(tex))
                    tex = alt if os.path.exists(alt) else tex
                mats[name]["map"] = tex
    return np.array(verts, float), np.array(uvs, float) if uvs else np.zeros((1, 2)), faces, mats


_tex_cache = {}


def face_colours(verts, uvs, faces, mats):
    cols = np.zeros((len(faces), 3))
    for i, f in enumerate(faces):
        m = mats.get(f[6], {"kd": (0.8, 0.8, 0.8), "map": None})
        c = np.array(m["kd"])
        if m["map"] and os.path.exists(m["map"]) and f[3] >= 0:
            tex = _tex_cache.get(m["map"])
            if tex is None:
                tex = _tex_cache[m["map"]] = np.asarray(Image.open(m["map"]).convert("RGB"), float) / 255
            uv = uvs[[f[3], f[4], f[5]]].mean(axis=0)
            h, w = tex.shape[:2]
            x = int(np.clip((uv[0] % 1.0) * w, 0, w - 1))
            y = int(np.clip((1 - uv[1] % 1.0) * h, 0, h - 1))
            c = tex[y, x] * (np.array(m["kd"]) if max(m["kd"]) > 0 else 1)
        cols[i] = c
    return cols


def render(path, out, size=384, yaw=-38, pitch=28, ss=3):
    verts, uvs, faces, mats = load_obj(path)
    cols = face_colours(verts, uvs, faces, mats)
    tri = np.array([f[:3] for f in faces])

    # Camera: turn the model a little and look down at it, like a toy on a table.
    a, b = np.radians(yaw), np.radians(pitch)
    ry = np.array([[np.cos(a), 0, np.sin(a)], [0, 1, 0], [-np.sin(a), 0, np.cos(a)]])
    rx = np.array([[1, 0, 0], [0, np.cos(b), -np.sin(b)], [0, np.sin(b), np.cos(b)]])
    v = verts @ ry.T @ rx.T

    # Normals in camera space, for shading.
    p0, p1, p2 = v[tri[:, 0]], v[tri[:, 1]], v[tri[:, 2]]
    n = np.cross(p1 - p0, p2 - p0)
    n /= np.linalg.norm(n, axis=1, keepdims=True) + 1e-12
    key = np.array([-0.45, 0.75, 0.5]); key /= np.linalg.norm(key)
    fill = np.array([0.6, 0.2, 0.75]); fill /= np.linalg.norm(fill)
    shade = 0.58 + 0.42 * np.clip(n @ key, 0, 1) + 0.12 * np.clip(n @ fill, 0, 1)
    shade = np.clip(shade, 0, 1.08)
    rgb = np.clip(cols * shade[:, None], 0, 1)

    # Fit to the canvas with a margin.
    W = size * ss
    lo, hi = v[:, :2].min(0), v[:, :2].max(0)
    scale = (W * 0.86) / max(hi - lo)
    centre = (lo + hi) / 2
    sx = (v[:, 0] - centre[0]) * scale + W / 2
    sy = W / 2 - (v[:, 1] - centre[1]) * scale
    sz = v[:, 2]

    img = np.zeros((W, W, 3)); alpha = np.zeros((W, W)); zbuf = np.full((W, W), -np.inf)
    for t in range(len(tri)):
        i0, i1, i2 = tri[t]
        x = np.array([sx[i0], sx[i1], sx[i2]]); y = np.array([sy[i0], sy[i1], sy[i2]])
        z = np.array([sz[i0], sz[i1], sz[i2]])
        x0, x1 = int(max(np.floor(x.min()), 0)), int(min(np.ceil(x.max()), W - 1))
        y0, y1 = int(max(np.floor(y.min()), 0)), int(min(np.ceil(y.max()), W - 1))
        if x1 < x0 or y1 < y0:
            continue
        den = (y[1] - y[2]) * (x[0] - x[2]) + (x[2] - x[1]) * (y[0] - y[2])
        if abs(den) < 1e-9:
            continue
        gx, gy = np.meshgrid(np.arange(x0, x1 + 1) + 0.5, np.arange(y0, y1 + 1) + 0.5)
        w0 = ((y[1] - y[2]) * (gx - x[2]) + (x[2] - x[1]) * (gy - y[2])) / den
        w1 = ((y[2] - y[0]) * (gx - x[2]) + (x[0] - x[2]) * (gy - y[2])) / den
        w2 = 1 - w0 - w1
        inside = (w0 >= -1e-6) & (w1 >= -1e-6) & (w2 >= -1e-6)
        if not inside.any():
            continue
        depth = w0 * z[0] + w1 * z[1] + w2 * z[2]
        zb = zbuf[y0:y1 + 1, x0:x1 + 1]
        win = inside & (depth > zb)          # camera looks down -z; larger z is nearer
        zb[win] = depth[win]
        img[y0:y1 + 1, x0:x1 + 1][win] = rgb[t]
        alpha[y0:y1 + 1, x0:x1 + 1][win] = 1

    rgba = np.dstack([img, alpha])
    im = Image.fromarray((rgba * 255).astype(np.uint8), "RGBA").resize((size, size), Image.LANCZOS)
    im.save(out)


if __name__ == "__main__":
    render(sys.argv[1], sys.argv[2], int(sys.argv[3]) if len(sys.argv) > 3 else 384)
