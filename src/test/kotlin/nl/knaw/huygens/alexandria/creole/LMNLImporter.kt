package nl.knaw.huygens.alexandria.creole

import com.google.common.base.Preconditions
import nl.knaw.huygens.alexandria.ErrorListener
import nl.knaw.huygens.alexandria.creole.Basics.id
import nl.knaw.huygens.alexandria.creole.Basics.qName
import nl.knaw.huygens.alexandria.creole.events.EndTagCloseEvent
import nl.knaw.huygens.alexandria.creole.events.EndTagOpenEvent
import nl.knaw.huygens.alexandria.creole.events.Events.endAnnotationCloseEvent
import nl.knaw.huygens.alexandria.creole.events.Events.endAnnotationOpenEvent
import nl.knaw.huygens.alexandria.creole.events.Events.startAnnotationCloseEvent
import nl.knaw.huygens.alexandria.creole.events.Events.startAnnotationOpenEvent
import nl.knaw.huygens.alexandria.creole.events.Events.textEvent
import nl.knaw.huygens.alexandria.creole.events.StartTagCloseEvent
import nl.knaw.huygens.alexandria.creole.events.StartTagOpenEvent
import nl.knaw.huygens.alexandria.data_model.*
import nl.knaw.huygens.alexandria.data_model.Annotation
import nl.knaw.huygens.alexandria.lmnl.grammar.LMNLLexer
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLSyntaxError
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.stream.Collectors


class LMNLImporter {
    internal class LimenContext(internal val limen: Limen, private val importerContext: ImporterContext) {
        internal val openMarkupDeque: Deque<Markup> = ArrayDeque()
        private val openMarkupStack = Stack<Markup>()
        val annotationStack = Stack<Annotation>()
        fun openMarkup(markup: Markup) {
            openMarkupDeque.push(markup)
            openMarkupStack.push(markup)
            limen.addMarkup(markup)
        }

        fun pushOpenMarkup(rangeName: String) { // LOG.info("currentLimenContext().openMarkupDeque={}", openMarkupDeque.stream().map(Markup::getTag).collect(Collectors.toList()));
            val findFirst = openMarkupDeque.stream() //
                    .filter { tr: Markup -> tr.extendedTag == rangeName } //
                    .findFirst()
            if (findFirst.isPresent) {
                val markup = findFirst.get()
                if (markup.textNodes.isEmpty()) { // every markup should have at least one textNode
                    addTextNode(TextNode(""))
                    closeMarkup()
                }
                openMarkupStack.push(markup)
            } else {
                importerContext.errors.add("Closing tag {$rangeName] found without corresponding open tag.")
            }
        }

        fun popOpenMarkup() {
            openMarkupStack.pop()
        }

        fun closeMarkup() {
            if (!openMarkupStack.isEmpty()) {
                val markup = openMarkupStack.pop()
                openMarkupDeque.remove(markup)
            }
        }

        fun addTextNode(textNode: TextNode?) {
            openMarkupDeque.descendingIterator() //
                    .forEachRemaining { tr: Markup -> tr.addTextNode(textNode) }
            limen.addTextNode(textNode)
        }

        fun currentMarkup(): Markup? {
            return if (openMarkupStack.isEmpty()) null else openMarkupStack.peek()
        }

        fun openAnnotation(annotation: Annotation) {
            if (annotationStack.isEmpty()) {
                val markup = currentMarkup()
                markup?.addAnnotation(annotation)
            } else {
                annotationStack.peek().addAnnotation(annotation)
            }
            annotationStack.push(annotation)
        }

        fun currentAnnotationLimen(): Limen {
            return annotationStack.peek().value()
        }

        fun closeAnnotation() {
            annotationStack.pop()
        }

    }

    internal class ImporterContext(private val lexer: LMNLLexer) {
        private val limenContextStack = Stack<LimenContext>()
        val errors: MutableList<String> = mutableListOf()
        private val eventList: MutableList<Event> = ArrayList()
        fun nextToken(): Token {
            return lexer.nextToken()
        }

        val modeName: String
            get() = lexer.modeNames[lexer._mode]

        val ruleName: String
            get() = lexer.ruleNames[lexer.token.type - 1]

        fun pushLimenContext(limen: Limen) {
            limenContextStack.push(LimenContext(limen, this))
        }

        fun currentLimenContext(): LimenContext {
            return limenContextStack.peek()
        }

        fun popLimenContext(): LimenContext {
            val limenContext = limenContextStack.pop()
            if (!limenContext.openMarkupDeque.isEmpty()) {
                val openRanges = limenContext.openMarkupDeque.stream() //
                        .map { m: Markup -> "[" + m.extendedTag + "}" } //
                        .collect(Collectors.joining(", "))
                errors.add("Unclosed LMNL range(s): $openRanges")
            }
            return limenContext
        }

        fun newMarkup(tagName: String?): Markup {
            return Markup(currentLimenContext().limen, tagName)
        }

        fun openMarkup(markup: Markup) {
            currentLimenContext().openMarkup(markup)
            addStartTagOpenEvent(markup)
        }

        fun pushOpenMarkup(rangeName: String) {
            currentLimenContext().pushOpenMarkup(rangeName)
        }

        fun popOpenMarkup() {
            currentLimenContext().popOpenMarkup()
        }

        fun closeMarkup() {
            val markup = currentLimenContext().currentMarkup()
            addEndTagCloseEvent(markup)
            currentLimenContext().closeMarkup()
        }

        fun addTextNode(textNode: TextNode?) {
            currentLimenContext().addTextNode(textNode)
        }

        fun openAnnotation(annotation: Annotation) {
            currentLimenContext().openAnnotation(annotation)
        }

        fun currentAnnotationLimen(): Limen {
            return currentLimenContext().currentAnnotationLimen()
        }

        fun closeAnnotation() {
            val annotation = currentLimenContext().annotationStack.peek()
            addEndAnnotationCloseEvent(annotation.tag)
            currentLimenContext().closeAnnotation()
        }

        fun hasErrors(): Boolean {
            return errors.isNotEmpty()
        }

        fun getEventList(): List<Event> {
            return eventList
        }

        private fun addStartTagOpenEvent(markup: Markup) {
            Preconditions.checkNotNull(markup)
            val qName = getQName(markup)
            val id = getId(markup)
            eventList.add(StartTagOpenEvent(qName, id))
        }

        fun addStartTagCloseEvent(markup: Markup?) {
            Preconditions.checkNotNull(markup)
            val qName = getQName(markup)
            val id = getId(markup)
            eventList.add(StartTagCloseEvent(qName, id))
        }

        fun addEndTagOpenEvent(markup: Markup?) {
            Preconditions.checkNotNull(markup)
            val qName = getQName(markup)
            val id = getId(markup)
            eventList.add(EndTagOpenEvent(qName, id))
        }

        fun addEndTagCloseEvent(markup: Markup?) {
            Preconditions.checkNotNull(markup)
            val qName = getQName(markup)
            val id = getId(markup)
            eventList.add(EndTagCloseEvent(qName, id))
        }

        private fun getId(markup: Markup?): Basics.Id {
            Preconditions.checkNotNull(markup)
            val idString = if (markup!!.hasId()) markup.id else ""
            return id(idString)
        }

        private fun getQName(markup: Markup?): Basics.QName {
            Preconditions.checkNotNull(markup)
            return qName(markup!!.tag)
        }

        fun addTextEvent(text: String?) {
            eventList.add(textEvent(text!!))
        }

        fun addStartAnnotationOpenEvent(tag: String?) {
            val startAnnotationOpenEvent: Event = startAnnotationOpenEvent(qName(tag!!))
            eventList.add(startAnnotationOpenEvent)
        }

        fun addStartAnnotationCloseEvent(tag: String?) {
            val startAnnotationCloseEvent: Event = startAnnotationCloseEvent(qName(tag!!))
            eventList.add(startAnnotationCloseEvent)
        }

        fun addEndAnnotationOpenEvent(tag: String?) {
            val endAnnotationOpenEvent: Event = endAnnotationOpenEvent(qName(tag!!))
            eventList.add(endAnnotationOpenEvent)
        }

        fun addEndAnnotationCloseEvent(tag: String?) {
            val endAnnotationCloseEvent: Event = endAnnotationCloseEvent(qName(tag!!))
            eventList.add(endAnnotationCloseEvent)
        }

    }

    @Throws(LMNLSyntaxError::class)
    fun importLMNL(input: String?): List<Event> {
        val antlrInputStream: CharStream = CharStreams.fromString(input)
        return importLMNL(antlrInputStream)
    }

    @Throws(LMNLSyntaxError::class)
    fun importLMNL(input: InputStream?): List<Event> {
        return try {
            val antlrInputStream = CharStreams.fromStream(input)
            importLMNL(antlrInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }

    @Throws(LMNLSyntaxError::class)
    private fun importLMNL(antlrInputStream: CharStream): List<Event> {
        val lexer = LMNLLexer(antlrInputStream)
        val errorListener = ErrorListener()
        lexer.addErrorListener(errorListener)
        val context = ImporterContext(lexer)
        val document = Document()
        val limen = document.value()
        context.pushLimenContext(limen)
        handleDefaultMode(context)
        context.popLimenContext()
        var errorMsg = ""
        if (context.hasErrors()) {
            val errors = context.errors.stream().collect(Collectors.joining("\n"))
            errorMsg = "Parsing errors:\n$errors"
        }
        if (errorListener.hasErrors()) {
            val errors = errorListener.errors.stream().collect(Collectors.joining("\n"))
            errorMsg += "\n\nTokenizing errors:\n$errors"
        }
        if (!errorMsg.isEmpty()) {
            throw LMNLSyntaxError(errorMsg)
        }
        return context.getEventList()
    }

    private fun handleDefaultMode(context: ImporterContext) {
        val methodName = "defaultMode"
        var token: Token
        do {
            token = context.nextToken()
            if (token.type != Token.EOF) {
                val ruleName = context.ruleName
                val modeName = context.modeName
                log(methodName, ruleName, modeName, token, context)
                when (token.type) {
                    LMNLLexer.BEGIN_OPEN_RANGE -> handleOpenRange(context)
                    LMNLLexer.BEGIN_CLOSE_RANGE -> handleCloseRange(context)
                    LMNLLexer.TEXT -> context.addTextEvent(token.text)
                    else -> handleUnexpectedToken(methodName, token, ruleName, modeName)
                }
            }
        } while (token.type != Token.EOF)
    }

    private fun handleOpenRange(context: ImporterContext) {
        val methodName = "handleOpenRange"
        var goOn = true
        while (goOn) {
            val token = context.nextToken()
            val ruleName = context.ruleName
            val modeName = context.modeName
            log(methodName, ruleName, modeName, token, context)
            when (token.type) {
                LMNLLexer.Name_Open_Range -> {
                    val markup = context.newMarkup(token.text)
                    context.openMarkup(markup)
                }
                LMNLLexer.BEGIN_OPEN_ANNO -> handleAnnotation(context)
                LMNLLexer.END_OPEN_RANGE -> {
                    context.addStartTagCloseEvent(context.currentLimenContext().currentMarkup())
                    context.popOpenMarkup()
                    goOn = false
                }
                LMNLLexer.END_ANONYMOUS_RANGE -> {
                    val textNode = TextNode("")
                    context.addTextNode(textNode)
                    val markup2 = context.currentLimenContext().currentMarkup()
                    context.addStartTagCloseEvent(markup2)
                    context.addEndTagOpenEvent(markup2)
                    context.closeMarkup()
                    goOn = false
                }
                else -> handleUnexpectedToken(methodName, token, ruleName, modeName)
            }
            goOn = goOn && token.type != Token.EOF
        }
    }

    private fun handleAnnotation(context: ImporterContext) {
        val methodName = "handleAnnotation"
        val annotation = Annotation("")
        context.openAnnotation(annotation)
        var goOn = true
        while (goOn) {
            val token = context.nextToken()
            val ruleName = context.ruleName
            val modeName = context.modeName
            log(methodName, ruleName, modeName, token, context)
            when (token.type) {
                LMNLLexer.Name_Open_Annotation -> {
                    annotation.tag = token.text
                    context.addStartAnnotationOpenEvent(annotation.tag)
                }
                LMNLLexer.OPEN_ANNO_IN_ANNO_OPENER, LMNLLexer.OPEN_ANNO_IN_ANNO_CLOSER -> {
                    if (annotation.tag.isEmpty()) {
                        context.addStartAnnotationOpenEvent("")
                    }
                    handleAnnotation(context)
                }
                LMNLLexer.END_OPEN_ANNO -> {
                    context.addStartAnnotationCloseEvent(annotation.tag)
                    context.pushLimenContext(context.currentAnnotationLimen())
                }
                LMNLLexer.ANNO_TEXT -> {
                    context.addTextNode(TextNode(token.text))
                    context.addTextEvent(token.text)
                }
                LMNLLexer.BEGIN_ANNO_OPEN_RANGE -> handleOpenRange(context)
                LMNLLexer.BEGIN_ANNO_CLOSE_RANGE -> handleCloseRange(context)
                LMNLLexer.BEGIN_CLOSE_ANNO -> context.addEndAnnotationOpenEvent(annotation.tag)
                LMNLLexer.Name_Close_Annotation -> {
                    val tag = token.text
                    if (tag != annotation.tag) {
                        val message = String.format("Found unexpected annotation close tag {%s], expected {%s]", tag, annotation.tag)
                        throw LMNLSyntaxError(message)
                    }
                }
                LMNLLexer.END_CLOSE_ANNO -> {
                    context.popLimenContext()
                    context.closeAnnotation()
                    goOn = false
                }
                LMNLLexer.END_EMPTY_ANNO -> {
                    context.addStartAnnotationCloseEvent(annotation.tag)
                    context.addEndAnnotationOpenEvent(annotation.tag)
                    context.closeAnnotation()
                    goOn = false
                }
                else -> handleUnexpectedToken(methodName, token, ruleName, modeName)
            }
            goOn = goOn && token.type != Token.EOF
        }
    }

    private fun handleCloseRange(context: ImporterContext) {
        val methodName = "handleCloseRange"
        var goOn = true
        while (goOn) {
            val token = context.nextToken()
            val ruleName = context.ruleName
            val modeName = context.modeName
            log(methodName, ruleName, modeName, token, context)
            when (token.type) {
                LMNLLexer.Name_Close_Range -> {
                    val rangeName = token.text
                    context.pushOpenMarkup(rangeName)
                    val markup = context.currentLimenContext().currentMarkup()
                    if (markup == null) {
                        val message = String.format("%s: unexpected token: {%s]", methodName, rangeName)
                        throw LMNLSyntaxError(message)
                    }
                    context.addEndTagOpenEvent(markup)
                }
                LMNLLexer.BEGIN_OPEN_ANNO_IN_RANGE_CLOSER -> handleAnnotation(context)
                LMNLLexer.END_CLOSE_RANGE -> {
                    context.closeMarkup()
                    goOn = false
                }
                else -> handleUnexpectedToken(methodName, token, ruleName, modeName)
            }
            goOn = goOn && token.type != Token.EOF
        }
    }

    private fun handleUnexpectedToken(methodName: String, token: Token, ruleName: String, modeName: String) {
        val message = String.format("%s: unexpected rule/token: token=%s, ruleName=%s, mode=%s", methodName, token, ruleName, modeName)
        LOG.error(message)
        throw LMNLSyntaxError(message)
    }

    private fun log(mode: String, ruleName: String, modeName: String, token: Token, context: ImporterContext) {
        // LOG.info("{}:\tlevel:{}, <{}> :\t{} ->\t{}", //
// mode, context.limenContextStack.size(), //
// token.getText().replace("\n", "\\n"), //
// ruleName, modeName);
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(LMNLImporter::class.java)
    }
}
