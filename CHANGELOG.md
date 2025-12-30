# Changelog

[![English](https://img.shields.io/badge/Language-English-blue?style=logo&logo=google-translate&logoColor=white)](#)
[![Tiếng Việt](https://img.shields.io/badge/Ngôn_Ngữ-Tiếng_Việt-green?style=logo&logo=google-translate&logoColor=white)](./CHANGELOG_VN.md)

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2025-12-30

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
