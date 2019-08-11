package com.tryprospect.todo.container;

import java.util.Arrays;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.model.AnnotatedMethod;

import com.tryprospect.todo.annotations.Status;

@Provider
public class StatusFilterFeature implements DynamicFeature {

    @Override
    public void configure(final ResourceInfo resourceInfo, final FeatureContext featureContext) {
        final AnnotatedMethod annotatedMethod = new AnnotatedMethod(resourceInfo.getResourceMethod());
        useStatusFilterIfMethodHasStatusAnnotation(annotatedMethod, featureContext);
    }

    private void useStatusFilterIfMethodHasStatusAnnotation(AnnotatedMethod annotatedMethod, final FeatureContext featureContext) {
        final Status status = annotatedMethod.getAnnotation(Status.class);
        if (status != null) {
            featureContext.register(new StatusFilter());
        }
    }

    private static class StatusFilter implements ContainerResponseFilter {

        @Override
        public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) {
            setResponseStatusValueToAnnotationValueIfPresent(responseContext);
        }

        private void setResponseStatusValueToAnnotationValueIfPresent(final ContainerResponseContext responseContext) {
            Arrays.stream(responseContext.getEntityAnnotations())
                    .filter(annotation -> annotation instanceof Status)
                    .forEach(entityWithStatusAnnotation ->
                        responseContext.setStatus(((Status)entityWithStatusAnnotation).value())
                    );
        }
    }
}