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

abstract class AbstractPattern : Pattern {
    var nullable: Boolean? = null
    var allowsText: Boolean? = null
    var allowsAnnotations: Boolean? = null
    var onlyAnnotations: Boolean? = null

    var hashcode = javaClass.hashCode()

    override
    val isNullable: Boolean
        get() {
            if (nullable == null) {
                init()
                if (nullable == null) {
                    throw RuntimeException("nullable == null! Make sure nullable is initialized in the init() of " + javaClass.simpleName)
                }
            }
            return nullable!!
        }

    internal abstract fun init()

    override fun allowsText(): Boolean {
        if (allowsText == null) {
            init()
            if (allowsText == null) {
                throw RuntimeException("allowsText == null! Make sure allowsText is initialized in the init() of "
                        + javaClass.simpleName)
            }
        }
        return allowsText!!
    }

    override fun allowsAnnotations(): Boolean {
        if (allowsAnnotations == null) {
            init()
            if (allowsAnnotations == null) {
                throw RuntimeException("allowsAnnotations == null! Make sure allowsAnnotations is initialized in the init() of "
                        + javaClass.simpleName)
            }
        }
        return allowsAnnotations!!
    }

    override fun onlyAnnotations(): Boolean {
        if (onlyAnnotations == null) {
            init()
            if (onlyAnnotations == null) {
                throw RuntimeException("onlyAnnotations == null! Make sure onlyAnnotations is initialized in the init() of "
                        + javaClass.simpleName)
            }
        }
        return onlyAnnotations!!
    }

    override fun hashCode(): Int {
        return hashcode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractPattern) return false

        if (nullable != other.nullable) return false
        if (allowsText != other.allowsText) return false
        if (allowsAnnotations != other.allowsAnnotations) return false
        if (onlyAnnotations != other.onlyAnnotations) return false
        if (hashcode != other.hashcode) return false

        return true
    }

    companion object {
        fun setHashcode(abstractPattern: AbstractPattern, hashcode: Int) {
            Preconditions.checkState(hashcode != 0, "hashCode should not be 0!")
            abstractPattern.hashcode = hashcode
        }
    }
}
