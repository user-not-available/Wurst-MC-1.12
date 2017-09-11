/*
 * Copyright © 2014 - 2017 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.features.mods.movement;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;]
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.wurstclient.compatibility.WConnection;
import net.wurstclient.compatibility.WMinecraft;
import net.wurstclient.events.listeners.UpdateListener;
import net.wurstclient.features.Category;
import net.wurstclient.features.Mod;
import net.wurstclient.features.special_features.YesCheatSpf.Profile;
import net.wurstclient.settings.ModeSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@Mod.Bypasses(ghostMode = false)
public final class StepMod extends Mod implements UpdateListener
{
	private final ModeSetting mode =
		new ModeSetting("Mode", new String[]{"Simple", "Legit"}, 1)
		{
			@Override
    public void onDisabled() {
        if(getLocalPlayer() != null) {
            getLocalPlayer().stepHeight = DEFAULT_STEP_HEIGHT;
        }
    }

    @SubscribeEvent
    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
        EntityPlayer localPlayer = (EntityPlayer)event.getEntityLiving();
        if(localPlayer.onGround) {
            localPlayer.stepHeight = 1.f;
        } else {
            localPlayer.stepHeight = DEFAULT_STEP_HEIGHT;
        }
    }

    private CPacketPlayer previousPositionPacket = null;

    @SubscribeEvent
    public void onPacketSending(PacketEvent.Outgoing.Pre event) {
        if(event.getPacket() instanceof CPacketPlayer.Position ||
                event.getPacket() instanceof CPacketPlayer.PositionRotation) {
            CPacketPlayer packetPlayer = (CPacketPlayer)event.getPacket();
            if(previousPositionPacket != null &&
                    !PacketHelper.isIgnored(event.getPacket())) {
                double diffY = packetPlayer.getY(0.f) - previousPositionPacket.getY(0.f);
                // y difference must be positive
                // greater than 1, but less than 1.5
                if(diffY > DEFAULT_STEP_HEIGHT &&
                        diffY <= 1.2491870787) {
                    List<Packet> sendList = Lists.newArrayList();
                    // if this is true, this must be a step
                    // now to send additional packets to get around NCP
                    double x = previousPositionPacket.getX(0.D);
                    double y = previousPositionPacket.getY(0.D);
                    double z = previousPositionPacket.getZ(0.D);
                    sendList.add(new CPacketPlayer.Position(
                            x,
                            y + 0.4199999869D,
                            z,
                            true
                    ));
                    sendList.add(new CPacketPlayer.Position(
                            x,
                            y + 0.7531999805D,
                            z,
                            true
                    ));
                    sendList.add(new CPacketPlayer.Position(
                            packetPlayer.getX(0.f),
                            packetPlayer.getY(0.f),
                            packetPlayer.getZ(0.f),
                            packetPlayer.isOnGround()
                    ));
                    for(Packet toSend : sendList) {
                        PacketHelper.ignore(toSend);
                        getNetworkManager().sendPacket(toSend);
                    }
                    event.setCanceled(true);
                }
            }
            previousPositionPacket = (CPacketPlayer)event.getPacket();
        }
    }
}
