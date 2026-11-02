#!/usr/bin/env python3
"""S3.6.3 deterministic, illustrated, alpha-safe WebP assets for the space shooter.
Run: python -m pip install Pillow ; python scripts/generate_space_premium_art.py
No private assets, network, or runtime Pillow dependency on Android.
"""
from PIL import Image, ImageDraw, ImageFilter, ImageFont
from pathlib import Path
from math import sin, cos, pi
import random

OUT=Path("app/src/main/res/drawable-nodpi")
OUT.mkdir(parents=True,exist_ok=True)
SEED=20261102
W,H=640,1050
WORLD=[
 ("terra", (3,16,47),(2,105,115),(60,218,153),(163,244,128)),
 ("aqua", (1,17,52),(13,78,145),(67,201,243),(186,245,254)),
 ("pyra", (27,5,39),(115,21,64),(255,106,57),(255,217,105)),
 ("luna", (7,10,37),(38,49,105),(175,188,244),(233,241,255)),
 ("nova", (19,3,48),(73,25,129),(185,74,255),(251,185,255))
]

def mix(a,b,t):return tuple(int(a[i]*(1-t)+b[i]*t) for i in range(3))
def rgb(c,alpha=255):return tuple(c)+(alpha,)
def glow(img,x,y,r,col,alpha=190):
    lay=Image.new("RGBA",img.size,(0,0,0,0))
    d=ImageDraw.Draw(lay)
    for j in range(8,0,-1):
        radius=r*(.42+j*.12)
        strength=int(alpha*(1-j/9)**2)
        d.ellipse((x-radius,y-radius,x+radius,y+radius),fill=rgb(col,strength))
    img.alpha_composite(lay)
def save(img,name,quality=82):
    path=OUT/("space_"+name+"_premium.webp")
    img.save(path,"WEBP",quality=quality,method=6)
    print(str(path),path.stat().st_size)

for idx,(name,top,bottom,accent,light) in enumerate(WORLD):
    rng=random.Random(SEED+idx*991)
    im=Image.new("RGBA",(W,H),(0,0,0,255))
    px=im.load()
    for y in range(H):
        t=y/(H-1)
        grad=mix(top,bottom,t**.83)
        for x in range(W):
            dx=(x-W*.73)/W
            dy=(y-H*.31)/H
            haze=max(0,1-(dx*dx*2.6+dy*dy*3.9))**2
            orbital=sin(x*.009+y*.004+idx*2)*.018
            px[x,y]=rgb(mix(grad,accent,min(.20,haze*.19+max(0,orbital))))
    d=ImageDraw.Draw(im,"RGBA")
    # Deep luminous nebula swirls behind all gameplay content.
    for i in range(15):
        x=rng.randint(-200,W+200);y=rng.randint(-130,880);r=rng.randint(70,205)
        glow(im,x,y,r,accent,alpha=rng.randint(22,65))
    d=ImageDraw.Draw(im,"RGBA")
    for i in range(180):
        x=rng.randint(1,W-2);y=rng.randint(1,H-2)
        rr=rng.choice([1,1,1,2,2,3])
        d.ellipse((x-rr,y-rr,x+rr,y+rr),fill=rgb(light,rng.randrange(80,220)))
        if rr==3:
            d.line((x-6,y,x+6,y),fill=rgb(light,100),width=1)
            d.line((x,y-6,x,y+6),fill=rgb(light,95),width=1)
    # Distinctive themed orbit planet + scenic lower strip. Keep center clear for math.
    planetX,planetY=(W*.82,H*.22);planetR=(W*.22 if idx!=3 else W*.18)
    glow(im,planetX,planetY,planetR*2.0,accent,alpha=125)
    d=ImageDraw.Draw(im,"RGBA")
    d.ellipse((planetX-planetR,planetY-planetR,planetX+planetR,planetY+planetR),
              fill=rgb(mix(top,accent,.62)),outline=rgb(light,170),width=3)
    for j in range(11):
        ry=planetY-planetR+j*planetR*.18
        xx=planetR*(1-abs(ry-planetY)/planetR)**.5
        d.arc((planetX-xx,ry-10,planetX+xx,ry+10),0,180,fill=rgb(light,60),width=3)
    d.ellipse((planetX-planetR*.65,planetY-planetR*.65,planetX-planetR*.35,planetY-planetR*.35),
              fill=rgb(light,62))
    # Scenic silhouette on bottom, designed not to compete with the HUD.
    if idx==0: # green waterfall floating islands
        for j in range(8):
            x=j*105-55;y=840+(j%3)*55;r=65+rng.randrange(20)
            d.ellipse((x-r,y-r*.5,x+r,y+r*.38),fill=rgb((17,83,77),245))
            d.ellipse((x-r*.86,y-r*.54,x+r*.86,y-r*.16),fill=rgb(accent,220))
            for k in range(3):
                xx=x+(k-1)*23
                d.line((xx,y-r*.10,xx-7,y+130),fill=rgb(light,95),width=3)
        for i in range(70):
            x=rng.randrange(W);y=rng.randrange(855,1040)
            d.ellipse((x,y,x+4,y+4),fill=rgb(light,160))
    elif idx==1: # ice shelf
        for j in range(13):
            x=j*55-40;height=rng.randrange(110,245)
            y=H-rng.randrange(20,65)
            poly=[(x-36,y),(x,y-height),(x+37,y),(x+22,H),(x-22,H)]
            d.polygon(poly,fill=rgb(mix(accent,light,j/19),210),outline=rgb(light,205),width=2)
            d.line((x,y-height,x-16,y-20),fill=rgb(light,170),width=2)
    elif idx==2: # fire volcano ridges
        for j in range(6):
            x=j*150-65;peak=H-rng.randrange(250,390)
            d.polygon([(x-150,H),(x,peak),(x+125,H)],
                fill=rgb((62,21,50),235),outline=rgb(accent,120),width=3)
            d.line([(x,peak+30),(x+12,peak+110),(x-17,peak+210),(x+20,H)],
                fill=rgb(light,145),width=8)
        for j in range(20):
            x=rng.randrange(W);y=rng.randrange(670,H)
            d.ellipse((x,y,x+3,y+8),fill=rgb(light,190))
    elif idx==3: # lunar craters / scientific towers
        d.polygon([(0,970),(80,900),(180,930),(300,870),(430,950),(W,865),(W,H),(0,H)],
                  fill=rgb((63,77,120),245))
        for j in range(10):
            x=rng.randrange(W);y=rng.randrange(900,1050);rad=rng.randrange(15,55)
            d.ellipse((x-rad,y-rad*.3,x+rad,y+rad*.3),
                outline=rgb(light,95),width=4)
        for x in [120,350,510]:
            d.rectangle((x-9,806,x+9,925),fill=rgb(light,150))
            d.ellipse((x-30,790,x+30,820),fill=rgb(light,150))
            d.line((x,800,x,755),fill=rgb(light,135),width=3)
    else: # purple crystal islands / swirling portal
        for j in range(26):
            x=rng.randrange(-30,W+30);base=rng.randrange(850,1130)
            height=rng.randrange(65,245);wide=rng.randrange(13,42)
            d.polygon([(x-wide,base),(x-wide*.6,base-height*.5),
                       (x,base-height),(x+wide,base-height*.46),
                       (x+wide,base)],fill=rgb(mix(accent,light,rng.random()*.7),210),
                       outline=rgb(light,155),width=2)
        glow(im,310,875,150,accent,145)
    # Darken visual floor to improve player visibility; no text/UI baked into art.
    shade=Image.new("RGBA",im.size,(0,0,0,0));sd=ImageDraw.Draw(shade)
    for y in range(730,H):
        alpha=int((y-730)/(H-730)*67)
        sd.line((0,y,W,y),fill=(2,5,28,alpha))
    im=Image.alpha_composite(im,shade).convert("RGB")
    save(im,"bg_"+name,quality=77)

def canvas(n=320):return Image.new("RGBA",(n,n),(0,0,0,0))
# Ship facing upward, with crisp cyan plating, orange side accents and strong silhouette.
n=320;ship=canvas(n);d=ImageDraw.Draw(ship,"RGBA")
glow(ship,160,153,78,(70,220,253),95)
d=ImageDraw.Draw(ship,"RGBA")
d.polygon([(160,21),(198,122),(255,189),(228,215),(205,192),
           (197,246),(160,217),(123,246),(115,192),(92,215),
           (65,189),(122,122)],fill=(230,247,255,255),outline=(36,120,192,255),width=7)
d.polygon([(160,35),(177,126),(160,205),(142,127)],fill=(41,105,169,255))
d.ellipse((139,92,181,152),fill=(18,55,114,255),outline=(115,245,255,255),width=5)
d.polygon([(65,189),(114,152),(114,187),(90,208)],fill=(251,134,52,255))
d.polygon([(255,189),(206,152),(206,187),(230,208)],fill=(251,134,52,255))
for x in (120,200):
    d.ellipse((x-17,212,x+17,250),fill=(26,63,120,255),outline=(159,229,251,255),width=5)
    d.polygon([(x-13,245),(x,303),(x+13,245)],fill=(68,231,253,200))
    d.polygon([(x-8,243),(x,288),(x+8,243)],fill=(255,187,61,245))
save(ship,"ship",quality=92)

# Four visibly different meteor types. Transparent, individually shaded.
for variant in range(4):
    rng=random.Random(SEED+411+variant)
    im=canvas();d=ImageDraw.Draw(im,"RGBA")
    tint=[(142,176,226),(227,121,77),(151,104,245),(76,191,173)][variant]
    glow(im,160,162,105,tint,70);d=ImageDraw.Draw(im,"RGBA")
    polygon=[]
    for j in range(12):
        ang=2*pi*j/12
        rad=104+rng.randrange(-14,15)
        polygon.append((160+cos(ang)*rad,160+sin(ang)*rad))
    d.polygon(polygon,fill=rgb(mix(tint,(30,38,75),.39)),outline=rgb(tint),width=9)
    for k in range(11):
        x=rng.randrange(80,235);y=rng.randrange(80,236);ra=rng.randrange(8,20)
        d.ellipse((x-ra,y-ra,x+ra,y+ra),fill=rgb(mix(tint,(23,26,61),.58),210),
                  outline=rgb(tint,130),width=2)
    for j in range(5):
        x=rng.randrange(80,220);y=rng.randrange(80,210)
        d.line((x,y,x+20,y-8),fill=rgb((246,245,255),120),width=3)
    save(im,"meteor_"+str(variant+1),quality=88)

# Beam, glow and burst; alpha-based FX.
fx=Image.new("RGBA",(90,300),(0,0,0,0));fd=ImageDraw.Draw(fx,"RGBA")
for width,color in [(40,(25,185,252,25)),(23,(48,232,252,92)),
                    (12,(113,242,255,200)),(5,(251,255,255,255))]:
    fd.rounded_rectangle((45-width//2,13,45+width//2,274),radius=width//2,fill=color)
fd.ellipse((20,0,70,46),fill=(221,255,255,230))
save(fx,"laser",quality=90)
burst=canvas();bd=ImageDraw.Draw(burst,"RGBA")
glow(burst,160,160,90,(255,170,68),170);bd=ImageDraw.Draw(burst,"RGBA")
for i in range(12):
    a=i*2*pi/12
    x1=160+cos(a)*52;y1=160+sin(a)*52
    x2=160+cos(a)*(120+(i%3)*20);y2=160+sin(a)*(120+(i%3)*20)
    bd.line((x1,y1,x2,y2),fill=(255,210,92,210),width=8)
    bd.ellipse((x2-5,y2-5,x2+5,y2+5),fill=(255,249,211,220))
bd.ellipse((102,102,218,218),fill=(255,191,77,210),outline=(255,255,218,225),width=9)
save(burst,"impact",quality=87)

assert all((OUT/("space_"+name+"_premium.webp")).exists() for name in
 ["bg_terra","bg_aqua","bg_pyra","bg_luna","bg_nova",
  "ship","meteor_1","meteor_2","meteor_3","meteor_4","laser","impact"])
