# WearTimer
A Stopwatch/Timer for the Android Wear platform

Known Issues:

The times shown on wear and mobile are not the same because the stopwatch-implementation is based on System.upTimeMillis(), which is different for both devices. Have to find a way to synchronize the startTime between the devices.

