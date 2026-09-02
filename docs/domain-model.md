# Defect hotspot domain model

Phase 1 defines the language and invariants used by later Git, Jira, scoring, and UI adapters. The domain package has no IntelliJ Platform, Git client, or Jira client dependency.

## Evidence model

- `DefectIssue` identifies one defect by `IssueKey`.
- `FixCommit` records when one commit changed one or more repository-relative `SourcePath` values.
- `DefectFix` groups all commit evidence for one defect. Repeated occurrences of the same commit ID are merged, while conflicting timestamps are rejected.
- `CodeHotspot` is evidence for one current path. It counts unique issue keys, never commit-message occurrences or commits.
- `HotspotScore` is derived data. Its points must equal its `ScoreContribution` values so every result remains explainable.

The source tree and commit history remain the facts. A score is a replaceable projection over those facts.

## History semantics

Phase 1 deliberately does not infer file identity across history:

- A `SourcePath` is the path recorded at one history point. A rename does not make its old and new values equal. Phase 4 must provide an explicit, bounded rename-following policy before combining them into one current-file hotspot.
- Evidence for a deleted path can remain in `DefectFix`, but it does not produce a current `CodeHotspot` unless the current-tree correlation supplies that path.
- A merge commit is not automatically a defect fix. A later Git adapter must decide whether to use the merge diff or its constituent commits and must not count both representations of the same change.
- A revert is not automatically a new defect. It remains distinct commit evidence until the correlation policy can identify and classify revert relationships.
- Repeated issue references and multiple fix commits for one issue never increase the unique defect count. They remain visible as supporting evidence.

These constraints prevent early convenience assumptions from silently inflating hotspot counts.

## Initial score

`UniqueDefectScoringPolicy` is intentionally small: each unique defect contributes an explicitly configured number of points, and explicitly configured thresholds map the total to a risk level. It exists to validate the evidence/derived-score boundary; Phase 5 will evaluate recency, severity, churn, and normalization without changing the evidence model.
