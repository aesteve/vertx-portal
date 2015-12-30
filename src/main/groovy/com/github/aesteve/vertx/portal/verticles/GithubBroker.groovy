package com.github.aesteve.vertx.portal.verticles

import com.github.aesteve.vertx.portal.dao.MongoDAO
import groovy.transform.TypeChecked
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.http.HttpClient
import io.vertx.groovy.core.http.HttpClientRequest
import io.vertx.lang.groovy.GroovyVerticle

import static io.vertx.core.http.HttpHeaders.USER_AGENT

@TypeChecked
class GithubBroker extends GroovyVerticle {

	private static final Logger log = LoggerFactory.getLogger GithubBroker.class

	HttpClient http
	MongoDAO mongo
	Map options = [
		defaultHost: 'api.github.com',
		defaultPort: 443,
		ssl: true
	]

	@Override
	void start() {
		log.info "start broker"
		mongo = new MongoDAO(vertx, [host: 'localhost', port: 27017])
		http = vertx.createHttpClient options
		mongo.deleteProjects {
			this.findVertxProjects()
			vertx.setPeriodic 60000, this.&findVertxProjects
		}
	}

	void findVertxProjects(Long timerId = null) {
		log.info "find projects"
		HttpClientRequest req = http['/search/repositories?q=vertx&sort=stars&per_page=1000']
		req.headers[USER_AGENT] = 'vertx-portal'
		req >> { response ->
			response >>> {
				Map body = it as Map
				log.info body.total_count
				def items = body.items.collect {
					it['score'] = Math.round it['score'] as Double
					it
				}
				items.each mongo.&createProject
			}
		}
		req++
	}
}
