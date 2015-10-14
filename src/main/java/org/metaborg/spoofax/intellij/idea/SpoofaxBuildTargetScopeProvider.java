package org.metaborg.spoofax.intellij.idea;

import com.google.inject.Inject;
import com.intellij.compiler.impl.BuildTargetScopeProvider;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerFilter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.api.CmdlineProtoUtil;
import org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope;
import org.metaborg.spoofax.intellij.jps.JpsPlugin;
import org.metaborg.spoofax.intellij.jps.targetbuilders.SpoofaxPostTargetType;

import java.util.Collections;
import java.util.List;

/**
 * Controls the list of build target builders when Make is invoked for a particular scope.
 */
public final class SpoofaxBuildTargetScopeProvider extends BuildTargetScopeProvider {

    private SpoofaxPostTargetType postTargetType;

    /**
     * This instance is created by IntelliJ's plugin system.
     * Do not call this method manually.
     */
    public SpoofaxBuildTargetScopeProvider() {
        JpsPlugin.injector().injectMembers(this);
    }

    @Inject
    private void inject(SpoofaxPostTargetType postTargetType) {
        this.postTargetType = postTargetType;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public List<TargetTypeBuildScope> getBuildTargetScopes(@NotNull final CompileScope baseScope,
                                                           @NotNull final CompilerFilter filter,
                                                           @NotNull final Project project,
                                                           boolean forceBuild) {
        return Collections.singletonList(CmdlineProtoUtil.createAllTargetsScope(postTargetType, forceBuild));
    }
}
