package com.github.aesteve.vertx.portal.dao

import com.github.aesteve.vertx.portal.model.Feed
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.mongo.MongoClient

class MongoDAO {

	private final static Logger log = LoggerFactory.getLogger MongoDAO.class

	final static String FEEDS_COLLECTION = 'feeds'
	final static String PROJECTS_COLLECTION = 'github_projects'

	MongoClient mongo

	public MongoDAO(Vertx vertx, Map conf) {
		mongo = MongoClient.createNonShared vertx, conf
	}

	void getFeed(String id, Handler<AsyncResult<Feed>> handler) {
		Map<String, Object> query = ['_id', id]
		mongo.findOne FEEDS_COLLECTION, query, null, { res ->
			if (-res) handler.handle res
			else {
				if (!res.result) handler.handle res
				else {
					Feed feed = res.result as Feed // map constructor
					handler.handle Future.succeededFuture(feed)
				}
			}
		}
	}

	void createFeed(Feed feed, Handler<AsyncResult<Feed>> handler) {
		mongo.insert FEEDS_COLLECTION, feed as Map, { res ->
			if (-res) handler.handle res
			else {
				if (!res.result) handler.handle res
				else {
					feed._id = res.result
					handler.handle Future.succeededFuture(feed)
				}
			}
		}
	}

	void getFeeds(Handler<AsyncResult<Collection<Feed>>> handler) {
		mongo.find FEEDS_COLLECTION, [:], { res ->
			if (-res) handler.handle res
			else {
				if (!res.result) handler.handle Future.succeededFuture([])
				else {
					def results = res.result.collect { it as Feed }
					handler.handle Future.succeededFuture(results)
				}
			}
		}
	}

	void deleteProjects(Handler<AsyncResult<Void>> handler) {
		mongo.remove PROJECTS_COLLECTION, [:], handler
	}

	void createProject(Map project) {
		mongo.insert PROJECTS_COLLECTION, project, { res ->
			if (-res) log.error res.cause()
		}
	}

	void getProjects(Handler<AsyncResult<List>> handler) {
		mongo.find PROJECTS_COLLECTION, [:], handler
	}
}
