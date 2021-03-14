/*
   JMeld is a visual diff and merge tool.
   Copyright (C) 2007  Kees Kuip
   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.
   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.
   You should have received a copy of the GNU Lesser General Public
   License along with this library; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor,
   Boston, MA  02110-1301  USA
 */
package org.jmeld.settings.util;

import org.jmeld.util.conf.AbstractConfigurationElement;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class FilterRule
    extends AbstractConfigurationElement
{
  public enum Rule
  {
    includes("includes"),
    excludes("excludes"),
    importFilter("import filter");

    // instance variables:
    private String text;

    private Rule(String text)
    {
      this.text = text;
    }

    @Override
    public String toString()
    {
      return text;
    }
  }
  public SimpleBooleanProperty active = new SimpleBooleanProperty(false);
  public SimpleStringProperty pattern = new SimpleStringProperty();
  public SimpleObjectProperty<Rule> rule = new SimpleObjectProperty<>();
  public SimpleStringProperty description = new SimpleStringProperty();

  public FilterRule(String description,
      Rule rule,
      String pattern,
      boolean active)
  {
    setDescription(description);
    setRule(rule);
    setPattern(pattern);
    setActive(active);
  }

  public FilterRule()
  {
  }

  public void setDescription(String description)
  {
    this.description.set(description);
    fireChanged();
  }

  public String getDescription()
  {
    return description.get();
  }

  public void setRule(Rule rule)
  {
    this.rule.set(rule);
    fireChanged();
  }

  public Rule getRule()
  {
    return rule.get();
  }

  public void setPattern(String pattern)
  {
    this.pattern.set(pattern);
    fireChanged();
  }

  public String getPattern()
  {
    return pattern.get();
  }

  public void setActive(boolean active)
  {
    this.active.set(active);
    fireChanged();
  }

  public boolean isActive()
  {
    return active.get();
  }
}
