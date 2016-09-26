/*
 * Copyright (C) 2016 The Guava Authors
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

package com.google.common.graph;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.graph.GraphConstants.GRAPH_STRING_FORMAT;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * This class provides a skeletal implementation of {@link ValueGraph}. It is recommended to extend
 * this class rather than implement {@link ValueGraph} directly.
 *
 * @author James Sexton
 * @param <N> Node parameter type
 * @param <V> Value parameter type
 * @since 20.0
 */
@Beta
public abstract class AbstractValueGraph<N, V> extends AbstractGraph<N>
    implements ValueGraph<N, V> {

  @Override
  public V edgeValueOrDefault(Object nodeU, Object nodeV, @Nullable V defaultValue) {
    checkNotNull(nodeU);
    checkNotNull(nodeV);
    boolean connected = nodes().contains(nodeU) && successors(nodeU).contains(nodeV);
    return connected ? edgeValue(nodeU, nodeV) : defaultValue;
  }

  /** Returns a string representation of this graph. */
  @Override
  public String toString() {
    String propertiesString =
        String.format("isDirected: %s, allowsSelfLoops: %s", isDirected(), allowsSelfLoops());
    return String.format(GRAPH_STRING_FORMAT, propertiesString, nodes(), edgeValueMap());
  }

  private Map<EndpointPair<N>, V> edgeValueMap() {
    Function<EndpointPair<N>, V> edgeToValueFn =
        new Function<EndpointPair<N>, V>() {
          @Override
          public V apply(EndpointPair<N> edge) {
            return edgeValue(edge.nodeU(), edge.nodeV());
          }
        };
    return Maps.asMap(edges(), edgeToValueFn);
  }
}
