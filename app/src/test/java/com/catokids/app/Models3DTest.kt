package com.catokids.app

import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.ui.games.Models3D
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** A missing 3D model or texture crashes the game, so every file is checked here. */
class Models3DTest {

    private val assets = File("src/main/assets")

    @Test
    fun `every 3D model and the textures it points to are bundled`() {
        Models3D.allPaths.forEach { path ->
            val glb = File(assets, path)
            assertTrue("Missing 3D model $path", glb.isFile)
            // GLB: 12-byte header, then a JSON chunk (length, type, data).
            val bytes = glb.readBytes()
            val jsonLength = ByteBuffer.wrap(bytes, 12, 4).order(ByteOrder.LITTLE_ENDIAN).int
            val json = String(bytes, 20, jsonLength)
            Regex("\"uri\"\\s*:\\s*\"([^\"]+)\"").findAll(json).map { it.groupValues[1] }
                .filterNot { it.startsWith("data:") }
                .forEach { uri ->
                    assertTrue("$path needs $uri", File(glb.parentFile, uri).isFile)
                }
        }
    }

    @Test
    fun `every bath time round has a 3D animal`() {
        Models3D.lessons3D.forEach { id ->
            val lesson = CatoCurriculum.lesson(id)
            assertTrue("3D lesson $id does not exist", lesson != null)
            lesson!!.content.rounds.forEach { round ->
                assertTrue("$id/${round.id} has no 3D animal for ${round.emoji}", Models3D.hasAnimal(round.emoji))
            }
        }
    }

    @Test
    fun `toy game lessons exist`() {
        com.catokids.app.ui.games.ToyGames.lessonIds.forEach { id ->
            assertTrue("Toy game lesson $id does not exist", CatoCurriculum.lesson(id) != null)
        }
    }

    @Test
    fun `every Pre-KG lesson is a toy its rounds can play`() {
        val prekg = CatoCurriculum.all.filter { it.grade == com.catokids.app.data.model.Grade.PREKG }
        prekg.forEach { lesson ->
            val toy = com.catokids.app.ui.games.toyFor(lesson)
            lesson.content.rounds.forEach { r ->
                val where = "${lesson.id}/${r.id} (${toy.name})"
                when (toy) {
                    com.catokids.app.ui.games.Toy.LIGHT -> assertTrue("$where has nothing to light", r.glyph.isNotBlank())
                    com.catokids.app.ui.games.Toy.BASKET -> assertTrue("$where counts nothing", r.count in 1..20)
                    com.catokids.app.ui.games.Toy.HOME -> assertTrue("$where has no homes", r.buckets.isNotEmpty() || r.pairs.isNotEmpty())
                    com.catokids.app.ui.games.Toy.BATH -> {}
                    else -> assertTrue("$where has no right answer", r.options.count { it.correct } >= 1)
                }
            }
        }
    }
}
