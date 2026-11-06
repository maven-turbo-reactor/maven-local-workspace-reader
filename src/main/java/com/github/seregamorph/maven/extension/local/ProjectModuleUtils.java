package com.github.seregamorph.maven.extension.local;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.jspecify.annotations.Nullable;

/**
 * @author Sergey Chernov
 */
final class ProjectModuleUtils {

    @Nullable
    static SortedMap<GroupArtifactId, MavenProject> getProjectModulesOrNull(MavenSession session) {
        List<MavenProject> allProjects = session.getAllProjects();
        if (allProjects == null) {
            // This usually should not happen, but at least there is one known scenario: IDEA reimport since
            // version 2026.3. See #2
            return null;
        }
        SortedMap<GroupArtifactId, MavenProject> projectModules = new TreeMap<>();
        for (MavenProject project : allProjects) {
            GroupArtifactId groupArtifactId = new GroupArtifactId(project.getGroupId(), project.getArtifactId());
            projectModules.put(groupArtifactId, project);
        }
        return projectModules;
    }

    private ProjectModuleUtils() {
    }
}
