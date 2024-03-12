**Input**
 
- Permission state
- Is online
- Coordinates available
- Query
- Success/Failure/Loading

**Output**

- Pin icon/ Available/
- Banner: Offline/Location Permission Denied/Coarse Location only/No banner
- Items: Success/Error blank/No items blank/Progress
- onQuery changed
- Query text

**Checklist**

*Items*

- [X] Given Loading then progress is visible
- [X] Given Success and list is not empty then items are visible and progress hidden
- [X] Given Success and list is empty then not found blank is visible
- [X] Given Failure then blank is visible

*Banner*
- [x] Given offline then Offline banner is visible
- [X] Offline banner has priority over location permission banners
- [X] Given online location permissions denied then then banner is visible
- [X] Given online and Coarse permission only then no fine location permission visible
- [x] Given online and location permission granted then no banner 

*Icon*
- [X] Given location is available then icon is available
- [X] Given location unavailable then icon is unavailable
- [X] Given location is expected soon then icon has animation
