package org.berendeev.nearby.data.model

sealed interface Issue
object NoInternetConnection : Issue
object UnknownDataLayerIssue : Issue
