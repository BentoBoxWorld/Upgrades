Upgrades Addon
==========
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.io/buildStatus/icon?job=BentoBoxWorld/Upgrades)](https://ci.codemc.io/job/BentoBoxWorld/job/Upgrades/)

## About
Add-on for BentoBox to provide Upgrades to an island of any BentoBox GameMode.

By default, this addon includes upgrades for island size, limits and commands

## Soft Dependence

Upgrades addon can hook onto Vault as well as limits addon and level addon :
* Without Vault, Money cost will be ignored.
* Without Level addonn, Island level will be ignored
* Without Limits addon, Limits upgrades will not be displayed

## Where to find

Currently Upgrades Addon is in **Alpha stage**, so it may or may not contain bugs.

You can download the last snapshot [here](https://ci.codemc.io/job/BentoBoxWorld/job/Upgrades/)

If you like this addon, but something is missing or is not working as you want, you can always submit an [Issue request](https://github.com/BentoBoxWorld/Upgrades/issues)

## How to use

1. Download last snapshot [here](https://ci.codemc.io/job/BentoBoxWorld/job/Upgrades/).
2. Then add it to BentoBox Addons.
3. Restart the server.
4. Edit the Config.yml how you want.
5. Restart the server.

## Config.yml

The config.yml has the following sections:

* **Disabled GameModes** - specify Game Modes where islandUpgrades will not work.
* **Range Upgrade Tiers** - specify default Range upgrade tiers. Upgrading this will augment island protection size
* **Block Limits Upgrade Tier** - specify default Block Limits tiers. Upgrading this will augment the block limits of the limits addon
* **Entity Limits Upgrade Tier** - specify default Entity Limits tiers. Upgrading this will augment the entity limits of the limits addon
* **Command Upgrade Tier** - specify default Command Tiers. You can link command to this upgrade.
* **GameMode** - ability to specify upgrade tiers for specific game mode.
* **Entity Icon** - This list the icons for Entity Upgrades
* **Command Icon** - This list the icons for Command Upgrades

### Other Add-ons

Upgrades is an add-on that uses the BentoBox API. Here are some other ones that you may be interested in:

* [**Addons**](https://github.com/BentoBoxWorld/BentoBox/blob/develop/ADDON.md)

Bugs and Feature requests
=========================
File bug and feature requests here: https://github.com/BentoBoxWorld/Upgrades/issues
