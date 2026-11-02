#!/usr/bin/env python3
"""Generate original premium maths adventure card art as lightweight offline WebP."""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
from pathlib import Path
import math,random
ROOT=Path("app/src/main/res/drawable-nodpi");ROOT.mkdir(exist_ok=True,parents=True)
W=600;H=620
def color(a,b,t):return tuple(round(a[i]*(1-t)+b[i]*t) for i in range(3))
def soft_glow(img,x,y,r,c,alpha):
    layer=Image.new('RGBA',img.size,(0,0,0,0));d=ImageDraw.Draw(layer)
    for k in range(16,0,-1):
        rad=r*(.3+.8*k/16)
        d.ellipse((x-rad,y-rad,x+rad,y+rad),fill=(*c,round(alpha*(1-k/17)**1.6)))
    img.alpha_composite(layer)
def sparkle(d,x,y,rad,c):
    d.polygon([(x,y-rad),(x+rad*.17,y-rad*.18),(x+rad,y),
               (x+rad*.17,y+rad*.17),(x,y+rad),(x-rad*.18,y+rad*.17),
               (x-rad,y),(x-rad*.17,y-rad*.18)],fill=c)
def fillgrad(top,bottom):
    im=Image.new('RGBA',(W,H));d=ImageDraw.Draw(im)
    for y in range(H):
        v=(y/H)**.9
        d.line((0,y,W,y),fill=(*color(top,bottom,v),255))
    return im
fonts=['/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf',
       '/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf']
def font(sz):
    for p in fonts:
        if Path(p).exists():return ImageFont.truetype(p,sz)
    return ImageFont.load_default()

cards={
'number_hunt':((12,111,155),(7,66,78),(23,216,151),(255,208,83)),
'addition':((23,87,190),(29,27,116),(45,214,249),(255,221,108)),
'subtraction':((195,39,109),(78,24,86),(255,140,177),(255,222,101)),
'multiplication':((5,135,100),(5,62,85),(58,246,181),(255,218,99)),
'division':((197,99,20),(93,41,88),(255,173,81),(255,237,143)),
'maze':((96,45,164),(22,40,101),(177,121,251),(255,232,109)),
'forest':((24,137,115),(10,65,80),(136,241,122),(255,215,101))
}
for name,(top,bottom,accent,gold) in cards.items():
    rng=random.Random(174700+list(cards).index(name)*197)
    im=fillgrad(top,bottom)
    for i in range(9):
        soft_glow(im,rng.randrange(W),rng.randrange(H),rng.randrange(110,310),accent,65)
    d=ImageDraw.Draw(im,'RGBA')
    # Distant dream islands and castles; no marketing UI or labels.
    for i in range(9):
        x=i*91-80;y=410+rng.randrange(-80,130);r=80+rng.randrange(40)
        d.ellipse((x-r,y-25,x+r,y+52),fill=(*color(bottom,accent,.32),230))
        d.polygon([(x-r*.6,y+15),(x,y+140),(x+r*.6,y+13)],fill=(*bottom,195))
    for i in range(24):
        x=rng.randrange(W);y=rng.randrange(H);r=rng.randrange(4,15)
        sparkle(d,x,y,r,(*gold,rng.randrange(95,210)))
    # Large golden treasure chest for number hunt (not a market stall).
    if name=='number_hunt':
        soft_glow(im,303,372,185,gold,210);d=ImageDraw.Draw(im,'RGBA')
        d.ellipse((108,425,502,502),fill=(3,55,65,140))
        d.polygon([(137,325),(210,280),(430,281),(490,337),(465,435),(165,435)],
                   fill=(127,67,45,255),outline=(*gold,255),width=13)
        d.polygon([(154,340),(222,299),(418,299),(474,343)],fill=(235,150,57,255))
        d.rounded_rectangle((151,376,474,482),radius=19,fill=(167,86,44,255),
                            outline=(*gold,255),width=14)
        d.rectangle((289,357,345,474),fill=(*gold,255))
        d.ellipse((298,402,335,439),fill=(255,252,191,255))
        d.ellipse((311,415,324,427),fill=(109,74,32,255))
        for x,y,size,c in [(165,215,88,(254,209,75)),(311,153,105,(122,255,196)),
                           (441,191,96,(255,118,183)),(276,57,64,(255,248,174))]:
            soft_glow(im,x,y,size,c,130)
            d=ImageDraw.Draw(im,'RGBA')
            d.text((x,y),str([1,3,5,8][[(165,215),(311,153),(441,191),(276,57)].index((x,y))]),
                   font=font(size),fill=(*c,255),stroke_width=4,stroke_fill=(12,60,90,240),
                   anchor='mm')
        for x,y in [(70,323),(526,318),(501,110)]:
            sparkle(d,x,y,24,(*gold,250))
    else:
        # Four-operation and adventure cards get distinct polished iconography.
        symbols={'addition':'+','subtraction':'−','multiplication':'×',
                 'division':'÷','maze':'✦','forest':'3'}
        symbol=symbols[name]
        soft_glow(im,300,270,220,accent,165);d=ImageDraw.Draw(im,'RGBA')
        for j in range(3):
            x=140+j*138;y=350+(j%2)*80
            d.rounded_rectangle((x-65,y-65,x+65,y+65),radius=27,
                                fill=(*color(top,bottom,.24),240),
                                outline=(*gold,200),width=7)
        d.ellipse((149,91,451,392),fill=(*color(top,accent,.48),255),
                  outline=(*gold,255),width=13)
        if name in ('addition','subtraction','multiplication','division'):
            d.text((300,222),symbol,fill=(255,254,244,255),font=font(185),
                   anchor="mm",stroke_width=5,stroke_fill=(*bottom,255))
            for i,sym in enumerate(["1","2","3"]):
                d.text((140+i*145,435+i%2*36),sym,
                       fill=(*gold,255),font=font(90),
                       anchor="mm",stroke_width=3,stroke_fill=(*bottom,255))
        elif name=='maze':
            d.text((300,215),'✦',fill=(*gold,255),font=font(170),anchor="mm")
            d.line([(100,465),(175,465),(175,365),(270,365),(270,505),
                    (420,505),(420,445),(515,445)],fill=(*gold,255),width=23,joint="curve")
        else:
            d.text((300,220),"3",fill=(*gold,255),font=font(170),anchor="mm")
            d.polygon([(80,465),(145,320),(206,462)],fill=(*accent,255))
            d.polygon([(360,485),(435,315),(511,485)],fill=(*accent,255))
    # Tonal vignette for better cropping in narrow and square cards.
    overlay=Image.new('RGBA',im.size,(0,0,0,0));od=ImageDraw.Draw(overlay)
    for y in range(520,H):
        od.line((0,y,W,y),fill=(1,12,35,round((y-520)/100*63)))
    im=Image.alpha_composite(im,overlay).convert('RGB')
    out=ROOT/f'card_{name}_premium.webp'
    im.save(out,'WEBP',quality=82,method=6)
    print(out,out.stat().st_size)
