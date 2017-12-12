package com.zztabc.index_test;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

import com.zztabc.neo4j_create_test.CreateDB;

public class IndexTest {
	public static void main(String[] args) {
		GraphDatabaseService graphDB = CreateDB.createDataBase("E://Neo4JDB//index_test");

		// 对一个节点创建索引项
		try (Transaction tx = graphDB.beginTx()) {
			String johnSmithName = "John Smith";
			String johnSmithEmail = "jsmith@example.org";
			
			Node personOne = graphDB.createNode();
			personOne.setProperty("name", johnSmithName);
			personOne.setProperty("email", johnSmithEmail);
			
			IndexManager indexManager = graphDB.index();
			Index<Node> userIndex = indexManager.forNodes("users");
			userIndex.add(personOne, "email", johnSmithEmail);
			
			
			
			tx.success();
		}
	}
}
