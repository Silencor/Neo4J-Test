package com.zztabc.neo4j_create_test;

import org.neo4j.graphdb.RelationshipType;

public enum MyRelationshipTypes implements RelationshipType {
	IS_FRIEND_OF, HAS_SEEN,
}
