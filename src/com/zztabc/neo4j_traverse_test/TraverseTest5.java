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

//˫�����
public class TraverseTest5 {
	public static void main(String[] args) {
		GraphDatabaseService graphDB = CreateDB.createDataBase("E://Neo4JDB//traverse_test");

		try (Transaction tx = graphDB.beginTx()) {
			Node jane = graphDB.findNode(MyLabels2.HUMAN, "name", "1.Jane");
			Node leeo = graphDB.findNode(MyLabels2.HUMAN, "name", "5.Leeo");

			// ˫���������
			// ��ȡ˫���������
			BidirectionalTraversalDescription bidirectionalTraversal = graphDB.bidirectionalTraversalDescription()
					// ���ñ�����������ʼ�� �ڵ������
					.startSide(graphDB.traversalDescription().relationships(MyRelationships2.KNOWS)
							.uniqueness(Uniqueness.NODE_PATH))
					// ���ñ��������Ľ����� �ڵ������
					.endSide(graphDB.traversalDescription().relationships(MyRelationships2.KNOWS)
							.uniqueness(Uniqueness.NODE_PATH))
					// ������ײ��������������������ײ��
					.collisionEvaluator(path -> Evaluation.INCLUDE_AND_CONTINUE)
					// ���ò�ѡ����Ϊ����������������任
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
