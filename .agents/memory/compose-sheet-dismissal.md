---
name: Compose sheet dismissal
description: Reusable rule for animated bottom sheets and modal panels in this Android Compose app.
---

Animated bottom sheets must separate the panel that remains rendered from the boolean that controls visibility. Closing should first set visibility to false, then clear the rendered panel only after the exit animation duration.

**Why:** Removing the panel data at the same time as setting `AnimatedVisibility` to false causes Compose to dispose the content immediately, so the exit animation cannot be seen.

**How to apply:** Keep a `displayedPanel` (or equivalent) alongside `isPanelVisible`; use the displayed value while the exit transition runs. Add a backdrop tap, downward drag threshold, and `BackHandler` that all trigger the same dismissal callback.