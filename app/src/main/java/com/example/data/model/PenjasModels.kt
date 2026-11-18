package com.example.data.model

data class JointMotion(
    val fromDeg: Float,
    val toDeg: Float,
    val linearLoop: Boolean = false,
    val oppositePhase: Boolean = false
)

data class ExerciseAnimationSpec(
    val durationSec: Float,
    val leftArm: JointMotion = JointMotion(8f, 8f),
    val rightArm: JointMotion = JointMotion(-8f, -8f),
    val leftLeg: JointMotion = JointMotion(5f, 5f),
    val rightLeg: JointMotion = JointMotion(-5f, -5f),
    val head: JointMotion = JointMotion(0f, 0f),
    val upperBody: JointMotion = JointMotion(0f, 0f),
    val figureOffsetYFrom: Float = 0f,
    val figureOffsetYTo: Float = 0f
)

data class PenjasExercise(
    val id: Int,
    val category: String,
    val emoji: String,
    val name: String,
    val durationSeconds: Int,
    val description: String,
    val steps: List<String>,
    val benefits: String,
    val musclesWorked: String,
    val targetReps: String,
    val tips: String,
    val avoidTip: String,
    val animationSpec: ExerciseAnimationSpec
)

data class PenjasQuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val categoryTag: String = "Kebugaran",
    val fitnessTip: String = ""
)

data class WeeklyQuizTheme(
    val weekIndex: Int,
    val weekLabel: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val educationSummary: List<String>,
    val questions: List<PenjasQuizQuestion>
)

data class RoutePoint(
    val xFraction: Float,
    val yFraction: Float
)

object PenjasCatalog {
    val exercises: List<PenjasExercise> = listOf(
        PenjasExercise(
            id = 0,
            category = "Pemanasan",
            emoji = "🙆",
            name = "Tekuk Kepala",
            durationSeconds = 20,
            description = "Gerakkan kepala perlahan ke kiri dan ke kanan untuk melemaskan otot leher.",
            steps = listOf(
                "Berdiri tegak, kaki selebar bahu.",
                "Miringkan kepala ke kiri, tahan sebentar.",
                "Miringkan ke kanan. Bahu tetap rileks dan napas teratur."
            ),
            benefits = "Melemaskan otot leher dan mengurangi rasa kaku.",
            musclesWorked = "Otot leher (trapezius atas)",
            targetReps = "5 kali tiap sisi",
            tips = "Gerakan pelan, tahan 2 hitungan di tiap sisi.",
            avoidTip = "Memutar kepala penuh dengan cepat.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 1.2f,
                head = JointMotion(-28f, 28f)
            )
        ),
        PenjasExercise(
            id = 1,
            category = "Pemanasan",
            emoji = "💫",
            name = "Putar Lengan",
            durationSeconds = 30,
            description = "Putar kedua lengan membentuk lingkaran besar untuk menyiapkan sendi bahu.",
            steps = listOf(
                "Rentangkan lengan ke samping setinggi bahu.",
                "Putar ke depan membentuk lingkaran besar.",
                "Setelah 15 detik, ganti arah putaran ke belakang."
            ),
            benefits = "Melenturkan sendi bahu dan melancarkan aliran darah ke lengan.",
            musclesWorked = "Bahu (deltoid) dan punggung atas",
            targetReps = "10 putaran ke depan, 10 ke belakang",
            tips = "Mulai dari lingkaran kecil, lalu makin besar.",
            avoidTip = "Mengangkat bahu hingga ke telinga.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 2.4f,
                leftArm = JointMotion(90f, 450f, linearLoop = true),
                rightArm = JointMotion(-90f, -450f, linearLoop = true)
            )
        ),
        PenjasExercise(
            id = 2,
            category = "Pemanasan",
            emoji = "🤸",
            name = "Peregangan Samping",
            durationSeconds = 30,
            description = "Angkat lengan ke atas lalu condongkan badan ke samping untuk meregangkan pinggang.",
            steps = listOf(
                "Angkat kedua lengan lurus ke atas.",
                "Condongkan badan ke kiri, tahan dua hitungan.",
                "Kembali tegak, lalu condong ke kanan."
            ),
            benefits = "Meregangkan otot pinggang dan sisi badan.",
            musclesWorked = "Otot samping pinggang dan punggung",
            targetReps = "3 kali tiap sisi, tahan 2–3 detik",
            tips = "Dorong lengan ke atas, dada tetap terbuka.",
            avoidTip = "Membungkuk ke depan atau ke belakang.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 1.2f,
                leftArm = JointMotion(165f, 165f),
                rightArm = JointMotion(-165f, -165f),
                upperBody = JointMotion(-18f, 18f)
            )
        ),
        PenjasExercise(
            id = 3,
            category = "Pemanasan",
            emoji = "🌀",
            name = "Putar Pinggang",
            durationSeconds = 30,
            description = "Letakkan tangan di pinggang, lalu goyangkan badan atas ke kiri dan ke kanan.",
            steps = listOf(
                "Letakkan kedua tangan di pinggang.",
                "Kaki selebar bahu dan lutut sedikit ditekuk.",
                "Putar pinggang pelan-pelan, ikuti irama."
            ),
            benefits = "Menyiapkan otot perut dan punggung bawah.",
            musclesWorked = "Otot perut, pinggang, punggung bawah",
            targetReps = "10 putaran tiap arah",
            tips = "Lutut sedikit ditekuk, gerak lembut.",
            avoidTip = "Memutar terlalu cepat atau terlalu keras.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 1.2f,
                leftArm = JointMotion(60f, 60f),
                rightArm = JointMotion(-60f, -60f),
                upperBody = JointMotion(-12f, 12f)
            )
        ),
        PenjasExercise(
            id = 4,
            category = "Olahraga pagi",
            emoji = "🏃",
            name = "Lari di Tempat",
            durationSeconds = 30,
            description = "Berlari kecil di tempat dengan ayunan lengan untuk menaikkan detak jantung.",
            steps = listOf(
                "Angkat kaki kiri dan kanan bergantian.",
                "Ayunkan lengan berlawanan dengan kaki.",
                "Mendarat ringan di ujung kaki."
            ),
            benefits = "Melatih daya tahan jantung dan paru-paru.",
            musclesWorked = "Paha, betis, jantung, dan paru-paru",
            targetReps = "30 detik",
            tips = "Mendarat di ujung kaki, napas teratur.",
            avoidTip = "Mendarat dengan tumit secara keras.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 0.35f,
                leftLeg = JointMotion(0f, 38f),
                rightLeg = JointMotion(0f, -38f, oppositePhase = true),
                leftArm = JointMotion(20f, 60f),
                rightArm = JointMotion(-20f, -60f, oppositePhase = true),
                figureOffsetYFrom = 0f,
                figureOffsetYTo = -6f
            )
        ),
        PenjasExercise(
            id = 5,
            category = "Olahraga pagi",
            emoji = "⭐",
            name = "Jumping Jack",
            durationSeconds = 30,
            description = "Lompat sambil membuka kaki dan mengangkat lengan ke atas, lalu kembali merapat.",
            steps = listOf(
                "Mulai berdiri tegak, lengan di samping badan.",
                "Lompat, buka kaki dan angkat lengan ke atas.",
                "Lompat lagi untuk kembali ke posisi awal."
            ),
            benefits = "Melatih kekuatan kaki, koordinasi, dan daya tahan.",
            musclesWorked = "Seluruh badan: bahu, paha, betis, jantung",
            targetReps = "20–30 kali",
            tips = "Mendarat dengan lutut sedikit lentur.",
            avoidTip = "Mendarat dengan kaki lurus dan kaku.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 0.6f,
                leftArm = JointMotion(8f, 165f),
                rightArm = JointMotion(-8f, -165f),
                leftLeg = JointMotion(4f, 26f),
                rightLeg = JointMotion(-4f, -26f),
                figureOffsetYFrom = 0f,
                figureOffsetYTo = -10f
            )
        ),
        PenjasExercise(
            id = 6,
            category = "Olahraga pagi",
            emoji = "🦵",
            name = "Squat",
            durationSeconds = 30,
            description = "Turunkan badan seperti akan duduk, punggung tetap tegak, lalu berdiri kembali.",
            steps = listOf(
                "Kaki selebar bahu, punggung tegak.",
                "Tekuk lutut dan turunkan pinggul perlahan.",
                "Dorong dengan tumit untuk berdiri kembali."
            ),
            benefits = "Menguatkan otot paha, betis, dan pantat.",
            musclesWorked = "Paha depan dan belakang, pantat, perut",
            targetReps = "10–15 kali",
            tips = "Berat badan di tumit, pandangan ke depan.",
            avoidTip = "Lutut menekuk ke dalam atau punggung membungkuk.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 1.2f,
                leftArm = JointMotion(20f, 85f),
                rightArm = JointMotion(-20f, -85f),
                leftLeg = JointMotion(5f, 50f),
                rightLeg = JointMotion(-5f, -50f),
                figureOffsetYFrom = 0f,
                figureOffsetYTo = 23f
            )
        ),
        PenjasExercise(
            id = 7,
            category = "Olahraga pagi",
            emoji = "🐇",
            name = "Lompat Kecil",
            durationSeconds = 30,
            description = "Lompat-lompat kecil dengan lutut lentur untuk melatih keseimbangan dan kelincahan.",
            steps = listOf(
                "Berdiri dengan lutut sedikit ditekuk.",
                "Lompat kecil, mendarat dengan ujung kaki.",
                "Ayunkan lengan santai mengikuti irama."
            ),
            benefits = "Melatih kelincahan dan membuat badan terasa segar.",
            musclesWorked = "Betis, paha, dan keseimbangan",
            targetReps = "30 detik",
            tips = "Lompat rendah, mendarat pelan.",
            avoidTip = "Melompat terlalu tinggi sebelum pemanasan.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 0.6f,
                leftArm = JointMotion(10f, 40f),
                rightArm = JointMotion(-10f, -40f),
                figureOffsetYFrom = 0f,
                figureOffsetYTo = -14f
            )
        )
    )

    val runningStretches: List<PenjasExercise> = listOf(
        PenjasExercise(
            id = 8,
            category = "Peregangan Pelari",
            emoji = "🦵",
            name = "Ayunan Kaki",
            durationSeconds = 30,
            description = "Ayunkan satu kaki ke depan dan ke belakang secara ritmis untuk melumasi sendi pinggul.",
            steps = listOf(
                "Berdiri tegak di samping dinding atau pegangan untuk keseimbangan.",
                "Ayunkan kaki kanan ke depan dan ke belakang dengan lutut rileks.",
                "Setelah 15 detik, ganti ke kaki kiri dengan napas teratur."
            ),
            benefits = "Membuka fleksor pinggul dan menyiapkan hamstring sebelum langkah lari.",
            musclesWorked = "Fleksor pinggul, paha depan, dan hamstring",
            targetReps = "10 ayunan per kaki",
            tips = "Jaga tubuh atas tetap tegak, tidak membungkuk saat kaki mengayun ke belakang.",
            avoidTip = "Mengayun terlalu keras hingga punggung bawah melengkung berlebihan.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 0.8f,
                leftLeg = JointMotion(-32f, 38f),
                rightLeg = JointMotion(4f, 4f),
                leftArm = JointMotion(15f, -15f),
                rightArm = JointMotion(-15f, 15f)
            )
        ),
        PenjasExercise(
            id = 9,
            category = "Peregangan Pelari",
            emoji = "🦶",
            name = "Peregangan Betis",
            durationSeconds = 30,
            description = "Dorong tumit kaki belakang ke lantai untuk meregangkan otot betis dan tendon Achilles.",
            steps = listOf(
                "Melangkah satu kaki ke depan dan tekuk lutut depan perlahan.",
                "Luruskan kaki belakang sambil menekan tumitnya rata ke lantai.",
                "Tahan 15 detik lalu tukar posisi kaki kiri dan kanan."
            ),
            benefits = "Mencegah kram betis, nyeri tulang kering, dan ketegangan tendon Achilles.",
            musclesWorked = "Otot betis (gastrocnemius, soleus) dan tendon Achilles",
            targetReps = "15 detik tiap sisi",
            tips = "Pastikan ujung jari kaki belakang menghadap lurus ke depan.",
            avoidTip = "Memantulkan tumit naik-turun secara menyentak.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 1.4f,
                leftLeg = JointMotion(18f, 26f),
                rightLeg = JointMotion(-22f, -30f),
                leftArm = JointMotion(30f, 42f),
                rightArm = JointMotion(-20f, -32f),
                upperBody = JointMotion(6f, 10f)
            )
        ),
        PenjasExercise(
            id = 10,
            category = "Peregangan Pelari",
            emoji = "🧘",
            name = "Lunge Buka Pinggul",
            durationSeconds = 30,
            description = "Posisi lunge rendah dengan lengan terangkat untuk melepas ketegangan paha dan pinggul.",
            steps = listOf(
                "Langkahkan kaki kanan lebar ke depan hingga lutut membentuk sudut 90 derajat.",
                "Turunkan pinggul perlahan dan angkat kedua lengan ke atas.",
                "Tahan 15 detik per sisi sambil menarik napas dalam."
            ),
            benefits = "Memulihkan otot psoas dan paha depan setelah jarak lari menengah atau jauh.",
            musclesWorked = "Fleksor pinggul (psoas), paha depan, dan bokong",
            targetReps = "2 kali per sisi",
            tips = "Pastikan lutut depan sejajar di atas pergelangan kaki, tidak melebihi ujung jari.",
            avoidTip = "Membiarkan pinggul miring ke satu sisi.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 1.5f,
                leftArm = JointMotion(140f, 165f),
                rightArm = JointMotion(-140f, -165f),
                leftLeg = JointMotion(28f, 36f),
                rightLeg = JointMotion(-28f, -36f),
                figureOffsetYFrom = 6f,
                figureOffsetYTo = 16f
            )
        ),
        PenjasExercise(
            id = 11,
            category = "Peregangan Pelari",
            emoji = "🌾",
            name = "Peregangan Hamstring",
            durationSeconds = 30,
            description = "Condongkan badan dari pinggul ke arah ujung kaki depan secara bergantian dan terkontrol.",
            steps = listOf(
                "Luruskan kaki kanan sedikit ke depan dengan tumit menempel lantai.",
                "Tekuk lutut kiri sedikit lalu condongkan pinggul ke belakang sambil meraih arah tulang kering.",
                "Kembali tegak dan bergantian dengan sisi kiri selama 30 detik."
            ),
            benefits = "Melenturkan otot paha belakang dan meredakan kaku punggung bawah.",
            musclesWorked = "Hamstring (paha belakang) dan punggung bawah",
            targetReps = "6–8 kali tiap kaki",
            tips = "Jaga punggung tetap rata saat mencondongkan badan dari lipatan pinggul.",
            avoidTip = "Membulatkan punggung bawah atau mengunci lutut terlalu keras.",
            animationSpec = ExerciseAnimationSpec(
                durationSec = 1.3f,
                leftArm = JointMotion(25f, 110f),
                rightArm = JointMotion(-25f, -110f),
                leftLeg = JointMotion(12f, 20f),
                rightLeg = JointMotion(-6f, -10f),
                upperBody = JointMotion(0f, 22f)
            )
        )
    )

    val allExercises: List<PenjasExercise> = exercises + runningStretches

    fun findExercise(id: Int): PenjasExercise {
        return allExercises.firstOrNull { it.id == id } ?: exercises.first()
    }

    val quizQuestions: List<PenjasQuizQuestion> = listOf(
        PenjasQuizQuestion(
            question = "Apa tujuan utama pemanasan sebelum berolahraga?",
            options = listOf(
                "Membuat badan cepat lelah",
                "Menyiapkan otot dan sendi agar tidak cedera",
                "Mengurangi berat badan",
                "Menghibur teman"
            ),
            correctIndex = 1,
            explanation = "Pemanasan menaikkan suhu otot dan melenturkan sendi sehingga risiko cedera berkurang."
        ),
        PenjasQuizQuestion(
            question = "Berapa lama pemanasan yang ideal?",
            options = listOf("5–10 menit", "30 detik", "1 jam", "Tidak perlu"),
            correctIndex = 0,
            explanation = "Pemanasan 5–10 menit sudah cukup untuk menyiapkan tubuh."
        ),
        PenjasQuizQuestion(
            question = "Pemanasan sebaiknya dimulai dengan gerakan …",
            options = listOf(
                "Paling berat",
                "Melompat setinggi mungkin",
                "Ringan dan pelan, lalu makin cepat",
                "Diam saja"
            ),
            correctIndex = 2,
            explanation = "Mulai dari gerakan ringan lalu tingkatkan bertahap."
        ),
        PenjasQuizQuestion(
            question = "Gerakan putar lengan terutama melatih …",
            options = listOf("Sendi bahu", "Jari kaki", "Tulang rusuk", "Mata"),
            correctIndex = 0,
            explanation = "Putar lengan melenturkan sendi bahu dan otot sekitarnya."
        ),
        PenjasQuizQuestion(
            question = "Sebelum olahraga pagi, sebaiknya kita …",
            options = listOf(
                "Menahan haus",
                "Minum air putih secukupnya",
                "Minum es manis banyak-banyak",
                "Tidur lagi"
            ),
            correctIndex = 1,
            explanation = "Tubuh perlu cukup cairan agar tidak mudah lelah dan pusing."
        ),
        PenjasQuizQuestion(
            question = "Jumping jack melatih …",
            options = listOf(
                "Hanya otot jari",
                "Kekuatan kaki, koordinasi, dan daya tahan",
                "Hanya pendengaran",
                "Tidak ada manfaat"
            ),
            correctIndex = 1,
            explanation = "Gerakan ini melibatkan lengan dan kaki sekaligus sehingga jantung ikut terlatih."
        ),
        PenjasQuizQuestion(
            question = "Saat melakukan squat, posisi punggung yang benar adalah …",
            options = listOf(
                "Membungkuk dalam",
                "Tegak dan dada terbuka",
                "Menoleh ke belakang",
                "Berputar"
            ),
            correctIndex = 1,
            explanation = "Punggung tegak melindungi tulang belakang dan membuat gerakan lebih efektif."
        ),
        PenjasQuizQuestion(
            question = "Peregangan sebaiknya ditahan selama sekitar …",
            options = listOf("1 detik", "15–30 detik", "5 menit", "Tidak perlu ditahan"),
            correctIndex = 1,
            explanation = "Menahan 15–30 detik memberi waktu otot untuk memanjang."
        ),
        PenjasQuizQuestion(
            question = "Salah satu manfaat olahraga pagi adalah …",
            options = listOf(
                "Badan lebih segar dan siap belajar",
                "Mengantuk sepanjang hari",
                "Lupa pelajaran",
                "Nafsu makan hilang"
            ),
            correctIndex = 0,
            explanation = "Olahraga pagi membantu peredaran darah dan konsentrasi belajar."
        ),
        PenjasQuizQuestion(
            question = "Jika terasa pusing atau nyeri saat berolahraga, harus …",
            options = listOf(
                "Terus memaksa",
                "Berhenti dan memberi tahu guru",
                "Berlari lebih cepat",
                "Menahan sendiri"
            ),
            correctIndex = 1,
            explanation = "Keselamatan nomor satu: berhenti, istirahat, dan minta bantuan."
        )
    )
}

object WeeklyQuizCatalog {
    val weeks: List<WeeklyQuizTheme> = listOf(
        WeeklyQuizTheme(
            weekIndex = 1,
            weekLabel = "Minggu 1",
            emoji = "🥗",
            title = "Nutrisi, Hidrasi & Energi Lari",
            subtitle = "Pola makan & cairan optimal untuk pelari aktif",
            educationSummary = listOf(
                "Minum 250–400 ml air putih 1–2 jam sebelum olahraga untuk mencegah dehidrasi dini.",
                "Karbohidrat kompleks (pisang, oatmeal, nasi merah) menjadi bahan bakar utama otot.",
                "Konsumsi protein dalam 30–60 menit setelah latihan membantu memperbaiki serabut otot."
            ),
            questions = listOf(
                PenjasQuizQuestion(
                    question = "Sumber energi utama yang cepat dipakai tubuh saat berlari pagi adalah …",
                    options = listOf("Karbohidrat", "Lemak jenuh tinggi", "Garam berlebih", "Kafein pekat"),
                    correctIndex = 0,
                    explanation = "Karbohidrat diolah menjadi glikogen otot sebagai bahan bakar utama aktivitas kardio.",
                    categoryTag = "Nutrisi Olahraga",
                    fitnessTip = "Pilih karbohidrat ringan seperti pisang atau roti gandum 45 menit sebelum lari."
                ),
                PenjasQuizQuestion(
                    question = "Tanda awal tubuh mulai kekurangan cairan (dehidrasi ringan) saat berolahraga adalah …",
                    options = listOf("Urine bening jernih", "Bibir kering, pusing, dan cepat lelah", "Detak jantung stabil", "Napas terasa sangat lega"),
                    correctIndex = 1,
                    explanation = "Dehidrasi membuat volume darah sedikit menurun sehingga tubuh lebih cepat lelah dan pusing.",
                    categoryTag = "Hidrasi",
                    fitnessTip = "Jangan menunggu sangat haus; teguk air secukupnya sebelum dan sesudah lari."
                ),
                PenjasQuizQuestion(
                    question = "Zat gizi yang paling berperan memperbaiki dan membangun jaringan otot setelah latihan adalah …",
                    options = listOf("Protein", "Gula rafinasi", "Soda berkarbonasi", "Minyak goreng"),
                    correctIndex = 0,
                    explanation = "Asam amino dari protein membantu pemulihan mikrorobekan otot setelah sesi lari.",
                    categoryTag = "Pemulihan Otot",
                    fitnessTip = "Telur, tempe, tahu, dada ayam, atau susu rendah lemak cocok dikonsumsi usai berolahraga."
                ),
                PenjasQuizQuestion(
                    question = "Saat berkeringat cukup banyak lebih dari 45 menit, tubuh juga kehilangan mineral penting yaitu …",
                    options = listOf("Elektrolit (natrium & kalium)", "Kalsium tulang seketika", "Kolesterol", "Serat kasar"),
                    correctIndex = 0,
                    explanation = "Keringat membawa natrium dan kalium yang berperan menjaga kontraksi otot bebas kram.",
                    categoryTag = "Elektrolit",
                    fitnessTip = "Pisang atau air kelapa alami merupakan sumber kalium alami yang ramah lambung."
                ),
                PenjasQuizQuestion(
                    question = "Jarak waktu ideal makan berat sebelum memulai lari intensitas sedang adalah …",
                    options = listOf("Langsung setelah makan", "2–3 jam sebelumnya", "12 jam sebelumnya", "Sambil berlari"),
                    correctIndex = 1,
                    explanation = "Memberi jeda 2–3 jam memberi kesempatan lambung mencerna agar tidak kram perut.",
                    categoryTag = "Kebiasaan Sehat",
                    fitnessTip = "Jika hanya punya waktu 30 menit, cukup konsumsi camilan ringan seperti sepotong buah."
                ),
                PenjasQuizQuestion(
                    question = "Minuman terbaik untuk menggantikan cairan setelah olahraga pagi berdurasi 20–30 menit adalah …",
                    options = listOf("Air putih bersih", "Minuman energi tinggi gula", "Kopi krim manis", "Minuman bersoda"),
                    correctIndex = 0,
                    explanation = "Untuk durasi di bawah 45 menit, air putih sudah paling efektif tanpa tambahan kalori gula.",
                    categoryTag = "Hidrasi",
                    fitnessTip = "Bawa botol minum pribadi agar asupan cairan harian 2 liter lebih mudah tercapai."
                )
            )
        ),
        WeeklyQuizTheme(
            weekIndex = 2,
            weekLabel = "Minggu 2",
            emoji = "❤️",
            title = "Kesehatan Jantung & Zona Latihan",
            subtitle = "Memahami BPM, napas, dan daya tahan kardiovaskular",
            educationSummary = listOf(
                "Zona Aerobik (sekitar 70–80% detak jantung maksimal) optimal untuk melatih daya tahan jantung.",
                "Lakukan pendinginan (jalan santai 3–5 menit) setelah lari agar detak jantung turun bertahap.",
                "Detak jantung istirahat yang lebih rendah umumnya menandakan jantung yang semakin bugar."
            ),
            questions = listOf(
                PenjasQuizQuestion(
                    question = "Apa singkatan dari satuan detak jantung per menit yang tampil di layar PulseRun?",
                    options = listOf("Beats Per Minute (BPM)", "Body Power Meter", "Breath Per Meter", "Blood Pulse Mass"),
                    correctIndex = 0,
                    explanation = "BPM mengukur berapa kali jantung berkontraksi memompa darah dalam satu menit.",
                    categoryTag = "Kardio",
                    fitnessTip = "Pantau BPM saat istirahat di pagi hari untuk melihat perkembangan kebugaranmu."
                ),
                PenjasQuizQuestion(
                    question = "Zona latihan yang paling efektif membakar lemak sambil tetap nyaman berbicara kalimat pendek adalah …",
                    options = listOf("Zona 2 (Pembakaran Lemak / Ringan–Sedang)", "Zona 5 (Sprint Maksimal)", "Diam tanpa bergerak", "Menahan napas"),
                    correctIndex = 0,
                    explanation = "Pada Zona 2, tubuh memanfaatkan oksigen dan cadangan lemak secara efisien.",
                    categoryTag = "Zona Jantung",
                    fitnessTip = "Gunakan 'talk test': bila masih bisa mengobrol singkat tanpa terengah, kamu di zona aerobik nyaman."
                ),
                PenjasQuizQuestion(
                    question = "Mengapa kita perlu melakukan pendinginan (cool-down) setelah selesai berlari cepat?",
                    options = listOf(
                        "Membantu detak jantung dan aliran darah kembali normal secara bertahap",
                        "Agar badan langsung kedinginan",
                        "Untuk menghabiskan sisa energi secara paksa",
                        "Supaya tidak perlu minum air"
                    ),
                    correctIndex = 0,
                    explanation = "Berhenti mendadak bisa membuat darah mengumpul di kaki dan memicu rasa pusing.",
                    categoryTag = "Keselamatan Kardio",
                    fitnessTip = "Akhiri setiap sesi lari dengan jalan santai 3 menit lalu peregangan ringan."
                ),
                PenjasQuizQuestion(
                    question = "Latihan lari rutin 3–4 kali seminggu memberikan manfaat utama bagi jantung yaitu …",
                    options = listOf(
                        "Otot jantung lebih kuat memompa darah dengan lebih efisien",
                        "Jantung berdetak tidak teratur setiap hari",
                        "Paru-paru menyusut",
                        "Tekanan darah selalu melonjak saat tidur"
                    ),
                    correctIndex = 0,
                    explanation = "Jantung yang terlatih memompa lebih banyak darah tiap detakan sehingga lebih hemat tenaga.",
                    categoryTag = "Kesehatan Jantung",
                    fitnessTip = "Konsistensi 20–30 menit lebih bermanfaat daripada lari ekstrem hanya sebulan sekali."
                ),
                PenjasQuizQuestion(
                    question = "Teknik pernapasan yang dianjurkan saat jogging santai agar tidak cepat sesak adalah …",
                    options = listOf(
                        "Bernapas berirama dalam melalui hidung & mulut secara teratur",
                        "Menahan napas setiap 5 langkah",
                        "Bernapas sangat pendek dan cepat dari dada atas",
                        "Hanya menghembuskan napas sekali per menit"
                    ),
                    correctIndex = 0,
                    explanation = "Pernapasan perut yang dalam dan berirama memasok oksigen lebih stabil ke otot.",
                    categoryTag = "Teknik Napas",
                    fitnessTip = "Coba ritme 2 langkah tarik napas, 2 langkah buang napas saat lari santai."
                ),
                PenjasQuizQuestion(
                    question = "Berapa target minimal aktivitas aerobik intensitas sedang per minggu menurut anjuran kesehatan WHO untuk remaja & dewasa?",
                    options = listOf("150 menit per minggu", "10 menit per bulan", "5 jam setiap hari tanpa libur", "Cukup 5 menit per minggu"),
                    correctIndex = 0,
                    explanation = "150 menit per minggu (misalnya 30 menit × 5 hari) menjaga kesehatan jantung dan metabolisme.",
                    categoryTag = "Standar Kebugaran",
                    fitnessTip = "Gabungkan pemanasan Penjas 10 menit dan lari santai 20 menit setiap hari aktif!"
                )
            )
        ),
        WeeklyQuizTheme(
            weekIndex = 3,
            weekLabel = "Minggu 3",
            emoji = "🦴",
            title = "Pencegahan Cedera & Teknik Gerak",
            subtitle = "Postur aman, sendi kuat, dan penanganan cedera awal",
            educationSummary = listOf(
                "Pemanasan dinamis (seperti Putar Lengan & Lari di Tempat) lebih cocok sebelum olahraga daripada peregangan diam lama.",
                "Mendarat di bagian tengah kaki dengan lutut sedikit lentur mengurangi hentakan ke sendi lutut.",
                "Gunakan prinsip RICE (Rest, Ice, Compression, Elevation) bila mengalami keseleo ringan."
            ),
            questions = listOf(
                PenjasQuizQuestion(
                    question = "Jenis pemanasan yang paling disarankan tepat sebelum mulai berlari adalah …",
                    options = listOf("Pemanasan dinamis dengan gerak aktif", "Duduk diam menekuk kaki 5 menit", "Langsung sprint 100 meter", "Tiduran telentang"),
                    correctIndex = 0,
                    explanation = "Gerak dinamis menaikkan suhu otot dan melumasi sendi sesuai pola gerak lari.",
                    categoryTag = "Pemanasan Aman",
                    fitnessTip = "Gunakan tab Penjas di PulseRun selama 3–4 menit sebelum menekan tombol Mulai Lari."
                ),
                PenjasQuizQuestion(
                    question = "Saat mendaratkan kaki ketika berlari atau jumping jack, posisi lutut yang aman adalah …",
                    options = listOf("Sedikit ditekuk (lentur) untuk meredam benturan", "Terkunci lurus dan kaku", "Ditekuk saling menabrak ke dalam", "Berjinjit sangat tinggi lalu jatuh keras"),
                    correctIndex = 0,
                    explanation = "Lutut yang sedikit lentur bertindak seperti pegas alami yang melindungi tulang rawan sendi.",
                    categoryTag = "Biomekanika",
                    fitnessTip = "Dengarkan langkah kakimu: langkah yang lebih senyap biasanya menandakan pendaratan yang lebih aman."
                ),
                PenjasQuizQuestion(
                    question = "Jika pergelangan kaki terkilir ringan saat berolahraga, langkah pertolongan pertama RICE berarti …",
                    options = listOf(
                        "Rest (Istirahat), Ice (Kompres Es), Compression (Balut), Elevation (Tinggikan)",
                        "Run (Lari terus), Ignore (Abaikan), Climb (Naik tangga), Eat (Makan)",
                        "Pijat keras langsung di bagian bengkak",
                        "Rendam air sangat panas seketika"
                    ),
                    correctIndex = 0,
                    explanation = "RICE membantu mengurangi pembengkakan dan mencegah kerusakan jaringan lebih lanjut pada 24–48 jam pertama.",
                    categoryTag = "Pertolongan Pertama",
                    fitnessTip = "Lapisi es dengan kain handuk saat mengompres selama 15 menit agar kulit tetap aman."
                ),
                PenjasQuizQuestion(
                    question = "Mengapa memilih sepatu olahraga yang pas dan bersol empuk penting bagi pelari?",
                    options = listOf(
                        "Meredam guncangan dan menjaga kestabilan lengkung telapak kaki",
                        "Supaya terlihat mahal saja",
                        "Membuat kaki tidak perlu pemanasan",
                        "Menghilangkan kebutuhan minum air"
                    ),
                    correctIndex = 0,
                    explanation = "Bantalan sepatu mengurangi tekanan berulang pada tumit, tulang kering, dan lutut.",
                    categoryTag = "Perlengkapan",
                    fitnessTip = "Sisakan ruang sekitar setengah jari di ujung sepatu agar jari kaki tidak lecet saat berlari."
                ),
                PenjasQuizQuestion(
                    question = "Posisi tubuh bagian atas yang baik saat berlari santai adalah …",
                    options = listOf(
                        "Tegak rileks, pandangan ke depan, bahu tidak terangkat tegang",
                        "Membungkuk dalam melihat ujung sepatu",
                        "Kepala mendongak ke langit terus",
                        "Kedua tangan mengepal kaku di depan dada"
                    ),
                    correctIndex = 0,
                    explanation = "Postur tegak membuka rongga dada sehingga napas lega dan punggung tidak cepat nyeri.",
                    categoryTag = "Postur Lari",
                    fitnessTip = "Ayunkan lengan santai dari bahu membentuk sudut siku sekitar 90 derajat."
                ),
                PenjasQuizQuestion(
                    question = "Kapan waktu paling tepat melakukan peregangan statis (ditahan 15–30 detik)?",
                    options = listOf("Setelah selesai berolahraga saat otot masih hangat", "Saat baru bangun tidur sebelum berdiri", "Tengah-tengah sprint", "Saat mengalami cedera otot parah"),
                    correctIndex = 0,
                    explanation = "Otot yang hangat lebih elastis sehingga peregangan statis pasca-lari efektif mengembalikan kelenturan.",
                    categoryTag = "Fleksibilitas",
                    fitnessTip = "Regangkan betis, paha depan, dan hamstring selama 20 detik setelah pendinginan."
                )
            )
        ),
        WeeklyQuizTheme(
            weekIndex = 4,
            weekLabel = "Minggu 4",
            emoji = "🌙",
            title = "Tidur, Pemulihan & Kebugaran Mental",
            subtitle = "Menjaga keseimbangan istirahat, stres, dan konsistensi",
            educationSummary = listOf(
                "Tidur berkualitas 7–9 jam per malam adalah waktu utama tubuh melepaskan hormon pemulihan otot.",
                "Olahraga teratur melepaskan endorfin yang membantu meredakan stres dan memperbaiki suasana hati.",
                "Hari istirahat aktif (jalan santai atau peregangan ringan) mencegah kejenuhan dan overtraining."
            ),
            questions = listOf(
                PenjasQuizQuestion(
                    question = "Durasi tidur malam yang dianjurkan bagi remaja dan dewasa aktif untuk pemulihan optimal adalah …",
                    options = listOf("7–9 jam per malam", "3–4 jam saja", "14 jam setiap siang", "Tidak perlu tidur setelah lari"),
                    correctIndex = 0,
                    explanation = "Selama tidur nyenyak, tubuh memperbaiki jaringan otot dan mengisi ulang energi sistem saraf.",
                    categoryTag = "Kualitas Tidur",
                    fitnessTip = "Kurangi menatap layar ponsel 30 menit sebelum tidur agar tidur lebih pulas."
                ),
                PenjasQuizQuestion(
                    question = "Hormon alami tubuh yang meningkat saat berolahraga dan membuat hati terasa lebih gembira adalah …",
                    options = listOf("Endorfin", "Adrenalin berlebih saat panik", "Kortisol kronis", "Asam laktat"),
                    correctIndex = 0,
                    explanation = "Endorfin membantu menurunkan rasa stres, memperbaiki mood, dan membuat pikiran lebih segar.",
                    categoryTag = "Kesehatan Mental",
                    fitnessTip = "Bahkan jalan kaki atau senam pagi 15 menit sudah cukup memicu pelepasan endorfin!"
                ),
                PenjasQuizQuestion(
                    question = "Apa arti 'Istirahat Aktif' (Active Recovery) di antara hari latihan lari yang berat?",
                    options = listOf(
                        "Melakukan aktivitas ringan seperti jalan santai atau peregangan lembut",
                        "Lari maraton lebih cepat lagi",
                        "Berbaring penuh tanpa bergerak 3 hari berturut-turut",
                        "Mengangkat beban maksimal"
                    ),
                    correctIndex = 0,
                    explanation = "Gerak ringan memperlancar aliran darah ke otot yang pegal tanpa menambah beban kelelahan.",
                    categoryTag = "Pemulihan",
                    fitnessTip = "Di hari istirahat aktif, cukup pilih 3 gerakan pemanasan ringan dari daftar Penjas."
                ),
                PenjasQuizQuestion(
                    question = "Tanda tubuh mengalami 'Overtraining' (latihan berlebihan tanpa cukup istirahat) adalah …",
                    options = listOf(
                        "Lelah berkepanjangan, sulit tidur, dan performa lari justru menurun",
                        "Semangat pagi makin meningkat",
                        "Otot terasa bugar dan ringan",
                        "Nafsu makan sehat terjaga"
                    ),
                    correctIndex = 0,
                    explanation = "Otot berkembang saat masa istirahat, bukan hanya saat berlatih keras terus-menerus.",
                    categoryTag = "Manajemen Beban",
                    fitnessTip = "Sisihkan minimal 1–2 hari santai setiap minggu untuk menjaga motivasi jangka panjang."
                ),
                PenjasQuizQuestion(
                    question = "Cara terbaik membangun kebiasaan hidup sehat agar bertahan lama adalah …",
                    options = listOf(
                        "Menetapkan target realistis dan bertahap sesuai kemampuan tubuh",
                        "Memaksakan target 20 km di hari pertama",
                        "Berolahraga hanya saat ingat saja",
                        "Meniru porsi atlet profesional secara langsung"
                    ),
                    correctIndex = 0,
                    explanation = "Target langkah harian yang terukur (misal 6.000–10.000 langkah) jauh lebih mudah dijaga rutin.",
                    categoryTag = "Kebiasaan Positif",
                    fitnessTip = "Atur target langkahmu di menu Profil sesuai aktivitas sekolah atau kerja harianmu."
                ),
                PenjasQuizQuestion(
                    question = "Selain menyehatkan tubuh, rutinitas olahraga pagi sebelum belajar atau bekerja membantu …",
                    options = listOf(
                        "Meningkatkan fokus, daya ingat, dan kestabilan energi sepanjang hari",
                        "Membuat mata selalu mengantuk",
                        "Menurunkan aliran oksigen ke otak",
                        "Menghilangkan kemampuan berkonsentrasi"
                    ),
                    correctIndex = 0,
                    explanation = "Aliran darah yang lancar membawa lebih banyak oksigen ke otak untuk kesiapan berpikir.",
                    categoryTag = "Produktivitas",
                    fitnessTip = "Kombinasikan sesi Pemanasan Pagi + Kuis Mingguan untuk melatih tubuh dan pikiran!"
                )
            )
        )
    )

    fun themeForWeek(weekIndex: Int): WeeklyQuizTheme {
        return weeks.firstOrNull { it.weekIndex == weekIndex } ?: weeks.first()
    }
}
