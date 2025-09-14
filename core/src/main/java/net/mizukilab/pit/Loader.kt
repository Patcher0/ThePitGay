package net.mizukilab.pit

import cn.charlotte.pit.ThePit
import cn.charlotte.pit.ThePit.setApi
import net.mizukilab.pit.impl.PitInternalImpl

object Loader {

    @JvmStatic
    fun start() {
        ThePit.getInstance().apply {
            setApi(PitInternalImpl)
        }
        PitHook.init()
    }
}
