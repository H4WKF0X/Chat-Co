package com.chatco.chatco.web;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
/**
 * Makes {@link ClientType} available as a controller method parameter.
 *
 * <p>This keeps controller methods simple because they do not need to manually
 * read and parse the {@code X-Client-Type} header.</p>
 */
public class ClientTypeArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(ClientType.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        return ClientType.fromHeader(webRequest.getHeader(ClientType.HEADER_NAME));
    }
}
