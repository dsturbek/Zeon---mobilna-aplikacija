package com.example.zeon.data

import com.example.zeon.R
import com.example.zeon.data.model.Exercise
import com.example.zeon.data.model.Trainer
import com.example.zeon.data.model.User
import com.example.zeon.data.model.Workout
import com.example.zeon.data.model.WorkoutExercise
import java.time.LocalDate
import java.time.LocalTime
import java.util.Date

object MockDataLoader {
    val Users = mutableListOf<User>(
        User(
            id = 1,
            email = "ivan@example.com",
            username = "ivan123",
            password = "123",
            profileImage = "https://example.com/images/ivan.png",
            birthDate = Date(1995, 4, 15),
            height = 180f,
            weight = 75f
        ),
        User(
            id = 2,
            email = "ana@example.com",
            username = "ana456",
            password = "anapass",
            profileImage = "https://example.com/images/ana.png",
            birthDate = Date(1998, 10, 22),
            height = 165f,
            weight = 60f
        )
    )

    fun loadMockTrainers(): List<Trainer> {
        return listOf(
            Trainer(
                id = 1,
                name = "Marko Horvat",
                specialization = "Strength Training",
                experience = 5,
                rating = 4.8,
                price = 150.0,
                imageRes = R.drawable.ic_launcher_foreground
            ),
            Trainer(
                id = 2,
                name = "Ana Kovač",
                specialization = "Yoga & Flexibility",
                experience = 3,
                rating = 4.5,
                price = 120.0,
                imageRes = R.drawable.ic_launcher_foreground
            ),
            Trainer(
                id = 3,
                name = "Sara Sarić",
                specialization = "Cardio & Endurance",
                experience = 7,
                rating = 4.9,
                price = 180.0,
                imageRes = R.drawable.ic_launcher_foreground
            )
        )
    }

    fun loadMockExercises(): MutableList<Exercise> {
        return mutableListOf(
            Exercise(
                id = "ex1",
                name = "Bench Press",
                description = "Leži na klupi, spusti šipku do prsa, zatim potisni ravno gore. Drži laktove pod kutom od 45 stupnjeva.",
                muscleGroup = "Prsa",
                videoUrl = "https://www.youtube.com/watch?v=rT7DgCr-3pg"
            ),
            Exercise(
                id = "ex2",
                name = "Incline Dumbbell Press",
                description = "Na kosoj klupi (30-45°), potiskuj bučice iznad prsa. Kontrolirano spuštaj i eksplozivno guraj gore.",
                muscleGroup = "Prsa",
                videoUrl = "https://www.youtube.com/watch?v=8iPEnn-ltC8"
            ),
            Exercise(
                id = "ex3",
                name = "Cable Fly",
                description = "Stoj između kablova, ruke široko, privuci ruke naprijed i stisni prsa. Kontrolirano vraćaj.",
                muscleGroup = "Prsa",
                videoUrl = "https://www.youtube.com/watch?v=Iwe6AmxVf7o"
            ),

            Exercise(
                id = "ex4",
                name = "Pull-up",
                description = "Visi na šipki, povuci se gore dok brada ne prijeđe šipku. Spuštaj se kontrolirano.",
                muscleGroup = "Leđa",
                videoUrl = "https://www.youtube.com/watch?v=eGo4IYlbE5g"
            ),
            Exercise(
                id = "ex5",
                name = "Barbell Row",
                description = "Nagni se naprijed, šipka ispod koljena. Povuci šipku do trbuha, stisni lopatice.",
                muscleGroup = "Leđa",
                videoUrl = "https://www.youtube.com/watch?v=FWJR5Ve8bnQ"
            ),
            Exercise(
                id = "ex6",
                name = "Lat Pulldown",
                description = "Sjedni na stroj, povuci ručku prema prsima. Fokusiraj se na leđne mišiće, ne ruke.",
                muscleGroup = "Leđa",
                videoUrl = "https://www.youtube.com/watch?v=CAwf7n6Luuc"
            ),

            Exercise(
                id = "ex7",
                name = "Squat",
                description = "Šipka na ramenima, čučni do paralelnog položaja. Koljena prate smjer prstiju.",
                muscleGroup = "Noge",
                videoUrl = "https://www.youtube.com/watch?v=ultWZbUMPL8"
            ),
            Exercise(
                id = "ex8",
                name = "Leg Press",
                description = "Sjedni na stroj, gurai platformu nogama. Koljena ne smiju ići unutra.",
                muscleGroup = "Noge",
                videoUrl = "https://www.youtube.com/watch?v=IZxyjW7MPJQ"
            ),
            Exercise(
                id = "ex9",
                name = "Romanian Deadlift",
                description = "Šipka u rukama, nagne se naprijed s ravnim leđima. Osjetiš naprezanje stražnjice.",
                muscleGroup = "Noge",
                videoUrl = "https://www.youtube.com/watch?v=2SHsk9AzdjA"
            ),

            Exercise(
                id = "ex10",
                name = "Overhead Press",
                description = "Potisni šipku iznad glave iz pozicije ramena.Core napet, ne izbačuj kukove.",
                muscleGroup = "Ramena",
                videoUrl = "https://www.youtube.com/watch?v=2yjwXTZQDDI"
            ),
            Exercise(
                id = "ex11",
                name = "Lateral Raise",
                description = "Bučice uz tijelo, podižeš ih sa strane do razine ramena. Kontrolirano spuštaj.",
                muscleGroup = "Ramena",
                videoUrl = "https://www.youtube.com/watch?v=3VcKaXpzqRo"
            ),

            Exercise(
                id = "ex12",
                name = "Bicep Curl",
                description = "Bučice u rukama, savijaj laktove prema ramenima. Gornji dio ruke nepomičan.",
                muscleGroup = "Ruke",
                videoUrl = "https://www.youtube.com/watch?v=ykJmrZ5v0Oo"
            ),
            Exercise(
                id = "ex13",
                name = "Tricep Dips",
                description = "Ruke na klupi iza sebe, spuštaj tijelo savijanjem lakata. Gurni se natrag gore.",
                muscleGroup = "Ruke",
                videoUrl = "https://www.youtube.com/watch?v=6kALZikXxLc"
            ),
            Exercise(
                id = "ex14",
                name = "Cable Tricep Pushdown",
                description = "Stoj ispred stroja, gurni ručku prema dolje ispružajući ruke. Laktovi uz tijelo.",
                muscleGroup = "Ruke",
                videoUrl = "https://www.youtube.com/watch?v=2-LAMcpzODU"
            ),

            Exercise(
                id = "ex15",
                name = "Plank",
                description = "Na podlakticama, tijelo ravno kao daska. Drži core napetim, ne daj kukovima da padaju.",
                muscleGroup = "Core",
                videoUrl = "https://www.youtube.com/watch?v=ASdvN_XEl_c"
            )
        )
    }

    fun loadMockWorkouts(): MutableList<Workout> {
        val today = LocalDate.now()
        val exercises = loadMockExercises()

        return mutableListOf(
            Workout(
                id = "w1",
                title = "Push",
                date = today,
                time = LocalTime.of(18, 0),
                numberOfExercises = 4,
                muscleGroup = "Prsa/Ramena/Triceps",
                exercises = listOf(
                    WorkoutExercise("we1", "ex1", exercises.find { it.id == "ex1" }!!, 4, 8),
                    WorkoutExercise("we2", "ex2", exercises.find { it.id == "ex2" }!!, 3, 10),
                    WorkoutExercise("we3", "ex11", exercises.find { it.id == "ex11" }!!, 3, 12),
                    WorkoutExercise("we4", "ex14", exercises.find { it.id == "ex14" }!!, 3, 12)
                )
            ),
            Workout(
                id = "w2",
                title = "Legs",
                date = today,
                time = LocalTime.of(21, 0),
                numberOfExercises = 3,
                muscleGroup = "Noge/Glutes",
                exercises = listOf(
                    WorkoutExercise("we5", "ex7", exercises.find { it.id == "ex7" }!!, 4, 8),
                    WorkoutExercise("we6", "ex9", exercises.find { it.id == "ex9" }!!, 3, 10),
                    WorkoutExercise("we7", "ex8", exercises.find { it.id == "ex8" }!!, 3, 12)
                )
            ),
            Workout(
                id = "w3",
                title = "Pull",
                date = today.plusDays(1),
                time = LocalTime.of(14, 0),
                numberOfExercises = 4,
                muscleGroup = "Leđa/Biceps",
                exercises = listOf(
                    WorkoutExercise("we8", "ex4", exercises.find { it.id == "ex4" }!!, 4, 6),
                    WorkoutExercise("we9", "ex5", exercises.find { it.id == "ex5" }!!, 4, 8),
                    WorkoutExercise("we10", "ex6", exercises.find { it.id == "ex6" }!!, 3, 10),
                    WorkoutExercise("we11", "ex12", exercises.find { it.id == "ex12" }!!, 3, 12)
                )
            ),
            Workout(
                id = "w4",
                title = "Full Body",
                date = today.plusDays(3),
                time = LocalTime.of(19, 0),
                numberOfExercises = 5,
                muscleGroup = "Cijelo tijelo",
                exercises = listOf(
                    WorkoutExercise("we12", "ex7", exercises.find { it.id == "ex7" }!!, 3, 10),
                    WorkoutExercise("we13", "ex1", exercises.find { it.id == "ex1" }!!, 3, 10),
                    WorkoutExercise("we14", "ex5", exercises.find { it.id == "ex5" }!!, 3, 10),
                    WorkoutExercise("we15", "ex10", exercises.find { it.id == "ex10" }!!, 3, 8),
                    WorkoutExercise("we16", "ex15", exercises.find { it.id == "ex15" }!!, 3, 30)
                )
            )
        )
    }
}