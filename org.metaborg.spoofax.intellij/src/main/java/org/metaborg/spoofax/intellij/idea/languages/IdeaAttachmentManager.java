/*
 * Copyright © 2015-2015
 *
 * This file is part of Spoofax for IntelliJ.
 *
 * Spoofax for IntelliJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoofax for IntelliJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Spoofax for IntelliJ.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.metaborg.spoofax.intellij.idea.languages;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.psi.tree.IFileElementType;
import javassist.util.proxy.ProxyFactory;
import org.jetbrains.annotations.NotNull;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.logging.InjectLogger;
import org.metaborg.spoofax.intellij.factories.ICharacterLexerFactory;
import org.metaborg.spoofax.intellij.factories.IFileElementTypeFactory;
import org.metaborg.spoofax.intellij.factories.IHighlightingLexerFactory;
import org.metaborg.spoofax.intellij.factories.IParserDefinitionFactory;
import org.metaborg.spoofax.intellij.idea.psi.SpoofaxAnnotator;
import org.metaborg.spoofax.intellij.idea.vfs.SpoofaxFileType;
import org.metaborg.spoofax.intellij.menu.BuilderMenuBuilder;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Creates and caches {@link IdeaLanguageAttachment} objects.
 */
@Singleton
public final class IdeaAttachmentManager implements IIdeaAttachmentManager {

    @NotNull
    private final ProxyFactory proxyFactory;
    @NotNull
    private final IHighlightingLexerFactory lexerFactory;
    @NotNull
    private final IParserDefinitionFactory parserDefinitionFactory;
    @NotNull
    private final ICharacterLexerFactory characterLexerFactory;
    @NotNull
    private final IFileElementTypeFactory fileElementTypeFactory;
    @NotNull
    private final BuilderMenuBuilder builderMenuBuilder;
    @NotNull
    private final Provider<SpoofaxSyntaxHighlighterFactory> syntaxHighlighterFactoryProvider;
    @NotNull
    private final HashMap<ILanguage, IdeaLanguageAttachment> languages = new HashMap<>();
    @NotNull
    private final HashMap<ILanguageImpl, IdeaLanguageImplAttachment> implementations = new HashMap<>();
    @NotNull
    private final SpoofaxAnnotator spoofaxAnnotator;
    @InjectLogger
    private Logger logger;

    @Inject
    private IdeaAttachmentManager(
            @NotNull final IHighlightingLexerFactory lexerFactory,
            @NotNull final IParserDefinitionFactory parserDefinitionFactory,
            @NotNull final ICharacterLexerFactory characterLexerFactory,
            @NotNull final IFileElementTypeFactory fileElementTypeFactory,
            @NotNull final BuilderMenuBuilder builderMenuBuilder,
            @NotNull final Provider<SpoofaxSyntaxHighlighterFactory> syntaxHighlighterFactoryProvider,
            @NotNull final SpoofaxAnnotator spoofaxAnnotator) {
        this.lexerFactory = lexerFactory;
        this.parserDefinitionFactory = parserDefinitionFactory;
        this.characterLexerFactory = characterLexerFactory;
        this.fileElementTypeFactory = fileElementTypeFactory;
        this.syntaxHighlighterFactoryProvider = syntaxHighlighterFactoryProvider;
        this.builderMenuBuilder = builderMenuBuilder;
        this.spoofaxAnnotator = spoofaxAnnotator;

        this.proxyFactory = new ProxyFactory();
        this.proxyFactory.setUseCache(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdeaLanguageAttachment get(@NotNull final ILanguage language) {
        IdeaLanguageAttachment obj = this.languages.get(language);
        if (obj == null) {
            obj = createLanguageAttachment(language);
            this.languages.put(language, obj);
            this.logger.debug("Created a new IdeaLanguageAttachment for language {}.", language);
        } else {
            this.logger.debug("Used cached IdeaLanguageAttachment for language {}.", language);
        }
        return obj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdeaLanguageImplAttachment get(@NotNull final ILanguageImpl implementation) {
        IdeaLanguageImplAttachment obj = this.implementations.get(implementation);
        if (obj == null) {
            obj = createLanguageImplAttachment(implementation);
            this.implementations.put(implementation, obj);
            this.logger.debug(
                    "Created a new IdeaLanguageImplAttachment for language implementation {}.",
                    implementation
            );
        } else {
            this.logger.debug(
                    "Used cached IdeaLanguageImplAttachment for language implementation {}.",
                    implementation
            );
        }
        return obj;
    }

    /**
     * Creates a new {@link IdeaLanguageAttachment}.
     *
     * @param language The language.
     * @return The created {@link IdeaLanguageAttachment}.
     */
    @NotNull
    private IdeaLanguageAttachment createLanguageAttachment(@NotNull final ILanguage language) {
        final SpoofaxIdeaLanguage ideaLanguage = createIdeaLanguage(language);
        final SpoofaxFileType fileType = createFileType(ideaLanguage);
        final SpoofaxTokenTypeManager tokenTypeManager = createTokenTypeManager(ideaLanguage);
        final IFileElementType fileElementType = createFileElementType(ideaLanguage, tokenTypeManager);
        final ParserDefinition parserDefinition = createParserDefinition(fileType, fileElementType);
        final SpoofaxSyntaxHighlighterFactory syntaxHighlighterFactory = createSyntaxHighlighterFactory();

        return new IdeaLanguageAttachment(
                ideaLanguage,
                fileType,
                tokenTypeManager,
                parserDefinition,
                syntaxHighlighterFactory,
                this.characterLexerFactory,
                this.spoofaxAnnotator
        );
    }

    /**
     * Creates a new {@link IdeaLanguageImplAttachment}.
     *
     * @param implementation The language implementation.
     * @return The created {@link IdeaLanguageImplAttachment}.
     */
    @NotNull
    private IdeaLanguageImplAttachment createLanguageImplAttachment(@NotNull final ILanguageImpl implementation) {
        final IdeaLanguageAttachment langAtt = get(implementation.belongsTo());

        final Lexer lexer = createLexer(implementation, langAtt.tokenTypeManager());
        final DefaultActionGroup buildActionGroup = createAction(implementation);

        return new IdeaLanguageImplAttachment(lexer, buildActionGroup);
    }

    /**
     * Creates a new IDEA language for a Spoofax language.
     *
     * @param language The language.
     * @return The created IDEA language.
     */
    @NotNull
    private SpoofaxIdeaLanguage createIdeaLanguage(@NotNull final ILanguage language) {
        try {
            this.proxyFactory.setSuperclass(SpoofaxIdeaLanguage.class);
            return (SpoofaxIdeaLanguage)this.proxyFactory.create(
                    new Class<?>[]{ILanguage.class},
                    new Object[]{language}
            );
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            this.logger.error("Unexpected unhandled exception.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new file type for an IDEA language.
     *
     * @param language The IDEA language.
     * @return The created file type.
     */
    @NotNull
    private SpoofaxFileType createFileType(@NotNull final SpoofaxIdeaLanguage language) {
        try {
            this.proxyFactory.setSuperclass(SpoofaxFileType.class);
            return (SpoofaxFileType)this.proxyFactory.create(
                    new Class<?>[]{SpoofaxIdeaLanguage.class},
                    new Object[]{language}
            );
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            this.logger.error("Unexpected unhandled exception.", e);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private IFileElementType createFileElementType(
            @NotNull final Language language,
            @NotNull final SpoofaxTokenTypeManager tokenTypesManager) {
        return this.fileElementTypeFactory.create(language, tokenTypesManager);
    }

    /**
     * Creates a new spoofax token type manager for an IDEA language.
     *
     * @param language The IDEA language.
     * @return The created token type manager.
     */
    @NotNull
    private SpoofaxTokenTypeManager createTokenTypeManager(@NotNull final SpoofaxIdeaLanguage language) {
        return new SpoofaxTokenTypeManager(language);
    }

    /**
     * Creates a new parser definition.
     *
     * @param fileType The language file type.
     * @return The created parser definition.
     */
    @NotNull
    private ParserDefinition createParserDefinition(
            @NotNull final SpoofaxFileType fileType,
            @NotNull final IFileElementType fileElementType) {
        return this.parserDefinitionFactory.create(fileType, fileElementType);
    }

    /**
     * Creates a new lexer.
     *
     * @param language         The language.
     * @param tokenTypeManager The token type manager.
     * @return The lexer.
     */
    @NotNull
    private Lexer createLexer(
            @NotNull final ILanguageImpl language,
            @NotNull final SpoofaxTokenTypeManager tokenTypeManager) {
        return this.lexerFactory.create(language, tokenTypeManager);
    }

    /**
     * Creates a new syntax highlighter factory.
     *
     * @return The syntax highlighter factory.
     */
    @NotNull
    private SpoofaxSyntaxHighlighterFactory createSyntaxHighlighterFactory() {
        return this.syntaxHighlighterFactoryProvider.get();
    }

    /**
     * Creates the builder menu action group.
     *
     * @param implementation The language implementation.
     * @return The action group.
     */
    @NotNull
    private DefaultActionGroup createAction(@NotNull final ILanguageImpl implementation) {
        return this.builderMenuBuilder.build(implementation);
    }
}
