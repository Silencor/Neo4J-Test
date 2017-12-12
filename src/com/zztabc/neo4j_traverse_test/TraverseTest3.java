package com.zztabc.neo4j_traverse_test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;

import com.zztabc.neo4j_create_test.CreateDB;
import com.zztabc.neo4j_create_test.MyRelationshipTypes;

//自定义Expander
public class TraverseTest3 {
	public static void main(String[] args) {
		GraphDatabaseService graphDB = CreateDB.createDataBase("E://Neo4JDB//traverse_test");

		try (Transaction tx = graphDB.beginTx()) {
			// 配置深度、关系映射
			Map<Integer, List<RelationshipType>> mappings = new HashMap<>();
			mappings.put(0, Arrays
					.asList(new RelationshipType[] { MyRelationshipTypes.IS_FRIEND_OF, MyRelationships2.WORK_WITH }));
			mappings.put(1, Arrays.asList(new RelationshipType[] { MyRelationships2.LIKES }));

			// 要遍历的节点
			Node john = graphDB.findNode(MyLabels2.PERSON, "name", "John");

			TraversalDescription traversalDescription = graphDB.traversalDescription()
					.expand(new DepthAwareExpander(mappings)).evaluator(Evaluators.atDepth(2));
			
			// 从节点john开始遍历
			Iterable<Node> nodes = traversalDescription.traverse(john).nodes();
			for (Node n : nodes) {
				System.out.print(n.getProperty("title") + " -> ");
			}
			
			tx.success();
		}
	}
}
