package com.qisumei.c4.client.resource;

import com.tacz.guns.client.animation.statemachine.ItemAnimationStateContext;
import net.minecraft.world.item.ItemStack;

public class C4AnimationStateContext extends ItemAnimationStateContext {
    private ItemStack currentItem = ItemStack.EMPTY;
    private int usingTick = 0;
    private boolean using = false;

    public void setCurrentItem(ItemStack stack) {
        this.currentItem = stack;
    }

    public int getStackCount() {
        return currentItem.getCount();
    }

    public int getUsingTick() {
        return usingTick;
    }

    public void setUsingTick(int tick) {
        this.usingTick = tick;
    }

    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }
}
