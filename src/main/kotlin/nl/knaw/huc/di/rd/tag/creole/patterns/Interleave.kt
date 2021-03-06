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
import nl.knaw.huc.di.rd.tag.creole.Basics
import nl.knaw.huc.di.rd.tag.creole.Constructors.choice
import nl.knaw.huc.di.rd.tag.creole.Constructors.interleave
import nl.knaw.huc.di.rd.tag.creole.Pattern

class Interleave(pattern1: Pattern, pattern2: Pattern) : PatternWithTwoPatternParameters(pattern1, pattern2) {

    override fun init() {
        nullable = pattern1.isNullable && pattern2.isNullable
        allowsText = pattern1.allowsText() || pattern2.allowsText()
        allowsAnnotations = pattern1.allowsAnnotations() || pattern2.allowsAnnotations()
        onlyAnnotations = pattern1.onlyAnnotations() && pattern2.onlyAnnotations()
    }

    override fun textDeriv(cx: Basics.Context, s: String): Pattern {
        //textDeriv cx (Interleave p1 p2) s =
        //  choice (interleave (textDeriv cx p1 s) p2)
        //         (interleave p1 (textDeriv cx p2 s))
        return choice(
                interleave(pattern1.textDeriv(cx, s), pattern2),
                interleave(pattern1, pattern2.textDeriv(cx, s))
        )
    }

    override fun startTagDeriv(qn: Basics.QName, id: Basics.Id): Pattern {
        // startTagDeriv (Interleave p1 p2) qn id =
        //   choice (interleave (startTagDeriv p1 qn id) p2)
        //          (interleave p1 (startTagDeriv p2 qn id))
        return choice(
                interleave(pattern1.startTagDeriv(qn, id), pattern2),
                interleave(pattern1, pattern2.startTagDeriv(qn, id))
        )
    }

    override fun startTagOpenDeriv(qn: Basics.QName, id: Basics.Id): Pattern {
        return choice(
                interleave(pattern1.startTagOpenDeriv(qn, id), pattern2),
                interleave(pattern1, pattern2.startTagOpenDeriv(qn, id))
        )
    }

    override fun endTagDeriv(qn: Basics.QName, id: Basics.Id): Pattern {
        // endTagDeriv (Interleave p1 p2) qn id =
        //   choice (interleave (endTagDeriv p1 qn id) p2)
        //          (interleave p1 (endTagDeriv p2 qn id))
        return choice(
                interleave(pattern1.endTagDeriv(qn, id), pattern2),
                interleave(pattern1, pattern2.endTagDeriv(qn, id))
        )
    }
}
