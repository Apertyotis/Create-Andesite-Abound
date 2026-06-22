package net.apertyotis.createandesiteabound.content.hachimiGlue;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.apertyotis.createandesiteabound.AllConfig;
import net.apertyotis.createandesiteabound.AllPackets;
import net.apertyotis.createandesiteabound.mixin.create.contraption.glue.SuperGlueSelectionHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class HachimiGlueHandler {

    public static final HachimiGlueHandler HACHIMI_GLUE_HANDLER = new HachimiGlueHandler();

    private SuperGlueEntity selected;
    private Direction selectedFace;
    private AABB boundingBox;

    private static final int SYNC_COOLDOWN = 10;
    private int cooldown = 0;

    public void tick() {
        if (!AllConfig.hachimi_glue)
            return;

        Player player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if (player == null || level == null)
            return;

        SuperGlueEntity selectedOld = selected;
        selected = null;

        if (((SuperGlueSelectionHandlerAccessor) CreateClient.GLUE_HANDLER).getFirstPos() == null &&
                AllItems.SUPER_GLUE.isIn(player.getMainHandItem())) {
            Vec3 traceOrigin = RaycastHelper.getTraceOrigin(player);
            Vec3 traceTarget = RaycastHelper.getTraceTarget(player, 32, traceOrigin);
            Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();


            AABB scanArea = player.getBoundingBox().inflate(32, 16, 32);
            List<SuperGlueEntity> glueNearby = level.getEntitiesOfClass(SuperGlueEntity.class, scanArea);
            double bestDistance = Double.MAX_VALUE;
            boolean outside = true;
            for (SuperGlueEntity glueEntity : glueNearby) {
                if (glueEntity.getBoundingBox().contains(projectedView)) {
                    double distanceToSqr = glueEntity.getBoundingBox().getCenter().distanceToSqr(traceOrigin);
                    if (outside) {
                        outside = false;
                        selected = glueEntity;
                        bestDistance = distanceToSqr;
                    } else if (distanceToSqr < bestDistance){
                        selected = glueEntity;
                        bestDistance = distanceToSqr;
                    }
                }
                if (outside) {
                    Optional<Vec3> clip = glueEntity.getBoundingBox().clip(traceOrigin, traceTarget);
                    if (clip.isEmpty())
                        continue;
                    Vec3 vec3 = clip.get();
                    double distanceToSqr = vec3.distanceToSqr(traceOrigin);
                    if (distanceToSqr < bestDistance) {
                        selected = glueEntity;
                        bestDistance = distanceToSqr;
                    }
                }
            }
        }

        boolean needSync = false;
        if (selectedOld != null && selectedOld != selected) {
            needSync = true;
        } else if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0) {
                needSync = true;
            }
        }
        if (needSync) {
            sync(selectedOld, boundingBox);
            boundingBox = null;
            cooldown = 0;
        }
        // selected != selectedOld 时，延迟 1gt 渲染，等待（距离足够近时）原版强力胶选择逻辑判断完成
        if (selected == null || selected != selectedOld)
            return;

        AABB bb = boundingBox == null ? selected.getBoundingBox() : boundingBox;
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        boolean inside = bb.contains(projectedView);
        RaycastHelper.PredicateTraceResult result = RaycastHelper.rayTraceUntil(player, 40,
                pos -> inside ^ bb.contains(VecHelper.getCenterOf(pos)));

        selectedFace = result.missed() ? null :
                inside ? result.getFacing().getOpposite() : result.getFacing();

        boolean near = ((SuperGlueSelectionHandlerAccessor) CreateClient.GLUE_HANDLER).getSelected() == selected;
        CreateClient.OUTLINER.chaseAABB(selected, bb)
                .colored(near ? 0xffa166 : 0xffeb85)
                .withFaceTextures(AllSpecialTextures.GLUE, AllSpecialTextures.GLUE)
                .disableLineNormals()
                .lineWidth(1 / 16f)
                .highlightFace(selectedFace);
    }

    public boolean mouseScrolled(double delta) {
        if (!AllKeys.ctrlDown() || selected == null)
            return false;
        if (selectedFace == null)
            return true;

        AABB bb = boundingBox == null ? selected.getBoundingBox() : boundingBox;
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        if (bb.contains(projectedView))
            delta *= -1;
        Vec3i vec = selectedFace.getNormal();
        int x = (int) (vec.getX() * delta);
        int y = (int) (vec.getY() * delta);
        int z = (int) (vec.getZ() * delta);

        Direction.AxisDirection axisDirection = selectedFace.getAxisDirection();
        if (axisDirection == Direction.AxisDirection.NEGATIVE)
            bb = bb.move(-x, -y, -z);

        double maxX = Math.max(bb.maxX - x * axisDirection.getStep(), bb.minX);
        double maxY = Math.max(bb.maxY - y * axisDirection.getStep(), bb.minY);
        double maxZ = Math.max(bb.maxZ - z * axisDirection.getStep(), bb.minZ);

        if (maxX == bb.minX || maxY == bb.minY || maxZ == bb.minZ)
            return true;

        bb = new AABB(bb.minX, bb.minY, bb.minZ, maxX, maxY, maxZ);
        boundingBox = bb;
        cooldown = SYNC_COOLDOWN;

        return true;
    }

    private void sync(SuperGlueEntity glue, AABB bb) {
        if (glue != null && bb != null) {
            glue.setBoundingBox(bb);
            AllPackets.getChannel().sendToServer(new HachimiGlueModificationPacket(glue.getId(), bb));
        }
    }

    public SuperGlueEntity getSelected() {
        return selected;
    }
}
