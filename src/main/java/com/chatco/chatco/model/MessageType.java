package com.chatco.chatco.model;

/**
 * Describes the content kind of a message, used to select the correct renderer.
 *
 * <ul>
 *   <li>{@code TEXT} – plain text message</li>
 *   <li>{@code IMAGE} – inline image attachment</li>
 *   <li>{@code FILE} – generic file attachment</li>
 *   <li>{@code SYSTEM} – automated system notification (e.g. user joined)</li>
 * </ul>
 */
public enum MessageType {
    TEXT, IMAGE, FILE, SYSTEM
}