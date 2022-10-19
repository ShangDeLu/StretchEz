package me.shangdelu.stretchez.raspberryPi

data class RaspberryItem(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var userDetails: UserDetails? = UserDetails("", "")
)