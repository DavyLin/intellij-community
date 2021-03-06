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
package com.intellij.execution.process;

import com.intellij.openapi.util.Key;

/**
 * @author traff
 */
public class CapturingProcessAdapter extends ProcessAdapter {
  private ProcessOutput myOutput;

  public CapturingProcessAdapter(ProcessOutput output) {
    myOutput = output;
  }

  public CapturingProcessAdapter() {
    this(new ProcessOutput());
  }

  @Override
  public void onTextAvailable(ProcessEvent event, Key outputType) {
    if (outputType == ProcessOutputTypes.STDOUT) {
      myOutput.appendStdout(event.getText());
    }
    if (outputType == ProcessOutputTypes.STDERR) {
      myOutput.appendStderr(event.getText());
    }
  }

  public ProcessOutput getOutput() {
    return myOutput;
  }
}
