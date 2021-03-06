/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.groovy.lang.psi.api;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil;

/**
* @author Max Medvedev
*/
public final class SpreadState {
  public static final Key<SpreadState> SPREAD_STATE = Key.create("Spread state");

  private final PsiType containerType;
  @Nullable private final SpreadState innerState;

  public SpreadState(PsiType type, @Nullable SpreadState state) {
    containerType = type;
    innerState = state;
  }

  public PsiType getContainerType() {
    return containerType;
  }

  @Nullable
  public SpreadState getInnerState() {
    return innerState;
  }

  public static SpreadState create(PsiType type, @Nullable SpreadState state) {
    return new SpreadState(type, state);
  }

  public static PsiType apply(PsiType item, @Nullable SpreadState state, Project project) {
    if (state == null) return item;
    return apply(TypesUtil.createSimilarCollection(state.getContainerType(), project, item), state.getInnerState(), project);
  }
}
