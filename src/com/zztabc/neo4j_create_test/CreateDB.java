package com.zztabc.neo4j_create_test;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class CreateDB {
	/**
	 * 在指定路径下创建数据库
	 * 
	 * @param path
	 *            数据库路径
	 */
	public static GraphDatabaseService createDataBase(String path) {
		GraphDatabaseService graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(new File(path));

		return graphDB;
	}

	/**
	 * 在指定数据库中创建节点和属性
	 * 
	 * @param graphDB
	 *            使用的数据库
	 */
	public static void createNodeAndRelationship(GraphDatabaseService graphDB) {
		try (Transaction tx = graphDB.beginTx()) {
			Node user1 = graphDB.createNode(MyLabels.USERS);
			Node user2 = graphDB.createNode(MyLabels.USERS);
			Node user3 = graphDB.createNode(MyLabels.USERS);

			user1.setProperty("name", "John Johnson");
			user2.setProperty("name", "Kate Smith");
			user3.setProperty("name", "Jack Jeffries");

			user1.createRelationshipTo(user2, MyRelationshipTypes.IS_FRIEND_OF);
			user1.createRelationshipTo(user3, MyRelationshipTypes.IS_FRIEND_OF);

			Node movie1 = graphDB.createNode(MyLabels.MOVIES);
			Node movie2 = graphDB.createNode(MyLabels.MOVIES);
			Node movie3 = graphDB.createNode(MyLabels.MOVIES);

			movie1.setProperty("name", "Fargo");
			movie2.setProperty("name", "Alien");
			movie3.setProperty("name", "Heat");

			Relationship rela1 = user1.createRelationshipTo(movie1, MyRelationshipTypes.HAS_SEEN);
			Relationship rela2 = user2.createRelationshipTo(movie3, MyRelationshipTypes.HAS_SEEN);
			Relationship rela3 = user3.createRelationshipTo(movie1, MyRelationshipTypes.HAS_SEEN);
			Relationship rela4 = user3.createRelationshipTo(movie2, MyRelationshipTypes.HAS_SEEN);

			rela1.setProperty("stars", 5);
			rela2.setProperty("stars", 3);
			rela3.setProperty("stars", 4);
			rela4.setProperty("stars", 5);

			tx.success();
		}
	}
}
