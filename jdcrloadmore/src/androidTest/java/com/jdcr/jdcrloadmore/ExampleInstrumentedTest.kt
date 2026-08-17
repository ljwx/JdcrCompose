package com.jdcr.jdcrloadmore

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/** 在 Android 设备上执行的基础包名测试。 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // 获取被测试库的 Context。
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.jdcr.jdcrloadmore.test", appContext.packageName)
    }
}
