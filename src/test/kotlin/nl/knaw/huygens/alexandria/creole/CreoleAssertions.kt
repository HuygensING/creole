package nl.knaw.huygens.alexandria.creole

import org.assertj.core.api.Assertions

object CreoleAssertions : Assertions() {
    fun assertThat(actual: Pattern?): PatternAssert {
        return PatternAssert(actual!!)
    }

    fun assertThat(actual: ValidationResult?): ValidationResultAssert {
        return ValidationResultAssert(actual!!)
    }

}