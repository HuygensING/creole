package nl.knaw.huygens.alexandria.storage;

/*
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

import nl.knaw.huc.di.tag.tagml.TAGML;
import nl.knaw.huygens.alexandria.storage.dto.TAGMarkupDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class TAGMarkup {
  private final TAGStore store;
  private final TAGMarkupDTO markupDTO;
  private final TAGDocument document;

  public TAGMarkup(TAGStore store, TAGMarkupDTO markupDTO) {
    checkNotNull(store);
    checkNotNull(markupDTO);
    this.store = store;
    this.markupDTO = markupDTO;
    this.document = store.getDocument(markupDTO.getDocumentId());
    update();
  }

  public Long getDbId() {
    return markupDTO.getDbId();
  }

  public String getTag() {
    return markupDTO.getTag();
  }

//  @Deprecated
//  public TAGMarkup addTextNode(TAGTextNode tagTextNode) {
//    markupDTO.getTextNodeIds().add(tagTextNode.getDbId());
//    Long ownerId = markupDTO.getDocumentId();
//    new TAGDocument(store, store.getDocumentDTO(ownerId))//
//        .associateTextNodeWithMarkupForLayer(tagTextNode, this, "");
//    update();
//    return this;
//  }

//  public TAGMarkup setOnlyTextNode(TAGTextNode tagTextNode) {
//    markupDTO.getTextNodeIds().clear();
//    addTextNode(tagTextNode);
//    return this;
//  }

//  public TAGMarkup setFirstAndLastTextNode(TAGTextNode first, TAGTextNode last) {
//    markupDTO.getTextNodeIds().clear();
//    addTextNode(first);
//    if (!first.getDbId().equals(last.getDbId())) {
//      TAGTextNode next = first.getNextTextNodes().get(0); // TODO: handle divergence
//      while (!next.getDbId().equals(last.getDbId())) {
//        addTextNode(next);
//        next = next.getNextTextNodes().get(0);// TODO: handle divergence
//      }
//      addTextNode(next);
//    }
//    update();
//    return this;
//  }

  public TAGMarkup addAnnotation(TAGAnnotation annotation) {
    markupDTO.getAnnotationIds().add(annotation.getDbId());
    update();
    return this;
  }

  public Stream<TAGAnnotation> getAnnotationStream() {
    return markupDTO.getAnnotationIds().stream()//
        .map(store::getAnnotationDTO)//
        .map(annotation -> new TAGAnnotation(store, annotation));
  }

  public Stream<TAGTextNode> getTextNodeStream() {
    return document.getTextNodeStreamForMarkup(this);
  }

  public Stream<TAGTextNode> getTextNodeStreamForLayer(String layerName) {
    return document.getTextNodeStreamForMarkupInLayer(this, layerName);
  }

  public TAGMarkupDTO getDTO() {
    return markupDTO;
  }

  public void setIsDiscontinuous(final boolean b) {
    markupDTO.setDiscontinuous(b);
  }

  public boolean isDiscontinuous() {
    return markupDTO.isDiscontinuous();
  }

  public boolean isContinuous() {
    return !markupDTO.isDiscontinuous();
  }

  public String getExtendedTag() {
    String layerSuffix = layerSuffix();
    String tag = getTag();
    if (isOptional()) {
      return layerSuffix + TAGML.OPTIONAL_PREFIX + tag;
    }
    // TODO: this is output language dependent: move to language dependency
    String suffix = getSuffix();
    if (StringUtils.isNotEmpty(suffix)) {
      return tag + "~" + suffix + layerSuffix;
    }
    return tag + layerSuffix;
  }

  public boolean hasN() {
    return getAnnotationStream()//
        .map(TAGAnnotation::getTag) //
        .anyMatch("n"::equals);
  }

  public String getSuffix() {
    return markupDTO.getSuffix();
  }

  public Optional<TAGMarkup> getDominatedMarkup() {
    return markupDTO.getDominatedMarkupId()
        .map(store::getMarkupDTO)
        .map(m -> new TAGMarkup(store, m));
  }

  public void setDominatedMarkup(TAGMarkup dominatedMarkup) {
    markupDTO.setDominatedMarkupId(dominatedMarkup.getDbId());
    if (!dominatedMarkup.getDTO().getDominatingMarkupId().isPresent()) {
      dominatedMarkup.setDominatingMarkup(this);
    }
    update();
  }

  public Optional<TAGMarkup> getDominatingMarkup() {
    return markupDTO.getDominatingMarkupId()
        .map(store::getMarkupDTO)
        .map(m -> new TAGMarkup(store, m));
  }

  public boolean hasMarkupId() {
    return markupDTO.getMarkupId() != null;
  }

  public String getMarkupId() {
    return markupDTO.getMarkupId();
  }

  public boolean isOptional() {
    return markupDTO.isOptional();
  }

  public TAGMarkup setOptional(boolean optional) {
    markupDTO.setOptional(optional);
    return this;
  }

  public TAGMarkup setMarkupId(String id) {
    markupDTO.setMarkupId(id);
    return this;
  }

  public boolean hasTag(String tag) {
    return tag.equals(markupDTO.getTag());
  }

  public void setSuffix(String suffix) {
    markupDTO.setSuffix(suffix);
  }

  public TAGMarkup addAllLayers(final Set<String> layers) {
    markupDTO.addAllLayers(layers);
    return this;
  }

  public Set<String> getLayers() {
    return markupDTO.getLayers();
  }

  public boolean isAnonymous() {
    List<TAGTextNode> textNodesForMarkup = document.getTextNodeStreamForMarkup(this)
        .collect(toList());
    return textNodesForMarkup.size() == 1 // markup has just 1 textnode
        && textNodesForMarkup.get(0).getText().isEmpty();  // and it's empty
  }

  public boolean matches(TAGMarkup other) {
    if (!other.getExtendedTag().equals(getExtendedTag())) {
      return false;
    }

    int thisAnnotationCount = markupDTO.getAnnotationIds().size();
    int otherAnnotationCount = other.getDTO().getAnnotationIds().size();
    if (thisAnnotationCount != otherAnnotationCount) {
      return false;
    }

    String thisAnnotationString = annotationsString();
    String otherAnnotationString = other.annotationsString();
    return thisAnnotationString.equals(otherAnnotationString);
  }

  @Override
  public String toString() {
    return markupDTO.toString();
  }

  @Override
  public int hashCode() {
    return markupDTO.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof TAGMarkup //
        && markupDTO.equals(((TAGMarkup) other).getDTO());
  }

  private void update() {
    store.persist(markupDTO);
  }

  private void setDominatingMarkup(TAGMarkup dominatingMarkup) {
    markupDTO.setDominatingMarkupId(dominatingMarkup.getDbId());
    if (!dominatingMarkup.getDTO().getDominatedMarkupId().isPresent()) {
      dominatingMarkup.setDominatedMarkup(this);
    }
    update();
  }

  private String annotationsString() {
    StringBuilder annotationsString = new StringBuilder();
    getAnnotationStream().forEach(a ->
        annotationsString.append(" ").append(a.toString())
    );
    return annotationsString.toString();
  }

  private String layerSuffix() {
    String layerSuffix = getLayers().stream()
        .filter(l -> !l.isEmpty())
        .collect(joining(","));
    return layerSuffix.isEmpty() ? "" : TAGML.DIVIDER + layerSuffix;
  }

  // TODO: refactor links to these dummy methods
  @Deprecated
  public int getTextNodeCount() {
    return 0;
  }

  @Deprecated
  public TAGMarkup setOnlyTextNode(final TAGTextNode t1) {
    return this;
  }

  @Deprecated
  public TAGMarkup setFirstAndLastTextNode(final TAGTextNode tn00, final TAGTextNode tn10) {
    return this;
  }
}
