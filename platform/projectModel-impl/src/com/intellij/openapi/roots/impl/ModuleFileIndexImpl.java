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

package com.intellij.openapi.roots.impl;

import com.intellij.openapi.file.exclude.ProjectFileExclusionManager;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ModuleFileIndexImpl implements ModuleFileIndex {
  private final Module myModule;
  private final FileTypeRegistry myFileTypeRegistry;
  private final DirectoryIndex myDirectoryIndex;
  private final ContentFilter myContentFilter;
  private final ProjectFileExclusionManager myExclusionManager;

  public ModuleFileIndexImpl(Module module, DirectoryIndex directoryIndex) {
    myModule = module;
    myDirectoryIndex = directoryIndex;
    myFileTypeRegistry = FileTypeRegistry.getInstance();

    myContentFilter = new ContentFilter();
    myExclusionManager = ProjectFileExclusionManager.SERVICE.getInstance(module.getProject());
  }

  @Override
  public boolean iterateContent(@NotNull ContentIterator iterator) {
    VirtualFile[] contentRoots = ModuleRootManager.getInstance(myModule).getContentRoots();
    for (VirtualFile contentRoot : contentRoots) {
      VirtualFile parent = contentRoot.getParent();
      if (parent != null) {
        DirectoryInfo parentInfo = myDirectoryIndex.getInfoForDirectory(parent);
        if (parentInfo != null && myModule.equals(parentInfo.module)) continue; // inner content - skip it
      }

      boolean finished = FileIndexImplUtil.iterateRecursively(contentRoot, myContentFilter, iterator);
      if (!finished) return false;
    }

    return true;
  }

  @Override
  public boolean iterateContentUnderDirectory(@NotNull VirtualFile dir, @NotNull ContentIterator iterator) {
    return FileIndexImplUtil.iterateRecursively(dir, myContentFilter, iterator);
  }

  @Override
  public boolean isContentSourceFile(@NotNull VirtualFile file) {
    return !file.isDirectory()
           && !myFileTypeRegistry.isFileIgnored(file)
           && isInSourceContent(file);
  }

  @Override
  public boolean isInContent(@NotNull VirtualFile fileOrDir) {
    if (fileOrDir.isDirectory()) {
      DirectoryInfo info = myDirectoryIndex.getInfoForDirectory(fileOrDir);
      return info != null && myModule.equals(info.module);
    }
    else {
      VirtualFile parent = fileOrDir.getParent();
      return parent != null && isInContent(parent);
    }
  }

  @Override
  public boolean isInSourceContent(@NotNull VirtualFile fileOrDir) {
    if (fileOrDir.isDirectory()) {
      DirectoryInfo info = myDirectoryIndex.getInfoForDirectory(fileOrDir);
      return info != null && info.isInModuleSource && myModule.equals(info.module);
    }
    else {
      VirtualFile parent = fileOrDir.getParent();
      return parent != null && isInSourceContent(parent);
    }
  }

  @Override
  @NotNull
  public List<OrderEntry> getOrderEntriesForFile(@NotNull VirtualFile fileOrDir) {
    VirtualFile dir = fileOrDir.isDirectory() ? fileOrDir : fileOrDir.getParent();
    if (dir == null) return Collections.emptyList();
    final DirectoryInfo info = myDirectoryIndex.getInfoForDirectory(dir);
    if (info == null) return Collections.emptyList();

    final List<OrderEntry> orderEntries = info.getOrderEntries();
    if (orderEntries.isEmpty()) return Collections.emptyList();

    final Module module = myModule;

    SmartList<OrderEntry> answer = null;
    final int size = orderEntries.size();
    for (int i = 0; i < size; i++) {
      OrderEntry entry = orderEntries.get(i);
      if (entry.getOwnerModule() == module) {
        if (answer == null) {
          answer = new SmartList<OrderEntry>();
        }
        answer.add(entry);
      }
    }

    return answer == null ? Collections.<OrderEntry>emptyList() : answer;
  }

  @Override
  public OrderEntry getOrderEntryForFile(@NotNull VirtualFile fileOrDir) {
    VirtualFile dir = fileOrDir.isDirectory() ? fileOrDir : fileOrDir.getParent();
    if (dir == null) return null;
    final DirectoryInfo info = myDirectoryIndex.getInfoForDirectory(dir);
    if (info == null) return null;
    final List<OrderEntry> orderEntries = info.getOrderEntries();
    //noinspection ForLoopReplaceableByForEach
    for (int i = 0; i < orderEntries.size(); i++) {
      final OrderEntry orderEntry = orderEntries.get(i);
      if (orderEntry.getOwnerModule() == myModule) return orderEntry;
    }
    return null;
  }

  @Override
  public boolean isInTestSourceContent(@NotNull VirtualFile fileOrDir) {
    if (fileOrDir.isDirectory()) {
      DirectoryInfo info = myDirectoryIndex.getInfoForDirectory(fileOrDir);
      return info != null && info.isInModuleSource && info.isTestSource && myModule.equals(info.module);
    }
    else {
      VirtualFile parent = fileOrDir.getParent();
      return parent != null && isInTestSourceContent(parent);
    }
  }

  private class ContentFilter implements VirtualFileFilter {
    @Override
    public boolean accept(@NotNull VirtualFile file) {
      if (file.isDirectory()) {
        DirectoryInfo info = myDirectoryIndex.getInfoForDirectory(file);
        return info != null && myModule.equals(info.module);
      }
      else {
        if(myExclusionManager != null && myExclusionManager.isExcluded(file)) return false;
        return !myFileTypeRegistry.isFileIgnored(file);
      }
    }
  }
}
