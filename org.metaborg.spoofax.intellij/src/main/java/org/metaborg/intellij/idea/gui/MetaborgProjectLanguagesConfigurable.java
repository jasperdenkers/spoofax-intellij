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

package org.metaborg.intellij.idea.gui;

import com.google.inject.Inject;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.intellij.idea.gui2.MetaborgProjectLanguagesConfigurableForm;
import org.metaborg.spoofax.intellij.idea.SpoofaxIdeaPlugin;

import javax.swing.*;

public final class MetaborgProjectLanguagesConfigurable extends LanguagesConfigurable {

    private MetaborgProjectLanguagesConfigurableForm form;

    /**
     * This instance is created by IntelliJ's plugin system.
     * Do not call this method manually.
     */
    public MetaborgProjectLanguagesConfigurable(final Project project) {
        super(project);
        SpoofaxIdeaPlugin.injector().injectMembers(this);
    }

    @Override
    @Inject
    protected void inject(final ILanguageService languageService) {
        super.inject(languageService);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Metaborg";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (this.form == null) {
            this.form = new MetaborgProjectLanguagesConfigurableForm();
        }
        return this.form.getComponent();
    }


    /**
     * Called on form load and Cancel.
     */
    @Override
    public void reset() {
        super.reset();
        this.form.getLanguagesPanel().attachController(this);

        updateLanguagesList();
    }

    /**
     * Called on form unload.
     */
    @Override
    public void disposeUIResources() {

    }

    @Override
    protected void updateLanguagesList() {
        this.form.getLanguagesPanel().setLanguages(getLanguages());
    }
}
