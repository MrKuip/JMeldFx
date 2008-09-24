package org.jmeld.util.node;

import org.jmeld.*;
import org.jmeld.diff.*;
import org.jmeld.settings.*;
import org.jmeld.ui.*;
import org.jmeld.ui.text.*;
import org.jmeld.util.*;
import org.jmeld.util.file.*;
import org.jmeld.util.file.cmd.*;

import javax.swing.tree.*;

import java.util.*;

public class JMDiffNode
    implements TreeNode
{
  public enum Compare
  {
    Equal,
    NotEqual,
    RightMissing,
    LeftMissing,
    BothMissing;
  }
  private String           text;
  private String           name;
  private String           id;
  private String           shortName;
  private String           parentName;
  private JMDiffNode       parent;
  private List<JMDiffNode> children;
  private BufferNode       nodeLeft;
  private BufferNode       nodeRight;
  private boolean          leaf;
  private Compare          compareState;
  private JMDiff           diff;
  private JMRevision       revision;

  public JMDiffNode(String name, boolean leaf)
  {
    this.name = name;
    this.shortName = name;
    this.leaf = leaf;

    children = new ArrayList();
    calculateNames();
  }

  public String getId()
  {
    return id;
  }

  private void initId()
  {
    id = (nodeLeft != null ? nodeLeft.getName() : "x")
         + (nodeRight != null ? nodeRight.getName() : "x");
  }

  public String getName()
  {
    return name;
  }

  public String getShortName()
  {
    return shortName;
  }

  public String getParentName()
  {
    return parentName;
  }

  public void addChild(JMDiffNode child)
  {
    children.add(child);
    child.setParent(this);
  }

  private void setParent(JMDiffNode parent)
  {
    this.parent = parent;
  }

  public void setBufferNodeLeft(BufferNode bufferNode)
  {
    nodeLeft = bufferNode;
    initId();
  }

  public BufferNode getBufferNodeLeft()
  {
    return nodeLeft;
  }

  public void setBufferNodeRight(BufferNode bufferNode)
  {
    nodeRight = bufferNode;
    initId();
  }

  public BufferNode getBufferNodeRight()
  {
    return nodeRight;
  }

  public List<JMDiffNode> getChildren()
  {
    return children;
  }

  @Override
  public Enumeration<JMDiffNode> children()
  {
    return Collections.enumeration(children);
  }

  @Override
  public boolean getAllowsChildren()
  {
    return isLeaf();
  }

  @Override
  public JMDiffNode getChildAt(int childIndex)
  {
    return children.get(childIndex);
  }

  @Override
  public int getChildCount()
  {
    return children.size();
  }

  @Override
  public int getIndex(TreeNode node)
  {
    return children.indexOf(node);
  }

  @Override
  public JMDiffNode getParent()
  {
    return parent;
  }

  @Override
  public boolean isLeaf()
  {
    return leaf;
  }

  private void calculateNames()
  {
    int index;

    index = name.lastIndexOf("/");
    if (index == -1)
    {
      parentName = null;
      return;
    }

    parentName = name.substring(0, index);
    shortName = name.substring(index + 1);
  }

  public AbstractCmd getCopyToRightCmd()
      throws Exception
  {
    // TODO: This is NOT OO!
    if (nodeLeft.exists() && nodeLeft instanceof FileNode
        && nodeRight instanceof FileNode)
    {
      return new CopyFileCmd(this, (FileNode) nodeLeft, (FileNode) nodeRight);
    }

    return null;
  }

  public AbstractCmd getCopyToLeftCmd()
      throws Exception
  {
    // TODO: This is NOT OO!
    if (nodeRight.exists() && nodeLeft instanceof FileNode
        && nodeRight instanceof FileNode)
    {
      return new CopyFileCmd(this, (FileNode) nodeRight, (FileNode) nodeLeft);
    }

    return null;
  }

  public AbstractCmd getRemoveLeftCmd()
      throws Exception
  {
    // TODO: This is NOT OO!
    if (nodeLeft instanceof FileNode)
    {
      return new RemoveFileCmd(this, (FileNode) nodeLeft);
    }

    return null;
  }

  public AbstractCmd getRemoveRightCmd()
      throws Exception
  {
    // TODO: This is NOT OO!
    if (nodeRight instanceof FileNode)
    {
      return new RemoveFileCmd(this, (FileNode) nodeRight);
    }

    return null;
  }

  public void compareContents()
  {
    boolean equals;
    Ignore ignore;

    if (!nodeLeft.exists() && !nodeRight.exists())
    {
      compareState = Compare.BothMissing;
      return;
    }

    if (nodeLeft.exists() && !nodeRight.exists())
    {
      compareState = Compare.RightMissing;
      return;
    }

    if (!nodeLeft.exists() && nodeRight.exists())
    {
      compareState = Compare.LeftMissing;
      return;
    }

    ignore = JMeldSettings.getInstance().getEditor().getIgnore();

    equals = CompareUtil.contentEquals(nodeLeft, nodeRight, ignore);
    compareState = equals ? Compare.Equal : Compare.NotEqual;
  }

  public void diff()
      throws JMeldException
  {
    BufferDocumentIF documentLeft;
    BufferDocumentIF documentRight;

    StatusBar.getInstance().start();

    documentLeft = null;
    documentRight = null;

    if (nodeLeft != null)
    {
      documentLeft = nodeLeft.getDocument();
      StatusBar.getInstance().setState("Reading left : %s", nodeLeft.getName());
      if (documentLeft != null)
      {
        documentLeft.read();
      }
    }

    if (nodeRight != null)
    {
      documentRight = nodeRight.getDocument();
      StatusBar.getInstance()
          .setState("Reading right: %s", nodeRight.getName());
      if (documentRight != null)
      {
        documentRight.read();
      }
    }

    StatusBar.getInstance().setState("Calculating differences");
    diff = new JMDiff();
    revision = diff.diff(documentLeft == null ? null : documentLeft.getLines(),
                         documentRight == null ? null : documentRight
                             .getLines());
    StatusBar.getInstance().setState("Ready calculating differences");
    StatusBar.getInstance().stop();
  }

  public JMDiff getDiff()
  {
    return diff;
  }

  public JMRevision getRevision()
  {
    return revision;
  }

  public boolean isCompareEqual(Compare state)
  {
    return compareState == state;
  }

  public void print(String indent)
  {
    System.out.println(indent + shortName + " (" + compareState + ")");
    indent += "  ";
    for (JMDiffNode node : children)
    {
      node.print(indent);
    }
  }

  @Override
  public String toString()
  {
    String pn;

    if (text == null)
    {
      text = name;
      if (parent != null)
      {
        pn = parent.getName();
        if (name.startsWith(pn))
        {
          text = name.substring(pn.length() + 1);
        }
      }
    }

    return text;
  }
}
