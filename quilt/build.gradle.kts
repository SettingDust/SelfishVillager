plugins { alias(catalog.plugins.quilt.loom) }

val id: String by rootProject.properties

repositories {
    maven {
        name = "Quilt"
        url = uri("https://maven.quiltmc.org/repository/release")
        content { includeGroupAndSubgroups("org.quiltmc") }
    }
}

dependencies {
    minecraft(catalog.minecraft)
    mappings(variantOf(catalog.yarn) { classifier("v2") })

    modRuntimeOnly(catalog.quilt.loader)
    modRuntimeOnly(catalog.quilt.fabric.api)
    modRuntimeOnly(catalog.fabric.kotlin) { exclude(module = "fabric-loader") }

    modRuntimeOnly(catalog.modmenu) { exclude(module = "fabric-loader") }
    modRuntimeOnly(catalog.yacl.fabric) { isTransitive = false }

    runtimeOnly(project(":mod", configuration = "namedElements")) { isTransitive = false }
    modRuntimeOnly(catalog.kinecraft.serialization)

    modRuntimeOnly(catalog.jade)
    modRuntimeOnly(catalog.reputation)
}

tasks {
    classes { dependsOn(":mod:remapJar") }

    ideaSyncTask { enabled = true }
}
