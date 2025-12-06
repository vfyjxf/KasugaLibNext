package lib.kasuga.create.content.train.signal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

// @TODO: Add LICENSE, Inspired by Create
public class ResourcePalette {
    List<ResourceLocation> resourceLocations = new ArrayList<>();

    public ResourcePalette(){

    }

    public int encode(ResourceLocation location){
        int index = resourceLocations.indexOf(location);
        if(index == -1){
            index = resourceLocations.size();
            resourceLocations.add(location);
        }
        return index;
    }

    public ResourceLocation decode(int index){
        if(index >= this.resourceLocations.size() || index < 0)
            throw new IllegalArgumentException("Illegal ResourceLocation Encoding Value.");
        return this.resourceLocations.get(index);
    }

    public void write(CompoundTag tag){
        ListTag listTag = new ListTag();
        for (ResourceLocation resourceLocation : resourceLocations) {
            listTag.add(StringTag.valueOf(resourceLocation.toString()));
        }
        tag.put("ResourcePalette", listTag);
    }

    public static ResourcePalette read(CompoundTag tag){
        ResourcePalette palette = new ResourcePalette();
        ListTag listTag = tag.getList("ResourcePalette", Tag.TAG_STRING);
        for(int i=0;i<listTag.size();i++){
            palette.addResourceString(listTag.getString(i));
        }
        return palette;
    }

    private void addResourceString(String string) {
        this.resourceLocations.add(ResourceLocation.parse(string));
    }
}
