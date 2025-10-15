package com.optimal.backend.springboot.agent.framework.core;

import java.time.LocalDate;
import java.util.UUID;

/**
 * ThreadLocal storage for user context information.
 * Ensures all tools within a request thread have access to the correct userId, chatID, and user's local date.
 * and user's local date without needing to extract it from conversation context.
 */
public class UserContext {

    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<UUID> currentChatId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserDate = new ThreadLocal<>();

    /**
     * Set the current user ID for this thread
     */
    public static void setUserId(UUID userId) {
        currentUserId.set(userId);
    }

    public static UUID getChatId() {
        return currentChatId.get();
    }

    /**
     * Set the current chat ID for this thread
     */
    public static void setChatId(UUID chatId) {
        currentChatId.set(chatId);
    }

    public static void setChatId(String chatId) {
        if (chatId != null && !chatId.trim().isEmpty()) {
            currentChatId.set(UUID.fromString(chatId));
        }
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
     * Set the current user date for this thread (in yyyy-MM-dd format)
     * This represents the user's local date in their timezone
     */
    public static void setUserDate(String userDate) {
        currentUserDate.set(userDate);
    }

    /**
     * Get the current user date string for this thread
     * 
     * @return User's local date in yyyy-MM-dd format, or null if not set
     */
    public static String getUserDate() {
        return currentUserDate.get();
    }

    /**
     * Get the user's local date as LocalDate for date calculations
     * Falls back to server's LocalDate.now() if user date is not set
     * 
     * @return LocalDate representing user's current date
     */
    public static LocalDate getUserLocalDate() {
        String dateStr = currentUserDate.get();
        if (dateStr == null || dateStr.trim().isEmpty()) {
            System.out.println("Warning: User date not set in context, falling back to server date");
            return LocalDate.now();
        }
        try {
            System.out.println("Successfully parsed user's local date");
            return LocalDate.parse(dateStr);
            
        } catch (Exception e) {
            System.err.println("Error parsing user date '" + dateStr + "': " + e.getMessage());
            return LocalDate.now();
        }
    }

    /**
     * Clear all context data for this thread
     * Should be called at the end of request processing to prevent memory leaks
     */
    public static void clear() {
        currentUserId.remove();
        currentUserDate.remove();
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