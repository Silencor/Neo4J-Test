package com.zztabc.neo4j_traverse_test;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.BidirectionalTraversalDescription;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Paths;
import org.neo4j.graphdb.traversal.SideSelectorPolicies;
import org.neo4j.graphdb.traversal.Uniqueness;

import com.zztabc.neo4j_create_test.CreateDB;
import com.zztabc.neo4j_create_test.PathPrinter;

//双向遍历
public class TraverseTest5 {
	public static void main(String[] args) {
		GraphDatabaseService graphDB = CreateDB.createDataBase("E://Neo4JDB//traverse_test");

		try (Transaction tx = graphDB.beginTx()) {
			Node jane = graphDB.findNode(MyLabels2.HUMAN, "name", "1.Jane");
			Node leeo = graphDB.findNode(MyLabels2.HUMAN, "name", "5.Leeo");

			// 双向遍历描述
			// 获取双向遍历描述
			BidirectionalTraversalDescription bidirectionalTraversal = graphDB.bidirectionalTraversalDescription()
					// 设置遍历描述的起始侧 节点遍历出
					.startSide(graphDB.traversalDescription().relationships(MyRelationships2.KNOWS)
							.uniqueness(Uniqueness.NODE_PATH))
					// 设置遍历描述的结束侧 节点遍历进
					.endSide(graphDB.traversalDescription().relationships(MyRelationships2.KNOWS)
							.uniqueness(Uniqueness.NODE_PATH))
					// 设置碰撞评估函数，包含所有碰撞点
					.collisionEvaluator(path -> Evaluation.INCLUDE_AND_CONTINUE)
					// 设置侧选择器为在两个遍历方向交替变换
					.sideSelector(SideSelectorPolicies.ALTERNATING, 100);

			PathPrinter pathPrinter = new PathPrinter("name");
			String output = "";

			for (Path path : bidirectionalTraversal.traverse(jane, leeo)) {
				output += Paths.pathToString(path, pathPrinter);
				output += "\n";
			}

			System.out.println(output);

			tx.success();
		}
		graphDB.shutdown();
	}
}
