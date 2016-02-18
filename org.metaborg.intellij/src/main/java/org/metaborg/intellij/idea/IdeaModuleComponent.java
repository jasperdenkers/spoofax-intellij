/*
 * Copyright © 2015-2016
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

package org.metaborg.intellij.idea;

import com.google.inject.*;
import com.intellij.openapi.application.*;
import com.intellij.openapi.components.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.*;
import com.intellij.openapi.vfs.*;
import org.apache.commons.vfs2.*;
import org.jetbrains.annotations.*;
import org.metaborg.core.language.*;
import org.metaborg.core.project.*;
import org.metaborg.core.project.configuration.*;
import org.metaborg.intellij.configuration.*;
import org.metaborg.intellij.idea.configuration.*;
import org.metaborg.intellij.idea.languages.*;
import org.metaborg.intellij.idea.projects.*;
import org.metaborg.intellij.logging.*;
import org.metaborg.intellij.projects.*;
import org.metaborg.intellij.resources.*;
import org.metaborg.util.log.*;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Module-level component.
 */
public final class IdeaModuleComponent implements ModuleComponent {

    private final Module module;
    private ProjectUtils projectUtils;
    private IIdeaProjectService projectService;
    private IIdeaProjectFactory projectFactory;
    private ILanguageSpecService languageSpecService;
    private IIntelliJResourceService resourceService;
    private ConfigurationUtils configurationUtils;
    @InjectLogger
    private ILogger logger;

    /**
     * This instance is created by IntelliJ's plugin system.
     * Do not call this constructor manually.
     */
    public IdeaModuleComponent(final Module module) {
        this.module = module;
        SpoofaxIdeaPlugin.injector().injectMembers(this);
    }

    @Inject
    @SuppressWarnings("unused")
    private void inject(
            final IIdeaProjectService projectService,
            final IIdeaProjectFactory projectFactory,
            final IIntelliJResourceService resourceService,
            final ILanguageSpecService languageSpecService,
            final ConfigurationUtils configurationUtils,
            final ProjectUtils projectUtils) {
        this.projectService = projectService;
        this.projectFactory = projectFactory;
        this.resourceService = resourceService;
        this.languageSpecService = languageSpecService;
        this.configurationUtils = configurationUtils;
        this.projectUtils = projectUtils;
    }

    /**
     * Occurs when the module is initialized.
     */
    @Override
    public void initComponent() {
        this.logger.debug("Initializing module: {}", this.module);
        this.logger.info("Initialized module: {}", this.module);
    }

    /**
     * Occurs when the module is opened.
     * <p>
     * Called after {@link #initComponent()} and {@link #moduleAdded()}.
     * <p>
     * This method is not called when modules are created
     * in a {@link com.intellij.ide.util.projectWizard.ModuleBuilder}.
     */
    @Override
    public void projectOpened() {
        this.logger.debug("Opening module: {}", this.module);

        @Nullable final IdeaProject project = registerModule();
        if (project == null)
            return;
        this.configurationUtils.loadAndActivateLanguages(this.module.getProject(),
                this.projectUtils.getCompileDependencies(project));

        this.logger.info("Opened module: {}", this.module);
    }

    /**
     * Occurs when the module has been completely loaded and added to the project.
     * <p>
     * Called after {@link #initComponent()}. May be called twice for a module.
     */
    @Override
    public void moduleAdded() {
        this.logger.debug("Adding module: {}", this.module);
        this.logger.info("Added module: {}", this.module);
    }

    /**
     * Occurs when the module is closed.
     */
    @Override
    public void projectClosed() {
        this.logger.debug("Closing module: {}", this.module);

        unregisterModule();

        this.logger.info("Closed module: {}", this.module);
    }

    /**
     * Occurs when the module is disposed.
     * <p>
     * Called after {@link #projectClosed()}.
     */
    @Override
    public void disposeComponent() {
        this.logger.debug("Disposing module: {}", this.module);
        this.logger.info("Disposed module: {}", this.module);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getComponentName() {
        return IdeaModuleComponent.class.getName();
    }

    /**
     * Registers the module as opened.
     *
     * @return The project; or <code>null</code> when it wasn't registered.
     */
    @Nullable
    private IdeaProject registerModule() {
        @Nullable final FileObject root = getRootDirectory(this.module);
        if (root == null)
            return null;
        final IdeaProject project = this.projectFactory.create(this.module, root);
        this.projectService.open(project);
        return project;
    }

    /**
     * Unregisters the module as opened.
     */
    private void unregisterModule() {
        this.projectService.close(this.module);
    }

    /**
     * Gets the root directory of the module.
     *
     * @param module The module.
     * @return The root directory; or <code>null</code>.
     */
    @Nullable
    private FileObject getRootDirectory(final Module module) {
        try {
            return this.resourceService.resolve(module.getModuleFilePath()).getParent();
        } catch (final FileSystemException e) {
            this.logger.error("Unhandled exception.", e);
        }
        return null;
    }
}
