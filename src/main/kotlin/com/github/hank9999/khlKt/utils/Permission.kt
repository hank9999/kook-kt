package com.github.hank9999.khlKt.utils

import com.github.hank9999.khlKt.utils.types.Permissions

class Permission(var permValue: Int = 148691464) {

    fun checkPermission(perm: Permissions): Boolean {
        val value = 1 shl perm.bitValue
        return (permValue and value) == value
    }

    fun getPermissions(): MutableList<Permissions> {
        val perms = mutableListOf<Permissions>()
        Permissions.values().forEach { if (checkPermission(it)) perms.add(it) }
        return perms
    }

    fun addPermission(perm: Permissions): Permission {
        permValue = permValue or (1 shl perm.bitValue)
        return this
    }

    fun delPermission(perm: Permissions): Permission {
        permValue = permValue xor (1 shl perm.bitValue)
        return this
    }
}