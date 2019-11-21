package nl.knaw.huc.di.rd.tag.lmnl

import arrow.core.Either
import lambdada.parsec.extension.charsToString
import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import lambdada.parsec.parser.Response.Reject
import nl.knaw.huc.di.rd.tag.creole.Basics.qName
import nl.knaw.huc.di.rd.tag.creole.Event
import nl.knaw.huc.di.rd.tag.creole.events.Events.endTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.startTagEvent
import nl.knaw.huc.di.rd.tag.creole.events.Events.textEvent

object LMNLTokenizer {
    private val specialChar = charIn("""[]{}\""")

    val escapedSpecialChar = (char('\\') then specialChar)
            .map { "${it.first}${it.second}" }

    private val tagName = charIn(CharRange('a', 'z')).rep
            .map { it.charsToString() }

    val startTag = (char('[') thenRight tagName thenLeft char('}'))
            .map { startTagEvent(qName(it)) }

    val endTag = (char('{') thenRight tagName thenLeft char(']'))
            .map { endTagEvent(qName(it)) }

    val text = (not(specialChar).map { it.toString() } or escapedSpecialChar).rep
            .map { textEvent(it.joinToString(separator = "")) }

    val lmnlParser = ((startTag or text or endTag).rep thenLeft eos())

    fun tokenize(lmnl: String): Either<Reject<Char, List<Event>>, List<Event>> {
        val lmnlReader = Reader.string(lmnl)
        return lmnlParser(lmnlReader)
                .fold(
                        { Either.right(it.value) },
                        { Either.left(it) }
                )
    }

}