package com.github.aesteve.vertx.portal.model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
@CompileStatic
class Feed {

	enum Type {
		TWITTER, RSS
	}

	String _id
	Type type
	String url

	boolean isValid() {
		return type && url
	}

}
