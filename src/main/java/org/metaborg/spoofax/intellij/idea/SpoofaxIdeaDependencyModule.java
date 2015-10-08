package org.metaborg.spoofax.intellij.idea;

import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.intellij.lexer.Lexer;
import org.metaborg.core.syntax.IParserConfiguration;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.intellij.SpoofaxIntelliJDependencyModule;
import org.metaborg.spoofax.intellij.idea.languages.IIdeaLanguageManager;
import org.metaborg.spoofax.intellij.idea.languages.IdeaLanguageManagerImpl;
import org.metaborg.spoofax.intellij.idea.languages.IdeaLanguageObjectManager;
import org.metaborg.spoofax.intellij.idea.model.SpoofaxModuleBuilder;
import org.metaborg.spoofax.intellij.languages.syntax.*;

/**
 * The Guice dependency injection module for the Spoofax IDEA plugin.
 */
public final class SpoofaxIdeaDependencyModule extends SpoofaxIntelliJDependencyModule {

    @Override
    protected void configure() {
        super.configure();

        bind(IdeaLanguageObjectManager.class).in(Singleton.class);
        bind(IIdeaLanguageManager.class).to(IdeaLanguageManagerImpl.class).in(Singleton.class);
        bind(SpoofaxModuleBuilder.class).in(Singleton.class);
        //bind(IdeaLanguageManager.class).in(Singleton.class);
        bind(IParserConfiguration.class).toInstance(new JSGLRParserConfiguration(
            /* implode    */ true,
            /* recovery   */ true,
            /* completion */ false,
            /* timeout    */ 30000));
        //SpoofaxTokenTypeManager tokenTypesManager,
        //@NotNull final IParserConfiguration
        //bind(ISpoofaxParser.class).to(OldSpoofaxParser.class).in(Singleton.class);
        //bind(Lexer.class).to(SpoofaxLexer.class).in(Singleton.class);

        install(new FactoryModuleBuilder()
                        .implement(Lexer.class, SpoofaxLexer.class)
                        .build(ISpoofaxLexerAdapterFactory.class));

    }

}
