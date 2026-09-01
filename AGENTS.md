# Red Hot Code Development Rules

These rules apply to all Kotlin code in this repository.

## Architecture

Follow Domain-Driven Design principles even though this is an IntelliJ plugin. Organize code around domain concepts and capabilities, not technical categories.

Prefer packages and types that express the language of the problem, for example concepts such as defect, issue, commit, change, hotspot, risk, repository history, or source location. Do not default to packages such as `util`, `helper`, `manager`, `common`, or `misc`.

Keep the domain model independent from IntelliJ Platform APIs, Git implementations, and Jira APIs whenever practical. IntelliJ-, Git-, and Jira-specific code belongs at the boundary and adapts external concepts to the domain.

Dependencies should point inward:

`IntelliJ/UI adapters + Git/Jira infrastructure -> application/use cases -> domain`

Infrastructure implementations may depend on domain/application abstractions, but domain code must not depend on infrastructure or UI code.

Do not introduce layers, interfaces, factories, repositories, or abstractions merely to satisfy a pattern. Add an abstraction only when it protects a real domain boundary, represents a meaningful role, or enables multiple implementations/testing.

## Domain model and encapsulation

Prefer rich domain objects over data bags plus procedural services.

Put behavior next to the state and invariants it owns. A caller should ask an object to perform a meaningful operation rather than retrieve internal data and manipulate it externally.

Keep mutable state private. Expose the smallest useful API. Prefer immutable values and read-only collections at boundaries.

Do not expose internal collections simply so callers can modify them. Provide domain operations instead.

Use Kotlin value classes, sealed types, data classes, and enums when they make domain concepts explicit, but do not turn every primitive into a wrapper without a concrete benefit.

Avoid anemic models such as objects containing only properties while all meaningful behavior lives in services.

## Law of Demeter

Respect the Law of Demeter: collaborate with direct dependencies rather than reaching through object graphs.

Avoid code shaped like:

```kotlin
project.repository().history().commits().first().issues().add(...)
```

Prefer intention-revealing operations on the object that owns the behavior:

```kotlin
hotspot.record(defectFix)
```

Do not expose implementation details to make another class's job easier. Move behavior to the object that has the required knowledge, or introduce a focused domain/application operation when behavior genuinely spans multiple objects.

## Naming

Names must describe a concrete responsibility or domain concept.

Do not create generic classes or objects named:

- `*Manager`
- `*Helper`
- `*Utils` / `*Util`
- `Common*`
- `Misc*`

If a type is difficult to name precisely, reconsider its responsibility and split or relocate behavior.

Prefer names such as `DefectIssue`, `IssueKey`, `DefectFix`, `CodeHotspot`, `HotspotScore`, `RepositoryHistory`, `CommitIssueLink`, or `SourceRisk` when those names match the actual responsibility.

Do not append `Service` automatically. Use `Service` only for a genuine domain/application service whose operation does not naturally belong to an entity or value object.

## Kotlin style

Write idiomatic Kotlin rather than Java translated into Kotlin.

Prefer immutable `val`, constructor injection, small focused functions, explicit nullability, and sealed hierarchies for closed variants.

Avoid unnecessary `!!`, broad `catch (Exception)`, global mutable state, companion objects as dumping grounds, disguised utility extensions, and premature concurrency.

## IntelliJ Platform boundaries

Never block the EDT with Git history scanning, Jira requests, diff processing, risk calculation, or cache persistence.

Keep UI classes thin. Project-view decorators, gutter markers, tool windows, actions, settings panels, and listeners should translate user/IDE events into application operations and render results; they should not contain correlation or scoring logic.

Use IntelliJ threading, cancellation, disposal, VFS, PSI, credential-storage, and read/write-action APIs correctly.

Never store Jira API tokens in repository files, ordinary settings XML, logs, telemetry, or exception messages. Use IntelliJ PasswordSafe.

Treat repository paths, commit messages, issue titles, and Jira data as potentially sensitive. Keep analysis local unless the user explicitly configures a remote Jira connection.

## Design tests

Tests should verify behavior and domain rules rather than implementation details.

Prefer tests that read like examples of domain language. Avoid excessive mocking. Use real domain objects and fake only true external boundaries.

When adding a new class, ask:

1. What domain or application responsibility does this class own?
2. Could this behavior belong to an existing object instead?
3. Does its name describe that responsibility precisely?
4. Does it preserve encapsulation rather than expose internals?
5. Does it depend only on the layers it should know about?

If these questions do not have clear answers, simplify the design before adding the class.
