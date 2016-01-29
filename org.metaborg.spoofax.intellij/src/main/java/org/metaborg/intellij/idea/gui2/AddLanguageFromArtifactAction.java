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

package org.metaborg.intellij.idea.gui2;

import com.google.inject.Inject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.metaborg.core.UnhandledException;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.spoofax.intellij.idea.SpoofaxIdeaPlugin;
import org.metaborg.spoofax.intellij.idea.languages.IIdeaLanguageManager;
import org.metaborg.spoofax.intellij.idea.vfs.SpoofaxArtifactFileType;
import org.metaborg.spoofax.intellij.languages.LanguageManager;

import java.io.IOException;

//import com.intellij.notification.Notification;
//import com.intellij.notification.Notifications;
//import com.intellij.openapi.command.WriteCommandAction;
//import org.jetbrains.annotations.NotNull;
//import org.metaborg.core.language.ILanguageDiscoveryService;


public class AddLanguageFromArtifactAction extends AnAction {

    private ILanguageService languageService;
    private IIdeaLanguageManager ideaLanguageManager;
    private ILanguageDiscoveryService discoveryService;
    private SpoofaxArtifactFileType artifactFileType;
    private LanguageManager languageManager;

    /**
     * This instance is created by IntelliJ's plugin system.
     * Do not call this method manually.
     */
    public AddLanguageFromArtifactAction() {
        super();
        SpoofaxIdeaPlugin.injector().injectMembers(this);
    }

    @Inject
    @SuppressWarnings("unused")
    private void inject(
            final LanguageManager languageManager, final ILanguageDiscoveryService discoveryService,
            final ILanguageService languageService, final IIdeaLanguageManager ideaLanguageManager,
            final SpoofaxArtifactFileType artifactFileType) {
        this.languageService = languageService;
        this.ideaLanguageManager = ideaLanguageManager;
        this.artifactFileType = artifactFileType;
        this.discoveryService = discoveryService;
        this.languageManager = languageManager;
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);

        final FileChooserDescriptor chooserDescriptor = FileChooserDescriptorFactory
                .createSingleFileDescriptor(this.artifactFileType);
        final VirtualFile file = FileChooser.chooseFile(chooserDescriptor, project, null);

        try {
            // TODO: Load on settings OK.
            final boolean success = this.languageManager.loadLanguageFromArtifact(file);
            // TODO: Notify when not successful.
        } catch (final IOException e1) {
            throw new UnhandledException(e1);
        }
    }
}
