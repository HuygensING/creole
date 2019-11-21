package nl.knaw.huc.di.rd.tag.lmnl

import arrow.core.Either
import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import lambdada.parsec.parser.Response.Accept
import nl.knaw.huc.di.rd.tag.creole.Basics.qName
import nl.knaw.huc.di.rd.tag.creole.events.Events.endTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.startTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.textEvent
import nl.knaw.huc.di.rd.tag.lmnl.LMNLTokenizer.endTag
import nl.knaw.huc.di.rd.tag.lmnl.LMNLTokenizer.escapedSpecialChar
import nl.knaw.huc.di.rd.tag.lmnl.LMNLTokenizer.startTag
import nl.knaw.huc.di.rd.tag.lmnl.LMNLTokenizer.text
import nl.knaw.huc.di.rd.tag.lmnl.LMNLTokenizer.tokenize
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class LMNLTokenizerTest {

    @Test
    fun tokenizeTest1() {
        val result = tokenize("[hello}World!{hello]")
        val startTagEvent = startTagEvent(qName("hello"))
        val textEvent = textEvent("World!")
        val endTagEvent = endTagEvent(qName("hello"))
        when (result) {
            is Either.Left -> fail("Parsing failed: ${result.a}")
            is Either.Right -> assertThat(result.b.toString()).isEqualTo(listOf(startTagEvent, textEvent, endTagEvent).toString())
        }
    }

    @Test
    fun emptyStringDoesNotParseAsText() {
        val lmnl = ""
        val lmnlReader = Reader.string(lmnl)
        val result = text(lmnlReader)
        println(result)
        assertThat(result is Response.Reject).isTrue()
    }

    @Test
    fun parseEscapedTagDelimiter1() {
        parseEscapedTagDelimiter("""\[""")
    }

    @Test
    fun parseEscapedTagDelimiter2() {
        parseEscapedTagDelimiter("""\]""")
    }

    @Test
    fun parseEscapedTagDelimiter3() {
        parseEscapedTagDelimiter("""\{""")
    }

    @Test
    fun parseEscapedTagDelimiter4() {
        parseEscapedTagDelimiter("""\}""")
    }

    private fun parseEscapedTagDelimiter(lmnl: String) {
        println(lmnl)
        val lmnlReader = Reader.string(lmnl)
        val p = escapedSpecialChar thenLeft eos()
        val result = p(lmnlReader)
        println(result)
        assertThat(result is Accept).isTrue()
        assertThat((result as Accept).value).isEqualTo(lmnl)
    }

    @Test
    fun parseText1() {
        val lmnl = "hello world"
        val lmnlReader = Reader.string(lmnl)
        val result = text(lmnlReader)
        println(result)
        assertThat(result is Accept).isTrue()
        assertThat((result as Accept).value.text).isEqualTo(lmnl)
    }

    @Test
    fun parseTextWithEscapedTagDelimiters() {
        val lmnl = """hello \[world\]"""
        println(lmnl)
        val lmnlReader = Reader.string(lmnl)
        val result = text(lmnlReader)
        println(result)
        assertThat(result is Accept).isTrue()
        assertThat((result as Accept).value.text).isEqualTo(lmnl)
    }

    @Test
    fun startTagTest() {
        val lmnl = "[start}"
        val lmnlReader = Reader.string(lmnl)
        val result = startTag(lmnlReader)
        println(result)
    }

    @Test
    fun endTagTest() {
        val lmnl = "{end]"
        val lmnlReader = Reader.string(lmnl)
        val p = endTag
        val result = p(lmnlReader)
        println(result)
        when (result) {
            is Accept -> assertThat(result.input.read()).isNull()
            is Response.Reject -> fail("unexpected rejection")
        }
    }

    @Test
    fun testOrWithChar() {
        val aal = lookahead(char('a') then char('a') then char('l'))
        val aap = lookahead(char('a') then char('a') then char('p'))
        val ara = lookahead(char('a') then char('r') then char('a'))
        val animal = aal or aap or ara

        val r = Reader.string("aal")
        val aalResult = animal(r)
        println(aalResult)
        assertThat(aalResult is Accept).isTrue()

        val r2 = Reader.string("aap")
        val aapResult = animal(r2)
        println(aapResult)
        assertThat(aapResult is Accept).isTrue()

        val r3 = Reader.string("ara")
        val araResult = animal(r3)
        println(araResult)
        assertThat(araResult is Accept).isTrue()

    }

    @Test
    fun testOrWithLookaheadString() {
        val aal = lookahead(string("aal"))
        val aap = lookahead(string("aap"))
        val ara = lookahead(string("ara"))
        val animal = aal or aap or ara

        val r = Reader.string("aal")
        val aalResult = animal(r)
        println(aalResult)
        assertThat(aalResult is Accept).isTrue()

        val r2 = Reader.string("aap")
        val aapResult = animal(r2)
        println(aapResult)
        assertThat(aapResult is Accept).isTrue()

        val r3 = Reader.string("ara")
        val araResult = animal(r3)
        println(araResult)
        assertThat(araResult is Accept).isTrue()

    }

}