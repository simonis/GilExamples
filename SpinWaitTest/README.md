(See draft Thread.onSpinWait() JEP here: [https://github.com/giltene/GilExamples/blob/master/SpinWaitTest/JEPdraft.md])

#SpinWaitTest

A simple thread-to-thread communication latency test that measures and reports on the
behavior of thread-to-thread ping-pong latencies when spinning using a shared volatile
field, aling with the impact of using a Thread.onSpinWait() call on that latency behavior.

This test can be used to measure and document the impact of Thread.onSpinWait() behavior
on thread-to-thread communication latencies. E.g. when the two threads are pinned to
the two hardware threads of a shared x86 core (with a shared L1), this test will
demonstrate an estimate the best case thread-to-thread latencies possible on the
platform, if the latency of measuring time with System.nanoTime() is discounted
(nanoTime latency can be separtely estimated across the percentile spectrum using
the NanoTimeLatency test in this package).

###Example results plot (two threads on a shared core on a Xeon E5-2697v2): 
![example results] 

###Running:

This test is obviously intended to be run on machines with 2 or more vcores (tests on single vcore machines will
produce understandably outrageously long runtimes).
 
(If needed) Prepare the SpinWaitTest.jar by running:
 
    % mvn clean install

The simplest way to run this test is:

    % ${JAVA_HOME}/bin/java -jar SpinWaitTest.jar

Since the test is intended to highlight the benefits of an intrinsic Thread.onSpinWait(), using a prototype JDK
that that intrinsifies org.performancehints.Thread.onSpinWait() as a PAUSE instruction
(see links below), is obviously recommended. Using such a JDK, you can compare the output of:

    % ${JAVA_HOME}/bin/java -XX:-UseOnSpinWaitIntrinsic -jar SpinWaitTest.jar > intrinsicSpinHint.hgrm

and 
    
    % ${JAVA_HOME}/bin/java -XX:+UseOnSpinWaitIntrinsic -jar SpinWaitTest.jar > vanilla.hgrm

By plotting them both with [HdrHistogram's online percentile plotter] (http://hdrhistogram.github.io/HdrHistogram/plotFiles.html)

On moden x86-64 sockets, comparisions seem to show an 18-20nsec difference in the round trip latency.  

For consistent measurement, it is recommended that this test be executed while
binding the process to specific cores. E.g. on a Linux system, the following
command can be used:

    % taskset -c 23,47 ${JAVA_HOME}/bin/java -XX:+UseOnSpinWaitIntrinsic -jar SpinWaitTest.jar
    
To place the spinning threads on the same core. (the choice of cores 23 and 47 is specific
to a 48 vcore system where cores 23 and 47 represent two hyper-threads on a common core. You will want
to identofy a matching pair on your specific system).
 
###Plotting results:
 
SpinHintTrst outputs a percentile histogram distribution in [HdrHistogram](http://hdrhistogram.org)'s common
.hgrm format. This output can/shuld be redirected to an .hgrm file (e.g. vanilla.hgrm),
which can then be directly plotted using tools like [HdrHistogram's online percentile plotter] (http://hdrhistogram.github.io/HdrHistogram/plotFiles.html)

 
###Prototype intrinsifying implementations

A prototype OpenJDK implementation that implements org.performancehints.Thread.onSpinWait() as a PAUSE instruction
on x86-64 is available. Relevant Webrevs can be found here:  
- HotSpot: [http://ivankrylov.github.io/onspinwait/9b94.hs.webrev/]  
- JDK: [http://ivankrylov.github.io/onspinwait/9b94.jdk.webrev/] 

    Note: These full implementations are included for x86. Implementations on
    other platforms may choose to use the same instructions as [linux cpu_relax](http://lxr.free-electrons.com/ident?i=cpu_relax)
    and/or [plasma_spin](https://github.com/gstrauss/plasma/blob/master/plasma_spin.h)
      
A downloadable working x86 OpenJDK9-based JDK (which accepts an optional -XX:+UseOnSpinWaitIntrinsic flag to turn the
feature on) can be found here:   
- Linux: [https://goo.gl/v3G30r]  
- Mac: [https://goo.gl/LTlyRd]  
- Windows: [http://tinyurl.com/qzhsolv]  

#### Additional tests

This package includes some additional tests that can be used to explore the impact of Thread.onSpinWait()
beahvior: 

To test pure ping pong throughput test with no latency measurement overhead:

    % ${JAVA_HOME}/bin/java -XX:+UseOnSpinWaitIntrinsic -cp SpinWaitTest.jar SpinHintThroughput

To test the behavior of thread ping-pong latencies when loops alternate between using Thread.onSpinWait()
(for the duration of the loop) and not using it (for the duration of the next loop):

    % ${JAVA_HOME}/bin/java -XX:+UseOnSpinWaitIntrinsic -cp SpinWaitTest.jar AlternatingOnSpinWaitTest
    
To document the latency of measure time with System.nanoTime() (so that it can be discounted when
observing ping pong latecies in the latency measuring tests):

    % ${JAVA_HOME}/bin/java -XX:+UseOnSpinWaitIntrinsic -cp SpinWaitTest.jar NanoTimeLatency
    
[example results]:https://raw.github.com/giltene/GilExamples/master/SpinWaitTest/SpinLoopLatency_E5-2697v2_sharedCore.png "Example Results on E5-2697v2"
