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

package org.iti.sqlSchemaComparison;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iti.sqlSchemaComparison.edge.IForeignKeyRelationEdge;
import org.iti.sqlSchemaComparison.vertex.ISqlElement;

public class SqlSchemaComparisonResult implements Serializable {

	private static final long serialVersionUID = 4042979283395718780L;

	private Map<ISqlElement, SchemaModification> modifications = new HashMap<>();

	public Map<ISqlElement, SchemaModification> getModifications() {
		return modifications;
	}

	public void addModification(ISqlElement modifiedElement, SchemaModification schemaModification) {
		modifications.put(modifiedElement, schemaModification);
	}

	public void removeModification(ISqlElement modifiedElement) {
		modifications.remove(modifiedElement);
	}

	private List<IForeignKeyRelationEdge> addedForeignKeyRelations = new ArrayList<>();

	public List<IForeignKeyRelationEdge> getAddedForeignKeyRelations() {
		return addedForeignKeyRelations;
	}

	public void setAddedForeignKeyRelations(
			List<IForeignKeyRelationEdge> addedForeignKeyRelations) {
		this.addedForeignKeyRelations = addedForeignKeyRelations;
	}

	private List<IForeignKeyRelationEdge> removedForeignKeyRelations = new ArrayList<>();

	public List<IForeignKeyRelationEdge> getRemovedForeignKeyRelations() {
		return removedForeignKeyRelations;
	}

	public void setRemovedForeignKeyRelations(
			List<IForeignKeyRelationEdge> removedForeignKeyRelations) {
		this.removedForeignKeyRelations = removedForeignKeyRelations;
	}

	@Override
	public String toString() {
		String output = "";
		String result = "";

		output += "Schema Comparison Result\n";
		output += "------------------------\n";

		for (Entry<ISqlElement, SchemaModification> entry : modifications.entrySet()) {
			switch (entry.getValue()) {
				case CREATE_TABLE:
				case DELETE_TABLE:
				case RENAME_TABLE:
				case DELETE_AFTER_RENAME_TABLE:
					result += "\n";
					result += "----------------------\n";
					result += "| TABLE MODIFICATION |\n";
					result += "----------------------";
					break;

				case CREATE_COLUMN:
				case DELETE_COLUMN:
				case RENAME_COLUMN:
				case MOVE_COLUMN:
					result += "\n";
					result += "------------------------\n";
					result += "| COLUMN MODIFICATIONS |\n";
					result += "------------------------";
					break;

				default:
					break;
			}

			result += String.format("\n%s | %s", entry.getValue().toString(), entry.getKey().getName());
		}

		result += toResultString("CREATED FOREIGN REFERENCES", addedForeignKeyRelations);
		result += toResultString("REMOVED FOREIGN REFERENCES", removedForeignKeyRelations);

		if (result.length() == 0)
			output += "Schemas are isomorphic!";
		else
			output += result;

		return output;
	}

	private String toResultString(String title, List<IForeignKeyRelationEdge> elements) {
		String output = "";
		String result = "";

		if (!elements.isEmpty()) {
			for (IForeignKeyRelationEdge r : elements)
				result += String.format("\n%s -> %s", r.getReferencingColumn(), r.getForeignKeyColumn());
		}

		if (result.length() > 0) {
			output += "\n";
			output += "-----------------------------\n";
			output += "| " + title + " |\n";
			output += "-----------------------------";
			output += result;
		}

		return output;
	}

}
