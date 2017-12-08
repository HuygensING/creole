package nl.knaw.huygens.alexandria.creole.events;

/*-
 * #%L
 * alexandria-markup
 * =======
 * Copyright (C) 2016 - 2017 Huygens ING (KNAW)
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
import nl.knaw.huygens.alexandria.creole.Basics;
import nl.knaw.huygens.alexandria.creole.Pattern;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class EndAnnotationOpenEvent extends AnnotationEvent {
  EndAnnotationOpenEvent(Basics.QName qName) {
    super(qName);
  }

  @Override
  public Pattern eventDeriv(Pattern p) {
    throw new NotImplementedException();
  }

  @Override
  public String toString() {
    return "<" + getQName().getLocalName().getValue();
  }
}
