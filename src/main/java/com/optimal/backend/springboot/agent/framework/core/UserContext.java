package com.optimal.backend.springboot.agent.framework.core;

import java.util.UUID;

/**
 * ThreadLocal storage for user context information.
 * Ensures all tools within a request thread have access to the correct userId
 * without needing to extract it from conversation context.
 */
public class UserContext {

    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();

    /**
     * Set the current user ID for this thread
     */
    public static void setUserId(UUID userId) {
        currentUserId.set(userId);
    }

    /**
     * Set the current user ID for this thread from a string
     */
    public static void setUserId(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            try {
                currentUserId.set(UUID.fromString(userId));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid userId format: " + userId, e);
            }
        }
    }

    /**
     * Get the current user ID for this thread
     * 
     * @return UUID of the current user, or null if not set
     */
    public static UUID getUserId() {
        return currentUserId.get();
    }

    /**
     * Get the current user ID as a string
     * 
     * @return String representation of the user ID, or null if not set
     */
    public static String getUserIdString() {
        UUID userId = currentUserId.get();
        return userId != null ? userId.toString() : null;
    }

    /**
     * Check if user ID is set for current thread
     */
    public static boolean hasUserId() {
        return currentUserId.get() != null;
    }

    /**
     * Clear the user ID for this thread
     * Should be called at the end of request processing to prevent memory leaks
     */
    public static void clear() {
        currentUserId.remove();
    }

    /**
     * Get the current user ID with validation
     * 
     * @throws IllegalStateException if no user ID is set
     */
    public static UUID requireUserId() {
        UUID userId = getUserId();
        if (userId == null) {
            throw new IllegalStateException(
                    "No user ID set in current context. Ensure UserContext.setUserId() is called before using tools.");
        }
        return userId;
    }

    /**
     * Get the current user ID as string with validation
     * 
     * @throws IllegalStateException if no user ID is set
     */
    public static String requireUserIdString() {
        return requireUserId().toString();
    }
}