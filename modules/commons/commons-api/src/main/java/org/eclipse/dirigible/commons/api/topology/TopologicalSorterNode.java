/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.commons.api.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TopologicalSorterNode {
	
	private ITopologicallySortable data;
	
	private Map<String, TopologicalSorterNode> nodes;
	
    private boolean visited;
    
    private List<TopologicalSorterNode> dependencies;

    public TopologicalSorterNode(ITopologicallySortable data, Map<String, TopologicalSorterNode> nodes) {
        this.data = data;
        this.nodes = nodes;
    }
    
    public ITopologicallySortable getData() {
		return data;
	}
    
    public boolean isVisited() {
		return visited;
	}
    
    public void setVisited(boolean visited) {
		this.visited = visited;
	}
    
    public List<TopologicalSorterNode> getDependencies() {
    	if (this.dependencies == null) {
    		this.dependencies = new ArrayList<>();
    		for (ITopologicallySortable sortable : this.data.getDependencies()) {
    			this.dependencies.add(nodes.get(sortable.getId()));
    		}
    	}
        return this.dependencies;
    }
    
    public void setDependencies(List<TopologicalSorterNode> dependencies) {
        this.dependencies = dependencies;
    }
    
    public String toString() {
        return data.getId();
    }

}
