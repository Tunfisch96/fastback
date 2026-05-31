# FastBack

FastBack is a Minecraft mod that provides fast, incremental world backups
powered by Git.

Rather than creating full archive copies of a world, FastBack stores
incremental snapshots and only saves files that have changed. This allows
frequent backups while minimizing storage usage and backup time. FastBack
supports both local and remote backup targets and is intended for use by
single-player users and server operators alike. :contentReference[oaicite:0]{index=0}

## Design Philosophy

When making changes, prioritize:

1. Data integrity above all else.
2. Reliability over convenience.
3. Predictable backup and restore behavior.
4. Performance and scalability.
5. Backwards compatibility.

A backup that is slower but reliable is preferable to a backup that is fast
but risks data loss.

Avoid features that compromise backup correctness or make restore operations
more difficult to understand.

Favor simple and robust implementations over clever optimizations.

## Project Structure

This project supports multiple mod loaders.

- `common/` contains shared backup and Git logic.
- `fabric/` contains Fabric-specific code.
- `neoforge/` contains NeoForge-specific code.
- `docs/` contains user documentation.

Whenever possible, shared functionality should live in `common/`.

Loader-specific code should remain isolated to the appropriate platform
module.

## Repository Scope

This repository contains the source code for FastBack.

Only inspect files tracked by git.

Ignore:

- `build/`
- `.gradle/`
- `run/`
- `logs/`
- generated resources
- IDE metadata
- temporary files
- crash reports
- test output
- backup repositories created during testing

Do not spend time analyzing generated files or build outputs.

## Cost-Aware Development

Repository-wide scans are expensive and should be avoided.

Before exploring the repository:

- Prefer targeted analysis.
- Read only files likely to be relevant.
- Start from files explicitly mentioned in the task.
- Follow references outward only as needed.
- Do not read entire directory trees unless necessary.

When discovering files, prefer:

```bash
git ls-files
