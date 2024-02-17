package me.phoenixra.atumconfig.api.placeholders;

import me.phoenixra.atumconfig.api.ConfigOwner;
import me.phoenixra.atumconfig.api.placeholders.context.PlaceholderContext;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a placeholder that can be injected into a
 * {@link InjectablePlaceholderList} to be used in a {@link PlaceholderContext}.
 *
 * <p>It cannot be registered and exists only in injection</p>
 */
public interface InjectablePlaceholder extends Placeholder{

    /**
     * Get the mod that holds the arguments.
     *
     * @return The config owner.
     */
    @Nullable
    @Override
    default ConfigOwner getConfigOwner() {
        return null;
    }
}
