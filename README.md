# GuardDroid

Eine generische, herstellerunabhängige Android-Jugendschutz- und Geräteverwaltungs-App.

## Überblick

GuardDroid ermöglicht Eltern und Erziehungsberechtigten, Android-Geräte mit flexiblen Kinderschutzregeln zu verwalten. Die App funktioniert auf nahezu allen Android-Geräten ab Android 8.0 – einschließlich Huawei-Geräte ohne Google Play Services.

**Wichtig:** GuardDroid ist keine Cloud-Lösung. Alle Regeln werden lokal auf dem Gerät gespeichert und ausgeführt. Kein Internet erforderlich, keine Anmeldung, kein Google-Account.

---

## Unterstützte Android-Versionen

- **Minimum:** Android 8.0 (API 26)
- **Getestet mit:** Android 8, 10, 12, 14
- **Ziel-SDK:** Android 15 (API 35)

---

## Unterstützte Geräte

Die App wurde speziell für Gerätevielfalt entwickelt:

| Gerät | Besonderheit | Unterstützung |
|-------|-------------|--------------|
| Google Pixel | Reines Android + GMS | Vollständig |
| Samsung (One UI) | GMS | Vollständig |
| Xiaomi/MIUI/HyperOS | GMS | Vollständig |
| Motorola | Reines Android + GMS | Vollständig |
| Huawei P20 lite (EMUI 8+) | GMS | Vollständig |
| Huawei P40 lite (EMUI 10) | HMS statt GMS | Vollständig |
| Huawei AppGallery | Ohne Play Store | Vollständig |
| Honor | HMS | Vollständig |
| Ohne GMS | AOSP/HMS | Vollständig |

---

## Funktionen

### Verwaltungsebenen

#### Stufe 1: Nur Überwachung (kein Device Admin)
- App-Nutzung via Accessibility Service überwachen
- Eingeschränkte Blocking-Möglichkeiten

#### Stufe 2: Device Administrator
- Deinstallationsschutz für geschützte Apps (`setUninstallBlocked`)
- Alle Stufe-1-Funktionen

#### Stufe 3: Device Owner (empfohlen)
- **App sperren** (`setPackagesSuspended`) – System verhindert Start
- **App ausblenden** (`setApplicationHidden`) – App erscheint nicht im Launcher
- **Installation sperren** (`DISALLOW_INSTALL_APPS`)
- **Unbekannte Quellen sperren** (`DISALLOW_INSTALL_UNKNOWN_SOURCES`)
- **Einstellungen einschränken** (`DISALLOW_CONFIG_WIFI`, etc.)
- **USB-Debugging sperren** (`DISALLOW_DEBUGGING_FEATURES`)

### App-Status

| Status | Bedeutung |
|--------|-----------|
| `ALWAYS_ALLOWED` | App ist immer nutzbar |
| `SCHEDULED` | App nur in konfigurierten Zeitfenstern nutzbar |
| `ADMIN_ONLY` | App nur im Admin-Modus nutzbar |
| `BLOCKED` | App ist gesperrt |
| `HIDDEN` | App ist ausgeblendet (benötigt Device Owner) |

### Zeitpläne

Zeitpläne können für einzelne Wochentage konfiguriert werden, mit mehreren Zeitfenstern pro Tag:

```
Montag–Freitag:
  06:00–07:30 → Telefon, Nachrichten
  07:30–15:00 → Schul-Apps  
  15:00–19:00 → Freizeit-Apps
  19:00–06:00 → Telefon, Nachrichten

Wochenende:
  09:00–21:00 → Freizeit-Apps
```

### Sicherheit

- Master-Code mit PBKDF2WithHmacSHA256, 100.000 Iterationen, 16-Byte zufälliges Salt
- Gespeichert in `EncryptedSharedPreferences` (Android Keystore)
- Rate Limiting: Nach 5 Fehlversuchen → 30 Minuten Sperre
- Admin-Modus Timeout: 5 Minuten Inaktivität (konfigurierbar)
- Kein Klartext-Passwort in Logs oder Speicher

---

## Architektur

```
app/                        → Haupt-App, Activities, Services, Receiver
core/
  common/                   → Gemeinsame Enums und Typen (AppStatus, Capability)
  device/                   → Geräteerkennung, CapabilityEngine
  management/               → ManagementProvider-Abstraktion
  security/                 → SecurityManager (Master-Code)
  scheduling/               → ScheduleEvaluator
  database/                 → Room-Datenbank, DAOs, Entities
feature/
  setup/                    → 8-Schritte-Einrichtungsassistent
  apps/                     → App-Liste für Benutzer
  admin/                    → Admin-Dashboard mit Code-Eingabe
  restrictions/             → Systemregeln-Verwaltung
```

### Management-Abstraktion

```
ManagementFactory
    ↓ wählt besten Provider
ManagementProvider (Interface)
    ├── DeviceOwnerProvider   → Vollständige Kontrolle via DevicePolicyManager
    ├── DeviceAdminProvider   → Eingeschränkte Kontrolle (nur Uninstall-Schutz)
    └── AccessibilityProvider → Nur Überwachung (kein System-Enforcement)
```

### Capability-Engine

Die App prüft bei jedem Start die tatsächlich verfügbaren Funktionen via API – **nicht** anhand des Hersteller-Namens:

```kotlin
capabilityEngine.check(Capability.APP_HIDING) 
// → SUPPORTED          wenn Device Owner aktiv
// → REQUIRES_DEVICE_OWNER  sonst
// → UNSUPPORTED        wenn API nicht verfügbar
```

---

## Installation

### Normale Installation

1. APK herunterladen und auf das Gerät übertragen
2. In Einstellungen → Sicherheit → "Unbekannte Quellen" temporär erlauben
3. APK installieren
4. Einrichtungsassistenten starten

### Device Owner einrichten (empfohlen, für vollständige Kontrolle)

**Voraussetzungen:**
- ADB muss installiert sein (Entwicklungscomputer)
- Gerät darf **keine Google-Konten** (oder andere Konten) haben
- GuardDroid muss bereits installiert sein

```bash
# USB-Debugging aktivieren (Einstellungen → Entwickleroptionen → USB-Debugging)
# Dann:
adb shell dpm set-device-owner dev.guarddroid.app/.receiver.GuardDroidAdminReceiver
```

**Konten entfernen falls vorhanden:**
1. Einstellungen → Konten → alle Konten entfernen
2. Dann ADB-Befehl ausführen
3. Konten können danach wieder hinzugefügt werden

**Alternativ (ohne ADB):**
- NFC-Provisioning beim ersten Gerätestart (Unternehmens-Szenario)
- QR-Code-Provisioning (Android 7+)

---

## Ersteinrichtung

Der Einrichtungsassistent führt in 8 Schritten durch:

1. **Willkommen** – Überblick über GuardDroid
2. **Geräteanalyse** – Automatische Erkennung aller Geräteeigenschaften
3. **Berechtigungen** – Nutzungsdaten, Barrierefreiheit, Device Admin
4. **Master-Code** – Sicheres Festlegen des Administrator-Codes
5. **Apps konfigurieren** – Status für jede installierte App festlegen
6. **Zeitpläne** – Nutzungszeiten definieren
7. **Systemregeln** – Installation, Einstellungen, USB-Debugging
8. **Zusammenfassung** – Überblick vor Abschluss

---

## Device Owner vs. Device Admin vs. Accessibility

| Funktion | Accessibility | Device Admin | Device Owner |
|----------|--------------|-------------|-------------|
| App-Nutzung überwachen | ✅ | ✅ | ✅ |
| Deinstallationsschutz | ❌ | ✅ | ✅ |
| App sperren (System) | ❌ | ❌ | ✅ |
| App ausblenden | ❌ | ❌ | ✅ |
| Installation sperren | ❌ | ❌ | ✅ |
| Unbekannte Quellen sperren | ❌ | ❌ | ✅ |
| Einstellungen einschränken | ❌ | ❌ | ✅ |

---

## GMS / HMS Hinweise

### Google-Geräte (mit GMS)
Alle Funktionen über offizielle Android-APIs verfügbar.

### Huawei-Geräte (mit HMS, ohne GMS)
- GuardDroid funktioniert vollständig ohne Google Play Services
- Device Owner über ADB setzbar (identischer Befehl)
- App-Sperren, Ausblenden etc. funktionieren über `DevicePolicyManager` (Android-API, nicht Google-spezifisch)
- Huawei P20 lite (Android 8, EMUI 8): Vollständig unterstützt
- Huawei P40 lite (Android 10, EMUI 10): Vollständig unterstützt

### Gerät ohne Google Play Store
- App über APK-Direktinstallation
- Alle Funktionen verfügbar (kein Play Store benötigt)

---

## Bekannte Einschränkungen

### Was GuardDroid **kann**
- Apps sperren (Device Owner, API 24+)
- Apps ausblenden (Device Owner)
- Deinstallation verhindern (Device Admin + Owner)
- Installation neuer Apps sperren (Device Owner)
- App-Nutzung überwachen (UsageStats)

### Was GuardDroid **nicht** kann
- Sich selbst zum Device Owner machen (Android-Sicherheitsgrenze)
- Root-Rechte erwerben
- Auf andere Apps' Daten zugreifen
- Internetverbindung der Apps blockieren (kein VPN in dieser Version)
- Apps blockieren ohne Device Owner oder Accessibility Service
- GuardDroid selbst kann bei Device Owner nicht deinstalliert werden

### Gerätespezifische Einschränkungen
- Einige Hersteller-Launcher zeigen ausgeblendete Apps weiterhin an
- Kiosk-Modus (Lock Task) benötigt Device Owner
- Manche EMUI-Versionen erfordern manuelle Bestätigung für Device Admin

---

## Entwicklung

### Voraussetzungen
- Android Studio Ladybug oder neuer
- JDK 17
- Android SDK mit API 35

### Build

```bash
# Debug APK
./gradlew assembleDebug

# Unit Tests
./gradlew test

# Lint
./gradlew lint

# Alle Checks
./gradlew check
```

### Projektstruktur

Das Projekt verwendet Gradle Kotlin DSL mit Version Catalog (`gradle/libs.versions.toml`).

Abhängigkeiten:
- **Hilt** für Dependency Injection
- **Room** für lokale Datenspeicherung
- **ViewBinding** für typsichere Views
- **Coroutines + Flow** für asynchrone Operationen
- **Material 3** für modernes UI
- **EncryptedSharedPreferences** für sicheren Master-Code

### Tests

```bash
# Unit Tests
./gradlew :core:scheduling:test
./gradlew :core:security:test
./gradlew :core:device:test

# Instrumented Tests (Emulator/Gerät)
./gradlew connectedAndroidTest
```

---

## Datenschutz

- Keine Netzwerkkommunikation
- Keine Analytics
- Keine Werbung
- Keine Cloud-Speicherung
- Alle Daten bleiben auf dem Gerät
- Keine Registrierung, kein Account

---

## Sicherheitsmodell

```
Master-Code
    ↓ PBKDF2WithHmacSHA256 (100.000 Iterationen)
    ↓ 16-Byte zufälliges Salt (SecureRandom)
    ↓ 32-Byte Hash
    ↓ gespeichert in EncryptedSharedPreferences (AES256-GCM)
    ↓ gesichert durch Android Keystore
```

Der Master-Code verlässt das Gerät niemals. Er wird nie im Klartext gespeichert oder übertragen.

---

## Lizenz

Apache License 2.0 – siehe [LICENSE](LICENSE)

---

## Beitragen

Dieses Projekt ist Open Source. Pull Requests sind willkommen.

Bitte beachten:
- Nur offizielle Android-APIs verwenden
- Keine Root-Exploits
- Keine Sicherheitslücken einführen
- Tests für neue Funktionen schreiben
- Keine externen Abhängigkeiten ohne gute Begründung
