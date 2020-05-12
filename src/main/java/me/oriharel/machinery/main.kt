package me.oriharel.machinery

import me.oriharel.machinery.structure.schematic.BuildTask
import me.oriharel.machinery.utilities.extensions.*
import org.bukkit.Location
import java.lang.invoke.SerializedLambda
import java.lang.reflect.Method
import java.util.*


fun main() {
    {
        print("dasda")
    }.serialize()?.deserializeAndExecute()
}





