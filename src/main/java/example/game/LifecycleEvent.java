package example.game;

public record LifecycleEvent(String playerId, String assetName, String eventName, ModerationStatus moderation) {
    public enum ModerationStatus { APPROVED, PENDING }
}
