package settingdust.selfishvillager.client

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.Binding
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder
import net.minecraft.text.Text
import settingdust.selfishvillager.SelfishVillager

fun init() {}

object ModMenuEntrypoint : ModMenuApi {
    private val config: YetAnotherConfigLib
        get() {
            SelfishVillager.Config.load()
            return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("${SelfishVillager.ID}.config.title"))
                .category(
                    ConfigCategory.createBuilder()
                        .name(Text.translatable("${SelfishVillager.ID}.config.general"))
                        .option(
                            Option.createBuilder<Boolean>()
                                .name(
                                    Text.translatable(
                                        "${SelfishVillager.ID}.config.general.ray_tracing"
                                    )
                                )
                                .binding(
                                    Binding.generic(
                                        true,
                                        { SelfishVillager.Config.general.rayTracing },
                                        { SelfishVillager.Config.general.rayTracing = it }
                                    )
                                )
                                .controller { BooleanControllerBuilder.create(it).coloured(true) }
                                .build()
                        )
                        .option(
                            Option.createBuilder<Double>()
                                .name(
                                    Text.translatable(
                                        "${SelfishVillager.ID}.config.general.detect_range"
                                    )
                                )
                                .binding(
                                    Binding.generic(
                                        16.0,
                                        { SelfishVillager.Config.general.detectRange },
                                        { SelfishVillager.Config.general.detectRange = it }
                                    )
                                )
                                .controller {
                                    DoubleSliderControllerBuilder.create(it)
                                        .range(0.0, 64.0)
                                        .step(1.0)
                                }
                                .build()
                        )
                        .option(
                            Option.createBuilder<Double>()
                                .name(
                                    Text.translatable(
                                        "${SelfishVillager.ID}.config.general.sneak_range"
                                    )
                                )
                                .binding(
                                    Binding.generic(
                                        8.0,
                                        { SelfishVillager.Config.general.sneakRange },
                                        { SelfishVillager.Config.general.sneakRange = it }
                                    )
                                )
                                .controller {
                                    DoubleSliderControllerBuilder.create(it)
                                        .range(0.0, 64.0)
                                        .step(1.0)
                                }
                                .build()
                        )
                        .build()
                )
                .build()
        }

    override fun getModConfigScreenFactory() = ConfigScreenFactory { config.generateScreen(it) }
}
