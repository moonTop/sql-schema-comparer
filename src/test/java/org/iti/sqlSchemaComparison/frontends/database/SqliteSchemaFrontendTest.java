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

package org.iti.sqlSchemaComparison.frontends.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import org.iti.sqlSchemaComparison.SchemaModification;
import org.iti.sqlSchemaComparison.SqlSchemaComparer;
import org.iti.sqlSchemaComparison.SqlSchemaComparisonResult;
import org.iti.sqlSchemaComparison.TestHelper;
import org.iti.sqlSchemaComparison.edge.ForeignKeyRelationEdge;
import org.iti.sqlSchemaComparison.edge.TableHasColumnEdge;
import org.iti.sqlSchemaComparison.frontends.ISqlSchemaFrontend;
import org.iti.sqlSchemaComparison.vertex.ISqlElement;
import org.iti.sqlSchemaComparison.vertex.SqlColumnVertex;
import org.iti.sqlSchemaComparison.vertex.SqlElementFactory;
import org.iti.sqlSchemaComparison.vertex.SqlTableVertex;
import org.iti.sqlSchemaComparison.vertex.sqlColumn.ColumnTypeVertex;
import org.iti.sqlSchemaComparison.vertex.sqlColumn.IColumnConstraint;
import org.iti.structureGraph.comparison.StructureGraphComparisonException;
import org.iti.structureGraph.nodes.IStructureElement;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SqliteSchemaFrontendTest {

	public static final String DATABASE_FILE_PATH = "src//test//java//databases//hrm.sqlite";

	public static final String DROPPED_COLUMN_DATABASE_FILE_PATH = "src//test//java//databases//refactored//hrm_DropColumn.sqlite";
	public static final String DROPPED_TABLE_DATABASE_FILE_PATH = "src//test//java//databases//refactored//hrm_DropTable.sqlite";
	public static final String MOVE_COLUMN_DATABASE_FILE_PATH = "src//test//java//databases//refactored//hrm_MoveColumn.sqlite";
	public static final String RENAME_COLUMN_DATABASE_FILE_PATH = "src//test//java//databases//refactored//hrm_RenameColumn.sqlite";
	public static final String RENAME_TABLE_DATABASE_FILE_PATH = "src//test//java//databases//refactored//hrm_RenameTable.sqlite";
	public static final String REPLACE_COLUMN_DATABASE_FILE_PATH = "src//test//java//databases//refactored//hrm_ReplaceColumn.sqlite";
	public static final String REPLACE_LOB_WITH_TABLE_DATABASE_FILE_PATH = "src//test//java//databases//refactored//hrm_ReplaceLobWithTable.sqlite";

	private static final String DROPPED_COLUMN_NAME = "boss";
	private static final String DROPPED_TABLE_NAME = "external_staff";
	private static final String MOVE_COLUMN_NAME = "account";
	private static final String RENAME_COLUMN_NAME = "phone";
	private static final String RENAME_TABLE_NAME = "external_employees";
	private static final String REPLACE_COLUMN_NAME = "company";
	private static final String REPLACE_COLUMN_TYPE = "TEXT";
	private static final String REPLACE_LOB_WITH_TABLE = "customer_address";
	private static final String REPLACE_LOB_WITH_COLUMN = "address";

	@Before
	public void setUp() { }

	@Test
	public void databaseConnectionEstablishedCorrectly() {
		ISqlSchemaFrontend frontend = new SqliteSchemaFrontend(DATABASE_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema = frontend.createSqlSchema();
		Set<ISqlElement> tables = SqlElementFactory.getSqlElementsOfType(SqlTableVertex.class, schema.vertexSet());
		Set<ISqlElement> columns = SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema.vertexSet());
		ArrayList<ISqlElement> mandatoryColumns = new ArrayList<>();
		int tableCount = tables.size();
		int columnCount = columns.size();

		for (ISqlElement column : columns) {
			if (column.isMandatory()) {
				mandatoryColumns.add(column);
			}
		}

		assertNotNull(schema);
		assertEquals(8, tableCount);
		assertEquals(31, columnCount);
		assertEquals(7, TestHelper.getColumnWithConstraint(schema, IColumnConstraint.ConstraintType.PRIMARY_KEY).size());
		assertEquals(1, mandatoryColumns.size());
		assertEquals("[Column] departments.name", mandatoryColumns.get(0).toString());

	}

	@Test
	public void foreignKeysEstablishedCorrectly() {
		ISqlSchemaFrontend frontend = new SqliteSchemaFrontend(DATABASE_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema = frontend.createSqlSchema();
		int foreignKeyEdges = 0;

		for (DefaultEdge edge : schema.edgeSet())
			if (edge instanceof ForeignKeyRelationEdge)
				foreignKeyEdges++;


		assertEquals(7, foreignKeyEdges);

	}

	@Test
	public void tableHasColumnRelationsEstablishedCorrectly() {
		ISqlSchemaFrontend frontend = new SqliteSchemaFrontend(DATABASE_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema = frontend.createSqlSchema();
		int tableHasColumnEdges = 0;

		for (DefaultEdge edge : schema.edgeSet())
			if (edge instanceof TableHasColumnEdge)
				tableHasColumnEdges++;


		assertEquals(31, tableHasColumnEdges);

	}

	@Test
	public void droppedColumnDetectedCorrectly() throws StructureGraphComparisonException {
		ISqlSchemaFrontend frontend1 = new SqliteSchemaFrontend(DATABASE_FILE_PATH);
		ISqlSchemaFrontend frontend2 = new SqliteSchemaFrontend(DROPPED_COLUMN_DATABASE_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema1 = frontend1.createSqlSchema();
		DirectedGraph<IStructureElement, DefaultEdge> schema2 = frontend2.createSqlSchema();
		SqlSchemaComparer comparer = new SqlSchemaComparer(schema1, schema2);
		SqlSchemaComparisonResult result = comparer.comparisonResult;

		assertEquals(31, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema1.vertexSet()).size());
		assertEquals(30, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema2.vertexSet()).size());

		Entry<ISqlElement, SchemaModification> entry = TestHelper.getModificationOfType(result, SchemaModification.DELETE_COLUMN);

		assertNotNull(entry);
		assertEquals(DROPPED_COLUMN_NAME, entry.getKey().getName());
	}

	@Test
	public void droppedTableDetectedCorrectly() throws StructureGraphComparisonException {
		ISqlSchemaFrontend frontend1 = new SqliteSchemaFrontend(DATABASE_FILE_PATH);
		ISqlSchemaFrontend frontend2 = new SqliteSchemaFrontend(DROPPED_TABLE_DATABASE_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema1 = frontend1.createSqlSchema();
		DirectedGraph<IStructureElement, DefaultEdge> schema2 = frontend2.createSqlSchema();
		SqlSchemaComparer comparer = new SqlSchemaComparer(schema1, schema2);
		SqlSchemaComparisonResult result = comparer.comparisonResult;

		assertEquals(8, SqlElementFactory.getSqlElementsOfType(SqlTableVertex.class, schema1.vertexSet()).size());
		assertEquals(7, SqlElementFactory.getSqlElementsOfType(SqlTableVertex.class, schema2.vertexSet()).size());

		for (Entry<ISqlElement, SchemaModification> entry : result.getModifications().entrySet()) {
			if (entry.getValue() == SchemaModification.DELETE_TABLE) {
				assertEquals(SchemaModification.DELETE_TABLE, entry.getValue());
				assertEquals(DROPPED_TABLE_NAME, entry.getKey().getName());
			}
		}
	}

	@Test
	public void moveColumnDetectedCorrectly() throws StructureGraphComparisonException {
		ISqlSchemaFrontend frontend1 = new SqliteSchemaFrontend(DATABASE_FILE_PATH);
		ISqlSchemaFrontend frontend2 = new SqliteSchemaFrontend(MOVE_COLUMN_DATABASE_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema1 = frontend1.createSqlSchema();
		DirectedGraph<IStructureElement, DefaultEdge> schema2 = frontend2.createSqlSchema();
		SqlSchemaComparer comparer = new SqlSchemaComparer(schema1, schema2);
		SqlSchemaComparisonResult result = comparer.comparisonResult;

		assertEquals(31, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema1.vertexSet()).size());
		assertEquals(31, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema2.vertexSet()).size());

		Entry<ISqlElement, SchemaModification> entry = TestHelper.getModificationOfType(result, SchemaModification.MOVE_COLUMN);

		assertNotNull(entry);
		assertEquals(SchemaModification.MOVE_COLUMN, entry.getValue());
		assertEquals(MOVE_COLUMN_NAME, entry.getKey().getName());
	}

	@Test
	public void renameColumnDetectedCorrectly() throws StructureGraphComparisonException {
		ISqlSchemaFrontend frontend1 = new SqliteSchemaFrontend(DATABASE_FILE_PATH);
		ISqlSchemaFrontend frontend2 = new SqliteSchemaFrontend(RENAME_COLUMN_DATABASE_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema1 = frontend1.createSqlSchema();
		DirectedGraph<IStructureElement, DefaultEdge> schema2 = frontend2.createSqlSchema();
		SqlSchemaComparer comparer = new SqlSchemaComparer(schema1, schema2);
		SqlSchemaComparisonResult result = comparer.comparisonResult;

		assertEquals(31, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema1.vertexSet()).size());
		assertEquals(31, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema2.vertexSet()).size());

		Entry<ISqlElement, SchemaModification> entry = TestHelper.getModificationOfType(result, SchemaModification.RENAME_COLUMN);

		assertNotNull(entry);
		assertEquals(SchemaModification.RENAME_COLUMN, entry.getValue());
		assertEquals(RENAME_COLUMN_NAME, entry.getKey().getName());

		entry = TestHelper.getModificationOfType(result, SchemaModification.CHANGE_COLUMN_TYPE);

		assertNull(entry);
	}

	@Test
	public void renameTableDetectedCorrectly() throws StructureGraphComparisonException {
		ISqlSchemaFrontend frontend1 = new SqliteSchemaFrontend(DATABASE_FILE_PATH);
		ISqlSchemaFrontend frontend2 = new SqliteSchemaFrontend(RENAME_TABLE_DATABASE_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema1 = frontend1.createSqlSchema();
		DirectedGraph<IStructureElement, DefaultEdge> schema2 = frontend2.createSqlSchema();
		SqlSchemaComparer comparer = new SqlSchemaComparer(schema1, schema2);
		SqlSchemaComparisonResult result = comparer.comparisonResult;

		assertEquals(31, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema1.vertexSet()).size());
		assertEquals(31, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema2.vertexSet()).size());

		for (Entry<ISqlElement, SchemaModification> entry : result.getModifications().entrySet()) {
			if (entry.getValue() == SchemaModification.RENAME_TABLE) {
				assertEquals(RENAME_TABLE_NAME, entry.getKey().getName());
			}
		}
	}

	@Test
	public void replaceColumnDetectedCorrectly() throws StructureGraphComparisonException {
		ISqlSchemaFrontend frontend1 = new SqliteSchemaFrontend(DATABASE_FILE_PATH);
		ISqlSchemaFrontend frontend2 = new SqliteSchemaFrontend(REPLACE_COLUMN_DATABASE_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema1 = frontend1.createSqlSchema();
		DirectedGraph<IStructureElement, DefaultEdge> schema2 = frontend2.createSqlSchema();
		SqlSchemaComparer comparer = new SqlSchemaComparer(schema1, schema2);
		SqlSchemaComparisonResult result = comparer.comparisonResult;

		assertEquals(31, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema1.vertexSet()).size());
		assertEquals(31, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema2.vertexSet()).size());

		Entry<ISqlElement, SchemaModification> renameColumnEntry = TestHelper.getModificationOfType(result, SchemaModification.RENAME_COLUMN);
		Entry<ISqlElement, SchemaModification> replaceColumnTypeEntry = TestHelper.getModificationOfType(result, SchemaModification.CHANGE_COLUMN_TYPE);

		assertNotNull(renameColumnEntry);
		assertEquals(REPLACE_COLUMN_NAME, renameColumnEntry.getKey().getName());
		assertNotNull(replaceColumnTypeEntry);
		assertEquals(REPLACE_COLUMN_TYPE, ((ColumnTypeVertex) replaceColumnTypeEntry.getKey()).getColumnType());
	}

	@Test
	public void replaceLobWithTable() throws StructureGraphComparisonException {
		ISqlSchemaFrontend frontend1 = new SqliteSchemaFrontend(DATABASE_FILE_PATH);
		ISqlSchemaFrontend frontend2 = new SqliteSchemaFrontend(REPLACE_LOB_WITH_TABLE_DATABASE_FILE_PATH);
		DirectedGraph<IStructureElement, DefaultEdge> schema1 = frontend1.createSqlSchema();
		DirectedGraph<IStructureElement, DefaultEdge> schema2 = frontend2.createSqlSchema();
		SqlSchemaComparer comparer = new SqlSchemaComparer(schema1, schema2);
		SqlSchemaComparisonResult result = comparer.comparisonResult;

		assertEquals(31, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema1.vertexSet()).size());
		assertEquals(34, SqlElementFactory.getSqlElementsOfType(SqlColumnVertex.class, schema2.vertexSet()).size());

		for (Entry<ISqlElement, SchemaModification> entry : result.getModifications().entrySet()) {
			if (entry.getValue() == SchemaModification.CREATE_TABLE) {
				assertEquals(REPLACE_LOB_WITH_TABLE, entry.getKey().getName());
			}

			if (entry.getValue() == SchemaModification.DELETE_COLUMN) {
				assertEquals(REPLACE_LOB_WITH_COLUMN, entry.getKey().getName());
			}
		}
	}

	@Test(expected=InvalidPathException.class)
	public void throwsInvalidFilePathExceptionForEmptyString() {
		ISqlSchemaFrontend frontend = new SqliteSchemaFrontend("");

		frontend.createSqlSchema();
	}

	@Test(expected=InvalidPathException.class)
	public void throwsInvalidFilePathExceptionForNull() {
		ISqlSchemaFrontend frontend = new SqliteSchemaFrontend(null);

		frontend.createSqlSchema();
	}

	@Test(expected=IllegalArgumentException.class)
	public void throwsInvalidArgumentExceptionOnInvalidFilePath() {
		ISqlSchemaFrontend frontend = new SqliteSchemaFrontend("dadidadam");

		frontend.createSqlSchema();
	}

	@After
	public void tearDown() { }
}
