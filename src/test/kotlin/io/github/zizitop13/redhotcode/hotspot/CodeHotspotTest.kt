package io.github.zizitop13.redhotcode.hotspot

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CodeHotspotTest {
    private val service = SourcePath("src/PaymentService.kt")
    private val controller = SourcePath("src/PaymentController.kt")

    @Test
    fun `a defect is counted once even when several commits reference it`() {
        val issue = DefectIssue(IssueKey("PAY-42"))
        val first = fix(issue, commit("a1", listOf(service)))
        val followUp = fix(issue, commit("b2", listOf(service)))

        val hotspot = CodeHotspot.from(service, listOf(first, followUp, first))

        assertEquals(1, hotspot.defectCount)
        assertEquals(setOf(CommitId("a1"), CommitId("b2")), hotspot.evidence.single().commits.map { it.id }.toSet())
    }

    @Test
    fun `one multi-file fix contributes once to each affected hotspot`() {
        val fix = fix(
            DefectIssue(IssueKey("PAY-42")),
            commit("a1", listOf(service, controller)),
        )

        val serviceHotspot = CodeHotspot.from(service, listOf(fix))
        val controllerHotspot = CodeHotspot.from(controller, listOf(fix))

        assertEquals(1, serviceHotspot.defectCount)
        assertEquals(1, controllerHotspot.defectCount)
    }

    @Test
    fun `a fix for another path does not affect the hotspot`() {
        val fix = fix(DefectIssue(IssueKey("PAY-42")), commit("a1", listOf(controller)))

        val hotspot = CodeHotspot.at(service).record(fix)

        assertEquals(0, hotspot.defectCount)
        assertTrue(hotspot.evidence.isEmpty())
    }

    @Test
    fun `score is derived from unique defects and keeps its explanation`() {
        val hotspot = CodeHotspot.from(
            service,
            listOf(
                fix(DefectIssue(IssueKey("PAY-42")), commit("a1", listOf(service))),
                fix(DefectIssue(IssueKey("PAY-43")), commit("b2", listOf(service))),
            ),
        )
        val policy = UniqueDefectScoringPolicy(
            pointsPerDefect = 2.5,
            thresholds = RiskThresholds(yellowAt = 2.0, orangeAt = 5.0, redAt = 8.0),
        )

        val score = hotspot.assessWith(policy)

        assertEquals(5.0, score.points)
        assertEquals(RiskLevel.ORANGE, score.riskLevel)
        assertEquals(listOf(IssueKey("PAY-42"), IssueKey("PAY-43")), score.contributions.map { it.issueKey })
        assertEquals(score.points, score.contributions.sumOf { it.points })
    }

    private fun commit(id: String, paths: List<SourcePath>) =
        FixCommit(CommitId(id), Instant.parse("2026-01-01T10:00:00Z"), paths)

    private fun fix(issue: DefectIssue, vararg commits: FixCommit) =
        DefectFix.create(issue, commits.asIterable())
}
