/*
 * Copyright © 2015-2016
 *
 * This file is part of Spoofax for IntelliJ.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metaborg.intellij.idea.facets;

import com.google.inject.Inject;
import com.intellij.facet.Facet;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.metaborg.intellij.idea.SpoofaxIdeaPlugin;
import org.metaborg.intellij.logging.InjectLogger;
import org.metaborg.util.log.*;

import javax.swing.*;

public class MetaborgFacetEditorTab extends FacetEditorTab {

    private JPanel mainPanel;
    private final FacetEditorContext editorContext;
    @InjectLogger
    private ILogger logger;

    /**
     * This instance is created by IntelliJ's plugin system.
     * Do not call this constructor manually.
     */
    public MetaborgFacetEditorTab(final FacetEditorContext editorContext,
                                  final FacetValidatorsManager validatorsManager) {
        SpoofaxIdeaPlugin.injector().injectMembers(this);

        this.editorContext = editorContext;
    }

    @Inject
    @SuppressWarnings("unused")
    private void inject() {
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Metaborg Facet Editor";
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void onFacetInitialized(@NotNull final Facet facet) {
        if (!this.editorContext.isNewFacet())
            return;

        if (facet instanceof MetaborgFacet) {
            final ModifiableRootModel model = ModuleRootManager.getInstance(facet.getModule()).getModifiableModel();
            ((MetaborgFacet)facet).applyFacet(model);
            model.dispose();
        }
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public void reset() {

    }

    @NotNull
    @Override
    public JComponent createComponent() {
        return this.mainPanel;
    }

    @Override
    public void disposeUIResources() {

    }
}
