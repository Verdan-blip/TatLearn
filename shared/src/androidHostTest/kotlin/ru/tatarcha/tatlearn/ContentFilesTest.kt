package ru.tatarcha.tatlearn

import kotlinx.serialization.json.Json
import ru.tatarcha.tatlearn.data.remote.dto.LessonContentDto
import ru.tatarcha.tatlearn.data.remote.dto.ModuleDto
import ru.tatarcha.tatlearn.domain.model.NarratorVoice
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Проверяет учебные JSON в composeResources/files прямо с диска (только Android host-тест: на iOS файлы ресурсов в тестах недоступны).
 * Битый JSON в уроке иначе проявляется только ошибкой UNKNOWN на экране урока.
 */
class ContentFilesTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val root = File("src/commonMain/composeResources/files")

    private fun modules(): List<ModuleDto> = json.decodeFromString(File(root, "modules.json").readText())

    /** Локальный путь (не `http`) должен указывать на непустой файл в composeResources/files, и у женского голоса диктора тоже. */
    private fun assertFile(lessonFile: String, audioUrl: String) {
        if (audioUrl.startsWith("http")) return
        val audio = File(root, audioUrl)
        assertTrue(audio.exists() && audio.length() > 0, "$lessonFile: audio file is missing or empty: $audioUrl")
        val female = File(root, NarratorVoice.Female.resolve(audioUrl))
        assertTrue(female.exists() && female.length() > 0, "$lessonFile: female narrator file is missing or empty: ${NarratorVoice.Female.resolve(audioUrl)}")
    }

    @Test fun modulesFileParsesAndHasUniqueIds() {
        val modules = modules()
        assertTrue(modules.isNotEmpty())
        assertEquals(modules.size, modules.map { it.id }.toSet().size, "module ids must be unique")
        val lessonIds = modules.flatMap { it.lessons }.map { it.id }
        assertEquals(lessonIds.size, lessonIds.toSet().size, "lesson ids must be unique")
        modules.mapNotNull { it.requires }.forEach { required ->
            assertTrue(modules.any { it.id == required }, "requires points to a missing module: $required")
        }
    }

    @Test fun everyLessonOfEveryModuleHasValidContentFile() {
        modules().flatMap { it.lessons }.forEach { listed ->
            val file = File(root, "lessons/${listed.id}.json")
            assertTrue(file.exists(), "missing lesson file ${file.name}")
            val lesson = json.decodeFromString<LessonContentDto>(file.readText())
            assertEquals(listed.id, lesson.lesson.id, "${file.name}: id mismatch")
            assertEquals(listed.number, lesson.lesson.number, "${file.name}: number mismatch")
            assertTrue(lesson.matching.pairs.size >= 2, "${file.name}: too few pairs")
            assertTrue(lesson.matching.notes.keys.all { key -> lesson.matching.pairs.any { it.tatar == key } }, "${file.name}: note key is not a pair word")
            lesson.quiz.forEach {
                assertTrue(it.correctIndex in it.options.indices, "${file.name}: quiz correctIndex out of range for ${it.word}")
                assertEquals(it.options.size, it.options.toSet().size, "${file.name}: duplicate quiz options for ${it.word}")
            }
            lesson.listening.forEach { assertTrue(it.correctIndex in it.options.indices, "${file.name}: listening correctIndex out of range") }
            lesson.listening.forEach { task ->
                val explanation = task.explanation ?: error("${file.name}: listening task without explanation")
                task.options.forEach { option -> assertTrue(option in explanation, "${file.name}: explanation does not mention '$option'") }
            }
            // Задание на слух без записи не попадёт в урок (LessonPlanner его пропускает), но если путь указан, файл должен существовать.
            (lesson.listening.mapNotNull { it.audioUrl } + lesson.quiz.mapNotNull { it.audioUrl } + lesson.translations.mapNotNull { it.audioUrl } +
                lesson.matching.pairs.mapNotNull { it.audioUrl } + lesson.sentences.flatMap { it.wordAudio.values + listOfNotNull(it.audioUrl) })
                .forEach { assertFile(file.name, it) }
            // Озвучка слов банка должна относиться к словам этого банка.
            lesson.sentences.forEach { task ->
                task.wordAudio.keys.forEach { assertTrue(it in task.wordBank, "${file.name}: wordAudio word '$it' is not in the word bank") }
            }
            lesson.sentences.forEach { task ->
                val bank = task.wordBank.toMutableList()
                task.answer.forEach { word -> assertTrue(bank.remove(word), "${file.name}: answer word '$word' is not in the word bank") }
            }
            lesson.translations.forEach { assertTrue(it.acceptedAnswers.isNotEmpty(), "${file.name}: translation without answers") }
        }
    }
}
