package example.game;

public final class LifecycleMailPolicy {
    public Decision decide(LifecycleEvent event) {
        if (event.moderation() == LifecycleEvent.ModerationStatus.PENDING) {
            return new Decision("MODERATION_REQUIRED", "PLAYER_ASSET_REVIEW", event.assetName());
        }
        return new Decision("SEND", "PLAYER_ASSET_APPROVED", event.assetName());
    }

    public record Decision(String action, String templateName, String assetName) {}
}
