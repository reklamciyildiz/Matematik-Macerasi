# Firebase local RTDB emulator regression tests

The Firebase configuration is in this subfolder and uses the fake
demo-matematik-macerasi project. No real credentials or data are accessed.
There is deliberately no firebase.json at the repository root and NO
production Firebase deploy job.

From commercial/firebase/emulator, run:

    npx --yes firebase-tools@15.34.2 emulators:exec --only database --project demo-matematik-macerasi --non-interactive "node ../tests/realtime-database-rules.mjs"

These checks test deployed-rule semantics in the local emulator. Anonymous
code-specific reads and writes remain a risk. This is NOT the final
server-authoritative entitlement solution.
