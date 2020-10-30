import java.io.File
import java.util.*
import kotlin.system.exitProcess

object CaesarCipher {

    private const val DICTIONARY_PATH = "/usr/share/dict/words"

    private var englishWords = hashSetOf<String>()
    private val options: Array<Option> = arrayOf(
            Option("Encrypt", this::encrypt),
            Option("Decrypt", this::decrypt),
            Option("Exit", this::exit),
    )

    @JvmStatic
    fun main(args: Array<String>) {
        loadDictionary()
        while(true) {
            printMainMenu()
        }
    }

    private fun printMainMenu() {
        println()
        println("What do you want to do?")
        options.print()
        options.acceptChoice()
    }

    private fun loadDictionary() {
        try {
            val scanner = Scanner(File(DICTIONARY_PATH))
            while(scanner.hasNextLine()) {
                englishWords.add(scanner.nextLine())
            }
        } catch (e: Exception) {}
    }

    private fun autoDetect(text: String) {
        val numberOfWords = text.split(" ").size
        val rank = rankInEnglish(text).toDouble()
        val percentageInEnglish = rank / numberOfWords
        if (percentageInEnglish > 0.25) {
            encrypt(text)
        } else {
            decrypt(text)
        }
    }

    private fun encrypt(inputText: String? = null) {
        var text = inputText
        if (text == null) {
            println()
            print("Encrypt: ")
            text = readLine() ?: ""
        }
        if (text.containsLetters()) {
            val key = Random().nextInt(24) + 1
            val encryptedText = encryptText(text, key)
            println(encryptedText)
        }
    }

    private fun decrypt(inputText: String? = null) {
        var text = inputText
        if (text == null) {
            println()
            print("Decrypt: ")
            text = readLine() ?: ""
        }
        if (text.containsLetters()) {
            if (englishWords.isNotEmpty()) {
                val key = autoDetectDecryptionKey(text)
                if (key < 0) {
                    println("Could not autodetect key.")
                    decryptAll(text)
                } else {
                    val decryptedText = decryptText(text, key)
                    println(decryptedText)
                }
            } else {
                println("Dictionary file not found.")
                decryptAll(text)
            }
        }
    }

    private fun decryptAll(text: String) {
        println("Printing all possibilities for human analysis:")
        (1..25).forEach { println("$it. ${decryptText(text, it)}") }
    }

    private fun autoDetectDecryptionKey(text: String): Int {
        val rankings = (0..25).map { 0 }.toMutableList()
        (0..25).forEach { rankings[it] = rankInEnglish(decryptText(text, it)) }
        var bestKey = -1 to 0
        rankings.forEachIndexed { i, rank ->
            if (rank > bestKey.second) {
                bestKey = i to rank
            }
        }
        return bestKey.first
    }

    private fun rankInEnglish(text: String): Int {
        var rank = 0
        val words = text.split(" ")
        words.forEach { word ->
            if (englishWords.contains(word.toLowerCase())) rank++
        }
        return rank
    }

    private fun encryptText(text: String, key: Int): String {
        return text.mapChars(Char::isLetter) { c ->
            (c + key).ensureAlpha(c.isUpperCase())
        }
    }

    private fun decryptText(text: String, key: Int): String {
        return text.mapChars(Char::isLetter) { c ->
            (c - key).ensureAlpha(c.isUpperCase())
        }
    }

    private fun exit() {
        exitProcess(0)
    }

    private fun Array<Option>.print() = forEachIndexed { i, option -> println("${i+1}. ${option.title}") }

    private fun Array<Option>.acceptChoice() {
        println()
        val text = readLine() ?: ""
        val choice = try { Integer.parseInt(text) } catch (e: NumberFormatException) { 0 }
        if (choice > 0 && choice < options.size + 1) {
            get(choice-1).function.invoke()
        } else {
            autoDetect(text)
        }
    }

    private fun String.containsLetters() = contains("[a-zA-Z]".toRegex())

    private fun Char.ensureAlpha(wasUpperCase: Boolean): Char {
        return if (wasUpperCase) {
            this.ensureBetween('A','Z')
        } else {
            this.ensureBetween('a','z')
        }
    }

    private fun Char.ensureBetween(lower: Char, upper: Char): Char {
        val difference = upper - lower + 1
        return when {
            this < lower -> this + difference
            this > upper -> this - difference
            else -> this
        }
    }

    private fun String.mapChars(predicate: (Char) -> Boolean, operation: (Char) -> Char): String {
        return String(toCharArray().map {
            if (predicate.invoke(it)) operation.invoke(it) else it
        }.toCharArray())
    }

    data class Option(val title: String, val function: () -> Unit)
}