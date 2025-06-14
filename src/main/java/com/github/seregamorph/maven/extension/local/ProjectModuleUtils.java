package com.github.seregamorph.maven.extension.local;

import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

/**
 * @author Sergey Chernov
 */
final class ProjectModuleUtils {

    static SortedMap<GroupArtifactId, MavenProject> getProjectModules(MavenSession session) {
        SortedMap<GroupArtifactId, MavenProject> projectModules = new TreeMap<>();
        for (MavenProject project : session.getAllProjects()) {
            GroupArtifactId groupArtifactId = new GroupArtifactId(project.getGroupId(), project.getArtifactId());
            projectModules.put(groupArtifactId, project);
        }
        return projectModules;
    }

    private ProjectModuleUtils() {
    }
}
