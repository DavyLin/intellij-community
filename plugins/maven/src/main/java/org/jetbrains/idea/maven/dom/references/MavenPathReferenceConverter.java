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
package org.jetbrains.idea.maven.dom.references;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.converters.PathReferenceConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenPropertyResolver;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;

import java.util.Collection;

/**
 * @author Sergey Evdokimov
 */
public class MavenPathReferenceConverter extends PathReferenceConverter {

  private final Condition<PsiFileSystemItem> myCondition;

  public MavenPathReferenceConverter() {
    this(Condition.TRUE);
  }

  public MavenPathReferenceConverter(@NotNull Condition<PsiFileSystemItem> condition) {
    myCondition = condition;
  }

  @NotNull
  @Override
  public PsiReference[] createReferences(final GenericDomValue genericDomValue, PsiElement element, ConvertContext context) {
    ElementManipulator<PsiElement> manipulator = ElementManipulators.getManipulator(element);
    TextRange range = manipulator.getRangeInElement(element);
    String text = range.substring(element.getText());

    FileReferenceSet set = new FileReferenceSet(text, element, range.getStartOffset(), null, SystemInfo.isFileSystemCaseSensitive, false) {

      private MavenDomProjectModel model;

      @Override
      protected Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
        return myCondition;
      }

      @Override
      protected boolean isSoft() {
        return true;
      }

      @Override
      public FileReference createFileReference(TextRange range, int index, String text) {
        return new FileReference(this, range, index, text) {
          @Override
          protected void innerResolveInContext(@NotNull String text,
                                               @NotNull PsiFileSystemItem context,
                                               Collection<ResolveResult> result,
                                               boolean caseSensitive) {
            if (model == null) {
              model = (MavenDomProjectModel)DomUtil.getFileElement(genericDomValue).getRootElement();
            }

            String resolvedText = MavenPropertyResolver.resolve(text, model);

            if (resolvedText.equals(text)) {
              super.innerResolveInContext(resolvedText, context, result, caseSensitive);
            }
            else {
              VirtualFile contextFile = context.getVirtualFile();
              if (contextFile == null) return;

              VirtualFile file = null;

              if (getIndex() == 0) {
                file = LocalFileSystem.getInstance().findFileByPath(resolvedText);
              }

              if (file == null) {
                file = LocalFileSystem.getInstance().findFileByPath(contextFile.getPath() + '/' + resolvedText);
              }

              if (file != null) {
                PsiFileSystemItem res = file.isDirectory() ? context.getManager().findDirectory(file) : context.getManager().findFile(file);

                if (res != null) {
                  result.add(new PsiElementResolveResult(res));
                }
              }
            }
          }
        };
      }
    };

    return set.getAllReferences();
  }

  @NotNull
  @Override
  public PsiReference[] createReferences(@NotNull PsiElement psiElement, boolean soft) {
    throw new UnsupportedOperationException();
  }
}
