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

package com.intellij.tools;

import com.intellij.ide.macro.MacroManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;

/**
 * @author Eugene Belyaev
 */
public class ToolAction extends AnAction implements DumbAware {
  private final String myActionId;

  public ToolAction(Tool tool) {
    myActionId = tool.getActionId();
    getTemplatePresentation().setText(tool.getName(), false);
    getTemplatePresentation().setDescription(tool.getDescription());
  }

  public void actionPerformed(AnActionEvent e) {
    MacroManager.getInstance().cacheMacrosPreview(e.getDataContext());
    final HackyDataContext dataContext = new HackyDataContext(e.getDataContext());
    Tool[] tools = ToolManager.getInstance().getTools();
    for (Tool tool : tools) {
      if (myActionId.equals(tool.getActionId())) {
        tool.execute(dataContext);
        break;
      }
    }
  }
}
