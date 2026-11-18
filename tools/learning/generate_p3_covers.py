#!/usr/bin/env python3
"""P3 art atlas: six original, text-free educational math covers (Apache/Pillow).
Deterministic local render; no paid design API, third-party images or network calls.
Areas in exact TopicArea order: NUMBERS, OPERATIONS, GEOMETRY, DATA, ALGEBRA, REVIEW.
"""
from pathlib import Path
from math import cos, sin, pi
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import random

W,H=240,148
PALETTES=[
 ((31,104,211),(8,39,111),(95,243,245)),
 ((39,108,203),(20,48,124),(255,207,111)),
 ((85,85,207),(42,49,121),(192,164,255)),
 ((9,152,152),(15,73,112),(145,245,192)),
 ((174,81,151),(66,44,119),(255,201,140)),
 ((66,118,185),(32,65,129),(255,212,130)),
]
def font(n):
    for name in ("DejaVuSans-Bold.ttf", "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
                 "C:/Windows/Fonts/arialbd.ttf"):
        try: return ImageFont.truetype(name,n)
        except OSError: pass
    return ImageFont.load_default()
def rr(d,b,r,fill,outline=None,width=1):
    d.rounded_rectangle(b,r,fill=fill,outline=outline,width=width)
def stroke(d,pts,fill,width=3):
    d.line(pts,fill=fill,width=width,joint="curve")
def artwork(index):
    rand=random.Random(20261118+index)
    a,b,accent=PALETTES[index]
    img=Image.new("RGBA",(W,H))
    p=img.load()
    for y in range(H):
        t=y/(H-1);v=t*t*.65+t*.35
        c=tuple(round(a[k]*(1-v)+b[k]*v) for k in range(3))+(255,)
        for x in range(W):p[x,y]=c
    glow=Image.new("RGBA",(W,H));gd=ImageDraw.Draw(glow)
    gd.ellipse((150,-5,234,79),fill=accent+(100,))
    gd.ellipse((-9,84,67,154),fill=(80,230,248,80))
    img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(12)))
    d=ImageDraw.Draw(img)
    for _ in range(14):
        x=rand.randrange(W);y=rand.randrange(H-10)
        d.ellipse((x-1,y-1,x+1,y+1),fill=(238,250,255,rand.randrange(50,140)))
    d.ellipse((-40,115,90,170),fill=(27,120,191,85))
    d.ellipse((145,119,280,186),fill=(25,110,177,75))
    rr(d,(18,113,222,141),13,(9,28,76,135),(160,225,255,50),1)
    if index==0:
        stroke(d,[(29,98),(215,98)],(164,236,255,220),3)
        for n in range(6):
            x=41+n*31
            stroke(d,[(x,93),(x,103)],(230,248,255),2)
            if n%2==0:
                d.ellipse((x-8,64,x+8,81),fill=(247,205,96),outline=(255,249,219),width=2)
            d.text((x,115),str(n+1),font=font(13),anchor="mm",fill=(255,255,245))
        for i in range(4):
            x=62+i*30;y=17+i*7
            rr(d,(x-8,y,x+18,y+28),7,[(112,231,244),(255,204,92),(255,147,177),(180,158,255)][i],(255,255,255,210),2)
            d.text((x+5,y+14),str(i+1),font=font(14),anchor="mm",fill=(23,49,100))
    elif index==1:
        for symbol,x,y,col in [("+",33,30,(104,235,144)),("−",104,18,(251,200,87)),
                               ("×",67,68,(255,148,142)),("÷",149,61,(132,211,252))]:
            d.ellipse((x+5,y+20,x+51,y+67),fill=(9,25,63,125))
            rr(d,(x,y,x+47,y+45),10,col,(255,255,255,235),2)
            d.text((x+23,y+19),symbol,anchor="mm",font=font(32),fill=(21,61,107))
    elif index==2:
        d.polygon([(54,102),(88,29),(131,102)],fill=(255,199,97,215))
        stroke(d,[(54,102),(88,29),(131,102),(54,102)],(255,249,221),3)
        for x,y in [(54,102),(88,29),(131,102)]:
            d.ellipse((x-4,y-4,x+4,y+4),fill=(255,255,255))
        d.ellipse((136,23,204,91),fill=(153,128,250,225),outline=(242,229,255),width=3)
        d.arc((146,33,194,81),30,260,fill=(255,255,255,150),width=3)
        d.polygon([(153,97),(185,79),(212,95),(179,113)],fill=(104,236,240),outline=(235,255,255))
        stroke(d,[(153,97),(154,118),(180,134),(179,113),(212,95),(211,117),(180,134)],(230,253,255),2)
    elif index==3:
        rr(d,(28,25,130,109),13,(13,58,112,190),(147,246,236,140),2)
        for n,(h,col) in enumerate([(25,(112,224,233)),(48,(255,206,107)),
                                     (39,(150,227,172)),(63,(172,157,253))]):
            x=41+n*20;rr(d,(x,100-h,x+13,101),3,col)
        d.ellipse((144,38,216,110),fill=(255,229,134),outline=(255,250,217),width=2)
        d.pieslice((144,38,216,110),225,360,fill=(110,231,220))
        d.pieslice((144,38,216,110),55,222,fill=(172,156,250))
        stroke(d,[(180,74),(180,40)],(255,255,255),2)
    elif index==4:
        stroke(d,[(41,105),(204,105)],(202,236,255),4)
        stroke(d,[(119,47),(119,105)],(250,242,199),5)
        stroke(d,[(52,52),(187,52)],(255,213,129),5)
        d.polygon([(104,108),(119,94),(135,108)],fill=(255,210,119),outline=(255,242,192))
        for x,y,s in [(47,64,"x"),(145,64,"7")]:
            rr(d,(x,y,x+54,y+34),8,(107,220,233) if s=="x" else (252,180,114),(255,250,226),2)
            d.text((x+27,y+16),s,font=font(22),anchor="mm",fill=(25,55,107))
        d.text((101,23),"=",font=font(22),fill=(255,230,174))
    else:
        d.ellipse((74,22,168,116),fill=(92,160,218,155),outline=(209,240,255),width=3)
        for radius in (25,40):
            d.ellipse((121-radius,69-radius,121+radius,69+radius),outline=(178,230,245,100),width=1)
        d.polygon([(121,31),(136,72),(121,65),(107,72)],fill=(255,214,115),outline=(255,253,208))
        d.polygon([(121,107),(107,72),(121,80),(136,72)],fill=(144,234,238),outline=(233,255,252))
        for px,py in [(45,42),(196,52),(191,103)]:
            pts=[]
            for z in range(10):
                angle=-pi/2+z*pi/5;r=11 if z%2==0 else 5
                pts.append((px+r*cos(angle),py+r*sin(angle)))
            d.polygon(pts,fill=(255,215,116),outline=(255,254,219))
    return img.convert("RGB")

def main():
    dest=Path(__file__).resolve().parents[2]/"app/src/main/res/drawable-nodpi/learn_topic_atlas.webp"
    dest.parent.mkdir(parents=True,exist_ok=True)
    atlas=Image.new("RGB",(W*6,H))
    for i in range(6):atlas.paste(artwork(i),(i*W,0))
    atlas.save(dest,format="WEBP",quality=69,method=6)
    assert dest.stat().st_size<100_000,"Unexpectedly heavy P3 topic artwork"
    print(f"Original six-topic visual atlas: {dest.stat().st_size} bytes")
if __name__=="__main__":main()
