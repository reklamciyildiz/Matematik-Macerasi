#!/usr/bin/env python3
"""Release-independent P3 visual/UX integration guard; no external dependencies."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
asset=root/"app/src/main/res/drawable-nodpi/learn_topic_atlas.webp"
assert asset.is_file(), "P3 original WebP atlas must be committed"
art=asset.read_bytes()
assert art[:4]==b"RIFF" and art[8:12]==b"WEBP", "Invalid WebP format"
assert 5000<len(art)<100000, "Unexpected artwork size"
src=root/"app/src/main/java/com/yigit033/matematikmacarasi/ui/screens/home"
cover=(src/"LearningTopicCover.kt").read_text(encoding="utf-8")
for name,index in [("NUMBERS",0),("OPERATIONS",1),("GEOMETRY",2),
                   ("DATA",3),("ALGEBRA",4),("REVIEW",5)]:
    assert f"TopicArea.{name} -> {index}" in cover, f"Wrong {name} atlas tile"
library=(src/"LearnTabScreen.kt").read_text(encoding="utf-8")
for required in ["LearningTopicCover(topic.area", "MathAreaFilterChip(",
                 "selectedAreaName", "LearningConceptExplorer(topic)",
                 "LearningAccessPolicy.accessible(", "LearningPremiumDialog(",
                 "progress.completed(", "topic.lessonIds", "BoxWithConstraints"]:
    assert required in library, f"Missing P3 learning UI or entitlement guard: {required}"
preview=(src/"LearningConceptExplorer.kt").read_text(encoding="utf-8")
for required in ["topic.grade > 4", "TopicArea.GEOMETRY", "TopicArea.NUMBERS",
                 "coerceAtMost(10)", "coerceAtLeast(0)", "heightIn(min = 48.dp)"]:
    assert required in preview, f"Missing math safety/interaction: {required}"
print(f"P3 topic visuals OK: six areas, {len(art)}-byte WebP, grade-scoped discovery, entitlement preserved")
