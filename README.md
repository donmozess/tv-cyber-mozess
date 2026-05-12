# TV Cyber-Mozess 📺

<div align="center">

[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-GPL%203.0-blue)](LICENSE)
[![Telegram](https://img.shields.io/badge/Telegram-@cybermozess-2CA5E0?logo=telegram)](https://t.me/cybermozess)

**Application IPTV Android native pour Smartphones et TV Box — par Cyber-Mozess**

</div>

## 📡 TV Cyber-Mozess

Lecteur IPTV moderne construit avec Jetpack Compose, conçu pour :
- 📱 **Smartphones Android**
- 📺 **Android TV / TV Box**
- 🔗 **Casting DLNA** vers les appareils compatibles

**Basé sur [M3UAndroid](https://github.com/oxyroid/M3UAndroid)** — le meilleur lecteur IPTV open-source, adapté aux besoins de la communauté Cyber-Mozess.

## ✨ Fonctionnalités

- **Multi-Plateforme** - UI optimisée pour mobile et TV
- **Playlists M3U** - Support complet des playlists M3U
- **Xtream API** - Compatible avec les serveurs Xtream
- **DLNA Casting** - Diffusez vers vos appareils réseau
- **Sans Publicité** - 0 pubs, permissions minimales
- **12+ Langues** - Interface multilingue

## 📥 Téléchargement

Les APK sont générées automatiquement et livrées sur le canal Telegram [@cybermozess](https://t.me/cybermozess).

| Plateforme | Type |
|-----------|------|
| 📱 Smartphone | `app-smartphone-release.apk` |
| 📺 TV Box | `app-tv-release.apk` |

## 🛠 Stack Technique

- **Langage** - 100% Kotlin
- **UI** - Jetpack Compose + Material Design 3
- **Architecture** - MVVM modulaire
- **Async** - Kotlin Coroutines & Flow
- **Base de données** - Room
- **DI** - Hilt
- **Média** - ExoPlayer + FFmpeg

## 🌍 Langues

- 🇫🇷 Français · 🇬🇧 English · 🇨🇳 简体中文
- 🇩🇪 Deutsch · 🇮🇩 Indonesia · 🇮🇹 Italiano
- 🇧🇷 Português · 🇷🇴 Română · 🇪🇸 Español
- 🇸🇪 Svenska · 🇹🇷 Türkçe

## 🏗 Développement

```bash
# Cloner le repo
git clone https://github.com/donmozess/tv-cyber-mozess.git
cd tv-cyber-mozess

# Builder l'APK Smartphone
./gradlew :app:smartphone:assembleRelease

# Builder l'APK TV
./gradlew :app:tv:assembleRelease
```

## 📄 Licence

GNU General Public License v3.0 — voir [LICENSE](LICENSE).

---

**Cyber-Mozess** · [cybermozess.com](https://cybermozess.com) · Développé avec ❤️ par Sanogo
