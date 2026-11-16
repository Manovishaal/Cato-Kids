package com.catokids.app.ui.games

import com.google.android.filament.Engine
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.ModelNode

/**
 * The 3D side of the Pre-KG toy games, rendered with SceneView (Google Filament, Apache 2.0).
 *
 * Models are Kenney's "Cube Pets" and "Food Kit" (CC0, public domain) in assets/models3d.
 * Every Cube Pets animal ships with named animations — idle, walk, eat, dance,
 * gesture-positive, gesture-negative — which the games play at the right moments.
 */
object Models3D {

    private const val PETS = "models3d/pets/animal-"
    const val MUD = "models3d/food/cookie-chocolate.glb"
    const val FOAM = "models3d/food/whipped-cream.glb"

    /** Animals by picture, with the short word the voice uses for them. */
    private val animals: Map<String, Pair<String, String>> = mapOf(
        "🐷" to ("${PETS}pig.glb" to "pig"),
        "🐶" to ("${PETS}dog.glb" to "dog"),
        "🐱" to ("${PETS}cat.glb" to "cat"),
        "🐰" to ("${PETS}bunny.glb" to "bunny"),
        "🐘" to ("${PETS}elephant.glb" to "elephant"),
        "🐵" to ("${PETS}monkey.glb" to "monkey"),
    )

    private fun key(emoji: String) = emoji.filter { it != '️' }

    fun hasAnimal(emoji: String) = key(emoji) in animals
    fun animalPath(emoji: String): String = animals[key(emoji)]?.first ?: "${PETS}dog.glb"
    fun animalName(emoji: String): String = animals[key(emoji)]?.second ?: "puppy"

    /** Every model file the games can load — checked by the unit tests. */
    val allPaths: Set<String> get() = animals.values.map { it.first }.toSet() + MUD + FOAM

    /** Pre-KG lessons that need the 3D engine. */
    val lessons3D = setOf(ToyGames.BATH)
}

/** Shared Filament objects for a whole lesson, so rounds don't rebuild the engine. */
class Scene3DContext(val engine: Engine, val modelLoader: ModelLoader, val materialLoader: MaterialLoader)

/** Plays one of the Cube Pets animations on its own (they would otherwise mix). */
fun ModelNode.playOnly(name: String, loop: Boolean = true) {
    if (animationCount == 0) return
    listOf("static", "idle", "walk", "run", "eat", "dance", "gesture-positive", "gesture-negative").forEach { stopAnimation(it) }
    playAnimation(name, loop = loop)
}
