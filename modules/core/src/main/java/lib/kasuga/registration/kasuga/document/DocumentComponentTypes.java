package lib.kasuga.registration.kasuga.document;

import com.mojang.serialization.Codec;
import lib.kasuga.content.document.DocumentComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class DocumentComponentTypes {
    // @TODO: properties 化
    public static class DocumentString extends DocumentComponentType<String> {
        @Override
        public Codec<String> codec() {
            return Codec.STRING;
        }

        @Override
        public StreamCodec<? super FriendlyByteBuf, String> networkCodec() {
            return ByteBufCodecs.STRING_UTF8;
        }

        @Override
        public String convert(Object object) throws IllegalArgumentException {
            if(object instanceof String s)
                return s;
            return String.valueOf(object);
        }

        @Override
        public Object convertBack(String original) throws IllegalArgumentException {
            return original;
        }

        @Override
        public boolean readable() {
            return false;
        }
    }
}
