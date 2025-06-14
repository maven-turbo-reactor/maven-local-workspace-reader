package com.github.seregamorph.maven.extension.local;

import java.util.Objects;

/**
 * @author Sergey Chernov
 */
public final class GroupArtifactId implements Comparable<GroupArtifactId> {

    private final String groupId;
    private final String artifactId;

    public GroupArtifactId(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    @Override
    public int compareTo(GroupArtifactId that) {
        return toString().compareTo(that.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupArtifactId that = (GroupArtifactId) o;
        return groupId.equals(that.groupId)
                && artifactId.equals(that.artifactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId);
    }

    @Override
    public String toString() {
        return groupId + ':' + artifactId;
    }
}
