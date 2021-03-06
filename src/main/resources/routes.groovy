import com.github.aesteve.vertx.groovy.io.impl.JacksonMarshaller
import com.github.aesteve.vertx.portal.model.Feed

router {
	marshaller 'application/json', new JacksonMarshaller()
	route('/api/1') {
		consumes 'application/json'
		produces 'application/json'
		route('/test') {
			get {
				response << 'test'
			}
		}
		route('/feeds') {
			get {
				mongo.getFeeds fail | it.&yield
			}
			get('/feeds/:id') {
				mongo.getFeed params['id'], fail | it.&yield
			}
			post {
				Feed feed = body as Feed
				if (!feed?.valid) fail 400
				else {
					mongo.createFeed feed, fail | it.&yield
				}
			}
		}
		route('/projects') {
			get {
				mongo.getProjects fail | it.&yield
			}
		}
	}
}