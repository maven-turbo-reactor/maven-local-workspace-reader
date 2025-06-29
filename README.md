## maven-local-workspace-reader

## How it works
In so-called Maven CLI executions when we specify `plugin:goal` instead of build phase, e.g. `mvn surefire:test` Maven
will try to resolve all artifacts from the repository (first `.m2` then remote), not from local target build jar artifacts.
This may be critical if the compilation and test execution phases are separate jobs. 

How to use. Add to your `.mvn/extensions.xml`:
```xml
<extensions>
    <extension>
        <!-- https://github.com/maven-turbo-reactor/maven-local-workspace-reader -->
        <groupId>com.github.seregamorph</groupId>
        <artifactId>maven-local-workspace-reader</artifactId>
        <version>0.7</version>
    </extension>
</extensions>
```

Then you don't need to install artifacts anymore to run surefire CLI goals:
```shell
mvn clean package -DskipTests=true
mvn surefire:test -DuseLocalWorkspaceReader=true
```
