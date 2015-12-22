package com.github.aesteve.vertx.portal

import com.github.aesteve.vertx.groovy.builder.ServerBuilder
import com.github.aesteve.vertx.groovy.builder.VerticleBuilder
import com.github.aesteve.vertx.groovy.builder.VerticlesDSL
import com.github.aesteve.vertx.portal.dao.MongoDAO
import io.vertx.core.Future
import io.vertx.groovy.core.http.HttpServer
import io.vertx.lang.groovy.GroovyVerticle

class MainVerticle extends GroovyVerticle {

	HttpServer server
	MongoDAO mongo

	@Override
	public void start(Future fut) {
		ServerBuilder builder = new ServerBuilder(vertx: vertx)
		mongo = new MongoDAO(vertx, [host: 'localhost', port: 27017])
		Binding binding = new Binding()
		binding.setVariable 'mongo', mongo
		server = builder.buildServer binding, this.class.getResourceAsStream('/server.groovy')
		server.listen { res ->
			if (-res) fut - res.cause()
			else {
				VerticleBuilder verticleBuilder = new VerticleBuilder(vertx: vertx)
				VerticlesDSL dsl = verticleBuilder.build this.class.getResourceAsStream('/verticles.groovy')
				dsl.start { deployRes ->
					if (-deployRes) fut - deployRes.cause()
					else fut.complete()
				}
			}
		}
	}

	@Override
	public void stop(Future fut) {
		server.close fut.completeOrFail()
	}
}
