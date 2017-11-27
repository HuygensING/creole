package nl.knaw.huygens.alexandria.creole;

/*
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

import org.assertj.core.api.SoftAssertions;

public class CreoleTest {
//  SoftAssertions softly = new SoftAssertions();

  static class TestPattern implements Pattern {
  }

  static final Pattern NULLABLE_PATTERN = new NullablePattern();

  static class NullablePattern extends Patterns.Text {
  }

  static final Pattern NOT_NULLABLE_PATTERN = new NotNullablePattern();

  static class NotNullablePattern extends Patterns.NotAllowed {
  }


}
