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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class UtilitiesTest extends CreoleTest {

  private static final Pattern NULLABLE_PATTERN = Patterns.TEXT;
  private static final Pattern NOT_NULLABLE_PATTERN = Patterns.NOT_ALLOWED;

  @Test
  public void testEmptyIsNullable() {
    Pattern p = Patterns.EMPTY;
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testNotAllowedIsNotNullable() {
    Pattern p = Patterns.NOT_ALLOWED;
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testTextIsNullable() {
    Pattern p = Patterns.TEXT;
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testChoiceNullability1() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p2 = NULLABLE_PATTERN;
    Pattern p = Patterns.choice(p1, p2);
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testChoiceNullability2() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p2 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.choice(p1, p2);
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testChoiceNullability3() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p2 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.choice(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testChoiceNullability4() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p2 = NULLABLE_PATTERN;
    Pattern p = Patterns.choice(p1, p2);
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testInterleaveNullability1() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p2 = NULLABLE_PATTERN;
    Pattern p = Patterns.interleave(p1, p2);
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testInterleaveNullability2() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p2 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.interleave(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testInterleaveNullability3() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p2 = NULLABLE_PATTERN;
    Pattern p = Patterns.interleave(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testInterleaveNullability4() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p2 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.interleave(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testConcurNullability1() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p2 = NULLABLE_PATTERN;
    Pattern p = Patterns.concur(p1, p2);
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testConcurNullability2() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p2 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.concur(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testConcurNullability3() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p2 = NULLABLE_PATTERN;
    Pattern p = Patterns.concur(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testConcurNullability4() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p2 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.concur(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testPartitionNullability1() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p = Patterns.partition(p1);
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testPartitionNullability2() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.partition(p1);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testOneOrMoreNullability1() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p = Patterns.oneOrMore(p1);
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testOneOrMoreNullability2() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.oneOrMore(p1);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testConcurOneOrMoreNullability1() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p = Patterns.concurOneOrMore(p1);
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testConcurOneOrMoreNullability2() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.concurOneOrMore(p1);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testRangeIsNotNullable() {
    Pattern p1 = NULLABLE_PATTERN;
    NameClass nameClass = NameClasses.ANY_NAME;
    Pattern p = Patterns.range(nameClass, p1);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testEndRangeIsNotNullable() {
    Pattern p1 = NULLABLE_PATTERN;
    NameClass nameClass = NameClasses.ANY_NAME;
    Basics.QName qName = Basics.qName("uri","localName");
    Pattern p = Patterns.endRange( qName, "id");
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testAfterIsNotNullable() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p2 = NULLABLE_PATTERN;
    Pattern p = Patterns.after(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testAllNullability1() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p2 = NULLABLE_PATTERN;
    Pattern p = Patterns.all(p1, p2);
    assertThat(Utilities.nullable(p)).isTrue();
  }

  @Test
  public void testAllNullability2() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p2 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.all(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testAllNullability3() {
    Pattern p1 = NOT_NULLABLE_PATTERN;
    Pattern p2 = NULLABLE_PATTERN;
    Pattern p = Patterns.all(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }

  @Test
  public void testAllNullability4() {
    Pattern p1 = NULLABLE_PATTERN;
    Pattern p2 = NOT_NULLABLE_PATTERN;
    Pattern p = Patterns.all(p1, p2);
    assertThat(Utilities.nullable(p)).isFalse();
  }
}