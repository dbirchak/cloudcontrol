/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 QAware GmbH, Munich, Germany
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
@file:JvmName("CloudControlApplication")

package de.qaware.oss.cloud.control

import org.apache.deltaspike.cdise.api.CdiContainerLoader
import org.slf4j.bridge.SLF4JBridgeHandler
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.context.Dependent

/**
 * The Cloud Control main application class. Boots the CDI context and
 * initializes the MIDI environment and device properly.
 */
fun main(args: Array<String>) {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    // this seems to be required at least under Windows
    System.setProperty("javax.sound.midi.Transmitter", "com.sun.media.sound.MidiInDeviceProvider#Launch Control")
    System.setProperty("javax.sound.midi.Receiver", "com.sun.media.sound.MidiOutDeviceProvider#Launch Control")

    // get cluster service property or initialize default
    var orchestrator = System.getProperty("cloud.orchestrator")
    if (orchestrator == null) {
        orchestrator = "kubernetes"
        System.setProperty("cluster.orchestrator", orchestrator)
    }

    // get the current CDI container
    val cdiContainer = CdiContainerLoader.getCdiContainer()
    cdiContainer.boot()

    // and initialize the CDI container control
    val contextControl = cdiContainer.contextControl;
    contextControl.startContext(Dependent::class.java)
    contextControl.startContext(ApplicationScoped::class.java)

    // ensure we shutdown nicely on exit
    Runtime.getRuntime().addShutdownHook(Thread() {
        cdiContainer.shutdown()
    })
}