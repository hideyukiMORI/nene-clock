# NeNe Clock — Agent Guide

Short English entry point. The authoritative handbook is [CLAUDE.md](CLAUDE.md) (Japanese);
the normative documents live in [docs/](docs/).

## Project identity

- Product: NeNe Clock — a small Java 21 / Swing desktop clock
- Package root: `io.github.hideyukimori.neneclock`
- Governing principle: **one meaning, one canonical implementation path, enforced by machines**

## Required reading before changing production code

1. `SPECIFICATION.md`
2. `docs/ARCHITECTURE_CONSTITUTION.md`
3. `docs/PROJECT_LAYOUT.md`
4. `docs/CODING_RULES.md`
5. `docs/QUALITY_GATES.md`
6. `docs/DEVELOPMENT_WORKFLOW.md`
7. `docs/GLOSSARY.md`

Then the active issue, the relevant accepted ADRs, and any active waivers.

## Agent rules

- Do not invent a second implementation path because it is locally convenient.
- Do not weaken a gate to make a change pass. Fix the code instead.
- Do not mark a rule `active` in `docs/QUALITY_GATES.md` before its enforcement exists.
- Do not read the current time outside `:adapters:system-time`, or `java.util.prefs`
  outside `:adapters:preferences` — both are mechanically rejected.
- Do not write `default` in a switch; it disables the compiler's exhaustiveness check.
- Do not add `@SuppressWarnings`, lint baselines, or tool exclusions without an active waiver.
- Do not claim a command passed unless it was actually executed.
- Prefer the smallest change that fully follows the canonical path.

## The only definition of done

```bash
./gradlew check
```

Local and CI run exactly this task.

## Required completion report

Issue and rule IDs, files and behavior changed, verification commands and results,
documentation or schema changes, active waiver IDs (or `none`), remaining risks.

Investigation-only requests do not authorize editing, committing, pushing, or opening PRs.
