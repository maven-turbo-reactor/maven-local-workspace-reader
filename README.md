## maven-local-workspace-reader

## How it works
In so-called Maven CLI executions when we specify `plugin:goal` instead of build phase, e.g. `mvn surefire:test` Maven
will try to resolve all artifacts from the repository (first `.m2` then remote), not from local target build jar artifacts.
This may be critical if the compilation and test execution phases are separate jobs.

Other use-cases include building module subsets like `mvn verify -pl :moduleA` or `mvn verify -pl :moduleA -am`.

Also this extension helps a lot with several worktrees used for the same project as it always prefers JAR artifact
assemblies from the current directory rather than from the shared .m2.

How to use. Add to your `.mvn/extensions.xml`:
```xml
<extensions>
    <extension>
        <!-- https://github.com/maven-turbo-reactor/maven-local-workspace-reader -->
        <groupId>com.github.seregamorph</groupId>
        <artifactId>maven-local-workspace-reader</artifactId>
        <version>0.8</version>
    </extension>
</extensions>
```

Then you don't need to install artifacts anymore to run surefire CLI goals:
```shell
mvn clean package -DskipTests=true
mvn surefire:test -DuseLocalWorkspaceReader=true
```
