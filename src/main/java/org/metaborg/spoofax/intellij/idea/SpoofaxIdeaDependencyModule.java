package org.metaborg.spoofax.intellij.idea;

import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.intellij.lang.ParserDefinition;
import com.intellij.lexer.Lexer;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.syntax.IParserConfiguration;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.intellij.SpoofaxIntelliJDependencyModule;
import org.metaborg.spoofax.intellij.factories.ICharacterLexerFactory;
import org.metaborg.spoofax.intellij.factories.IHighlightingLexerFactory;
import org.metaborg.spoofax.intellij.factories.IParserDefinitionFactory;
import org.metaborg.spoofax.intellij.factories.IProjectFactory;
import org.metaborg.spoofax.intellij.idea.languages.*;
import org.metaborg.spoofax.intellij.idea.model.IntelliJProject;
import org.metaborg.spoofax.intellij.idea.model.SpoofaxModuleBuilder;

/**
 * The Guice dependency injection module for the Spoofax IDEA plugin.
 */
public final class SpoofaxIdeaDependencyModule extends SpoofaxIntelliJDependencyModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        super.configure();

        bind(IIdeaAttachmentManager.class).to(IdeaAttachmentManager.class).in(Singleton.class);
        bind(IIdeaLanguageManager.class).to(IdeaLanguageManagerImpl.class).in(Singleton.class);
        bind(SpoofaxModuleBuilder.class).in(Singleton.class);
        bind(ILexerParserManager.class).to(LexerParserManager.class).in(Singleton.class);

        bind(SpoofaxSyntaxHighlighterFactory.class);
        bind(IParserConfiguration.class).toInstance(new JSGLRParserConfiguration(
            /* implode    */ true,
            /* recovery   */ true,
            /* completion */ false,
            /* timeout    */ 30000));

        install(new FactoryModuleBuilder()
                        .implement(Lexer.class, SpoofaxLexer.class)
                        .build(IHighlightingLexerFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(Lexer.class, CharacterLexer.class)
                        .build(ICharacterLexerFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(ParserDefinition.class, SpoofaxParserDefinition.class)
                        .build(IParserDefinitionFactory.class));
        install(new FactoryModuleBuilder()
                        .implement(IntelliJProject.class, IntelliJProject.class)
                        .build(IProjectFactory.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void bindProject() {
        bind(IntelliJProjectService.class).in(Singleton.class);
        bind(IProjectService.class).to(IntelliJProjectService.class).in(Singleton.class);
        bind(IIntelliJProjectService.class).to(IntelliJProjectService.class).in(Singleton.class);
    }
}
