# Changelog

[![English](https://img.shields.io/badge/Language-English-blue?style=logo&logo=google-translate&logoColor=white)](#)
[![Tiếng Việt](https://img.shields.io/badge/Ngôn_Ngữ-Tiếng_Việt-green?style=logo&logo=google-translate&logoColor=white)](./CHANGELOG_VN.md)

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.2] - 2026-01-05 - Native Linux Blur & Optimization

### Added
- **Native Linux Blur Control**: Mirrored system-level control via a Rust library, replacing the previous script-based approach.
    - **Functional Toggle**: Linux users can now enable/disable blur effects directly in Settings without restarts.
    - **Wide Compatibility**: Automated detection and support for KDE (KWin), Picom (X11), and GNOME (Mutter).
    - **Intelligent Retry Logic**: Smoothly handles window initialization delays (XID retrieval) to ensure blur is applied reliably.
- **Dynamic Blur Customization**: Leverage the Opacity slider to adjust the depth and "thickness" of the glass effect on Linux.

### Changed
- **Settings Visibility**: Unlocked visual effect settings for Linux users, providing full UI parity across platforms.

## [0.3.1] - 2026-01-04 - Hotfix & Linux Optimization

### Fixed
- **Cloud Sync Build**: Resolved an issue where the distributed build (artifact) failed to initialize cloud synchronization due to missing resource configurations.
- **Linux Compatibility**:
    - **Adaptive UI**: Automatically hide the "Acrylic/Blur" toggle in Settings when running on Linux (as this is managed by the OS compositor).
    - **Detailed Documentation**: Added comprehensive guides and automatic setup scripts for enabling Blur on Hyprland, KDE, GNOME, and Picom (X11).

## [0.3.0] - 2026-01-03 - Cloud Sync, Trash Bin & Settings

### Added
- **Cloud Synchronization**: Seamlessly sync notes across devices using Google Firebase (Realtime Database).
    - Requires `serviceAccountKey.json` configuration.
    - Secure login/logout flow.
- **Trash Bin & Recovery**: 
    - **Soft Delete**: Notes moved to trash can be restored.
    - **Safety First**: Confirmation dialogs for deletion.
    - **Trash View**: Dedicated view to manage deleted notes.
- **Centralized Settings**: New configuration interface in the Note List.
    - **Theme System**: Option to sync with **System Theme** (default), or force Light/Dark.
    - **Visual Control**: Toggles for **Acrylic (Glass)** effect and **Auto-hide Title** (both Enabled by default).
- **Enhanced Typography**: Integrated **Mali** font family with full support for **Bold**, *Italic*, and ***BoldItalic*** styles.

## [0.2.1] - 2026-01-01 - Fix UI & Linux support

### Fixed
- **Theme Synchronization**: Resolved an issue where theme changes from the Note List weren't applied to note windows until opacity was manually adjusted.
- **Glass Effect Refinement**: Improved blur stability and corrected corner rounding logic for better visual consistency.
- **Linux Compatibility**: Enhanced background blur and window interaction performance on Linux systems.
- **UI & Interaction**:
    - Optimized note window corner radius to 7px for a sleeker profile.
    - Adjusted minimum window width to 220px for better flexibility.
    - Improved resize grabbing sensitivity and native event handling stability.

## [0.2.0] - 2025-12-31 - Update custom UI

### Added
- **Dark Mode Support**: Full system-wide theme switching with persistent settings and real-time updates across all windows.
- **Transparency & Glass Effect**: Individual note opacity control (30% - 100%) featuring native Windows Acrylic "frosted glass" effect.
- **Handwriting Fonts**: Authentic sticky note aesthetic using "Fuzzy Bubbles" and premium Vietnamese-supported fonts (Patrick Hand, Itim).
- **Expanded Color Themes**: 7 curated adaptive color palettes (Yellow, Green, Blue, Orange, Red, Purple, Teal) tailored for both Light and Dark modes.

### Changed
- **Modernized UI Components**: Redesigned drag handles, improved popup aesthetics with 12px rounding, and theme-adaptive backgrounds.
- **Enhanced Note List**: Wider layout (400px) and improved font rendering for better readability.
- **Responsive Interactivity**: Smart action buttons and improved slider UX with expanded hit areas and visual feedback.

## [0.1.0] - 2025-12-30 - Initial Release

### Added
- **Core Engine**: Initial release of NoTeThing sticky notes application.
- **Modern UI**: Integrated AtlantaFX (Primer Light theme) for a sleek, professional interface.
- **Note Management**: 
    - Full CRUD support for notes.
    - Centralized "Note List" window to manage all existing notes.
- **Window Features**:
    - **Always on Top**: Ability to pin notes above other windows using JNA for native integration.
    - **Frameless/Transparent**: Modern frameless window design.
    - **Custom Resize**: Multi-platform resizing support (Native Windows via JNA and JavaFX fallback).
    - **Smart Header**: Auto-hiding header/toolbar that appears only when focused.
- **Persistence**: 
    - Automatic saving and loading of note content, position, and window size.
    - Data stored locally in `.notething` directory.
- **Visuals**: Bootstrap Icons integration via Ikonli.
- **Documentation**: 
    - Dual-language README (English & Vietnamese).
    - MIT License.
    - Copyright headers in source files.
