**Input**

- [ ] Permission state
- [ ] Is online
- [ ] Coordinates available
- [ ] Query
- [ ] Success/Failure/Loading

**Output**

- [ ] Pin icon
- [ ] onQuery changed
- [ ] Query text
- [ ] Banner Offline
- [ ] Banner Location Permission Denied
- [ ] Banner Fine Location Permission Denied
- [ ] Place items
- [ ] Error blank
- [ ] Not items blank
- [ ] Progress

**Checklist**

- [X] Given Loading then progress is visible
- [X] Given Success and list is not empty then items are visible and progress hidden
- [X] Given Success and list is empty then not found blank is visible
- [X] Given Failure then blank is visible
---
- [x] Given offline then Offline banner is visible
- [X] Offline banner has priority over location permission banners
- [X] Given online location permissions denied then then banner is visible
- [X] Given online and Coarse permission only then no fine location permission visible
- [x] Given online and location permission granted then no banner 
---
- [ ] Given location unavailable then icon is unavailable
- [ ] Given location is expected soon then icon has animation
- [ ] Given location is available then icon is success