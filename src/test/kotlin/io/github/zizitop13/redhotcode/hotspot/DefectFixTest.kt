package io.github.zizitop13.redhotcode.hotspot

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DefectFixTest {
    private val issue = DefectIssue(IssueKey("PAY-42"))
    private val service = SourcePath("src/PaymentService.kt")
    private val controller = SourcePath("src/PaymentController.kt")

    @Test
    fun `a fix retains all commits and affected files`() {
        val fix = DefectFix.create(
            issue,
            listOf(
                commit("a1", "2026-01-01T10:00:00Z", listOf(service)),
                commit("b2", "2026-01-02T10:00:00Z", listOf(controller)),
            ),
        )

        assertEquals(listOf(CommitId("a1"), CommitId("b2")), fix.commits.map { it.id })
        assertEquals(setOf(service, controller), fix.affectedPaths)
    }

    @Test
    fun `repeated evidence for one commit merges paths without duplicating the commit`() {
        val fix = DefectFix.create(
            issue,
            listOf(
                commit("a1", "2026-01-01T10:00:00Z", listOf(service)),
                commit("a1", "2026-01-01T10:00:00Z", listOf(controller)),
            ),
        )

        assertEquals(1, fix.commits.size)
        assertEquals(setOf(service, controller), fix.commits.single().affectedPaths)
    }

    @Test
    fun `the same commit id cannot carry conflicting timestamps`() {
        assertFailsWith<IllegalArgumentException> {
            DefectFix.create(
                issue,
                listOf(
                    commit("a1", "2026-01-01T10:00:00Z", listOf(service)),
                    commit("a1", "2026-01-02T10:00:00Z", listOf(controller)),
                ),
            )
        }
    }

    @Test
    fun `path-specific evidence does not expose unrelated changes`() {
        val fix = DefectFix.create(
            issue,
            listOf(
                commit("a1", "2026-01-01T10:00:00Z", listOf(service, controller)),
                commit("b2", "2026-01-02T10:00:00Z", listOf(controller)),
            ),
        )

        val serviceEvidence = requireNotNull(fix.evidenceFor(service))

        assertEquals(listOf(CommitId("a1")), serviceEvidence.commits.map { it.id })
        assertTrue(serviceEvidence.commits.single().affectedPaths.contains(service))
    }

    @Test
    fun `mutable input collections cannot alter recorded evidence`() {
        val paths = mutableListOf(service)
        val commit = FixCommit(CommitId("a1"), Instant.parse("2026-01-01T10:00:00Z"), paths)
        val commits = mutableListOf(commit)
        val fix = DefectFix.create(issue, commits)

        paths += controller
        commits += commit("b2", "2026-01-02T10:00:00Z", listOf(controller))

        assertEquals(setOf(service), fix.affectedPaths)
        assertEquals(listOf(CommitId("a1")), fix.commits.map { it.id })
    }

    private fun commit(id: String, at: String, paths: List<SourcePath>) =
        FixCommit(CommitId(id), Instant.parse(at), paths)
}
