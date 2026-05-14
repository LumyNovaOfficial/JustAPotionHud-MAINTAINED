# JustAPotionHUD-MAINTAINED

`JustAPotionHUD-MAINTAINED` is a maintained Fabric continuation of the original `JustPotionHUD`.

Official original mod:
- https://modrinth.com/mod/justpotionhud

This project was rebuilt from scratch because I did not have access to the original source code. I tried contacting the original owner and did not receive a reply, so this version was remade independently to keep the mod usable on newer Minecraft versions.

## What This Project Is

- A clean potion HUD replacement for Fabric
- Focused on modern Minecraft `1.21.x`
- Maintained with separated version targets instead of forcing one broken jar across too many versions
- Built to preserve texture pack compatibility for potion icons

## Supported Builds

Current project layout:

- `fabric-1.21.1` for `1.21` to `<1.21.2`
- `fabric-1.21.4` for `1.21.2` to `<1.21.6`
- `fabric-1.21.6-1.21.8` for `1.21.6` to `<1.21.9`
- `fabric-1.21.10` for `1.21.9` to `<1.21.11`
- `fabric-1.21.11` for `1.21.11`

This split exists because Minecraft client rendering APIs changed between these versions, and keeping them separated is more reliable than pretending one binary works everywhere.

## Notes

- Potion icons are rendered using Minecraft's own effect sprite handling where required, so resource packs can still replace effect icons correctly.
- This project is a maintenance-oriented continuation, not the official original release.
- The goal here is compatibility and stability first.

## Build

Requires:

- Java 21

Build all versions:

```bash
./gradlew build --no-daemon
```

## License

This repository is licensed under the MIT License.
