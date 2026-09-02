package io.github.zizitop13.redhotcode.hotspot

class CodeHotspot private constructor(
    val path: SourcePath,
    evidence: Map<IssueKey, DefectFix>,
) {
    private val evidenceByIssue: Map<IssueKey, DefectFix> = evidence.toMap()

    val defectCount: Int
        get() = evidenceByIssue.size

    val evidence: List<DefectFix>
        get() = evidenceByIssue.values.sortedBy { it.issue.key }

    fun record(fix: DefectFix): CodeHotspot {
        val relevantFix = fix.evidenceFor(path) ?: return this
        val updated = evidenceByIssue.toMutableMap()
        updated[fix.issue.key] = updated[fix.issue.key]?.combine(relevantFix) ?: relevantFix
        return CodeHotspot(path, updated)
    }

    fun assessWith(policy: HotspotScoringPolicy): HotspotScore = policy.assess(this)

    companion object {
        fun at(path: SourcePath): CodeHotspot = CodeHotspot(path, emptyMap())

        fun from(path: SourcePath, fixes: Iterable<DefectFix>): CodeHotspot =
            fixes.fold(at(path), CodeHotspot::record)
    }
}
