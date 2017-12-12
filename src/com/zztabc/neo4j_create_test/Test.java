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

		// 查询名字为Jack Jeffries 所看过的所有电影
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

		// 查询名字为John JohnSon的朋友喜欢看的电影
		// 基于迭代的查询
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

		// 基于内置遍历API的查询
		try (Transaction tx = graphDB.beginTx()) {
			Node userJohn = graphDB.findNode(MyLabels.USERS, "name", "John Johnson");

			// 实例化一个遍历描述，定义遍历规则
			TraversalDescription traversalFriendsLikeMovies = graphDB.traversalDescription()
					.relationships(MyRelationshipTypes.IS_FRIEND_OF)
					.relationships(MyRelationshipTypes.HAS_SEEN, Direction.OUTGOING).uniqueness(Uniqueness.NODE_PATH)
					.evaluator(Evaluators.atDepth(2));

			// 遍历起点
			Traverser traverser = traversalFriendsLikeMovies.traverse(userJohn);
			Iterable<Node> friendsLikeMoviesNode = traverser.nodes();
			friendsLikeMoviesNode.forEach(node -> System.out.println(node.getProperty("name")));

		}

		System.out.println("------------------------------");

		// 使用自定义的评估函数，查询名字为John JohnSon的朋友喜欢看且John未看过的电影
		try (Transaction tx = graphDB.beginTx()) {
			Node userJohn = graphDB.findNode(MyLabels.USERS, "name", "John Johnson");

			// 实例化一个遍历描述，定义遍历规则
			TraversalDescription traversalFriendsLikeMovies = graphDB.traversalDescription()
					.relationships(MyRelationshipTypes.IS_FRIEND_OF)
					.relationships(MyRelationshipTypes.HAS_SEEN, Direction.OUTGOING).uniqueness(Uniqueness.NODE_PATH)
					.evaluator(Evaluators.atDepth(2)).evaluator(new CustomNodeFilteringEvaluator(userJohn));

			// 遍历起点
			Traverser traverser = traversalFriendsLikeMovies.traverse(userJohn);
			Iterable<Node> friendsLikeMoviesNode = traverser.nodes();
			friendsLikeMoviesNode.forEach(node -> System.out.println(node.getProperty("name")));
		}
	}
}

// 自定义评估函数
class CustomNodeFilteringEvaluator implements Evaluator {
	private final Node userNode;

	public CustomNodeFilteringEvaluator(Node userNode) {
		this.userNode = userNode;
	}

	@Override
	public Evaluation evaluate(Path path) {
		// 遍历路径中的最后一个节点，当前例子中是所有的USERS、MOVIES节点
		Node currentNode = path.endNode();
		// 判断是否是MOVIES节点，如果不是，则丢弃并且继续遍历
		if (!currentNode.hasLabel(MyLabels.MOVIES)) {
			return Evaluation.EXCLUDE_AND_CONTINUE;
		}
		// 遍历指向当前节点的HAS_SEEN关系
		for (Relationship r : currentNode.getRelationships(Direction.INCOMING, MyRelationshipTypes.HAS_SEEN)) {
			// 获取HAS_SEEN关系的源头，即USERS节点，如果节点是给定的目标节点（John），则丢弃
			if (r.getStartNode().equals(userNode)) {
				return Evaluation.EXCLUDE_AND_CONTINUE;
			}
		}
		return Evaluation.INCLUDE_AND_CONTINUE;
	}
}
