package io.github.zizitop13.redhotcode.hotspot

import java.time.Instant

data class DefectIssue(val key: IssueKey)

class FixCommit(
    val id: CommitId,
    val fixedAt: Instant,
    affectedPaths: Iterable<SourcePath>,
) {
    val affectedPaths: Set<SourcePath> = affectedPaths.toSet()

    init {
        require(this.affectedPaths.isNotEmpty()) {
            "A fix commit must affect at least one source path"
        }
    }

    fun affects(path: SourcePath): Boolean = path in affectedPaths

    internal fun merge(other: FixCommit): FixCommit {
        require(id == other.id) { "Only evidence for the same commit can be merged" }
        require(fixedAt == other.fixedAt) {
            "Commit $id cannot have conflicting timestamps"
        }
        return FixCommit(id, fixedAt, affectedPaths + other.affectedPaths)
    }
}

class DefectFix private constructor(
    val issue: DefectIssue,
    commits: Map<CommitId, FixCommit>,
) {
    private val commitsById: Map<CommitId, FixCommit> = commits.toMap()

    val commits: List<FixCommit>
        get() = commitsById.values.sortedWith(compareBy(FixCommit::fixedAt, FixCommit::id))

    val affectedPaths: Set<SourcePath>
        get() = commitsById.values.flatMapTo(linkedSetOf(), FixCommit::affectedPaths)

    fun affects(path: SourcePath): Boolean = commitsById.values.any { it.affects(path) }

    fun evidenceFor(path: SourcePath): DefectFix? {
        val relevantCommits = commitsById.values.filter { it.affects(path) }
        return relevantCommits.takeIf { it.isNotEmpty() }?.let { create(issue, it) }
    }

    fun combine(other: DefectFix): DefectFix {
        require(issue.key == other.issue.key) {
            "Only fixes for the same defect can be combined"
        }

        val combined = commitsById.toMutableMap()
        other.commitsById.forEach { (id, commit) ->
            combined[id] = combined[id]?.merge(commit) ?: commit
        }
        return DefectFix(issue, combined)
    }

    companion object {
        fun create(issue: DefectIssue, commits: Iterable<FixCommit>): DefectFix {
            val byId = linkedMapOf<CommitId, FixCommit>()
            commits.forEach { commit ->
                byId[commit.id] = byId[commit.id]?.merge(commit) ?: commit
            }
            require(byId.isNotEmpty()) { "A defect fix must contain commit evidence" }
            return DefectFix(issue, byId)
        }
    }
}
