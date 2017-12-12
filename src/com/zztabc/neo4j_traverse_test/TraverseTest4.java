package com.zztabc.neo4j_traverse_test;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Paths;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;

import com.zztabc.neo4j_create_test.CreateDB;
import com.zztabc.neo4j_create_test.PathPrinter;

//Uniqueness NODE_GLOBAL, NODE_PATH
public class TraverseTest4 {
	public static void main(String[] args) {
		GraphDatabaseService graphDB = CreateDB.createDataBase("E://Neo4JDB//traverse_test");

		try (Transaction tx = graphDB.beginTx()) {
			Node jane = graphDB.findNode(MyLabels2.HUMAN, "name", "1.Jane");
			Node leeo = graphDB.findNode(MyLabels2.HUMAN, "name", "5.Leeo");

			// NODE_GLOBAL唯一性，每个节点只能访问一次
			System.out.println("NODE_GLOBAL唯一性，每个节点只能访问一次");
			TraversalDescription traversal = graphDB.traversalDescription().relationships(MyRelationships2.KNOWS)
					.evaluator(path -> {
						Node currentNode = path.endNode();

						// 当遍历到Leeo时停止
						if (currentNode.getId() == leeo.getId()) {
							return Evaluation.EXCLUDE_AND_PRUNE;
						}

						// 起点与终点的最短path
						Path singlePath = GraphAlgoFactory
								.shortestPath(PathExpanders.forType(MyRelationships2.KNOWS), 1)
								.findSinglePath(currentNode, leeo);

						if (singlePath != null) {
							// 有路径，即可达，则将该节点包含在path中并继续
							return Evaluation.INCLUDE_AND_CONTINUE;
						} else {
							// 没有路径，丢弃当前节点
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
					}).uniqueness(Uniqueness.NODE_GLOBAL);

			// 从jane节点开始
			Iterable<Node> nodes = traversal.traverse(jane).nodes();
			for (Node n : nodes) {
				System.out.println(n.getProperty("name"));
			}

			tx.success();
		}

		// NODE_PATH唯一性，每个路径只能访问一次
		System.out.println("NODE_PATH唯一性，每个路径只能访问一次，节点可多次，只要不属于同一路径");
		try (Transaction tx = graphDB.beginTx()) {
			Node jane = graphDB.findNode(MyLabels2.HUMAN, "name", "1.Jane");
			Node leeo = graphDB.findNode(MyLabels2.HUMAN, "name", "5.Leeo");

			TraversalDescription traversal = graphDB.traversalDescription().relationships(MyRelationships2.KNOWS)
					.evaluator(path -> {
						Node currentNode = path.endNode();

						if (currentNode.getId() == leeo.getId()) {
							return Evaluation.EXCLUDE_AND_PRUNE;
						}

						Path singlePath = GraphAlgoFactory
								.shortestPath(PathExpanders.forType(MyRelationships2.KNOWS), 1)
								.findSinglePath(currentNode, leeo);

						if (singlePath != null) {
							return Evaluation.INCLUDE_AND_CONTINUE;
						} else {
							return Evaluation.EXCLUDE_AND_CONTINUE;
						}
					}).uniqueness(Uniqueness.NODE_PATH);

			// 打印节点
			Iterable<Node> nodes = traversal.traverse(jane).nodes();
			for (Node node : nodes) {
				System.out.println(node.getProperty("name"));
			}

			// 打印路径
			PathPrinter pathPrinter = new PathPrinter("name");
			String output = "";

			for (Path path : traversal.traverse(jane)) {
				output += Paths.pathToString(path, pathPrinter);
				output += "\n";
			}

			System.out.println(output);

			tx.success();
		}
	}
}
