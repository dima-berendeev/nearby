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
- [ ] Given there is no internet then Offline banner is visible
- [ ] Given there is internet then Offline banner is invisible
- [ ] Offline banner has priority over location permission banners
---
- [ ] Given location permissions denied then then banner is visible
- [ ] Coarse permission only
- [ ] Fine permission granted then no banners 
