package lib.kasuga.content.document;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class DocumentComponentType<T> {
    public abstract Codec<T> codec();

    public abstract StreamCodec<? super FriendlyByteBuf, T> networkCodec();

    public abstract T convert(Object object) throws IllegalArgumentException;

    public abstract Object convertBack(T original) throws IllegalArgumentException;

    public abstract boolean readable();
}
