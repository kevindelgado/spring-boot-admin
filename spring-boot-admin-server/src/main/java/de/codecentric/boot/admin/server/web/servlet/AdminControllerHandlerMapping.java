/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.web.servlet;

import java.lang.reflect.Method;
import java.util.Set;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import de.codecentric.boot.admin.server.web.AdminController;
import de.codecentric.boot.admin.server.web.PathUtils;

public class AdminControllerHandlerMapping extends RequestMappingHandlerMapping {

	private final String adminContextPath;

	public AdminControllerHandlerMapping(String adminContextPath) {
		this.adminContextPath = adminContextPath;
		setPatternParser(new PathPatternParser());
	}

	@Override
	protected boolean isHandler(Class<?> beanType) {
		return AnnotatedElementUtils.hasAnnotation(beanType, AdminController.class);
	}

	@Override
	protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
		super.registerHandlerMethod(handler, method, withPrefix(mapping));
	}

	private RequestMappingInfo withPrefix(RequestMappingInfo mapping) {
		if (!StringUtils.hasText(this.adminContextPath)) {
			return mapping;
		}

		PathPatternsRequestCondition pathPatternsCondition = mapping.getPathPatternsCondition();
		if (pathPatternsCondition != null) {
			return mapping.mutate().paths(withNewPatternsFromPathPatterns(pathPatternsCondition.getPatterns())).build();
		}

		PatternsRequestCondition patternsCondition = mapping.getPatternsCondition();
		if (patternsCondition != null) {
			PatternsRequestCondition newPatterns = new PatternsRequestCondition(
					withNewPatternsFromStrings(patternsCondition.getPatterns()));
			return new RequestMappingInfo(newPatterns, mapping.getMethodsCondition(), mapping.getParamsCondition(),
					mapping.getHeadersCondition(), mapping.getConsumesCondition(), mapping.getProducesCondition(),
					mapping.getCustomCondition());
		}

		return mapping;
	}

	private String[] withNewPatternsFromPathPatterns(Set<PathPattern> patterns) {
		return patterns.stream()
				.map((pattern) -> PathUtils.normalizePath(this.adminContextPath + pattern.getPatternString()))
				.toArray(String[]::new);
	}

	private String[] withNewPatternsFromStrings(Set<String> patterns) {
		return patterns.stream().map((pattern) -> PathUtils.normalizePath(this.adminContextPath + pattern))
				.toArray(String[]::new);
	}

}
