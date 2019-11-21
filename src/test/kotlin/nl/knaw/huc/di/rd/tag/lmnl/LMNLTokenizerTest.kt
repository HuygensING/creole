package nl.knaw.huc.di.rd.tag.lmnl

import lambdada.parsec.io.Reader
import lambdada.parsec.parser.Response
import nl.knaw.huc.di.rd.tag.creole.Basics.qName
import nl.knaw.huc.di.rd.tag.creole.events.Events.endTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.startTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.textEvent
import nl.knaw.huc.di.rd.tag.lmnl.LMNLTokenizer.endTag
import nl.knaw.huc.di.rd.tag.lmnl.LMNLTokenizer.startTag
import nl.knaw.huc.di.rd.tag.lmnl.LMNLTokenizer.tokenize
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Test

class LMNLTokenizerTest {

    @Test
    fun tokenizeTest1() {
        val events = tokenize("[hello}World!{hello]")
        val startTagEvent = startTagEvent(qName("hello"))
        val textEvent = textEvent("World!")
        val endTagEvent = endTagEvent(qName("hello"))
        assertThat(events).hasSameElementsAs(listOf(startTagEvent, textEvent, endTagEvent))
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
            is Response.Accept -> assertThat(result.input.read()).isNull()
            is Response.Reject -> fail("unexpected rejection")
        }
    }

}