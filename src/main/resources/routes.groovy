import com.github.aesteve.vertx.groovy.io.impl.JacksonMarshaller
import com.github.aesteve.vertx.portal.model.Feed

router {
	marshaller 'application/json', new JacksonMarshaller()
	route('/api/1') {
		consumes 'application/json'
		produces 'application/json'
		route('/feeds') {
			get {
				mongo.getFeeds fail | it.&yield
			}
			post {
				Feed feed = body as Feed
				if (!feed.valid) fail 400
				else it++
			}
			post {
				mongo.createFeed body as Feed, fail | it.&yield
			}
		}
		get('/feeds/:id') {
			mongo.getFeed params['id'], fail | it.&yield
		}
		route('/projects') {
			get { ctx ->
				mongo.getProjects {
					if(-it) it.cause().printStackTrace()
					println it
					ctx.yield it
				}
			}
		}
	}
}