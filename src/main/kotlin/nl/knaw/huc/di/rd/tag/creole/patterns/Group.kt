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
import nl.knaw.huc.di.rd.tag.creole.Constructors.group
import nl.knaw.huc.di.rd.tag.creole.Constructors.nullPattern
import nl.knaw.huc.di.rd.tag.creole.Pattern

class Group(pattern1: Pattern, pattern2: Pattern) : PatternWithTwoPatternParameters(pattern1, pattern2) {

    override fun init() {
        nullable = pattern1.isNullable && pattern2.isNullable
        allowsText = if (pattern1.isNullable)
            pattern1.allowsText() || pattern2.allowsText()
        else
            pattern1.allowsText()
        allowsAnnotations = pattern1.allowsAnnotations() || pattern2.allowsAnnotations()
        onlyAnnotations = pattern1.onlyAnnotations() && pattern2.onlyAnnotations()
    }

    override fun textDeriv(cx: Basics.Context, s: String): Pattern {
        //textDeriv cx (Group p1 p2) s =
        //  let p = group (textDeriv cx p1 s) p2
        //  in if nullable p1 then choice p (textDeriv cx p2 s)
        //                    else p
        val p = group(pattern1.textDeriv(cx, s), pattern2)
        return if (pattern1.isNullable)
            choice(p, pattern2.textDeriv(cx, s))
        else
            p
    }

    override fun startTagDeriv(qName: Basics.QName, id: Basics.Id): Pattern {
        // startTagDeriv (Group p1 p2) qn id =
        //   let d = group (startTagDeriv p1 qn id) p2
        //   in if nullable p1 then choice d (startTagDeriv p2 qn id)
        //                     else d
        val d = group(pattern1.startTagDeriv(qName, id), pattern2)
        return if (pattern1.isNullable)
            choice(d, pattern2.startTagDeriv(qName, id))
        else
            d
    }

    override fun endTagDeriv(qName: Basics.QName, id: Basics.Id): Pattern {
        // endTagDeriv (Group p1 p2) qn id =
        //   let p = group (endTagDeriv p1 qn id) p2
        //   if nullable p1 then choice p
        //                             (endTagDeriv p2 qn id)
        //                  else p
        val p = group(pattern1.endTagDeriv(qName, id), pattern2)
        return if (pattern1.isNullable)
            choice(p, pattern2.endTagDeriv(qName, id))
        else
            p
    }

    override fun startAnnotationDeriv(qName: Basics.QName): Pattern {
        val p = group(pattern1, pattern2.startAnnotationDeriv(qName))
        return if (pattern1.allowsAnnotations())
            nullPattern()
        else
            p
    }

    override fun endAnnotationDeriv(qName: Basics.QName): Pattern {
        val p = group(pattern1.endAnnotationDeriv(qName), pattern2)
        return if (pattern1.isNullable)
            choice(p, pattern2.endAnnotationDeriv(qName))
        else
            p
    }


}
