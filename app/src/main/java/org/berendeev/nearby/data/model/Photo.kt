package org.berendeev.nearby.data.model

data class Photo(val prefix: String, val suffix: String) {
    fun getUrl(width: Int, height: Int): String {
        return prefix + width + "x" + height + suffix
    }
}
