package nl.knaw.huc.di.rd.tag.creole.patterns

/*
     * #%L
 * creole
 * =======
 * Copyright (C) 2016 - 2019 HuC DI (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
     */

import nl.knaw.huc.di.rd.tag.creole.Constructors.choice
import nl.knaw.huc.di.rd.tag.creole.Constructors.concurOneOrMore
import nl.knaw.huc.di.rd.tag.creole.Constructors.concurZeroOrMore
import nl.knaw.huc.di.rd.tag.creole.Constructors.mixed
import nl.knaw.huc.di.rd.tag.creole.Constructors.range
import nl.knaw.huc.di.rd.tag.creole.NameClasses.anyName
import nl.knaw.huc.di.rd.tag.creole.Pattern

object Patterns {
    val EMPTY: Pattern = Empty()
    val NOT_ALLOWED: Pattern = NotAllowed()
    val TEXT: Pattern = Text()
    val WELLFORMED0 = concurZeroOrMore(mixed(range(anyName(), TEXT)))
    val WELLFORMED = concurOneOrMore(range(anyName(), choice(TEXT, WELLFORMED0)))

    /*
  A Pattern represents a pattern after simplification.

  data Pattern = Empty
               | NotAllowed
               | Text
               | Choice Pattern Pattern
               | Interleave Pattern Pattern
               | Group Pattern Pattern
               | Concur Pattern Pattern
               | Partition Pattern
               | OneOrMore Pattern
               | ConcurOneOrMore Pattern
               | Range NameClass Pattern
               | EndRange QName Id
               | After Pattern Pattern
               | All Pattern Pattern
               | Atom Pattern
               | EndAtom
               | Annotation NameClass Pattern
               | EndAnnotation NameClass
   */
}
