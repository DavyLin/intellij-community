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
package com.intellij.designer.palette;

import com.intellij.designer.model.MetaModel;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Alexander Lobas
 */
public final class DefaultPaletteItem implements PaletteItem {
  private final String myTitle;
  private final String myIconPath;
  private Icon myIcon;
  private final String myTooltip;
  private final String myVersion;

  private MetaModel myMetaModel;

  public DefaultPaletteItem(String title, String iconPath, String tooltip, String version) {
    myTitle = title;
    myIconPath = iconPath;
    myTooltip = tooltip;
    myVersion = version;
  }

  @Override
  public String getTitle() {
    return myTitle;
  }

  @Override
  public Icon getIcon() {
    if (myIcon == null) {
      myIcon = IconLoader.findIcon(myIconPath, myMetaModel.getModel());
    }
    return myIcon;
  }

  @Override
  public String getTooltip() {
    return myTooltip;
  }

  @Override
  public String getVersion() {
    return myVersion;
  }

  public MetaModel getMetaModel() {
    return myMetaModel;
  }

  public void setMetaModel(MetaModel metaModel) {
    myMetaModel = metaModel;
  }
}