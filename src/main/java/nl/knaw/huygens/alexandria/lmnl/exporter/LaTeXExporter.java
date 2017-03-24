package nl.knaw.huygens.alexandria.lmnl.exporter;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huygens.alexandria.freemarker.FreeMarker;
import nl.knaw.huygens.alexandria.lmnl.data_model.Document;
import nl.knaw.huygens.alexandria.lmnl.data_model.IndexPoint;
import nl.knaw.huygens.alexandria.lmnl.data_model.KdTree;
import nl.knaw.huygens.alexandria.lmnl.data_model.Limen;
import nl.knaw.huygens.alexandria.lmnl.data_model.NodeRangeIndex;
import nl.knaw.huygens.alexandria.lmnl.data_model.TextNode;
import nl.knaw.huygens.alexandria.lmnl.data_model.TextRange;

public class LaTeXExporter {
  private static Logger LOG = LoggerFactory.getLogger(LaTeXExporter.class);
  public static final Comparator<TextRangeLayer> ON_MAX_RANGE_SIZE = Comparator.comparing(TextRangeLayer::getTag)//
      .thenComparing(Comparator.comparingInt(TextRangeLayer::getMaxRangeSize));
  private List<IndexPoint> indexPoints;
  private Limen limen;
  private Set<Integer> longTextRangeIndexes;
  private NodeRangeIndex index;

  public LaTeXExporter(Document document) {
    this.limen = document.value();
  }

  public String exportTextRangeOverlap() {
    Map<String, Object> map = new HashMap<>();
    int maxTextRangesPerTextNode = limen.textNodeList.parallelStream()//
        .map(limen::getTextRanges)//
        .mapToInt(Set::size)//
        .max()//
        .getAsInt();
    map.put("maxdepth", maxTextRangesPerTextNode);
    StringBuilder latexBuilder = new StringBuilder();
    if (limen != null) {
      limen.getTextNodeIterator().forEachRemaining(tn -> {
        int size = limen.getTextRanges(tn).size();
        addColoredTextNode(latexBuilder, tn, size);
      });
    }
    map.put("body", latexBuilder.toString());
    return FreeMarker.templateToString("colored-text.tex.ftl", map, this.getClass());
  }

  private void addColoredTextNode(StringBuilder latexBuilder, TextNode tn, int depth) {
    String content = tn.getContent();
    if ("\n".equals(content)) {
      latexBuilder.append("\\TextNode{").append(depth).append("}{\\n}\\\\\n");
    } else {
      String[] parts = content.split("\n");
      List<String> colorboxes = new ArrayList<>();
      for (int i = 0; i < parts.length; i++) {
        String part = parts[i];
        if (i < parts.length - 1) {
          part += "\\n";
        }
        colorboxes.add("\\TextNode{" + depth + "}{" + part + "}");
      }
      latexBuilder.append(colorboxes.stream().collect(Collectors.joining("\\\\\n")));
    }
  }

  public String exportDocument() {
    Map<String, Object> map = new HashMap<>();
    StringBuilder latexBuilder = new StringBuilder();
    appendLimen(latexBuilder, limen);
    map.put("body", latexBuilder.toString());
    return FreeMarker.templateToString("document.tex.ftl", map, this.getClass());
  }

  private void appendLimen(StringBuilder latexBuilder, Limen limen) {
    ColorPicker colorPicker = new ColorPicker("blue", "brown", "cyan", "darkgray", "gray", "green", "lightgray", //
        "lime", "magenta", "olive", "orange", "pink", "purple", "red", "teal", "violet", "black");
    latexBuilder.append("\n    % TextNodes\n");
    if (limen != null) {
      Set<TextRange> openTextRanges = new LinkedHashSet<>();
      AtomicInteger textNodeCounter = new AtomicInteger(0);
      Map<TextNode, Integer> textNodeIndices = new HashMap<>();
      limen.getTextNodeIterator().forEachRemaining(tn -> {
        int i = textNodeCounter.getAndIncrement();
        textNodeIndices.put(tn, i);
        Set<TextRange> textRanges = limen.getTextRanges(tn);

        List<TextRange> toClose = new ArrayList<>();
        toClose.addAll(openTextRanges);
        toClose.removeAll(textRanges);
        Collections.reverse(toClose);

        List<TextRange> toOpen = new ArrayList<>();
        toOpen.addAll(textRanges);
        toOpen.removeAll(openTextRanges);

        openTextRanges.removeAll(toClose);
        openTextRanges.addAll(toOpen);

        addTextNode(latexBuilder, tn, i);
      });

      connectTextNodes(latexBuilder, textNodeCounter);
      markTextRanges(latexBuilder, limen, colorPicker, textNodeIndices);
      // drawTextRangesAsSets(latexBuilder, limen, colorPicker, textNodeIndices);
    }
  }

  private void addTextNode(StringBuilder latexBuilder, TextNode tn, int i) {
    String content = tn.getContent()//
        .replaceAll(" ", "\\\\s ")//
        .replaceAll("\n", "\\\\n ");
    String relPos = i == 0 ? "below=of doc" : ("right=of tn" + (i - 1));
    String nodeLine = "    \\node[textnode] (tn" + i + ") [" + relPos + "] {" + content + "};\n";
    latexBuilder.append(nodeLine);
  }

  public String exportMatrix() {
    Map<String, Object> map = new HashMap<>();
    String body = exportMatrix(limen.textNodeList, limen.textRangeList, getIndexPoints(), getLongTextRangeIndexes());
    map.put("body", body);
    return FreeMarker.templateToString("matrix.tex.ftl", map, this.getClass());
  }

  private String exportMatrix(List<TextNode> allTextNodes, List<TextRange> allTextRanges, List<IndexPoint> indexPoints, Set<Integer> longTextRangeIndexes) {
    List<String> rangeLabels = allTextRanges.stream().map(TextRange::getTag).collect(Collectors.toList());
    List<String> rangeIndex = new ArrayList<>();
    rangeIndex.add("");
    for (int i = 0; i < rangeLabels.size(); i++) {
      rangeIndex.add(String.valueOf(i));
    }
    rangeLabels.add(0, "");
    rangeLabels.add("");
    String tabularContent = StringUtils.repeat("l|", rangeLabels.size() - 1) + "l";
    StringBuilder latexBuilder = new StringBuilder()//
        .append("\\begin{tabular}{").append(tabularContent).append("}\n")//
        .append(rangeIndex.stream().map(c -> "$" + c + "$").collect(Collectors.joining(" & "))).append("\\\\\n")//
        .append("\\hline\n")//
    ;

    Iterator<IndexPoint> pointIterator = indexPoints.iterator();
    IndexPoint indexPoint = pointIterator.next();
    for (int i = 0; i < allTextNodes.size(); i++) {
      List<String> row = new ArrayList<>();
      row.add(String.valueOf(i));
      for (int j = 0; j < allTextRanges.size(); j++) {
        if (i == indexPoint.getTextNodeIndex() && j == indexPoint.getTextRangeIndex()) {
          String cell = longTextRangeIndexes.contains(j) ? "\\underline{X}" : "X";
          row.add(cell);
          if (pointIterator.hasNext()) {
            indexPoint = pointIterator.next();
          }

        } else {
          row.add(" ");
        }
      }
      String content = allTextNodes.get(i).getContent()//
          .replaceAll(" ", "\\\\s ")//
          .replaceAll("\n", "\\\\n ");
      row.add(content);
      latexBuilder.append(row.stream().collect(Collectors.joining(" & "))).append("\\\\ \\hline\n");
    }

    latexBuilder
        .append(rangeLabels.stream()//
            .map(c -> "\\rot{$" + c + "$}")//
            .collect(Collectors.joining(" & ")))//
        .append("\\\\\n")//
        .append("\\end{tabular}\n");
    return latexBuilder.toString();
  }

  public String exportKdTree() {
    Map<String, Object> map = new HashMap<>();
    String kdTree = exportKdTree(getIndex().getKdTree(), getLongTextRangeIndexes());
    map.put("body", kdTree);
    return FreeMarker.templateToString("kdtree.tex.ftl", map, this.getClass());
  }

  private String exportKdTree(KdTree<IndexPoint> kdTree, Set<Integer> longTextRangeIndexes) {
    StringBuilder latexBuilder = new StringBuilder();
    KdTree.KdNode root = kdTree.getRoot();
    IndexPoint rootIP = root.getContent();
    String content = toNodeContent(rootIP, longTextRangeIndexes);
    latexBuilder.append("\\Tree [.\\node[textNodeAxis]{").append(content).append("};\n");
    appendChildTree(latexBuilder, root.getLesser(), "textRangeAxis", longTextRangeIndexes);
    appendChildTree(latexBuilder, root.getGreater(), "textRangeAxis", longTextRangeIndexes);
    return latexBuilder.append("]\\\\\n").toString();
  }

  public String exportGradient() {
    Map<String, Object> map = new HashMap<>();
    StringBuilder latexBuilder = new StringBuilder();
    appendGradedLimen(latexBuilder, limen);
    map.put("body", latexBuilder.toString());
    return FreeMarker.templateToString("gradient.tex.ftl", map, this.getClass());
  }

  private List<IndexPoint> getIndexPoints() {
    if (indexPoints == null) {
      indexPoints = getIndex().getIndexPoints();
    }
    return indexPoints;
  }

  private NodeRangeIndex getIndex() {
    if (index == null) {
      index = new NodeRangeIndex(limen);
    }
    return index;
  }

  private Set<Integer> getLongTextRangeIndexes() {
    if (longTextRangeIndexes == null) {
      longTextRangeIndexes = new HashSet<>();
      for (int i = 0; i < limen.textRangeList.size(); i++) {
        if (limen.containsAtLeastHalfOfAllTextNodes(limen.textRangeList.get(i))) {
          longTextRangeIndexes.add(i);
        }
      }
    }
    return longTextRangeIndexes;
  }

  private void appendGradedLimen(StringBuilder latexBuilder, Limen limen) {
    int maxTextRangesPerTextNode = limen.textNodeList.parallelStream().map(limen::getTextRanges).mapToInt(Set::size).max().getAsInt();
    latexBuilder.append("\n    % TextNodes\n");
    if (limen != null) {
      Set<TextRange> openTextRanges = new LinkedHashSet<>();
      AtomicInteger textNodeCounter = new AtomicInteger(0);
      Map<TextNode, Integer> textNodeIndices = new HashMap<>();
      limen.getTextNodeIterator().forEachRemaining(tn -> {
        int i = textNodeCounter.getAndIncrement();
        textNodeIndices.put(tn, i);
        Set<TextRange> textRanges = limen.getTextRanges(tn);

        List<TextRange> toClose = new ArrayList<>();
        toClose.addAll(openTextRanges);
        toClose.removeAll(textRanges);
        Collections.reverse(toClose);

        List<TextRange> toOpen = new ArrayList<>();
        toOpen.addAll(textRanges);
        toOpen.removeAll(openTextRanges);

        openTextRanges.removeAll(toClose);
        openTextRanges.addAll(toOpen);

        int size = limen.getTextRanges(tn).size();
        float gradient = size / (float) maxTextRangesPerTextNode;

        int r = 256 - Math.round(255 * gradient);
        int g = 255;
        int b = 256 - Math.round(255 * gradient);
        Color color = new Color(r, g, b);
        String fillColor = ColorUtil.toLaTeX(color);

        addGradedTextNode(latexBuilder, tn, i, fillColor, size);
      });

    }
  }

  // private void drawTextRangesAsSets(StringBuilder latexBuilder, Limen limen, ColorPicker colorPicker, Map<TextNode, Integer> textNodeIndices) {
  // limen.textRangeList.forEach(tr -> {
  // String color = colorPicker.nextColor();
  // latexBuilder.append(" \\node[draw=").append(color).append(",shape=rectangle,fit=");
  // tr.textNodes.forEach(tn -> {
  // int i = textNodeIndices.get(tn);
  // latexBuilder.append("(tn").append(i).append(")");
  // });
  // latexBuilder.append(",label={[").append(color).append("]below:$").append(tr.getTag()).append("$}]{};\n");
  // });
  // }

  private void addGradedTextNode(StringBuilder latexBuilder, TextNode tn, int i, String fillColor, int size) {
    String content = tn.getContent()//
        .replaceAll(" ", "\\\\s ")//
        .replaceAll("\n", "\\\\n ")//
    ;
    String relPos = i == 0 ? "" : "right=0 of tn" + (i - 1);
    String nodeLine = "    \\node[textnode,fill=" + fillColor + "] (tn" + i + ") [" + relPos + "] {" + content + "};\n";
    latexBuilder.append(nodeLine);
  }

  private void connectTextNodes(StringBuilder latexBuilder, AtomicInteger textNodeCounter) {
    latexBuilder.append("");
    latexBuilder.append("\n    % connect TextNodes\n    \\graph{").append("(doc)");
    for (int i = 0; i < textNodeCounter.get(); i++) {
      latexBuilder.append(" -> (tn").append(i).append(")");
    }
    latexBuilder.append("};\n");
  }

  private void markTextRanges(StringBuilder latexBuilder, Limen limen, ColorPicker colorPicker, Map<TextNode, Integer> textNodeIndices) {
    // AtomicInteger textRangeCounter = new AtomicInteger(0);
    latexBuilder.append("\n    % TextRanges");
    Map<TextRange, Integer> layerIndex = calculateLayerIndex(limen.textRangeList, textNodeIndices);
    limen.textRangeList.forEach(tr -> {
      int rangeLayerIndex = layerIndex.get(tr);
      float textRangeRow = 0.75f * (rangeLayerIndex + 1);
      String color = colorPicker.nextColor();
      if (tr.isContinuous()) {
        TextNode firstTextNode = tr.textNodes.get(0);
        TextNode lastTextNode = tr.textNodes.get(tr.textNodes.size() - 1);
        int first = textNodeIndices.get(firstTextNode);
        int last = textNodeIndices.get(lastTextNode);

        appendTextRange(latexBuilder, tr, String.valueOf(rangeLayerIndex), textRangeRow, color, first, last);

      } else {
        Iterator<TextNode> textNodeIterator = tr.textNodes.iterator();
        TextNode firstTextNode = textNodeIterator.next();
        TextNode lastTextNode = firstTextNode;
        boolean finished = false;
        int partNo = 0;
        while (!finished) {
          TextNode expectedNextNode = firstTextNode.getNextTextNode();
          boolean goOn = textNodeIterator.hasNext();
          while (goOn) {
            TextNode nextTextNode = textNodeIterator.next();
            if (nextTextNode.equals(expectedNextNode)) {
              lastTextNode = nextTextNode;
              expectedNextNode = lastTextNode.getNextTextNode();
              goOn = textNodeIterator.hasNext();

            } else {
              appendTextRangePart(latexBuilder, textNodeIndices, tr, rangeLayerIndex, textRangeRow, color, firstTextNode, lastTextNode, partNo);
              firstTextNode = nextTextNode;
              lastTextNode = firstTextNode;
              partNo++;
              goOn = false;
            }
          }

          finished = finished || !textNodeIterator.hasNext();
        }

        appendTextRangePart(latexBuilder, textNodeIndices, tr, rangeLayerIndex, textRangeRow, color, firstTextNode, lastTextNode, partNo);

        for (int i = 0; i < partNo; i++) {
          String leftNode = MessageFormat.format("tr{0}_{1}e", rangeLayerIndex, i);
          String rightNode = MessageFormat.format("tr{0}_{1}b", rangeLayerIndex, (i + 1));
          latexBuilder.append("    \\draw[densely dashed,color=").append(color).append("] (").append(leftNode).append(".south) to [out=350,in=190] (").append(rightNode).append(".south);\n");
        }
      }

    });
  }

  private void appendTextRangePart(StringBuilder latexBuilder, Map<TextNode, Integer> textNodeIndices, TextRange tr, int rangeLayerIndex, float textRangeRow, String color, TextNode firstTextNode,
      TextNode lastTextNode, int partNo) {
    int first = textNodeIndices.get(firstTextNode);
    int last = textNodeIndices.get(lastTextNode);
    String textRangePartNum = String.valueOf(rangeLayerIndex) + "_" + partNo;
    appendTextRange(latexBuilder, tr, textRangePartNum, textRangeRow, color, first, last);
  }

  private void appendTextRange(StringBuilder latexBuilder, TextRange tr, String rangeLayerIndex, float textRangeRow, String color, int first, int last) {
    latexBuilder.append("\n    \\node[label=below right:{$")//
        .append(tr.getTag())//
        .append("$}](tr")//
        .append(rangeLayerIndex)//
        .append("b)[below left=")//
        .append(textRangeRow)//
        .append(" and 0 of tn")//
        .append(first)//
        .append("]{};\n");
    latexBuilder.append("    \\node[](tr")//
        .append(rangeLayerIndex)//
        .append("e)[below right=")//
        .append(textRangeRow)//
        .append(" and 0 of tn")//
        .append(last)//
        .append("]{};\n");
    latexBuilder.append("    \\draw[Bar-Bar,thick,color=")//
        .append(color).append("] (tr")//
        .append(rangeLayerIndex)//
        .append("b) -- (tr")//
        .append(rangeLayerIndex)//
        .append("e);\n");
    latexBuilder.append("    \\draw[thin,color=lightgray] (tr")//
        .append(rangeLayerIndex)//
        .append("b.east) -- (tn")//
        .append(first)//
        .append(".west);\n");
    latexBuilder.append("    \\draw[thin,color=lightgray] (tr")//
        .append(rangeLayerIndex)//
        .append("e.west) -- (tn")//
        .append(last)//
        .append(".east);\n");
  }

  private static class TextRangeLayer {
    final Map<TextNode, Integer> textNodeIndex;

    final List<TextRange> textRanges = new ArrayList<>();
    final Set<String> tags = new HashSet<>();
    int maxRangeSize = 1; // the number of textnodes of the biggest textrange
    int lastTextNodeUsed = 0;

    TextRangeLayer(Map<TextNode, Integer> textNodeIndex) {
      this.textNodeIndex = textNodeIndex;
    }

    public void addTextRange(TextRange textRange) {
      textRanges.add(textRange);
      tags.add(normalize(textRange.getTag()));
      maxRangeSize = Math.max(maxRangeSize, textRange.textNodes.size());
      lastTextNodeUsed = textNodeIndex.get(textRange.textNodes.get(textRange.textNodes.size() - 1));
    }

    public List<TextRange> getTextRanges() {
      return textRanges;
    }

    public boolean hasTag(String tag) {
      return tags.contains(tag);
    }

    public String getTag() {
      return tags.iterator().next();
    }

    public int getMaxRangeSize() {
      return maxRangeSize;
    }

    public boolean canAdd(TextRange textRange) {
      String nTag = normalize(textRange.getTag());
      if (!tags.contains(nTag)) {
        return false;
      }
      TextNode firstTextNode = textRange.textNodes.get(0);
      int firstTextNodeIndex = textNodeIndex.get(firstTextNode);
      return (firstTextNodeIndex > lastTextNodeUsed);
    }

    private String normalize(String tag) {
      return tag.replaceFirst("=.*$", "");
    }
  }

  private Map<TextRange, Integer> calculateLayerIndex(List<TextRange> textranges, Map<TextNode, Integer> textNodeIndex) {
    List<TextRangeLayer> layers = new ArrayList<>();
    textranges.forEach(tr -> {
      Optional<TextRangeLayer> oLayer = layers.stream().filter(layer -> layer.canAdd(tr)).findFirst();
      if (oLayer.isPresent()) {
        oLayer.get().addTextRange(tr);

      } else {
        TextRangeLayer layer = new TextRangeLayer(textNodeIndex);
        layer.addTextRange(tr);
        layers.add(layer);
      }
    });

    AtomicInteger layerCounter = new AtomicInteger();
    Map<TextRange, Integer> index = new HashMap<>();
    layers.stream().sorted(ON_MAX_RANGE_SIZE).forEach(layer -> {
      int i = layerCounter.getAndIncrement();
      layer.getTextRanges().forEach(tr -> index.put(tr, i));
    });

    return index;
  }

  private String toNodeContent(IndexPoint indexPoint, Set<Integer> longTextRangeIndexes) {
    String content = indexPoint.toString();
    if (longTextRangeIndexes.contains(indexPoint.getTextRangeIndex())) {
      return "\\underline{" + content + "}";
    }
    return content;
  }

  private void appendChildTree(StringBuilder latexBuilder, KdTree.KdNode kdnode, String style, Set<Integer> longTextRangeIndexes) {
    if (kdnode == null) {
      latexBuilder.append("[.\\node[").append(style).append("]{};\n]\n");
      return;
    }
    IndexPoint indexPoint = kdnode.getContent();
    String content = toNodeContent(indexPoint, longTextRangeIndexes);
    latexBuilder.append("[.\\node[").append(style).append("]{").append(content).append("};\n");
    String nextStyle = (style.equals("textNodeAxis") ? "textRangeAxis" : "textNodeAxis");
    if (!(kdnode.getLesser() == null && kdnode.getGreater() == null)) {
      appendChildTree(latexBuilder, kdnode.getLesser(), nextStyle, longTextRangeIndexes);
      appendChildTree(latexBuilder, kdnode.getGreater(), nextStyle, longTextRangeIndexes);
    }
    latexBuilder.append("]\n");
  }

}
