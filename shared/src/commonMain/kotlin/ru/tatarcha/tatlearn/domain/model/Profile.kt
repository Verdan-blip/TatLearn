package ru.tatarcha.tatlearn.domain.model

/** Позы Мыраужана, доступные как аватар (кадр головы подбирается на экране). */
enum class AvatarPose { Greeting, Cheer, Searching, Sleeping, Correct, Timer }

/** Аватар: цвет с первой буквой имени (индекс из [AvatarColors]) или поза маскота. */
sealed interface Avatar {
    data class Color(val index: Int) : Avatar
    data class Pose(val pose: AvatarPose) : Avatar

    companion object {
        val Default: Avatar = Color(0)
    }
}

/** Сколько цветов аватара есть в палитре (сами цвета берутся из темы на экране). */
object AvatarColors {
    const val COUNT = 4
}

/** Профиль гостя: хранится на устройстве, [id] — локальный идентификатор для будущей привязки аккаунта. */
data class UserProfile(val id: String, val name: String, val avatar: Avatar)

/** Результат проверки имени профиля. */
enum class NameError { Empty, TooLong }

object ProfileRules {
    const val MAX_NAME_LENGTH = 20

    /** С какой длины показывается счётчик «N/20». */
    const val COUNTER_FROM = 15

    /** Имя по умолчанию, если пользователь пропустил знакомство или оставил поле пустым. */
    const val DEFAULT_NAME = "Друг"

    /** Обрезает края и схлопывает повторные пробелы. */
    fun normalize(raw: String): String = raw.trim().replace(Regex("\\s+"), " ")

    fun validate(raw: String): NameError? {
        val name = normalize(raw)
        return when {
            name.isEmpty() -> NameError.Empty
            name.length > MAX_NAME_LENGTH -> NameError.TooLong
            else -> null
        }
    }

    /** Буква для аватара: первая буква имени заглавной (в том числе Ә, Ө, Ү); пусто, если имени нет. */
    fun initial(name: String): String = normalize(name).take(1).uppercase()
}
