package io.github.zizitop13.redhotcode.hotspot

enum class RiskLevel {
    GREEN,
    YELLOW,
    ORANGE,
    RED,
}

data class ScoreContribution(
    val issueKey: IssueKey,
    val points: Double,
    val reason: String,
) {
    init {
        require(points.isFinite() && points >= 0.0) {
            "Contribution points must be finite and non-negative"
        }
        require(reason.isNotBlank()) { "Contribution reason must not be blank" }
    }
}

class HotspotScore(
    val points: Double,
    val riskLevel: RiskLevel,
    contributions: Iterable<ScoreContribution>,
) {
    val contributions: List<ScoreContribution> = contributions.toList()

    init {
        require(points.isFinite() && points >= 0.0) {
            "Score points must be finite and non-negative"
        }
        require(kotlin.math.abs(points - this.contributions.sumOf { it.points }) < 0.000_001) {
            "Score points must equal the sum of explainable contributions"
        }
    }
}

fun interface HotspotScoringPolicy {
    fun assess(hotspot: CodeHotspot): HotspotScore
}

data class RiskThresholds(
    val yellowAt: Double,
    val orangeAt: Double,
    val redAt: Double,
) {
    init {
        require(yellowAt.isFinite() && yellowAt > 0.0) {
            "Yellow threshold must be finite and positive"
        }
        require(orangeAt.isFinite() && orangeAt > yellowAt) {
            "Orange threshold must be greater than yellow"
        }
        require(redAt.isFinite() && redAt > orangeAt) {
            "Red threshold must be greater than orange"
        }
    }

    fun levelFor(points: Double): RiskLevel = when {
        points >= redAt -> RiskLevel.RED
        points >= orangeAt -> RiskLevel.ORANGE
        points >= yellowAt -> RiskLevel.YELLOW
        else -> RiskLevel.GREEN
    }
}

class UniqueDefectScoringPolicy(
    private val pointsPerDefect: Double,
    private val thresholds: RiskThresholds,
) : HotspotScoringPolicy {
    init {
        require(pointsPerDefect.isFinite() && pointsPerDefect > 0.0) {
            "Points per defect must be finite and positive"
        }
    }

    override fun assess(hotspot: CodeHotspot): HotspotScore {
        val contributions = hotspot.evidence.map { fix ->
            ScoreContribution(
                issueKey = fix.issue.key,
                points = pointsPerDefect,
                reason = "Unique defect ${fix.issue.key}",
            )
        }
        val points = contributions.sumOf(ScoreContribution::points)
        return HotspotScore(points, thresholds.levelFor(points), contributions)
    }
}
