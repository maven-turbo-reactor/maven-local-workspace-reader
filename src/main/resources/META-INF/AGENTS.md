# maven-local-workspace-reader: notes for AI agents

This file ships inside the `maven-local-workspace-reader` JAR (`META-INF/AGENTS.md`). It is for AI coding agents
working in a project that has this Maven core extension enabled.

Source and docs: https://github.com/maven-turbo-reactor/maven-local-workspace-reader

## What it does

The extension is a Maven core extension (a `RepositorySessionDecorator`). When enabled, it changes how artifacts of
**modules in the current reactor** are resolved. Maven then uses the build output in the module's `target` directory
before it looks in `~/.m2` or a remote repository.

Without it, Maven resolves sibling modules from `~/.m2` or a remote repository in these cases:
- plugin goals invoked directly, e.g. `mvn surefire:test` (no lifecycle phase)
- module subsets, e.g. `mvn verify -pl :moduleA`
- several git worktrees of one project that share one `~/.m2`

That means stale or missing artifacts, or artifacts built from another worktree.

## Enabling it

1. The project lists the extension in `.mvn/extensions.xml`:
   ```xml
   <extensions>
       <extension>
           <groupId>com.github.seregamorph</groupId>
           <artifactId>maven-local-workspace-reader</artifactId>
           <version><!-- see the project README for the latest version --></version>
       </extension>
   </extensions>
   ```
2. The extension does nothing unless the property `useLocalWorkspaceReader` is exactly `true`. Pass it on the command
   line (`-DuseLocalWorkspaceReader=true`) or add it to `.mvn/maven.config` to turn it on for every build.

To check that it is active, look for log lines like `Decorating <project> with local reactor repository`.

## Resolution rules

These rules apply only to artifacts whose `groupId:artifactId` belongs to a project in the current reactor:

| Requested artifact | Resolved to |
|--------------------|-------------|
| `pom`              | the module's `pom.xml` |
| `jar`, when Maven's default reader already points at a directory (e.g. `target/classes` after `mvn compile`) | that directory |
| `jar`, otherwise   | `${project.build.directory}/${project.build.finalName}[-classifier].jar`, if that file exists |
| anything else, or the file is missing | Maven's default resolution |

If the expected JAR is missing and the build includes `package`, `install`, or `deploy`, the extension logs a warning
(`Local artifact ... not found in ..., expected at ..., fallback to default resolution`) and uses default resolution.

## Typical commands

```shell
# Build once, then run tests as a separate step without `mvn install`
mvn clean package -DskipTests=true
mvn surefire:test -DuseLocalWorkspaceReader=true

# Build or test a subset of modules against the sibling modules built in this checkout
mvn verify -pl :moduleA -DuseLocalWorkspaceReader=true
```

## Things to keep in mind

- **Stale output wins.** The extension uses whatever is in `target/` right now and does not check whether it is up
  to date. After changing a dependency module, rebuild it (e.g. `mvn package -pl :dep -am`) before running goals
  against its dependents. If results look wrong, suspect stale JARs in `target/` before you suspect the extension.
- **`mvn clean` removes what the extension relies on.** After a clean, resolution falls back to `~/.m2` or remote
  until the modules are rebuilt.
- **Only reactor modules are affected.** External dependencies resolve as usual.
- **Custom `finalName` or `directory` is fine.** The JAR path comes from `project.build.finalName` and
  `project.build.directory`.
- **You usually don't need `mvn install`.** It only puts artifacts in `~/.m2` so other modules can find them, and
  with this extension enabled they are found in `target/`. Skipping install also keeps other worktrees from picking up
  this checkout's artifacts.
