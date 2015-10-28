package org.metaborg.spoofax.intellij.idea.languages;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.apache.commons.vfs2.FileObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.style.*;
import org.metaborg.core.syntax.IParserConfiguration;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.IntRange;
import org.metaborg.spoofax.intellij.StringFormatter;
import org.metaborg.spoofax.intellij.logging.InjectLogger;
import org.slf4j.Logger;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Adapts the Spoofax SGLR parser to allow it to be used as an IntelliJ lexer.
 */
public final class SpoofaxLexer extends LexerBase {

    @NotNull
    private final ILanguageImpl languageImpl;
    @NotNull
    private final IParserConfiguration parserConfiguration;
    @NotNull
    private final ISyntaxService<IStrategoTerm> syntaxService;
    @NotNull
    private final ICategorizerService<IStrategoTerm, IStrategoTerm> categorizer;
    @NotNull
    private final IStylerService<IStrategoTerm, IStrategoTerm> styler;
    @NotNull
    private final SpoofaxTokenTypeManager tokenTypesManager;
    @NotNull
    private final IResourceService resourceService;
    @InjectLogger
    private Logger logger;
    // The character buffer.
    private CharSequence buffer;
    // The range of characters in the buffer to lex.
    private IntRange bufferRange;
    // A list of tokens gathered from the lexed characters.
    private List<SpoofaxToken> tokens = new ArrayList<SpoofaxToken>();
    // The current index in {@link #tokens}.
    private int tokenIndex;

    @Inject
    private SpoofaxLexer(
            @Assisted @NotNull final ILanguageImpl languageImpl,
            @Assisted @NotNull final SpoofaxTokenTypeManager tokenTypesManager,
            @NotNull final ISyntaxService<IStrategoTerm> syntaxService,
            @NotNull final ICategorizerService<IStrategoTerm, IStrategoTerm> categorizer,
            @NotNull final IStylerService<IStrategoTerm, IStrategoTerm> styler,
            @NotNull final IParserConfiguration parserConfiguration,
            @NotNull final IResourceService resourceService) {

        this.syntaxService = syntaxService;
        this.categorizer = categorizer;
        this.styler = styler;
        this.tokenTypesManager = tokenTypesManager;
        this.languageImpl = languageImpl;
        this.parserConfiguration = parserConfiguration;
        this.resourceService = resourceService;
    }

    /**
     * Initiates a lexing session.
     *
     * @param buffer       The character sequence to lex.
     * @param startOffset  The inclusive start offset.
     * @param endOffset    The exclusive end offset.
     * @param initialState Not used. Must be zero.
     */
    @Override
    public final void start(@NotNull final CharSequence buffer,
                            final int startOffset,
                            final int endOffset,
                            final int initialState) {
        assert buffer != null;
        assert initialState == 0;
        assert 0 <= startOffset && startOffset <= buffer.length();
        assert 0 <= endOffset && endOffset <= buffer.length();

        this.buffer = buffer;
        this.bufferRange = IntRange.between(startOffset, endOffset);
        this.tokenIndex = 0;
        this.tokens.clear();

        if (buffer.length() == 0)
            return;

        ParseResult<IStrategoTerm> result = parseAll();
        tokenizeAll(result);
    }

//    /**
//     * Returns a list of tokens between the specified offsets.
//     *
//     * @param buffer      The character sequence to parse.
//     * @param startOffset The inclusive start offset.
//     * @param endOffset   The exclusive end offset.
//     * @return The resulting list of tokens.
//     */
//    @NotNull
//    private final List<SpoofaxToken> parseToTokens(@NotNull final CharSequence buffer,
//                                                   @NotNull final Range<Integer> range,
//                                                   final int startOffset,
//                                                   final int endOffset) {
//        assert buffer != null;
//        assert 0 <= startOffset && startOffset <= buffer.length();
//        assert 0 <= endOffset && endOffset <= buffer.length();
//
//        if (buffer.length() == 0)
//            return new ArrayList<>();
//
//        ParseResult<IStrategoTerm> result = parseAll();
//
//        List<SpoofaxToken> spoofaxTokens = tokenizeWithTokenizer(result, range, startOffset, endOffset, buffer.length());
//        return spoofaxTokens;
//    }

    /**
     * Parses the whole buffer.
     *
     * @return The parse result.
     */
    private final ParseResult<IStrategoTerm> parseAll() {
        // Dummy location. Bug in Metaborg Core prevents it being null.
        // TODO: Fix JSGLRI to allow null location.
        FileObject location = this.resourceService.resolve(
                "file:///home/daniel/eclipse/spoofax1507/workspace/TestProject/trans/test.spoofax");
        ParseResult<IStrategoTerm> result;
        try {
            result = this.syntaxService.parse(buffer.toString(), location, this.languageImpl, this.parserConfiguration);
        } catch (ParseException e) {
            throw new MetaborgRuntimeException("Unhandled exception", e);
        }
        return result;
    }

    /**
     * Uses the Spoofax tokenizer to tokenize the parse result,
     * and adds the tokens to the list of tokens.
     *
     * @param result     The parse result to tokenize.
     */
    private void tokenizeAll(@NotNull final ParseResult<IStrategoTerm> result) {
        if (result.result == null) {
            // A null parse result might occur when the input contains an error,
            // and recovery fails or is disabled.
            logger.error("Cannot categorize input of {}, parse result is empty", this.languageImpl);
            return;
        }

        // This uses the stratego term tokenizer.

        // Found here:
        // https://github.com/metaborg/spoofax/blob/master/org.metaborg.spoofax.core/src/main/java/org/metaborg/spoofax/core/style/CategorizerService.java#L48

        final ImploderAttachment rootImploderAttachment = ImploderAttachment.get(result.result);
        final ITokenizer tokenizer = rootImploderAttachment.getLeftToken().getTokenizer();
//        List<SpoofaxToken> spoofaxTokens = new ArrayList<>();

        Iterable<IRegionCategory<IStrategoTerm>> categorizedTokens = this.categorizer.categorize(this.languageImpl,
                                                                                                 result);
        Iterable<IRegionStyle<IStrategoTerm>> styledTokens = this.styler.styleParsed(this.languageImpl,
                                                                                     categorizedTokens);
        Iterator<IRegionStyle<IStrategoTerm>> styledTokenIterator = styledTokens.iterator();

        IRegionStyle<IStrategoTerm> currentRegionStyle = styledTokenIterator.hasNext() ? styledTokenIterator.next() : null;

        final int tokenCount = tokenizer.getTokenCount();
        int offset = 0;
        for (int i = 0; i < tokenCount; ++i) {
            final IToken token = tokenizer.getTokenAt(i);


//            if (token.getStartOffset() < offset)
//                // FIXME: The tokenizer sometimes returned the same token with the same start and end _twice_?
//                continue;

            // ASSUME: The list of regions is ordered by offset.
            // ASSUME: No region overlaps another region.
            // ASSUME: Every character in the input is covered by a region.
            int tokenStart = token.getStartOffset();
            int tokenEnd = token.getEndOffset() + 1;
            IntRange tokenRange = IntRange.between(tokenStart, tokenEnd);

            if (tokenRange.isEmpty())
                continue;

//            assert tokenRange.start == offset;
//            if (tokenRange.contains(offset))
//                // FIXME: The current offset should never be within a token.
//                // The tokenizer sometimes returned the same token with the same start and end _twice_?
//                continue;

            assert offset == tokenRange.start : StringFormatter.format("The current token (starting @ {}) must start where the previous token left off (@ {}).", tokenStart, offset);
            if (tokenRange.overlapsRange(this.bufferRange)) {
//            if (rangeStart <= tokenStart && tokenEnd <= rangeEnd) {

                // ASSUME: The styled tokens are ordered by offset.
                // ASSUME: No styled region overlaps another styled region.

                // Iterate until we find a style that ends after the token start.
                while (currentRegionStyle != null && currentRegionStyle.region().endOffset() + 1 <= tokenRange.start)
                    currentRegionStyle = styledTokenIterator.hasNext() ? styledTokenIterator.next() : null;

                // Get the style of the token
                IStyle tokenStyle = currentRegionStyle != null && currentRegionStyle.region().startOffset() <= tokenRange.start ? currentRegionStyle.style() : null;
                SpoofaxTokenType styledTokenType = this.tokenTypesManager.getTokenType(tokenStyle);

                SpoofaxToken spoofaxToken = new SpoofaxToken(styledTokenType, tokenRange); //tokenStart, tokenEnd);
                this.tokens.add(spoofaxToken);
//                assert getRangeStart(this.bufferRange) <= tokenStart;
//                assert tokenEnd <= getRangeEnd(this.bufferRange);
            }
            offset = tokenRange.end;
        }

        assert offset == this.buffer.length() : StringFormatter.format("The last token ended @ {}, which is before the end of the buffer @ {}.", offset, this.buffer.length());


//        return spoofaxTokens;
    }



    /**
     * Gets the current state of the lexer.
     *
     * @return An integer that indicates the current state.
     */
    @Override
    public int getState() {
        // Unused: always zero.
        return 0;
    }

    /**
     * The current token type, or <code>null</code>.
     *
     * @return The current token type, or <code>null</code> when lexing is finished.
     */
    @Nullable
    @Override
    public IElementType getTokenType() {
        if (0 <= tokenIndex && tokenIndex < tokens.size())
            return tokens.get(tokenIndex).type;
        else
            return null;
    }

    /**
     * Gets the start of the current token.
     *
     * @return The zero-based offset of the start of the current token in the character buffer.
     */
    @Override
    public int getTokenStart() {
        assert 0 <= tokenIndex && tokenIndex < tokens.size();
        return tokens.get(tokenIndex).range.start;
    }

    /**
     * Gets the end of the current token.
     *
     * @return The zero-based offset of the end of the current token in the character buffer.
     */
    @Override
    public int getTokenEnd() {
        assert 0 <= tokenIndex && tokenIndex < tokens.size();
        return tokens.get(tokenIndex).range.end;
    }

    /**
     * Advance the lexer to the next token.
     */
    @Override
    public void advance() {
        tokenIndex++;
    }

    /**
     * Gets the character buffer.
     *
     * @return The character buffer.
     */
    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return this.buffer;
    }

    /**
     * Gets the end of the relevant range of characters.
     *
     * @return The zero-based offset of the end of the relevant range of characters in the character buffer.
     */
    @Override
    public int getBufferEnd() {
        return this.bufferRange.end;
    }

}