package com.github.seregamorph.maven.extension.local;

import static com.github.seregamorph.maven.extension.local.MavenPropertyUtils.getProperty;
import static com.github.seregamorph.maven.extension.local.MavenPropertyUtils.isTrue;

import java.io.File;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.RepositorySessionDecorator;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instead of default resolving built artifacts in repositories (either local or remote) Maven will first check
 * module target directory.
 *
 * @author Sergey Chernov
 */
@Named
public class LocalReactorRepositorySessionDecorator implements RepositorySessionDecorator {

    private static final Logger logger = LoggerFactory.getLogger(LocalReactorRepositorySessionDecorator.class);

    private final MavenSession mavenSession;

    @Inject
    public LocalReactorRepositorySessionDecorator(MavenSession mavenSession) {
        this.mavenSession = mavenSession;
    }

    @Override
    public RepositorySystemSession decorate(MavenProject project, RepositorySystemSession session) {
        boolean useLocalWorkspaceReader = isTrue(getProperty(mavenSession, "useLocalWorkspaceReader"));
        if (useLocalWorkspaceReader) {
            WorkspaceReader originalWorkspaceReader = session.getWorkspaceReader();
            logger.info("Decorating {} with local reactor repository", project);
            DefaultRepositorySystemSession delegate = new DefaultRepositorySystemSession(session);
            Map<GroupArtifactId, MavenProject> modules = ProjectModuleUtils.getProjectModules(project);
            delegate.setWorkspaceReader(new LocalReactorWorkspaceReader(originalWorkspaceReader, modules));
            return delegate;
        } else {
            return null;
        }
    }

    static class LocalReactorWorkspaceReader implements WorkspaceReader {

        private final WorkspaceReader delegate;
        private final Map<GroupArtifactId, MavenProject> modules;

        public LocalReactorWorkspaceReader(WorkspaceReader delegate, Map<GroupArtifactId, MavenProject> modules) {
            this.delegate = delegate;
            this.modules = modules;
        }

        @Override
        public WorkspaceRepository getRepository() {
            return delegate.getRepository();
        }

        @Override
        public File findArtifact(Artifact artifact) {
            GroupArtifactId groupArtifactId = new GroupArtifactId(artifact.getGroupId(), artifact.getArtifactId());
            MavenProject project = modules.get(groupArtifactId);
            if (project != null) {
                File localFile = findLocalFileCandidate(artifact, project);
                if (localFile.exists()) {
                    logger.debug("Found local artifact {} in {}", artifact, project);
                    return localFile;
                } else {
                    logger.warn("Local artifact {} not found in {}, expected at {}, fallback to default resolution",
                        artifact, project, localFile);
                }
            }
            return delegate.findArtifact(artifact);
        }

        // never null, but may be non existing
        private static File findLocalFileCandidate(Artifact artifact, MavenProject project) {
            if ("pom".equals(artifact.getExtension())) {
                return project.getFile();
            } else if ("jar".equals(artifact.getExtension())) {
                String fileName = project.getBuild().getFinalName();
                String classifier = artifact.getClassifier();
                if (classifier != null && !classifier.isEmpty()) {
                    fileName += "-" + classifier;
                }
                fileName += ".jar";
                return new File(project.getBuild().getDirectory(), fileName);
            } else {
                throw new UnsupportedOperationException("Unsupported artifact extension: " + artifact.getExtension());
            }
        }

        @Override
        public List<String> findVersions(Artifact artifact) {
            return delegate.findVersions(artifact);
        }
    }
}
