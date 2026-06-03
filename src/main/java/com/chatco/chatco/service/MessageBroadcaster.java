package com.chatco.chatco.service;

import com.vaadin.flow.shared.Registration;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service
public class MessageBroadcaster {

    private final Map<Long, List<Consumer<Long>>> listeners = new ConcurrentHashMap<>();

    public Registration register(Long conversationId, Consumer<Long> listener) {
        List<Consumer<Long>> conversationListeners =
                listeners.computeIfAbsent(conversationId, ignored -> new CopyOnWriteArrayList<>());
        conversationListeners.add(listener);

        return () -> {
            conversationListeners.remove(listener);
            if (conversationListeners.isEmpty()) {
                listeners.remove(conversationId, conversationListeners);
            }
        };
    }

    public void broadcast(Long conversationId) {
        List<Consumer<Long>> conversationListeners = listeners.get(conversationId);
        if (conversationListeners == null) {
            return;
        }
        conversationListeners.forEach(listener -> listener.accept(conversationId));
    }
}
