package me.phoenixra.atumconfig.api.placeholders;


import me.phoenixra.atumconfig.api.ConfigOwner;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a placeholder that
 * can be registered in {@link PlaceholderManager}
 * <p>It is so-called global placeholder.</p>
 */
public interface RegistrablePlaceholder extends Placeholder{

    /**
     * Register the arguments.
     *
     * @return The arguments.
     */
    @NotNull
    default RegistrablePlaceholder register() {
        PlaceholderManager.registerPlaceholder(this);
        return this;
    }

    /**
     * Get the config owner that holds the arguments.
     *
     * @return The config owner.
     */
    @NotNull
    @Override
    ConfigOwner getConfigOwner();
}
