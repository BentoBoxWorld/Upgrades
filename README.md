# 🎁 Upgrades Addon

<div align="center">

[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord&label=Discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.io/buildStatus/icon?job=BentoBoxWorld/Upgrades)](https://ci.codemc.io/job/BentoBoxWorld/job/Upgrades/)
[![Java Version](https://img.shields.io/badge/Java-21+-orange?logo=java)](https://www.java.com)
[![License](https://img.shields.io/github/license/BentoBoxWorld/Upgrades?color=blue)](LICENSE)

A powerful BentoBox addon that enables island upgrades for any game mode within the BentoBox ecosystem.

[Download](#-installation) • [Documentation](#-configuration) • [Report Issues](https://github.com/BentoBoxWorld/Upgrades/issues) • [Discord](https://discord.bentobox.world)

</div>

---

## 📋 Overview

**Upgrades** is a BentoBox addon that provides a comprehensive upgrade system for islands. Players can purchase enhancements to expand their island's capabilities, including island size expansion, entity and block limits, and custom command execution.

### Key Features

- 🏝️ **Island Range Expansion** - Upgrade island protection radius
- 📦 **Block Limit Upgrades** - Increase block placement limits per type
- 👥 **Entity Limits** - Expand entity spawning and placement limits
- 🎯 **Entity Group Limits** - Manage specific entity group restrictions
- ⚡ **Command Upgrades** - Execute custom commands on upgrade purchase
- 💰 **Economy Integration** - Full Vault support for monetary costs
- 🎮 **Multi-GameMode Support** - Works with BSkyBlock, AcidIsland, CaveBlock, SkyGrid, AOneBlock, and more
- ⚙️ **Highly Configurable** - Customize tiers, costs, and requirements per game mode
- 🌐 **Multilingual** - Built-in support for multiple languages

---

## 📦 Requirements

- **Minecraft**: 1.21+
- **Java**: 21+
- **BentoBox**: 3.0.0 or higher
- **Paper/Spigot**: Latest stable version recommended

### Optional Dependencies

For full functionality, install these addons:

| Addon | Purpose |
|-------|---------|
| [Vault](https://github.com/MilkBowl/Vault) | Monetary costs for upgrades |
| [Level](https://github.com/BentoBoxWorld/Level) | Island level requirements |
| [Limits](https://github.com/BentoBoxWorld/Limits) | Block and entity limit upgrades |

> **Note**: Without these dependencies, certain features will be disabled but the addon will still function.

---

## 🚀 Installation

### Quick Start

1. **Download** the latest release from the [Jenkins CI](https://ci.codemc.io/job/BentoBoxWorld/job/Upgrades/)
2. **Place** the JAR file in your `plugins/BentoBox/addons/` directory
3. **Restart** your server
4. **Configure** the addon by editing `plugins/BentoBox/addons/Upgrades/config.yml`
5. **Restart** your server again to apply changes

### Manual Installation

```bash
# 1. Clone the repository
git clone https://github.com/BentoBoxWorld/Upgrades.git
cd Upgrades

# 2. Build the project
mvn clean package

# 3. Copy the artifact to your server
cp target/Upgrades-*.jar /path/to/server/plugins/BentoBox/addons/
```

---

## ⚙️ Configuration

The addon is configured through `config.yml` located in `plugins/BentoBox/addons/Upgrades/`.

### Main Configuration Sections

#### Disabled GameModes
```yaml
disabled-gamemodes:
  - BSkyBlock  # Disable upgrades for specific game modes
```

#### Upgrade Tiers

Define upgrade progression with different tiers:

- **Range Upgrade Tiers** - Controls island protection radius expansion
- **Block Limits Upgrade Tiers** - Controls per-block-type placement limits
- **Entity Limits Upgrade Tiers** - Controls entity spawning limits
- **Entity Group Limits Upgrade Tiers** - Controls grouped entity limits
- **Command Upgrade Tiers** - Execute commands on purchase

#### GameMode-Specific Configuration
```yaml
gamemodes:
  BSkyBlock:
    range:
      tier-1:
        cost: 1000
        value: 50
```

#### Customization

- **Entity Icons** - Customize appearance of entity upgrade options
- **Entity Group Icons** - Customize grouped entity upgrade display
- **Command Icons** - Customize command upgrade presentation

---

## 💡 Usage

### For Players

1. Use the `/upgrade` command to open the upgrades panel
2. Browse available upgrades for your island
3. Click on an upgrade to purchase it (if you have sufficient funds/level)
4. Upgrades apply immediately after purchase

### For Administrators

1. Configure upgrade tiers in `config.yml`
2. Set appropriate costs and requirements
3. Customize icons and descriptions in locale files
4. Reload the addon with `/reload` if needed

---

## 🌍 Localization

Upgrade supports multiple languages through locale files in `plugins/BentoBox/addons/Upgrades/locales/`:

- 🇬🇧 English (en-US)
- 🇫🇷 French (fr)
- 🇯🇵 Japanese (ja)
- 🇵🇱 Polish (pl)
- 🇨🇳 Simplified Chinese (zh-CN)

To add a new language, create a new YAML file following the existing locale format.

---

## 🤝 Related Projects

This addon integrates with the BentoBox ecosystem. Check out other official addons:

- [BentoBox](https://github.com/BentoBoxWorld/BentoBox) - Core framework
- [Level](https://github.com/BentoBoxWorld/Level) - Island leveling system
- [Limits](https://github.com/BentoBoxWorld/Limits) - Block and entity limits
- [Visit](https://github.com/BentoBoxWorld/Visit) - Island visiting system
- [More Addons](https://github.com/BentoBoxWorld/BentoBox/blob/develop/ADDON.md)

---

## 🐛 Bug Reports & Feature Requests

Found an issue? Have an idea for an improvement? We'd love to hear from you!

- 📝 [Create an Issue](https://github.com/BentoBoxWorld/Upgrades/issues)
- 💬 [Join our Discord](https://discord.bentobox.world)
- 📖 [Check the Wiki](https://github.com/BentoBoxWorld/Upgrades/wiki)

---

## 👥 Contributing

Contributions are welcome! Whether it's bug fixes, features, or documentation improvements:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

Copyright (c) 2020-2026 Guillaume-Lebegue, tastybento, and BentoBoxWorld Contributors

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for the full text.

---

## 🙏 Credits

- **Guillaume-Lebegue** - Original author
- **tastybento** - Core BentoBox maintainer and contributor
- **BentoBox Community** - For support and feedback

---

## 📞 Support

Need help? We're here for you!

- 💬 **Discord**: [Join the BentoBox Discord](https://discord.bentobox.world)
- 📧 **Issues**: [GitHub Issues](https://github.com/BentoBoxWorld/Upgrades/issues)
- 📖 **Documentation**: Check the wiki for detailed guides

---

<div align="center">

Made with ❤️ by the BentoBox Community

</div>
