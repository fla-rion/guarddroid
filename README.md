# GuardDroid

<p align="center">
  <img src="app/src/main/res/drawable/ic_shield.xml" width="80" alt="GuardDroid Shield"/>
</p>

<p align="center">
  <a href="#deutsch">🇩🇪 Deutsch</a> &nbsp;|&nbsp;
  <a href="#english">🇬🇧 English</a>
</p>

<p align="center">
  <a href="https://github.com/fla-rion/guarddroid/actions/workflows/ci.yml">
    <img src="https://github.com/fla-rion/guarddroid/actions/workflows/ci.yml/badge.svg" alt="CI"/>
  </a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-green?logo=android" alt="Android 8+"/>
  <img src="https://img.shields.io/badge/Kotlin-2.0-blue?logo=kotlin" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/License-Apache%202.0-lightgrey" alt="License"/>
  <img src="https://img.shields.io/badge/GMS%2FHMS-compatible-orange" alt="GMS HMS"/>
</p>

---

## Deutsch

Eine generische, herstellerunabhängige Android-Jugendschutz- und Geräteverwaltungs-App.

### Überblick

GuardDroid ermöglicht Eltern und Erziehungsberechtigten, Android-Geräte mit flexiblen Kinderschutzregeln zu verwalten. Die App funktioniert auf nahezu allen Android-Geräten ab Android 8.0 – einschließlich Huawei-Geräte ohne Google Play Services.

> **Wichtig:** GuardDroid ist keine Cloud-Lösung. Alle Regeln werden lokal gespeichert und ausgeführt. Kein Internet erforderlich, keine Anmeldung, kein Google-Account.

### Unterstützte Android-Versionen

| Version | API | Status |
|---------|-----|--------|
| Android 8.0 Oreo | 26 | ✅ Minimum |
| Android 10 | 29 | ✅ Getestet |
| Android 12 | 31 | ✅ Getestet |
| Android 14 | 34 | ✅ Getestet |
| Android 15 | 35 | ✅ Ziel-SDK |

### Unterstützte Geräte

| Gerät | System | GMS/HMS | Unterstützung |
|-------|--------|---------|--------------|
| Google Pixel | Stock Android | GMS | ✅ Vollständig |
| Samsung (One UI) | One UI | GMS | ✅ Vollständig |
| Xiaomi / HyperOS | MIUI/HyperOS | GMS | ✅ Vollständig |
| Motorola | Stock Android | GMS | ✅ Vollständig |
| Huawei P20 lite | EMUI 8+ | GMS | ✅ Vollständig |
| Huawei P40 lite | EMUI 10 | HMS | ✅ Vollständig |
| Honor | Magic UI/HMS | HMS | ✅ Vollständig |
| Ohne GMS | AOSP | keins | ✅ Vollständig |

### Funktionen

#### App-Status

| Status | Bedeutung |
|--------|-----------|
| `IMMER_ERLAUBT` | App ist immer nutzbar |
| `ZEITPLAN` | App nur in konfigurierten Zeitfenstern |
| `NUR_ADMIN` | App nur im Admin-Modus nutzbar |
| `GESPERRT` | App ist gesperrt |
| `AUSGEBLENDET` | App erscheint nicht im Launcher (benötigt Device Owner) |

#### Verwaltungsebenen

| Funktion | Accessibility | Device Admin | Device Owner |
|----------|:---:|:---:|:---:|
| App-Nutzung überwachen | ✅ | ✅ | ✅ |
| Deinstallationsschutz | ❌ | ✅ | ✅ |
| App sperren (System) | ❌ | ❌ | ✅ |
| App ausblenden | ❌ | ❌ | ✅ |
| Installation sperren | ❌ | ❌ | ✅ |
| Unbekannte Quellen sperren | ❌ | ❌ | ✅ |
| Einstellungen einschränken | ❌ | ❌ | ✅ |

#### Sicherheit

- Master-Code: PBKDF2WithHmacSHA256, 100.000 Iterationen, 16-Byte Salt
- Gespeichert in `EncryptedSharedPreferences` (Android Keystore)
- Rate Limiting: Nach 5 Fehlversuchen → 30 Minuten Sperre
- Admin-Modus Timeout: 5 Minuten (konfigurierbar)

### Installation

#### APK direkt installieren

1. APK von [Releases](https://github.com/fla-rion/guarddroid/releases) herunterladen
2. Einstellungen → Sicherheit → „Unbekannte Quellen" temporär erlauben
3. APK installieren und Einrichtungsassistenten folgen

#### Device Owner einrichten (empfohlen)

Für vollständige Sperr-Funktionen:

```bash
# Gerät darf keine Konten haben – vorher alle entfernen:
# Einstellungen → Konten → alle entfernen

adb shell dpm set-device-owner dev.guarddroid.app/.receiver.GuardDroidAdminReceiver
```

Konten können danach wieder hinzugefügt werden.

#### APK auf GitHub manuell bauen

Unter [Actions → CI](https://github.com/fla-rion/guarddroid/actions/workflows/ci.yml) → „Run workflow" → Build-Typ wählen → APK als Artifact herunterladen.

### Einrichtungsassistent (8 Schritte)

1. **Willkommen** – Überblick über GuardDroid
2. **Geräteanalyse** – Automatische Erkennung aller Geräteeigenschaften
3. **Berechtigungen** – Nutzungsdaten, Barrierefreiheit, Device Admin
4. **Master-Code** – Sicheres Festlegen des Administrator-Codes
5. **Apps konfigurieren** – Status für jede installierte App festlegen
6. **Zeitpläne** – Nutzungszeiten pro Wochentag und Zeitfenster
7. **Systemregeln** – Installation, Einstellungen, USB-Debugging
8. **Zusammenfassung** – Überblick vor Abschluss

### Architektur

```
app/                    → Activities, Services, BroadcastReceiver
core/
  common/               → AppStatus, Capability (gemeinsame Typen)
  device/               → DeviceAnalyzer, CapabilityEngine
  management/           → ManagementProvider (DeviceOwner / Admin / Accessibility)
  security/             → SecurityManager (PBKDF2, Keystore)
  scheduling/           → ScheduleEvaluator (Zeitpläne)
  database/             → Room DB, DAOs, Entities
  update/               → GitHub Releases Update-Checker, WorkManager
feature/
  setup/                → 8-Schritte-Einrichtungsassistent
  apps/                 → App-Liste für den Benutzer
  admin/                → Admin-Dashboard mit Code-Eingabe
  restrictions/         → Systemregeln-Verwaltung
```

### Datenschutz

- Keine Netzwerkkommunikation (außer freiwilliger Update-Check)
- Keine Analytics, keine Werbung
- Alle Daten lokal auf dem Gerät
- Keine Registrierung, kein Account

### Bekannte Einschränkungen

- GuardDroid kann sich nicht selbst zum Device Owner machen (Android-Sicherheitsgrenze)
- Einige Hersteller-Launcher zeigen ausgeblendete Apps weiterhin an
- Kein VPN-basiertes Internet-Blocking in dieser Version

### Entwicklung

```bash
# Debug APK bauen
./gradlew assembleDebug

# Tests
./gradlew test

# Lint
./gradlew lint
```

**Voraussetzungen:** Android Studio Ladybug+, JDK 17, Android SDK API 35

### Lizenz

Apache License 2.0 – siehe [LICENSE](LICENSE)

---

## English

A generic, manufacturer-independent Android parental control and device management app.

### Overview

GuardDroid allows parents and guardians to manage Android devices with flexible parental control rules. The app works on virtually all Android devices from Android 8.0 onwards — including Huawei devices without Google Play Services.

> **Important:** GuardDroid is not a cloud solution. All rules are stored and executed locally. No internet required, no sign-in, no Google account.

### Supported Android Versions

| Version | API | Status |
|---------|-----|--------|
| Android 8.0 Oreo | 26 | ✅ Minimum |
| Android 10 | 29 | ✅ Tested |
| Android 12 | 31 | ✅ Tested |
| Android 14 | 34 | ✅ Tested |
| Android 15 | 35 | ✅ Target SDK |

### Supported Devices

| Device | System | GMS/HMS | Support |
|--------|--------|---------|---------|
| Google Pixel | Stock Android | GMS | ✅ Full |
| Samsung (One UI) | One UI | GMS | ✅ Full |
| Xiaomi / HyperOS | MIUI/HyperOS | GMS | ✅ Full |
| Motorola | Stock Android | GMS | ✅ Full |
| Huawei P20 lite | EMUI 8+ | GMS | ✅ Full |
| Huawei P40 lite | EMUI 10 | HMS | ✅ Full |
| Honor | Magic UI/HMS | HMS | ✅ Full |
| Without GMS | AOSP | none | ✅ Full |

### Features

#### App Status

| Status | Meaning |
|--------|---------|
| `ALWAYS_ALLOWED` | App is always accessible |
| `SCHEDULED` | App only accessible during configured time windows |
| `ADMIN_ONLY` | App only accessible in admin mode |
| `BLOCKED` | App is blocked |
| `HIDDEN` | App hidden from launcher (requires Device Owner) |

#### Management Levels

| Feature | Accessibility | Device Admin | Device Owner |
|---------|:---:|:---:|:---:|
| Monitor app usage | ✅ | ✅ | ✅ |
| Uninstall protection | ❌ | ✅ | ✅ |
| Block app (system) | ❌ | ❌ | ✅ |
| Hide app | ❌ | ❌ | ✅ |
| Block installation | ❌ | ❌ | ✅ |
| Block unknown sources | ❌ | ❌ | ✅ |
| Restrict settings | ❌ | ❌ | ✅ |

#### Security

- Master code: PBKDF2WithHmacSHA256, 100,000 iterations, 16-byte salt
- Stored in `EncryptedSharedPreferences` (Android Keystore)
- Rate limiting: After 5 failed attempts → 30-minute lockout
- Admin mode timeout: 5 minutes (configurable)

### Installation

#### Direct APK install

1. Download APK from [Releases](https://github.com/fla-rion/guarddroid/releases)
2. Settings → Security → Allow "Unknown sources" temporarily
3. Install APK and follow the setup wizard

#### Set up Device Owner (recommended)

For full blocking capabilities:

```bash
# Device must have no accounts — remove all first:
# Settings → Accounts → remove all

adb shell dpm set-device-owner dev.guarddroid.app/.receiver.GuardDroidAdminReceiver
```

Accounts can be re-added afterwards.

#### Build APK manually on GitHub

Go to [Actions → CI](https://github.com/fla-rion/guarddroid/actions/workflows/ci.yml) → "Run workflow" → choose build type → download APK as artifact.

### Setup Wizard (8 steps)

1. **Welcome** – GuardDroid overview
2. **Device Analysis** – Automatic detection of all device capabilities
3. **Permissions** – Usage data, accessibility, device admin
4. **Master Code** – Securely set the administrator code
5. **Configure Apps** – Set status for each installed app
6. **Schedules** – Define usage times per weekday and time window
7. **System Rules** – Installation, settings, USB debugging
8. **Summary** – Review before finishing

### Architecture

```
app/                    → Activities, Services, BroadcastReceiver
core/
  common/               → AppStatus, Capability (shared types)
  device/               → DeviceAnalyzer, CapabilityEngine
  management/           → ManagementProvider (DeviceOwner / Admin / Accessibility)
  security/             → SecurityManager (PBKDF2, Keystore)
  scheduling/           → ScheduleEvaluator (time-based rules)
  database/             → Room DB, DAOs, Entities
  update/               → GitHub Releases update checker, WorkManager
feature/
  setup/                → 8-step setup wizard
  apps/                 → App list for users
  admin/                → Admin dashboard with code input
  restrictions/         → System rules management
```

### Privacy

- No network communication (except optional update check)
- No analytics, no ads
- All data stored locally on device
- No registration, no account required

### Known Limitations

- GuardDroid cannot make itself Device Owner (Android security boundary)
- Some manufacturer launchers still show hidden apps
- No VPN-based internet blocking in this version

### Development

```bash
# Build debug APK
./gradlew assembleDebug

# Tests
./gradlew test

# Lint
./gradlew lint
```

**Requirements:** Android Studio Ladybug+, JDK 17, Android SDK API 35

### License

Apache License 2.0 – see [LICENSE](LICENSE)

---

## Google Play Store – Veröffentlichungsplan / Publishing Plan

### 🇩🇪 Schritte für den Google Play Store

#### Schritt 1: Play Console Konto (einmalig, ca. 25 USD)
- Konto erstellen: [play.google.com/console](https://play.google.com/console)
- Einmalige Registrierungsgebühr: 25 USD
- Entwickler-Identität verifizieren (Name, Adresse, ggf. D-U-N-S-Nummer)

#### Schritt 2: Signing-Keystore erstellen
```bash
keytool -genkey -v \
  -keystore guarddroid-release.keystore \
  -alias guarddroid \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```
> ⚠️ **Keystore niemals verlieren!** Ohne ihn können keine Updates veröffentlicht werden.
> Keystore NICHT ins Git-Repository committen.

#### Schritt 3: Keystore als GitHub Secret hinterlegen
```bash
# Keystore als Base64 kodieren
base64 -w 0 guarddroid-release.keystore > keystore.b64
```
In GitHub → Settings → Secrets → Actions folgende Secrets anlegen:
- `KEYSTORE_BASE64` – Base64-kodierter Keystore
- `STORE_PASSWORD` – Keystore-Passwort
- `KEY_ALIAS` – Key-Alias (z.B. `guarddroid`)
- `KEY_PASSWORD` – Key-Passwort

Danach baut der Release-Workflow automatisch eine signierte APK und ein AAB.

#### Schritt 4: App-Eintrag in Play Console anlegen
- App-Name: **GuardDroid**
- Kategorie: **Produktivität** oder **Tools**
- Kurzbeschreibung (80 Zeichen): *Jugendschutz & Geräteverwaltung für Android – offline, ohne Cloud*
- Vollständige Beschreibung: Funktionen aus dieser README nutzen
- Screenshots: Mindestens 2 Phone-Screenshots (1080×1920 oder ähnlich)
- Feature Graphic: 1024×500 px (Pflicht)
- App-Symbol: 512×512 px (hochauflösend, bereits im Projekt vorhanden)

#### Schritt 5: Datenschutzerklärung (Pflicht)
Google Play **verlangt** eine Datenschutzerklärung, auch wenn die App keine Daten sammelt. Mindestinhalt:
- Welche Daten werden gesammelt? → Keine, außer lokal gespeicherte Kinderschutz-Regeln
- Werden Daten geteilt? → Nein
- Kontakt des Entwicklers

Empfehlung: einfache HTML-Seite auf GitHub Pages oder Hosting eigener Wahl.

#### Schritt 6: Inhaltsbewertung
- Im Play Console-Fragebogen ausfüllen
- GuardDroid fällt voraussichtlich unter **PEGI 3 / Everyone**
- Kategorie „Überwachung von Geräten" im Fragebogen beachten → ehrlich beantworten

#### Schritt 7: AAB hochladen & Release erstellen
1. GitHub Release mit Tag erstellen: `git tag v1.0.0 && git push --tags`
2. Der Release-Workflow baut automatisch `app-release.aab`
3. In Play Console → Production → Release erstellen → AAB hochladen
4. Rollout auf 10% starten, dann schrittweise erhöhen

#### Schritt 8: Wichtige Play-Policy-Anforderungen für GuardDroid
- **Device Admin / Überwachung**: Google verlangt ehrliche Beschreibung; GuardDroid muss klar als Kinderschutz-Tool deklariert werden
- **Accessibility Service**: Muss im Play Store Listing erklärt werden; Google prüft Accessibility-APIs streng → vollständige Erklärung einreichen
- **Permissions**: `PACKAGE_USAGE_STATS` muss begründet werden (UsageStats-Declaration bei Google einreichen falls nötig)
- **Zielgruppe**: Nicht als „Für Kinder" markieren (die App richtet sich an Eltern, nicht Kinder)

#### Zeitplan (realistisch)
| Schritt | Aufwand | Dauer |
|---------|---------|-------|
| Play Console Konto + Keystore | 1–2 Stunden | Tag 1 |
| Screenshots & Grafiken erstellen | 2–4 Stunden | Tag 1–2 |
| Datenschutzerklärung | 1 Stunde | Tag 2 |
| Play Console Eintrag befüllen | 2 Stunden | Tag 2–3 |
| Google-Prüfung | automatisch | 1–7 Tage |

---

### 🇬🇧 Steps for Google Play Store

#### Step 1: Play Console Account (one-time, ~$25 USD)
- Create account: [play.google.com/console](https://play.google.com/console)
- One-time registration fee: $25 USD
- Verify developer identity (name, address)

#### Step 2: Create Signing Keystore
```bash
keytool -genkey -v \
  -keystore guarddroid-release.keystore \
  -alias guarddroid \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```
> ⚠️ **Never lose the keystore!** Without it you cannot publish updates.
> Do NOT commit the keystore to the Git repository.

#### Step 3: Add Keystore as GitHub Secret
```bash
# Encode keystore as Base64
base64 -w 0 guarddroid-release.keystore > keystore.b64
```
In GitHub → Settings → Secrets → Actions, create these secrets:
- `KEYSTORE_BASE64` – Base64-encoded keystore
- `STORE_PASSWORD` – Keystore password
- `KEY_ALIAS` – Key alias (e.g. `guarddroid`)
- `KEY_PASSWORD` – Key password

The release workflow will then automatically build a signed APK and AAB.

#### Step 4: Create App Listing in Play Console
- App name: **GuardDroid**
- Category: **Productivity** or **Tools**
- Short description (80 chars): *Parental control & device management for Android – offline, no cloud*
- Full description: Use features from this README
- Screenshots: At least 2 phone screenshots (1080×1920 or similar)
- Feature graphic: 1024×500 px (required)
- App icon: 512×512 px (high-res, already in project)

#### Step 5: Privacy Policy (required)
Google Play **requires** a privacy policy even if the app collects no data. Minimum content:
- What data is collected? → None, except locally stored parental rules
- Is data shared? → No
- Developer contact

Recommendation: Simple HTML page on GitHub Pages or any hosting.

#### Step 6: Content Rating
- Complete the Play Console questionnaire
- GuardDroid will likely fall under **PEGI 3 / Everyone**
- Note the "device monitoring" category in the questionnaire → answer honestly

#### Step 7: Upload AAB & Create Release
1. Create GitHub Release with tag: `git tag v1.0.0 && git push --tags`
2. The release workflow automatically builds `app-release.aab`
3. In Play Console → Production → Create release → Upload AAB
4. Start rollout at 10%, then increase gradually

#### Step 8: Important Play Policy Requirements for GuardDroid
- **Device Admin / Monitoring**: Google requires honest description; GuardDroid must clearly be declared as a parental control tool
- **Accessibility Service**: Must be explained in the Play Store listing; Google reviews Accessibility APIs strictly → submit full explanation
- **Permissions**: `PACKAGE_USAGE_STATS` may require a declaration if prompted by Google
- **Target audience**: Do NOT mark as "For children" (the app targets parents, not children)

#### Timeline (realistic)
| Step | Effort | Duration |
|------|--------|----------|
| Play Console account + keystore | 1–2 hours | Day 1 |
| Screenshots & graphics | 2–4 hours | Day 1–2 |
| Privacy policy | 1 hour | Day 2 |
| Fill Play Console listing | 2 hours | Day 2–3 |
| Google review | automatic | 1–7 days |
