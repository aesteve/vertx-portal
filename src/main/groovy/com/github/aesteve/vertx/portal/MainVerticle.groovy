package com.github.aesteve.vertx.portal

import com.github.aesteve.vertx.groovy.builder.ServerBuilder
import com.github.aesteve.vertx.groovy.builder.VerticleBuilder
import com.github.aesteve.vertx.groovy.builder.VerticlesDSL
import com.github.aesteve.vertx.portal.dao.MongoDAO
import groovy.transform.TypeChecked
import io.vertx.core.Future
import io.vertx.groovy.core.http.HttpServer
import io.vertx.lang.groovy.GroovyVerticle

@TypeChecked
class MainVerticle extends GroovyVerticle {

	HttpServer server
	MongoDAO mongo
	Binding binding = new Binding()
	Map mongoOpts = [host: 'localhost', port: 27017]

	@Override
	public void start(Future fut) {
		mongo = new MongoDAO(vertx, mongoOpts)
		binding.setVariable 'mongo', mongo
		VerticleBuilder verticleBuilder = new VerticleBuilder(vertx: vertx)
		VerticlesDSL dsl = verticleBuilder.build this.class.getResourceAsStream('/verticles.groovy')
		dsl.start { res ->
			if (!res) fut -= res.cause
			else createServer(fut)
		}
	}

	@Override
	public void stop(Future fut) {
		if (!server) fut++
		else server.close(fut.completeOrFail())
	}

	private void createServer(Future<Void> fut) {
		ServerBuilder builder = new ServerBuilder(vertx: vertx)
		server = builder.buildServer binding, this.class.getResourceAsStream('/server.groovy')
		server.listen { res ->
			if (!res) { fut -= res.cause }
			else fut++
		}
	}
}
