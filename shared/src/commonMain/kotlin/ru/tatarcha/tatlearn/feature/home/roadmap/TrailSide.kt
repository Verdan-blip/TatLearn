package ru.tatarcha.tatlearn.feature.home.roadmap

/** Где тропа входит в ряд ленты или выходит из него: над левой плиткой, над правой или по центру. */
enum class TrailSide(val fraction: Float) {
    Left(0.25f),
    Center(0.5f),
    Right(0.75f),
}
