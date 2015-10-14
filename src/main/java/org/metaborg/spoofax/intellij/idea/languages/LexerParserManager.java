package org.metaborg.spoofax.intellij.idea.languages;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import org.jetbrains.annotations.NotNull;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;

/**
 * {@inheritDoc}
 */
@Singleton
public final class LexerParserManager implements ILexerParserManager {

    @NotNull
    private final IIdeaAttachmentManager attachmentManager;

    @Inject
    private LexerParserManager(@NotNull final IIdeaAttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Lexer getHighlightingLexer(@NotNull final ILanguageImpl implementation) {
        return this.attachmentManager.get(implementation).lexer;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Lexer getCharacterLexer(@NotNull final ILanguage language) {
        return this.attachmentManager.get(language).characterLexer;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public PsiParser getParser(@NotNull final ILanguage language) {
        return this.attachmentManager.get(language).parser;
    }
}
