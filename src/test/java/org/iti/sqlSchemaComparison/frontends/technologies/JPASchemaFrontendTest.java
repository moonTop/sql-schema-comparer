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

package org.iti.sqlSchemaComparison.frontends.technologies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.iti.sqlSchemaComparison.edge.ForeignKeyRelationEdge;
import org.iti.sqlSchemaComparison.frontends.ISqlSchemaFrontend;
import org.iti.sqlSchemaComparison.vertex.ISqlElement;
import org.iti.sqlSchemaComparison.vertex.SqlColumnVertex;
import org.iti.sqlSchemaComparison.vertex.SqlElementFactory;
import org.iti.sqlSchemaComparison.vertex.SqlTableVertex;
import org.iti.structureGraph.nodes.IStructureElement;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JPASchemaFrontendTest {

	private static final String JPA_FILE_PATH = "jpa//Department.java";
	private static final String JPA_FOLDER = "jpa";

	@Before
	public void setUp() { }

	@Test
	public void databaseConnectionEstablishedCorrectly() {
		ISqlSchemaFrontend frontend = new JPASchemaFrontend(JPA_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema = frontend.createSqlSchema();

		assertNotNull(schema);
		ISqlElement[] tables = SqlElementFactory.getSqlElementsOfType(SqlTableVertex.class, schema.vertexSet()).toArray(new ISqlElement[] {});

		assertEquals(1, tables.length);
		assertEquals("departments", tables[0].getName());
		assertEquals(2, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema.vertexSet()).size());
	}

	@Test
	public void directoryProcessing() {
		ISqlSchemaFrontend frontend = new JPASchemaFrontend(JPA_FOLDER);
		DirectedGraph<IStructureElement, DefaultEdge> schema = frontend.createSqlSchema();

		assertNotNull(schema);
		assertEquals(3, SqlElementFactory.getSqlElementsOfType(SqlTableVertex.class, schema.vertexSet()).size());
		assertEquals(10, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema.vertexSet()).size());
		assertEquals(2, getForeignKeyCount(schema));
	}

	private int getForeignKeyCount(DirectedGraph<IStructureElement, DefaultEdge> schema) {
		int foreignKeyEdges = 0;

		for (DefaultEdge edge : schema.edgeSet())
			if (edge instanceof ForeignKeyRelationEdge)
				foreignKeyEdges++;

		return foreignKeyEdges;
	}

	@After
	public void tearDown() { }

}
