# Clean Home – מסך בית נקי לכרומקאסט עם Google TV

מסך בית בלי פרסומות: שעון גדול, רקע שמשתנה לפי שעות היום (זריחה, יום, שקיעה, לילה),
ושורה של Netflix, YouTube, Apple TV, Prime Video ודפדפן (TV Bro).
אפליקציה שעוד לא מותקנת פותחת את הדף שלה בחנות Play.

---

## דרך א' – רק דרך האינטרנט (בלי Android Studio, בלי מחשב חזק)

### 1. בנייה ב-GitHub (חינם)
1. לפתוח חשבון ב-https://github.com ולבחור **New repository** (למשל בשם `CleanHome`, **Public**).
2. בדף הריפו: **Add file ← Upload files**, ולגרור לשם **את כל התוכן** של התיקייה `CleanHome`
   (כולל התיקייה `.github`). ללחוץ **Commit changes**.
3. בלשונית **Actions** תתחיל בנייה (בערך 3–5 דקות). כשמופיע ✅ – הקובץ מוכן.
4. קישור ההורדה הישיר יהיה:
   `https://github.com/<שם-המשתמש>/CleanHome/releases/download/latest/CleanHome.apk`

> אם בווינדוס התיקייה `.github` לא עולה בגרירה: **Add file ← Create new file**, בשם
> `.github/workflows/build.yml`, ולהדביק את התוכן של הקובץ הזה.

### 2. התקנה ישירות בטלוויזיה
1. בכרומקאסט, מחנות Play: להתקין את האפליקציה **Downloader** (של AFTVnews).
2. הגדרות ← אפליקציות ← אבטחה והגבלות ← **מקורות לא ידועים** ← להפעיל ל-Downloader.
3. לפתוח את Downloader, להקליד את קישור ההורדה מלמעלה ← **Install**.

### 3. להפוך אותו למסך הבית
1. לפתוח את **Clean Home** מרשימת האפליקציות.
2. ללחוץ על הכפתור **"הפעלת כפתור הבית"** ← לבחור **Clean Home – כפתור הבית** ← להפעיל.
3. מעכשיו כפתור הבית בשלט פותח את Clean Home. כדי לבטל – לכבות את השירות באותו מקום.

---

## דרך ב' – עם Android Studio במחשב Windows
1. להתקין את **Android Studio**, לפתוח את התיקייה (**File ← Open**), ואז
   **Build ← Build App Bundle(s) / APK(s) ← Build APK(s)**.
2. בכרומקאסט: הגדרות ← מערכת ← מידע ← 7 לחיצות על **Android TV OS build**,
   ואז באפשרויות למפתחים להפעיל **Wireless debugging**.
3. להריץ `install.bat` ולעקוב אחרי ההוראות. `restore.bat` מבטל.

---

## הערות
- רשימת האפליקציות בשורה נמצאת בראש הקובץ `HomeActivity.java` (המערך `APPS`).
- בדרך א' מסך הבית של Google TV לא נמחק – הוא רק "מדולג". לפעמים בהדלקה הוא יופיע לרגע.
