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

    public static class DocumentBoolean extends DocumentComponentType<Boolean> {
        @Override
        public Codec<Boolean> codec() {
            return Codec.BOOL;
        }

        @Override
        public StreamCodec<? super FriendlyByteBuf, Boolean> networkCodec() {
            return ByteBufCodecs.BOOL;
        }

        @Override
        public Boolean convert(Object object) throws IllegalArgumentException {
            if(object instanceof Boolean s)
                return s;
            if(object instanceof String str)
                return Boolean.valueOf(str);
            if(object instanceof Number num)
                return !Objects.equals(num.intValue(), 0);
            return true;
        }

        @Override
        public Object convertBack(Boolean original) throws IllegalArgumentException {
            return original;
        }

        @Override
        public boolean readable() {
            return false;
        }
    }

    public static class DocumentInt extends DocumentComponentType<Integer> {
        @Override
        public Codec<Integer> codec() {
            return Codec.INT;
        }

        @Override
        public StreamCodec<? super FriendlyByteBuf, Integer> networkCodec() {
            return ByteBufCodecs.INT;
        }

        @Override
        public Integer convert(Object object) throws IllegalArgumentException {
            if(object instanceof Integer s)
                return s;
            if(object instanceof Number n)
                return n.intValue();
            if(object instanceof String str)
                return Integer.valueOf(str);
            return 0;
        }

        @Override
        public Object convertBack(Integer original) throws IllegalArgumentException {
            return original;
        }

        @Override
        public boolean readable() {
            return false;
        }
    }
}
