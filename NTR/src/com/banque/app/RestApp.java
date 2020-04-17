package com.banque.app;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.banque.service.RestService;

public class RestApp extends Application {
	
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(RestService.class);
		return s;
	}
}
