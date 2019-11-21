package nl.knaw.huc.di.rd.tag.lmnl

import lambdada.parsec.extension.charsToString
import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import nl.knaw.huc.di.rd.tag.creole.Basics.qName
import nl.knaw.huc.di.rd.tag.creole.Event
import nl.knaw.huc.di.rd.tag.creole.events.EndTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.endTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.startTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.textEvent
import nl.knaw.huc.di.rd.tag.creole.events.StartTagEvent

object LMNLTokenizer {
    private fun escaped(c: Char) = char('\\') then char(c)

    val tagDelimiter = charIn("[]{}")

    val escapedTagDelimiter = escaped('[') or escaped(']') or escaped('{') or escaped('}')

    val tagName = charIn(CharRange('a', 'z')).rep.map { it.charsToString() }

    val startTag = (char('[') thenRight tagName thenLeft char('}')).map { startTagEvent(qName(it)) }

    val endTag = (char('{') thenRight tagName thenLeft char(']')).map { endTagEvent(qName(it)) }

    val text = (not(tagDelimiter) /*or escapedTagDelimiter*/).optrep.map { textEvent(it.charsToString()) }

    val lmnlParser = (startTag then (startTag or text or endTag).optrep then endTag then eos()).map { toEventList(it) }
//    val lmnlParser = (startTag then text then endTag then eos()).map { listOf(it.first.first.first, it.first.first.second, it.first.second) }

    private fun toEventList(pair: Pair<Pair<Pair<StartTagEvent, List<Event>>, EndTagEvent>, Unit>): List<Event> {
        return mutableListOf<Event>(pair.first.first.first).also {
            it.addAll(pair.first.first.second)
            it.add(pair.first.second)
        }
    }

    fun tokenize(lmnl: String): List<Event> {
        val lmnlReader = Reader.string(lmnl)
        val result = lmnlParser(lmnlReader)
        return result.fold({ it.value }, { listOf() })
    }


}