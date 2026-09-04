# NeNe Clock

A small desktop clock written in Java 21 with Swing.

The point of this repository is not the clock. It is the constraint system around it:
**every meaning has exactly one canonical implementation path, and machines — not memory —
keep it that way.**

## Status

Milestone **M0**: clock display and persisted settings, with the full quality gate active.
The stopwatch (FR-010), the countdown timer (FR-020) and the remaining tabs (FR-050) are M1
and are **not implemented yet**.

## Requirements

- JDK 21 or later
- No runtime dependencies beyond the JDK
- The Gradle Wrapper is committed; no separate Gradle installation is needed

## Build, test, run

```bash
./gradlew check   # the single definition of done — CI runs exactly this
./gradlew run     # launch the clock
```

On Windows PowerShell:

```powershell
.\gradlew.bat check
.\gradlew.bat run
```

Under WSLg the Swing window appears on the Windows desktop with no manual `DISPLAY`
configuration. Unit tests never need a display server — they run headless.

## Install and launch without Gradle

```bash
sudo tools/install-desktop-entry.sh   # /opt/nene-clock + a .desktop entry
tools/place-windows-shortcut.sh       # WSL only: copy the shortcut to the Windows desktop
```

`install-desktop-entry.sh` builds the distribution (`installDist`), copies it to
`/opt/nene-clock`, writes the icon PNGs **from the running implementation** (`AppIcon`,
via `./gradlew writeAppIcons` — no image files live in the repository), and installs
`/usr/share/applications/nene-clock.desktop`.

Under WSL that entry is enough: **WSLg turns it into a Windows Start Menu shortcut on
its own**, which can be pinned to the taskbar. `place-windows-shortcut.sh` copies that
shortcut to the Windows desktop and points it at an `.ico` built from the same icon —
WSLg's own icon has a penguin badge stamped onto it.

Both scripts are idempotent. `tools/uninstall-desktop-entry.sh` removes what they installed.

## Windows installer

Windows users do not need WSL or a JDK: download the `.msi` from the
[Releases page](https://github.com/hideyukiMORI/nene-clock/releases) and double-click it.
It installs per user (no admin prompt), adds a Start Menu entry and a desktop shortcut, and
re-installing a newer version upgrades in place. The installer is unsigned for now, so
SmartScreen shows "Windows protected your PC" on first launch — choose *More info → Run anyway*.

The installer is built by **one Gradle task** on a Windows runner ([ADR 0013](docs/adr/0013-windows-is-distributed-as-a-jpackage-msi.md)):

```bash
./gradlew packageInstaller      # Windows: app/build/installer/*.msi (+ .sha256)
                                # elsewhere: an app-image, to prove the wiring
```

`jpackage` bundles a trimmed runtime, the module set comes from `jdeps`, and the `.ico`
is written from `AppIcon` like every other icon. Pushing a `v*` tag attaches the MSI to a
GitHub Release; running the *Windows installer* workflow by hand keeps it as a run artifact.

## Project layout

```text
:app                        composition root
:ui:swing                   Swing panels and window
:adapters:system-time       the only module allowed to read the current time
:adapters:preferences       the only module allowed to touch java.util.prefs
:core:application           ports, formatting, typed outcomes
:core:domain                values, invariants, closed choices (JDK only)
:quality:architecture-tests ArchUnit rules — architecture as executable tests
```

Dependencies point inward only, and the graph is verified on every build.

## What the build actually enforces

| Layer | Enforces |
|---|---|
| `javac -Xlint:all -Werror` | warnings are errors; `sealed` + `switch` exhaustiveness |
| Gradle module graph | dependency direction, no cycles, no unapproved modules |
| forbidden-apis | **per method**: no `now()`, `nanoTime()`, `Math.random()`, default zone/locale |
| Error Prone + NullAway | `null` means exactly one thing |
| Checkstyle | structure and complexity bounds |
| ArchUnit | **per package**: core knows nothing about Swing or Preferences |
| `validateConformance` | project-specific rules (naming, suppression waivers, `default`, docs integrity) |
| JaCoCo | branch coverage floor for the two core modules |

The determinism rule is the interesting one: because `forbidden-apis` can name JDK method
signatures directly, "core may not read the wall clock" is a build failure here rather than a
review convention. Reading the current time is possible in exactly one module, and that fact is
visible as a three-line difference in `adapters/system-time/build.gradle.kts`.

## Documentation

| Document | Contents |
|---|---|
| [SPECIFICATION.md](SPECIFICATION.md) | what the product does (FR-NNN) |
| [docs/ARCHITECTURE_CONSTITUTION.md](docs/ARCHITECTURE_CONSTITUTION.md) | architectural rules (ARC-NNN) |
| [docs/CODING_RULES.md](docs/CODING_RULES.md) | Java and Swing rules (JAV-NNN, SWG-NNN) |
| [docs/QUALITY_GATES.md](docs/QUALITY_GATES.md) | which rules are mechanically enforced *today* |
| [docs/PROJECT_LAYOUT.md](docs/PROJECT_LAYOUT.md) | modules and allowed dependencies |
| [docs/DEVELOPMENT_WORKFLOW.md](docs/DEVELOPMENT_WORKFLOW.md) | how a change moves through the repo |
| [docs/adr/](docs/adr/) | decisions and what was rejected |

The normative documents are written in Japanese; code, comments and this README are in English.

## License

See [LICENSE](LICENSE).
