/*
 *  Copyright 1999 Hagen Schink <hagen.schink@gmail.com>
 *
 *  This file is part of sql-schema-comparer.
 *
 *  sql-schema-comparer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  sql-schema-comparer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with sql-schema-comparer.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.iti.sqlSchemaComparison.vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iti.sqlSchemaComparison.edge.IColumnHasConstraint;
import org.iti.sqlSchemaComparison.vertex.sqlColumn.IColumnConstraint;
import org.iti.sqlSchemaComparison.vertex.sqlColumn.IColumnConstraint.ConstraintType;
import org.iti.structureGraph.nodes.IStructureElement;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public abstract class SqlElementFactory {

	public static ISqlElement createSqlElement(SqlElementType type, String id) {
		switch (type) {
			case Column:
				return null;

			default:
				return new SqlTableVertex(id);
		}
	}

	public static boolean equals(ISqlElement t, Object o) {
		if (o instanceof ISqlElement) {
			ISqlElement e = (ISqlElement)o;

			return equals(t.getName(), e.getName(), t.getClass(), e.getClass());
		}

		return false;
	}

	public static boolean equals(SqlColumnVertex t, Object o) {
		if (o instanceof SqlColumnVertex) {
			SqlColumnVertex e = (SqlColumnVertex)o;

			return equals(t.getName(), e.getName(), t.getClass(), e.getClass())
					&& t.getTable().equals(e.getTable());
		}

		return false;
	}

	private static boolean equals(String id, String otherId, Class<? extends ISqlElement> class1, Class<? extends ISqlElement> class2) {
		return id.equals(otherId) && class1.equals(class2);
	}

	public static int hashCode(ISqlElement t) {
		return t.getName().hashCode() + t.getClass().hashCode();
	}

	public static Set<ISqlElement> getSqlElementsOfType(Class<? extends ISqlElement> type, Collection<IStructureElement> vertices) {
		Set<ISqlElement> verticesOfType = new HashSet<>();

		for (IStructureElement element : vertices) {
			ISqlElement t = (ISqlElement)element;

			if (t.getClass().equals(type))
				verticesOfType.add(t);
		}

		return verticesOfType;
	}

	public static ISqlElement getMatchingSqlElement(ISqlElement vertex, Set<IStructureElement> vertices) {

		for (IStructureElement element : vertices) {
			ISqlElement v = (ISqlElement)element;

			if (v.equals(vertex))
				return v;
		}

		return null;
	}

	public static ISqlElement getMatchingSqlElement(Class<? extends ISqlElement> type, String id, Collection<IStructureElement> vertices) {

		for (IStructureElement element : vertices) {
			ISqlElement v = (ISqlElement)element;

			if (v.getClass().equals(type)) {
				if (type.equals(SqlColumnVertex.class)) {
					String otherTable = ((SqlColumnVertex) v).getTable();
					String otherId = otherTable + "." + v.getName();

					if (otherId.equals(id))
						return v;

					continue;
				} else if (v.getName().equals(id))
					return v;
			}
		}

		return null;
	}

	public static List<ISqlElement> getMatchingSqlColumns(String id, Collection<IStructureElement> vertices, boolean matchColumnTable) {
		Set<ISqlElement> columns = SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, vertices);
		List<ISqlElement> matchingColumns = new ArrayList<>();

		for (ISqlElement column : columns) {
			String otherTable =  (matchColumnTable) ? ((SqlColumnVertex) column).getTable() + "." : "";
			String otherId = otherTable + column.getName();

			if (otherId.equals(id))
				matchingColumns.add(column);
		}

		return matchingColumns;
	}

	public static ISqlElement getPrimaryKey(ISqlElement table, DirectedGraph<IStructureElement, DefaultEdge> schema) {
		Set<ISqlElement> columns = ((SqlTableVertex) table).getColumns(schema);

		for (ISqlElement e : columns) {
			if (e instanceof SqlColumnVertex)
				for (IColumnConstraint c : getColumnConstraints(e, schema))
					if (c.getConstraintType().equals(ConstraintType.PRIMARY_KEY))
						return e;
		}

		return null;
	}

	private static List<IColumnConstraint> getColumnConstraints(ISqlElement column, DirectedGraph<IStructureElement, DefaultEdge> schema) {
		List<IColumnConstraint> columnConstraints = new ArrayList<>();
		Set<DefaultEdge> outgoingEdges = schema.outgoingEdgesOf(column);

		for (DefaultEdge outgoingEdge : outgoingEdges) {
			if (outgoingEdge instanceof IColumnHasConstraint) {
				columnConstraints.add((IColumnConstraint) schema.getEdgeTarget(outgoingEdge));
			}
		}

		return columnConstraints;
	}

}
