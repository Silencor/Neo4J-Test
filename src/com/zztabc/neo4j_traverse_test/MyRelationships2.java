package com.zztabc.neo4j_traverse_test;

import org.neo4j.graphdb.RelationshipType;

public enum MyRelationships2 implements RelationshipType {
	LIKES, WORK_WITH, IS_FRIEND_OF, KNOWS
}
