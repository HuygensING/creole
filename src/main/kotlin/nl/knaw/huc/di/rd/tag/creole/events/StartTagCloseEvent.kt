package nl.knaw.huc.di.rd.tag.creole.events

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
import nl.knaw.huc.di.rd.tag.creole.Pattern

class StartTagCloseEvent(qName: Basics.QName, id: Basics.Id) : TagEvent(qName, id) {

    override fun eventDeriv(p: Pattern): Pattern {
        // eventDeriv p (StartTagEvent qn id) = startTagDeriv p qn id
        return p.startTagCloseDeriv(qName, id)
    }

    override fun toString(): String {
        return qName.localName.value + "}"
    }

}
