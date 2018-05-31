package nl.knaw.huc.di.tag.model.graph;

/*-
 * #%L
 * alexandria-markup
 * =======
 * Copyright (C) 2016 - 2018 HuC DI (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import nl.knaw.huc.di.tag.model.graph.edges.Edge;
import nl.knaw.huc.di.tag.model.graph.edges.Edges;
import nl.knaw.huc.di.tag.model.graph.edges.LayerEdge;
import nl.knaw.huygens.alexandria.storage.TAGObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.alexandria.StreamUtil.stream;

@Entity(version = 1)
public class TextGraph extends HyperGraph<Long, Edge> implements TAGObject {
  Logger LOG = LoggerFactory.getLogger(getClass());
  String id = "";
  Map<String, Long> layerRootMap = new HashMap<>();
  Long firstTextNodeId;
  Long lastTextNodeId;

  @PrimaryKey(sequence = "textgraph_pk_sequence")
  private Long dbId;

  @Override
  public Long getDbId() {
    return dbId;
  }

  protected TextGraph() {
    super(GraphType.ORDERED);
  }

  public void setLayerRootMarkup(final String layerName, final Long markupNodeId) {
    layerRootMap.put(layerName, markupNodeId);
  }

  public void addChildMarkup(final Long parentMarkupId, final String layerName, final Long childMarkupId) {
    final LayerEdge edge = Edges.parentMarkupToChildMarkup(layerName);
    addDirectedHyperEdge(edge, edge.label(), parentMarkupId, childMarkupId);
  }

  public void appendTextNode(final Long textNodeId) {
    if (firstTextNodeId == null) {
      firstTextNodeId = textNodeId;
    } else {
      TextChainEdge edge = Edges.textChainEdge();
      addDirectedHyperEdge(edge, edge.label(), lastTextNodeId, textNodeId);
    }
    lastTextNodeId = textNodeId;
  }

  public void linkMarkupToTextNode(final Long markupId, final String layerName, final Long textNodeId) {
    final LayerEdge edge = Edges.markupToText(layerName);
    addDirectedHyperEdge(edge, edge.label(), markupId, textNodeId);
  }

  public Stream<Long> getTextNodeIdStream() {
    return stream(new TextNodeIdChainIterator());
  }

  public Set<String> getLayerNames() {
    return layerRootMap.keySet();
  }

  public Stream<Long> getTextNodeIdStreamForLayer(final String layerName) {
    return getTextNodeIdStream().filter(id -> belongsToLayer(id, layerName));
  }

  private boolean belongsToLayer(final Long id, final String layerName) {
    return getIncomingEdges(id).stream()
        .filter(LayerEdge.class::isInstance)
        .map(LayerEdge.class::cast)
        .anyMatch(e -> e.hasLayer(layerName));
  }

  public Stream<Long> getMarkupIdStreamForTextNodeId(final Long textNodeId, final String layerName) {
    return stream(new Iterator<Long>() {
      Optional<Long> next = getParentMarkup(textNodeId);

      private Optional<Long> getParentMarkup(Long nodeId) {
        return getIncomingEdges(nodeId).stream()
            .filter(LayerEdge.class::isInstance)
            .map(LayerEdge.class::cast)
            .filter(e -> e.hasLayer(layerName))
            .map(e -> getSource(e))
            .findFirst();
      }

      @Override
      public boolean hasNext() {
        return next.isPresent();
      }

      @Override
      public Long next() {
        Long nodeId = next.get();
        next = getParentMarkup(nodeId);
        return nodeId;
      }
    });
  }

  public Stream<Long> getMarkupIdStreamForTextNodeId(final Long textNodeId) {
    return stream(new Iterator<Long>() {
      Deque<Long> markupToProcess = new ArrayDeque<>(getParentMarkupList(textNodeId));
      Optional<Long> next = calcNext();
      Set<Long> markupHandled = new HashSet<>();

      private Optional<Long> calcNext() {
        return markupToProcess.isEmpty()
            ? Optional.empty()
            : Optional.of(markupToProcess.pop());
      }

      private List<Long> getParentMarkupList(Long nodeId) {
        List<Long> list = getIncomingEdges(nodeId).stream()
            .filter(LayerEdge.class::isInstance)
            .map(LayerEdge.class::cast)
            .map(e -> getSource(e))
            .collect(toList());
        return list;
      }

      @Override
      public boolean hasNext() {
        return next.isPresent();
      }

      @Override
      public Long next() {
        Long nodeId = next.get();
        markupHandled.add(nodeId);
        next = calcNext();
        List<Long> parentMarkupList = getParentMarkupList(nodeId);
        parentMarkupList.removeAll(markupHandled);
        markupToProcess.addAll(parentMarkupList);
        return nodeId;
      }
    });
  }

  class TextNodeIdChainIterator implements Iterator<Long> {
    Long next = firstTextNodeId;

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public Long next() {
      Long currentNode = next;
      List<Long> nextIds = getOutgoingEdges(currentNode).stream()
          .map(TextGraph.this::getTargets)
          .flatMap(Collection::stream)
          .collect(toList());
      next = nextIds.isEmpty() ? null : nextIds.get(0);
      return currentNode;
    }
  }

}
