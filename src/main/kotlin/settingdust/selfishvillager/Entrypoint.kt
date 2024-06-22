package settingdust.selfishvillager

import com.mojang.serialization.JsonOps
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.div
import kotlin.io.path.reader
import kotlin.io.path.writeText
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.StructureTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.TextCodecs
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.MathHelper
import net.minecraft.village.VillageGossipType
import net.minecraft.world.BlockStateRaycastContext
import net.minecraft.world.World
import org.joml.Vector3d
import org.quiltmc.qkl.library.serialization.CodecFactory

fun init() {
    PlayerBlockBreakEvents.AFTER.register { world, player, pos, state, _ ->
        if (state.isIn(SelfishVillager.Tags.NON_PROPERTY)) return@register
        if ((world as ServerWorld)
            .structureAccessor
            .getStructureStarts(ChunkPos(pos)) { true }
            .none { it.boundingBox.contains(pos) })
            return@register
        val villagerEntities = visableVillagers(world, player, pos)
        if (villagerEntities.isEmpty()) return@register
        player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 1f, 1f)
        for (villagerEntity in villagerEntities) {
            villagerEntity.`selfishvillager$gossips`.startGossip(
                player.uuid, VillageGossipType.MINOR_NEGATIVE, 10)
        }
    }

    UseBlockCallback.EVENT.register { player, world, _, hitResult ->
        if (player.isSpectator) return@register ActionResult.PASS
        if (world !is ServerWorld) return@register ActionResult.PASS
        if (hitResult.type != HitResult.Type.BLOCK) return@register ActionResult.PASS
        val state = world.getBlockState(hitResult.blockPos)
        if (state.isIn(SelfishVillager.Tags.NON_PROPERTY)) return@register ActionResult.PASS
        if (!state.isIn(SelfishVillager.Tags.INTERACTABLE_PROPERTY))
            return@register ActionResult.PASS
        val registry = world.registryManager[RegistryKeys.STRUCTURE]
        val pos = hitResult.blockPos
        if (world.structureAccessor
            .getStructureStarts(ChunkPos(pos)) { registry.getEntry(it).isIn(StructureTags.VILLAGE) }
            .none { it.boundingBox.contains(pos) })
            return@register ActionResult.PASS
        val villagerEntities =
            visableVillagers(world, player, BlockPos.ofFloored(player.boundingBox.center))
        if (villagerEntities.isEmpty()) return@register ActionResult.PASS
        player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 2f, 1f)
        for (villagerEntity in villagerEntities) {
            villagerEntity.`selfishvillager$gossips`.startGossip(
                player.uuid, VillageGossipType.MAJOR_NEGATIVE, 10)
        }
        return@register ActionResult.PASS
    }
}

private fun visableVillagers(
    world: World,
    player: PlayerEntity,
    pos: BlockPos
): List<GossipHolder> {
    val range =
        if (player.isSneaking) SelfishVillager.Config.general.sneakRange
        else SelfishVillager.Config.general.detectRange
    return world.getEntitiesByClass(LivingEntity::class.java, Box(pos).expand(range)) { villager ->
        if (villager !is GossipHolder) return@getEntitiesByClass false
        if (!SelfishVillager.Config.general.rayTracing) return@getEntitiesByClass true
        if (villager is VillagerEntity && villager.isSleeping) return@getEntitiesByClass false
        val targetVector = player.pos.subtract(villager.pos)
        val angle =
            Vector3d(
                    villager.rotationVector.x, villager.rotationVector.y, villager.rotationVector.z)
                .angle(Vector3d(targetVector.x, targetVector.y, targetVector.z)) * 180 /
                MathHelper.PI

        val entityHitResult by lazy {
            ProjectileUtil.raycast(
                villager,
                villager.pos,
                player.pos,
                player.boundingBox,
                { it == player },
                range * range)
        }

        val blockHitResult by lazy {
            world.raycast(
                BlockStateRaycastContext(villager.eyePos, player.boundingBox.center) {
                    it.isOpaque
                })
        }
        (angle <= 70 || angle >= 290) &&
            entityHitResult?.type != HitResult.Type.MISS &&
            blockHitResult.type != HitResult.Type.BLOCK
    } as List<GossipHolder>
}

object SelfishVillager {

    const val ID = "selfish_villager"

    private val codecFactory = CodecFactory {
        encodeDefaults = true
        ignoreUnknownKeys = true

        codecs { unnamed(TextCodecs.CODEC) }
    }

    object Tags {
        val INTERACTABLE_PROPERTY =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(ID, "interactable_property"))!!
        val NON_PROPERTY = TagKey.of(RegistryKeys.BLOCK, Identifier.of(ID, "non_property"))!!
    }

    @Serializable
    data class GeneralConfig(
        var rayTracing: Boolean = true,
        var detectRange: Double = 16.0,
        var sneakRange: Double = 8.0
    ) {
        companion object {
            val CODEC = codecFactory.create<GeneralConfig>()
        }
    }

    data object Config {
        var general = GeneralConfig()
            private set

        private val configDir = FabricLoader.getInstance().configDir / ID
        private val generalConfigPath = configDir / "general.json"

        fun load() {
            try {
                configDir.createDirectories()
                generalConfigPath.createFile()
                generalConfigPath.writeText("{}")
            } catch (_: Exception) {}
            general =
                GeneralConfig.CODEC.parse(
                        JsonOps.INSTANCE, JsonHelper.deserialize(generalConfigPath.reader()))
                    .orThrow
        }

        init {
            load()
        }
    }
}
