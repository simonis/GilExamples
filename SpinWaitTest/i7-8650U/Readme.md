Measured at variable CPU frequency (with TurboBoost) up to 3900MHz:

```
# switch turbo boost on again
wrmsr --all 0x1a0 0x850089

# set variable CPU frequency
echo 400000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq
echo 400000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_min_freq
echo 400000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_min_freq
echo 400000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_min_freq
echo 400000 > /sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq
echo 400000 > /sys/devices/system/cpu/cpu5/cpufreq/scaling_min_freq
echo 400000 > /sys/devices/system/cpu/cpu6/cpufreq/scaling_min_freq
echo 400000 > /sys/devices/system/cpu/cpu7/cpufreq/scaling_min_freq
echo 4200000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq
echo 4200000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq
echo 4200000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_max_freq
echo 4200000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_max_freq
echo 4200000 > /sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq
echo 4200000 > /sys/devices/system/cpu/cpu5/cpufreq/scaling_max_freq
echo 4200000 > /sys/devices/system/cpu/cpu6/cpufreq/scaling_max_freq
echo 4200000 > /sys/devices/system/cpu/cpu7/cpufreq/scaling_max_freq
```

And at fixed frequency of 1900MHz:

```
# switch off turbo boost
wrmsr --all 0x1a0 0x4000850089

# set fixed CPU frequency
echo 1900000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq
echo 1900000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_min_freq
echo 1900000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_min_freq
echo 1900000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_min_freq
echo 1900000 > /sys/devices/system/cpu/cpu4/cpufreq/scaling_min_freq
echo 1900000 > /sys/devices/system/cpu/cpu5/cpufreq/scaling_min_freq
echo 1900000 > /sys/devices/system/cpu/cpu6/cpufreq/scaling_min_freq
echo 1900000 > /sys/devices/system/cpu/cpu7/cpufreq/scaling_min_freq
echo 1900000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq
echo 1900000 > /sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq
echo 1900000 > /sys/devices/system/cpu/cpu2/cpufreq/scaling_max_freq
echo 1900000 > /sys/devices/system/cpu/cpu3/cpufreq/scaling_max_freq
echo 1900000 > /sys/devices/system/cpu/cpu4/cpufreq/scaling_max_freq
echo 1900000 > /sys/devices/system/cpu/cpu5/cpufreq/scaling_max_freq
echo 1900000 > /sys/devices/system/cpu/cpu6/cpufreq/scaling_max_freq
echo 1900000 > /sys/devices/system/cpu/cpu7/cpufreq/scaling_max_freq
```

Used `taskset` to pin the two worker threads either to two Hyperthreads running on the same core (0,4) or on two different cores (0,1):

```
taskset -c 0,4 java -XX:+UnlockDiagnosticVMOptions -XX:DisableIntrinsic=_onSpinWait -jar SpinWaitTest.jar > i7-8650U_spinHint_none_ht_0_4_a.hgrm
taskset -c 0,4 java -jar SpinWaitTest.jar > i7-8650U_spinHint_pause_ht_0_4_a.hgrm
```

If running at variable frequency the CPU decreases the frequency one it gets too hot. This happens faster if the worker threads are running on different cores and if we don't use the PAUSE instruction emitted by the `_onSpinWait` intrinsic. Nevertheless the results are quite instable becuase of this.

The histogram files can be plotted with [`plotFiles.html`](./plotFiles.html) which is a local copy from the [HdrHistogram's online percentile plotter](http://hdrhistogram.github.io/HdrHistogram/plotFiles.html).
