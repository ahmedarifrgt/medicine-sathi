package com.example.data.model

object AuthorInfo {
    const val NAME = "MD ARIF"
    const val TITLE = "Computer Science & Engineering Student | Aspiring Software Engineer"
    const val LOCATION = "Textile, Green Valley Housing Society, Chattogram"
    const val PHONE = "01879524393"
    const val EMAIL = "ahmedarif.rgt@gmail.com"
    const val LINKEDIN_URL = "https://www.linkedin.com/in/ahmedarif-rgt"
    const val LINKEDIN_DISPLAY = "linkedin.com/ahmedarif.rgt"
    const val GITHUB_URL = "https://github.com/ahmedarifrgt"
    const val GITHUB_DISPLAY = "github.com/ahmedarifrgt"

    const val ABOUT_ME = "I am a passionate and motivated Computer Science and Engineering student at Southern University Bangladesh, with a strong interest in software development, web development, mobile application development, and emerging technologies.\n\n" +
            "I have hands-on experience with Python, Flutter, FastAPI, Java, MySQL, Git, and SQLite, and I enjoy developing practical software solutions that are reliable, user-friendly, and professionally designed.\n\n" +
            "I have worked on projects including a Gym Management System, Secure Electronic Voting System, University Chatbot System, and business management applications.\n\n" +
            "I am continuously developing my programming, problem-solving, and software engineering skills through academic projects, personal development, and practical implementation. I am particularly interested in Artificial Intelligence, Machine Learning, secure software systems, full-stack development, and mobile application development.\n\n" +
            "My goal is to build a successful career as a Software Engineer by contributing to meaningful technology projects, continuously learning modern technologies, and developing solutions that provide real-world value."

    val EDUCATION = listOf(
        EducationItem(
            degree = "B.Sc. in Computer Science & Engineering",
            institution = "Southern University Bangladesh",
            graduationYear = "2026",
            grade = "CGPA: 3.3"
        ),
        EducationItem(
            degree = "Higher Secondary Certificate (HSC) – Science",
            institution = "Langadu Government Degree College",
            graduationYear = "",
            grade = "GPA: 4.86"
        )
    )

    val TECHNICAL_SKILLS = listOf(
        SkillCategory("Programming", listOf("Python", "Java", "Dart", "Kotlin")),
        SkillCategory("Mobile Development", listOf("Flutter", "Android Jetpack Compose")),
        SkillCategory("Backend Development", listOf("FastAPI", "RESTful APIs")),
        SkillCategory("Databases", listOf("MySQL", "SQLite", "Room DB")),
        SkillCategory("Version Control & Tools", listOf("Git", "GitHub", "Android Studio"))
    )

    val AREAS_OF_INTEREST = listOf(
        "Software Development",
        "Web Development",
        "Full-Stack Development",
        "Mobile App Development",
        "AI & Machine Learning",
        "Secure Software Systems"
    )
}

data class EducationItem(
    val degree: String,
    val institution: String,
    val graduationYear: String,
    val grade: String
)

data class SkillCategory(
    val category: String,
    val skills: List<String>
)
