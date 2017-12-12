package com.zztabc.neo4j_create_test;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;

public class Test {
	public static void main(String[] args) {
		GraphDatabaseService graphDB = CreateDB.createDataBase("E://Neo4JDB//create_test");

		// CreateDB.createNodeAndRelationship(graphDB);

		// ��ѯ����ΪJack Jeffries �����������е�Ӱ
		try (Transaction tx = graphDB.beginTx()) {
			Node userJack = graphDB.findNode(MyLabels.USERS, "name", "Jack Jeffries");

			// Iterable<Relationship> allRelationships = userJack.getRelationships();
			Iterable<Relationship> allMoviesRelationships = userJack
					.getRelationships(RelationshipType.withName("HAS_SEEN"));
			for (Relationship relationship : allMoviesRelationships) {
				// if(relationship.getType().name().equalsIgnoreCase("HAS_SEEN")) {
				System.out.println(relationship.getEndNode().getProperty("name"));
				// }
			}

			tx.success();
		}

		System.out.println("------------------------------");

		// ��ѯ����ΪJohn JohnSon������ϲ�����ĵ�Ӱ
		// ���ڵ����Ĳ�ѯ
		try (Transaction tx = graphDB.beginTx()) {
			Node userJohn = graphDB.findNode(MyLabels.USERS, "name", "John Johnson");

			Iterable<Relationship> allFriendsRelationships = userJohn
					.getRelationships(RelationshipType.withName("IS_FRIEND_OF"));

			for (Relationship friendsRelationship : allFriendsRelationships) {
				Node friend = friendsRelationship.getEndNode();
				Iterable<Relationship> allFriendsLikeMoviesRelationships = friend
						.getRelationships(RelationshipType.withName("HAS_SEEN"));
				for (Relationship friendsLikeMoviesRelationship : allFriendsLikeMoviesRelationships) {
					System.out.println(friendsLikeMoviesRelationship.getEndNode().getProperty("name"));
				}
			}

			tx.success();
		}

		System.out.println("------------------------------");

		// �������ñ���API�Ĳ�ѯ
		try (Transaction tx = graphDB.beginTx()) {
			Node userJohn = graphDB.findNode(MyLabels.USERS, "name", "John Johnson");

			// ʵ����һ�����������������������
			TraversalDescription traversalFriendsLikeMovies = graphDB.traversalDescription()
					.relationships(MyRelationshipTypes.IS_FRIEND_OF)
					.relationships(MyRelationshipTypes.HAS_SEEN, Direction.OUTGOING).uniqueness(Uniqueness.NODE_PATH)
					.evaluator(Evaluators.atDepth(2));

			// �������
			Traverser traverser = traversalFriendsLikeMovies.traverse(userJohn);
			Iterable<Node> friendsLikeMoviesNode = traverser.nodes();
			friendsLikeMoviesNode.forEach(node -> System.out.println(node.getProperty("name")));

		}

		System.out.println("------------------------------");

		// ʹ���Զ����������������ѯ����ΪJohn JohnSon������ϲ������Johnδ�����ĵ�Ӱ
		try (Transaction tx = graphDB.beginTx()) {
			Node userJohn = graphDB.findNode(MyLabels.USERS, "name", "John Johnson");

			// ʵ����һ�����������������������
			TraversalDescription traversalFriendsLikeMovies = graphDB.traversalDescription()
					.relationships(MyRelationshipTypes.IS_FRIEND_OF)
					.relationships(MyRelationshipTypes.HAS_SEEN, Direction.OUTGOING).uniqueness(Uniqueness.NODE_PATH)
					.evaluator(Evaluators.atDepth(2)).evaluator(new CustomNodeFilteringEvaluator(userJohn));

			// �������
			Traverser traverser = traversalFriendsLikeMovies.traverse(userJohn);
			Iterable<Node> friendsLikeMoviesNode = traverser.nodes();
			friendsLikeMoviesNode.forEach(node -> System.out.println(node.getProperty("name")));
		}
	}
}

// �Զ�����������
class CustomNodeFilteringEvaluator implements Evaluator {
	private final Node userNode;

	public CustomNodeFilteringEvaluator(Node userNode) {
		this.userNode = userNode;
	}

	@Override
	public Evaluation evaluate(Path path) {
		// ����·���е����һ���ڵ㣬��ǰ�����������е�USERS��MOVIES�ڵ�
		Node currentNode = path.endNode();
		// �ж��Ƿ���MOVIES�ڵ㣬������ǣ��������Ҽ�������
		if (!currentNode.hasLabel(MyLabels.MOVIES)) {
			return Evaluation.EXCLUDE_AND_CONTINUE;
		}
		// ����ָ��ǰ�ڵ��HAS_SEEN��ϵ
		for (Relationship r : currentNode.getRelationships(Direction.INCOMING, MyRelationshipTypes.HAS_SEEN)) {
			// ��ȡHAS_SEEN��ϵ��Դͷ����USERS�ڵ㣬����ڵ��Ǹ�����Ŀ��ڵ㣨John��������
			if (r.getStartNode().equals(userNode)) {
				return Evaluation.EXCLUDE_AND_CONTINUE;
			}
		}
		return Evaluation.INCLUDE_AND_CONTINUE;
	}
}
