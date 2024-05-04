package com.catchyapps.whatsdelete.basicapputils

object AppTextToEmojiHelper {

    fun textToEmojiConverter(input: String, emoji: String): String {
        return buildString {
            for (char in input) {
                val pattern = when (char.uppercaseChar()) {
                    in 'A'..'Z' -> when (char) {
                        'A' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$emoji\n$emoji  $emoji\n$emoji  $emoji"
                        'B' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$\n$emoji  $emoji\n$emoji$emoji$emoji"
                        'C' -> "$emoji$emoji$emoji\n$emoji\n$emoji\n$emoji\n$emoji$emoji$emoji"
                        'D' -> "$emoji$emoji$\n$emoji  $emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji$emoji$"
                        'E' -> "$emoji$emoji$emoji\n$emoji\n$emoji$emoji\n$emoji\n$emoji$emoji$emoji"
                        'F' -> "$emoji$emoji$emoji\n$emoji\n$emoji$emoji\n$emoji\n$emoji"
                        'G' -> "$emoji$emoji$emoji\n$emoji\n$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$emoji"
                        'H' -> "$emoji  $emoji\n$emoji  $emoji\n$emoji$emoji$emoji\n$emoji  $emoji\n$emoji  $emoji"
                        'I' -> "$emoji$emoji$emoji\n  $emoji\n  $emoji\n  $emoji\n$emoji$emoji$emoji"
                        'J' -> "$emoji$emoji$emoji\n    $emoji\n    $emoji\n$emoji  $emoji\n$emoji$emoji "
                        'K' -> "$emoji  $emoji\n$emoji $emoji \n$emoji$emoji  \n$emoji $emoji \n$emoji  $emoji"
                        'L' -> "$emoji\n$emoji\n$emoji\n$emoji\n$emoji$emoji$emoji"
                        'M' -> "$emoji   $emoji\n$emoji$emoji$emoji$emoji\n$emoji $emoji $emoji\n$emoji   $emoji"
                        'N' -> "$emoji   $emoji\n$emoji$emoji  $emoji\n$emoji $emoji $emoji\n$emoji   $emoji"
                        'O' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji$emoji$emoji"
                        'P' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$\n$emoji\n$emoji"
                        'Q' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$emoji\n$emoji$emoji\n$emoji$emoji"
                        'R' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$\n$emoji $emoji \n$emoji  $emoji"
                        'S' -> "$emoji$emoji$emoji\n$emoji\n$emoji$emoji$emoji\n    $emoji\n$emoji$emoji$emoji"
                        'T' -> "$emoji$emoji$emoji\n  $emoji\n  $emoji\n  $emoji\n  $emoji"
                        'U' -> "$emoji  $emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji$emoji$emoji"
                        'V' -> "$emoji  $emoji\n$emoji  $emoji\n $emoji$emoji\n $emoji$emoji\n  $emoji"
                        'W' -> "$emoji   $emoji\n$emoji   $emoji\n$emoji$emoji$emoji\n$emoji$emoji$emoji\n$emoji $emoji"
                        'X' -> "$emoji  $emoji\n $emoji$emoji\n  $emoji\n $emoji$emoji\n$emoji  $emoji"
                        'Y' -> "$emoji  $emoji\n $emoji$emoji\n  $emoji\n  $emoji\n  $emoji"
                        'Z' -> "$emoji$emoji$emoji\n   $emoji\n  $emoji\n $emoji\n$emoji$emoji$emoji"
                        else -> "" // Default case for uppercase letters not defined
                    }
                    in '0'..'9' -> when (char) {
                        '0' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji$emoji$emoji"
                        '1' -> "  $emoji\n$emoji$emoji\n  $emoji\n  $emoji\n$emoji$emoji$emoji"
                        '2' -> "$emoji$emoji$emoji\n   $emoji\n$emoji$emoji$emoji\n$emoji\n$emoji$emoji$emoji"
                        '3' -> "$emoji$emoji$emoji\n   $emoji\n$emoji$emoji$emoji\n   $emoji\n$emoji$emoji$emoji"
                        '4' -> "$emoji  $emoji\n$emoji  $emoji\n$emoji$emoji$emoji\n   $emoji\n   $emoji"
                        '5' -> "$emoji$emoji$emoji\n$emoji\n$emoji$emoji$emoji\n   $emoji\n$emoji$emoji$emoji"
                        '6' -> "$emoji$emoji$emoji\n$emoji\n$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$emoji"
                        '7' -> "$emoji$emoji$emoji\n   $emoji\n   $emoji\n   $emoji\n   $emoji"
                        '8' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$emoji"
                        '9' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$emoji\n   $emoji\n$emoji$emoji$emoji"
                        else -> "" // Default case for digits not defined
                    }
                    else -> "" // Default case for characters not defined
                }
                append("$pattern\n\n") // Append each pattern with a newline
            }
        }
    }


    fun textToSingleEmoji(input: String, emoji: String): String {
        return buildString {
            for (char in input.uppercase()) {
                val pattern = when (char) {
                    'A' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$emoji\n$emoji  $emoji\n$emoji  $emoji"
                    'B' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$\n$emoji  $emoji\n$emoji$emoji$emoji"
                    'C' -> "$emoji$emoji$emoji\n$emoji\n$emoji\n$emoji\n$emoji$emoji$emoji"
                    'D' -> "$emoji$emoji$\n$emoji  $emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji$emoji$"
                    'E' -> "$emoji$emoji$emoji\n$emoji\n$emoji$emoji\n$emoji\n$emoji$emoji$emoji"
                    'F' -> "$emoji$emoji$emoji\n$emoji\n$emoji$emoji\n$emoji\n$emoji"
                    'G' -> "$emoji$emoji$emoji\n$emoji\n$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$emoji"
                    'H' -> "$emoji  $emoji\n$emoji  $emoji\n$emoji$emoji$emoji\n$emoji  $emoji\n$emoji  $emoji"
                    'I' -> "$emoji$emoji$emoji\n  $emoji\n  $emoji\n  $emoji\n$emoji$emoji$emoji"
                    'J' -> "$emoji$emoji$emoji\n    $emoji\n    $emoji\n$emoji  $emoji\n$emoji$emoji "
                    'K' -> "$emoji  $emoji\n$emoji $emoji \n$emoji$emoji  \n$emoji $emoji \n$emoji  $emoji"
                    'L' -> "$emoji\n$emoji\n$emoji\n$emoji\n$emoji$emoji$emoji"
                    'M' -> "$emoji   $emoji\n$emoji$emoji$emoji$emoji\n$emoji $emoji $emoji\n$emoji   $emoji"
                    'N' -> "$emoji   $emoji\n$emoji$emoji  $emoji\n$emoji $emoji $emoji\n$emoji   $emoji"
                    'O' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji$emoji$emoji"
                    'P' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$\n$emoji\n$emoji"
                    'Q' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$emoji\n$emoji$emoji\n$emoji$emoji"
                    'R' -> "$emoji$emoji$emoji\n$emoji  $emoji\n$emoji$emoji$\n$emoji $emoji \n$emoji  $emoji"
                    'S' -> "$emoji$emoji$emoji\n$emoji\n$emoji$emoji$emoji\n    $emoji\n$emoji$emoji$emoji"
                    'T' -> "$emoji$emoji$emoji\n  $emoji\n  $emoji\n  $emoji\n  $emoji"
                    'U' -> "$emoji  $emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji  $emoji\n$emoji$emoji$emoji"
                    'V' -> "$emoji  $emoji\n$emoji  $emoji\n $emoji$emoji\n $emoji$emoji\n  $emoji"
                    'W' -> "$emoji   $emoji\n$emoji   $emoji\n$emoji$emoji$emoji\n$emoji$emoji$emoji\n$emoji $emoji"
                    'X' -> "$emoji  $emoji\n $emoji$emoji\n  $emoji\n $emoji$emoji\n$emoji  $emoji"
                    'Y' -> "$emoji  $emoji\n $emoji$emoji\n  $emoji\n  $emoji\n  $emoji"
                    'Z' -> "$emoji$emoji$emoji\n   $emoji\n  $emoji\n $emoji\n$emoji$emoji$emoji"
                    else -> "" // Default case for characters not defined
                }
                append("$pattern\n\n") // Append each pattern with a newline
            }
        }
    }
}