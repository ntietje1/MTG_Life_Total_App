package model

data class VersionNumber(val value: String) {
    fun isHigherThan(other: VersionNumber): Boolean {
        val thisParts = this.value.split(".").map { it.toInt() }
        val otherParts = other.value.split(".").map { it.toInt() }
        return thisParts[0] > otherParts[0] || (thisParts[0] == otherParts[0] && thisParts[1] > otherParts[1]) || (thisParts[0] == otherParts[0] && thisParts[1] == otherParts[1] && thisParts[2] > otherParts[2])
    }

    fun isSame(other: VersionNumber): Boolean {
        val thisParts = this.value.split(".").map { it.toInt() }
        val otherParts = other.value.split(".").map { it.toInt() }
        return thisParts[0] == otherParts[0] && thisParts[1] == otherParts[1] && thisParts[2] == otherParts[2]
    }
    companion object {
        val zero = VersionNumber("0.0.0")
        val current = VersionNumber("1.9.0")
    }
}