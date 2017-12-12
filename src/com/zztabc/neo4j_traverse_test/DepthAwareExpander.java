package com.zztabc.neo4j_traverse_test;

import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.BranchState;

//�Զ���Expander
public class DepthAwareExpander implements PathExpander {
	// ʹ��Map�洢Ҫ���ٵı�����Ⱥ͹�ϵ����֮���ӳ��
	private final Map<Integer, List<RelationshipType>> relationshipToDepthMapping;

	public DepthAwareExpander(Map<Integer, List<RelationshipType>> relationshipToDepthMapping) {
		this.relationshipToDepthMapping = relationshipToDepthMapping;
	}

	@Override
	public Iterable<Relationship> expand(Path path, BranchState state) {
		// ���ұ����ĵ�ǰ���
		int depth = path.length();

		// �ڵ�ǰ����Ȳ���Ҫ���ٵĹ�ϵ
		List<RelationshipType> relationshipTypes = relationshipToDepthMapping.get(depth);

		// ��չ��ǰ�ڵ����ù������͵����й�ϵ
		RelationshipType[] relationshipTypeArray = new RelationshipType[0];
		RelationshipType[] relationshipTypesArray = relationshipTypes.toArray(relationshipTypeArray);
		return path.endNode().getRelationships(relationshipTypesArray);
	}

	@Override
	public PathExpander reverse() {
		// TODO Auto-generated method stub
		return null;
	}
}
