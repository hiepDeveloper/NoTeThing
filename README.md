# NoTeThing 📝

[![English](https://img.shields.io/badge/Language-English-blue?style=logo&logo=google-translate&logoColor=white)](#)
[![Tiếng Việt](https://img.shields.io/badge/Ngôn_Ngữ-Tiếng_Việt-green?style=logo&logo=google-translate&logoColor=white)](./README_VN.md)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=logo&logo=opensourceinitiative&logoColor=white)](./LICENSE)
[![Version](https://img.shields.io/badge/Version-0.3.0-orange?style=logo&logo=github&logoColor=white)](./CHANGELOG.md)
[![Build Status](https://github.com/hiepDeveloper/NoTeThing/actions/workflows/build.yml/badge.svg)](https://github.com/hiepDeveloper/NoTeThing/actions)
[![Java](https://img.shields.io/badge/Java-21+-red?style=logo&logo=openjdk&logoColor=white)](https://openjdk.org/)

A lightweight, modern, cross-platform Sticky Notes application built with JavaFX and AtlantaFX.

## Features
- 🌙 **Dark Mode**: Toggle between Light and Dark themes with persistent settings.
- ✨ **Modern UI**: Uses AtlantaFX for a sleek and professional look.
- 📌 **Always on Top**: Keep your notes always visible on top of other windows.
- 🍷 **Opacity & Glass Effect**: Customize note transparency with a modern native "Acrylic" blur effect (Windows & Linux supported).
- ✍️ **Handwriting Fonts**: Support for beautiful handwriting fonts (Fuzzy Bubbles, Patrick Hand) with full Vietnamese support.
- 🎨 **Adaptive Themes**: 7 curated color themes with adaptive headers and backgrounds.
- 💾 **Auto-save**: Never lose your thoughts; content is saved automatically.
- 📦 **Note Manager**: Easily manage all your notes in one centralized list.
- 🖼️ **Frameless Design**: Clean, draggable, and resizable interface.

## 🚀 Quick Start (Portable Version)
For a quick experience without installing Java, you can use the Portable version:
1. Go to the [Releases](https://github.com/hiepDeveloper/NoTeThing/releases) page.
2. Download the Portable zip file corresponding to your operating system (**Windows** or **Linux**).
3. Extract the zip and run the launcher:
   - **Windows**: Run **`NoTeThing.exe`**.
   - **Linux**: Run the **`NoTeThing`** executable inside the folder.
   > **Note for Linux Users**: To enable the beautiful Blur/Glass effect, run the `setup_linux_blur_en.sh` script or read [LINUX_BLUR_GUIDE_EN.md](./BlurEffectForLinux/LINUX_BLUR_GUIDE_EN.md).


## Prerequisites
- **Java**: JDK 21 or higher.
- **Maven**: 3.8 or higher.

## Getting Started
### Clone the project
```bash
git clone https://github.com/hiepDeveloper/NoTeThing.git
cd NoTeThing/notething
```

### Run the application
```bash
mvn clean javafx:run
```

## Built With
- [JavaFX](https://openjfx.io/) - The GUI framework.
- [AtlantaFX](https://github.com/mkpaz/atlantafx) - Modern JavaFX theme library.
- [Ikonli](https://github.com/kordamp/ikonli) - Bootstrap Icons for the interface.
- [JNA](https://github.com/java-native-access/jna) - Java Native Access for advanced window management.

## License
This project is licensed under the MIT License - see the LICENSE file for details.
