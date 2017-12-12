package com.zztabc.neo4j_traverse_test;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.impl.StandardExpander;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;

import com.zztabc.neo4j_create_test.CreateDB;

//StandardExpander
public class TraverseTest {
	public static void main(String[] args) {
		GraphDatabaseService graphDB = CreateDB.createDataBase("E://Neo4JDB//traverse_test");

		try (Transaction tx = graphDB.beginTx()) {
			Node john = graphDB.findNode(MyLabels2.PERSON, "name", "John");

			// 构造StandardExpander，添加要扩展的关系类型
			PathExpander standardExpander = StandardExpander.DEFAULT.add(MyRelationships2.WORK_WITH)
					.add(MyRelationships2.IS_FRIEND_OF).add(MyRelationships2.LIKES);

			// 在最终结果中只考虑深度2上的节点，并且只保留电影节点
			TraversalDescription traverse = graphDB.traversalDescription().expand(standardExpander)
					.evaluator(Evaluators.atDepth(2)).evaluator(path -> {
						if (path.endNode().hasProperty("title")) {
							return Evaluation.INCLUDE_AND_CONTINUE;
						}
						return Evaluation.EXCLUDE_AND_CONTINUE;
					});

			// 从节点john开始遍历
			Iterable<Node> nodes = traverse.traverse(john).nodes();
			nodes.forEach(node -> System.out.println(node.getProperties("title") + "->"));

			tx.success();
		}
	}
}
