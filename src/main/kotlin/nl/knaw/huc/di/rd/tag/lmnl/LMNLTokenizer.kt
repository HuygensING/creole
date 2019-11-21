package nl.knaw.huc.di.rd.tag.lmnl

import lambdada.parsec.extension.charsToString
import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import nl.knaw.huc.di.rd.tag.creole.Basics.qName
import nl.knaw.huc.di.rd.tag.creole.Event
import nl.knaw.huc.di.rd.tag.creole.events.Events.endTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.startTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.textEvent

object LMNLTokenizer {
    val specialChar = charIn("""[]{}\""")

    val escapedSpecialChar = (char('\\') then specialChar)
            .map { "${it.first}${it.second}" }

    val tagName = charIn(CharRange('a', 'z')).rep
            .map { it.charsToString() }

    val startTag = (char('[') thenRight tagName thenLeft char('}'))
            .map { startTagEvent(qName(it)) }

    val endTag = (char('{') thenRight tagName thenLeft char(']'))
            .map { endTagEvent(qName(it)) }

    val text = (not(specialChar).map { it.toString() } or escapedSpecialChar).rep
            .map { textEvent(it.joinToString(separator = "")) }

    val lmnlParser = ((startTag or text or endTag).rep thenLeft eos())

    fun tokenize(lmnl: String): List<Event> {
        val lmnlReader = Reader.string(lmnl)
        val result = lmnlParser(lmnlReader)
        println(result)
        return result.fold({ it.value }, { listOf() })
    }

}