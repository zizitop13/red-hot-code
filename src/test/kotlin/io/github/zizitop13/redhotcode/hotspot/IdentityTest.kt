package io.github.zizitop13.redhotcode.hotspot

import kotlin.test.Test
import kotlin.test.assertFailsWith

class IdentityTest {
    @Test
    fun `issue keys reject ambiguous and malformed values`() {
        listOf("", "pay-42", "PAY", "PAY-0", "PAY-01", "PAY 42").forEach { value ->
            assertFailsWith<IllegalArgumentException>(value) { IssueKey(value) }
        }
    }

    @Test
    fun `source paths are repository-relative Git paths`() {
        listOf("", "/src/Main.kt", "src\\Main.kt").forEach { value ->
            assertFailsWith<IllegalArgumentException>(value) { SourcePath(value) }
        }
    }
}
