# Asset Kullanım Rehberi

## Kurallar

- `references/` içindeki referans pano sadece tasarım içindir; oyunda görsel olarak kullanılmaz.
- `assets/scenes/` sahneleri gerçek arka plan görselleridir.
- `assets/characters/`, `assets/props/` atlas içinden ayrılmış **küçük çözünürlüklü** sprite’lardır; sadece manifestteki küçük boyutlarda kullanın. Tam ekran büyütmeyin.
- `assets/ui/` görsellerinde hiçbir yazı/rakam bulunmaz; dinamik veriler Compose bileşenleridir.
- Temel UI için Compose ile düzgün çizilebilen sistem bileşenlerini önceliklendirin; düşük kaliteli/upscale görsel yerine Compose kullanın.
- WebP formatı Android için uygun; lossy WebP metin/rakam için kullanılmaz çünkü bunlar native text olur.
- `treasure_map_art.webp` üzerinde dekoratif **6** ve **X** bulunduğu için aktif matematik ekranında kullanılmaz.
- Arka planlar şeffaf değil, karakter/UI öğeleri RGBA WebP olabilir.

## Dosyalar

| Dosya | Boyut | Kullanım | Kısıt |
|---|---:|---|---|
| `references/GDD_APPROVED_CONCEPT_BOARD.webp` | 1536×1024 | Design reference ONLY. Not production screens. | Never use as a game screen/image background. |
| `assets/scenes/island_explorer_hero.webp` | 1086×1448 | Intro / world-landing hero illustration; text and buttons placed in Compose. | Contains character and dog baked into illustration; avoid layering same characters again. |
| `assets/scenes/number_board_backdrop.webp` | 840×1400 | Behind 4x4 number grid; use ContentScale.Crop and dim/scrim as needed. | Already derived from approved island art and softened; keep math tiles native Compose. |
| `assets/scenes/route_ocean_backdrop.webp` | 840×1320 | Hazine Rotasi background; dynamic touchable stepping stones must be Compose overlays. | Contains scenic ocean and islands, but NO real route path logic baked in. |
| `assets/scenes/treasure_map_art.webp` | 1254×1254 | Decorative art for map/mission preview card only. | Contains decorative 6 and red X baked in. DO NOT use as live mathematics board or dynamic map numbers. |
| `assets/characters/explorer_idle.webp` | 111×121 | Explorer hero thumbnail for sidebars, only <=90dp. | Low-resolution sprite from approved game concept; do not upscale. |
| `assets/characters/explorer_point.webp` | 123×125 | Explorer hero pointing, only <=90dp. | Low-resolution sprite from approved game concept; do not upscale. |
| `assets/characters/explorer_map.webp` | 108×128 | Explorer hero reading map, only <=90dp. | Low-resolution sprite from approved game concept; do not upscale. |
| `assets/characters/companion_dog.webp` | 97×117 | Friendly dog thumbnail, <=100dp. | Low-resolution sprite from approved game concept; do not upscale. |
| `assets/props/chest_closed.webp` | 120×124 | Treasure-chest collectible illustration, <=100dp. | Low-resolution sprite, avoid full-screen use. |
| `assets/props/chest_open.webp` | 120×123 | Open-chest illustration, <=100dp. | Low-resolution sprite, avoid full-screen use. |
| `assets/props/map_piece.webp` | 78×97 | Map-piece reward icon, <=96dp. | Low-resolution sprite, avoid full-screen use. |
| `assets/props/island_node.webp` | 105×124 | Island illustration, <=90dp. | Small source, avoid full-screen use. |
| `assets/props/forest_node.webp` | 113×124 | Forest island thumbnail, <=90dp. | Small source, avoid full-screen use. |
| `assets/props/temple_node.webp` | 100×123 | Temple art thumbnail, <=90dp. | Small source, avoid full-screen use. |
| `assets/ui/number_tile_normal.webp` | 260×260 | Number tile visual state; render number text in Compose. | Apply ContentScale.FillBounds or draw background with Compose; do not bake numerals. |
| `assets/ui/number_tile_selected.webp` | 260×260 | Number tile visual state; render number text in Compose. | Apply ContentScale.FillBounds or draw background with Compose; do not bake numerals. |
| `assets/ui/number_tile_disabled.webp` | 260×260 | Number tile visual state; render number text in Compose. | Apply ContentScale.FillBounds or draw background with Compose; do not bake numerals. |
| `assets/ui/number_tile_error.webp` | 260×260 | Number tile visual state; render number text in Compose. | Apply ContentScale.FillBounds or draw background with Compose; do not bake numerals. |
| `assets/ui/panel_parchment_wide.webp` | 1024×520 | Blank parchment surface: title, paragraph, progress and interactive buttons must be Compose. | If scaling panel heavily, use proper nine-slice scaling or redraw in Compose to protect corners. |
| `assets/ui/panel_parchment_tall.webp` | 870×1120 | Blank parchment surface: title, paragraph, progress and interactive buttons must be Compose. | If scaling panel heavily, use proper nine-slice scaling or redraw in Compose to protect corners. |
| `assets/ui/button_green.webp` | 850×190 | Decorative button background; actual button/text/semantics must be native Compose. | Prefer Compose gradient if possible; do not duplicate hit target in static image. |
| `assets/ui/button_blue.webp` | 850×190 | Decorative button background; actual button/text/semantics must be native Compose. | Prefer Compose gradient if possible; do not duplicate hit target in static image. |
| `assets/ui/button_orange.webp` | 850×190 | Decorative button background; actual button/text/semantics must be native Compose. | Prefer Compose gradient if possible; do not duplicate hit target in static image. |
| `assets/ui/button_disabled.webp` | 850×190 | Decorative button background; actual button/text/semantics must be native Compose. | Prefer Compose gradient if possible; do not duplicate hit target in static image. |
| `assets/props/star_reward.webp` | 256×256 | Gold star reward / UI badge; update counts with actual StarManager. | Not a currency balance by itself. |
