package nl.knaw.huygens.alexandria;

/*-
 * #%L
 * alexandria-markup-core
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

import nl.knaw.huygens.alexandria.compare.TAGComparison;
import nl.knaw.huygens.alexandria.compare.TAGComparisonAssert;
import nl.knaw.huygens.alexandria.creole.Pattern;
import nl.knaw.huygens.alexandria.creole.PatternAssert;
import nl.knaw.huygens.alexandria.creole.ValidationResult;
import nl.knaw.huygens.alexandria.creole.ValidationResultAssert;
import org.assertj.core.api.Assertions;

public class AlexandriaAssertions extends Assertions {

  public static TAGComparisonAssert assertThat(TAGComparison actual) {
    return new TAGComparisonAssert(actual);
  }

  public static PatternAssert assertThat(Pattern actual) {
    return new PatternAssert(actual);
  }

  public static ValidationResultAssert assertThat(ValidationResult actual) {
    return new ValidationResultAssert(actual);
  }
}