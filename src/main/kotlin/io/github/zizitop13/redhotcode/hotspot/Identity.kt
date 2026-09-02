package io.github.zizitop13.redhotcode.hotspot

@JvmInline
value class IssueKey(val value: String) : Comparable<IssueKey> {
    init {
        require(ISSUE_KEY.matches(value)) {
            "Issue key must have the form PROJECT-123"
        }
    }

    override fun compareTo(other: IssueKey): Int = value.compareTo(other.value)

    override fun toString(): String = value

    private companion object {
        val ISSUE_KEY = Regex("[A-Z][A-Z0-9]*-[1-9][0-9]*")
    }
}

@JvmInline
value class CommitId(val value: String) : Comparable<CommitId> {
    init {
        require(value.isNotBlank()) { "Commit id must not be blank" }
        require('\u0000' !in value) { "Commit id must not contain NUL" }
    }

    override fun compareTo(other: CommitId): Int = value.compareTo(other.value)

    override fun toString(): String = value
}

@JvmInline
value class SourcePath(val value: String) : Comparable<SourcePath> {
    init {
        require(value.isNotBlank()) { "Source path must not be blank" }
        require(!value.startsWith('/')) { "Source path must be repository-relative" }
        require('\\' !in value) { "Source path must use Git's '/' separator" }
        require('\u0000' !in value) { "Source path must not contain NUL" }
    }

    override fun compareTo(other: SourcePath): Int = value.compareTo(other.value)

    override fun toString(): String = value
}
