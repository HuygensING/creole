package nl.knaw.huc.di.rd.tag.creole.patterns

/*-
 * #%L
 * creole
 * =======
 * Copyright (C) 2016 - 2019 HuC DI (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import com.google.common.base.Preconditions
import nl.knaw.huc.di.rd.tag.creole.Pattern

abstract class PatternWithTwoPatternParameters internal constructor(val pattern1: Pattern, val pattern2: Pattern) : AbstractPattern() {

    init {
        Preconditions.checkNotNull(pattern1)
        Preconditions.checkNotNull(pattern2)
        Companion.setHashcode(this, javaClass.hashCode() + pattern1.hashCode() * pattern2.hashCode())
    }

    override fun equals(other: Any?): Boolean {
        return (other!!.javaClass == this.javaClass
                && pattern1 == (other as PatternWithTwoPatternParameters).pattern1
                && pattern2 == other.pattern2)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + pattern1.hashCode()
        result = 31 * result + pattern2.hashCode()
        return result
    }
}
