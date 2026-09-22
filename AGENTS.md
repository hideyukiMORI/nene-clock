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

## Choosing what to verify (QLT-013)

Select verification from the diff: run the smallest set of checks that would catch a regression in
the behaviour you changed and in its direct dependents. **If you cannot say what this change could
break, do not run that check.** The mapping from "what changed" to "what to run" is normative in
`docs/DEVELOPMENT_WORKFLOW.md` section 9.

- Docs, comments, PR text only → run nothing, except `./gradlew validateConformance` when you touched
  rule IDs, waivers, or the module graph in prose.
- Behaviour in a module → that module's `test`, plus its direct callers' `test`.
- UI → that module's `test` **and** `./gradlew run` to look at it; record what you saw
  (`check` says nothing about appearance).
- Shared foundations (Gradle setup, `config/`, `build-logic` convention plugins, JDK, dependency
  locks) → the full `./gradlew check`. State the target and the reason in one line before running it.

Reuse successful results. The identity of what was verified is `git rev-parse HEAD^{tree}`, not the
commit id. Do not re-run because of a rebase, an amend, a squash, a handover, a review round, or a
merge. Re-run only when the check's input changed, the last run failed, or a specific unverified
point remains.

Do not derail: fix failures caused by your diff; file unrelated pre-existing failures as a separate
issue with evidence and keep going. Never re-run a flaky test until it passes and call that green.

## The only definition of done

```bash
./gradlew check
```

Local and CI run exactly this task. CI runs it once per pull request — that run is the verdict.
Running the same full gate again by hand before push, at review, or at merge is not required and
is not "extra safety"; it is the same result, paid for twice.

## Required completion report

Issue and rule IDs, files and behavior changed, verification commands and results,
documentation or schema changes, active waiver IDs (or `none`), remaining risks.

Investigation-only requests do not authorize editing, committing, pushing, or opening PRs.
